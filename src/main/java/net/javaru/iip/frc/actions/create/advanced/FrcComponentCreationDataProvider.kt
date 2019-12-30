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

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import javax.swing.Icon


abstract class FrcComponentCreationDataProvider
{
    abstract val componentVersion: Int
  
    /**
     * The **primary* simple name of the class type being created for use in Dialogs, error messages, and 
     * possibly for class discovery. For example: `Command`, `Subsystem`, etc. Must not be null or a blank string.
     */
    abstract val componentTypeSimpleName:String

    /**
     * Aan immutable list of possible class names and abbreviations fo use in class discovery. For example
     * for the Command component, this might be `['Command', 'Cmd']`. At a minimum, it should return a list
     * of just the value returned by [componentTypeSimpleName]. Lazy initialization is strongly encouraged.
     */
    abstract val componentTypeSimpleNames: List<String>
    
    open val autoAppendSuffix: String by lazy { componentTypeSimpleName }

    /**
     * Indicates the base type of the template, indicating if it can extend a base class, implement an interface, both, or neither.
     */
    abstract val baseType: BaseType


    /**
     * Returns the highest level interface or class that defines the FRC component. In the event the template
     * does not extend a class or implement an interface, this val should be configured to an empty string (""), and
     * the [baseType] val should be configured to  [BaseType.None]
     * */
    abstract val topLevelClassFqName: String

    /** Returns the typical base class or interface that is extended or implemented by users when creating this FRC component.
     * This may or may not be the same value as the [topLevelClassFqName]. */
    abstract val typicalBaseClassFqName: String
    
    /** Returns a (potentially empty) list of additional base classes. */
    open val additionalBaseClassFqNames: List<String> by lazy { ImmutableList.of<String>() }
    
    /**
     * An immutable Set of base name suffixes for the component. A class ending in one of these
     * suffixes would typically indicate the class was 'made' to be extended, acting as a base
     * class. For example for the Command component, this might be 
     * `['CommandBase', 'CmdBase', 'BaseCommand', 'BaseCmd', 'Base']`.
     */
    open val classTypeBaseNameSuffixes: Set<String> by lazy {
        val names = ImmutableSet.builder<String>()
        componentTypeSimpleNames.forEach {
            names.add("${it}Base") // e.g. CommandBase & CmdBase
            names.add("Base${it}") // e.g. BaseCommand & BaseCmd
        }
        names.add("Base") // On the fence if this should be included. But I could see a class like DriveBase or MoveElevatorBase.
        return@lazy names.build()
    }

    /**
     * An immutable Set of common suffixes for the component. A name ending in one of these suffixes
     * typically indicates the class name does *not* need to have a suffix auto appended to it. 
     * For example, for the Command component, this might be 
     * `['Command', 'Cmd', 'CommandBase', 'CmdBase', 'BaseCommand', 'BaseCmd']`.
     */
    open val componentTypeCommonSuffixes: Set<String> by lazy {
        val names = mutableSetOf<String>()
        names.addAll(componentTypeSimpleNames)
        names.addAll(classTypeBaseNameSuffixes)
        names.remove("Base")
        return@lazy ImmutableSet.copyOf(names)
    }

    abstract val fileTemplateDescriptor: FileTemplateDescriptor
    
    open val icon: Icon by lazy { fileTemplateDescriptor.icon }
    
    open val fileTemplateName: String by lazy { fileTemplateDescriptor.fileName } }

/**
 * Indicates the base type of the template.
 */
enum class BaseType
{
    /** Indicates the template does not extend a class or implement an interface. */
    None,
    
    /** Indicates the template only implements an interface, and does not have the possibility of extending a base class. */
    InterfaceOnly,

    /** Indicates the template only extends a base class, and does not have the possibility of implementing an interface. */
    BaseClassOnly, 
    
    /** indicates the template can either extend a base class or implement an interface. */
    InterfaceAndBaseClass;
    
    fun hasBaseClass(): Boolean
    {
        return when (this)
        {
            None          -> false
            BaseClassOnly -> true
            InterfaceOnly -> true
            InterfaceAndBaseClass -> true
        }
    }


}