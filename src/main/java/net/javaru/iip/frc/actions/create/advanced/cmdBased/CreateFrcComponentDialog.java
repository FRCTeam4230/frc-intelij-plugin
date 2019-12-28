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

package net.javaru.iip.frc.actions.create.advanced.cmdBased;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;

import kotlin.Unit;
import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.CreateFrcComponentDataProvider;
import net.javaru.iip.frc.util.FrcClassUtilsKt;
import net.javaru.iip.frc.util.FrcUiUtilsKt;
import net.javaru.iip.frc.util.PsiClassNameComparator;
import net.javaru.iip.frc.wpilib.WpiLibConstants;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public abstract class CreateFrcComponentDialog extends DialogWrapper
{

    private static final String BASE_CLASS_FQ_NAME = "baseClassFqName";
    private static final String BASE_CLASS_NEEDS_IMPORTING = "baseClassNeedsImporting";
    private static final String BASE_CLASS_EXTENDS_CLAUSE = "baseClassExtendsClause";
    private static final String NEEDS_GET_REQUIREMENTS = "needsGetRequirementsImpl";
    private static final String INCLUDE_JAVADOC_FOR_OVERRIDES = "includeJavaDocsForOverrides";
    private static final String MAKE_ABSTRACT = "makeAbstract";
    
    @NotNull
    protected final Project myProject;
    @NotNull
    protected final Module myModule;
    @NotNull
    protected final ClassCreator myClassCreator;
    @NotNull
    protected final PsiDirectory myDirectory;
    @NotNull
    protected final CreateFrcComponentDataProvider myDataProvider;
    
    private static final Logger LOG = Logger.getInstance(CreateFrcComponentDialog.class);
    private JPanel myTopPanel;
    private JBLabel myComponentNameLabel;
    private JBTextField myComponentNameTextField;
    private JCheckBox myAutoAppendComponentTypeCheckBox;
    private JPanel myOptionsPanel;
    private JPanel mySuperClassPanel;
    private JCheckBox myIncludeJavaDocCheckBox;
    
    
    private Map<PsiClass, JBRadioButton> myTopLevelComponentClassesMap;
    private ButtonGroup mySuperButtonGroup = new ButtonGroup();
    
    
    /**
     * <strong style="font-color: red;">Implmenting classes must call <tt>init()</tt> at the end of their constructors.</strong>
     *
     * @param module       the module
     * @param classCreator the ClassCreator to use
     * @param directory    The PsiDirectory the class the action was called on.
     */
    protected CreateFrcComponentDialog(@NotNull Module module,
                                    @NotNull ClassCreator classCreator,
                                    @NotNull PsiDirectory directory,
                                    @NotNull CreateFrcComponentDataProvider dataProvider)
    {
        super(module.getProject());
        this.myModule = module;
        this.myProject = module.getProject();
        this.myClassCreator = classCreator;
        this.myDirectory = directory;
        this.myDataProvider = dataProvider;
        setTitle(getTitle());
        preInitUI();
        initUI();
        postInitUI();
        init(); //from DialogWrapper SHOULD BE LAST STATEMENT IN CONSTRUCTOR 
    }
    
    
    /**
     * Hook Method that is called prior to the {@link #initUI()} method to allow subclasses to initialize anything that 
     * needs to be initialized prior to the call that call. Default is an empty no op method. 
     */
    protected void preInitUI() { /* no op */ }
    
    /**
     * Hook Method that is called after the {@link #initUI()} method to allow subclasses to initialize any additional 
     * UI components or do other work that depends on the UI having been initialized first. This is called prior to the
     * {@link DialogWrapper#init()} method. Keep in mind that the {@code initUI} method calls the {@link #initOptionsPanel}
     * for the initialization that panel
     */
    protected void postInitUI() { /* no op */ }
    
    protected void initUI()
    {
        //TODO init to last used
        myAutoAppendComponentTypeCheckBox.setSelected(true);
        myAutoAppendComponentTypeCheckBox.setText(message("frc.new.class.adv.general.dialog.autoAppend.text", myDataProvider.getClassTypeSimpleName()));
        
        myIncludeJavaDocCheckBox.setSelected(true);
    
        FrcUiUtilsKt.addTextChangedListener(myComponentNameTextField, (documentEvent, text) -> {
            myAutoAppendComponentTypeCheckBox.setEnabled(nameCanBeAutoAppended(myComponentNameTextField.getText().trim()));
            return Unit.INSTANCE;
        });
    
        initSuperClassPanel();
        initOptionsPanel(myTopPanel, myOptionsPanel);
    }
    
    protected void initSuperClassPanel()
    {
        final String labelText = message("frc.new.class.adv.general.dialog.componentBase.label");
        final List<PsiClass> classes = getExtendableClasses();
        this.myTopLevelComponentClassesMap = FrcUiUtilsKt.initClassSelectionPanelRadioButtons(myTopPanel,
                                                                                              mySuperClassPanel,
                                                                                              labelText,
                                                                                              mySuperButtonGroup,
                                                                                              classes,
                                                                                              true);
    
        final Set<Entry<PsiClass, JBRadioButton>> entries = this.myTopLevelComponentClassesMap.entrySet();
        for (Entry<PsiClass, JBRadioButton> entry : entries)
        {
            // TODO: modify to use last selected value
            if (myDataProvider.getTypicalBaseClassFqName().equals(entry.getKey().getQualifiedName()))
            {
                entry.getValue().setSelected(true);
            }
        }
    }
    
    
    /**
     * Initializes the lower options panel for component specific UI options. 
     * @param topPanel the main parent panel. <strong>Implementations should avoid modifying this panel</strong> It is provided for determining used mneumonics
     * @param optionsPanel the options panel to be initialized.
     */
    protected void initOptionsPanel(JPanel topPanel, JPanel optionsPanel)
    {
        /* no op by default */
    }
    
    
    @Override
    protected void doOKAction()
    {
        LOG.trace("[FRC] doOKAction called");
        
        if (!getOKAction().isEnabled())
        {
            return;
        }
        
        final String newClassName = getNewClassName().trim();
        final StringBuilder pkgName = new StringBuilder();
        
        final PsiPackage pkg = JavaDirectoryService.getInstance().getPackageInSources(myDirectory);
        if (pkg != null)
        {
            pkgName.append(pkg.getQualifiedName());
            if (newClassName.contains("."))
            {
                String[] names = newClassName.split("\\.");
                for (int i = 0; i < names.length - 1; i++)
                {
                    pkgName.append(".").append(names[i]);
                }
            }
        }
        
        Map<String, String> additionalProperties = getAdditionalProperties(pkgName.toString(), newClassName);
        
        if (myClassCreator.createClass(newClassName,
                                       myDirectory,
                                       additionalProperties))
        {
            close(OK_EXIT_CODE);
        }
    }
    
    
    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent()
    {
        return getNewClassNameField();
    }
    
    
    @Override
    public String getTitle()
    {
        return message("frc.new.class.adv.general.dialog.title", myDataProvider.getClassTypeSimpleName());
    }
    
    /**
     * Implementations should return a map of additional properties to pass into the Velocity Template. If none,
     * return an empty Map.
     *
     * @param targetPackageName the fully qualified target Package of the new class. While never null, there is a
     *                          slight chance of it being an empty string. This value may not match the value
     *                          of the {@link #myDirectory} as that represents the directory the action was initiated
     *                          on. For example, if the action is activated on the `frc.robot.commands` package,
     *                          but the user enters a class name of `claw.OpenClaw`, this parameter will be
     *                          `frc.robot.commands.claw` since the user is asking for the `claw` subpackage
     *                          to be used.
     * @param newClassName      the name of the new component class
     *
     * @return a map of additional properties to pass into the Velocity Template
     */
    protected Map<String, String> getAdditionalProperties(@NotNull String targetPackageName, @NotNull String newClassName)
    {
        ImmutableMap.Builder<String, String> props = ImmutableMap.builder();
        addGeneralProperties(props);
        addBaseClassProperties(props, targetPackageName, newClassName);
        return props.build();
    }
    
    protected void addGeneralProperties(@NotNull ImmutableMap.Builder<String, String> props)
    {
        props.put(INCLUDE_JAVADOC_FOR_OVERRIDES, Boolean.toString(myIncludeJavaDocCheckBox.isSelected()));
    }
    
    
    protected void addBaseClassProperties(@NotNull Builder<String, String> props, @NotNull String targetPackageName, @NotNull String newClassName)
    {
        PsiClass base = null;
        for (Entry<PsiClass, JBRadioButton> entry : myTopLevelComponentClassesMap.entrySet())
        {
            if (entry.getValue().isSelected())
            {
                base = entry.getKey();
                break;
            }
        }
        if (base == null)
        {
            // should not happen, but just in case
            LOG.warn("[FRC] No selected Option found for the Base Class. Will attempt to default to the Typical Base Class.");
            final PsiClass[] classes = FrcClassUtilsKt.findClass(myProject, myDataProvider.getTypicalBaseClassFqName());
            if (classes.length > 0)
            {
                base = classes[0];
            }
        }
        
        if (base == null)
        {
            LOG.warn("[FRC] Could not find the Typical Base Class. Cannot configure base class properties for the template. Template creation will result in invalid class.");
            return;
        }
        
        final String baseFqName = base.getQualifiedName() != null ? base.getQualifiedName() : myDataProvider.getTypicalBaseClassFqName();
        props.put(BASE_CLASS_FQ_NAME, baseFqName);
        final boolean baseClassNeedsImporting = !baseFqName.contains(".") || !baseFqName.substring(0, baseFqName.lastIndexOf('.')).equals(targetPackageName);
        props.put(BASE_CLASS_NEEDS_IMPORTING, Boolean.toString(baseClassNeedsImporting));
        final String baseName = base.getName() != null ? base.getName() : myDataProvider.getTypicalBaseClassFqName().substring(myDataProvider.getTypicalBaseClassFqName().lastIndexOf('.') + 1);
        final String extendsClause = base.isInterface() ? "implements " + baseName : "extends " + baseName;
        props.put(BASE_CLASS_EXTENDS_CLAUSE, extendsClause);
        props.put(MAKE_ABSTRACT, Boolean.toString(newClassName.contains("Abstract")));
        // TODO it'd be nice to make this more sophisticated (to handle the event of a custom interface that has the getRequirements as a default
        props.put(NEEDS_GET_REQUIREMENTS, Boolean.toString(base.isInterface()));
    }
    
    
    protected abstract void addComponentSpecificProperties(@NotNull Builder<String, String> props, @NotNull String targetPackageName, @NotNull String newClassName);
    
    
    protected String getSubsystemTopClassFQN()
    {
        return WpiLibConstants.SUBSYSTEM_V2_TOP_FQN;
    }
    
    
    @NotNull
    protected JTextComponent getNewClassNameField() { return myComponentNameTextField; }
    
    
    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll()
    {
        final ImmutableList.Builder<ValidationInfo> results = ImmutableList.builder();
        
        boolean nameIsValid = isProposedClassNameValid();
        @Nullable
        String createClassErrorMessage = ClassCreator.checkCanCreateClass(myDirectory,
                                                                          getNewClassNameField().getText(),
                                                                          myDataProvider.getClassTypeSimpleName());
        
        if (!nameIsValid)
        {
            results.add(new ValidationInfo(message("frc.new.class.adv.validation.invalidName", myDataProvider.getClassTypeSimpleName()),
                                           getNewClassNameField()));
        }
        
        if (createClassErrorMessage != null)
        {
            results.add(new ValidationInfo(createClassErrorMessage, getNewClassNameField()));
        }
        
        results.addAll(doAdditionalValidation());
        // Everything is valid
        return results.build();
    }
    
    
    /**
     * A method subclasses can override to do additional validation specific t the class type being created.
     *
     * @return List<ValidationInfo> of invalid fields, or an empty list if no errors found.
     */
    protected List<ValidationInfo> doAdditionalValidation()
    {
        return Collections.emptyList();
    }
    
    
    protected boolean isProposedClassNameValid()
    {
        String text = getNewClassNameField().getText();
        return text.length() > 0 && PsiNameHelper.getInstance(myProject).isQualifiedName(text);
    }
    
    
    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        return myTopPanel;
    }
    
    
    /**
     * Returns the desired name for the new class. Implmenting classes can override if
     * they want to dynamically create the class name, such as by auto-appending a suffix like
     * 'Command' or 'Subsystem' to the name the user inputs. Overriding implementations should be
     * sure a trimmed value is returned. By default, this method will return the trimmed value
     * of the Class Name Field as returned by the {@link #getNewClassNameField()} method.
     *
     * @return the desired name for the new class
     */
    protected String getNewClassName()
    {
        final String name = getNewClassNameField().getText().trim();
        return myAutoAppendComponentTypeCheckBox.isEnabled() && myAutoAppendComponentTypeCheckBox.isSelected() && !isBaseClassName(name)
               ? name + myDataProvider.getClassTypeSimpleName() 
               : name;
    }
    
    
    protected List<PsiClass> getExtendableClasses()
    {
        final ImmutableList.Builder<PsiClass> classes = ImmutableList.builder();
        
        final String typicalBaseClassFqName = myDataProvider.getTypicalBaseClassFqName();
        final String topLevelClassFqName = myDataProvider.getTopLevelClassFqName();
        
        
        final PsiClass[] foundBaseClasses = FrcClassUtilsKt.findClass(myProject, typicalBaseClassFqName);
        classes.add(foundBaseClasses);
        
        final PsiClass[] topLevelClasses;
        
        if (!topLevelClassFqName.equals(typicalBaseClassFqName))
        {
            topLevelClasses = FrcClassUtilsKt.findClass(myProject, topLevelClassFqName);
            classes.add(topLevelClasses);
        }
        else
        {
            topLevelClasses = foundBaseClasses;
        }
        
        
        // We should only find the one interface, but the easiest way to handle is to iterate over them.
        for (PsiClass foundInterface : topLevelClasses)
        {
            List<PsiClass> projectImpls = findProjectBaseImpls(foundInterface);
            classes.addAll(projectImpls);
        }
        
        return classes.build();
    }
    
    
    /**
     * Returns an immutable List of all 'base' implementations of the provided top level class, This
     * includes all interfaces and abstract classes that implement or extend the provided top level
     * class, as well as any classes that have a "Base Class" name, as returned by
     * {@link #isBaseClass(PsiClass)}.
     *
     * @param topLevelClassFqName the top level interface or (abstract) class name to find an implementation for.
     */
    @NotNull
    protected List<PsiClass> findProjectBaseImpls(String topLevelClassFqName)
    {
        final List<PsiClass> implementations = FrcClassUtilsKt.findImplementationsInModule(topLevelClassFqName,
                                                                                           myModule,
                                                                                           false,
                                                                                           false,
                                                                                           null);
        return doFindProjectBaseImplsProcessing(implementations);
    }
    
    
    /**
     * Returns an immutable List of all 'base' implementations of the provided top level class, This
     * includes all interfaces and abstract classes that implement or extend the provided top level
     * class, as well as any classes that have a "Base Class" name, as returned by
     * {@link #isBaseClass(PsiClass)}.
     *
     * @param topLevelClass the top level interface or (abstract) class name to find an implementation for.
     */
    @NotNull
    protected List<PsiClass> findProjectBaseImpls(PsiClass topLevelClass)
    {
        final List<PsiClass> implementations = FrcClassUtilsKt.findImplementationsInModule(topLevelClass,
                                                                                           myModule,
                                                                                           false,
                                                                                           false,
                                                                                           null);
        return doFindProjectBaseImplsProcessing(implementations);
    }
    
    
    @NotNull
    protected List<PsiClass> doFindProjectBaseImplsProcessing(List<PsiClass> foundImplementations)
    {
        return foundImplementations.stream()
                                   .filter(this::isBaseClass)
                                   .sorted(PsiClassNameComparator.INSTANCE)
                                   .collect(Collectors.toList());
    }
    
    
    /**
     * Determines if a PsiClass is a 'Base Class', returning true if the PsiClass is an interface, is abstract, or
     * has a 'Base Name' ending such as 'CommandBase' or 'BaseCommand' in the case of a Command. Just having 'Base'
     * in the name however does not make it a 'Base Class'.
     */
    protected boolean isBaseClass(PsiClass psiClass)
    {
        @Nullable
        final String name = psiClass.getName();
        
        boolean isBaseClass = isBaseClassName(name);
        
        return (isBaseClass || FrcClassUtilsKt.isInterfaceOrAbstract(psiClass)) && FrcClassUtilsKt.isOpen(psiClass);
    }
    
    
    protected boolean isBaseClassName(@Nullable String name)
    {
        boolean isBaseClass = false;
        if (name != null)
        {
            final Set<String> baseSuffixes = myDataProvider.getClassTypeBaseNameSuffixes();
            isBaseClass = baseSuffixes.stream().anyMatch(name::endsWith);
        }
        return isBaseClass;
    }
    
    
    /**
     * Returns if the provided name if valid to have the component type auto appended to it.
     * For example 'OpenClaw' or 'DriveTrain' would return `true`, but 'OpenClawCommand' or
     * 'DriveTrainSubsystem' would return `false`.
     */
    protected boolean nameCanBeAutoAppended(String name)
    {
        boolean needsAutoAppending = true;
        if (name != null)
        {
            final Set<String> commonSuffixes = myDataProvider.getComponentTypeCommonSuffixes();
            needsAutoAppending = commonSuffixes.stream().noneMatch(name::endsWith);
        }
        return needsAutoAppending;
    }
}    

