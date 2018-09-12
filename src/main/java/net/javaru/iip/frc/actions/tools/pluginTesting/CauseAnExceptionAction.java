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

package net.javaru.iip.frc.actions.tools.pluginTesting;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.actions.tools.AbstractFrcToolsAction;



/**
 * An action that will purposefully cause an exception for testing purposes. 
 */
public class CauseAnExceptionAction extends AbstractFrcToolsAction
{
    private static final Logger LOG = Logger.getInstance(CauseAnExceptionAction.class);


    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        LOG.info("[FRC] Throwing simulated exception for testing exception handling");
        throw new RuntimeException("Sample exception for testing exception handling");
    }

}
