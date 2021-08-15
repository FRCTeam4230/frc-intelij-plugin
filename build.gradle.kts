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

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.gradle.ext.ProjectSettings


val frcPluginBaseVersion: String by project
val ideaMajorVersion: String by project
val frcPluginEapDesignator: String by project
val frcPluginVersion = "$frcPluginBaseVersion-$ideaMajorVersion$frcPluginEapDesignator" // ex: v1.3.0-2019.2,  1.3.1-2020.1-eap.1
//val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion
val javaVersion: JavaVersion = JavaVersion.VERSION_11
val sandboxPath = determineSandboxDir()

group = "net.javaru.iip.frc"
version = frcPluginVersion 

plugins {
    base
    java
    kotlin("jvm") version "1.4.31"
    // gradle plugin-for writing IntelliJ plugins:  
    //     https://github.com/JetBrains/gradle-intellij-plugin
    //     https://lp.jetbrains.com/gradle-intellij-plugin/
    /*
    TODO: Version 1.1.4.1 is a custom version stored in my local maven repo
          v1.1.4 fails on windows due an archive extraction issue:
                  Execution failed for task ':runIde'
                  A problem occurred starting process 'command 'tar''
                  when it tries to run:
                  Starting process 'command 'tar''. Working directory: P:\dev\proj\javaru\intellij-idea-plugins\IntelliFRC\code\FRC Command: tar -xpf C:\Users\Mark\.gradle\caches\modules-2\files-2.1\com.jetbrains\jbre\jbr_jcef-11_0_11-windows-x64-b1504.13\906f1067164b4a0c3396e2ebd875d4f9cdbde853\jbre-jbr_jcef-11_0_11-windows-x64-b1504.13.tar.gz --directory C:\Users\Mark\.gradle\caches\modules-2\files-2.1\com.jetbrains\jbre\jbr_jcef-11_0_11-windows-x64-b1504.13\extracted
        It is fixed in this PR: https://github.com/JetBrains/gradle-intellij-plugin/pull/747
        which is "Use FileSystemOperations for extracting tar archives on Windows"
        It is pending to be included in a release. Once it is, we can migrate to that the new release, likely 1.1.5 or 1.2
        We set via an if condition so the CI build does not fail
        *************************************************************************************
        **  WE CAN THEN ALSO COMMENT OUT THE pluginManagement BLOCK IN settings.gradle.kts **
        *************************************************************************************
     */
    val ver = if (System.getProperty("os.name").contains("windows", true)) "1.1.4.1" else "1.1.4"
    id("org.jetbrains.intellij") version ver

    // Extends the Gradle's "idea" DSL with specific settings: code style, facets, run configurations etc.
    //    https://github.com/jetbrains/gradle-idea-ext-plugin
    //    https://plugins.gradle.org/plugin/org.jetbrains.gradle.plugin.idea-ext
    //    v0.10+ requires IDEA 2020.2+   v0.6.1+ requires IntelliJ IDEA 2019.2
    id("org.jetbrains.gradle.plugin.idea-ext") version "0.10"
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }
    withType<Test> {
        useJUnitPlatform {
            excludeTags = setOf("slow", "manual")
        }
        systemProperty("file.encoding", Charsets.UTF_8.name())
        configureEach {
            testLogging {
                events("failed")
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        all {
            kotlinOptions {
                jvmTarget = javaVersion.toString()
                javaParameters = true
                //noReflect = false
            }
        }
    }
}

tasks.test {
    // https://docs.gradle.org/current/userguide/java_testing.html#using_junit5
    useJUnitPlatform {
        excludeTags("slow")
    }
}

tasks.clean {
    doFirst {
        File("$sandboxPath/plugins/${rootProject.name}").deleteRecursively()
        // Delete indexes & cache to resolve issues of new project templates being read from cache
        File("$sandboxPath/system/caches").deleteRecursively()
        File("$sandboxPath/system/index").deleteRecursively()
    }
}

intellij {
    // The Gradle plugin for writing intellij plugins
    pluginName.set("FRC")
    // IntelliJ IDEA dependency
    version.setViaProjectProperty("ideaVersion")
    // Bundled plugin dependencies - comma separated list
    plugins.set(listOf("java", "gradle", "Groovy", "com.jetbrains.sh"))  // Java required to be declared as of v2019.2, but will not work with older builds. See, including the first 4 comments, https://blog.jetbrains.com/platform/2019/06/java-functionality-extracted-as-a-plugin/
    sandboxDir.set(sandboxPath)
    updateSinceUntilBuild.set(true)
    sameSinceUntilBuild.set(false)
    downloadSources.set(true)
}

tasks {
    patchPluginXml {
        version.set(frcPluginVersion)
        sinceBuild.setViaProjectProperty("ideaSinceBuild")
        untilBuild.setViaProjectProperty("ideaUntilBuild")
    }
    
    runIde {
        systemProperties = mapOf(
            //"key" to "value",
            //systemPropertyGetOrDefault("idea.log.config.file", resolvePath(project.rootDir.canonicalPath, ".sandbox", "log.xml")),
            systemPropertyGetOrDefault("idea.log.config.file", resolvePath(project.rootDir.canonicalPath, "idea-sandbox-log4j-config.xml")),
            systemPropertyGetOrDefault("frc.show.betas.in.new.project.wizard", "true"),
            // Turn on frc.i10n to see a notification character appended to all localized messages to aid in testing/debugging of message bundles and localization needs
            systemPropertyGetOrDefault("frc.i10n", "false"),
            systemPropertyGetOrDefault("frc.is.internal", "true"),
            systemPropertyGetOrDefault("frc.rest.use.qa", "true"),
            systemPropertyGetOrDefault("frc.experimental.gradleDslSelection", "true"),
            systemPropertyGetOrDefault("frc.experimental.kotlinTemplates", "true")
            // Legacy Ant based robot project system properties
            //systemPropertyGetOrDefault("frc.simulated.log.service.enabled", "false"),
            //systemPropertyGetOrDefault("frc.simulated.log.service.use.configured.port", "false"),
            //systemPropertyGetOrDefault("frc.use.wpilib.beta.site", "false"),
            //systemPropertyGetOrDefault("frc.alt.wpilib.base.dir", ""),
            //systemPropertyGetOrDefault("wpilib.base.dir", "")
                                )
    }
    
    runPluginVerifier {
        // Reports appear in ${project.buildDir}/reports/pluginVerifier by default. Set `verificationReportsDirectory` to change
        val baseDir = File(projectProperty("verifierLocalIdesBaseDir").replace("~", System.getProperty("user.home")))
        localPaths.set(projectPropertyList("verifierLocalIdes").map { File(baseDir, it) })
        ideVersions.set(projectPropertyList("verifierIdeVersions")) 
    }

    publishPlugin {
        // See https://plugins.jetbrains.com/docs/intellij/deployment.html  and  https://github.com/JetBrains/intellij-platform-plugin-template/blob/main/build.gradle.kts
        dependsOn("patchChangelog")
        // For now, we will not use the publish task unless this project property is set. Once we have tested things, we can remove this guard
        if (projectPropertyBoolean("autoPublish", defaultValue = false))
        {
            project.version = "${project.version}" //-${properties["buildNumber"]}"

            token.set(System.getenv("JETBRAINS_MARKETPLACE_PUBLISH_TOKEN"))
            // 
            // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
            // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
            // example of setting programmatically, which assumes pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
            //   channels.set(listOf(projectProperty("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
            channels.set(listOf("default" ))
        }
    }
}

idea {
    // Configure some IDEA Project settings (i.e. for Intellij IDEA used to code the plugin)
    // https://github.com/JetBrains/gradle-idea-ext-plugin
    // Note: The DSL apparently changed in v0.4 since if I upgrade to it or later, the following breaks.
    //       But I have not had the time to dig into it and see what needs to change
    //       The DSL spec is documented on the project's wiki, but it is no the most stellar documentation,
    //       and is only for the Groovy based DSL. When I find some time I can look at modifying.
    //       https://github.com/JetBrains/gradle-idea-ext-plugin/wiki
    //       This issue has some links to help using with the Kotlin Gradle DSL:  https://github.com/JetBrains/gradle-idea-ext-plugin/issues/44
    project {
        (this as ExtensionAware)
        configure<ProjectSettings> {
            this as ExtensionAware

            configure<org.jetbrains.gradle.ext.CopyrightConfiguration> {
                useDefault = "Apache 2 -- 2015 inception"
                profiles {
                    create("Apache 2 -- 2015 inception") {
                        this.keyword = "Copyright"
                        this.notice = """
                                    #set( ${'$'}inceptionYear = 2015 )
                                    Copyright ${'$'}inceptionYear#if(${'$'}today.year!=${'$'}inceptionYear)-${'$'}today.year#end the original author or authors.
                                    
                                        Licensed under the Apache License, Version 2.0 (the "License");
                                        you may not use this file except in compliance with the License.
                                        You may obtain a copy of the License at
                                    
                                          https://www.apache.org/licenses/LICENSE-2.0
                                        
                                        Unless required by applicable law or agreed to in writing, software
                                        distributed under the License is distributed on an "AS IS" BASIS,
                                        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                                        See the License for the specific language governing permissions and
                                        limitations under the License.
                                """.trimIndent()
                    }
                }
            }

            configure< org.jetbrains.gradle.ext.EncodingConfiguration> {
                // setting EncodingConfiguration requires IDEA 2019.1+
                this.encoding = "UTF-8"
                this.bomPolicy = org.jetbrains.gradle.ext.EncodingConfiguration.BomPolicy.WITH_NO_BOM
                properties {
                    encoding = "ISO-8859-1"
                    transparentNativeToAsciiConversion = false
                }
                // Haven't gotten this part working, if it is ever needed
                //mapping = mapOf("../src/main/resources/Foo" to "ISO-8859-1")
            }


            // EXAMPLE OF CREATING A RUN CONFIGURATION from: https://github.com/JetBrains/gradle-idea-ext-plugin/issues/44#issuecomment-471340778
            // For available RunConfigurations, see https://github.com/JetBrains/gradle-idea-ext-plugin/blob/master/src/main/groovy/org/jetbrains/gradle/ext/RunConfigurations.groovy
            //     All potential configs "extends BaseRunConfiguration"
            //     They are as of v0.7:  Application, TestNG, JUnit, Remote, Gradle

//            configure<NamedDomainObjectContainer<org.jetbrains.gradle.ext.RunConfiguration>> {
//                this as PolymorphicDomainObjectContainer<org.jetbrains.gradle.ext.RunConfiguration>
//                create("MyApplication", org.jetbrains.gradle.ext.Application::class) {
//                    this.mainClass = "com.example.MyApplication"
//                    this.workingDirectory = "./core/assets/"
//                    this.moduleName = "FRC.main"
//                }
//            }

        }
    }
}


repositories {
    mavenLocal()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}


dependencies {
    val jacksonVersion = "2.11.2"

    // For Kotlin dependencies, you can use shorthand for a dependency on a Kotlin module, for example, kotlin("test") for "org.jetbrains.kotlin:kotlin-test".
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))

    implementation("org.jdom:jdom2:2.0.6")
    implementation("commons-io:commons-io:2.7")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("com.jcraft:jsch:0.1.54")
    // Klaxon is a library to parse JSON in Kotlin.  https://github.com/cbeust/klaxon   Available in jcenter bintray: https://jcenter.bintray.com/com/beust/klaxon/   Help available in the #klaxon channel of the Kotlin Slack Workspace
    //implementation("com.beust:klaxon:5.0.9")
    implementation(platform("com.google.guava:guava-bom:29.0-jre"))
    implementation("com.google.guava:guava")


    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatypes-collections:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-guava:$jacksonVersion")
    implementation("org.freemarker:freemarker:2.3.30")

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testImplementation("com.google.guava:guava-testlib") // version in BOM above
}


// --- Utility functions -----------------------------------------------
inline fun <reified T : Task> task(noinline configuration: T.() -> Unit) = tasks.creating(T::class, configuration)

// allows for the standard task syntax after declaring something like:  val publishPlugin: PublishTask by tasks
inline operator fun <T : Task> T.invoke(a: T.() -> Unit): T = apply(a)

fun systemPropertyGetOrDefault(key: String, default: String) = Pair<String, String>(key, System.getProperty(key, default))

/** Retrieves a project property as a String. */
fun projectProperty(key: String) = project.findProperty(key).toString()
/** Retrieves a project property as a String, using the default value if the property is not defined. */
fun projectProperty(key: String, defaultValue: String) = project.findProperty(key)?.toString() ?: defaultValue
/** Retrieves a project property as a Boolean. */
fun projectPropertyBoolean(key: String) = projectProperty(key).toBoolean()
/** Retrieves a project property as a Boolean, using the default value if the property is not defined. */
fun projectPropertyBoolean(key: String, defaultValue: Boolean) = projectProperty(key, defaultValue.toString()).toBoolean()
/** Retrieves a project property that is a delimited String and returns it as a List of String, or an empty List if the property is not set. */
fun projectPropertyList(key: String, vararg delimiters: Char = charArrayOf(',')): List<String> = project.findProperty(key)?.toString()?.split(*delimiters)?.map { it.trim() } ?: emptyList()

/** Sets a Gradle Property value via a Project Property (usually set in gradle.properties). */
fun Property<String>.setViaProjectProperty(key: String) = this.set(projectProperty(key))
/** Sets a Gradle Property value via a Project Property (usually set in gradle.properties). */
fun Property<String>.setViaProjectProperty(key: String, defaultValue: String) = this.set(projectProperty(key, defaultValue))
/** Sets a Gradle Property value via a Project Property (usually set in gradle.properties). */
fun Property<Boolean>.setBooleanViaProjectProperty(key: String) = this.set(projectPropertyBoolean(key))
/** Sets a Gradle Property value via a Project Property (usually set in gradle.properties), using the default value if the property is not defined. */
fun Property<Boolean>.setBooleanViaProjectProperty(key: String, defaultValue: Boolean) = this.set(projectPropertyBoolean(key, defaultValue))


fun resolvePath(base: String, vararg children: String): String
{
    var file = File(base)
    children.forEach { file = file.resolve(it) }
    return file.absolutePath.toString()
}

fun determineSandboxDir(): String
{
    val ideaVersion = project.properties["ideaVersion"]?.toString() ?: "UNKNOWN"
    val sandboxSuffix =
        when
        {
            ideaVersion.endsWith("LATEST-EAP-SNAPSHOT", ignoreCase = true)                                        ->
                "LATEST-EAP-SNAPSHOT"
            ideaVersion.contains("""([\d]{3})-EAP-SNAPSHOT""".toRegex(RegexOption.IGNORE_CASE))                         ->
                """[\d]{3}""".toRegex().find(ideaVersion)!!.value
            ideaVersion.contains("""([A-Z]{2}-)?(2[\d]{3})\.[\d](\.[\d]{1,2})?""".toRegex(RegexOption.IGNORE_CASE))     ->
                """(2[\d]{3})\.[\d]""".toRegex().find(ideaVersion)!!.value.substring(2, 6).replace(".", "")
            ideaVersion.contains("""([A-Z]{2}-)?([\d]{3})\.[\d]{1,5}(\.[\d]{1,4})?""".toRegex(RegexOption.IGNORE_CASE)) ->
                """[\d]{3}""".toRegex().find(ideaVersion)!!.value
            else                                                                                                        ->
                ideaVersion
        }

    return "${project.rootDir.canonicalPath}/.sandboxes/.sandbox-$sandboxSuffix"
}

