/*
 * Copyright 2015-2020 the original author or authors
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

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.intellij.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper


val frcPluginBaseVersion: String by project
val ideaMajorVersion: String by project
val frcPluginEapDesignator: String by project

group = "net.javaru.iip.frc"
// ex: v1.3.0-2019.2,  1.3.1-2020.1-eap.1
version = "$frcPluginBaseVersion-$ideaMajorVersion$frcPluginEapDesignator"

//buildscript {
//    build.loadExtraPropertiesOf(project)
//}


val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion

val ideaVersion: String by project
val isEAP: String by project
val ideaSameSinceUntilBuild: String by project
val ideaDownloadSources: String by project
val publishRepoUsername: String by project
val publishRepoPassword: String by project
val publishRepoChannel: String by project
val ideaUpdateSinceUntilBuild: String by project
val ideaSinceBuild: String by project
val ideaUntilBuild: String by project


plugins {
    base
    java
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.intellij") version "0.4.21" // gradle plugin-for writing IntelliJ plugins:  https://github.com/JetBrains/gradle-intellij-plugin

    // Extends the Gradle's "idea" DSL with specific settings: code style, facets, run configurations etc.
    //    https://github.com/jetbrains/gradle-idea-ext-plugin
    //    https://plugins.gradle.org/plugin/org.jetbrains.gradle.plugin.idea-ext
    //    v0.6.1+ requires IntelliJ IDEA 2019.2
    id("org.jetbrains.gradle.plugin.idea-ext") version "0.7"
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
                jvmTarget = JavaVersion.VERSION_1_8.toString()
                javaParameters = true
                //noReflect = false
            }
        }
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.named<Test>("test") {
    // https://docs.gradle.org/current/userguide/java_testing.html#java_testing
    useJUnitPlatform {
        excludeTags("slow")
    }
}


val patchPluginXml: PatchPluginXmlTask by tasks
val publishPlugin: PublishTask by tasks
val runIde: RunIdeTask by tasks

// The Gradle plugin for writing intellij plugins
intellij {
    pluginName = "FRC"
    // IntelliJ IDEA dependency
    version = ideaVersion
    // Bundled plugin dependencies - comma separated list
    setPlugins("java", "gradle"/*, "Groovy"*/)  // Java required to be declared as of v2019.2, but will not work with older builds. See, including the first 4 comments, https://blog.jetbrains.com/platform/2019/06/java-functionality-extracted-as-a-plugin/ 
    sandboxDirectory = project.rootDir.canonicalPath + "/.sandbox"

    updateSinceUntilBuild = ideaUpdateSinceUntilBuild.toBoolean()
    sameSinceUntilBuild = isEAP.toBoolean() || ideaSameSinceUntilBuild.toBoolean()
    downloadSources = ideaDownloadSources.toBoolean()

    patchPluginXml {
        version(version)
        sinceBuild(ideaSinceBuild)
        untilBuild(ideaUntilBuild)
    }

    runIde {
        runIde.systemProperties = mapOf(
                //"key" to "value",
                //systemPropertyGetOrDefault("idea.log.config.file", resolvePath(project.rootDir.canonicalPath, ".sandbox", "log.xml")),
                systemPropertyGetOrDefault("idea.log.config.file", resolvePath(project.rootDir.canonicalPath, "idea-sandbox-log4j-config.xml")),
                systemPropertyGetOrDefault("frc.show.betas.in.new.project.wizard", "true"),
                systemPropertyGetOrDefault("frc.is.internal", "true")
                // Legacy Ant based robot project system properties
                //systemPropertyGetOrDefault("frc.simulated.log.service.enabled", "false"),
                //systemPropertyGetOrDefault("frc.simulated.log.service.use.configured.port", "false"),
                //systemPropertyGetOrDefault("frc.use.wpilib.beta.site", "false"),
                //systemPropertyGetOrDefault("frc.alt.wpilib.base.dir", ""),
                //systemPropertyGetOrDefault("wpilib.base.dir", "")
                                       )
    }

    publishPlugin {
        // See http://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html
        // See https://github.com/minecraft-dev/MinecraftDev/blob/dev/build.gradle.kts
        if (properties["publish"] != null)
        {
            project.version = "${project.version}" //-${properties["buildNumber"]}"

            username(publishRepoUsername)
            password(publishRepoPassword)
            channels(publishRepoChannel)
        }
    }
}


// Configure some IDEA Project settings (i.e. for Intellij IDEA used to code the plugin)
idea {
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
                                    Copyright ${'$'}inceptionYear#if(${'$'}today.year!=${'$'}inceptionYear)-${'$'}today.year#end the original author or authors
                                    
                                        Licensed under the Apache License, Version 2.0 (the "License");
                                        you may not use this file except in compliance with the License.
                                        You may obtain a copy of the License at
                                    
                                          http://www.apache.org/licenses/LICENSE-2.0
                                        
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
    mavenCentral()
    maven("https://dl.bintray.com/jetbrains/intellij-plugin-service")
    maven("https://plugins.gradle.org/m2/")
    jcenter()
}


dependencies {
    val junit5Version = "5.6.2"
    val http4kVersion = "3.248.0"
    val jacksonVersion = "2.10.1"

    compile(kotlin("stdlib", kotlinVersion))
    compile(kotlin("reflect", kotlinVersion))
    testImplementation(kotlin("test", kotlinVersion))
    compile("org.jdom:jdom2:2.0.6")
    compile("commons-io:commons-io:2.7")
    compile("org.apache.commons:commons-lang3:3.10")
    compile("com.jcraft:jsch:0.1.54")
    // Klaxon is a library to parse JSON in Kotlin.  https://github.com/cbeust/klaxon   Available in jcenter bintray: https://jcenter.bintray.com/com/beust/klaxon/   Help available in the #klaxon channel of the Kotlin Slack Workspace
    compile("com.beust:klaxon:5.0.9")
    // https://www.http4k.org 
    compile("org.http4k:http4k-core:$http4kVersion")
    //compile("org.http4k:http4k-client-okhttp:$http4kVersion")
    compile("org.http4k:http4k-client-apache:$http4kVersion")
    compile("org.http4k:http4k-client-apache-async:$http4kVersion")
    //compile("org.http4k:http4k-server-jetty:$http4kVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatypes-collections:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-guava:$jacksonVersion")
    implementation("org.freemarker:freemarker:2.3.30")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
    testImplementation("org.junit.vintage:junit-vintage-engine:$junit5Version")
}


// --- Utility functions -----------------------------------------------
inline fun <reified T : Task> task(noinline configuration: T.() -> Unit) = tasks.creating(T::class, configuration)

// allows for the standard task syntax after declaring something like:  val publishPlugin: PublishTask by tasks
inline operator fun <T : Task> T.invoke(a: T.() -> Unit): T = apply(a)

fun systemPropertyGetOrDefault(key: String, default: String) = Pair<String, String>(key, System.getProperty(key, default))

fun resolvePath(base: String, vararg children: String): String
{
    var file = File(base)
    children.forEach { file = file.resolve(it) }
    return file.absolutePath.toString()
}

