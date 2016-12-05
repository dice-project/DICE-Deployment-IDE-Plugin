package org.dice.deployments.model;

import java.util.ArrayList;
import java.util.List;

import org.dice.deployments.Activator;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class ModelProvider {

  private List<DeploymentService> services;

  public ModelProvider() {
    services = new ArrayList<DeploymentService>();

    Preferences prefs =
        InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node("services");

    String serviceIds = prefs.get("list", "");
    if (serviceIds.equals(""))
      return;

    for (String serviceId : serviceIds.split(",")) {
      Preferences s = prefs.node(serviceId);
      services.add(new DeploymentService(serviceId, s.get("name", ""),
                                         s.get("address", ""),
                                         s.get("container", ""),
                                         s.get("username", ""),
                                         s.get("password", "")));
    }
  }

  public List<DeploymentService> getServices() {
    return services;
  }

  public void addService(DeploymentService service) {
    services.add(service);
  }

  public boolean save() {
    Preferences prefs =
        InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node("services");
    try {
      prefs.clear();
      List<String> serviceIds = new ArrayList<>();
      for (DeploymentService s : services) {
        Preferences service = prefs.node(s.getId());

        service.put("name", s.getName());
        service.put("address", s.getAddress());
        service.put("container", s.getContainer());
        service.put("username", s.getUsername());
        service.put("password", s.getPassword());
        serviceIds.add(s.getId());
      }
      prefs.put("list", String.join(",", serviceIds));
      prefs.flush();
    } catch (BackingStoreException e) {
      return false;
    }
    return true;
  }
}
