<#ftl output_format="plainText" encoding="UTF-8"> <#-- Output formats: https://freemarker.apache.org/docs/dgui_misc_autoescaping.html -->
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--
    IMPORTANT: This tempolate uses alternate square bracket interpolation syntax
               For example:
                    [=data.robotClassSimpleName]
               rather than:
                    ${data.robotClassSimpleName}
               so as to not clash with Kotlin string templates syntax
               The option to use that can't be set in the template, but has to be set as an
               option on the Configuration object in the code. Note that this only affects interpolation
               syntax, and *NOT* Tag syntax. So we will still use `<#if isSuchAndSuch>` and not `[#if isSuchAndSuch]`.
               Tag syntax can be changed if desired, but we are not.
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
import edu.wpi.first.deployutils.deploy.artifact.FileTreeArtifact
import edu.wpi.first.gradlerio.deploy.roborio.FRCJavaArtifact
import edu.wpi.first.gradlerio.deploy.roborio.RoboRIO
import edu.wpi.first.toolchain.NativePlatforms
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
<#if data.getIncludeKotlinSupport()>
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
</#if>
import java.net.URI

plugins {
    java
<#if data.getIncludeKotlinSupport()>
    kotlin("jvm") version "[=data.kotlinVersion]"
</#if>
    id("edu.wpi.first.GradleRIO") version "[=data.wpilibVersion.versionString]"
    idea
}


val javaVersion: JavaVersion = JavaVersion.VERSION_17
val javaLanguageVersion: JavaLanguageVersion by extra { JavaLanguageVersion.of(javaVersion.toString()) }
val jvmVendor: JvmVendorSpec by extra { JvmVendorSpec.ADOPTIUM }
<#if data.getIncludeKotlinSupport()>
val kotlinJvmTarget = JvmTarget.fromTarget(javaVersion.toString())
</#if>

@Suppress("PropertyName")
val ROBOT_MAIN_CLASS = "[=data.mainClassFQ]"

<#if data.isRoboRioRobotTemplate()>
// Define my targets (RoboRIO) and artifacts (deployable files)
// This is added by GradleRIO's backing project DeployUtils.
deploy {
    targets {
        create("roborio", RoboRIO::class.java) {
            // Team number is loaded either from the .wpilib/wpilib_preferences.json
            // or from command line. If not found an exception will be thrown.
            // You can use project.frc.getTeamOrDefault(####) instead of project.frc.teamNumber
            // if you want to store a team number in this file.
            team = project.frc.teamNumber
            debug = project.frc.getDebugOrDefault(false)
            artifacts.create("frcJava", FRCJavaArtifact::class.java) {

            }
            artifacts.create("frcStaticFileDeploy", FileTreeArtifact::class.java) {
                files = project.fileTree("src/main/deploy")
                directory = "/home/lvuser/deploy"
                // Change to true to delete files on roboRIO that no longer exist in deploy directory of this project
                deleteOldFiles = false
            }
        }
    }
}
val deployArtifact = deploy.targets.getByName("roborio").artifacts.getByName("frcJava") as FRCJavaArtifact

// Set to true to use debug for JNI.
wpi.java.debugJni = false
</#if>

// Set this to true to enable desktop support.
val includeDesktopSupport = [=data.getIncludeDesktopSupportGradleSetting()]

repositories {
    // Set repositories to use. In Gradle DSL builds, the GradleRIO plugin automatically configures these repos.
    // But with a Kotlin DSL build file, they are not getting automatically configured.
    // If anyone can determine a way to apply programmatically, please open a ticket at
    // https://gitlab.com/Javaru/frc-intellij-idea-plugin/-/issues and I will update the template.
    maven {
        name = "WPILocal"
        url = wpi.frcHome.map { it.dir("maven") }.get().asFile.toURI()
    }
    maven {
        name = "WPIOfficialRelease"
        url = URI("https://frcmaven.wpi.edu/artifactory/release")
    }
    maven {
        name = "WPIFRCMavenVendorCacheRelease"
        url = URI("https://frcmaven.wpi.edu/artifactory/vendor-mvn-release")
    }
    wpi.vendor.vendorRepos.forEach {
        maven {
            name = it.name
            url = URI(it.url)
        }
    }
    mavenCentral()
}

dependencies {
    annotationProcessor(wpi.java.deps.wpilibAnnotations())
    implementation(wpi.java.deps.wpilib())
    implementation(wpi.java.vendor.java())
<#if data.isRoboRioRobotTemplate()>

    roborioDebug(wpi.java.deps.wpilibJniDebug(NativePlatforms.roborio))
    roborioDebug(wpi.java.vendor.jniDebug(NativePlatforms.roborio))

    roborioRelease(wpi.java.deps.wpilibJniRelease(NativePlatforms.roborio))
    roborioRelease(wpi.java.vendor.jniRelease(NativePlatforms.roborio))
</#if>

    nativeDebug(wpi.java.deps.wpilibJniDebug(NativePlatforms.desktop))
    nativeDebug(wpi.java.vendor.jniDebug(NativePlatforms.desktop))
    simulationDebug(wpi.sim.enableDebug())

    nativeRelease(wpi.java.deps.wpilibJniRelease(NativePlatforms.desktop))
    nativeRelease(wpi.java.vendor.jniRelease(NativePlatforms.desktop))
    simulationRelease(wpi.sim.enableRelease())
<#if data.junitUseJUnitPlatform()>

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
</#if>
}

java {
    toolchain {
        languageVersion.set(javaLanguageVersion)
        vendor.set(jvmVendor)
    }
}

tasks.compileJava {
    options.encoding = Charsets.UTF_8.name()
    // Configure string concat to always inline compile
    options.compilerArgs.add("-XDstringConcat=inline")
}
<#if data.getIncludeKotlinSupport()>
    
kotlin {
    compilerOptions {
        jvmTarget.set(kotlinJvmTarget)
    }

    jvmToolchain {
        // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
        languageVersion.set(javaLanguageVersion)
        vendor.set(jvmVendor)
    }
}
</#if>    
<#if data.junitUseJUnitPlatform()>

tasks.test {
    useJUnitPlatform()
    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
}
</#if>

// Simulation configuration (e.g. environment variables).
wpi.sim.addGui().defaultEnabled = true
wpi.sim.addDriverstation()

<#if data.isRomiTemplate()>
//Sets the websocket client remote host.
//wpi.sim.envVar("HALSIMWS_HOST", "10.0.0.2") wpi.sim.enVar is not public. While groovy allows setting it, Kotlin does not. Until this is resolved, we work around as follows:
wpi.sim.environment["HALSIMWS_HOST"] = "10.0.0.2"
wpi.sim.addWebsocketsServer().defaultEnabled = true
wpi.sim.addWebsocketsClient().defaultEnabled = true

</#if>
<#if data.isXrpTemplate()>
//Sets the XRP Client Host
//wpi.sim.envVar("HALSIMXRP_HOST", "192.168.42.1") wpi.sim.enVar is not public. While groovy allows setting it, Kotlin does not. Until this is resolved, we work around as follows:
wpi.sim.environment["HALSIMXRP_HOST"] = "192.168.42.1"
wpi.sim.addXRPClient().defaultEnabled = true

</#if>

// Setting up my Jar File. In this case, adding all libraries into the main jar ('fat jar')
// in order to make them all available at runtime. Also adding the manifest so WPILib
// knows where to look for our Robot Class.
tasks.jar {
    from(project.configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    <#if data.isRoboRioRobotTemplate()>
    from(sourceSets.main.get().allSource)
    </#if>
    manifest(edu.wpi.first.gradlerio.GradleRIOPlugin.javaManifest(ROBOT_MAIN_CLASS))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

<#if data.isRoboRioRobotTemplate()>
// Configure jar and deploy tasks
deployArtifact.setJarTask(tasks.jar.get())
</#if>
wpi.java.configureExecutableTasks(tasks.jar.get())
wpi.java.configureTestTasks(tasks.test.get())


idea {
    project {
        // The project.sourceCompatibility setting is not always picked up, so we set explicitly
        languageLevel = IdeaLanguageLevel(javaVersion)
    }
    module {
        // Improve development & (especially) debugging experience (and IDEA's capabilities) by having libraries' source & javadoc attached
        isDownloadJavadoc = true
        isDownloadSources = true
        // Exclude the .vscode directory from indexing and search
        excludeDirs.add(file(".run" ))
        excludeDirs.add(file(".vscode" ))
    }
}

// Helper Functions to keep syntax cleaner
// @formatter:off
fun DependencyHandler.addDependencies(configurationName: String, dependencies: List<Provider<String>>) = dependencies.forEach { add(configurationName, it) }
fun DependencyHandler.roborioDebug(dependencies: List<Provider<String>>) = addDependencies("roborioDebug", dependencies)
fun DependencyHandler.roborioRelease(dependencies: List<Provider<String>>) = addDependencies("roborioRelease", dependencies)
fun DependencyHandler.nativeDebug(dependencies: List<Provider<String>>) = addDependencies("nativeDebug", dependencies)
fun DependencyHandler.simulationDebug(dependencies: List<Provider<String>>) = addDependencies("simulationDebug", dependencies)
fun DependencyHandler.nativeRelease(dependencies: List<Provider<String>>) = addDependencies("nativeRelease", dependencies)
fun DependencyHandler.simulationRelease(dependencies: List<Provider<String>>) = addDependencies("simulationRelease", dependencies)
fun DependencyHandler.implementation(dependencies: List<Provider<String>>) = dependencies.forEach{ implementation(it) }
fun DependencyHandler.annotationProcessor(dependencies: List<Provider<String>>) = dependencies.forEach{ annotationProcessor(it) }
 // @formatter:on