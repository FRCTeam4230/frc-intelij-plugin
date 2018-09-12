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

package net.javaru.iip.frc.facet.framework.detector;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jetbrains.annotations.NotNull;
import com.intellij.framework.detection.DetectedFrameworkDescription;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.framework.detection.FrameworkDetectionContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.util.indexing.FileContent;



// Unused at this time because if I configure two detectors in the plugin.xml, it adds two instances 
// of the FRC facet if both the ant file and a Robot class exist. And that causes issues.
// I'm a bit surprised since FrcFacetType.isOnlyOneFacetAllowed() returns true 
// To use - configure in plugin.xml
public class AntBuildFrcFrameworkDetector extends FrcAbstractFrameworkDetector
{
    private static final Logger LOG = Logger.getInstance(AntBuildFrcFrameworkDetector.class);

    private final XPathExpression<Element> xPathExpression = XPathFactory.instance()
                                                                         .compile("//project/property[@file] | //bookstore/import[@file]", Filters.element());


    public AntBuildFrcFrameworkDetector()
    {
        super("FRC-viaAntBuild", 1);
    }


    @NotNull
    @Override
    public FileType getFileType()
    {
        //return XmlFileType.INSTANCE;
        return StdFileTypes.XML;
    }


    @NotNull
    @Override
    public ElementPattern<FileContent> createSuitableFilePattern()
    {
        return FileContentPattern.fileContent().withName("build.xml").xmlWithRootTag("project");
    }


    @Override
    public List<? extends DetectedFrameworkDescription> detect(@NotNull Collection<VirtualFile> newFiles, @NotNull FrameworkDetectionContext context)
    {
        final Collection<VirtualFile> foundFiles = new ArrayList<>(newFiles.size());

        for (VirtualFile virtualFile : newFiles)
        {
            try (InputStream inputStream = virtualFile.getInputStream())
            {
                final Document document = new SAXBuilder().build(inputStream);
                final List<Element> elements = xPathExpression.evaluate(document);
                for (Element element : elements)
                {
                    final Attribute attribute = element.getAttribute("file");
                    String value = attribute.getValue();
                    if (value != null)
                    {
                        value = value.toLowerCase();
                        if (value.contains("wpilib") || value.contains("wpi-lib"))
                        {
                            foundFiles.add(virtualFile);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                LOG.warn("[FRC] an exception occurred during facet detection (via Ant build detection): " + e.toString());
            }
        }

        if (foundFiles.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            return context.createDetectedFacetDescriptions(this, foundFiles);
        }
    }
}

/* 
    Example project root build file:
    
    ============================================================================
    <?xml version="1.0" encoding="UTF-8"?>
    
    <project name="FRC Deployment" default="deploy">
    
      <!--
      The following properties can be defined to override system level
      settings. These should not be touched unless you know what you're
      doing. The primary use is to override the wpilib version when
      working with older robots that can't compile with the latest
      libraries.
      -->
    
      <!-- By default the system version of WPI is used -->
      <!-- <property name="version" value=""/> -->
    
      <!-- By default the system team number is used -->
      <!-- <property name="team-number" value=""/> -->
    
      <!-- By default the target is set to 10.TE.AM.2 -->
      <!-- <property name="target" value=""/> -->
    
      <!-- Any other property in build.properties can also be overridden. -->
    
      <property file="${user.home}/wpilib/wpilib.properties"/>
      <property file="build.properties"/>
      <property file="${user.home}/wpilib/java/${version}/ant/build.properties"/>
    
      <import file="${wpilib.ant.dir}/build.xml"/>
    
    </project>
    ============================================================================

 */
