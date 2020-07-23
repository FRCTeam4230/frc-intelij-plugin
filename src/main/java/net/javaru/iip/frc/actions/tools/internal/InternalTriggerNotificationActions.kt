/*
 * Copyright 2015-2020 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package net.javaru.iip.frc.actions.tools.internal

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications.createNotification
import net.javaru.iip.frc.notify.FrcNotifications.notify
import net.javaru.iip.frc.notify.FrcNotifications.notifyBalloon
import net.javaru.iip.frc.notify.FrcNotifications.showBalloon
import org.apache.commons.lang3.RandomUtils
import org.intellij.lang.annotations.Language
import kotlin.random.Random


class FrcInternalNotificationsActionsGroup : FrcInternalActionsGroup()

/**
 * An action that will purposefully cause an exception for testing purposes.
 */
abstract class AbstractTriggerNotificationAction : AbstractFrcInternalAction()
{
    internal val LOG = Logger.getInstance(AbstractTriggerNotificationAction::class.java)

    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        LOG.info("[FRC] Making a simulated FRC Notification via ${javaClass.simpleName}")
        val project = actionEvent.getData(CommonDataKeys.PROJECT)
        doNotification(project)
    }

    abstract fun doNotification(project: Project?)
}

class TriggerNotificationActionableInfoAction : AbstractTriggerNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        notify(FrcNotificationType.ACTIONABLE_INFO,
               "This is a test FRC Actionable Info notification",
               "My Sub-title",
               project)
    }
}

class TriggerNotificationGeneralInfoAction : AbstractTriggerNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        notify(FrcNotificationType.GENERAL_INFO,
               "This is a test FRC General Info notification",
               "My Sub-title",
               project)
    }
}

class TriggerNotificationActionableErrorAction : AbstractTriggerNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        notify(FrcNotificationType.ACTIONABLE_ERROR,
               "This is a test FRC Actionable Error notification",
               "My Sub-title",
               project)
    }
}

class TriggerNotificationActionableErrorImportantAction : AbstractTriggerNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        val notification = createNotification(FrcNotificationType.ACTIONABLE_ERROR,
                                              "This is a test FRC Actionable *Important* Error notification",
                                              "My Sub-title")
        notification.isImportant = true
        notification.notify(project)
    }
}


class TriggerNotificationBalloonAction : AbstractTriggerNotificationAction()
{
    override fun doNotification(project: Project?)
    {

        @Suppress("HtmlRequiredLangAttribute")
        @Language("HTML")
        val content = """
                          |<html>
                          |This is a test <span style="color:red">balloon</span> notification<br/>
                          |Line #2<br/>
                          |${randomNumberOfLines()}
                          |A random Number: ${RandomUtils.nextInt(1, 5000)} <br/>
                          |<h1>Header 1</h1>
                          |Section 1 Text. Blah blah blah.<br/>
                          |<h2>Header 2</h2>
                          |Section 2 Text. Blah blah blah.<br/>
                          |<h3>Header 3</h3>
                          |Section 3 Text. Blah blah blah.<br/>
                          |<h4>Header 4</h4>
                          |Section 4 Text. Blah blah blah.<br/>
                          |<strong>Strong (i.e. bold) Text</strong>
                          |<br/>
                          |<em>Italics / Emphasis Text</em><br/>
                          |<span style='font-size: xx-small'>XX-Small font size.</span><br/>
                          |<span style='font-size: x-small'>X-Small font size.</span><br/>
                          |<span style='font-size: small'>Small font size.</span><br/>
                          |<span style='font-size: smaller'>Smaller font size.</span><br/>
                          |Standard font size (unmodified)<br/>
                          |<span style='font-size: medium'>Medium font size.</span><br/>
                          |<span style='font-size: larger'>Larger font size.</span><br/>
                          |<span style='font-size: large'>Large font size.</span><br/>
                          |<span style='font-size: x-large'>X-Large font size.</span><br/>
                          |<span style='font-size: xx-large'>XX-Large font size.</span><br/>
                          |<br/><br/>
                          |A long line to test text wrapping. A long line to test text wrapping.
                          |A long line to test text wrapping. A long line to test text wrapping.
                          |A long line to test text wrapping. A long line to test text wrapping.
                          |A long line to test text wrapping. A long line to test text wrapping.
                          |A long line to test text wrapping. A long line to test text wrapping. <br/>
                          |</html>
                      """.trimMargin()

        notifyBalloon(FrcNotificationType.ACTIONABLE_INFO,
                      content,
                      "Some Sub-title")
    }

    fun randomNumberOfLines():String
    {
        val sb = StringBuilder()
        for (i in 3..(RandomUtils.nextInt(4, 11)))
        {
            sb.append("Line #").append(i).append("<br/>")
        }
        return sb.toString()
    }
}


class TriggerNotificationSmallBalloonAction : AbstractTriggerNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        @Suppress("HtmlRequiredLangAttribute")
        @Language("HTML")
        val content = """
                          |<html>
                          |A <em>short/small</em> test <span style="color:green">balloon</span> notification. (${RandomUtils.nextInt(1, 5000)})<br/>
                          |</html>
                      """.trimMargin()

        notifyBalloon(FrcNotificationType.ACTIONABLE_INFO,
                      content,
                      "Some Sub-title")
    }
}

class TriggerNotificationBalloonWithActionsAction : AbstractTriggerNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        @Suppress("HtmlRequiredLangAttribute")
        @Language("HTML")
        val content = """
                          |<html>
                          |Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt 
                          |ut labore et dolore magna aliqua. Tempus iaculis urna id volutpat lacus laoreet non curabitur. 
                          |Leo vel orci porta non pulvinar. Ullamcorper a lacus vestibulum sed arcu non odio euismod. 
                          |Turpis nunc eget lorem dolor sed viverra ipsum nunc. Sed sed risus pretium quam. Sed libero enim 
                          |sed faucibus turpis in. Facilisis magna etiam tempor orci. Ullamcorper sit amet risus nullam eget 
                          |felis eget. Sit amet nulla facilisi morbi tempus. Ipsum suspendisse ultrices gravida dictum fusce ut.
                          |</html>
                      """.trimMargin()

        val notification = createNotification(FrcNotificationType.ACTIONABLE_INFO_WITH_FRC_ICON, content)
            .addAction(object : NotificationAction("Option A")
                       {
                           override fun actionPerformed(e: AnActionEvent, notification: Notification)
                           {
                               val projectFromNotification = e.getData(CommonDataKeys.PROJECT)
                               notify(FrcNotificationType.ACTIONABLE_INFO, "You selected option A from project ${projectFromNotification?.name}")
                           }
                       })
            .addAction(object : NotificationAction("Option B")
                       {
                           override fun actionPerformed(e: AnActionEvent, notification: Notification)
                           {
                               val projectFromNotification = e.getData(CommonDataKeys.PROJECT)
                               notify(FrcNotificationType.ACTIONABLE_INFO, "You selected option B from project ${projectFromNotification?.name}")
                           }
                       })

        showBalloon(notification, project)
    }
}




