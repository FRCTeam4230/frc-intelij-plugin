/*
 * Copyright 2015 Mark Vedder
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

package net.javaru.iip.frc.settings;

import net.javaru.iip.frc.util.ClonerImpl;



// Example:  org.intellij.plugins.intelliLang.AdvancedSettingsUI
public class FrcSettings implements Cloneable
{

    public FrcSettings() { }

    private boolean rioLogTargetWindowIsFrcToolWindow = true;

    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    protected FrcSettings clone()
    {
        return ClonerImpl.deepClone(this);
    }


    public boolean isRioLogTargetWindowIsFrcToolWindow() { return rioLogTargetWindowIsFrcToolWindow; }


    public void setRioLogTargetWindowIsFrcToolWindow(boolean rioLogTargetWindowIsFrcToolWindow) { this.rioLogTargetWindowIsFrcToolWindow = rioLogTargetWindowIsFrcToolWindow; }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FrcSettings that = (FrcSettings) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
            .append(rioLogTargetWindowIsFrcToolWindow, that.rioLogTargetWindowIsFrcToolWindow)
            .isEquals();
    }


    @Override
    public int hashCode()
    {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
            .append(rioLogTargetWindowIsFrcToolWindow)
            .toHashCode();
    }
}
