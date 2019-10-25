/*
 * Copyright 2015-2019 the original author or authors
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

package net.javaru.iip.frc.wizard

import org.intellij.lang.annotations.Language


@Suppress("HtmlRequiredLangAttribute")
enum class FrcWizardRobotTemplateDefinition(
        val displayName: String,
        @field:Language("HTML") @param:Language("HTML") private val _description: String

                                           )

{
    CommandBased("Command Based Robot", "A robot project that allows robots to be implemented using the command based model to allow complex functionality to be developed from simpler functionality."), 
    Iterative("Iterative Robot", "A robot project that allow robots to be implemented in an iterative manner synced to receiving driver station packets."), 
    Timed("Timed Robot", "A robot project that allows robots to be implemented in an iterative manner synced to a timer."), 
    TimedSkeleton("Timed Skeleton (Advanced)", "A skeleton (stub) Timed Robot project."), 
    Sample("Sample Robot", "A robot project used for small sample programs or for highly advanced programs with more complete control over program flow. This is <em>not</em> a good choice to use for competition, especially for the inexperienced. Use Timed Robot or Command Based Robot instead.");

   
    val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"
    
    val displayNameAndDescription:String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : $_description</html>"
    
    override fun toString(): String
    {
        return displayName
    }
}