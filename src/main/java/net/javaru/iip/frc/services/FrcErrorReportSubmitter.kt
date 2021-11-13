/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.services

import com.intellij.diagnostic.IdeErrorsDialog
import com.intellij.diagnostic.IdeaReportingEvent
import com.intellij.diagnostic.LogMessage
import com.intellij.ide.DataManager
import com.intellij.idea.IdeaLogger
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.Consumer
import io.sentry.Attachment
import io.sentry.Scope
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import io.sentry.protocol.SentryId
import io.sentry.protocol.User
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.i18n.FrcBundle
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.util.FrcSystemConfigs
import net.javaru.iip.frc.util.frcPluginVersion
import net.javaru.iip.frc.util.getPluginResourceAsStream
import java.awt.Component
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*


class FrcErrorReportSubmitter: ErrorReportSubmitter()
{
    private val LOG = logger<FrcErrorReportSubmitter>()

    init
    {
        val useQA = FrcSystemConfigs.ErrorReportSubmitterUseQa.value
        val key = if (useQA) "sentry.dsn.test.and.qa" else "sentry.dsn.prod"
        LOG.debug("Sentry init: useQA: $useQA  DSN property key: $key")
        val sentryDsn = Properties().apply {
            load(getPluginResourceAsStream("services/frc-plugin-tokens.properties"))
        }.getProperty(key, "DSN_NOT_FOUND").also {
            if (it == "DSN_NOT_FOUND")
            {
                val msg = "Could not load Sentry DSN from frc-plugin-tokens.properties"
                if (FrcPluginGlobals.IS_IN_FRC_INTERNAL_MODE) LOG.error(msg) else LOG.warn(msg)
            }
            else if (FrcPluginGlobals.IS_IN_FRC_INTERNAL_MODE)
            {
                LOG.debug("Sentry DSN set to: $it")
            }
        }

        Sentry.init { options ->
            options.apply {
                dsn = sentryDsn
                release = frcPluginVersion ?: "<undetermined>"
                // short version is basically hte major version, such as 2021.3 for all 2021.3 versions such as 2021.3.3, 2021.3.1, 2021.3, etc.
                // we use it as the environment since in most cases we simply need to differentiate between major versions
                environment = ApplicationInfo.getInstance().shortVersion
                // We don't want to get people's system names (for privacy reasons), and we don't really need it.
                // But if you do not set it, Sentry sets it automatically
                isAttachServerName = false
                // When enabled, stack traces are automatically attached to all messages logged. Stack traces are always
                // attached to exceptions; however, when this option is set, stack traces are also sent with messages.
                // This option, for instance, means that stack traces appear next to all log messages.
                // https://docs.sentry.io/platforms/java/configuration/options/#attach-stacktrace
                isAttachStacktrace = true
                inAppIncludes.addAll(mutableListOf("net.javaru", "io.javaru", "org.javaru"))
                setDebug(useQA)
                // This applies to performance monitoring, which we are not using at this time
                //tracesSampleRate = 1.0


                // AN example of a BeforeSendCallback from the docs
//                beforeSend = BeforeSendCallback { event: SentryEvent, hint: Any? ->
//                    // Drop an event altogether:
//                    if (event.getTag("SomeTag") != null)
//                    {
//                        null
//                    }
//                    else
//                    {
//                        event
//                    }
//                }

            }
        }.also {
            Sentry.setTag("ide.build", ApplicationInfo.getInstance().build.asString())
            Sentry.setTag("ide.version", ApplicationInfo.getInstance().fullVersion)
            Sentry.setTag("ide.code", ApplicationInfo.getInstance().build.productCode)
            Sentry.setTag("ide.name", "${ApplicationInfo.getInstance().fullApplicationName} ${ApplicationNamesInfo.getInstance().editionName}")
            Sentry.setTag("os", SystemInfo.getOsNameAndVersion())
            val frcApplicationSettings = FrcApplicationSettings.getInstance()
            Sentry.setTag("frc.team", frcApplicationSettings.teamNumber.toString())
            val niid = frcApplicationSettings.niid
            Sentry.setTag("niid", niid)
            Sentry.setUser(User().apply {
                this.id = niid
            })
        }
    }


    override fun getReportActionText() = "Report to FRC Plugin Author"

    // TODO: add a privacy policy
    override fun getPrivacyNoticeText(): String? = null

    override fun submit(events: Array<out IdeaLoggingEvent>,
                        additionalInfo: String?,
                        parentComponent: Component,
                        consumer: Consumer<in SubmittedReportInfo>): Boolean
    {
        val lastActionId = IdeaLogger.ourLastActionId ?: "<unknown>"
        val context = DataManager.getInstance().getDataContext(parentComponent)
        val project = CommonDataKeys.PROJECT.getData(context)

        object : Backgroundable(project, "Sending error report")
        {
            override fun run(indicator: ProgressIndicator)
            {
                for (ideaEvent in events)
                {
                    // Using withScope allows us to send data with one specific event.
                    // Do not confuse with Sentry.configureScope { scope -> . . . }
                    // configureScope changes the current active scope, all successive calls to configure-scope will keep the changes.
                    // withScope however creates a clone of the current scope and will stay isolated until the function call is completed.
                    // https://docs.sentry.io/platforms/java/enriching-events/scopes/#local-scopes
                    Sentry.withScope { scope: Scope ->
                        // Set the last action ID as it might be useful for debugging
                        scope.setExtraSafely("last.action", lastActionId)
                        scope.setExtraSafely("plugin.name", IdeErrorsDialog.getPlugin(ideaEvent)?.name)
                        scope.setExtraSafely("plugin.id", IdeErrorsDialog.getPlugin(ideaEvent)?.pluginId?.idString)
                        val throwable: Throwable? = if (ideaEvent is IdeaReportingEvent)
                        {
                            scope.addThrowableAsAttachment(ideaEvent.throwable, "ideaEvent.throwable.txt")
                            ideaEvent.data.throwable
                        }
                        else
                        {
                            ideaEvent.throwable
                        }
                        scope.addThrowableAsAttachment(throwable, "the.throwable.txt")
                        val sentryEvent = SentryEvent(throwable)
                        sentryEvent.level = SentryLevel.ERROR
                        sentryEvent.setMessageSafely(scope, ideaEvent, additionalInfo)
                        sentryEvent.setStacktraceHashes(throwable)
                        try
                        {
                            // For some reason calling
                            //     if (ideaEventData is LogMessage)
                            // always returns false, even when it is a LogMessage. So we just do the
                            // cast, and catch any exception since in most cases it is a LogMessage
                            val ideaEventData = ideaEvent.data
                            val attachments = (ideaEventData as LogMessage).allAttachments
                            for (ideaAttachment in attachments)
                            {
                                scope.addAttachment(Attachment(ideaAttachment.bytes, ideaAttachment.path))
                            }
                        }
                        catch (e: Exception)
                        {
                            LOG.debug("Could not add attachment: $e")
                        }
                        logReportSubmission(sentryEvent)
                    }
                }

                // We just always say thanks regardless of success
                ApplicationManager.getApplication().invokeLater {
                    FrcNotifications.createNotification(
                        FrcNotificationType.ERROR_REPORT_SUBMITTER,
                        FrcBundle.message("frc.notification.errorReportSubmitter.submitted.text"),
                        FrcBundle.message("frc.notification.errorReportSubmitter.submitted.title"),
                                                       )
                        .apply {
                            isImportant = false
                        }.notify(project)
                    consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE))
                }
            }
        }.queue()
        return true
    }

    private fun Scope.addThrowableAsAttachment(throwable: Throwable?, attachmentName: String)
    {
        if (throwable != null)
        {
            val sw = StringWriter()
            PrintWriter(sw).use {
                throwable.printStackTrace(it)
                it.flush()
            }
            addAttachment(Attachment(sw.toString().toByteArray(), attachmentName))
        }
    }

    private fun Scope.setExtraSafely(key: String, value: String?)
    {
        if (value != null) this.setExtra(key, value)
    }

    private data class StacktraceHashes(val fullHash: String, val limitedHash: String, val singleHash: String)
    {
        companion object
        {
            fun create(t: Throwable?): StacktraceHashes
            {
                if (t == null)
                    return StacktraceHashes("0", "0", "0")

                val stackTrace = t.stackTrace
                return StacktraceHashes(
                    Arrays.hashCode(stackTrace).toString(16),
                    stackTrace.take(5).toTypedArray().contentHashCode().toString(16),
                    stackTrace.take(1).toTypedArray().contentHashCode().toString(16),
                                       )
            }
        }
    }

    private fun SentryEvent.setStacktraceHashes(throwable: Throwable?) {
        if (throwable != null) {
            val (fullHash, limitedHash, singleLine) = StacktraceHashes.create(throwable)
            this.setTag("ex.hash.full", fullHash)
            this.setTag("ex.hash.limited", limitedHash)
            this.setTag("ex.hash.single", singleLine)
        }
    }

    private fun SentryEvent.setMessageSafely(scope: Scope, ideaEvent: IdeaLoggingEvent, additionalInfo: String?): Message?
    {
        val additionalInfoClean = additionalInfo ?: "<none entered>"

        var detailedMessage =
            """Event Message:    ${ideaEvent.message}
              |Additional Info:  $additionalInfoClean
              |""".trimMargin()

        if (ideaEvent is IdeaReportingEvent)
        {
            if (ideaEvent.message != ideaEvent.originalMessage)
            {
                detailedMessage += "Original Message: ${ideaEvent.originalMessage}"
            }

            scope.addAttachment(Attachment(ideaEvent.originalThrowableText.toByteArray(), "originalCausingThrowableStacktrace.txt"))
        }
        return this.setMessageSafely(scope, detailedMessage)
    }

    private fun SentryEvent.setMessageSafely(scope: Scope, eventMessage: String?, additionalInfo: String?): Message?
    {
        if(additionalInfo == null && eventMessage == null) {
            return null
        }

        val detailedMessage =
            """Event Message:   $eventMessage
              |Additional Info: ${additionalInfo ?: "<none entered>"}""".trimMargin()
        return this.setMessageSafely(scope, detailedMessage)
    }

    private fun SentryEvent.setMessageSafely(scope: Scope, messageOrAdditionalInfo: String?) : Message?
    {
        return if (messageOrAdditionalInfo != null)
        {
            this.message = Message().apply {
                message = messageOrAdditionalInfo
            }
            if (messageOrAdditionalInfo.length > 8192) {
                // Messages over 8,192 characters are truncated, so we add the full message as an attachment
                scope.addAttachment(Attachment(messageOrAdditionalInfo.toByteArray(), "non-truncated_message.txt"))
            }
            this.message
        }
        else
        {
            null
        }
    }

    private fun logReportSubmission(sentryEvent: SentryEvent)
    {
        val sentryId = Sentry.captureEvent(sentryEvent)
        val msg = if (sentryId != SentryId.EMPTY_ID)
            "[FRC] Issue report submitted as: $sentryId"
        else
            "[FRC] Issue report submission failed."
        LOG.info(msg)
    }
}