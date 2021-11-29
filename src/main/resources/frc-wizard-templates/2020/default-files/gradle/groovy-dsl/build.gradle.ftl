<#ftl output_format="plainText" encoding="UTF-8"> <#-- Output formats: https://freemarker.apache.org/docs/dgui_misc_autoescaping.html -->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
plugins {
    id "java"
    id "edu.wpi.first.GradleRIO" version "${data.wpilibVersion.versionString}"
}

sourceCompatibility = JavaVersion.VERSION_11
targetCompatibility = JavaVersion.VERSION_11

def ROBOT_MAIN_CLASS = "${data.mainClassFQ}"

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

// Set this to true to enable desktop support.
def includeDesktopSupport = false

<#if data.junitUseJUnitPlatform()>

test {
    useJUnitPlatform()
}
</#if>
        
dependencies {
    implementation wpi.deps.wpilib()
    nativeZip wpi.deps.wpilibJni(wpi.platforms.roborio)
    nativeDesktopZip wpi.deps.wpilibJni(wpi.platforms.desktop)


    implementation wpi.deps.vendor.java()
    nativeZip wpi.deps.vendor.jni(wpi.platforms.roborio)
    nativeDesktopZip wpi.deps.vendor.jni(wpi.platforms.desktop)

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
    testRuntimeOnly "org.junit.vintage:junit-vintage-engine"
    testImplementation "junit:junit:${data.junit4Version}"
</#if>

    // Enable simulation gui support. Must check the box in vscode to enable support
    // upon debugging
    simulation wpi.deps.sim.gui(wpi.platforms.desktop, false)
}

// Setting up my Jar File. In this case, adding all libraries into the main jar ('fat jar')
// in order to make them all available at runtime. Also adding the manifest so WPILib
// knows where to look for our Robot Class.
jar {
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    manifest edu.wpi.first.gradlerio.GradleRIOPlugin.javaManifest(ROBOT_MAIN_CLASS)
}
