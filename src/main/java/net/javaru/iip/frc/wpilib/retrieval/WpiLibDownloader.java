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

package net.javaru.iip.frc.wpilib.retrieval;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;

import net.javaru.iip.frc.FrcIcons.FRC;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.util.UnzipUtils;
import net.javaru.iip.frc.util.UriUtils;
import net.javaru.iip.frc.wpilib.WpiLibPaths;



public class WpiLibDownloader
{
    private static final Logger LOG = Logger.getInstance(WpiLibDownloader.class);


    public static void downloadLatest() throws WpiLibDownloadFailedException
    {
        try
        {
            final Document siteXml = fetchSiteXml();
            final JavaFeatureDescriptor javaFeatureDescriptor = parseSiteXml(siteXml);


            //final Document javaFeatureXml = WpiRepoHttpClient.fetchXmlResourceAsDocument(javaFeatureDescriptor.getUri());

            // For now, as a quick hit to get the download working, we are not going to get and a parse the 
            // the feature XML and traverse down the chain. We know we wan the java and core JARs. We'll hard
            // code the names for now.

            final URI siteXmlUri = WpiRepoUris.getRepoSiteXmlFileUri();

            final URI javaJarUri = UriUtils.resolveSiblingResource(siteXmlUri,
                                                                   String.format("plugins/edu.wpi.first.wpilib.plugins.java_%s.jar",
                                                                                 javaFeatureDescriptor.getVersion()));


            final URI coreJarUri = UriUtils.resolveSiblingResource(siteXmlUri,
                                                                   String.format("plugins/edu.wpi.first.wpilib.plugins.core_%s.jar",
                                                                                 javaFeatureDescriptor.getVersion()));

            LOG.debug("[FRC] javaJarUri: " + javaJarUri);
            LOG.debug("[FRC] coreJarUri: " + coreJarUri);
            final Path javaJarFilePath = WpiRepoHttpClient.downloadAndSaveToTemp(javaJarUri);
            final Path coreJarFilePath = WpiRepoHttpClient.downloadAndSaveToTemp(coreJarUri);
            // The java.zip file has 3 directories in it:  ant;  javadoc;  lib;  And is extracted in 'current' so we end up with: 
            //      C:\Users\Dilbert\wpilib\java\current\ant   C:\Users\Dilbert\wpilib\java\current\javadoc   C:\Users\Dilbert\wpilib\java\current\lib
            extractZipFileContainedInZipFile(javaJarFilePath, "resources/java.zip", WpiLibPaths.getJavaCurrentDir());
            // The tools.zip content needs to go into C:\Users\Mark\wpilib\tools  so we end up with C:\Users\Mark\wpilib\tools\plugins  and  C:\Users\Mark\wpilib\tools\*.jar 
            extractZipFileContainedInZipFile(coreJarFilePath, "resources/tools.zip", WpiLibPaths.getToolsDir());


            Files.createDirectories(WpiLibPaths.getUserLibDir());
            createWpilibPropertiesFile();
            LOG.debug("[FRC] Download latest wpilib completed");
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] Could not download latest version of WPILib, Cause Summary: " + e.toString(), e);
            throw new WpiLibDownloadFailedException(e);
        }

    }


    public static Document fetchSiteXml() throws IOException, JDOMException
    {
        final URI siteXmlUri = WpiRepoUris.getRepoSiteXmlFileUri();
        final Document siteDocument = WpiRepoHttpClient.fetchXmlResourceAsDocument(siteXmlUri);
        return siteDocument;
    }


    public static JavaFeatureDescriptor parseSiteXml(Document siteDocument)
    {
        final XPathFactory xPathFactory = XPathFactory.instance();
        final XPathExpression<Element> expression = xPathFactory.compile("/site/feature[contains(@id, 'java')]", Filters.element());
        final Element javaFeatureElement = expression.evaluateFirst(siteDocument);
        final String id = javaFeatureElement.getAttribute("id").getValue();
        final String versionString = javaFeatureElement.getAttribute("version").getValue();
        //final JJWpiVersion version = new JJWpiVersion(versionString);
        final String javaFeatureRelativeUrl = javaFeatureElement.getAttribute("url").getValue();
        final URI javaFeatureUri = UriUtils.resolveSiblingResource(WpiRepoUris.getRepoSiteXmlFileUri(), javaFeatureRelativeUrl);

        return new JavaFeatureDescriptor(id, versionString, javaFeatureUri);
    }


    private static void extractZipFileContainedInZipFile(Path jarFilePath, String innerFilePath, Path destDir) throws IOException
    {
        ZipFile outerJarFile = new ZipFile(jarFilePath.toFile());
        //Will be null if not found
        final ZipEntry zipEntry = outerJarFile.getEntry(innerFilePath);
        
        LOG.debug("[FRC] Extracting '" + innerFilePath + "' from '" + jarFilePath.getFileName() +  "' to " + destDir);
        final BufferedInputStream inputStream = new BufferedInputStream(outerJarFile.getInputStream(zipEntry));
        UnzipUtils.unzip(inputStream, destDir, true);
    }


    private static void createWpilibPropertiesFile() throws IOException
    {
        final Path file = WpiLibPaths.getWpilibPropertiesFile();
        if (!Files.exists(file))
        {
            LOG.debug("[FRC] Creating wpilib.properties file at: " + file);

            //TODO: When team number moves to Facet, we need to get it from there
            FrcApplicationSettings settings = FrcApplicationSettings.Settings.INSTANCE();
            final AtomicInteger teamNumber = new AtomicInteger(settings.getTeamNumber());
            if (teamNumber.get() <= 0)
            {
                ApplicationManager.getApplication().invokeLater(() ->
                                                                {
                                                                    final String teamNumberInput =
                                                                        Messages.showInputDialog("FRC Team number:", "Team Number", FRC.FIRST_ICON_MEDIUM_16, null, new InputValidator()
                                                                        {
                                                                            @Override
                                                                            public boolean checkInput(String inputString)
                                                                            {
                                                                                try
                                                                                {
                                                                                    final int teamNum = Integer.parseInt(inputString);
                                                                                    return teamNum > 0;
                                                                                }
                                                                                catch (NumberFormatException ignore)
                                                                                {
                                                                                    return false;
                                                                                }
                                                                            }
                                            
                                            
                                                                            @Override
                                                                            public boolean canClose(String inputString)
                                                                            {
                                                                                return checkInput(inputString);
                                                                            }
                                                                        });
                                            
                                                                    if (teamNumberInput != null)
                                                                    {
                                                                        final int num = Integer.parseInt(teamNumberInput);
                                                                        teamNumber.set(num);
                                                                        settings.setTeamNumber(num);
                                                                    }
                                                                });

            }


            Files.createDirectories(file.getParent());
            final boolean append = true;
            final boolean autoFlush = true;
            //We don't use a FileWriter or the PrintWriter(File) constructor so we can specify the Character Set, which is IS_ 8859-1 for properties files 
            try (
                final PrintWriter writer
                    = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file.toFile(), append), StandardCharsets.ISO_8859_1), autoFlush)
            )
            {
                writer.println("#Don't add new properties, they will be deleted by the eclipse plugin.");
                writer.println(new SimpleDateFormat("'#'EEE MMM dd HH:mm:ss zzz yyyy").format(new Date()));
                writer.println("version=current");
                writer.println("team-number=" + teamNumber.get());
            }
        }
    }
}
