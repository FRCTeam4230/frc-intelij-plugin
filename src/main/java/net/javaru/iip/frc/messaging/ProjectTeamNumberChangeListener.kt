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
package net.javaru.iip.frc.messaging

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic

interface ProjectTeamNumberChangeListener
{
    companion object
    {
        /**
         * Example usage:
         * <pre>
         * MessageBusConnection connection = project.getMessageBus().connect();
         * connection.subscribe(ProjectTeamNumberChangeListener.TOPIC, myListener);
        </pre> *
         */
        val TOPIC = Topic("FRC Project Team Number Change", ProjectTeamNumberChangeListener::class.java)

        @JvmOverloads
        fun subscribe(project: Project, listener: ProjectTeamNumberChangeListener, parentDisposable: Disposable = project): MessageBusConnection
        {
            val connection = project.messageBus.connect(parentDisposable)
            connection.subscribe(TOPIC, listener)
            return connection
        }
    }
    
    
    /**
     * Called when the project's team number has been changed **and applied.**
     *
     * @param newTeamNum the new **verified** team number
     */
    fun projectTeamNumberChanged(newTeamNum: Int)

    /**
     * Called when the project's team number is pending a change, for example having been changed in in settings dialog **but not yet applied.**
     *
     * @param newTeamNum the new **verified** team number
     */
    fun projectTeamNumberPotentialChange(newTeamNum: Int)

    
}