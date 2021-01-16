<#ftl output_format="plainText" encoding="UTF-8"> <#-- Output formats: https://freemarker.apache.org/docs/dgui_misc_autoescaping.html -->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching the testing instance of IntelliJ IDEA -->
</#compress>
plugins {
    id "java"
    id "edu.wpi.first.GradleRIO" version "${data.wpilibVersion.versionString}"
}

sourceCompatibility = JavaVersion.VERSION_11
targetCompatibility = JavaVersion.VERSION_11

def ROBOT_MAIN_CLASS = "${data.mainClassFQ}"

<#if !data.isRomiRobot()>
// Define my targets (RoboRIO) and artifacts (deployable files)
// This is added by GradleRIO's backing project EmbeddedTools.
deploy {
    targets {
        roboRIO("roborio") {
            // Team number is loaded either from the .wpilib/wpilib_preferences.json
            // or from command line. If not found an exception will be thrown.
            // You can use getTeamOrDefault(team) instead of getTeamNumber if you
            // want to store a team number in this file.
            team = frc.getTeamNumber()
        }
    }
    artifacts {
        frcJavaArtifact('frcJava') {
            targets << "roborio"
            // Debug can be overridden by command line, for use with VSCode
            debug = frc.getDebugOrDefault(false)
        }
        // Built in artifact to deploy arbitrary files to the roboRIO.
        fileTreeArtifact('frcStaticFileDeploy') {
            // The directory below is the local directory to deploy
            files = fileTree(dir: 'src/main/deploy')
            // Deploy to RoboRIO target, into /home/lvuser/deploy
            targets << "roborio"
            directory = '/home/lvuser/deploy'
        }
    }
}
</#if>

// Set this to true to enable desktop support.
def includeDesktopSupport = ${data.getIncludeDesktopSupportGradleSetting()}

<#if data.junitUseJUnitPlatform()>

test {
    useJUnitPlatform()
}
</#if>

dependencies {
    implementation wpi.deps.wpilib()
<#if !data.isRomiRobot()>
    nativeZip wpi.deps.wpilibJni(wpi.platforms.roborio)
</#if>
    nativeDesktopZip wpi.deps.wpilibJni(wpi.platforms.desktop)


    implementation wpi.deps.vendor.java()
<#if !data.isRomiRobot()>
    nativeZip wpi.deps.vendor.jni(wpi.platforms.roborio)
</#if>
    nativeDesktopZip wpi.deps.vendor.jni(wpi.platforms.desktop)

<#if data.junitIsJUnit4Only()>
    testImplementation "junit:junit:4.13.1"
</#if>
<#if data.junitUseJUnitPlatform()>
    implementation platform('org.junit:junit-bom:${data.junit5Version}')
    testImplementation "org.junit.jupiter:junit-jupiter-api"
    testImplementation "org.junit.jupiter:junit-jupiter-params"
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine"
</#if>
<#if data.junitIncludeVintageSupport()>
    testImplementation "junit:junit:4.13.1"
    testRuntimeOnly "org.junit.vintage:junit-vintage-engine"
</#if>

    // Enable simulation gui support. Must check the box in vscode to enable support
    // upon debugging
    simulation wpi.deps.sim.gui(wpi.platforms.desktop, false)
    simulation wpi.deps.sim.driverstation(wpi.platforms.desktop, false)

    // Websocket extensions require additional configuration.
    // simulation wpi.deps.sim.ws_server(wpi.platforms.desktop, false)
    <#if !data.isRomiRobot()>// </#if>simulation wpi.deps.sim.ws_client(wpi.platforms.desktop, false)
}

<#if data.isRomiRobot()>
// Set the websocket remote host (the Romi IP address).
sim {
    envVar "HALSIMWS_HOST", "10.0.0.2"
}
<#else>
// Simulation configuration (e.g. environment variables).
sim {
    // Sets the websocket client remote host.
    // envVar "HALSIMWS_HOST", "10.0.0.2"
}
</#if>

// Setting up my Jar File. In this case, adding all libraries into the main jar ('fat jar')
// in order to make them all available at runtime. Also adding the manifest so WPILib
// knows where to look for our Robot Class.
jar {
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    manifest edu.wpi.first.gradlerio.GradleRIOPlugin.javaManifest(ROBOT_MAIN_CLASS)
}
