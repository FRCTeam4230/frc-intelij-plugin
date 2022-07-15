/*
 * Copyright 2015-2022 the original author or authors.
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
import com.intellij.openapi.diagnostic.ExceptionWithAttachments
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.RuntimeExceptionWithAttachments
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
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
import net.javaru.iip.frc.notify.FrcNotifyType
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.util.FrcSystemConfigs
import net.javaru.iip.frc.util.frcPluginPrimaryVersion
import net.javaru.iip.frc.util.frcPluginVersion
import net.javaru.iip.frc.util.getPluginResourceAsStream
import net.javaru.iip.frc.util.insertBeforeLast
import java.awt.Component
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*


object FrcErrorReportSubmitter: ErrorReportSubmitter()
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
                isEnableExternalConfiguration = false // We disable since we don't load any
                dsn = sentryDsn
                // We use just the primary release (i.e. 1.2.3 and not 1.2.3-2020.1) so the sentry tool can track fixed versions, regressions, and such properly
                release = frcPluginPrimaryVersion ?: "<undetermined>"
                // short version is basically hte major version, such as 2021.3 for all 2021.3 versions such as 2021.3.3, 2021.3.1, 2021.3, etc.
                // we use it as the environment since in most cases we simply need to differentiate between major versions
                environment = ApplicationInfo.getInstance().shortVersion
                // We don't want to get people's system names (for privacy reasons), and we don't really need it.
                // But if you do not set it, Sentry sets it automatically
                isAttachServerName = false
                isSendDefaultPii = false // whether to send Personal Identifiable Information (PII)
                // When enabled, stack traces are automatically attached to all messages logged. Stack traces are always
                // attached to exceptions; however, when this option is set, stack traces are also sent with messages.
                // This option, for instance, means that stack traces appear next to all log messages.
                // https://docs.sentry.io/platforms/java/configuration/options/#attach-stacktrace
                isAttachStacktrace = true
                inAppIncludes.addAll(mutableListOf("net.javaru", "io.javaru", "org.javaru"))
                enableUncaughtExceptionHandler = false // when enabled, it catches a lot of noise from the IDE and other Plugins that we can't do anything about
                isEnableNdk = false // Android Native Development Kit: https://docs.sentry.io/platforms/android/using-ndk/
                isEnableScopeSync = false // the Java to NDK Scope sync
                isEnableScopeSync = false // the Java to NDK Scope sync
                isEnableAutoSessionTracking = false // web sessions; n/a for us; but is on by default
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
            Sentry.setTag("release.full", frcPluginVersion ?: "<undetermined>")
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
                try
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
                            scope.setExtraSafely("event.type", "ErrorReportSubmitter")
                            val throwable: Throwable? = if (ideaEvent is IdeaReportingEvent)
                            {
                                scope.addThrowableAsAttachment(ideaEvent.throwable, "ideaEvent.throwable.txt")
                                ideaEvent.data.throwable
                            }
                            else
                            {
                                ideaEvent.throwable
                            }
                            scope.addThrowableAsAttachment(throwable)
                            val sentryEvent = SentryEvent(throwable)
                            sentryEvent.level = SentryLevel.ERROR
                            sentryEvent.setMessageSafely(scope, ideaEvent, throwable, additionalInfo)
                            sentryEvent.setStacktraceHashes(throwable)
                            try
                            {
                                // For some reason calling
                                //     if (ideaEventData is LogMessage)
                                // always returns false, even when it is a LogMessage. So we just do the
                                // cast, and catch any exception since in most cases it is a LogMessage
                                val ideaEventData = ideaEvent.data
                                // Note: Max attachment size is 20 MB
                                (ideaEventData as LogMessage).allAttachments.forEach{
                                    scope.addIdeaAttachment(it, "eventAttachment-")
                                }
                            }
                            catch (e: Exception)
                            {
                                LOG.debug("Could not add attachment: $e")
                            }

                            scope.addIdeaExceptionAttachments(throwable)
                            val sentryId = Sentry.captureEvent(sentryEvent)
                            logReportSubmission(sentryId)
                        }
                    }
                }
                catch (t: Throwable)
                {
                    LOG.info("[FRC] Could not report error. Reason: $t")
                }

                // We just always say thanks regardless of success
                ApplicationManager.getApplication().invokeLater {
                    FrcNotifyType.ERROR_REPORT_SUBMITTER.builder()
                        .withContent(FrcBundle.message("frc.notification.errorReportSubmitter.submitted.text"))
                        .withFrcPrefixedTitle(FrcBundle.message("frc.notification.errorReportSubmitter.submitted.title"))
                        .build()                               
                        .apply {
                            isImportant = false
                        }.notify(project)
                    consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE))
                }
            }
        }.queue()
        return true
    }



    fun submitReportableEvent(event: ReportableEvent)
    {
        try
        {
            object : Backgroundable(event.project, "Log issue")
            {
                override fun run(indicator: ProgressIndicator)
                {
                    try
                    {
                        Sentry.withScope { scope: Scope ->
                            scope.setExtraSafely("event.type", "FrcReportableEvent")
                            scope.setExtraSafely("last.action", event.lastActionId)
                            scope.setExtraSafely("correlationId", event.correlationId)
                            event.additionalData?.forEach { scope.setExtraSafely(it.key, it.value) }
                            scope.addThrowableAsAttachment(event.throwable)
                            scope.addIdeaExceptionAttachments(event.throwable)
                            val sentryEvent = SentryEvent(event.throwable)// Is null safe
                            sentryEvent.setStacktraceHashes(event.throwable)
                            sentryEvent.level = event.level
                            sentryEvent.setMessageSafely(scope, event.messageOrAdditionalInfo, event.throwable, event.messageAddendum)
                            sentryEvent.setTagSafely("last.action", event.lastActionId)
                            sentryEvent.setTagSafely("correlationId", event.correlationId)
                            event.attachments?.forEach { scope.addAttachment(it) }
                            val sentryId = Sentry.captureEvent(sentryEvent)
                            logReportSubmission(sentryId)
                        }
                    }
                    catch (t: Throwable)
                    {
                        LOG.info("[FRC] Could not report ReportableEvent [correlationId: ${event.correlationId}]. Reason: $t")
                    }
                }
            }.queue()
        }
        catch (t: Throwable)
        {
            LOG.info("[FRC] Could not process ReportableEvent [correlationId: ${event.correlationId}]. Reason: $t")
        }
    }

    private fun Scope.addThrowableAsAttachment(throwable: Throwable?, attachmentName: String = "the.throwable.txt")
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

    private fun Scope.setExtraSafely(key: String, value: Any?)
    {
        if (value != null) this.setExtra(key, value.toString())
    }

    private fun Scope.addIdeaExceptionAttachments(throwable: Throwable?)
    {

        if (throwable is ExceptionWithAttachments)
        {
            val prefix = "exceptionAttachment-"
            throwable.attachments.forEach {
                this.addIdeaAttachment(it, prefix)
            }
        }
    }

    private fun Scope.addIdeaAttachment(ideaAttachment: com.intellij.openapi.diagnostic.Attachment, prefix: String)
    {
        this.addAttachment(Attachment(ideaAttachment.bytes, "$prefix${ideaAttachment.path}"))

        if (String(ideaAttachment.bytes).trim() != ideaAttachment.displayText.trim())
        {
            val path = "$prefix${ideaAttachment.path.insertBeforeLast('.', "-displayText")}"
            this.addAttachment(Attachment(ideaAttachment.bytes, path))
        }
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
            this.setTagSafely("ex.hash.full", fullHash)
            this.setTagSafely("ex.hash.limited", limitedHash)
            this.setTagSafely("ex.hash.single", singleLine)
        }
    }

    private fun SentryEvent.setTagSafely(key:String, value: String?)
    {
        if (value != null) this.setTag(key, value)
    }

    private fun SentryEvent.setMessageSafely(scope: Scope, ideaEvent: IdeaLoggingEvent, throwable: Throwable?, additionalInfo: String?): Message?
    {
        val additionalInfoClean = additionalInfo ?: "<none>"

        val sb = StringBuilder()
        sb.append("\u2022 IDEA Logging Event Message: ${ideaEvent.message}\n")
        sb.append("\u2022 Additional Info / User Comments:  $additionalInfoClean\n")

        if (ideaEvent is IdeaReportingEvent)
        {
            if (ideaEvent.message != ideaEvent.originalMessage)
            {
                sb.append("\u2022 Original Message: ${ideaEvent.originalMessage}\n")
            }

            scope.addAttachment(Attachment(ideaEvent.originalThrowableText.toByteArray(), "originalCausingThrowableStacktrace.txt"))
        }
        sb.addThrowableInfo(throwable)
        return this.setMessageSafely(scope, sb.toString())
    }


    private fun SentryEvent.setMessageSafely(scope: Scope, eventMessage: String?, throwable: Throwable?, messageAddendum: Map<String, String?>? = emptyMap()): Message?
    {
        val sb = StringBuilder()
        if (eventMessage!= null) sb.append("\u2022 Event Message: ").append(eventMessage).append("\n")
        sb.addThrowableInfo(throwable)

        if (!messageAddendum.isNullOrEmpty()) {
            sb.append("\u2022 Addendum:\n")
            messageAddendum.forEach {
                sb.append("  \u25E6 ").append(it.key).append(": ").append(it.value).append("\n")
            }
        }
        return if (sb.isNotEmpty()) this.setMessageSafely(scope, sb.toString()) else null
    }


    private fun SentryEvent.setMessageSafely(scope: Scope, theMessage: String?) : Message?
    {
        return if (theMessage != null && theMessage.isNotBlank())
        {
            this.message = Message().apply {
                message = theMessage
            }
            if (theMessage.length > 8192) {
                // Messages over 8,192 characters are truncated, so we add the full message as an attachment
                scope.addAttachment(Attachment(theMessage.toByteArray(), "non-truncated_message.txt"))
            }
            this.message
        }
        else
        {
            null
        }
    }

    private fun StringBuilder.addThrowableInfo(throwable: Throwable?)
    {
        if (throwable != null)
        {
            if (throwable.message != null)
            {
                append("\u2022 Throwable Message: ").append(throwable.message).append("\n")
            }
            if (throwable is RuntimeExceptionWithAttachments && throwable.userMessage != null)
            {
                append("\u2022 RuntimeExceptionWithAttachments Additional Msg: ").append(throwable.userMessage).append("\n")
            }
        }
    }

    private fun logReportSubmission(sentryId: SentryId)
    {
        val msg = if (sentryId != SentryId.EMPTY_ID)
            "[FRC] Issue report submitted as: $sentryId"
        else
            "[FRC] Issue report submission failed."
        LOG.info(msg)
    }
}

/**
 * @param correlationId An ID that can be used to correlate the reported event to the action of location that caused it
 * @param project The IntelliJ IDEA project. Used for creating the background task. Is *not* reported.
 * @param throwable Optional throwable related to the event
 * @param level The level to report the event as
 * @param lastActionId The ID of the last action to be invoked, obtain via: `IdeaLogger.ourLastActionId`
 * @param messageOrAdditionalInfo The message ro information to report with the event
 * @param messageAddendum An optional map of key:value pairs that may be useful in troubleshooting, such as variable names and values
 * @param additionalData An optional map of key:value pairs to be added to the Sentry Report as extras.
 * @param attachments Collection of attachments to add to the report; note max attachment size is 20MB
 */
data class ReportableEvent(
    val correlationId: String? = null,
    val project: Project? = null,
    val throwable: Throwable? = null,
    val level: SentryLevel = SentryLevel.WARNING,
    val lastActionId: String? = null,
    val messageOrAdditionalInfo: String? = IdeaLogger.ourLastActionId,
    val messageAddendum: Map<String, String?>? = emptyMap(),
    val additionalData: Map<String, Any?>? = emptyMap(),
    val attachments: Collection<Attachment>? = emptyList()
                          )