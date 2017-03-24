package org.dice.deployments.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenRunConfig extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
    String configTypeId =
        "org.dice.deployments.ui.launchDeployConfigurationType";
    ILaunchConfigurationType configType =
        manager.getLaunchConfigurationType(configTypeId);

    IWorkbenchWindow window =
        HandlerUtil.getActiveWorkbenchWindowChecked(event);
    DebugUITools.openLaunchConfigurationDialogOnGroup(window.getShell(),
        new StructuredSelection(configType),
        IDebugUIConstants.ID_RUN_LAUNCH_GROUP, null);
    return null;
  }

}
