<#ftl output_format="plainText" encoding="UTF-8">
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
</#compress>
{
  "java.configuration.updateBuildConfiguration": "automatic",
<#if !data.isRomiRobot()>
  "java.server.launchMode":"Standard",
</#if>
  "files.exclude": {
    "**/.git": true,
    "**/.svn": true,
    "**/.hg": true,
    "**/CVS": true,
    "**/.DS_Store": true,
    "bin/": true,
    "**/.classpath": true,
    "**/.project": true,
    "**/.settings": true,
    "**/.factorypath": true<#if !data.isRomiRobot()>,
    "**/*~":true</#if>
  }
}
