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

package net.javaru.iip.frc.wpilib.version

import com.intellij.openapi.diagnostic.Logger
import net.javaru.iip.frc.util.mapExceptionFreeAndNotNull

private val LOG = Logger.getInstance(WpiLibVersionImpl::class.java)

class WpiLibVersionImpl private constructor(override val versionString: String,
                                            override val generation: Int,
                                            override val major: Int,
                                            override val minor: Int,
                                            override val patch: Int,
                                            override val preReleaseModifier: WpiLibVersion.PreReleaseModifier?,
                                            override val preReleaseModifierVersion: Int?,
                                            override val preReleaseModifierSubVersion: String = "",
                                            override val preReleasePreviewVersion: Int?) : AbstractWpiLibVersion()
{

    override fun cloneIt(): WpiLibVersion
    {
        return WpiLibVersionImpl(this.versionString, this.generation, this.major, this.minor, this.patch, this.preReleaseModifier, this.preReleaseModifierVersion, this.preReleaseModifierSubVersion, this.preReleasePreviewVersion)
    }

    companion object
    {

        private val pre2017Pattern = """([\d]{1,2})\.([\d]{1,2})\.([\d]{1,2})\.([\d]{12})""".toRegex().toPattern()
        private val post2017Pattern = """(?<major>[\d]{4})\.(?<minor>[\d]{1,2})(\.(?<patch>[\d]{1,2})(?<preAll>[-.](?<preName>alpha|beta|rc)([-.]?(?<preVersion>[\d]{1,2}))?)?(?<preSubVersion>[a-z])?(?<previewAll>[-.](pre|p-)[-]?(?<previewVer>[\d]*))?)?""".toRegex(RegexOption.IGNORE_CASE).toPattern()


        @Throws(IllegalArgumentException::class)
        fun parse(version: String): WpiLibVersion
        {
            var matcher = post2017Pattern.matcher(version.toLowerCase())
            if (matcher.find())
            {
                val major = Integer.parseInt(matcher.group("major"))
                // WPI released v2018.06.21 as the first GradleRIO version on 2018-06-21. This is when they 
                // appear to have taken the project over, prior to its first use in the 2019 build season.   
                // The next release was 2019.0.0-alpha-1 a few days later on 2018-06-28 
                val generation = if (major >= 2019 || version == "2018.06.21") 2019 else 2017
                return WpiLibVersionImpl(version,
                                         generation,
                                         major,
                                         if (matcher.group("minor") != null) Integer.parseInt(matcher.group("minor")) else 0,
                                         if (matcher.group("patch") != null) Integer.parseInt(matcher.group("patch")) else 0,
                                         if (matcher.group("preAll") != null) WpiLibVersion.PreReleaseModifier.valueOf(matcher.group("preName")) else null,
                                         if (matcher.group("preVersion") != null) Integer.parseInt(matcher.group("preVersion")) else null,
                                         if (matcher.group("preSubVersion") != null) matcher.group("preSubVersion") else "",
                                         if (matcher.group("previewAll") != null) Integer.parseInt(matcher.group("previewVer")) else null
                                        )
            }
            else
            {
                matcher = pre2017Pattern.matcher(version)
                if (matcher.find())
                {
                    val dateTimeString = matcher.group(4)
                    return WpiLibVersionImpl(version,
                                             2015,
                                             Integer.parseInt(dateTimeString.substring(0, 4)),
                                             Integer.parseInt(dateTimeString.substring(4, 6)),
                                             Integer.parseInt(dateTimeString.substring(6, 12)), null, null, "", null)
                }
                else
                {
                    throw IllegalArgumentException("Cannot parse the version string '$version' to a WpiLibVersionImpl instance.")
                }

            }
        }

        /**
         * Converts the list of version strings to a List of `WpiLibVersion`s. Any invalid version strings are ignored/dropped 
         * (but logged at the warm level).
         */
        fun parse(versionStrings: Collection<String>): List<WpiLibVersion> = versionStrings.mapExceptionFreeAndNotNull { parse(it) }
    }
}


