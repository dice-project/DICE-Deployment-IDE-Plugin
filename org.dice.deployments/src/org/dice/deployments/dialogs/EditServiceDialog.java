package org.dice.deployments.dialogs;

import org.dice.deployments.model.DeploymentService;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EditServiceDialog extends TitleAreaDialog {

  private Text name;
  private Text address;
  private DeploymentService service;
  private Text container;
  private Text username;
  private Text password;

  public EditServiceDialog(Shell shell, DeploymentService service) {
    super(shell);
    this.service = service;
  }

  public DeploymentService getService() {
    return service;
  }

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
    Label label;

    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    parent.setLayout(layout);

    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;

    label = new Label(parent, SWT.NONE);
    label.setText("Name");
    name = new Text(parent, SWT.BORDER);
    name.setLayoutData(gridData);

    label = new Label(parent, SWT.NONE);
    label.setText("Address");
    address = new Text(parent, SWT.BORDER);
    address.setLayoutData(gridData);

    label = new Label(parent, SWT.NONE);
    label.setText("Default container uuid");
    container = new Text(parent, SWT.BORDER);
    container.setLayoutData(gridData);

    label = new Label(parent, SWT.NONE);
    label.setText("Username");
    username = new Text(parent, SWT.BORDER);
    username.setLayoutData(gridData);

    label = new Label(parent, SWT.NONE);
    label.setText("Password");
    password = new Text(parent, SWT.NONE);
    password.setLayoutData(gridData);

    if (service != null) {
      name.setText(service.getName());
      address.setText(service.getAddress());
      container.setText(service.getContainer());
      username.setText(service.getUsername());
      password.setText(service.getPassword());
      service = null;
    }

    return parent;
  }

  private boolean isValid() {
    return !(name.getText().isEmpty() || address.getText().isEmpty()
             || container.getText().isEmpty() || username.getText().isEmpty()
             || password.getText().isEmpty());
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    ((GridLayout)parent.getLayout()).numColumns += 2;

    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;

    Button button = new Button(parent, SWT.PUSH);
    button.setText("OK");
    button.setLayoutData(gridData);
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (isValid()) {
          service =
              new DeploymentService(name.getText(), address.getText(),
                                    container.getText(), username.getText(),
                                    password.getText());
          close();
        } else {
          setErrorMessage("Please enter all data");
        }
      }
    });

    button = new Button(parent, SWT.PUSH);
    button.setText("Cancel");
    button.setLayoutData(gridData);
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        close();
      }
    });
  }
}
