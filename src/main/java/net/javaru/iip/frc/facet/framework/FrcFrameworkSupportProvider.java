/*
 * Copyright 2015-2017 Mark Vedder
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

package net.javaru.iip.frc.facet.framework;

import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.ModifiableRootModel;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.facet.FrcFacetType;



//NOTE: Configured in plugin.xml 
public class FrcFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<FrcFacet>
{
    //For example, see StrutsFrameworkSupportProvider fot Structs2 plugin in jetbrains open source plugins
    
    private static final Logger LOG = Logger.getInstance(FrcFrameworkSupportProvider.class);


    protected FrcFrameworkSupportProvider()
    {
        super(FrcFacetType.getInstance());
    }


    @Override
    protected void setupConfiguration(FrcFacet facet, ModifiableRootModel rootModel, FrameworkVersion version)
    {
        // This method is called after "Finished" is clicked in the new Project Wizard. 
        // It is call stacktrace is:
        //        at net.javaru.iip.frc.facet.framework.FrcFrameworkSupportProvider.setupConfiguration(FrcFrameworkSupportProvider.java:46)
        //        at net.javaru.iip.frc.facet.framework.FrcFrameworkSupportProvider.setupConfiguration(FrcFrameworkSupportProvider.java:30)
        //        at com.intellij.facet.ui.FacetBasedFrameworkSupportProvider.addSupport(FacetBasedFrameworkSupportProvider.java:108)
        //        at com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurableBase.addSupport(FrameworkSupportConfigurableBase.java:121)
        //        at com.intellij.ide.util.newProjectWizard.AddSupportForFrameworksPanel.addSupport(AddSupportForFrameworksPanel.java:449)
        //        at com.intellij.ide.projectWizard.ProjectTypeStep$6.update(ProjectTypeStep.java:198)
        //        at com.intellij.ide.util.projectWizard.ModuleBuilder.setupModule(ModuleBuilder.java:265)
        //        at com.intellij.ide.util.projectWizard.ModuleBuilder.createModule(ModuleBuilder.java:256)
        //        at com.intellij.ide.util.projectWizard.ModuleBuilder.createAndCommitIfNeeded(ModuleBuilder.java:294)
        //        at com.intellij.ide.util.projectWizard.ModuleBuilder.lambda$commitModule$3(ModuleBuilder.java:337)
        //        at com.intellij.ide.util.projectWizard.ModuleBuilder$$Lambda$677 .1296299083.compute(Unknown Source:-1)
        //        at com.intellij.openapi.application.impl.ApplicationImpl.runWriteAction(ApplicationImpl.java:1027)
        //        at com.intellij.ide.util.projectWizard.ModuleBuilder.commitModule(ModuleBuilder.java:336)
        //        at com.intellij.ide.util.projectWizard.ModuleBuilder.commit(ModuleBuilder.java:322)
        //        at com.intellij.ide.util.projectWizard.JavaModuleBuilder.commit(JavaModuleBuilder.java:179)
        //        at com.intellij.ide.impl.NewProjectUtil.doCreate(NewProjectUtil.java:149)
        //        at com.intellij.ide.impl.NewProjectUtil.createFromWizard(NewProjectUtil.java:76)
        //        at com.intellij.ide.impl.NewProjectUtil.createNewProject(NewProjectUtil.java:71)
        //        at com.intellij.ide.actions.NewProjectAction.actionPerformed(NewProjectAction.java:36)
        LOG.debug("[FRC] FrcFrameworkSupportProvider.setupConfiguration() called");
        //TODO Let's see about auto downloading/attaching WpiLib and user lib
        //     Will need to coordinate with the code that detects unattached libs (in FrcProjectComponentImpl I believe) so that the user is not prompted while this is happening
    }
}
