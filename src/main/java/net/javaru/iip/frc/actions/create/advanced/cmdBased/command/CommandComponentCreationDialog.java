/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.swing.*;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.ui.components.JBCheckBox;

import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.FrcComponentCreationDataProvider;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.FrcComponentCreationDialog;
import net.javaru.iip.frc.util.FrcClassUtils2;
import net.javaru.iip.frc.util.FrcClassUtilsKt;
import net.javaru.iip.frc.util.FrcCollectionExtsKt;
import net.javaru.iip.frc.util.FrcUiUtilsKt;
import net.javaru.iip.frc.util.PsiClassNameComparator;
import net.javaru.iip.frc.wpilib.WpiLibConstants;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class CommandComponentCreationDialog extends FrcComponentCreationDialog
{
    private static final Logger logger = LoggerFactory.getLogger(CommandComponentCreationDialog.class);
    
    /** List of full qualified names of the required subsystems. For example 'frc.team9999.ArmSubsystem, frc.team9999.BallSubsystem'. */
    private static final String ALL_SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST = "requiredSubsystemsFqnCommaDelimitedString";
    /** List of simple names of the required subsystems. For example 'ArmSubsystem, BallSubsystem'. */
    private static final String ALL_SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST = "requiredSubsystemsNamesCommaDelimitedString";
    /** List of variable names for the required subsystems. For example 'armSubsystem, ballSubsystem' for the 'ArmSubsystem, BallSubsystem'. */
    private static final String ALL_SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST = "requiredSubsystemsVarsCommaDelimitedString";
    /** List of singleton calls for subsystems that are singletons. Non-singletons have a &lt;NULL&gt; placeholder. For example:
     * 'JavaSubsystem.getInstance(),KotlinSubsystem.INSTANCE,&lt;NULL&gt;,&lt;NULL&gt;' (with two non-singleton subsystems as part of the listing). */
    private static final String ALL_SUBSYSTEMS_SINGLETON_CALLS_COMMA_DELIMITED_LIST = "requiredSubsystemsSingletonCallsCommaDelimitedString";
    
    /** List of full qualified names of the required subsystems. For example 'frc.team9999.ArmSubsystem, frc.team9999.BallSubsystem'. */
    private static final String SINGLETON_SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST = "singletonSubsystemsFqnCommaDelimitedString";
    /** List of simple names of the required subsystems. For example 'ArmSubsystem, BallSubsystem'. */
    private static final String SINGLETON_SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST = "singletonSubsystemsNamesCommaDelimitedString";
    /** List of variable names for the required subsystems. For example 'armSubsystem, ballSubsystem' for the 'ArmSubsystem, BallSubsystem'. */
    private static final String SINGLETON_SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST = "singletonSubsystemsVarsCommaDelimitedString";
    /** List of singleton calls for subsystems that are singletons. Non-singletons have a &lt;NULL&gt; placeholder. For example:
     * 'JavaSubsystem.getInstance(),KotlinSubsystem.INSTANCE,&lt;NULL&gt;,&lt;NULL&gt;' (with two non-singleton subsystems as part of the listing). */
    private static final String SINGLETON_SUBSYSTEMS_SINGLETON_CALLS_COMMA_DELIMITED_LIST = "singletonSubsystemsSingletonCallsCommaDelimitedString";
    
    private static final String NON_SINGLETON_SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST = "nonSingletonSubsystemsFqnCommaDelimitedString";
    /** List of simple names of the required subsystems. For example 'ArmSubsystem, BallSubsystem'. */
    private static final String NON_SINGLETON_SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST = "nonSingletonSubsystemsNamesCommaDelimitedString";
    /** List of variable names for the required subsystems. For example 'armSubsystem, ballSubsystem' for the 'ArmSubsystem, BallSubsystem'. */
    private static final String NON_SINGLETON_SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST = "nonSingletonSubsystemsVarsCommaDelimitedString";
    
    
    private static final String KOTLIN_PRIMARY_CONSTRUCTOR = "kotlinPrimaryConstructor";
    private static final String KOTLIN_BASE_CLASS_CONSTRUCTOR = "kotlinBaseClassConstructor";
    private static final String HAS_ONLY_SINGLETON_SUBSYSTEMS =  "hasOnlySingletonSubsystems";
    private static final String HAS_SINGLETON_SUBSYSTEMS =       "hasSingletonSubsystems";
    private static final String NULL_VALUE = "<NULL>";
    private Map<PsiClass, JBCheckBox> mySubsystemsClassesMap;
    
    
    public CommandComponentCreationDialog(@NotNull Module module,
                                          @NotNull ClassCreator<? extends PsiElement> classCreator,
                                          @NotNull PsiDirectory directory,
                                          @NotNull FrcComponentCreationDataProvider dataProvider)
    {
        super(module, classCreator, directory, dataProvider);
    }
    
    
    @Override
    protected void initMajorOptionsPanel(JPanel topPanel, JPanel optionsPanel)
    {
        if (includeSubsystemSelection())
        {
            // Display subsystems
            final String labelText = message("frc.new.class.adv.command.dialog.subsystems.label");
            List<PsiClass> subsystems = FrcClassUtilsKt.findImplementationsInModule(getSubsystemTopClassFQN(),
                                                                                    myModule,
                                                                                    false,
                                                                                    false,
                                                                                    null);
            // TODO: add option to show abstract subsystem classes... although I can't think of a use case for making an abstract subsystem a required subsystem of a command 
            
            //  hasModifier(JvmModifier.ABSTRACT)  is an experimental API. So for now we use the (older) hasModifierProperty(PsiModifier.ABSTRACT)
            //subsystems = subsystems.stream().filter(psiClass -> !psiClass.hasModifier(JvmModifier.ABSTRACT)).collect(Collectors.toList());
            subsystems = subsystems.stream().filter(psiClass -> !psiClass.hasModifierProperty(PsiModifier.ABSTRACT)).collect(Collectors.toList());
            subsystems = FrcClassUtilsKt.sortedByName(subsystems);
            
            // See our FrcClassUtilsKt.isAbstract() extension
            this.mySubsystemsClassesMap = FrcUiUtilsKt.initClassSelectionPanelCheckBoxes(topPanel,
                                                                                         optionsPanel,
                                                                                         labelText,
                                                                                         subsystems,
                                                                                         true);
        }
    }
    
    
    protected String getSubsystemTopClassFQN()
    {
        if (myDataProvider instanceof CommandCreationDataProvider)
        {
            return ((CommandCreationDataProvider) myDataProvider).getSubsystemTopFqName();
        }
        else
        {
            return WpiLibConstants.SUBSYSTEM_V2_TOP_FQN;
        }
    }
    
    protected boolean includeSubsystemSelection()
    {
        if (myDataProvider instanceof CommandCreationDataProvider)
        {
            return ((CommandCreationDataProvider) myDataProvider).getIncludeSubsystemChooser();
        }
        else
        {
            return true;
        }
    }
    
    @Override
    protected void addComponentSpecificProperties(@NotNull Map<String, String> props,
                                                  @Nullable PsiClass baseClass,
                                                  @NotNull String targetPackageName,
                                                  @NotNull String newClassName)
    {
        addSubsystemProperties(props);
    }
    
    
    protected void addSubsystemProperties(@NotNull Map<String, String> props)
    {
        final List<PsiClass> subsystems = getSelectedSubsystems();
        final List<PsiClass> nonSingletonSubsystems = new ArrayList<>();
        final List<PsiClass> singletonSubsystems = new ArrayList<>();
        subsystems.forEach(psiClass -> {
            if (FrcClassUtils2.isSingleton(psiClass))
                singletonSubsystems.add(psiClass);
            else
                nonSingletonSubsystems.add(psiClass);
        });
    
        final boolean hasSingletonSubsystems = !singletonSubsystems.isEmpty();
        final boolean hasNonSingletonSubsystems = !nonSingletonSubsystems.isEmpty();
        final boolean hasOnlySingletonSubsystems = !hasNonSingletonSubsystems;
        props.put(HAS_ONLY_SINGLETON_SUBSYSTEMS, Boolean.toString(hasOnlySingletonSubsystems));
        props.put(HAS_SINGLETON_SUBSYSTEMS, Boolean.toString(hasSingletonSubsystems));
    
        addSubsystemProperties(props,
                               subsystems,
                               ALL_SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST,
                               ALL_SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST,
                               ALL_SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST,
                               ALL_SUBSYSTEMS_SINGLETON_CALLS_COMMA_DELIMITED_LIST);
    
        addSubsystemProperties(props,
                               singletonSubsystems,
                               SINGLETON_SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST,
                               SINGLETON_SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST,
                               SINGLETON_SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST,
                               SINGLETON_SUBSYSTEMS_SINGLETON_CALLS_COMMA_DELIMITED_LIST);
    
        addSubsystemProperties(props,
                               nonSingletonSubsystems,
                               NON_SINGLETON_SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST,
                               NON_SINGLETON_SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST,
                               NON_SINGLETON_SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST,
                               null);
    
        final boolean isBaseClassAnInterface = BooleanUtils.toBoolean(props.get(BASE_CLASS_IS_INTERFACE));
        final StringBuilder sb = new StringBuilder();
        if (myDataProvider.isKotlinTemplate())
        {
            // #if($makeAbstract == "true")abstract#end class ${NAME}${kotlinPrimaryConstructor}${baseClassExtendsClause}${kotlinBaseClassConstructor}
            // in the parent FrcComponentCreationDialog, the baseClassExtendsClause is set to:  ": baseName"
            //   class FooCommand: CommandBase()
            //   class FooCommand(private var subsystemOne: SubsystemOne): CommandBase()
            //   class FooCommand: Command
            //   class FooCommand(private var subsystemOne: SubsystemOne): Command
            
            
            if (hasNonSingletonSubsystems)
            {
                sb.append("(");
                final Iterator<PsiClass> iterator = nonSingletonSubsystems.iterator();
                while (iterator.hasNext())
                {
                    PsiClass psiClass = iterator.next();
                    final String className = psiClass.getName();
                    // TODO: look to see if there is a more sophisticated method in IntelliJ IDEA for suggesting/creating a variable name to handle things like "URLBuilder" to be 'urlBuilder' and not 'uRLBuilder'
                    final String varName = StringUtils.uncapitalize(className);
                    sb.append("private val ").append(varName).append(": ").append(className);
                    if (iterator.hasNext())
                    {
                        sb.append(", ");
                    }
    
                }
                sb.append(")");
            }
            props.put(KOTLIN_PRIMARY_CONSTRUCTOR, sb.toString());
            
            if (isBaseClassAnInterface)
            {
                props.put(KOTLIN_BASE_CLASS_CONSTRUCTOR, "");
            }
            else
            {
                props.put(KOTLIN_BASE_CLASS_CONSTRUCTOR, "()");
            }
        }
        
        // note: the properties are logged at trace level in doOKAction() of the parent class
    }
    
    
    private void addSubsystemProperties(@NotNull Map<String, String> props,
                                        @NotNull List<PsiClass> subsystems,
                                        @NotNull String fqnKey,
                                        @NotNull String simpleNameKey,
                                        @NotNull String varNameKey,
                                        @Nullable String singletonCallKey)
    {
        final String subSystemsFQN = FrcCollectionExtsKt.toCommaDelimitedString(subsystems, false, PsiClass::getQualifiedName);
        props.put(fqnKey, subSystemsFQN);
        
        final String subSystemsSimpleNames = FrcCollectionExtsKt.toCommaDelimitedString(subsystems, false, PsiClass::getName);
        props.put(simpleNameKey, subSystemsSimpleNames);
        
        final String subSystemsVarNames = FrcCollectionExtsKt.toCommaDelimitedString(subsystems, false, psiClass ->
                StringUtils.uncapitalize(psiClass.getName()));
        props.put(varNameKey, subSystemsVarNames);
       
        if (singletonCallKey != null)
        {
            final List<String> singletonCalls = createSingletonCalls(subsystems);
            final String singletonCallsString = FrcCollectionExtsKt.toCommaDelimitedString(singletonCalls, false, String::toString, NULL_VALUE);
            props.put(singletonCallKey, singletonCallsString);
        }
    }
    
    
    @NotNull
    protected List<String> createSingletonCalls(@NotNull List<PsiClass> classes)
    {
        final List<String> calls = new ArrayList<>(classes.size());
        for (PsiClass psiClass : classes)
        {
            final String singletonCall = createSingletonCall(psiClass);
            calls.add(singletonCall);
        }
        return calls;
    }
    
    
    @Nullable
    private String createSingletonCall(PsiClass psiClass)
    {
        String result = null;
        if (FrcClassUtils2.isSingleton(psiClass))
        {
            // favor using the getter if it is present
            final PsiMethod singletonMethod = FrcClassUtils2.getSingletonMethod(psiClass);
            if (singletonMethod != null)
            {
                result = psiClass.getName() + "." + singletonMethod.getName() + "()";
            }
            else
            {
                final PsiField singletonField = FrcClassUtils2.getSingletonField(psiClass);
                if (singletonField != null && singletonField.hasModifierProperty(PsiModifier.PUBLIC))
                {
                    result = psiClass.getName();
                    if (!(myDataProvider.isKotlinTemplate() && isKotlinObjectInstance(singletonField)))
                    {
                        result += "." + singletonField.getName();
                    }
                }
            }
        }
        return result;
    }
    
    private boolean isKotlinObjectInstance(final PsiField singletonField)
    {
        return  ("INSTANCE".equals(singletonField.getName()) && singletonField.getClass().getName().startsWith("org.jetbrains.kotlin"));
    }
    
    
    protected List<PsiClass> getSelectedSubsystems()
    {
        final List<PsiClass> subsystems = new ArrayList<>();
        if (includeSubsystemSelection())
        {
            for (Entry<PsiClass, JBCheckBox> entry : mySubsystemsClassesMap.entrySet())
            {
                if (entry.getValue().isSelected())
                {
                    subsystems.add(entry.getKey());
                }
            }
            subsystems.sort(PsiClassNameComparator.INSTANCE);
        }
        return subsystems;
    }
}
