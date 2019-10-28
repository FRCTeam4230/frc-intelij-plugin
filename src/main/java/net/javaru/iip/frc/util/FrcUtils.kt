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

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.util.lang.JavaVersion
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.i18n.FrcMessageKey

private object FrcUtils

private val LOG = Logger.getInstance(FrcUtils::class.java)

data class TitleMessagePair(val title:String, val message: String)

/**
 * Can execute code quietly such that no exception is thrown. Some examples to call a method:
 *
 *  * <tt>FrcUtils.executeQuietly(() -> socket.disconnect());</tt>
 *  * <tt>FrcUtils.executeQuietly(socket::disconnect);</tt>
 *
 * @param callable the callable to run
 */
fun executeQuietly(callable: Runnable)
{
    try
    {
        callable.run()
    }
    catch (ignore: Throwable)
    {
    }
}


/**
 * Validates that the selected version of Java in the Wizard is at least that of the provided minimum. If the supplied minimum is null or an invalid version string,
 * a warning is logged and no validation occurs.
 *
 * @param moduleBuilder              the module builder
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
fun validateMinimumJavaVersion(moduleBuilder: ModuleBuilder,
                               wizardContext: WizardContext,
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
    return validateMinimumJavaVersion(moduleBuilder,
                                      wizardContext,
                                      minVersion,
                                      additionalMessageKey)
}

/**
 * @param moduleBuilder                          the module builder
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
fun validateMinimumJavaVersion(moduleBuilder: ModuleBuilder,
                               wizardContext: WizardContext,
                               requiredMinimumJavaVersionFeatureLevel: Int,
                               minor: Int,
                               update: Int,
                               build: Int,
                               additionalMessageKey: FrcMessageKey? = null): Boolean
{
    return validateMinimumJavaVersion(moduleBuilder,
                                      wizardContext,
                                      JavaVersion.compose(requiredMinimumJavaVersionFeatureLevel, minor, update, build, false),
                                      additionalMessageKey)
}

/**
 * @param moduleBuilder                          the module builder
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
fun validateMinimumJavaVersion(moduleBuilder: ModuleBuilder,
                               wizardContext: WizardContext,
                               requiredMinimumJavaVersionFeatureLevel: Int,
                               additionalMessageKey: FrcMessageKey? = null): Boolean
{
    return validateMinimumJavaVersion(moduleBuilder,
                                      wizardContext,
                                      JavaVersion.compose(requiredMinimumJavaVersionFeatureLevel),
                                      additionalMessageKey)
}

/**
 * Validates that the selected version of Java in the Wizard is at least that of trhe provided minimum. If the supplied minimum is null,
 * a warning is logged and no validation occurs.
 *
 * @param moduleBuilder              the module builder
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
fun validateMinimumJavaVersion(moduleBuilder: ModuleBuilder,
                               wizardContext: WizardContext,
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
            val jdkVersionString = sdk.versionString
            val configuredJavaVersion = JavaVersion.tryParse(jdkVersionString)
            //final LanguageLevel languageLevel = LanguageLevel.parse(jdkVersionString);
            // There is also a JavaVersion in the Gradle API code: org.gradle.api.JavaVersion;


            if (configuredJavaVersion != null && !configuredJavaVersion.isAtLeast(requiredMinimumJavaVersion.feature))
            {
                val titleMsgPair = createTitleMessagePair(requiredMinimumJavaVersion, configuredJavaVersion, additionalMessageKey)
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


fun createTitleMessagePair(requiredMinimumJavaVersion: JavaVersion,
                           configuredJavaVersion: JavaVersion,
                           additionalMessageKey: FrcMessageKey?
                          ): TitleMessagePair
{
    val title = message("frc.ui.wizard.validate.minJavaVersion.title")
    
    val additionalMessage = if (additionalMessageKey == null) "" else " ${message(additionalMessageKey)}"
    val message = message("frc.ui.wizard.validate.minJavaVersion.message",
                          requiredMinimumJavaVersion,
                          requiredMinimumJavaVersion.feature,
                          configuredJavaVersion,
                          configuredJavaVersion.feature,
                          additionalMessage)
    
    return TitleMessagePair(title, message)
}
