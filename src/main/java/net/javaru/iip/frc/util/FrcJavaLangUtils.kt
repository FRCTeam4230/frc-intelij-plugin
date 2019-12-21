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

package net.javaru.iip.frc.util

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.pom.java.LanguageLevel
import com.intellij.util.lang.JavaVersion
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.FrcPluginGlobals.DEFAULT_MIN_REQUIRED_JAVA_VERSION
import net.javaru.iip.frc.FrcPluginGlobals.DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL
import net.javaru.iip.frc.i18n.FrcBundle
import net.javaru.iip.frc.i18n.FrcMessageKey

// *************************
// ALSO SEE FrcPsiNameHelper
//          WpiLibHelpers.kt
// *************************

private object FrcJavaLanUtils
private val LOG = Logger.getInstance(FrcJavaLanUtils::class.java)

/*
==========================================================================================================================================================
The various Java validity checks such isValidPackage are mostly extracted from com.intellij.psi.PsiNameHelper and com.intellij.psi.impl.PsiNameHelperImpl
However it requires a Project to create an PsiNameHelperImpl. All it does with the project is extract the "LanguageLevelProjectExtension"
Then from that it primarily uses the getLanguageLevel() method. We want to use in the Wizard, where we do not yet have a Project. So we've extracted them
here for use in our plugin.
That said, the PsiNameHelperImpl.getInstance() 
==========================================================================================================================================================
 */


/**
 * Validates that the selected version of Java in the Wizard is at least that of the provided minimum. If the supplied minimum is null or an invalid version string,
 * a warning is logged and no validation occurs.
 *
 * @param wizardContext              the wizard context
 * @param requiredMinimumJavaVersion the minimum version of java required
 *
 * @return true if the configured JDK meets the minimum requirements *or if it cannot be validated*
 *
 * @throws ConfigurationException if the configured JDK does not meet the minimum required version
 *
 */
@Throws(ConfigurationException::class)
@JvmOverloads
fun validateMinimumJavaVersion(wizardContext: WizardContext,
                               requiredMinimumJavaVersion: String?,
                               additionalMessageKey: FrcMessageKey? = null): Boolean
{
    if (requiredMinimumJavaVersion == null)
    {
        LOG.warn("[FRC] Supplied minimum Java version String was null. Unable to validate the selected SDK (in the wizard) meets the minimum requirements.")
        return true
    }
    val minVersion = JavaVersion.tryParse(requiredMinimumJavaVersion)
    if (minVersion == null)
    {
        LOG.warn("[FRC] Supplied minimum Java version String of '" + requiredMinimumJavaVersion
                 + "' could not be parsed into a Java version. Unable to validate the selected SDK (in the wizard) meets the minimum requirements.")
        return true
    }
    return validateMinimumJavaVersion(wizardContext,
                                      minVersion,
                                      additionalMessageKey)
}

/**
 * @param wizardContext                          the wizard context
 * @param requiredMinimumJavaVersionFeatureLevel the feature level of the minimum version of java required. For example '8' for '1.8.0_221' and '11' for '11.0.1' and '11.0.1.5'
 * @param minor                                  the minor value of the minimum version of java required. For example '0' for '1.8.0_221' and '11.0.1'
 * @param update                                 the minor value of the minimum version of java required. For example '221' for '1.8.0_221' and  '1' for '11.0.1'
 * @param build                                  the minor value of the minimum version of java required. For example  '0' for '1.8.0_221' and '11.0.1'.
 *
 * @return true if the configured JDK meets the minimum requirements *or if it cannot be validated*
 *
 * @throws ConfigurationException if the configured JDK does not meet the minimum required version
 *
 */
@Throws(ConfigurationException::class)
@JvmOverloads
fun validateMinimumJavaVersion(wizardContext: WizardContext,
                               requiredMinimumJavaVersionFeatureLevel: Int,
                               minor: Int,
                               update: Int,
                               build: Int,
                               additionalMessageKey: FrcMessageKey? = null): Boolean
{
    return validateMinimumJavaVersion(wizardContext,
                                      JavaVersion.compose(requiredMinimumJavaVersionFeatureLevel, minor, update, build, false),
                                      additionalMessageKey)
}

/**
 * @param wizardContext                          the wizard context
 * @param requiredMinimumJavaVersionFeatureLevel the feature level of the minimum version of java required. For example '8' for '1.8.0_221' and '11' for '11.0.1' and '11.0.1.5'
 *
 * @return true if the configured JDK meets the minimum requirements *or if it cannot be validated*
 *
 * @throws ConfigurationException if the configured JDK does not meet the minimum required version
 *
 */
@Throws(ConfigurationException::class)
@JvmOverloads
fun validateMinimumJavaVersion(wizardContext: WizardContext,
                               requiredMinimumJavaVersionFeatureLevel: Int,
                               additionalMessageKey: FrcMessageKey? = null): Boolean
{
    return validateMinimumJavaVersion(wizardContext,
                                      JavaVersion.compose(requiredMinimumJavaVersionFeatureLevel),
                                      additionalMessageKey)
}

/**
 * Validates that the selected version of Java in the Wizard is at least that of the provided minimum. If the supplied minimum is null,
 * a warning is logged and no validation occurs.
 *
 * @param wizardContext              the wizard context
 * @param requiredMinimumJavaVersion the minimum version of java required
 * @param additionalMessageKey       a message key data object to be used to append an additional information to the configuration error message. 
 *                                   The following parameters are automatically passed into when resolving the message bundle: 
 *                                   0=required Java version;  1=required Java level;  2=configured Java version;  3=configured Java level;
 *                                   Thus those can be used in the additional message. Any parameters used by the additional message must start with 4 in 
 *                                   the message key regardless of whether the default supplied parameters are used. 
 *
 * @return true if the configured JDK meets the minimum requirements *or if it cannot be validated*
 *
 * @throws ConfigurationException if the configured JDK does not meet the minimum required version
 */
@Throws(ConfigurationException::class)
@JvmOverloads
fun validateMinimumJavaVersion(wizardContext: WizardContext,
                               requiredMinimumJavaVersion: JavaVersion?,
                               additionalMessageKey: FrcMessageKey? = null): Boolean
{
    if (requiredMinimumJavaVersion == null)
    {
        LOG.warn("[FRC] Supplied minimum JavaVersion null. Unable to validate the selected SDK (in the wizard) meets the minimum requirements.")
        return true
    }
    val sdk: Sdk? = wizardContext.projectJdk
    if (sdk == null)
    {
        LOG.warn("[FRC] sdk not set on the wizardContext. Unable to validate the selected SDK (in the wizard) meets the minimum requirements.")
    }
    if (sdk is ProjectJdkImpl)
    {
        try
        {
            if (!sdk.isValidJavaVersion(requiredMinimumJavaVersion))
            {
                val jdkVersionString = sdk.versionString
                val configuredJavaVersion = JavaVersion.tryParse(jdkVersionString)
                val titleMsgPair = createInvalidJdkTitleMessagePair(requiredMinimumJavaVersion, configuredJavaVersion, additionalMessageKey)
                throw ConfigurationException(titleMsgPair.message, titleMsgPair.title)
            }
        }
        catch (e: Exception)
        {
            if (e is ConfigurationException)
            {
                throw e
            }
            LOG.warn("Could not validate minimum JDK version due to the exception: $e", e)
        }
    }
    return true
}

/**
 * Extracts the language level fro a JavaVersion object. In the event the JavaVersion is null or the language level cannot successfully be extracted,
 * [FrcPluginGlobals.DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL] is used and returned.
 */
fun JavaVersion?.extractLanguageLevel(): LanguageLevel = LanguageLevel.parse(toString()) ?: DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL


fun ProjectJdkImpl.isValidJavaVersion(requiredMinimumJavaVersionFeatureLevel: Int): Boolean
{
    val jdkVersion = JavaVersion.tryParse(this.versionString)
    // We want to filter out other JavaSdkType (and JavaDependentSdkType) SDKs such as IdeaJdk (for plugin development) and even AndroidSdkType 
    val isValidSdkType = this.sdkType is com.intellij.openapi.projectRoots.JavaSdk 
    return (isValidSdkType && jdkVersion?.isAtLeast(requiredMinimumJavaVersionFeatureLevel) ?: false)
}

@JvmOverloads
fun Sdk?.isValidJdk(requiredMinimumJavaVersion: JavaVersion = DEFAULT_MIN_REQUIRED_JAVA_VERSION): Boolean = if (this != null && this is ProjectJdkImpl) this.isValidJavaVersion(requiredMinimumJavaVersion) else false

fun ProjectJdkImpl.isValidJavaVersion(requiredMinimumJavaVersion: JavaVersion): Boolean = isValidJavaVersion(requiredMinimumJavaVersion.feature)

fun createInvalidJdkTitleMessagePair(requiredMinimumJavaVersion: JavaVersion,
                                     configuredJavaVersion: JavaVersion?,
                                     additionalMessageKey: FrcMessageKey?
                                    ): TitleMessagePair
{
    val title = FrcBundle.message("frc.ui.wizard.validate.minJavaVersion.title")
    
    val additionalMessage = if (additionalMessageKey == null) "" else " ${FrcBundle.message(additionalMessageKey)}"
    val message = FrcBundle.message("frc.ui.wizard.validate.minJavaVersion.message",
                                    requiredMinimumJavaVersion,
                                    requiredMinimumJavaVersion.feature,
                                                            configuredJavaVersion
                                                            ?: FrcBundle.message("frc.ui.wizard.validate.minJavaVersion.noSdk"),
                                                            configuredJavaVersion?.feature ?: "?",
                                    additionalMessage)
    
    return TitleMessagePair(title, message)
}

/**
 * Safely extracts the language level from a JavaVersion. In the event the JavaVersion is `null`, or the language level
 * cannot be extracted, the [FrcPluginGlobals.DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL] is returned.
 */
fun JavaVersion?.extractLanguageLevelSafely(): LanguageLevel
{
    if (this == null)
    {
        LOG.debug("[FRC] Supplied JavaVersion was null. Will default language level to $DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL")
        return DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL
    }
    
    var languageLevel = LanguageLevel.parse(toString())
    if (languageLevel == null)
    {
        LOG.info("[FRC] Could not parse JavaVersion '${this}' to a language level. Will default to $DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL")
        languageLevel = DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL
    }
    return languageLevel
}



