<#ftl output_format="plainText" encoding="UTF-8"> <#-- Output formats: https://freemarker.apache.org/docs/dgui_misc_autoescaping.html -->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
<#if data.isRoboRioRobotTemplate()>
import edu.wpi.first.gradlerio.deploy.roborio.RoboRIO

</#if>
plugins {
    id "java"
<#if data.getIncludeKotlinSupport()>
    id "org.jetbrains.kotlin.jvm" version "${data.kotlinVersion}"
</#if>
    id "edu.wpi.first.GradleRIO" version "${data.wpilibVersion.versionString}"
}

def javaVersion = JavaVersion.VERSION_11
sourceCompatibility = javaVersion
targetCompatibility = javaVersion

def ROBOT_MAIN_CLASS = "${data.mainClassFQ}"

<#if data.isRoboRioRobotTemplate()>
// Define my targets (RoboRIO) and artifacts (deployable files)
// This is added by GradleRIO's backing project DeployUtils.
deploy {
    targets {
        roborio(getTargetTypeClass('RoboRIO')) {
            // Team number is loaded either from the .wpilib/wpilib_preferences.json
            // or from command line. If not found an exception will be thrown.
            // You can use getTeamOrDefault(team) instead of getTeamNumber if you
            // want to store a team number in this file.
            team = project.frc.getTeamNumber()
            debug = project.frc.getDebugOrDefault(false)

            artifacts {
                // First part is artifact name, 2nd is artifact type
                // getTargetTypeClass is a shortcut to get the class type using a string

                frcJava(getArtifactTypeClass('FRCJavaArtifact')) {
                }

                // Static files artifact
                frcStaticFileDeploy(getArtifactTypeClass('FileTreeArtifact')) {
                    files = project.fileTree('src/main/deploy')
                    directory = '/home/lvuser/deploy'
                }
            }
        }
    }
}

def deployArtifact = deploy.targets.roborio.artifacts.frcJava

// Set to true to use debug for JNI.
wpi.java.debugJni = false
</#if>

// Set this to true to enable desktop support.
def includeDesktopSupport = ${data.getIncludeDesktopSupportGradleSetting()}

<#if data.getIncludeKotlinSupport()>

compileKotlin {
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}
</#if>
<#if data.junitUseJUnitPlatform()>

test {
    useJUnitPlatform()
}
</#if>

dependencies {
<#if data.isRomiTemplate()>
    implementation wpi.java.deps.wpilib()
    implementation wpi.java.vendor.java()

    nativeDebug wpi.java.deps.wpilibJniDebug(wpi.platforms.desktop)
    nativeDebug wpi.java.vendor.jniDebug(wpi.platforms.desktop)
    simulationDebug wpi.sim.enableDebug()

    nativeRelease wpi.java.deps.wpilibJniRelease(wpi.platforms.desktop)
    nativeRelease wpi.java.vendor.jniRelease(wpi.platforms.desktop)
    simulationRelease wpi.sim.enableRelease()
</#if>
<#if data.isRoboRioRobotTemplate()>
    implementation wpi.java.deps.wpilib()
    implementation wpi.java.vendor.java()

    roborioDebug wpi.java.deps.wpilibJniDebug(wpi.platforms.roborio)
    roborioDebug wpi.java.vendor.jniDebug(wpi.platforms.roborio)

    roborioRelease wpi.java.deps.wpilibJniRelease(wpi.platforms.roborio)
    roborioRelease wpi.java.vendor.jniRelease(wpi.platforms.roborio)

    nativeDebug wpi.java.deps.wpilibJniDebug(wpi.platforms.desktop)
    nativeDebug wpi.java.vendor.jniDebug(wpi.platforms.desktop)
    simulationDebug wpi.sim.enableDebug()

    nativeRelease wpi.java.deps.wpilibJniRelease(wpi.platforms.desktop)
    nativeRelease wpi.java.vendor.jniRelease(wpi.platforms.desktop)
    simulationRelease wpi.sim.enableRelease()
</#if>


<#if data.junitIsJUnit4Only()>
    testImplementation "junit:junit:${data.junit4Version}"
</#if>
<#if data.junitUseJUnitPlatform()>
    implementation platform('org.junit:junit-bom:${data.junit5Version}')
    testImplementation "org.junit.jupiter:junit-jupiter-api"
    testImplementation "org.junit.jupiter:junit-jupiter-params"
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine"
</#if>
<#if data.junitIncludeVintageSupport()>
    testImplementation "junit:junit:${data.junit4Version}"
    testRuntimeOnly "org.junit.vintage:junit-vintage-engine"
</#if>
}

// Simulation configuration (e.g. environment variables).
wpi.sim.addGui().defaultEnabled = true
wpi.sim.addDriverstation()

<#if data.isRomiTemplate()>
//Sets the websocket client remote host.
wpi.sim.envVar("HALSIMWS_HOST", "10.0.0.2")
wpi.sim.addWebsocketsServer().defaultEnabled = true
wpi.sim.addWebsocketsClient().defaultEnabled = true

</#if>
// Setting up my Jar File. In this case, adding all libraries into the main jar ('fat jar')
// in order to make them all available at runtime. Also adding the manifest so WPILib
// knows where to look for our Robot Class.
jar {
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    manifest edu.wpi.first.gradlerio.GradleRIOPlugin.javaManifest(ROBOT_MAIN_CLASS)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

<#if data.isRoboRioRobotTemplate()>
// Configure jar and deploy tasks
deployArtifact.jarTask = jar
</#if>
wpi.java.configureExecutableTasks(jar)
wpi.java.configureTestTasks(test)