package org.dice.deployments.ui.launchers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.dice.deployments.client.exception.ClientError;
import org.dice.deployments.client.model.Container;
import org.dice.deployments.client.model.Service;
import org.dice.deployments.datastore.model.ServiceProvider;
import org.dice.deployments.ui.Utils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
    return true;
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

    Utils.createPathSelector(parent, blueprintText, FileDialog.class);
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

    Utils.createPathSelector(parent, resourcesText, DirectoryDialog.class);
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
        service = (Service) sel.getFirstElement();
        scheduleContainerUpdate();
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
            containerId = container == null ? null : container.getId();
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
    } else if (!validateServices()) {
      errMsg = "No deployment services available. "
          + "Please add at least one service in preferences.";
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

  private boolean validateServices() {
    return services.getThreadSafe().size() > 0;
  }

  private boolean validateContainer() {
    try {
      return container != null && service.listContainers().contains(container);
    } catch (ClientError e) {
      return false;
    }
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
    Set<Container> cs = new HashSet<>();
    try {
      if (service != null) {
        cs = service.listContainers();
      }
    } catch (ClientError e) {
      // TODO Log error to eclipse error console
    }

    // Apply default selection
    container = null;
    for (Container c : cs) {
      if (c.getId().equals(containerId)) {
        container = c;
        break;
      }
    }

    final Set<Container> finalCs = cs;
    Display.getDefault().syncExec(() -> {
      containerCombo.setInput(finalCs);
      if (container != null) {
        containerCombo.setSelection(new StructuredSelection(container));
      } else {
        containerCombo.setSelection(null);
      }
    });
  }

}
