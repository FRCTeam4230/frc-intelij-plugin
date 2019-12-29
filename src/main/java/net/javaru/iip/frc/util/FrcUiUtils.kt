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
@file:Suppress("HtmlRequiredLangAttribute")

package net.javaru.iip.frc.util

import com.google.common.collect.ImmutableMap
import com.intellij.icons.AllIcons
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.ex.WindowManagerEx
import com.intellij.psi.PsiClass
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.ui.IconAndLabelButton
import org.intellij.lang.annotations.Language
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.util.*
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.ButtonModel
import javax.swing.DefaultButtonModel
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.JTextField
import javax.swing.ScrollPaneConstants
import javax.swing.colorchooser.AbstractColorChooserPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

private object FrcUiUtils

private val LOG = Logger.getInstance(FrcUiUtils::class.java)

@JvmOverloads
fun displayExceptionDialog(t: Throwable, project: Project?, title: String = "An error has occurred")
{
    @Language("HTML")
    val message = message("frc.generic.exception.message", t)
    Messages.showErrorDialog(project, message, title)
}

@JvmOverloads
fun displayExceptionDialog(t: Throwable, component: Component, title: String = "An error has occurred")
{
    @Language("HTML")
    val message = message("frc.generic.exception.message", t)
    Messages.showErrorDialog(component, message, title)
}

/**
 * Returns the provided text inside HTML tags centering the text for use on a Swing label.
 * For example, given the text 'My Message', this will return:
 * <pre>
 * "&lt;html&gt;&lt;div style='text-align: center;'&gt;" + text + "&lt;/div&gt;&lt;/html&gt;"
</pre> *
 * @param text the text to wrap
 * @return the provided text inside HTML tags centering the text
 */
@Language("HTML")
fun centerLabelText(text: String): String
{
    return "<html><div style='text-align: center;'>$text</div></html>"
}

@Language("HTML")
fun boldLabelText(text: String): String
{
    return "<html><div style='font-weight: bold;'>$text</div></html>"
}


/**
 * Sets the text of the supplied field to the supplied value iff the text field is blank.
 * In other words, it fills in blank fields with a default value.
 *
 * @param text  the (default) text to use if the field is empty
 */
fun JTextField.setTextIfEmpty(text: String?)
{
    if (StringUtil.isEmpty(this.text))
    {
        this.text = StringUtil.notNullize(text)
    }
}

/**
 * Adds a `DocumentListener` to the components; document that will call the supplied action anytime the text is changed.
 */
fun JTextComponent.addTextChangedListener(action: (text: String) -> Unit)
{
    document.addDocumentListener(object : DocumentListener
                                 {
                                     override fun changedUpdate(e: DocumentEvent?) = fire()
                                     override fun insertUpdate(e: DocumentEvent?) = fire()
                                     override fun removeUpdate(e: DocumentEvent?) = fire()
                                     fun fire() = action.invoke(text)
                                 })
}

/**
 * Adds a `DocumentListener` to the components; document that will call the supplied action anytime the text is changed.
 */
fun JTextComponent.addTextChangedListener(action: (e: DocumentEvent?, text: String) -> Unit)
{
    document.addDocumentListener(object : DocumentListener
                                 {
                                     override fun changedUpdate(e: DocumentEvent?) = fire(e)
                                     override fun insertUpdate(e: DocumentEvent?) = fire(e)
                                     override fun removeUpdate(e: DocumentEvent?) = fire(e)
                                     fun fire(e: DocumentEvent?) = action.invoke(e, text)
                                 })
}

/**
 * Finds the `IdeFrame` for the supplied project (which may be null when no project is
 * opened). In the event that cannot be found, it attempts to find the most recently
 * focused component for the project is found. In the event that cannot be found, it
 * attempts to find the most recently focused window. In the event that cannot be
 * found, `null` is returned. Primarily meant for use when needing a parent component
 * for use when opening a dialog window.
 *
 * @receiver project – may be null when no project is opened.
 */
fun Project?.findIdeFrameOrAlternateParentComponent(): Component?
{
    val windowManager = WindowManagerEx.getInstanceEx()

    var parentComponent: Component? = null
    val ideFrame = windowManager.findFrameFor(this)
    if (ideFrame != null)
    {
        parentComponent = ideFrame.component
    }
    if (parentComponent == null)
    {
        parentComponent = windowManager.getFocusedComponent(this)
    }

    if (parentComponent == null)
    {
        parentComponent = windowManager.mostRecentFocusedWindow
    }

    return parentComponent
}

/**
 * Recursively finds all used/assigned  Mnemonics for a component, returning them as a Set.
 */
@JvmOverloads
fun findAllUsedMnemonics(component: Component?, convertAllToUpperCase: Boolean = true): MutableSet<Char>
{
    val mnemonics: MutableSet<Char> = HashSet()
    findAllUsedMnemonics(component, mnemonics, convertAllToUpperCase)
    return mnemonics
}

/**
 * Recursively finds all used/assigned  Mnemonics for a component, adding them to the supplied set.
 *
 * @param convertAllToUpperCase if *all* values in the set (including any present when passed in) should be converted to uppercase
 */
@JvmOverloads
fun findAllUsedMnemonics(component: Component?, mnemonics: MutableSet<Char>, convertAllToUpperCase: Boolean = true)
{
    try
    {
        if (component == null) return
        if (component is AbstractButton) mnemonics.add(component.mnemonic.toChar())
        if (component is ButtonModel) mnemonics.add(component.mnemonic.toChar())
        if (component is AbstractColorChooserPanel) mnemonics.add(component.mnemonic.toChar())
        if (component is DefaultButtonModel) mnemonics.add(component.mnemonic.toChar())
        if (component is JLabel) mnemonics.add(component.displayedMnemonic.toChar())
        if (component is JTabbedPane)
        {
            for (i in 0..component.tabCount)
            {
                mnemonics.add(component.getMnemonicAt(i).toChar())
            }
        }

        if (component is Container)
        {
            for (childComponent in component.components)
            {
                findAllUsedMnemonics(childComponent, mnemonics)
            }
        }

        if (convertAllToUpperCase)
        {
            val upperCase = mnemonics.map { it.toUpperCase() }
            mnemonics.clear()
            mnemonics.addAll(upperCase)
        }
    }
    catch (e: Exception)
    {
        LOG.info("[FRC] An exception occurred when finding all used Mnemonics: $e", e)
    }
}


fun <T> calculateMnemonics(topComponent: JComponent?, elements: Collection<T>, getNameFunction: (T) -> String?): Map<T, Char> = calculateMnemonics(elements, findAllUsedMnemonics(topComponent), getNameFunction)

fun <T> calculateMnemonics(elements: Collection<T>, unavailableMnemonics: Set<Char>, getNameFunction: (T) -> String?): MutableMap<T, Char>
{
    val usedMnemonics = unavailableMnemonics.map { it.toUpperCase() }.toMutableSet()

    val mnemonicsMap: MutableMap<T, Char> = HashMap()

    try
    {

        // First pass, see if first letter is available
        elements.forEach { element ->
            val name = getNameFunction.invoke(element)
            val first = name?.first()
            if (first != null && !usedMnemonics.contains(first.toUpperCase()))
            {
                usedMnemonics.add(first.toUpperCase())
                mnemonicsMap[element] = first
            }
        }

        if (mnemonicsMap.size != elements.size)
        {
            // Second pass, see if a camel case letter if available
            elements.forEach { element ->
                if (!mnemonicsMap.containsKey(element))
                {
                    val name = getNameFunction.invoke(element)
                    if (name != null && name.length >= 2)
                    {
                        val chars = name.toCharArray()
                        // first look for camel casing
                        camel@ for (char in chars)
                        {
                            if (char.isUpperCase() && !usedMnemonics.contains(char.toUpperCase()))
                            {
                                usedMnemonics.add(char.toUpperCase())
                                mnemonicsMap[element] = char
                                break@camel
                            }
                        }
                    }
                }
            }
        }

        if (mnemonicsMap.size != elements.size)
        {
            // Third pass, just find an available letter, but we look for one in the shortest names 
            elements.filter { !mnemonicsMap.containsKey(it) && getNameFunction(it)?.length ?: 0 >= 2 }.sortedByDescending { getNameFunction(it)?.length ?: 0 }
                .forEach {
                    val name = getNameFunction.invoke(it)
                    if (name != null && name.length >= 2)
                    {
                        val chars = name.toCharArray()
                        // first look for camel casing
                        forChars@ for (char in chars)
                        {
                            if (!usedMnemonics.contains(char.toUpperCase()))
                            {
                                usedMnemonics.add(char.toUpperCase())
                                mnemonicsMap[it] = char
                                break@forChars
                            }
                        }
                    }
                }
        }
    }
    catch (e: Exception)
    {
        LOG.info("[FRC] An exception occurred when calculating Mnemonics: $e", e)
    }
    return mnemonicsMap
}

@JvmOverloads
fun initClassSelectionPanelCheckBoxes(topComponent: JComponent,
                                      panel: JPanel,
                                      labelText: String,
                                      classes: List<PsiClass>,
                                      includeIcons: Boolean,
                                      maxItemCols: Int = 4,
                                      maxItemRows: Int = 6,
                                      textCreator: (PsiClass) -> String? = { psiClass -> psiClass.name })
        : Map<PsiClass, JBCheckBox>
{
    return initClassSelectionPanel(topComponent,
                                                                              panel,
                                                                              labelText,
                                                                              classes,
                                                                              includeIcons,
                                                                              maxItemCols,
                                                                              maxItemRows,
                                                                              ::JBCheckBox,
                                                                              null,
                                                                              textCreator
                                                                             )

}

@JvmOverloads
fun initClassSelectionPanelRadioButtons(topComponent: JComponent,
                                        panel: JPanel,
                                        labelText: String,
                                        buttonGroup: ButtonGroup,
                                        classes: List<PsiClass>,
                                        includeIcons: Boolean,
                                        maxItemCols: Int = 4,
                                        maxItemRows: Int = 6,
                                        textCreator: (PsiClass) -> String? = { psiClass -> psiClass.name })
        : Map<PsiClass, JBRadioButton>
{
    return initClassSelectionPanel(topComponent,
                                                                              panel,
                                                                              labelText,
                                                                              classes,
                                                                              includeIcons,
                                                                              maxItemCols,
                                                                              maxItemRows,
                                                                              ::JBRadioButton,
                                                                              buttonGroup,
                                                                              textCreator
                                                                             )
}

@JvmOverloads
fun <T : AbstractButton> initClassSelectionPanel(topComponent: JComponent,
                                                 panel: JPanel,
                                                 labelText: String,
                                                 classes: List<PsiClass>,
                                                 includeIcons: Boolean,
                                                 maxItemCols: Int,
                                                 maxItemRows: Int,
                                                 buttonConstructor: () -> T,
                                                 buttonGroup: ButtonGroup?,
                                                 textCreator: (PsiClass) -> String? = { psiClass -> psiClass.name })
        : Map<PsiClass, T>
{
    // TODO add a context Help icon -- use com.intellij.ui.ContextHelpLabel, which displays an AllIcons.General.ContextHelp icon -- next 
    //      to the label to explain how items are selected have an optional/nullable FrcMessageKey passed in for the message to 
    //      displayed when it's hovered over.
    val mapBuilder = ImmutableMap.builder<PsiClass, T>()

    if (classes.isNotEmpty())
    {
        val classesCount = classes.size

        var colCount = classesCount / maxItemRows
        if (classesCount % maxItemRows != 0) colCount++
        if (colCount > maxItemCols)
        {
            colCount = maxItemCols
        }

        var rowCount = classesCount / colCount
        if (classesCount % colCount != 0) rowCount++
        
        // if rowCount > maxItemRows, we want to wrap in a scroll pane 
        val thePanel = if (rowCount > maxItemRows)
        {
            val innerPanel = JPanel(GridLayoutManager(rowCount, colCount))
            val scrollPane = JBScrollPane(innerPanel)
            scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            val height = maxItemRows * 30 // a good approximation for now
            panel.add(scrollPane, GridConstraints(0, 0, 1, 1,
                                                  GridConstraints.ANCHOR_WEST,
                                                  GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_CAN_SHRINK,
                                                  GridConstraints.SIZEPOLICY_FIXED,
                                                  Dimension(-1, 100),
                                                  Dimension(-1, height),
                                                  Dimension(-1, height),
                                                  0))
            innerPanel
        }
        else
        {
            panel
        }
        
        
        //Add a row for the label
        rowCount++

        val manager = GridLayoutManager(rowCount, colCount)
        thePanel.layout = manager
        val gc = GridConstraints(0, 0, 1, 1,
                                 GridConstraints.ANCHOR_WEST,
                                 GridConstraints.FILL_NONE,
                                 GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_CAN_SHRINK,
                                 GridConstraints.SIZEPOLICY_FIXED,
                                 Dimension(-1, -1),
                                 Dimension(-1, -1),
                                 Dimension(-1, -1),
                                 1)
        gc.isUseParentLayout = false
        gc.colSpan = colCount
        val label = JBLabel(labelText)
        thePanel.add(label, gc)
        gc.colSpan = 1
        gc.indent = 3
        val mnemonics = calculateMnemonics(topComponent, classes) { obj: NavigationItem -> obj.name }
        
        var row = 0
        for (psiClass in classes)
        {
            val text = textCreator.invoke(psiClass)
            if (text != null)
            {
                row++
                if (row >= rowCount)
                {
                    row = 1
                    gc.column = gc.column + 1
                }
                gc.row = row

                val optionButton: T = buttonConstructor.invoke()
                
                optionButton.actionCommand = psiClass.qualifiedName
                optionButton.toolTipText = psiClass.qualifiedName
                buttonGroup?.add(optionButton)
                val mnemonic = mnemonics[psiClass]
                
                
                if (includeIcons)
                {
                    // Although psiClass.getIcon would probably never return null, we default to the emptyNode Icon just in case.
                    val icon =  psiClass.getIcon(0) ?: AllIcons.Nodes.EmptyNode // for flags, we could use: getIcon(Iconable.ICON_FLAG_VISIBILITY or Iconable.ICON_FLAG_READ_STATUS)
                    val xButton = IconAndLabelButton(optionButton, icon, text)
                    xButton.toolTipText = psiClass.qualifiedName
                    if (mnemonic != null)
                    {
                        xButton.setMnemonic(mnemonic)
                    }
                    thePanel.add(xButton, gc)
                }
                else
                {
                    thePanel.add(optionButton, gc)
                    optionButton.text = text
                    
                    if (mnemonic != null)
                    {
                        optionButton.setMnemonic(mnemonic)
                    }
                }
                mapBuilder.put(psiClass, optionButton)
            }
        }
    }
    return mapBuilder.build()
}