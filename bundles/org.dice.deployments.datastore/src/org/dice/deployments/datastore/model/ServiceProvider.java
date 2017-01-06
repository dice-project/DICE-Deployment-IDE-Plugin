package org.dice.deployments.datastore.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dice.deployments.client.model.Service;
import org.dice.deployments.datastore.Activator;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;

public class ServiceProvider implements IProvider {

  public final static ServiceProvider INSTANCE = new ServiceProvider();

  private WritableList<Service> services;
  private Map<String, Service> lut;

  private ServiceProvider() {
    services = new WritableList<>();
    lut = new HashMap<>();

    ISecurePreferences prefs = Activator.getPrefs().node("services");
    try {
      String serviceIds = prefs.get("list", "");
      if (!serviceIds.equals("")) {
        for (String serviceId : serviceIds.split(",")) {
          Service s = serviceFromPreferences(serviceId, prefs);
          services.add(s);
          lut.put(serviceId, s);
        }
      }
    } catch (StorageException e) {
      // TODO: What would be the sensible thing to do here?
      e.printStackTrace();
    }
  }

  public static Service serviceFromPreferences(String id,
      ISecurePreferences parent) throws StorageException {
    ISecurePreferences node = parent.node(id);
    return new Service(id, node.get("name", ""), node.get("address", ""),
        node.get("container", ""), node.get("username", ""),
        node.get("password", ""));
  }

  public void add(Service s) {
    lut.put(s.getId(), s);
    services.add(s);
  }

  public void remove(Service s) {
    lut.remove(s);
    services.remove(s);
  }

  public WritableList<Service> get() {
    return services;
  }

  public Collection<Service> getThreadSafe() {
    return lut.values();
  }

  public Service get(String id) {
    return lut.get(id);
  }

  public Service getDefaultService() {
    return services.get(0);
  }

  public boolean saveServices() {
    ISecurePreferences prefs = Activator.getPrefs().node("services");
    try {
      List<String> ids = new ArrayList<>(services.size());
      prefs.clear();
      for (Service s : services) {
        ISecurePreferences service = prefs.node(s.getId());
        ids.add(s.getId());
        service.put("name", s.getName(), false);
        service.put("address", s.getAddress(), false);
        service.put("container", s.getContainer(), false);
        service.put("username", s.getUsername(), true);
        service.put("password", s.getPassword(), true);
      }
      prefs.put("list", String.join(",", ids), false);
      prefs.flush();
    } catch (StorageException | IOException e) {
      return false;
    }
    return true;
  }

}
