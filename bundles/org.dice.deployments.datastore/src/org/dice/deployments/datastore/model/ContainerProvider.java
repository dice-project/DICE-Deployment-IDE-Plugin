package org.dice.deployments.datastore.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dice.deployments.client.model.Container;
import org.dice.deployments.client.model.Service;
import org.dice.deployments.datastore.Activator;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

public final class ContainerProvider implements IProvider {

  public final static ContainerProvider INSTANCE = new ContainerProvider();

  private final static long UPDATE_DELAY_MS = 2000;

  private WritableList<Container> containers;
  private Map<String, Container> lut;

  private Job updater;
  private ServiceProvider services;

  private ContainerProvider() {
    Display.getDefault().syncExec(() -> {
          containers = new WritableList<>();
        });
    lut = new HashMap<>();

    services = ServiceProvider.INSTANCE;

    updater = createUpdater(UPDATE_DELAY_MS);
    Activator.registerJob(updater);
    updater.schedule();
  }

  private Job createUpdater(long delay) {
    return Job.createSystem(new ICoreRunnable() {
      @Override
      public void run(IProgressMonitor monitor) throws CoreException {
        Collection<Service> ss = services.getThreadSafe();
        Map<String, Container> cs = fetchContainers(ss);
        Display.getDefault().syncExec(() -> updateInput(cs));
        updater.schedule(delay);
      }
    });
  }

  private Map<String, Container> fetchContainers(Collection<Service> ss) {
    Map<String, Container> cs = new HashMap<>();
    for (Service s : ss) {
      for (Container c : s.listContainers()) {
        cs.put(c.getId(), c);
      }
    }
    return cs;
  }

  private void updateInput(final Map<String, Container> cs) {
    // Next loop deletes containers that have been removed upstream and updates
    // existing ones with new data.
    for (Iterator<Container> iter = containers.iterator(); iter.hasNext();) {
      Container target = iter.next();
      Container source = cs.get(target.getId());
      if (source == null) {
        iter.remove();
        lut.remove(target.getId());
      } else {
        target.update(source);
      }
    }

    // Add newly created containers
    Collection<Container> added = cs.values();
    added.removeAll(containers); // This also modifies cs!!!!
    containers.addAll(added);
    lut.putAll(cs);
  }

  public void update() {
    updater.schedule();
    try {
      updater.join();
    } catch (InterruptedException e) {
      // TODO: Maybe we should retry the join here?
    }
  }

  public Container get(String id) {
    return lut.get(id);
  }

  @Override
  public WritableList<Container> get() {
    return containers;
  }

}
