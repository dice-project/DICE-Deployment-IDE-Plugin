package org.dice.deployments.ui.views;

import org.dice.deployments.client.model.Container;
import org.dice.deployments.datastore.model.ContainerProvider;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

public class ContainersView extends ViewPart {

  public static final String ID = "org.dice.deployments.ui.views.Containers";

  private TableViewer viewer;

  public ContainersView() {}

  @Override
  public void createPartControl(Composite parent) {
    createContainerList(parent);
    hookActions();
    getSite().setSelectionProvider(viewer);
  }

  private void createContainerList(Composite parent) {
    viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

    ObservableListContentProvider provider =
        new ObservableListContentProvider();
    @SuppressWarnings("unchecked")
    IObservableSet<Container> elements = provider.getKnownElements();

    createColumn(viewer, "Id", "id", elements);
    createColumn(viewer, "Description", "description", elements);
    createColumn(viewer, "State", "state", elements);

    viewer.setContentProvider(provider);
    viewer.setInput(ContainerProvider.INSTANCE.get());

    final Table table = viewer.getTable();
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .span(4, 1).applyTo(table);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
  }

  private void hookActions() {
    IToolBarManager manager =
        getViewSite().getActionBars().getToolBarManager();

    Action action = new Action() {
      public void run() {
        System.out.println("Action 1 executed");
      }
    };
    action.setText("Add item");
    manager.add(action);
  }

  private static TableViewerColumn createColumn(TableViewer viewer,
      String name, String attr, IObservableSet<Container> elements) {
    final TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);

    @SuppressWarnings("unchecked")
    final IObservableMap<Container, String> map =
        BeanProperties.value(Container.class, attr).observeDetail(elements);
    col.setLabelProvider(new ObservableMapCellLabelProvider(map));

    TableColumn tcol = col.getColumn();
    tcol.setText(name);
    tcol.setWidth(150);
    tcol.setResizable(true);

    return col;
  }

  @Override
  public void setFocus() {
    viewer.getControl().setFocus();
  }
}
