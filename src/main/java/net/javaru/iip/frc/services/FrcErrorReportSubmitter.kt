/*
 * Copyright 2015-2025 the original author or authors.
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

import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import net.javaru.iip.frc.i18n.FrcBundle.message
import java.awt.Component


class FrcErrorReportSubmitter: ErrorReportSubmitter()
{
    override fun getReportActionText() = "Report to FRC Plugin Author"

    override fun getPrivacyNoticeText(): String? = message("frc.errorReportSubmitter.privacy.policy")

    override fun submit(events: Array<out IdeaLoggingEvent>,
                        additionalInfo: String?,
                        parentComponent: Component,
                        // Note: we can't replace use of deprecated 'Consumer' until the ErrorReportSubmitter.submit() method changes
                        consumer: com.intellij.util.Consumer<in SubmittedReportInfo>): Boolean
    {
        return FrcErrorReportSubmitterSentryWorker.submit(events, additionalInfo, parentComponent, consumer)
    }
}