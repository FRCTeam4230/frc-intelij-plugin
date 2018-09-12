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

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.internal.impldep.org.apache.maven.wagon.PathUtils.password
import org.gradle.api.tasks.wrapper.Wrapper
import org.jetbrains.gradle.ext.ProjectSettings

import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.intellij.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import java.time.LocalDate


group = "net.javaru.iip.frc"
version = "0.8-SNAPSHOT"

//buildscript {
//    build.loadExtraPropertiesOf(project)
//}

// TODO: Per gradle run warning, this syntax is deprecated and will be removed in Gradle 5.0.
//       Creating a custom task named 'wrapper' has been deprecated. This is scheduled to be removed in Gradle 5.0. You can configure the existing task using the 'wrapper { }' syntax or create your custom task under a different name.    
task<Wrapper>("wrapper") {
    // After modifying the anything, run from cmd line:   gradle wrapper
    //    Alternatively you can just run:   gradle wrapper --gradle-version 4.10 --distribution-type ALL
    gradleVersion = "4.10"
    distributionType = Wrapper.DistributionType.ALL

}


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
    kotlin("jvm") version "1.2.41"
    id("org.jetbrains.intellij") version "0.3.9" // gradle plugin-for writing IntelliJ plugins
    // v0.4 -- 0.4.2 breaks the copyright configuration. can;t find any notes about changes 
    id("org.jetbrains.gradle.plugin.idea-ext") version "0.3" // Extends the Gradle's "idea" DSL with specific settings: code style, facets, run configurations etc.
}


tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }
    withType<Test> {
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
    //setPlugins("ant")
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
            lookupSystemPropertyPair("frc.simulated.log.service.enabled", "false"),
            lookupSystemPropertyPair("frc.simulated.log.service.use.configured.port", "false"),
            lookupSystemPropertyPair("frc.use.wpilib.beta.site", "false"),
            lookupSystemPropertyPair("frc.alt.wpilib.base.dir", ""),
            lookupSystemPropertyPair("wpilib.base.dir", "")
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
    project {
        (this as ExtensionAware)
        configure<ProjectSettings> {
            copyright {
                useDefault = "Apache 2 -- 2015 inception"
                profiles {
                    create("Apache 2 -- 2015 inception") {
                        keyword = "Copyright"
                        allowReplaceRegexp = "Mark Ve|TRGR"
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
    val junit5Version = "5.0.2"

    compile(kotlin("stdlib", kotlinVersion))
    compile(kotlin("reflect", kotlinVersion))
    compile(kotlin("runtime", kotlinVersion))
    testCompile(kotlin("test", kotlinVersion))
    compile("org.jdom:jdom2:2.0.6")
    compile("commons-io:commons-io:2.6")
    compile("org.apache.commons:commons-lang3:3.7")
    compile("com.jcraft:jsch:0.1.54")
    testCompile("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junit5Version")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
    testCompile("junit:junit:4.12")
    testRuntime("org.junit.vintage:junit-vintage-engine:4.12.2")
}


// --- Utility functions -----------------------------------------------
inline fun <reified T : Task> task(noinline configuration: T.() -> Unit) = tasks.creating(T::class, configuration)

// allows for the standard task syntax after declaring something like:  val publishPlugin: PublishTask by tasks
inline operator fun <T : Task> T.invoke(a: T.() -> Unit): T = apply(a)

fun lookupSystemPropertyPair(key: String, default: String) = Pair<String, String>(key, System.getProperty(key, default))
