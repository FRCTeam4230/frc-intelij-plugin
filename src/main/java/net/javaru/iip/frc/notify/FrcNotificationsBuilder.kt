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

@file:Suppress("UnstableApiUsage", "RemoveEmptyClassBody")

package net.javaru.iip.frc.notify

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.util.NlsContexts.NotificationContent
import com.intellij.openapi.util.NlsContexts.NotificationTitle
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.i18n.FrcBundle
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.i18n.FrcMessageKey
import net.javaru.iip.frc.notify.FrcNotifications.FrcTitle
import org.jetbrains.annotations.PropertyKey
import java.util.*


object FrcNotificationsBuilder
{
    @JvmStatic
    fun builder(): TypeStep = Steps()

    @JvmStatic
    fun builder(type: FrcNotifyType): ContentStep = Steps().withFrcType(type)
    
    class Steps internal constructor(): TypeStep, TitleStep, SubtitleStep, ContentStep, ActionsStep, AddActionStep, BuildStep
    {
        private val LOG = logger<Steps>()
        private var type: FrcNotifyType = FrcNotifyType.GENERAL_INFO
        private var content: String? = null
        private var normalizedTitle: String = ""
        private var subtitle: String? = null
        private val actions: MutableCollection<NotificationAction> = mutableListOf()

        enum class TitleType {NoTitle, PrefixedTitle, NonPrefixedTitle}
        
        override fun withFrcType(type: FrcNotifyType): ContentStep = this.apply { this.type = type }
        
        override fun withContent(@NotificationContent content: String): TitleStep = this.apply { this.content = content } 
        
        // region TitleSteps
        override fun withFrcTitle(): SubtitleStep = titleSettingImpl(FrcTitle, TitleType.PrefixedTitle)
        override fun withFrcPrefixedTitleKey(key: String, vararg params: Any): SubtitleStep = withFrcPrefixedTitle(FrcMessageKey.of(key, *params))
        override fun withFrcPrefixedTitle(messageKey: FrcMessageKey): SubtitleStep = titleSettingImpl( messageKey, TitleType.PrefixedTitle)
        override fun withFrcPrefixedTitle(@NotificationTitle title: String): SubtitleStep = titleSettingImpl( title, TitleType.PrefixedTitle)
        override fun withNonPrefixedTitleKey(key: String, vararg params: Any): SubtitleStep = withNonPrefixedTitle(FrcMessageKey.of(key, *params))
        override fun withNonPrefixedTitle(messageKey: FrcMessageKey): SubtitleStep = titleSettingImpl( messageKey, TitleType.NonPrefixedTitle)
        override fun withNonPrefixedTitle(@NotificationTitle title: String): SubtitleStep = titleSettingImpl( title, TitleType.NonPrefixedTitle)
        override fun withNoTitle(): ActionsStep = titleSettingImpl( "", TitleType.NoTitle)
        
       
        private fun titleSettingImpl(titleKey: FrcMessageKey, titleType: TitleType): Steps = titleSettingImpl(message(titleKey), titleType)
        
        private fun titleSettingImpl(@NotificationTitle title: String, titleType: TitleType): Steps
        {
            normalizedTitle = when(titleType)
            {
                TitleType.NoTitle -> ""
                TitleType.NonPrefixedTitle -> title
                TitleType.PrefixedTitle -> when {
                    title.isBlank() -> FrcTitle
                    title == FrcTitle || title.startsWith(FrcTitle) -> title
                    else -> "$FrcTitle: $title"
                }
            }
            return this
        }
        // endregion TitleSteps
        
        override fun withSubtitle(subtitle: String?): ActionsStep = this. apply {this.subtitle = subtitle }
        
        override fun withNoActions(): BuildStep { return this }
        override fun withAction(action: NotificationAction): AddActionStep = this.apply { actions.add(action) }
        override fun withActions(vararg actions: NotificationAction): BuildStep = this.apply { this.actions.addAll(actions) }
        override fun withActions(actions: Collection<NotificationAction>): BuildStep = this.apply { this.actions.addAll(actions) }
        override fun noMoreActions(): BuildStep { return this }
        
        override fun withAction(text: String, expiring: Boolean, action: (actionEvent: AnActionEvent, notification: Notification) -> Unit): AddActionStep
        {
            this.actions.add(object : NotificationAction(text)
            {
                override fun actionPerformed(e: AnActionEvent, notification: Notification)
                {
                    if (expiring) notification.expire()
                    action(e, notification)
                }
            })
            return this
        }
        
        override fun withActionBasic(@NotificationContent text: String, expiring: Boolean, action: (actionEvent: AnActionEvent) -> Unit): AddActionStep
        {
            this.actions.add(object : NotificationAction(text)
            {
                override fun actionPerformed(e: AnActionEvent, notification: Notification)
                {
                    if (expiring) notification.expire()
                    action(e)
                }
            })
            return this 
        }
       
        override fun build(): Notification
        {
            if (content == null)
            {
                LOG.warn("[FRC] content for notification was null. No content will be displayed in notification")
                content = ""
            }
            
            return Notification(
                /* groupId = */ type.groupId,
                /* title =   */ normalizedTitle,
                /* content = */ content ?: "",
                /* type =    */ type.notificationType
                               ).also {
                it.subtitle = subtitle
                it.addActions(actions)
            }
        }

        override fun notifyAllProject(notifyFrcProjectsOnly: Boolean, uuidKey: UUID): UUID
        {
            ProjectManager.getInstance().openProjects.filter {
                if (notifyFrcProjectsOnly)
                    it.isFrcFacetedProject()
                else
                    true
            }.forEach {
                val notification = build().apply {
                    whenExpired { FrcSharedNotificationTracker.expireAll(uuidKey) }
                }.notifyFluent(it)
                FrcSharedNotificationTracker.add(uuidKey, it, notification)
            }
            return uuidKey
        }
        
        override fun notifyAllProjectsViaBalloon(notifyFrcProjectsOnly: Boolean, uuidKey: UUID): UUID
        {
            ProjectManager.getInstance().openProjects.filter {
                if (notifyFrcProjectsOnly) it.isFrcFacetedProject()
                else true
            }.forEach {
                val balloonResult = build()
                    .whenExpired { FrcSharedNotificationTracker.expireAll(uuidKey) }
                    .notifyViaBalloon(it)
                FrcSharedNotificationTracker.add(uuidKey, it, balloonResult)
                balloonResult.balloon?.addListener(object : JBPopupListener
                                                   {
                                                       override fun onClosed(event: LightweightWindowEvent)
                                                       {
                                                           FrcSharedNotificationTracker.expireAll(uuidKey)
                                                       }
                                                   })
            }
            return uuidKey
        }
    }

    @Suppress("unused")
    interface TypeStep
    {
        fun withFrcType(type: FrcNotifyType): ContentStep
    }

    @Suppress("unused")
    interface ContentStep
    {
        /** Notification Builder step that sets the content of the notification via an i18n [FrcMessageKey]. */
        fun withContent(messageKey: FrcMessageKey): TitleStep = withContent(message(messageKey))

        /** Notification Builder step that sets the content of the notification. */
        fun withContent(@NotificationContent content: String): TitleStep

        /** Notification Builder step that sets the content of the notification via an i18n bundleKey. */
        fun withContentKey(@PropertyKey(resourceBundle = FrcBundle.bundleName) key: String, vararg params: Any) = withContent(FrcMessageKey.of(key, *params))
    }

    @Suppress("unused")
    interface TitleStep: CompletableStep
    {
        /** Sets the notification's title to "FRC". */
        fun withFrcTitle(): SubtitleStep
        
        /** Sets the notification's title to the provided title prefixed with "FRC: ". For example: "FRC: My Title". */
        fun withFrcPrefixedTitleKey(@PropertyKey(resourceBundle = FrcBundle.bundleName) key: String, vararg params: Any): SubtitleStep
        
        /** Sets the notification's title to the provided title prefixed with "FRC: ". For example: "FRC: My Title". */
        fun withFrcPrefixedTitle(messageKey: FrcMessageKey): SubtitleStep
        
        /** Sets the notification's title to the provided title prefixed with "FRC: ". For example: "FRC: My Title". */
        fun withFrcPrefixedTitle(@NotificationTitle title: String): SubtitleStep
        
        /** 
         * Sets the notification's title to provided title. 
         * @see withFrcPrefixedTitle 
         */
        fun withNonPrefixedTitleKey(@PropertyKey(resourceBundle = FrcBundle.bundleName) key: String, vararg params: Any): SubtitleStep
        
        /** 
         * Sets the notification's title to provided title. 
         * @see withFrcPrefixedTitle 
         */
        fun withNonPrefixedTitle(messageKey: FrcMessageKey): SubtitleStep
        
        /** 
         * Sets the notification's title to provided title. 
         * @see withFrcPrefixedTitle 
         */
        fun withNonPrefixedTitle(@NotificationTitle title: String): SubtitleStep

        /** Will create the notification without a title and subsequently no subtitle (since a subtitle without a title makes no sense). */
        fun withNoTitle(): ActionsStep
    }

    @Suppress("unused")
    interface SubtitleStep: CommonActionsSteps, CompletableStep
    {
        fun withNoSubTitle(): ActionsStep = withSubtitle(null as String?)
        fun withSubtitle(messageKey: FrcMessageKey?): ActionsStep = withSubtitle(if (messageKey == null) null else message(messageKey))
        fun withSubtitle(subtitle: String?): ActionsStep
    }

    

    @Suppress("unused")
    interface CommonActionsSteps
    {
        fun withAction(action: NotificationAction): AddActionStep
        
        /**
         * Adds an action that is created from the supplied parameters. The action has access to the actionEvent and the (original) notification.
         * Note, if the action lambda needs access to the project, it can be obtained from the actionEvent as follows:
         * ```kotlin
         * val projectFromNotification: Project? = e.getData(CommonDataKeys.PROJECT)
         * ```
         * For actions that need to open a URL, use:
         * ```
         * BrowserUtil.browse(url)
         * ```
         * @param text The action (i.e. link) text
         * @param expiring if true, the notification will expire when the action is performed (i.e. when the link is clicked)
         * @param action lambda definition of the action to take when invoked, which is supplied with both the actionEvent and the notification
         */
        fun withAction(@NotificationContent text: String, expiring: Boolean = true, action: (actionEvent: AnActionEvent, notification: Notification) -> Unit): AddActionStep

        /**
         * Adds an action that is created from the supplied parameters. The action has access to the actionEvent and the (original) notification.
         * Note, if the action lambda needs access to the project, it can be obtained from the actionEvent as follows:
         * ```kotlin
         * val projectFromNotification: Project? = e.getData(CommonDataKeys.PROJECT)
         * ```
         * For actions that need to open a URL, use:
         * ```
         * BrowserUtil.browse(url)
         * ```
         * @param textKey I18N key for the action (i.e. link) text
         * @param expiring if true, the notification will expire when the action is performed (i.e. when the link is clicked)
         * @param action lambda definition of the action to take when invoked, which is supplied with both the actionEvent and the notification
         */
        fun withAction(textKey: FrcMessageKey, expiring: Boolean = true, action: (actionEvent: AnActionEvent, notification: Notification) -> Unit): AddActionStep =
            withAction(message(textKey), expiring, action)

        /**
         * Adds an action that is created from the supplied parameters, such that the action lambda only has access to the actionEvent and
         * not the (original) notification.
         * Note, if the action lambda needs access to the project, it can be obtained from the actionEvent as follows:
         * ```kotlin
         * val projectFromNotification: Project? = e.getData(CommonDataKeys.PROJECT)
         * ```
         * For actions that need to open a URL, use:
         * ```
         * BrowserUtil.browse(url)
         * ```
         * @param text The action (i.e. link) text
         * @param expiring if true, the notification will expire when the action is performed (i.e. when the link is clicked)
         * @param action lambda definition of the action to take when invoked, which is supplied the actionEvent
         */
        fun withActionBasic(@NotificationContent text: String, expiring: Boolean = true, action: (actionEvent: AnActionEvent) -> Unit): AddActionStep

        /**
         * Adds an action that is created from the supplied parameters, such that the action lambda only has access to the actionEvent and
         * not the (original) notification. 
         * Note, if the action lambda needs access to the project, it can be obtained from the actionEvent as follows:
         * ```kotlin
         * val projectFromNotification: Project? = e.getData(CommonDataKeys.PROJECT)
         * ```
         * For actions that need to open a URL, use:
         * ```
         * BrowserUtil.browse(url)
         * ```
         * @param textKey I18N key for the action (i.e. link) text
         * @param expiring if true, the notification will expire when the action is performed (i.e. when the link is clicked)
         * @param action lambda definition of the action to take when invoked, which is supplied the actionEvent
         */
        fun withActionBasic(textKey: FrcMessageKey, expiring: Boolean = true, action: (actionEvent: AnActionEvent) -> Unit): AddActionStep =
            withActionBasic(message(textKey), expiring, action)
    }
    
    @Suppress("unused")
    interface ActionsStep: CommonActionsSteps, CompletableStep
    {
        fun withActions(vararg actions: NotificationAction): BuildStep
        fun withActions(actions: Collection<NotificationAction>): BuildStep 
        fun withNoActions(): BuildStep
    }

    @Suppress("unused")
    interface AddActionStep: CommonActionsSteps
    {
        fun noMoreActions(): BuildStep
    }
    
    @Suppress("unused")
    interface CompletableStep
    {
        fun build(): Notification
        fun notify(project: Project?): Notification = build().notifyFluent(project)
        fun notifyViaBalloon(project: Project?, hideOnClickOutside: Boolean = false): BalloonResult = build().notifyViaBalloon(project, hideOnClickOutside)
        fun notifyAllProject(notifyFrcProjectsOnly: Boolean = true, uuidKey: UUID = UUID.randomUUID()): UUID
        fun notifyAllProjectsViaBalloon(notifyFrcProjectsOnly: Boolean = true, uuidKey: UUID = UUID.randomUUID()): UUID
    }

    @Suppress("unused")
    interface BuildStep:CompletableStep
    {
        
    }
}


