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

import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.XmlSerializerUtil
import net.javaru.iip.frc.net.FrcHttpClient
import net.javaru.iip.frc.util.mapExceptionFreeAndNotNull
import net.javaru.iip.frc.util.mapper
import net.javaru.iip.frc.util.prettyPrintWriter
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl
import org.intellij.lang.annotations.Language
import org.jdom2.Document
import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import org.jdom2.xpath.XPathFactory
import java.io.StringReader
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


val LOG = Logger.getInstance(MavenMetadata::class.java)

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
val dateTimeZonedFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss z")
val wpiLibGradlePluginMavenMetadataURI = URI("https://plugins.gradle.org/m2/edu/wpi/first/GradleRIO/edu.wpi.first.GradleRIO.gradle.plugin/maven-metadata.xml")

// NOTE: This class is registered as an <applicationService> in the plugin.xml
@Suppress("MemberVisibilityCanBePrivate", "unused")
@State(name = "GradleRioMavenMetadata", storages = [(Storage("frc.xml"))])
/**
 * A `PersistentStateComponent` that stores the last retrieved `maven-metadata.xml` information for the GradleRIO plugin.
 * It stores a `MavenMetadata` instance as JSON (since  the persistent API does not store complex objects without additional
 * development work). It then stores the [lastChecked] time. The `lastChecked` time should not be confused with the 
 * `lastUpdated` property of the `MavenMetadata` instance. The `MavenMetadata`'s `lastUpdated` property is contained within 
 * the `maven-metadata.xml` and represents the last time the Gradle Repo was updated with a new instance of the artifact.
 * The `lastChecked` time of this State class is the last time we have checked the Gradle Repo to see if the 
 * `maven-metadata.xml` file has been modified (and thus presumably the latest version of the GradleRIO plugin).
 */
data class GradleRioMavenMetadataState(@Language("JSON") var mavenMetadataJson: String = defaultMavenMetadataJson, 
                                       var lastChecked: String = "20191123193733") :
        PersistentStateComponent<GradleRioMavenMetadataState>
{
    val lastCheckedDateTime: LocalDateTime
        get() = LocalDateTime.parse(lastChecked, dateTimeFormatter)
    
    val durationSinceLastCheck: Duration
        get() = Duration.between(lastCheckedDateTime, LocalDateTime.now())
    
    val daysSinceLastCheck: Long
        get() = durationSinceLastCheck.toDays()
    
    val hoursSinceLastCheck: Long
        get() = durationSinceLastCheck.toHours()
    
    @Suppress("RemoveExplicitTypeArguments")
    val wpiLibMavenMetadata: WpiLibMavenMetadata
        get() = WpiLibMavenMetadata(MavenMetadata.fromJson(mavenMetadataJson))
    
    fun setMavenMetadata(metadata: MavenMetadata)
    {
        mavenMetadataJson = metadata.toJson(false)
    }

    /**
     * @return a component state. All properties, public and annotated fields are serialized. Only values, which differ
     * from the default (i.e., the value of newly instantiated class) are serialized. `null` value indicates
     * that the returned state won't be stored, as a result previously stored state will be used.
     * @see com.intellij.util.xmlb.XmlSerializer
     */
    override fun getState(): GradleRioMavenMetadataState? = this

    /**
     * This method is called when new component state is loaded. The method can and will be called several times, if
     * config files were externally changed while IDE was running.
     *
     *
     * State object should be used directly, defensive copying is not required.
     *
     * @param state loaded component state
     * @see com.intellij.util.xmlb.XmlSerializerUtil.copyBean
     */
    override fun loadState(state: GradleRioMavenMetadataState) = XmlSerializerUtil.copyBean(state, this)
    
    @JvmOverloads
    fun updateLastCheckedTime(checkTime: LocalDateTime = LocalDateTime.now())
    {
        lastChecked = dateTimeFormatter.format(checkTime)
    }

    companion object
    {
        @JvmOverloads
        @JvmStatic
        fun getInstance(checkForUpdate: Boolean = false): GradleRioMavenMetadataState
        {
            if (checkForUpdate)
            {
                updateFromRepo()
            }
            return ServiceManager.getService(GradleRioMavenMetadataState::class.java)
        }   

        @JvmStatic
        fun clone(original: GradleRioMavenMetadataState): GradleRioMavenMetadataState
        {
            return original.copy()
        }


        @JvmOverloads
        @JvmStatic
        fun updateState(updated: WpiLibMavenMetadata, checkTime: LocalDateTime = LocalDateTime.now())
        {
            updateState(updated.mavenMetadata, checkTime)
        }

        @JvmOverloads
        @JvmStatic
        fun updateState(updated: MavenMetadata, checkTime: LocalDateTime = LocalDateTime.now())
        {
            val state = ServiceManager.getService(GradleRioMavenMetadataState::class.java)
            state.mavenMetadataJson = updated.toJson(false)
            state.updateLastCheckedTime(checkTime)
        }


        @JvmOverloads
        @JvmStatic
        fun updateState(updated: GradleRioMavenMetadataState, checkTime: LocalDateTime = LocalDateTime.now())
        {
            val state = ServiceManager.getService(GradleRioMavenMetadataState::class.java)
            state.mavenMetadataJson = updated.mavenMetadataJson
            state.updateLastCheckedTime(checkTime)
        }

        /**
         * Checks the Gradle Repo for an update to the MavenMetadata for the GradleRIO. If there is an newer version
         * of the metadata available, the `GradleRioMavenMetadataState` is updated (including the `lastChecked` time).
         * If there is not a newer version available, the `lastChecked` is updated. In the event the repo cannot be checked,
         * no update is made to either the state or the `lastChecked` timestamp.
         *
         * @return `true` if the State has been updated as a result of checking the repo;
         *         `false` if there is not a newer version, or the repo could not be checked.
         */
        fun updateFromRepo(): Boolean
        {
            return try
            {
                val fetched = fetchGradleRioMavenMetadata()
                val state = ServiceManager.getService(GradleRioMavenMetadataState::class.java)

                return when (fetched)
                {
                    null                      ->
                    {
                        // CASE 1: Could not download the most recent
                        false
                    }
                    state.wpiLibMavenMetadata ->
                    {
                        // CASE 2: No change. But we want to change the last checked date
                        state.updateLastCheckedTime()
                        false
                    }
                    else                      ->
                    {
                        // CASE 3: There is a new version, so we need to update
                        updateState(fetched)
                        true
                    }
                }
            }
            catch (e: Exception)
            {
                LOG.warn("[FRC] Could not complete a check for a new version of WPI Lib from the Gradle Repo due to an exception: $e", e)
                false
            }
        }
    }
}

/** A data class that represents a `maven-metadata.xml` file (which can be metadata for for any artifact). */
data class MavenMetadata(val groupId: String,
                         val artifactId: String,
                         val version: String,
                         val latest: String,
                         val release: String,
                         val versions: List<String>,
                         val lastUpdated: String /* ex: 20191123193733 */
                         )
{
    @Language("JSON")
    fun toJson(prettyPrint: Boolean): String 
    {
        val writer: ObjectWriter = if (prettyPrint) prettyPrintWriter else mapper.writer()
        return writer.writeValueAsString(this)
    }
    
    companion object
    {
        @JvmStatic
        fun fromJson(@Language("JSON") json: String): MavenMetadata = mapper.readValue<MavenMetadata>(json)
    }
    
}

/** 
 * A data class to represent a `maven-metadata.xml` file for a WpiLib artifact such as the GradleRIO plugin. 
 * It wraps a [mavenMetadata] instance, providing convenience methods to get various properties as `WpiLibVersion` 
 * instances rather than Strings, as well as some date and time values as DateTime instances.
 */
data class WpiLibMavenMetadata(val mavenMetadata: MavenMetadata)
{
    val wpiLibVersions: List<WpiLibVersion> by lazy { WpiLibVersionImpl.parse(mavenMetadata.versions).sorted() }
    /** returns a descending list of the versions, such that the newest version is first */
    val wpiLibVersionsDescending by lazy { wpiLibVersions.reversed() }
    val versionAsWpiLibVersion by lazy { WpiLibVersionImpl.parse(mavenMetadata.version) }
    val latestAsWpiLibVersion by lazy { WpiLibVersionImpl.parse(mavenMetadata.latest) } 
    val releaseAsWpiLibVersion by lazy { WpiLibVersionImpl.parse(mavenMetadata.release) }
    val lastUpdatedAsDateTime: ZonedDateTime by lazy {ZonedDateTime.parse("${mavenMetadata.lastUpdated} UTC", dateTimeZonedFormatter) }
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
    
    return if (document == null) null else parseMavenMetadata(document)
}

fun parseMavenMetadata(document: Document?): MavenMetadata?
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

@Suppress("unused")
@Language("JSON")
fun createJsonForLatestMetadata(prettyPrint: Boolean = false): String
{
    val gradleRioMavenMetadata = fetchGradleRioMavenMetadata()
    if (gradleRioMavenMetadata == null)
    {
        throw RuntimeException("Could not retrieve the latest maven-metadata for the GradleRIO Plugin")
    }
    else
    {
        return gradleRioMavenMetadata.mavenMetadata.toJson(prettyPrint)
    }
}



fun fetchGradleRioMavenMetadata(): WpiLibMavenMetadata?
{
    return try
    {
        LOG.debug("[FRC] Checking Gradle Repo for GradleRio MavenMetadata update")
        val mavenMetadataDocument = FrcHttpClient.fetchXmlResourceAsDocument(wpiLibGradlePluginMavenMetadataURI)
        val mavenMetadata = parseMavenMetadata(mavenMetadataDocument)
        if (mavenMetadata == null) null else WpiLibMavenMetadata(mavenMetadata)
    }
    catch (e: Exception)
    {
        LOG.warn("[FRC] Could not complete a check for a new version of WPI Lib from the Gradle Repo due to an exception: $e", e)
        null
    }
}

@Language("JSON")
val defaultMavenMetadataJson = """{"groupId":"edu.wpi.first.GradleRIO","artifactId":"edu.wpi.first.GradleRIO.gradle.plugin","version":"2020.1.1-beta-3a","latest":"2020.1.1-beta-3a","release":"2020.1.1-beta-3a","versions":["2018.06.21","2019.0.0-alpha-1","2019.0.0-alpha-2","2019.0.0-alpha-3","2019.0.0-beta0-pre1","2019.0.0-beta0-pre3","2019.0.0-beta0-pre4","2019.0.0-beta0-pre5","2019.0.0-beta0-pre6","2019.1.1-beta-1","2019.1.1-beta-2a","2019.1.1-beta-3","2019.1.1-beta-3a","2019.1.1-beta-3-p-2","2019.1.1-beta-3-pre3","2019.1.1-beta-3-pre4","2019.1.1-beta-3-pre5","2019.1.1-beta-3-pre6","2019.1.1-beta-3-pre7","2019.1.1-beta-3-pre8","2019.1.1-beta-3-pre9","2019.1.1-beta-4","2019.1.1-beta-4a","2019.1.1-beta-4b","2019.1.1-beta-4c","2019.1.1-beta-4-pre1","2019.1.1-beta-4-pre2","2019.1.1-beta-4-pre4","2019.1.1-rc-1","2019.1.1","2019.2.1","2019.3.1","2019.3.2","2019.4.1","2020.1.1-beta-1","2020.1.1-beta-2","2020.1.1-beta-3","2020.1.1-beta-3a"],"lastUpdated":"20191123193733"}"""


@Suppress("unused")
@Language("JSON")
val defaultMavenMetadataJsonPretty = 
        """
            {
              "groupId" : "edu.wpi.first.GradleRIO",
              "artifactId" : "edu.wpi.first.GradleRIO.gradle.plugin",
              "version" : "2020.1.1-beta-3a",
              "latest" : "2020.1.1-beta-3a",
              "release" : "2020.1.1-beta-3a",
              "versions" : [ "2018.06.21", "2019.0.0-alpha-1", "2019.0.0-alpha-2", "2019.0.0-alpha-3", "2019.0.0-beta0-pre1", "2019.0.0-beta0-pre3", "2019.0.0-beta0-pre4", "2019.0.0-beta0-pre5", "2019.0.0-beta0-pre6", "2019.1.1-beta-1", "2019.1.1-beta-2a", "2019.1.1-beta-3", "2019.1.1-beta-3a", "2019.1.1-beta-3-p-2", "2019.1.1-beta-3-pre3", "2019.1.1-beta-3-pre4", "2019.1.1-beta-3-pre5", "2019.1.1-beta-3-pre6", "2019.1.1-beta-3-pre7", "2019.1.1-beta-3-pre8", "2019.1.1-beta-3-pre9", "2019.1.1-beta-4", "2019.1.1-beta-4a", "2019.1.1-beta-4b", "2019.1.1-beta-4c", "2019.1.1-beta-4-pre1", "2019.1.1-beta-4-pre2", "2019.1.1-beta-4-pre4", "2019.1.1-rc-1", "2019.1.1", "2019.2.1", "2019.3.1", "2019.3.2", "2019.4.1", "2020.1.1-beta-1", "2020.1.1-beta-2", "2020.1.1-beta-3", "2020.1.1-beta-3a" ],
              "lastUpdated" : "20191123193733"
            }
        """.trimIndent()

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

