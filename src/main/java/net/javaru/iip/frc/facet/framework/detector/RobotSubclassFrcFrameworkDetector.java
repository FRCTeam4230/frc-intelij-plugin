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

package net.javaru.iip.frc.facet.framework.detector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.intellij.framework.detection.DetectedFrameworkDescription;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.framework.detection.FrameworkDetectionContext;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.indexing.FileContent;

import net.javaru.iip.frc.facet.FrcFacetConfiguration;
import net.javaru.iip.frc.run.RunDebugConfigsCreationData;
import net.javaru.iip.frc.wpilib.WpiLibConstants;

import static net.javaru.iip.frc.run.RunDebugConfigurationsKt.createAllRunDebugConfigurations;



//NOTE: Configured in plugin.xml
// REPLACED WITH WpiLibLibraryFrcFrameworkDetector which should be faster; will delete this once the new one has proven out
public class RobotSubclassFrcFrameworkDetector extends FrcAbstractFrameworkDetector
{
    private static final Logger LOG = Logger.getInstance(RobotSubclassFrcFrameworkDetector.class);
    
    // All robot classes extend RobotBase. Thus, we are technically fine just checking for that one.
    // But we include the others in the event WPI Lib modifies the inheritance hierarchy.
    private static final Set<String> SUPER_CLASSES_FQN = ImmutableSet.of(WpiLibConstants.ROBOT_BASE_FQN,
                                                                         WpiLibConstants.SAMPLE_ROBOT_FQN,
                                                                         WpiLibConstants.ITERATIVE_ROBOT_BASE_FQN,
                                                                         WpiLibConstants.ITERATIVE_ROBOT_FQN,
                                                                         WpiLibConstants.TIMED_ROBOT_FQN);

    private static final Set<String> SUPER_CLASSES_NAMES = initSuperClassNames();


    private static Set<String> initSuperClassNames()
    {
        final Builder<String> setBuilder = ImmutableSet.builder();
        for (String fqn : SUPER_CLASSES_FQN)
        {
            setBuilder.add(fqn);
            setBuilder.add(fqn.substring(fqn.lastIndexOf('.') + 1));
        }
        return setBuilder.build();
    }


    public RobotSubclassFrcFrameworkDetector()
    {
        super("FRC-viaRobotImpl", 1);
    }

    @NotNull
    @Override
    public FileType getFileType()
    {
        return JavaFileType.INSTANCE;
    }


    @NotNull
    @Override
    public ElementPattern<FileContent> createSuitableFilePattern()
    {
        return FileContentPattern.fileContent();
    }


    @Override
    public List<? extends DetectedFrameworkDescription> detect(@NotNull Collection<? extends VirtualFile> newFiles, @NotNull FrameworkDetectionContext context)
    {
        // ≤ 212  public abstract List<? extends DetectedFrameworkDescription> detect(@NotNull Collection<VirtualFile> newFiles, @NotNull FrameworkDetectionContext context);
        // ≥ 213  public abstract List<? extends DetectedFrameworkDescription> detect(@NotNull Collection<? extends VirtualFile> newFiles, @NotNull FrameworkDetectionContext context);
    
        // I'd like to look for both robot classes or the FRC ant build
        //   Using two separate detector impls results in two instances of the Facet being added
        //   Because a detector has to declare the FileType in getFileType(), I'm not sure how to get both Java and XML files
        //      If I do find a way, com.intellij.framework.detection.impl.FrameworkDetectionProcessor#collectSuitableFiles
        //      shows some code for sorting the file types.

        try
        {
            final List<VirtualFile> foundFiles = detectRobotClasses(newFiles, context);
            
            return (foundFiles.isEmpty()) ? Collections.emptyList() : context.createDetectedFacetDescriptions(this, foundFiles);
        }
        catch (ProcessCanceledException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] An exception occurred when checking for FRC Framework. Cause Summary: " + e, e);
            return Collections.emptyList();
        }
    }


    @NotNull
    private List<VirtualFile> detectRobotClasses(@NotNull Collection<? extends VirtualFile> newFiles, @NotNull FrameworkDetectionContext context)
    {
        final List<VirtualFile> foundFiles = new ArrayList<>(newFiles.size());
        final Project project = context.getProject();
        final String projectName = (project == null) ? "<project-is-null>" : project.getName();
        
        LOG.info("[FRC] FrameworkDetector checking if project named '" + projectName + "' for FRC Project");
        if (project != null)
        {
            for (VirtualFile virtualFile : newFiles)
            {
                final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile instanceof PsiJavaFile)
                {
                    if (checkIfRobotClass((PsiJavaFile) psiFile, project))
                    {
                        foundFiles.add(virtualFile);
                    }
                }
            }
        }
        else
        {
            for (VirtualFile virtualFile : newFiles)
            {
                if (checkIfExtendsRobot(virtualFile))
                {
                    foundFiles.add(virtualFile);
                }
            }
        }
        
        if (foundFiles.isEmpty()) {
            LOG.info("[FRC] FrameworkDetector found no FRC Robot based files for project named '" + projectName +"'.");
        }
        else {
            LOG.info("[FRC] FrameworkDetector found " + foundFiles.size() + " FRC Robot based files for project named '" + projectName +"'. Files: " + foundFiles);
            try { if (project != null) createAllRunDebugConfigurations(RunDebugConfigsCreationData.Companion.create(project));} catch (Throwable ignore) {}
        }
        
        return foundFiles;
    }


    private boolean checkIfExtendsRobot(@Nullable VirtualFile virtualFile)
    {
        try
        {
            if (virtualFile == null)
            {
                return false;
            }
            final String content = new String(virtualFile.contentsToByteArray());
            final Matcher matcher = WpiLibConstants.EXTENDS_A_ROBOT_REGEX.matcher(content);
            return matcher.find();

        }
        catch (ProcessCanceledException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] An exception occurred when checking if VirtualFile '" + virtualFile.getPath() + " extends Robot'. Cause Summary: " + e);
            return false;
        }
    }


    private boolean checkIfRobotClass(@Nullable PsiJavaFile psiFile, @NotNull Project project)
    {
        try
        {
            if (psiFile == null || project.isDisposed())
            {
                return false;
            }
            final PsiClass[] classes = psiFile.getClasses();
            if (LOG.isTraceEnabled()) {
                LOG.trace("[FRC] FrameworkDetector checking psiFile '" + psiFile.getName() + "'. Its classes are: " + Arrays.toString( classes));  
            }
            
            // This will only work if the WpiLib classes are on the classpath (i.e. added as a library)
            return hasFrcSuperClass(classes, project);
        }
        catch (ProcessCanceledException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] An exception occurred when checking if PsiJavaFile file '" + psiFile + "' ('" + psiFile.getVirtualFile().getPath()
                     + "') is a robot class. Cause Summary: " + e, e);
            return false;
        }
    }


    /**
     * Checks to see if any classes in a group is a subclass of one of the WPI Lib Robot classes. The code
     * does traverse all the way up the inheritance hierarchy; so a class does not directly have
     * to extend one of the Robt classes to be 'discovered'.
     * <b>This will only work if the WpiLib classes are on the classpath (i.e. added as a library).</b>
     *
     * @param classes the classes to check
     *
     * @param project the project
     * @return true if one of the classes extends an FRC robot class
     */
    private boolean hasFrcSuperClass(@NotNull PsiClass[] classes, @NotNull Project project)
    {
        for (PsiClass psiClass : classes)
        {
            if (project.isDisposed())
            {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[FRC] FrameworkDetector psiClass '" + psiClass.getQualifiedName() + "' does NOT have an FRC super class");
                }
                return false;
            }
            if (hasFrcSuperClass(psiClass, project, 0))
            {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[FRC] FrameworkDetector psiClass '" + psiClass.getQualifiedName() + "' DOES have an FRC super class");
                }
                return true;
            }
        }
        return false;
    }


    /**
     * Checks to see if a class is a subclass of one of the WPI Lib Robot classes. The code
     * does traverse all the way up the inheritance hierarchy; so a class does not directly have
     * to extend one of the Robt classes to be 'discovered'.
     * <b>This will only work if the WpiLib classes are on the classpath (i.e. added as a library).</b>
     *
     * @param psiClass       the class to check
     * @param project        the containing project
     * @param recursionCount the number of times the method has been called. Outside calls should set to zero
     *
     * @return true if the class extends an FRC robot class
     */
    private boolean hasFrcSuperClass(@Nullable PsiClass psiClass, Project project, int recursionCount)
    {
        try
        {
            if (psiClass == null || project.isDisposed())
            {
                return false;
            }
            
            if (SUPER_CLASSES_FQN.contains((psiClass.getQualifiedName())))
            {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("[FRC] FrameworkDetector psiClass '" + psiClass.getQualifiedName() + "' IS an FRC/WpiLib base robot class. Returning true hasFrcSuperClass().");
                }
                return true;
            }
            else if (extendsFrcRobot(psiClass))
            {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("[FRC] FrameworkDetector psiClass '" + psiClass.getQualifiedName() + "' extends an FRC/WpiLib base robot class. Returning true for hasFrcSuperClass.");
                }
                return true;
            }
            else
            {
                PsiClass superClass = psiClass.getSuperClass();
                if (superClass == null || "java.lang.Object".equals(superClass.getQualifiedName())) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("[FRC] FrameworkDetector psiClass '" + psiClass.getQualifiedName() + "' is not, nor does it extend, an FRC/WpiLib base robot class. It does not have a Superclass (other than java.lang.Object). Returning false for hasFrcSuperClass() for this class." );
                    }
                    return false;
                }
                if (LOG.isTraceEnabled()) {
                    LOG.trace("[FRC] FrameworkDetector psiClass '" + psiClass.getQualifiedName() + "' is not, nor does it extend, an FRC/WpiLib base robot class. Checking it super class " + superClass);
                }
                // Issue 16: A Stackoverflow occurred of well over 1000 calls to the below recursive calls.
                //           Not sure what class caused it. But to prevent the issue, we limit the traversal
                //           of super classes to a depth of 33, which is way more than is ever likely for a robot project
                return (++recursionCount <= 32) && hasFrcSuperClass(superClass, project, recursionCount);
            }
        }
        catch (ProcessCanceledException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] An exception occurred when checking for FRC Super Class on psiClass '" + psiClass.getQualifiedName() + "'. Cause Summary: "
                     + e, e);
            return false;
        }
    }


    private boolean extendsFrcRobot(@Nullable PsiClass psiClass)
    {
        if (psiClass == null)
        {
            return false;
        }

        final PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();
        for (PsiClassType extendsListType : extendsListTypes)
        {
            final String extendsClassName = extendsListType.getClassName();
            if (SUPER_CLASSES_NAMES.contains(extendsClassName))
            {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Override this method if only one facet of this type are allowed in a single module
     *
     * @param files files accepted by detector's filter
     *
     * @return configuration for detected facet
     */
    @SuppressWarnings("UseOfConcreteClass")
    @Nullable
    @Override
    protected FrcFacetConfiguration createConfiguration(Collection<? extends VirtualFile> files)
    {
        //  ≤ 212  protected C createConfiguration(Collection<VirtualFile> files)
        //  ≥ 213  protected C createConfiguration(Collection<? extends VirtualFile> files)
        LOG.debug("[FRC] createConfiguration(files) called in RobotSubclassFrcFrameworkDetector");
        return getFacetType().createDefaultConfiguration();
    }
}
