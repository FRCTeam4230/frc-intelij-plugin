/*
 * Copyright 2015 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.ui;

import java.awt.*;
import java.nio.charset.Charset;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.compiler.server.BuildManager;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import net.javaru.iip.frc.udp.RioLogMonitoringProcess;



//Based on the IntelliJ IDEA com.intellij.execution.RunContentExecutor class
public abstract class AbstractRioLogContentExecutor implements Disposable
{
    private static final Logger LOG = Logger.getInstance(AbstractRioLogContentExecutor.class);
    protected final Project myProject;

    private JComponent consolePanel;
    private ActionToolbar actionToolbar;
    
    protected ProcessHandler myProcessHandler;
    private ConsoleView myConsoleView;
    private Runnable myRerunRunnable;
    private Runnable myStopRunnable;
    @Nullable
    private Runnable myAfterCompletionRunnable;
    private Computable<Boolean> myStopEnabled;
    private String myTitle = "roboRIO";
    private String myHelpId = null;
    private boolean myActivateToolWindow = true;
    private Executor myExecutor;
    private RunContentDescriptor myRunContentDescriptor;

    
    /*
        The com.intellij.execution.ExecutionHelper may be useful
    */

    protected AbstractRioLogContentExecutor(@NotNull Project project, boolean activateToolWindow, @Nullable Runnable afterCompletionRunnable)
    {
        myProject = project;
        myActivateToolWindow = activateToolWindow;
        this.myAfterCompletionRunnable = afterCompletionRunnable;
    }


    private ConsoleView createConsole(@NotNull Project project, @NotNull ProcessHandler processHandler)
    {
        TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        ConsoleView console = consoleBuilder.getConsole();
        console.attachToProcess(processHandler);
        return console;
    }

    private JComponent createConsolePanel(ConsoleView view, ActionGroup actions)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(view.getComponent(), BorderLayout.CENTER);
        actionToolbar = createToolbar(actions);
        panel.add(actionToolbar.getComponent(), BorderLayout.WEST);
        return panel;
    }


    private static ActionToolbar createToolbar(ActionGroup actions)
    {
        return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, false);
    }


    public void run()
    {
        FileDocumentManager.getInstance().saveAllDocuments();

        final RioLogMonitoringProcess process = new RioLogMonitoringProcess(this::invokeClearAll);
        
        myProcessHandler = new BaseOSProcessHandler(process, null, Charset.defaultCharset())
        {
            @Override
            public boolean isSilentlyDestroyOnClose()
            {
                return true;
            }
        };

        myProcessHandler.putUserDataIfAbsent(BuildManager.ALLOW_AUTOMAKE, true);


        myConsoleView = createConsole(myProject, myProcessHandler);
        
        
        
        myStopRunnable = myProcessHandler::destroyProcess;
        myStopEnabled = () -> !myProcessHandler.isProcessTerminated();
        myRerunRunnable = () ->
        {
            myProcessHandler.destroyProcess();
            invokeClearAll();
            myProcessHandler.waitFor(2000L);
            run();
        };
        

        if (myHelpId != null)
        {
            myConsoleView.setHelpId(myHelpId);
        }

        //Executor executor = DefaultRunExecutor.getRunExecutorInstance(); //Gets the Run Window I believe
        myExecutor = createExecutor();
        DefaultActionGroup actions = new DefaultActionGroup();

        consolePanel = createConsolePanel(myConsoleView, actions);
        myRunContentDescriptor = new RunContentDescriptor(myConsoleView, myProcessHandler, consolePanel, myTitle, AllIcons.General.MessageHistory);

        Disposer.register(myProject, this);
        Disposer.register(this, myRunContentDescriptor);
        addActionsToActionGroup(actions);


        ExecutionManager.getInstance(myProject).getContentManager().showRunContent(myExecutor, myRunContentDescriptor);

        if (myActivateToolWindow)
        {
            activateRioLogConsole();
        }

        if (myAfterCompletionRunnable != null)
        {
            myProcessHandler.addProcessListener(new ProcessAdapter()
            {
                @Override
                public void processTerminated(ProcessEvent event)
                {
                    SwingUtilities.invokeLater(myAfterCompletionRunnable);
                }
            });
        }

        process.start();
        myProcessHandler.startNotify();
    }


    private void addActionsToActionGroup(DefaultActionGroup actions)
    {
        // We need to grab some actions that are created in the ConsoleView itself.
        // This is a bit hackish but works. 

        AnAction softWrapAction = null, grepConsoleAction = null;
        for (AnAction action : myConsoleView.createConsoleActions())
        {
            final Class<? extends AnAction> actionClass = action.getClass();

            if (LOG.isDebugEnabled())
            {
                LOG.debug(String.format("[FRC] Adding console Action: Text='%s'; Desc='%s'; class='%s'; enclosingClass='%s'; declaringClass='%s'",
                                       action.getTemplatePresentation().getText(),
                                       action.getTemplatePresentation().getDescription(),
                                       actionClass,
                                       actionClass.getEnclosingClass(),
                                       actionClass.getDeclaringClass()));
            }
            if (actionClass.getName().equals("krasa.grepconsole.action.OpenConsoleSettingsAction"))
            {
                grepConsoleAction = action;
            }
            else if (action instanceof ToggleUseSoftWrapsToolbarAction) 
            {
                softWrapAction = action;
            }
        }

        // See com.intellij.execution.impl.ConsoleViewImpl.createConsoleActions for example 
        actions.add(new RioLogRerunAction(consolePanel));
        actions.add(new RioLogStopAction());
        actions.add(new RioLogPauseOutputAction(myConsoleView, myProcessHandler));
        actions.add(new Separator());

        if (grepConsoleAction != null)
        {
            actions.add(grepConsoleAction);
        }

        if (myConsoleView instanceof OccurenceNavigator)
        {
            final CommonActionsManager commonActionsManager = CommonActionsManager.getInstance();
            OccurenceNavigator occurenceNavigator = (OccurenceNavigator) myConsoleView;
            final AnAction prevAction = commonActionsManager.createPrevOccurenceAction(occurenceNavigator);
            prevAction.getTemplatePresentation().setText(occurenceNavigator.getPreviousOccurenceActionName());
            actions.add(prevAction);            
            final AnAction nextAction = commonActionsManager.createNextOccurenceAction(occurenceNavigator);
            nextAction.getTemplatePresentation().setText(occurenceNavigator.getNextOccurenceActionName());
            actions.add(nextAction);
        }

        if (softWrapAction != null)
        {
            actions.add(softWrapAction);
        }

        actions.add(new RioLogClearAllAction());
        actions.add(new Separator());
        // We no longer provide a close button. As long as a FRC Facet is present, we want a RioLog console. 
        // The 'work' of the close action was moved to myCloseRunnable and closing is managed by the RioLogConsoleProjectService
        // Leaving this line of code here commented out in case in the future we need to remember how we did include a close button.
        //actions.add(new com.intellij.execution.ui.actions.CloseAction(myExecutor, descriptor, myProject));
    }


    protected abstract Executor createExecutor();


    public void activateRioLogConsole()
    {
        ApplicationManager.getApplication().invokeLater(() -> 
        {
            final ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(getToolWindowId());
            toolWindow.activate(null);
            final ContentManager contentManager = toolWindow.getContentManager();
            final Content content = contentManager.findContent(getTitle());
            if (content != null)
            {
                contentManager.setSelectedContent(content, true);
            }
        });
    }

    public String getTitle()
    {
        return myTitle;
    }

    public void invokeClearAll()
    {
        myConsoleView.clear();
    }

    protected abstract String getToolWindowId();

    public void close()
    {
        LOG.debug("[FRC] Executing " + getClass().getSimpleName() + ".close()");
        if (myRunContentDescriptor != null)
        {
            final boolean removedOk = ExecutionManager.getInstance(myProject).getContentManager().removeRunContent(myExecutor, myRunContentDescriptor);
            if (removedOk)
            {
                LOG.debug("RioLogContentExecutor was removed OK");
                myRunContentDescriptor = null;
                myExecutor = null;
                myRunContentDescriptor = null;
            }
            else 
            {
                LOG.debug("RioLogContentExecutor was NOT removed");    
            }
        }
    }

    @Override
    public void dispose()
    {
        Disposer.dispose(this);
        LOG.debug("[FRC] Disposing of " + getClass().getSimpleName() + " complete.");
    }


    //Taken from com.intellij.execution.configurations.CommandLineState - need to modify to use in this class
    protected static class RioLogPauseOutputAction extends ToggleAction implements DumbAware
    {
        private final ConsoleView myConsole;
        private final ProcessHandler myProcessHandler;


        public RioLogPauseOutputAction(final ConsoleView console, final ProcessHandler processHandler)
        {
            super(ExecutionBundle.message("run.configuration.pause.output.action.name"), "Pauses the output which will be buffered and then displayed when the output is un-paused. Note that scrolling up will pause scrolling.",
                  AllIcons.Actions.Pause);
            myConsole = console;
            myProcessHandler = processHandler;
        }


        @Override
        public boolean isSelected(final AnActionEvent event)
        {
            return myConsole.isOutputPaused();
        }


        @Override
        public void setSelected(final AnActionEvent event, final boolean flag)
        {
            myConsole.setOutputPaused(flag);
            ApplicationManager.getApplication().invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    update(event);
                }
            });
        }


        @Override
        public void update(@NotNull final AnActionEvent event)
        {
            super.update(event);
            final Presentation presentation = event.getPresentation();
            final boolean isRunning = myProcessHandler != null && !myProcessHandler.isProcessTerminated();
            if (isRunning)
            {
                presentation.setEnabled(true);
            }
            else
            {
                if (!myConsole.canPause())
                {
                    presentation.setEnabled(false);
                    return;
                }
                if (!myConsole.hasDeferredOutput())
                {
                    presentation.setEnabled(false);
                }
                else
                {
                    presentation.setEnabled(true);
                    myConsole.performWhenNoDeferredOutput(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            update(event);
                        }
                    });
                }
            }
        }
    }


    private class RioLogStopAction extends DumbAwareAction
    {
        public RioLogStopAction()
        {
            super(ExecutionBundle.message("run.configuration.stop.action.name"), "Stops monitoring of the roboRIO log output.", AllIcons.Actions.Suspend);
        }


        @Override
        public void actionPerformed(AnActionEvent e)
        {
            myStopRunnable.run();
        }


        @Override
        public void update(AnActionEvent e)
        {
            e.getPresentation().setVisible(myStopRunnable != null);
            e.getPresentation().setEnabled(myStopEnabled != null && myStopEnabled.compute());
        }
    }

    private class RioLogRerunAction extends DumbAwareAction
    {
        public RioLogRerunAction(JComponent consolePanel)
        {
            super("Restart", "Clears the console and restarts the roboRIO Log monitoring",
                  AllIcons.Actions.Restart);
            registerCustomShortcutSet(CommonShortcuts.getRerun(), consolePanel);
        }


        @Override
        public void actionPerformed(AnActionEvent e)
        {
            myRerunRunnable.run();
        }


        @Override
        public void update(AnActionEvent e)
        {
            e.getPresentation().setVisible(myRerunRunnable != null);
        }
    }
    
    private class RioLogClearAllAction extends DumbAwareAction
    {
        public RioLogClearAllAction()
        {
            super("Clear All", "Clears the content of the roboRIO Log console.", AllIcons.Actions.GC);
        }


        @Override
        public void actionPerformed(AnActionEvent anActionEvent)
        {
            myConsoleView.clear();
        }


        @Override
        public void update(AnActionEvent e)
        {
            boolean enabled = myConsoleView.getContentSize() > 0;
            if (!enabled)
            {
                enabled = e.getData(LangDataKeys.CONSOLE_VIEW) != null;
                Editor editor = e.getData(CommonDataKeys.EDITOR);
                if (editor != null && editor.getDocument().getTextLength() == 0)
                {
                    enabled = false;
                }
            }
            e.getPresentation().setEnabled(enabled);
        }
    }
}
