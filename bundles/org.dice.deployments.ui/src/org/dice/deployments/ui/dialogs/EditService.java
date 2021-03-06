package org.dice.deployments.ui.dialogs;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.dice.deployments.client.exception.ClientError;
import org.dice.deployments.client.model.Container;
import org.dice.deployments.client.model.Service;
import org.dice.deployments.ui.Utils;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.swt.WidgetSideEffects;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EditService extends TitleAreaDialog {

  private static final int MAX_LABEL_LENGTH = 25;

  private Service base;
  private Service update;

  private Job validator;
  private ComboViewer containers;
  private Button okButton;

  public EditService(Shell shell) {
    super(shell);

  }

  public EditService(Shell shell, Service s) {
    super(shell);

    base = s;
    update = s == null ? new Service() : new Service(s);
  }

  public Service getService() {
    return update;
  }

  /*
   * ***********************************************************************
   * TitleAreaDialog overrides
   * ***********************************************************************
   */
  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    setTitle("Add a new Deployment Service");
    setMessage("Please enter the data of the new service",
        IMessageProvider.INFORMATION);
    return contents;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite main = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    main.setLayout(layout);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(main);

    createInput(main, "Name", update.getName(), update::setName);
    createFileSelector(main, "Keystore file", update.getKeystoreFile(),
        update::setKeystoreFile);
    createInput(main, "Keystore password", update.getKeystorePass(),
        update::setKeystorePass, SWT.PASSWORD);
    createInput(main, "Address", update.getAddress(), update::setAddress);
    createInput(main, "Username", update.getUsername(), update::setUsername);
    createInput(main, "Password", update.getPassword(), update::setPassword,
        SWT.PASSWORD);
    createContainerSelector(main);

    return main;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    ((GridLayout) parent.getLayout()).numColumns += 2;

    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;

    okButton = new Button(parent, SWT.PUSH);
    okButton.setText("OK");
    okButton.setEnabled(false);
    okButton.setLayoutData(gridData);
    okButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (base != null) {
          base.update(update);
          update = base;
        }
        close();
      }
    });

    Button button = new Button(parent, SWT.PUSH);
    button.setText("Cancel");
    button.setLayoutData(gridData);
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        update = null;
        close();
      }
    });

    updateDialog();
  }

  /*
   * ***********************************************************************
   * GUI construction helpers
   * ***********************************************************************
   */
  private void createInput(Composite parent, String name, String init,
      Consumer<String> consumer, int style) {
    Label label = new Label(parent, SWT.NONE);
    label.setText(name);

    Text text = new Text(parent, SWT.BORDER | style);
    text.setText(init);
    GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(text);

    ISWTObservableValue val = WidgetProperties.text(SWT.Modify).observe(text);
    ISideEffectFactory factory = WidgetSideEffects.createFactory(text);
    factory.create(() -> (String) val.getValue(), data -> {
      consumer.accept(data);
      updateDialog();
    });
  }

  private void createInput(Composite parent, String name, String init,
      Consumer<String> consumer) {
    createInput(parent, name, init, consumer, 0);
  }

  private void createFileSelector(Composite parent, String name, String init,
      Consumer<String> consumer) {
    Label label = new Label(parent, SWT.NONE);
    label.setText(name);

    Text text = new Text(parent, SWT.BORDER);
    text.setText(init);
    GridDataFactory.fillDefaults().grab(true, false).applyTo(text);

    ISWTObservableValue val = WidgetProperties.text(SWT.Modify).observe(text);
    ISideEffectFactory factory = WidgetSideEffects.createFactory(text);
    factory.create(() -> (String) val.getValue(), data -> {
      consumer.accept(data);
      updateDialog();
    });

    Utils.createPathSelector(parent, text, FileDialog.class);
  }

  private void createContainerSelector(Composite parent) {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Default container:");

    containers = new ComboViewer(parent);
    containers.setContentProvider(ArrayContentProvider.getInstance());
    containers.setInput(null);
    containers.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        Container c = (Container) element;
        String desc = Utils.shortenTo(c.getDescription(), MAX_LABEL_LENGTH);
        return String.format("%s (%s)", desc, c.getId());
      }
    });
    containers.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        Container c = (Container) sel.getFirstElement();
        update.setContainer(c == null ? "" : c.getId());
      }
    });
    GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
        .applyTo(containers.getCombo());
  }

  private void updateDialog() {
    if (okButton == null) {
      // Dialog is not ready yet
      return;
    }

    setMessage("Waiting for validation");
    okButton.setEnabled(false);
    scheduleUpdate(500);
  }

  /*
   * ***********************************************************************
   * Background workers
   * ***********************************************************************
   */
  private void scheduleUpdate(long delay) {
    if (validator == null) {
      validator = Job.createSystem(new ICoreRunnable() {
        @Override
        public void run(IProgressMonitor monitor) throws CoreException {
          if (validateService()) {
            updateContainerList();
          }
        }
      });
    }

    validator.cancel();
    validator.schedule(delay);
  }

  private boolean validateService() {
    final String message = update.validate();
    Display.getDefault().syncExec(() -> {
      setErrorMessage(message);
    });
    return message == null;
  }

  private void updateContainerList() {
    Set<Container> cs;
    String err = null;

    try {
      cs = update.listContainers();
    } catch (ClientError e) {
      err = e.getMessage();
      cs = new HashSet<>();
    }

    Container iter = cs.size() > 0 ? cs.iterator().next() : null;
    for (Container c : cs) {
      if (c.getId().equals(update.getContainer())) {
        iter = c;
        break;
      }
    }

    err = err == null ? "Configured service has no containers." : err;
    updateContainerCombo(iter, cs, err);
  }

  private void updateContainerCombo(Container item,
      Set<Container> containerSet, String message) {
    StructuredSelection sel =
        item == null ? null : new StructuredSelection(item);
    Display.getDefault().syncExec(() -> {
      containers.setInput(containerSet);
      containers.setSelection(sel);
      if (sel != null) {
        okButton.setEnabled(true);
        setMessage("All ok.");
      } else {
        setErrorMessage(message);
      }
    });
  }

}
