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

package net.javaru.iip.frc.wpilib.gradlePluginRepo

import com.intellij.openapi.diagnostic.Logger
import net.javaru.iip.frc.util.mapExceptionFreeAndNotNull
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl
import org.intellij.lang.annotations.Language
import org.jdom2.Document
import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import org.jdom2.xpath.XPathFactory
import java.io.StringReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


val LOG = Logger.getInstance(MavenMetadata::class.java)

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
val dateTimeZonedFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss z")

data class MavenMetadata(val groupId: String,
                         val artifactId: String,
                         val version: String,
                         val latest: String,
                         val release: String,
                         val versions: List<String>,
                         val lastUpdated: String /* ex: 20191123193733 */
                         )

data class WpiLibMavenMetadata(val mavenMetadata: MavenMetadata)
{
    val wpiLibVersions: List<WpiLibVersion> = WpiLibVersionImpl.parse(mavenMetadata.versions).sorted()
    
    val versionAsWpiLibVersion = WpiLibVersionImpl.parse(mavenMetadata.version)
    val latestAsWpiLibVersion = WpiLibVersionImpl.parse(mavenMetadata.latest)
    val releaseAsWpiLibVersion = WpiLibVersionImpl.parse(mavenMetadata.release)
    val lastUpdatedAsDateTime: ZonedDateTime = ZonedDateTime.parse("${mavenMetadata.lastUpdated} UTC", dateTimeZonedFormatter)
}

fun parseMavenMetadata(@Language("XML") mavenMetadata: String): MavenMetadata?
{
    val document = try
    {
        val saxBuilder = SAXBuilder()
         saxBuilder.build(StringReader(mavenMetadata))
    }
    catch (e: Exception)
    {
        LOG.warn("[FRC] Could not convert the mavenMetadata XML to a Document object. Cause Details: $e", e)
        null
    }
    
    return if (document == null) null else mavenMetadata(document)
}

fun mavenMetadata(document: Document?): MavenMetadata?
{
    if (document == null) 
    {
        LOG.warn("[FRC] Could not parse the mavenMetadata Document to a MavenMetadata object as a null document was received.")
        return null
    }
    
    return try
    {
        val xPathFactory = XPathFactory.instance()
        val expression = xPathFactory.compile("//metadata", Filters.element())
        val metadataElement = expression.evaluateFirst(document)

        val groupIdElement = metadataElement.getChild("groupId")
        val groupId = groupIdElement.textNormalize

        val artifactIdElement = metadataElement.getChild("artifactId")
        val artifactId = artifactIdElement.textNormalize

        val versionElement = metadataElement.getChild("version")
        val version = versionElement.textNormalize

        val versioningElement = metadataElement.getChild("versioning")

        val latestElement = versioningElement.getChild("latest")
        val latest = latestElement.textNormalize

        val releaseElement = versioningElement.getChild("release")
        val release = releaseElement.textNormalize

        val lastUpdatedElement = versioningElement.getChild("lastUpdated")
        val lastUpdated = lastUpdatedElement.textNormalize


        val versionsElement = versioningElement.getChild("versions")
        val versionElements = versionsElement.getChildren("version")
        val versions = versionElements.mapExceptionFreeAndNotNull { it?.textNormalize }

        MavenMetadata(groupId, artifactId, version, latest, release, versions, lastUpdated)
    }
    catch (e: Exception)
    {
        LOG.warn("[FRC] Could not parse the mavenMetadata document to a MavenMetadata object. Cause Details: $e", e)
        null
    }
}


/*
<?xml version='1.0' encoding='US-ASCII'?>
<metadata>
    <groupId>edu.wpi.first.GradleRIO</groupId>
    <artifactId>edu.wpi.first.GradleRIO.gradle.plugin</artifactId>
    <version>2020.1.1-beta-3a</version>
    <versioning>
        <latest>2020.1.1-beta-3a</latest>
        <release>2020.1.1-beta-3a</release>
        <versions>
            <version>2018.06.21</version>
            <version>2019.0.0-alpha-1</version>
            <version>2019.0.0-alpha-2</version>
            <version>2019.0.0-alpha-3</version>
            <version>2019.0.0-beta0-pre1</version>
            <version>2019.0.0-beta0-pre3</version>
            <version>2019.0.0-beta0-pre4</version>
            <version>2019.0.0-beta0-pre5</version>
            <version>2019.0.0-beta0-pre6</version>
            <version>2019.1.1-beta-1</version>
            <version>2019.1.1-beta-2a</version>
            <version>2019.1.1-beta-3</version>
            <version>2019.1.1-beta-3a</version>
            <version>2019.1.1-beta-3-p-2</version>
            <version>2019.1.1-beta-3-pre3</version>
            <version>2019.1.1-beta-3-pre4</version>
            <version>2019.1.1-beta-3-pre5</version>
            <version>2019.1.1-beta-3-pre6</version>
            <version>2019.1.1-beta-3-pre7</version>
            <version>2019.1.1-beta-3-pre8</version>
            <version>2019.1.1-beta-3-pre9</version>
            <version>2019.1.1-beta-4</version>
            <version>2019.1.1-beta-4a</version>
            <version>2019.1.1-beta-4b</version>
            <version>2019.1.1-beta-4c</version>
            <version>2019.1.1-beta-4-pre1</version>
            <version>2019.1.1-beta-4-pre2</version>
            <version>2019.1.1-beta-4-pre4</version>
            <version>2019.1.1-rc-1</version>
            <version>2019.1.1</version>
            <version>2019.2.1</version>
            <version>2019.3.1</version>
            <version>2019.3.2</version>
            <version>2019.4.1</version>
            <version>2020.1.1-beta-1</version>
            <version>2020.1.1-beta-2</version>
            <version>2020.1.1-beta-3</version>
            <version>2020.1.1-beta-3a</version>
        </versions>
        <lastUpdated>20191123193733</lastUpdated>
    </versioning>
</metadata>
 */

