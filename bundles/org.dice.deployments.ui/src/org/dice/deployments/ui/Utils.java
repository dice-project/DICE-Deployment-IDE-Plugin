package org.dice.deployments.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class Utils {

  public static IWorkspaceRoot getActiveWorkspace() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

  public static IProject getActiveProject() {
    IWorkbenchWindow window =
        PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    if (window == null)
      return null;

    ISelection selection = window.getSelectionService().getSelection();
    if (selection instanceof IStructuredSelection)
      return getProjectFromSelection((IStructuredSelection) selection);

    return getProjectFromEditor(window.getActivePage().getActiveEditor());
  }

  public static IResource getResourceFromPath(IPath path) {
    return getActiveWorkspace().getFileForLocation(path);
  }

  public static IResource getResourceFromPath(String path) {
    return getActiveWorkspace().getFileForLocation(new Path(path));
  }

  private static IProject getProjectFromEditor(IEditorPart editor) {
    IEditorInput input = editor.getEditorInput();
    if (input instanceof IFileEditorInput) {
      return ((IFileEditorInput) input).getFile().getProject();
    }
    return null;
  }

  private static IProject getProjectFromSelection(
      IStructuredSelection selection) {
    Object item = selection.getFirstElement();
    if (item instanceof IResource) {
      return ((IResource) item).getProject();
    }
    return null;
  }

  public static String shortenTo(String string, int lenght) {
    if (string.length() > lenght) {
      string = string.substring(0, lenght - 4) + " ...";
    }
    return string;
  }
}
