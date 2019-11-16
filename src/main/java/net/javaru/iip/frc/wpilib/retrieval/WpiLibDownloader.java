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

package net.javaru.iip.frc.wpilib.retrieval;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;

import net.javaru.iip.frc.FrcIcons.FRC;
import net.javaru.iip.frc.components.FrcProjectComponentImpl;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.util.UnzipUtils;
import net.javaru.iip.frc.util.UriUtils;
import net.javaru.iip.frc.wpilib.WpiLibPaths;
import net.javaru.iip.frc.wpilib.version.WpiLibVersion;
import net.javaru.iip.frc.wpilib.version.WpiLibVersionExtKt;
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl;



public class WpiLibDownloader
{
    private static final Logger LOG = Logger.getInstance(WpiLibDownloader.class);


    /**
     * Returns the latest JavaFeatureDescriptor (which includes a version property) and the repo URI it came from.
     * Thus if the use beta site is enabled, either the latest beta or the latest release will be returned.
     * @return the latest JavaFeatureDescriptor (which includes a version property) and the repo URI it came from
     */
    public static ImmutablePair<JavaFeatureDescriptor, URI> downloadLatestJavaDescriptor() throws WpiLibDownloadFailedException
    {
        try
        {
            final URI betaRepoUri = WpiRepoUris.getBetaRepoUri();
            final URI releaseRepoUri = WpiRepoUris.getReleaseRepoUri();
            
            @Nullable
            final JavaFeatureDescriptor betaDescriptor = WpiRepoUris.useBetaRepo() ? parseSiteXml(fetchSiteXml(betaRepoUri), betaRepoUri) : null ;
            final JavaFeatureDescriptor releaseDescriptor = parseSiteXml(fetchSiteXml(releaseRepoUri), releaseRepoUri);
            
            if (betaDescriptor == null || releaseDescriptor.getVersion().isNewerThan(betaDescriptor.getVersion()))
            {
                return ImmutablePair.of(releaseDescriptor, releaseRepoUri);
            }
            else
            {
                return ImmutablePair.of(betaDescriptor, betaRepoUri); 
            }
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] Could not check for the latest version of WPILib. Cause Summary: " + e.toString(), e);
            throw new WpiLibDownloadFailedException(e);
        }
    }


    /**
     * Returns the latest version of the WpiLib available, or {@code null} if the version could not be retrieved. 
     * @return the latest version of the WpiLib available, or {@code null} if the version could not be retrieved.
     */
    @Nullable
    public static WpiLibVersion getLatestVersionAvailable()
    {
        try
        {
            return downloadLatestJavaDescriptor().getKey().getVersion();
        }
        catch (Exception ignore)
        {
            return null;
        }
    }
    
    public static void downloadLatest() throws WpiLibDownloadFailedException
    {
        try
        {
            // We do the properties file update first so that if the team number id not set,
            // The user is prompted immediately after starting the action, rather than a few
            // minutes after the download completes
            updateOrCreateWpilibPropertiesFile();
            
            final ImmutablePair<JavaFeatureDescriptor, URI> latest = downloadLatestJavaDescriptor();
            final JavaFeatureDescriptor javaFeatureDescriptor = latest.getKey();
            LOG.info("[FRC] Current WPILib version (as indicated in 'site.xml') is '" + javaFeatureDescriptor.getVersion() + "'");

            //final Document javaFeatureXml = WpiRepoHttpClient.fetchXmlResourceAsDocument(javaFeatureDescriptor.getUri());

            // For now, as a quick hit to get the download working, we are not going to get and a parse the 
            // the feature XML and traverse down the chain. We know we wan the java and core JARs. We'll hard
            // code the names for now.
            // TODO: parse the site.xml in the event things change like they did in 2018 ;)

            final URI siteXmlUri = WpiRepoUris.getSiteUri(latest.getValue());

            final URI javaJarUri = UriUtils.resolveSiblingResource(siteXmlUri,
                                                                   String.format("plugins/edu.wpi.first.wpilib.plugins.java_%s.jar",
                                                                                 javaFeatureDescriptor.getVersion().getVersionString()));


            final URI coreJarUri = UriUtils.resolveSiblingResource(siteXmlUri,
                                                                   String.format("plugins/edu.wpi.first.wpilib.plugins.core_%s.jar",
                                                                                 javaFeatureDescriptor.getVersion().getVersionString()));

            LOG.info("[FRC] Using javaJarUri: " + javaJarUri);
            LOG.info("[FRC] Using coreJarUri: " + coreJarUri);
            final Path javaJarFilePath = WpiRepoHttpClient.downloadAndSaveToTemp(javaJarUri);
            final Path coreJarFilePath = WpiRepoHttpClient.downloadAndSaveToTemp(coreJarUri);
            // The java.zip file has 3 directories in it:  ant;  javadoc;  lib;  And is extracted in 'current' so we end up with: 
            //      C:\Users\UserName\wpilib\java\current\ant   C:\Users\UserName\wpilib\java\current\javadoc   C:\Users\UserName\wpilib\java\current\lib
            extractZipFileContainedInZipFile(javaJarFilePath, "resources/java.zip", WpiLibPaths.getJavaCurrentDir());
            // The tools.zip content needs to go into C:\Users\UserName\wpilib\tools  so we end up with C:\Users\UserName\wpilib\tools\plugins  and  C:\Users\UserName\wpilib\tools\*.jar 
            extractZipFileContainedInZipFile(coreJarFilePath, "resources/tools.zip", WpiLibPaths.getToolsDir());

            // common.zip was added in 2018 and contains the JRE that is deployed to the roboRIO upon building
            //     It's content goes to C:\Users\UserName\wpilib\common
            //     We ultimately end up with C:\Users\UserName\wpilib\common\current\lib\linux\athena\shared which contains a number or *.so files
            if (WpiLibVersionExtKt.is2018OrLater(javaFeatureDescriptor.getVersion()))
            {
                extractZipFileContainedInZipFile(coreJarFilePath, "resources/common.zip", WpiLibPaths.getCommonCurrentVersionDir());
            }
            
            Files.createDirectories(WpiLibPaths.getUserLibDir());
            
            LOG.debug("[FRC] Download & extraction of latest wpilib completed");
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] Could not download latest version of WPILib. Cause Summary: " + e.toString(), e);
            throw new WpiLibDownloadFailedException(e);
        }

    }


    public static Document fetchSiteXml(@NotNull URI repoBaseUri) throws IOException, JDOMException
    {
        final URI siteXmlUri = WpiRepoUris.getSiteUri(repoBaseUri);
        final Document siteDocument = WpiRepoHttpClient.fetchXmlResourceAsDocument(siteXmlUri);
        return siteDocument;
    }


    public static JavaFeatureDescriptor parseSiteXml(Document siteDocument, @NotNull URI repoBaseUri)
    {
        final XPathFactory xPathFactory = XPathFactory.instance();
        final XPathExpression<Element> expression = xPathFactory.compile("/site/feature[contains(@id, 'java')]", Filters.element());
        final Element javaFeatureElement = expression.evaluateFirst(siteDocument);
        final String id = javaFeatureElement.getAttribute("id").getValue();
        final String versionString = javaFeatureElement.getAttribute("version").getValue();
        WpiLibVersion version = WpiLibVersionImpl.Companion.parse(versionString);
        final String javaFeatureRelativeUrl = javaFeatureElement.getAttribute("url").getValue();
        final URI javaFeatureUri = UriUtils.resolveSiblingResource(WpiRepoUris.getSiteUri(repoBaseUri), javaFeatureRelativeUrl);

        return new JavaFeatureDescriptor(id, version, javaFeatureUri);
    }


    private static void extractZipFileContainedInZipFile(Path jarFilePath, String innerFilePath, Path destDir) throws IOException
    {
        ZipFile outerJarFile = new ZipFile(jarFilePath.toFile());
        // Will be null if not found
        final ZipEntry zipEntry = outerJarFile.getEntry(innerFilePath);
        if (zipEntry == null)
        {
            LOG.warn("[FRC] '" + innerFilePath + "' was not found in '" + jarFilePath.getFileName() + "'. Cannot extract it to " +destDir);
            throw  new FileNotFoundException("'" + innerFilePath + "' was not found in '" + jarFilePath.getFileName() + "' and could not be extracted to " + destDir);
        }
        else 
        {
            LOG.info("[FRC] Extracting '" + innerFilePath + "' and its contents from '" + jarFilePath.getFileName() + "' to " + destDir);
            final BufferedInputStream inputStream = new BufferedInputStream(outerJarFile.getInputStream(zipEntry));
            UnzipUtils.unzip(inputStream, destDir, true);
        }
    }


    public static void updateOrCreateWpilibPropertiesFile() throws IOException
    {
        final Path file = WpiLibPaths.getWpilibPropertiesFile();

        LOG.debug("[FRC] Creating / Updating wpilib.properties file at: " + file);
     
        if (!FrcApplicationSettings.getInstance().isTeamNumberConfigured())
        {
            ApplicationManager.getApplication().invokeAndWait(WpiLibDownloader::promptForTeamNumber);
        }

        if (Files.exists(file))
        {
            if (!Files.isWritable(file))
            {
                throw new IOException("File write access denied for file " + file );
            }
            
            if (!Files.isReadable(file))
            {
                throw new IOException("File read access denied for file " + file);
            }
        }
        
        Files.createDirectories(file.getParent());
        final boolean append = false;
        final boolean autoFlush = true;
        //We don't use a FileWriter or the PrintWriter(File) constructor so we can specify the Character Set, which is ISO 8859-1 for properties files 
        try (
            final PrintWriter writer
                = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file.toFile(), append), StandardCharsets.ISO_8859_1), autoFlush)
        )
        {
            writer.println("#Don't add new properties, they will be deleted by the eclipse and/or IntelliJ IDEA plugin.");
            writer.println(new SimpleDateFormat("'#'EEE MMM dd HH:mm:ss zzz yyyy").format(new Date()));
            writer.println("version=current");
            writer.println("team-number=" + FrcApplicationSettings.getInstance().getTeamNumber());
        }
    }
    

    private static void promptForTeamNumber()
    {
        final String teamNumberInput =
            Messages.showInputDialog("<html>Your FRC Team Number is needed <br>to properly configure WPILib.<br><br>FRC Team Number:<html>", "Team Number", FRC.FIRST_ICON_MEDIUM_16, null, new InputValidator()
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
            FrcApplicationSettings.getInstance().setTeamNumber(num);
        }
        else
        {
            // The user canceled on the input prompt
            final Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : projects)
            {
                if (FrcProjectComponentImpl.isFrcFacetedProject(project))
                {
                    FrcNotifications.Companion.notifyAboutTeamNumberNeedingToBeConfigured(project, true, true);
                }
            }
        }
    }
}
