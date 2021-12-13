<#ftl output_format="plainText" encoding="UTF-8"> <#-- Output formats: https://freemarker.apache.org/docs/dgui_misc_autoescaping.html -->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
import org.gradle.internal.os.OperatingSystem

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        String frcYear = '${data.frcYearString}'
        File frcHome
        if (OperatingSystem.current().isWindows()) {
            String publicFolder = System.getenv('PUBLIC')
            if (publicFolder == null) {
                publicFolder = "C:\\Users\\Public"
            }
            frcHome = new File(publicFolder, "frc${r"${frcYear}"}")
        } else {
            def userFolder = System.getProperty("user.home")
            frcHome = new File(userFolder, "frc${r"${frcYear}"}")
        }
        def frcHomeMaven = new File(frcHome, 'maven')
        maven {
            name 'frcHome'
            url frcHomeMaven
        }
    }
}
