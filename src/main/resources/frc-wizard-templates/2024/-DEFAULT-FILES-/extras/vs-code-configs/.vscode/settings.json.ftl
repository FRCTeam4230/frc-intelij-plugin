<#ftl output_format="plainText" encoding="UTF-8">
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
</#compress>

{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.server.launchMode": "Standard",
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
    "**/.factorypath": true,
    "**/*~": true
  },
<#if data.isRomiTemplate() || data.isXrpTemplate()>
  "wpilib.skipSelectSimulateExtension": true,
</#if>
  "java.test.config": [
    {
<#noparse>
      "name": "WPIlibUnitTests",
      "workingDirectory": "${workspaceFolder}/build/jni/release",
      "vmargs": [ "-Djava.library.path=${workspaceFolder}/build/jni/release" ],
      "env": {
        "LD_LIBRARY_PATH": "${workspaceFolder}/build/jni/release" ,
        "DYLD_LIBRARY_PATH": "${workspaceFolder}/build/jni/release"
</#noparse>
      }
    },
  ],
  "java.test.defaultConfig": "WPIlibUnitTests"
}