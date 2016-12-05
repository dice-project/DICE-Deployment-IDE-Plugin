package org.dice.deployments.preferences.pages;

import org.dice.deployments.Activator;
import org.dice.deployments.dialogs.EditServiceDialog;
import org.dice.deployments.model.DeploymentService;
import org.dice.deployments.model.ModelProvider;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class PreferencesPage extends PreferencePage
    implements IWorkbenchPreferencePage {

  public static final String DESCRIPTION = "Available Deployment Services";

  private TableViewer viewer;
  private WritableList<DeploymentService> input;
  private ModelProvider model;

  @Override
  public void init(IWorkbench workbench) {
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
  }

  @Override
  protected Control createContents(Composite parent) {
    model = new ModelProvider();

    GridLayout myLayout = new GridLayout();
    myLayout.numColumns = 4;
    parent.setLayout(myLayout);

    Label myLabel = new Label(parent, SWT.LEAD);
    myLabel.setText(DESCRIPTION);
    GridData gridData = new GridData();
    gridData.horizontalSpan = 4;
    myLabel.setLayoutData(gridData);

    int style = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
    viewer = new TableViewer(parent, style);
    createColumn(viewer, "name");
    createColumn(viewer, "address");
    
    input = new WritableList<>(model.getServices(), DeploymentService.class);
    ViewerSupport.bind(viewer, input,
                       BeanProperties.values(new String[] { "name",
                                                            "address" }));

    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.verticalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalSpan = 4;
    viewer.getControl().setLayoutData(gridData);

    final Table table = viewer.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    Button add = new Button(parent, SWT.CENTER);
    add.setText("Add");
    add.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        DeploymentService service = showEditDialog(null);
        if (service != null) {
          input.add(service);
        }
      }
    });

    Button edit = new Button(parent, SWT.CENTER);
    edit.setText("Edit");
    edit.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (viewer.getSelection().isEmpty())
          return; // TODO: We should disable button instead of doing this
        
        IStructuredSelection selection = viewer.getStructuredSelection();
        DeploymentService service =
            (DeploymentService)selection.getFirstElement();
        int pos = input.indexOf(service); // In order not to shuffle order
        
        service = showEditDialog(service);
        if (service != null) {
          input.set(pos, service);
        }
      }
    });

    Button delete = new Button(parent, SWT.CENTER);
    delete.setText("Delete");

    noDefaultAndApplyButton();
    return new Composite(parent, SWT.NONE);
  }
  
  private DeploymentService showEditDialog(DeploymentService service) {
    Shell shell =
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    EditServiceDialog dialog = new EditServiceDialog(shell, service);
    dialog.open();
    return dialog.getService();
  }

  private static TableViewerColumn createColumn(TableViewer viewer,
                                                String name) {
    final TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
    TableColumn tcol = col.getColumn();
    tcol.setText(name);
    tcol.setWidth(100);
    tcol.setResizable(true);
    return col;
  }

  @Override
  public boolean performOk() {
    if (!model.save())
      return false;
    return super.performOk();
  }
  
}
