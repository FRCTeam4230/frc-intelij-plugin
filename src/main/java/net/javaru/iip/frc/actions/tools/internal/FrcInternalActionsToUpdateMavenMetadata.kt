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

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import net.javaru.iip.frc.wpilib.gradlePluginRepo.internalConvertXmlToJson

class FrcInternalWpiLibActionsGroup : FrcInternalActionsGroup()

class ConvertInternalWpiLibXmlMetadataToJsonAction : AbstractFrcInternalAction()
{
    override fun actionPerformed(e: AnActionEvent)
    {
        val json = internalConvertXmlToJson()
        log.info("\n\n$json\n\n")
        Messages.showMessageDialog(json, "JSON String", null)
    }
}

class GetAndConvertLatestWpiLibXmlMetadataToJsonAction : AbstractFrcInternalAction()
{
    override fun actionPerformed(e: AnActionEvent)
    {
        // TODO: NEED TO FIX, and then wrap in a background or such… But we'll wait until we rework the MavenMetadata classes and services
        // java.security.PrivilegedActionException: com.fasterxml.jackson.databind.exc.InvalidDefinitionException:
        // Direct self-reference leading to cycle (through reference chain:
        //   net.javaru.iip.frc.wpilib.gradlePluginRepo.WpiLibMavenMetadata["wpiLibVersions"]->
        //   java.util.Arrays$ArrayList[0]->
        //   net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl["correspondingReleaseVersion"]
//        val json = fetchGradleRioMavenMetadata()?.toJson(true) ?: "<WAS NULL>"
//        log.info("\n\n$json\n\n")
//        Messages.showMessageDialog(json, "JSON String", null)
        Messages.showMessageDialog("Not implemented at this time.", "D'Oh!", null)
    }
}