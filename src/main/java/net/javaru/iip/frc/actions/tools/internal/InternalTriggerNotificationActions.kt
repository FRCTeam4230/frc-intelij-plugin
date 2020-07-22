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
                          |
                          |<br/>
                          |<h1>Header 1</h1>
                          |<br/>
                          |<h2>Header 2</h2>
                          |<br/>
                          |<h3>Header 3</h3>
                          |<br/>
                          |<h4>Header 4</h4>
                          |<br/>
                          |<strong>Strong (i.e. bold) Text</strong>
                          |<br/>
                          |<em>Italics / Emphasis Text</em><br/>
                          |<br/>
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


class TriggerSmallNotificationBalloonAction : AbstractTriggerNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        val random = Random(1234)

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



