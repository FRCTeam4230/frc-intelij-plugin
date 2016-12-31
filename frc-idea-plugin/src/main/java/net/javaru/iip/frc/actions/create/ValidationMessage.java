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

import javax.swing.*;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import com.intellij.icons.AllIcons;



public class ValidationMessage implements Comparable<ValidationMessage>
{
    
    public static final Icon ERROR_ICON = AllIcons.General.BalloonError;
    public static final Icon WARN_ICON = AllIcons.General.BalloonWarning;
    public static final Icon INFO_ICON = AllIcons.General.BalloonInformation;


    @NotNull
    private final Level level;
    @NotNull
    private final String text;


    public ValidationMessage(@NotNull Level level, @NotNull String text)
    {
        this.level = level;
        this.text = text;
    }


    @NotNull
    public Level getLevel() { return level; }


    @NotNull
    public String getText() { return text; }

    @NotNull
    public Icon getIcon() { return level.getIcon(); }
    

    @Override
    public int compareTo(@NotNull ValidationMessage other)
    {
        return new CompareToBuilder()
            .append(getLevel(), other.getLevel())
            .toComparison();
    }


    //Order is important since we sort a group of messages
    public enum Level {
        ERROR(ERROR_ICON), WARN(WARN_ICON), INFO(INFO_ICON);

        @NotNull
        private final Icon icon;


        Level(@NotNull Icon icon) { this.icon = icon; }


        @NotNull
        public Icon getIcon() { return icon; }
    }
    
    

}
