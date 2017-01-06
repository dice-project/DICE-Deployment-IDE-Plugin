package org.dice.deployments.ui.launchers;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import org.dice.deployments.client.model.Container;
import org.dice.deployments.client.model.Service;
import org.dice.deployments.datastore.model.ServiceProvider;
import org.dice.deployments.ui.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LaunchDeployMainTab implements ILaunchConfigurationTab {

  private static final int MAX_LABEL_LENGTH = 25;

  private Composite control;
  private ILaunchConfigurationDialog confDialog;
  private String errMsg;
  private Job validator;
  private Job updater;

  private ServiceProvider services;

  private String blueprint;
  private Text blueprintText;

  private String resources;
  private Text resourcesText;

  private Service service;
  private ComboViewer serviceCombo;

  private Container container;
  private ComboViewer containerCombo;
  private String containerId;

  public LaunchDeployMainTab() {
    services = ServiceProvider.INSTANCE;
  }

  /*
   * ***********************************************************************
   * ILaunchConfigurationTab methods
   * ***********************************************************************
   */
  @Override
  public void createControl(Composite parent) {
    control = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().applyTo(control);

    createBlueprintGroup(control);
    createServiceGroup(control);
  }

  @Override
  public Control getControl() {
    return control;
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy c) {
    // TODO: Auto-guess blueprint file if one is opened in editor/selected.
    c.setAttribute(LaunchDeployConfig.BLUEPRINT_PATH, "");
    // TODO: Auto-fill with project's resources folder if present.
    c.setAttribute(LaunchDeployConfig.RESOURCES_PATH, "");
    Service s = services.getDefaultService();
    if (s != null) {
      c.setAttribute(LaunchDeployConfig.CONTAINER_ID, s.getContainer());
      c.setAttribute(LaunchDeployConfig.SERVICE_ID, s.getId());
    }
  }

  @Override
  public void initializeFrom(ILaunchConfiguration c) {
    try {
      blueprintText
          .setText(c.getAttribute(LaunchDeployConfig.BLUEPRINT_PATH, ""));
      resourcesText
          .setText(c.getAttribute(LaunchDeployConfig.RESOURCES_PATH, ""));
      Service s =
          services.get(c.getAttribute(LaunchDeployConfig.SERVICE_ID, ""));
      if (s != null) {
        serviceCombo.setSelection(new StructuredSelection(s));
      }
      // Container combo will be updated as part of container refresh process
      containerId = c.getAttribute(LaunchDeployConfig.CONTAINER_ID, "");
    } catch (CoreException e) {
      // Initialization failed, but we cannot do much about it.
      e.printStackTrace(System.err);
    }
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy c) {
    c.setAttribute(LaunchDeployConfig.BLUEPRINT_PATH, blueprint);
    c.setAttribute(LaunchDeployConfig.RESOURCES_PATH, resources);
    c.setAttribute(LaunchDeployConfig.CONTAINER_ID,
        container == null ? null : container.getId());
    c.setAttribute(LaunchDeployConfig.SERVICE_ID,
        service == null ? null : service.getId());
  }

  @Override
  public String getErrorMessage() {
    return errMsg;
  }

  @Override
  public String getMessage() {
    return "Fill in the details about deploy";
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    return errMsg == null;
  }

  @Override
  public boolean canSave() {
    return errMsg == null;
  }

  @Override
  public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
    confDialog = dialog;
  }

  @Override
  public String getName() {
    return "Deployment details";
  }

  @Override
  public Image getImage() {
    // TODO: Check how to return image here
    return null;
  }

  @Override
  @Deprecated
  public void launched(ILaunch launch) {}

  @Override
  public void dispose() {}

  @Override
  public void activated(ILaunchConfigurationWorkingCopy workingCopy) {}

  @Override
  public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {}

  /*
   * ***********************************************************************
   * GUI construction helpers
   * ***********************************************************************
   */
  private void createBlueprintGroup(Composite control) {
    Group group = new Group(control, SWT.BORDER);
    group.setText("Blueprint");
    GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
    group.setLayout(new GridLayout(3, false));

    createBlueprintSelector(group);
    createResourceSelector(group);
  }

  private void createBlueprintSelector(Composite parent) {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Main blueprint file:");

    blueprintText = new Text(parent, SWT.NONE);
    blueprintText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        blueprint = blueprintText.getText();
        scheduleValidation(200);
      }
    });
    GridDataFactory.fillDefaults().grab(true, false).applyTo(blueprintText);

    createPathSelector(parent, blueprintText, FileDialog.class);
  }

  private void createResourceSelector(Composite parent) {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Resources folder:");

    resourcesText = new Text(parent, SWT.NONE);
    resourcesText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        resources = resourcesText.getText();
        scheduleValidation(200);
      }
    });
    GridDataFactory.fillDefaults().grab(true, false).applyTo(resourcesText);

    createPathSelector(parent, resourcesText, DirectoryDialog.class);
  }

  private <T> Button createPathSelector(Composite parent, Text target,
      Class<T> dialogClass) {
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
          T dialog = con.newInstance(control.getShell());
          Method m = dialogClass.getMethod("setFilterPath", String.class);
          m.invoke(dialog, path.toOSString());
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

  private void createServiceGroup(Composite control) {
    Group group = new Group(control, SWT.BORDER);
    group.setText("Deployment Service");
    group.setLayout(new GridLayout(2, false));
    GridDataFactory.fillDefaults().grab(true, false).applyTo(group);

    createServiceSelector(group);
    createContainerSelector(group);
  }

  private void createServiceSelector(Composite parent) {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Deployment Service:");

    serviceCombo = new ComboViewer(parent);
    serviceCombo.setContentProvider(ArrayContentProvider.getInstance());
    serviceCombo.setInput(services.get());
    serviceCombo.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        Service s = (Service) element;
        return String.format("%s (%s)", s.getName(), s.getAddress());
      }
    });
    serviceCombo.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        Service s = (Service) sel.getFirstElement();
        if (service == null || service.getId() != s.getId()) {
          service = s;
          containerId = s.getContainer();
          scheduleContainerUpdate();
        }
      }
    });
    GridDataFactory.fillDefaults().grab(true, false)
        .applyTo(serviceCombo.getCombo());
  }

  private void createContainerSelector(Composite parent) {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Container:");

    containerCombo = new ComboViewer(parent);
    containerCombo.setContentProvider(ArrayContentProvider.getInstance());
    containerCombo.setInput(null);
    containerCombo.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        Container c = (Container) element;
        String desc = Utils.shortenTo(c.getDescription(), MAX_LABEL_LENGTH);
        return String.format("%s (%s)", desc, c.getId());
      }
    });
    containerCombo
        .addSelectionChangedListener(new ISelectionChangedListener() {
          @Override
          public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection sel =
                (IStructuredSelection) event.getSelection();
            container = (Container) sel.getFirstElement();
            scheduleValidation(200);
          }
        });
    GridDataFactory.fillDefaults().grab(true, false)
        .applyTo(containerCombo.getCombo());
  }

  /*
   * ***********************************************************************
   * Background workers
   * ***********************************************************************
   */
  private void scheduleValidation(long delay) {
    if (validator == null) {
      validator = Job.createSystem(new ICoreRunnable() {
        @Override
        public void run(IProgressMonitor monitor) throws CoreException {
          validate();
        }
      });
    }

    validator.cancel();
    validator.schedule(delay);
  }

  private void validate() {
    errMsg = null;

    if (!validateBlueprintPath()) {
      errMsg = "Invalid blueprint path.";
    } else if (!validateResourcesPath()) {
      errMsg = "Invalid resources path.";
    } else if (!validateContainer()) {
      errMsg = "Missing container";
    }

    Display.getDefault().syncExec(() -> {
      if (confDialog != null) {
        confDialog.updateButtons();
        confDialog.updateMessage();
      }
    });
  }

  private boolean validateBlueprintPath() {
    File file = new File(blueprint);
    return file.canRead();
  }

  private boolean validateResourcesPath() {
    File file = new File(resources);
    return file.isDirectory() && file.canRead();
  }

  private boolean validateContainer() {
    return container != null && service.listContainers().contains(container);
  }

  private void scheduleContainerUpdate() {
    containerCombo.setInput(null);

    if (updater == null) {
      updater = Job.createSystem(new ICoreRunnable() {
        @Override
        public void run(IProgressMonitor monitor) throws CoreException {
          updateContainerList();
        }
      });
    }

    updater.cancel();
    updater.schedule();
  }

  private void updateContainerList() {
    Set<Container> cs = service.listContainers();

    // Apply default selection
    container = null;
    for (Container c : cs) {
      if (c.getId().equals(containerId)) {
        container = c;
        break;
      }
    }

    Display.getDefault().syncExec(() -> {
      containerCombo.setInput(cs);
      if (container != null) {
        containerCombo.setSelection(new StructuredSelection(container));
      } else {
        containerCombo.setSelection(null);
      }
    });
  }

}
