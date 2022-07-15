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
package net.javaru.iip.frc.actions.tools.internal

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.notify.FrcNotifyType
import net.javaru.iip.frc.plugin.FrcPluginVersionManagerApplicationService
import org.apache.commons.lang3.RandomUtils
import org.intellij.lang.annotations.Language


class FrcInternalNotificationsActionsGroup : FrcInternalActionsGroup()

/**
 * An action that will purposefully cause an exception for testing purposes.
 */
abstract class AbstractShowNotificationAction : AbstractFrcInternalAction()
{
    internal val logger =logger<AbstractShowNotificationAction>()

    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        logger.info("[FRC] Making a simulated FRC Notification via ${javaClass.simpleName}")
        val project = actionEvent.getData(CommonDataKeys.PROJECT)
        doNotification(project)
    }

    abstract fun doNotification(project: Project?)
}

class ShowNotificationActionableInfoAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
            .withContent(content = "This is a test FRC Actionable Info notification without a prefixed title.")
            .withNonPrefixedTitle(title = "Actionable info test")
            .notify(project)
    }
}

class ShowNotificationGeneralInfoAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.GENERAL_INFO_WITH_FRC_ICON
            .withContent(content = "This is a test FRC General Info notification.") 
            .withFrcPrefixedTitle(title = "General info test")
            .notify(project)
    }
}

class ShowNotificationActionableErrorAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.ACTIONABLE_ERROR
            .withContent(content = "This is a test FRC Actionable Error notification")
            .withFrcPrefixedTitle(title = "Actionable error test")
            .notify(project)
    }
}

class ShowNotificationActionableErrorImportantAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.ACTIONABLE_ERROR
            .withContent("This is a test FRC Actionable *Important* Error notification")
            .withFrcTitle()
            .withSubtitle("My subtitle")
            .build().apply { 
                isImportant = true
            }.notify(project)
    }
}

class ShowNotificationThatUsesNotificationActionsAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
            .withContent("Please select an option. B will expire this notification. A will not.")
            .withFrcTitle()
            .withActionBasic("Option A", expiring = false) {
                val projectFromNotification = it.getData(CommonDataKeys.PROJECT)
                FrcNotifyType.ACTIONABLE_INFO
                    .withContent("You selected option A from project ${projectFromNotification?.name}. This option does NOT expire the original notification.")
                    .notify(projectFromNotification)
            }
            .withAction("Option B") {e, n ->
                val projectFromNotification = e.getData(CommonDataKeys.PROJECT)

                FrcNotifyType.ACTIONABLE_INFO
                    .withContent("You selected option B from project ${projectFromNotification?.name}. " +
                                                          "The original notification has an ID of ${n.id}. " +
                                                          "This option DOES expire the original notification.")
                    .notify(projectFromNotification)

            }
            .noMoreActions()
            .notify(project)
    }
}

class ShowNotificationThatUsesSeveralNotificationActionsAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
            .withContent("Please select an option. B will expire this notification. A will not.")
            .withFrcTitle()
            .withActionBasic("Action option A", expiring = false) {
                val projectFromNotification = it.getData(CommonDataKeys.PROJECT)
                FrcNotifyType.ACTIONABLE_INFO.withContent("You selected option A from project ${projectFromNotification?.name}.").notify(projectFromNotification)
            }
            .withActionBasic("Action option B", expiring = false) {
                val projectFromNotification = it.getData(CommonDataKeys.PROJECT)
                FrcNotifyType.ACTIONABLE_INFO.withContent("You selected option B from project ${projectFromNotification?.name}.").notify(projectFromNotification)
            }
            .withActionBasic("Action option C", expiring = false) {
                val projectFromNotification = it.getData(CommonDataKeys.PROJECT)
                FrcNotifyType.ACTIONABLE_INFO.withContent("You selected option C from project ${projectFromNotification?.name}.").notify(projectFromNotification)
            }
            .withActionBasic("Action option D", expiring = false) {
                val projectFromNotification = it.getData(CommonDataKeys.PROJECT)
                FrcNotifyType.ACTIONABLE_INFO.withContent("You selected option D from project ${projectFromNotification?.name}.").notify(projectFromNotification)
            }
            .withActionBasic("Action option E", expiring = false) {
                val projectFromNotification = it.getData(CommonDataKeys.PROJECT)
                FrcNotifyType.ACTIONABLE_INFO.withContent("You selected option E from project ${projectFromNotification?.name}.").notify(projectFromNotification)
            }
            .noMoreActions()
            .notify(project)
    }
}


class ShowNotificationBalloonAction : AbstractShowNotificationAction()
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
        
        FrcNotifyType.ACTIONABLE_INFO.withContent(content).withFrcTitle().withSubtitle("Some subtitle").notifyViaBalloon(project)
    }

    private fun randomNumberOfLines():String
    {
        val sb = StringBuilder()
        for (i in 3..(RandomUtils.nextInt(4, 11)))
        {
            sb.append("Line #").append(i).append("<br/>")
        }
        return sb.toString()
    }
}


class ShowNotificationSmallBalloonAction : AbstractShowNotificationAction()
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
        
        FrcNotifyType.ACTIONABLE_INFO.withContent(content).withFrcTitle().withSubtitle("Some subtitle").notifyViaBalloon(project)
    }
}

class ShowNotificationBalloonWithActionsAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        @Suppress("HtmlRequiredLangAttribute", "SpellCheckingInspection")
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
        
        
        FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
            .withContent(content)
            .withFrcPrefixedTitle("Sample balloon")
            .withActionBasic("Option A", expiring = false) {
                val projectFromNotification = it.getData(CommonDataKeys.PROJECT)
                FrcNotifyType.ACTIONABLE_INFO.withContent("You selected option A from project ${projectFromNotification?.name}").notify(projectFromNotification)
            }
            .withActionBasic("Option B", expiring = false) {
                val projectFromNotification = it.getData(CommonDataKeys.PROJECT)
                FrcNotifyType.ACTIONABLE_INFO.withContent("You selected option B from project ${projectFromNotification?.name}").notify(projectFromNotification)
            }
            .noMoreActions()
            .notifyViaBalloon(project)
    }
}



class ShowNotificationSharedByFrcProjectsAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
            .withContent("This is a notification shared across all open FRC projects.")
            .withFrcPrefixedTitle("ID #${RandomUtils.nextInt(1, 5000)}")
            .notifyAllProject()
    }
}


class ShowNotificationSharedByALLOpenProjectsAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
            .withContent("This is a notification shared across ALL open projects (FRC & Non-FRC).")
            .withFrcPrefixedTitle("ID #${RandomUtils.nextInt(1, 5000)}")
            .notifyAllProject(notifyFrcProjectsOnly = false)
    }
}



class ShowNotificationBalloonSharedByFrcProjectsAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
            .withContent("<html><h2>This is a notification shared across all open FRC projects.</h2></html>")
            .withFrcPrefixedTitle("ID #${RandomUtils.nextInt(1, 5000)}")
            .notifyAllProjectsViaBalloon(notifyFrcProjectsOnly = true)
    }
}

class ShowNotificationBalloonSharedByALLOpenProjectsAction : AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
            .withContent("<html><h2>This is a notification shared across ALL open projects (FRC & Non-FRC).</h2></html>")
            .withFrcPrefixedTitle("ID #${RandomUtils.nextInt(1, 5000)}")
            .notifyAllProjectsViaBalloon(notifyFrcProjectsOnly = false)
    }
}

class ShowEndOfLifeMessageAction: AbstractShowNotificationAction()
{
    override fun doNotification(project: Project?)
    {
        FrcPluginVersionManagerApplicationService.getInstance().checkPluginUpdateStatus(project, 201, 211)
    }
}

abstract class AbstractShowBuiltNotificationAction : AbstractFrcInternalAction()
{
    internal val logger = logger<AbstractShowNotificationAction>()

    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        logger.info("[FRC] Making a simulated FRC Notification via ${javaClass.simpleName}")
        val project = actionEvent.getData(CommonDataKeys.PROJECT)
        doNotification(project)
    }

    open fun doNotification(project: Project?)
    {
        getType().builder()
            .withContent(getContent())
            .withFrcPrefixedTitle(getTitle())
            .withSubtitle(getSubTitle())
            .withNoActions()
            .notify(project)
    }
    open fun getContent(): String = "This is the message content. Blah blah blah."
    open fun getTitle(): String = "Message title"
    
    open fun getSubTitle(): String? = null
    abstract fun getType(): FrcNotifyType
}

class ShowBuildToolWindowNotificationInfo: AbstractShowBuiltNotificationAction()
{
    override fun getType(): FrcNotifyType = FrcNotifyType.BUILD__INFO
}

class ShowBuildToolWindowNotificationInfoWithIcon: AbstractShowBuiltNotificationAction()
{
    override fun getType(): FrcNotifyType = FrcNotifyType.BUILD__INFO_WITH_ICON
}

class ShowBuildToolWindowNotificationInfoWithFrcIcon: AbstractShowBuiltNotificationAction()
{
    override fun getType(): FrcNotifyType = FrcNotifyType.BUILD__INFO_WITH_FRC_ICON
}

class ShowRunToolWindowNotificationInfo: AbstractShowBuiltNotificationAction()
{
    override fun getType(): FrcNotifyType = FrcNotifyType.RUN_TOOL_WINDOW__INFO
    override fun getSubTitle() = "My subtitle"
}

class ShowRunToolWindowNotificationInfoWithIcon: AbstractShowBuiltNotificationAction()
{
    override fun getType(): FrcNotifyType = FrcNotifyType.RUN_TOOL_WINDOW__INFO_WITH_ICON
}

class ShowRunToolWindowNotificationInfoWithFrcIcon: AbstractShowBuiltNotificationAction()
{
    override fun getType(): FrcNotifyType = FrcNotifyType.RUN_TOOL_WINDOW__INFO_WITH_FRC_ICON
}

