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

package net.javaru.iip.frc.facet.detector;

import java.util.ArrayList;
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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.util.indexing.FileContent;

import net.javaru.iip.frc.wpilib.WpiLibConstants;



//NOTE: Configured in plugin.xml
public class RobotSubclassFrcFrameworkDetector extends FrcAbstractFrameworkDetector
{
    private static final Logger LOG = Logger.getInstance(RobotSubclassFrcFrameworkDetector.class);
    
    private static final Set<String> SUPER_CLASSES_FQN = ImmutableSet.of(WpiLibConstants.ITERATIVE_ROBOT_FQN,
                                                                         WpiLibConstants.ROBOT_BASE_FQN);

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
        return StdFileTypes.JAVA;
    }


    @NotNull
    @Override
    public ElementPattern<FileContent> createSuitableFilePattern()
    {
        return FileContentPattern.fileContent();
    }


    @Override
    public List<? extends DetectedFrameworkDescription> detect(@NotNull Collection<VirtualFile> newFiles, @NotNull FrameworkDetectionContext context)
    {
        // I'd like to look for both robot classes or the FRC ant build
        //   Using two separate detector impls results in two instances of the Facet being added
        //   Because a detector has to declare the FileType in getFileType(), I'm not sure how to get both Java and XML files
        //      If I do find a way, com.intellij.framework.detection.impl.FrameworkDetectionProcessor#collectSuitableFiles
        //      shows some code for sorting the file types.

//        final List<VirtualFile> foundFiles = new ArrayList<>(newFiles.size() + 4);
//        foundFiles.addAll(detectRobotClasses(newFiles, context));
        final List<VirtualFile> foundFiles = detectRobotClasses(newFiles, context);
        return (foundFiles.isEmpty()) ? Collections.emptyList() : context.createDetectedFacetDescriptions(this, foundFiles);
    }


    @NotNull
    private List<VirtualFile> detectRobotClasses(@NotNull Collection<VirtualFile> newFiles, @NotNull FrameworkDetectionContext context)
    {
        final List<VirtualFile> foundFiles = new ArrayList<>(newFiles.size());
        if (context.getProject() != null)
        {
            for (VirtualFile virtualFile : newFiles)
            {
                final PsiFile psiFile = PsiManager.getInstance(context.getProject()).findFile(virtualFile);
                if (psiFile instanceof PsiJavaFileImpl)
                {
                    if (checkIfRobotClass((PsiJavaFileImpl) psiFile))
                    {
                        foundFiles.add(virtualFile);
                    }
                }
            }
        }
        else //if (context instanceof ???)
        {
            for (VirtualFile virtualFile : newFiles)
            {
                if (checkIfExtendsRobot(virtualFile))
                {
                    foundFiles.add(virtualFile);
                }
            }
        }
        return foundFiles;
    }


    private boolean checkIfExtendsRobot(VirtualFile virtualFile)
    {
        try
        {
            final String content = new String(virtualFile.contentsToByteArray());
            final Matcher matcher = WpiLibConstants.EXTENDS_A_ROBOT_REGEX.matcher(content);
            return matcher.find();

        }
        catch (Exception e)
        {
            LOG.warn("[FRC] Exception when processing VirtualFile '" + virtualFile.getPath() + "'. Cause Summary: " + e.toString());
            return false;
        }
    }


    private boolean checkIfRobotClass(PsiJavaFileImpl psiFile)
    {
        final PsiClass[] classes = psiFile.getClasses();
        // This will only work if the WpiLib classes are on the classpath (i.e. added as a library)
        return hasFrcSuperClass(classes);
    }


    /**
     * Checks to see if any classes in a group is a sub-class of one of the WPI Lib Robot classes. The code
     * does traverse all the way up the inheritance hierarchy; so a class does not directly have
     * to extend ne of the Robt classes to be 'discovered'.
     * <b>This will only work if the WpiLib classes are on the classpath (i.e. added as a library).</b>
     *
     * @param classes the classes to check
     *
     * @return true if one of the classes extends an FRC robot class
     */
    private boolean hasFrcSuperClass(PsiClass[] classes)
    {
        for (PsiClass psiClass : classes)
        {
            if (hasFrcSuperClass(psiClass))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks to see if a class is a sub-class of one of the WPI Lib Robot classes. The code
     * does traverse all the way up the inheritance hierarchy; so a class does not directly have
     * to extend ne of the Robt classes to be 'discovered'.
     * <b>This will only work if the WpiLib classes are on the classpath (i.e. added as a library).</b>
     *
     * @param psiClass the class to check
     *
     * @return true if the class extends an FRC robot class
     */
    @SuppressWarnings("SimplifiableIfStatement")
    private boolean hasFrcSuperClass(@Nullable PsiClass psiClass)
    {
        if (psiClass == null)
        {
            return false;
        }

        if (SUPER_CLASSES_FQN.contains((psiClass.getQualifiedName())))
        {
            return true;
        }
        else if (extendsFrcRobot(psiClass))
        {
            return true;
        }
        else
        {
            return hasFrcSuperClass(psiClass.getSuperClass());
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
}
