package org.dice.deployments.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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

  public static <T> Button createPathSelector(Composite parent, Text target,
      Class<T> dialogClass) {
    return createPathSelector(parent, target, dialogClass, null);
  }

  public static <T> Button createPathSelector(Composite parent, Text target,
      Class<T> dialogClass, String[] filter) {
    /*
     * This method is not the most beautiful piece of code ever written, but it
     * should get the work done without duplicating the code in each "browse"
     * button that we would like to create.
     */
    Button button = new Button(parent, SWT.NONE);
    button.setText("Browse");
    button.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        IPath path = Utils.getActiveWorkspace().getLocation();
        IProject project = Utils.getActiveProject();
        if (project != null) {
          path = project.getLocation();
        }

        try {
          Constructor<T> con = dialogClass.getConstructor(Shell.class);
          T dialog = con.newInstance(parent.getShell());
          Method m = dialogClass.getMethod("setFilterPath", String.class);
          m.invoke(dialog, path.toOSString());
          m = dialogClass.getMethod("setFilterExtensions", String[].class);
          m.invoke(dialog, (Object) filter);
          m = dialogClass.getMethod("open");
          String value = (String) m.invoke(dialog);
          if (value != null) {
            target.setText(value);
          }
        } catch (Exception e) {
          // This should not happen and indicates bug in plugin
          e.printStackTrace();
        }
      }
    });

    return button;
  }

}
