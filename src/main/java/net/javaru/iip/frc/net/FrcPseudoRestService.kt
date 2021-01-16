/*
 * Copyright 2015-2021 the original author or authors
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

package net.javaru.iip.frc.net

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.HttpRequests
import net.javaru.iip.frc.util.getPluginResourceAsText

private val LOG = Logger.getInstance(FrcPseudoRestService::class.java)
const val gitBaseRawUrl = "https://gitlab.com/Javaru/frc-intellij-idea-plugin/-/raw"

/**
 * A Pseudo REST Service that can fetch basic resources (basically just files) from the project's git code repository.
 * This is a hackish way to get current data without the hassle/expense of setting up a web server or REST serve somewhere.
 * It is primarily meant to check for notifications we want to "broadcast" to users.
 */
object FrcPseudoRestService
{
    // IN the Plugin.xml we make this an application service so in the future we can potentially add some caching and/or synchronization, etc.
    fun getInstance(): FrcPseudoRestService = service()

    /**
     * Gets a (raw) resource from the project's source control repo, from the `rest-v`` branch by default.
     * If the source code repo is not available, the one included in the plugin (at build) time can be used if available.
     *
     * *Do NOT call from EDT or inside a read action*
     *
     * To call from a `StartupActivity`, call in a `Task.Backgroundable`, Task.modal`, or `Task.ConditionalModal`,
     * being sure to not
     * to forget to call the `queue()` method to queue it up. For example:
     *
     * ```
     * object : Task.Backgroundable(project, "My Task Name")
     * {
     *     override fun run(indicator: ProgressIndicator)
     *     {
     *         val text = FrcPseudoRestService.getPseudoRestServiceResource("some/path/data.txt")
     *     }
     * }.queue()
     *
     * object : Task.Modal(project, "My Task Name", canBeCancelled = true)
     * {
     *     override fun run(indicator: ProgressIndicator)
     *     {
     *         val text = FrcPseudoRestService.getPseudoRestServiceResource("some/path/data.txt")
     *     }
     * }.queue()
     *
     * ```
     *
     * More preferably, put the work in a service, and call the service method in the `Task.Backgroundable`
     *
     *
     * @param resourcePath the 'classpath' relative path of the desired resource. This should exclude
     *                     the base URL, repo information, branch, and src path An example would be
     *                     `"licenses/wpilib-license.txt"` (to get the "src/main/resources/licenses/wpilib-license.txt" file)
     *                     Be sure to NOT include a leading slash.
     *
     * @param basePath the base path such as `"src/main/resources"`. In essence the project path that is not part of the path
     *                 for the resource when it is on the classpath. Be sure to NOT include a leading or trailing slash.
     *                 Defaults to `"src/main/resources"`
     *
     * @param branchName the name of the branch to fetch the resource from. Default is `rest-v1` (and NOT `main` or `master`)
     *
     * @param defaultToBundled if true, if the resource could not be obtained from the server, the resource bundled/included
     *                         in the plugin will be read from the classpath and used if available.
     */
    @JvmOverloads
    fun getResource(resourcePath: String, basePath: String? = "src/main/resources", branchName: String = "rest-v1", defaultToBundled: Boolean = true): String?
    {
        // NOTE: We can not test or run this outside the IDE. Doing so results in 403 errors
        //       as the HttpRequests code is run without certificate stores loaded

        // https://gitlab.com/Javaru/frc-intellij-idea-plugin/-/raw/branchName/src/main/resources/license.txt
        val fullResourcePath = if (basePath.isNullOrBlank()) resourcePath else "$basePath/$resourcePath".removePrefix("/")
        val urlPath = "$gitBaseRawUrl/$branchName/$fullResourcePath"
        return try
        {
            val request = HttpRequests.request(urlPath)
            request.readString()
        }
        catch (e: Exception)
        {
            val additionalMsg = if (defaultToBundled) " Will check for resource on classpath." else ""
            LOG.info("[FRC] getPseudoRestServiceResource() could not get resource from url '$urlPath'.$additionalMsg Cause: $e")
            return if (defaultToBundled)
            {
                // First look for the resource directly on the class path, then look for it along with the basePath if not found
                val text = getPluginResourceAsText(resourcePath) ?: getPluginResourceAsText(fullResourcePath)
                text
            } else
                null
        }
    }
}





