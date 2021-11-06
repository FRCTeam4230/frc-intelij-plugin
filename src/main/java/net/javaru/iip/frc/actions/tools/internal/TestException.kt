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

import java.util.*


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