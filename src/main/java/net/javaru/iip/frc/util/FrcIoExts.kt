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

import java.nio.file.Path


///**
// * Returns the end path by removing the base path from the full path.
// * For example, give a full path of `/java/jdk8/docs/api` and a base path of `/java/jdk8`, this will return `docs/api`
// */
//fun getEndPath(fullPath: Path, basePath: Path): Path = fullPath.subpath(basePath.nameCount, fullPath.nameCount)

/**
 * Returns the end path by removing the base path from the full path (i.e. the receiver/this).
 * For example, give a full path (receiver) of `/java/jdk8/docs/api` and a base path of `/java/jdk8`, this will return `docs/api`
 */
fun Path.removeBasePath(basePath: Path): Path = this.subpath(basePath.nameCount, this.nameCount)
