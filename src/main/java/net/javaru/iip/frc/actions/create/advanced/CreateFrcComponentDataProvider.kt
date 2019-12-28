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

package net.javaru.iip.frc.actions.create.advanced

import com.google.common.collect.ImmutableSet


abstract class CreateFrcComponentDataProvider
{
    /**
     * The **primary* simple name of the class type being created for use in Dialogs, error messages, and 
     * possibly for class discovery. For example: `Command`, `Subsystem`, etc. Must not be null or a blank string.
     */
    abstract val classTypeSimpleName:String

    /**
     * Aan immutable list of possible class names and abbreviations fo use in class discovery. For example
     * for the Command component, this might be `['Command', 'Cmd']`. At a minimum, it should return a list
     * of just the value returned by [classTypeSimpleName]. Lazy initialization is strongly encouraged.
     */
    abstract val classTypeSimpleNames: List<String>

    /**
     * An immutable Set of base name suffixes for the component. A class ending in one of these
     * suffixes would typically indicate the class was 'made' to be extended, acting as a base
     * class. For example for the Command component, this might be 
     * `['CommandBase', 'CmdBase', 'BaseCommand', 'BaseCmd', 'Base']`.
     */
    open val classTypeBaseNameSuffixes: Set<String> by lazy {
        val names = ImmutableSet.builder<String>()
        classTypeSimpleNames.forEach {
            names.add("${it}Base") // e.g. CommandBase & CmdBase
            names.add("Base${it}") // e.g. BaseCommand & BaseCmd
        }
        names.add("Base") // On the fence if this should be included. But I could see a class like DriveBase or MoveElevatorBase.
        return@lazy names.build()
    };

    /**
     * An immutable Set of common suffixes for the component. A name ending in one of these suffixes
     * typically indicates the class name does *not* need to have a suffix auto appended to it. 
     * For example, for the Command component, this might be 
     * `['Command', 'Cmd', 'CommandBase', 'CmdBase', 'BaseCommand', 'BaseCmd']`.
     */
    open val componentTypeCommonSuffixes: Set<String> by lazy {
        val names = mutableSetOf<String>()
        names.addAll(classTypeSimpleNames)
        names.addAll(classTypeBaseNameSuffixes)
        names.remove("Base")
        return@lazy ImmutableSet.copyOf(names)
    }

    /** Returns the highest level interface of class that defines the FRC component. */
    abstract val topLevelClassFqName: String
    
    /** Returns the typical base class or interface that is extended or implemented by users when creating this FRC component. 
     * This may or may not be the same value as the [topLevelClassFqName]. */
    abstract val typicalBaseClassFqName: String
}