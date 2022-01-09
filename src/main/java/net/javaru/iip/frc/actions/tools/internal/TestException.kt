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
import java.lang.Exception
import java.util.*
import javax.swing.Icon


class TestException @JvmOverloads constructor(message: String = createMessage(), cause: Throwable? = createCause()) : RuntimeException(message, cause)
{
    companion object
    {
        /** Creates a [TestException] with a consistent line number at the top of the stack trace for some fingerprinting testing.  */
        @JvmOverloads
        fun create(message: String = createMessage(), cause: Throwable? = createCause()): TestException = TestException(message, cause)

        private const val serialVersionUID: Long = -2017461045868952683L
    }
}

private const val TEST_LOGGER = "FRC.TEST.LOGGER"
private const val TEST_MESSAGE = "test exception; please ignore"

// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank
// Lines intentionally blank to keep exception creation on line 68
private fun createTestException() = TestException.create(createMessage(), createCause(IllegalStateException("test subCause exception: ${randomString()}")))

abstract class AbstractCauseAnExceptionAction(text: String?, description: String?, icon: Icon?) : AbstractFrcInternalAction(text, description, icon)
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        val count = if (actionEvent.modifiers and ActionEvent.SHIFT_MASK == 0) 1 else 3
        logger.info("[FRC] Throwing $count simulated complex exception(s) for testing exception handling")
        ApplicationManager.getApplication().executeOnPooledThread {
            for (i in 1..count)
            {
                // Lines intentionally blank
                // Lines intentionally blank
                // Lines intentionally blank
                // Lines intentionally blank
                // Lines intentionally blank
                // Lines intentionally blank
                // Lines intentionally blank to keep exception creation on line 86
                val exception = doCreateTestException()

                if (includeAttachments)
                    Logger.getInstance(TEST_LOGGER).error(TEST_MESSAGE, exception, *createAttachments())
                else
                    Logger.getInstance(TEST_LOGGER).error(TEST_MESSAGE, exception)
                if (i != count) TimeoutUtil.sleep(200)
            }
        }
    }

    // Keep on Line 98
    open fun doCreateTestException(): Exception = createTestException()

    abstract val includeAttachments: Boolean

    companion object
    {
        private val logger = logger<AbstractCauseAnExceptionAction>()
    }
}

private val random = Random()

fun randomString() = random.nextLong().toString(16)

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
    "Test SubmitReportableEvent",
    AllIcons.Nodes.AbstractException
                                                                )
{
    override fun actionPerformed(e: AnActionEvent)
    {
        // Keep the throw clause on Line 150
        e.project.runSafely("FRC Internal Action Test", mapOf("key-1" to "value-1", "key-2" to Random().nextInt(100), "key-3" to randomString())) {
            throw RuntimeExceptionWithAttachments(TEST_MESSAGE, *createInternalAttachments())
        }
    }
}

abstract class AbstractCauseAWithAttachmentsExceptionAction(text: String?, description: String?, icon: Icon?): AbstractCauseAnExceptionAction(text, description, icon)
{
    override fun doCreateTestException(): Exception = TestExceptionWithAttachments.create()
}

class TestExceptionWithAttachments @JvmOverloads constructor(
    userMessage: String = "The RuntimeExceptionWithAttachments 'userMessage' value.",
    details: String = createMessage(),
    vararg attachments: Attachment) : RuntimeExceptionWithAttachments(userMessage, details, *attachments)
{
    companion object
    {
        /** Creates a [TestException] with a consistent line number at the top of the stack trace for some fingerprinting testing.  */
        @JvmOverloads
        fun create(
            userMessage: String = "The RuntimeExceptionWithAttachments 'userMessage' value.",
            details: String = "Test ExceptionWithAttachments (Please Ignore). Random String: ${randomString()}"
                  ): Exception
        {
            return TestExceptionWithAttachments(userMessage, details, *createInternalAttachments())
        }

        /** Creates a Test Exception, appending a random string to the end to the supplied [baseMessage]. */
        @Suppress("unused")
        @JvmOverloads
        fun createWithRandom(baseMessage: String, cause: Throwable? = createCause()): TestException = TestException("$baseMessage Random String: ${randomString()}", cause)
        private const val serialVersionUID: Long = -2017461045868952683L
    }
}

class CauseAWithAttachmentsExceptionAction(override val includeAttachments: Boolean = true) : AbstractCauseAWithAttachmentsExceptionAction
                                                                                                  (
                                                                                                  "Cause an ExceptionWithAttachments Exception",
                                                                                                  "Cause an exceptions that is of type RuntimeExceptionWithAttachments 'external' attachments.",
                                                                                                  AllIcons.Debugger.Db_disabled_exception_breakpoint
                                                                                                  )




private class SampleCausedByException(subCause: Throwable? = null): RuntimeException("A test 'caused By' exception for testing; please ignore. Random String: ${randomString()}", subCause)
{
    companion object
    {
        private const val serialVersionUID: Long = -8654523244061272730L
    }
}



private fun createAttachments() = arrayOf(Attachment("first.txt", "content ${randomString()}"), Attachment("second.txt", "more content ${randomString()}"), Attachment("third.txt", "even more content ${randomString()}"))
private fun createInternalAttachments() = arrayOf(Attachment("first-internal.txt", "internal content ${randomString()}"), Attachment("second-internal.txt", "more internal content ${randomString()}"), Attachment("third-internal.txt ${randomString()}", "even more internal content"))
private fun createMessage() = "Test Exception (Please Ignore). Random String: ${randomString()}"
private fun createCause(subCause: Throwable? = null) = SampleCausedByException(subCause) // Keep on line 208