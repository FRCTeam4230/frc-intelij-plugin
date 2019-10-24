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

package net.javaru.iip.frc.freemarker

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import net.javaru.iip.frc.FrcIcons

const val FM_TEMPLATE_EXT = ".ftl"

/**
 * @param resourceLoaderClass a class to be used for resource loading. This basically just needs to be any class within the plugin.
 *                            We may have to rethink this when we implement templates via extension points
 *                            
 * @param basePackagePath the base package for loading of templates. Separate steps with `/` `.` and note that it matters if this starts with
 *            `/` or not: f it doesn't start with a / then it's relative to the path (package) of the resourceLoaderClass class. If it starts 
 *            with / then it's relative to the root of the package hierarchy. Note that path components should be separated by forward slashes 
 *            independently of the separator character used by the underlying operating system.
 *            
 */
@JvmOverloads
fun freemarkerConfiguration(resourceLoaderClass:  Any, basePackagePath: String = "/"): Configuration = freemarkerConfiguration(basePackagePath, resourceLoaderClass::class.java)

/**
 * @param resourceLoaderClass a class to be used for resource loading. This basically just needs to be any class within the plugin.
 *                            We may have to rethink this when we implement templates via extension points
 *                            
 * @param basePackagePath the base package for loading of templates. Separate steps with `/` `.` and note that it matters if this starts with
 *            `/` or not: f it doesn't start with a / then it's relative to the path (package) of the resourceLoaderClass class. If it starts 
 *            with / then it's relative to the root of the package hierarchy. Note that path components should be separated by forward slashes 
 *            independently of the separator character used by the underlying operating system.
 *            
 */
@JvmOverloads
fun freemarkerConfiguration(basePackagePath: String = "/", resourceLoaderClass: Class<*> = FrcIcons::class.java): Configuration
{
    val cfg = Configuration(Configuration.VERSION_2_3_29)
    // we just need a class on our classpath, so we use FrcIcons as a convenient class
    cfg.setClassForTemplateLoading(resourceLoaderClass, basePackagePath)
    cfg.defaultEncoding = "UTF-8"

    // Sets how errors will appear.
    //cfg.templateExceptionHandler = TemplateExceptionHandler.DEBUG_HANDLER
    cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

    // Don't log exceptions inside FreeMarker that it will throw at you anyway:
    cfg.logTemplateExceptions = false
//    val tcHTML = TemplateConfiguration()
//    tcHTML.outputFormat = HTMLOutputFormat.INSTANCE
//    cfg.templateConfigurations = ConditionalTemplateConfigurationFactory(PathGlobMatcher("templates/html/**"), tcHTML)
    return cfg
}