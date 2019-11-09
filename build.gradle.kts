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

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.intellij.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper


group = "net.javaru.iip.frc"
version = "0.8.0.192"

//buildscript {
//    build.loadExtraPropertiesOf(project)
//}


val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion

val ideaVersion: String by project
val isEAP: String by project
val useSameSinceUntilBuild: String by project
val downloadIdeaSources: String by project
val publishRepoUsername: String by project
val publishRepoPassword: String by project
val publishRepoChannel: String by project


plugins {
    base
    java
    kotlin("jvm") version "1.3.41"  // It's best to kep the major.minor version consistent with the latest version of IntelliJ IDEA (and update the valid IDEA versions as appropriate)
    id("org.jetbrains.intellij") version "0.4.10" // gradle plugin-for writing IntelliJ plugins:  https://github.com/JetBrains/gradle-intellij-plugin
    // v0.4 -- 0.4.2 breaks the copyright configuration. can't find any notes about changes 
    id("org.jetbrains.gradle.plugin.idea-ext") version "0.3" // Extends the Gradle's "idea" DSL with specific settings: code style, facets, run configurations etc.
}


tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }
    withType<Test> {
        useJUnitPlatform{
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

// The Gradle plugin for writing intellij plugins
intellij {
    pluginName = "FRC"
    // IntelliJ IDEA dependency
    version = ideaVersion
    // Bundled plugin dependencies - comma separated list
    setPlugins("java", "Gradle")  // Java required to be declared as of v2019.2, but will not work with older builds. See, including the first 4 comments, https://blog.jetbrains.com/platform/2019/06/java-functionality-extracted-as-a-plugin/ 
    sandboxDirectory = project.rootDir.canonicalPath + "/.sandbox"
    updateSinceUntilBuild = false
    sameSinceUntilBuild = isEAP.toBoolean() || useSameSinceUntilBuild.toBoolean()
    downloadSources = downloadIdeaSources.toBoolean()
}


val publishPlugin: PublishTask by tasks
val runIde: RunIdeTask by tasks

runIde {
    runIde.systemProperties = mapOf(
            //"key" to "value",
            systemPropertyGetOrDefault("idea.log.config.file", resolvePath(project.rootDir.canonicalPath, ".sandbox", "log.xml")),
            systemPropertyGetOrDefault("frc.simulated.log.service.enabled", "false"),
            systemPropertyGetOrDefault("frc.simulated.log.service.use.configured.port", "false"),
            systemPropertyGetOrDefault("frc.use.wpilib.beta.site", "false"),
            systemPropertyGetOrDefault("frc.alt.wpilib.base.dir", ""),
            systemPropertyGetOrDefault("wpilib.base.dir", "")
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

// Configure some IDEA Project settings
idea {
    // https://github.com/JetBrains/gradle-idea-ext-plugin
    // Note, the DSL apparently changed in v0.4 since if I upgrade to it or later, the following breaks. But I cannot find any documentation on the change and have not ug into the code to see what needs to change
    //       Looks like the new v0.5 DSL is (now) documented here: https://github.com/JetBrains/gradle-idea-ext-plugin/wiki/DSL-spec-v.-0.5 
    project {
        (this as ExtensionAware)
        configure<ProjectSettings> {
            copyright {
                useDefault = "Apache 2 -- 2015 inception"
                profiles {
                    create("Apache 2 -- 2015 inception") {
                        keyword = "Copyright"
                        notice = """
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
        }
    }
}


repositories {
    mavenCentral()
    maven("http://dl.bintray.com/jetbrains/intellij-plugin-service")
    maven("https://plugins.gradle.org/m2/")
    jcenter()
}


dependencies {
    val junit5Version = "5.5.2"
    val http4kVersion = "3.194.0"
    
    compile(kotlin("stdlib", kotlinVersion))
    compile(kotlin("reflect", kotlinVersion))
    testImplementation(kotlin("test", kotlinVersion))
    compile("org.jdom:jdom2:2.0.6")
    compile("commons-io:commons-io:2.6")
    compile("org.apache.commons:commons-lang3:3.7")
    compile("com.jcraft:jsch:0.1.54")
    // Klaxon is a library to parse JSON in Kotlin.  https://github.com/cbeust/klaxon   Available in jcenter bintray: https://jcenter.bintray.com/com/beust/klaxon/   Help available in the #klaxon channel of the Kotlin Slack Workspace
    compile("com.beust:klaxon:5.0.9")
    // https://www.http4k.org 
    compile("org.http4k:http4k-core:$http4kVersion")
    //compile("org.http4k:http4k-client-okhttp:$http4kVersion")
    compile("org.http4k:http4k-client-apache:$http4kVersion")
    compile("org.http4k:http4k-client-apache-async:$http4kVersion")
    //compile("org.http4k:http4k-server-jetty:$http4kVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")
    compile("org.freemarker:freemarker:2.3.29")
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

fun resolvePath(base:String, vararg children: String): String
{
    var file = File(base)
    children.forEach { file = file.resolve(it) }
    return file.absolutePath.toString()
}

