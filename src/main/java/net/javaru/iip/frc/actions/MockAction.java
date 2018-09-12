/*
 * Copyright 2015-2018 the original author or authors
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

package net.javaru.iip.frc.actions;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;



/**
 * A MockAction, along with some factory 'create' methods, to act as placeholders and/or for testing toolbars and such during the development process.
 */
@SuppressWarnings("ComponentNotRegistered")
public class MockAction extends DumbAwareAction
{
    private static final Logger LOG = Logger.getInstance(MockAction.class);
    
    final String text;


    public MockAction(@Nullable String text,
                      @Nullable Icon icon)
    {
        super(text, "MockAction " + text, icon);
        this.text = text;
    }


    @Override
    public void actionPerformed(AnActionEvent e)
    {
        LOG.info("[FRC] - MockAction '" + text + "' activated");
    }
    
    public static MockAction createMockAddAction() { return new MockAction("Add", AllIcons.General.Add); }
    public static MockAction createMockCancelAction() { return new MockAction("Cancel", Actions.Cancel); }
    public static MockAction createMockRefreshAction() { return new MockAction("Refresh", Actions.Refresh); }
    public static MockAction createMockRemoveAction() { return new MockAction("Remove", AllIcons.General.Remove); }
}
