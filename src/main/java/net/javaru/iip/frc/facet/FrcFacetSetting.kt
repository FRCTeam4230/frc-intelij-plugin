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

package net.javaru.iip.frc.facet

// For example, see org.jetbrains.kotlin.config.KotlinFacetSettings  in Kotlin Plugin
class FrcFacetSetting 
{
    companion object
    {
        // Increment this when making serialization-incompatible changes to configuration data
        val CURRENT_VERSION = 0
        val DEFAULT_VERSION = 0
    }

    var version = CURRENT_VERSION
    // var useProjectSettings: Boolean = true
}


