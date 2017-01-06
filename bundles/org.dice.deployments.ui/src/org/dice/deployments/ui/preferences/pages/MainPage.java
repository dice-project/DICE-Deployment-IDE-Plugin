package org.dice.deployments.ui.preferences.pages;

import org.dice.deployments.client.model.Service;
import org.dice.deployments.datastore.model.ServiceProvider;
import org.dice.deployments.ui.dialogs.EditService;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MainPage extends PreferencePage
    implements IWorkbenchPreferencePage {

  public static final String DESCRIPTION = "Available Deployment Services";

  private TableViewer viewer;
  private ServiceProvider services;

  @Override
  public void init(IWorkbench workbench) {}

  @Override
  protected Control createContents(Composite parent) {
    services = ServiceProvider.INSTANCE;

    Group group = new Group(parent, SWT.BORDER);
    group.setText(DESCRIPTION);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
    GridLayoutFactory.swtDefaults().numColumns(4).applyTo(group);

    createServiceList(group);
    createAddButton(group);
    createEditButton(group);
    createDeleteButton(group);

    noDefaultAndApplyButton();
    return group;
  }

  private void createDeleteButton(Composite parent) {
    Button delete = new Button(parent, SWT.CENTER);
    delete.setText("Delete");
    delete.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (viewer.getSelection().isEmpty())
          return; // TODO: We should disable button instead of doing this

        services.remove(
            (Service) viewer.getStructuredSelection().getFirstElement());
      }
    });
  }

  private void createEditButton(Composite parent) {
    Button edit = new Button(parent, SWT.CENTER);
    edit.setText("Edit");
    edit.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (viewer.getSelection().isEmpty())
          return; // TODO: We should disable button instead of doing this

        IStructuredSelection selection = viewer.getStructuredSelection();
        Service service = (Service) selection.getFirstElement();
        showEditDialog(service);
      }
    });
  }

  private void createAddButton(Composite parent) {
    Button add = new Button(parent, SWT.CENTER);
    add.setText("Add");
    add.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        Service service = showEditDialog(null);
        if (service != null) {
          services.add(service);
        }
      }
    });
  }

  private void createServiceList(Composite parent) {
    viewer = new TableViewer(parent,
        SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

    ObservableListContentProvider provider =
        new ObservableListContentProvider();
    @SuppressWarnings("unchecked")
    IObservableSet<Service> elements = provider.getKnownElements();

    createColumn(viewer, "Service name", "name", elements);
    createColumn(viewer, "Service address", "address", elements);

    viewer.setContentProvider(provider);
    viewer.setInput(services.get());

    final Table table = viewer.getTable();
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .span(4, 1).applyTo(table);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
  }

  private Service showEditDialog(Service service) {
    Shell shell = getControl().getShell();
    EditService dialog = new EditService(shell, service);
    dialog.open();
    return dialog.getService();
  }

  private static TableViewerColumn createColumn(TableViewer viewer,
      String name, String attr, IObservableSet<Service> elements) {
    final TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);

    @SuppressWarnings("unchecked")
    final IObservableMap<Service, String> map =
        BeanProperties.value(Service.class, attr).observeDetail(elements);
    col.setLabelProvider(new ObservableMapCellLabelProvider(map));

    TableColumn tcol = col.getColumn();
    tcol.setText(name);
    tcol.setWidth(150);
    tcol.setResizable(true);

    return col;
  }

  @Override
  public boolean performOk() {
    if (!services.saveServices())
      return false;
    return super.performOk();
  }

}
