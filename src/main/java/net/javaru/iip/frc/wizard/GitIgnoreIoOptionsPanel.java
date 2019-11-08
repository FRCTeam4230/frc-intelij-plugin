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

package net.javaru.iip.frc.wizard;

import javax.swing.*;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;



// See NotificationsConfigurablePanel
//      com.intellij.notification.impl.ui.NotificationsConfigurablePanel
public class GitIgnoreIoOptionsPanel extends JPanel implements Disposable
{
    private static final Logger LOG = Logger.getInstance(GitIgnoreIoOptionsPanel.class);
    
    
    
    private IgnoreOptionsTable myTable;
    
    @Override
    public void dispose()
    {
        myTable = null;
        
    }
    
    private class IgnoreOptionsTable extends TreeTable
    {
    
        public IgnoreOptionsTable(TreeTableModel treeTableModel)
        {
            super(treeTableModel);
        }
    }
}
