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

package net.javaru.iip.frc.actions.tools.internal

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.RuntimeExceptionWithAttachments
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.TimeoutUtil
import net.javaru.iip.frc.util.runSafely
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.Icon


private val random = Random()

fun randomString() = random.nextLong().toString(16)

class TestException @JvmOverloads constructor(message: String = "Test Exception (Please Ignore). Random String: ${randomString()}", cause: Throwable? = null) : RuntimeException(message, cause)
{
    companion object
    {
        /** Creates a [TestException] with a consistent line number at the top of the stack trace for some fingerprinting testing.  */
        @JvmOverloads
        fun create(message: String = "Test Exception (Please Ignore). Random String: ${randomString()}", cause: Throwable? = null): TestException = TestException(message, cause)

        /** Creates a Test Exception, appending a random string to the end to the supplied [baseMessage]. */
        @JvmOverloads
        fun createWithRandom(baseMessage: String, cause: Throwable? = null): TestException = TestException("$baseMessage Random String: ${randomString()}", cause)
        private const val serialVersionUID: Long = -2017461045868952683L

    }
}


private const val TEST_LOGGER = "FRC.TEST.LOGGER"
private const val TEST_MESSAGE = "test exception; please ignore"

abstract class AbstractCauseAnExceptionAction(text: String?, description: String?, icon: Icon?) : AbstractFrcInternalAction(text, description, icon)
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        val count = if (actionEvent.modifiers and ActionEvent.SHIFT_MASK == 0) 1 else 3
        logger.info("[FRC] Throwing $count simulated complex exception(s) for testing exception handling")
        val attachments = arrayOf(Attachment("first-.txt", "content"), Attachment("second.txt", "more content"), Attachment("third.txt", "even more content"))
        ApplicationManager.getApplication().executeOnPooledThread {
            for (i in 1..count)
            {
                // Lines intentionally blank
                // Lines intentionally blank
                // Lines intentionally blank
                // Lines intentionally blank to keep exception creation on line 68
                val exception =
                    TestException.create("random exception text ${randomString()}", TestException("Cause with random text ${randomString()}", TestException("Nested cause with random text ${randomString()}"))) // We want the stacktrace line numbers to be consistent, so we always create on the same line, 186 if possible

                if (includeAttachments)
                    Logger.getInstance(TEST_LOGGER).error(TEST_MESSAGE, exception, *attachments)
                else
                    Logger.getInstance(TEST_LOGGER).error(TEST_MESSAGE, exception)
                if (i != count) TimeoutUtil.sleep(200)
            }
        }
    }

    abstract val includeAttachments: Boolean

    companion object
    {
        private val logger = logger<AbstractCauseAnExceptionAction>()
    }
}

/** An action that will purposefully cause an exception for testing purposes. */
class CauseAnExceptionAction : AbstractCauseAnExceptionAction(
    "Cause An Exception",
    "Hold down SHIFT for a sequence of exceptions",
    AllIcons.Nodes.ExceptionClass
                                                             )
{
    override val includeAttachments: Boolean
        get() = false
}

/** An action that will purposefully cause an exception, with attachments, for testing purposes. */
class CauseAnExceptionWithAttachmentsAction : AbstractCauseAnExceptionAction(
    "Cause an Exception with Attachments",
    "Cause a sequence of exceptions along with attachments. Hold down SHIFT for a sequence of exceptions",
    AllIcons.Nodes.AbstractException
                                                                            )
{
    override val includeAttachments: Boolean
        get() = true
}

// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank

class TestSubmitReportableEventAction: AbstractFrcInternalAction(
    "Text SubmitReportableEvent",
    AllIcons.Nodes.AbstractException
                                                                )
{
    override fun actionPerformed(e: AnActionEvent)
    {
        e.project.runSafely("FRC Internal Action Test",
                            mapOf("key-1" to "value-1", "key-2" to Random().nextInt(100))) {
            val attachments = arrayOf(Attachment("first-.txt", "content"), Attachment("second.txt", "more content"), Attachment("third.txt", "even more content"))
            throw RuntimeExceptionWithAttachments(TEST_MESSAGE, *attachments)
        }
    }

}