/*
 * Copyright 2015-2016 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.actions.create;

import java.util.Arrays;
import java.util.List;
import javax.swing.*;

import org.jetbrains.annotations.Nullable;



public interface TaskValidator 
{
    void setValidationPanel(@Nullable JPanel validationPanel);

    /**
     * Updates the validation messages panel with the supplied messages.
     * If messages is null or empty, any previous messages are removed,
     * leaving a blank panel.
     *
     * @param messages the messages to display
     */
    void updateDisplayedMessages(@Nullable List<ValidationMessage> messages);

    /**
     * Updates the validation messages panel with the supplied messages. 
     * If messages is null or empty, any previous messages are removed,
     * leaving a blank panel.
     * @param messages the messages to display
     */
    default void updateDisplayedMessages(@Nullable ValidationMessage... messages)
    {
        if (messages == null)
        {
            updateDisplayedMessages();
        }
        else
        {
            updateDisplayedMessages(Arrays.asList(messages));
        }
    }

    /**
     * Updates the displayed validation messages to have no messages. That is,
     * it removes any previous messages and leaves a blank panel.
     */
    default void updateDisplayedMessages()
    {
        updateDisplayedMessages((List<ValidationMessage>) null);
    }
}
