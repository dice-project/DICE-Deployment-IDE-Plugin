package org.dice.deployments.client.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dice.deployments.client.http.Client;
import org.dice.deployments.client.http.Result;

public class Service extends ModelObject {

  private String id;
  private String name;
  private String address;
  private String container;
  private String username;
  private String password;

  private Client client;

  public Service(String id, String name, String address, String container,
      String username, String password) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.container = container;
    this.username = username;
    this.password = password;
  }

  public Service() {
    this(UUID.randomUUID().toString(), "", "", "", "", "");
  }

  public Service(Service s) {
    this(s.id, s.name, s.address, s.container, s.username, s.password);
  }

  // Getters and setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    fireChange("name", this.name, this.name = name);
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    closeClient();
    fireChange("address", this.address, this.address = address);
  }

  public String getContainer() {
    return container;
  }

  public void setContainer(String container) {
    fireChange("container", this.container, this.container = container);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    closeClient();
    fireChange("username", this.username, this.username = username);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    closeClient();
    fireChange("password", this.password, this.password = password);
  }

  public String getId() {
    return id;
  }

  // Aux methods
  public void update(Service s) {
    if (!s.id.equals(id)) {
      return; // TODO: Silently skipping update is not OK. Add log message here
              // (but first we need to learn how to log stuff in eclipse).
    }

    setName(s.name);
    setAddress(s.address);
    setContainer(s.container);
    setUsername(s.username);
    setPassword(s.password);
  }

  private void closeClient() {
    if (client != null) {
      client.close();
      client = null;
    }
  }

  public String validate() {
    if (client != null) {
      client.close();
    }

    String msg = null;

    try {
      validateName();
      URI uri = validateURI();
      client = new Client(uri, 2000);
      validateService(client);
      validateAuth(client);
    } catch (RuntimeException e) {
      msg = e.getMessage();
    } catch (IOException e) {
      msg = "Cannot connect to service. Check connectivity.";
    } finally {
      if (client != null && msg != null) {
        client.close();
        client = null;
      }
    }

    return msg;
  }

  private void validateName() {
    if (name.equals("")) {
      throw new RuntimeException("Invalid service name.");
    }
  }

  private URI validateURI() {
    if (address.length() == 0) {
      throw new RuntimeException("Invalid service URL.");
    }

    URI uri;
    try {
      uri = new URI(address);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Invalid service URL.");
    }
    return uri;
  }

  private void validateService(Client c) throws IOException {
    if (!c.getHeartbeat()) {
      throw new RuntimeException("Service is not responding. Check address.");
    }
  }

  private void validateAuth(Client c) throws IOException {
    if (!c.authenticate(username, password)) {
      throw new RuntimeException("Invalid username and/or password.");
    }
  }

  public Set<Container> listContainers() {
    // TODO: This is quite bad, but should suffice for now. In the future, it
    // would be best not to allow service to exist if validation fails, but
    // this would have a nasty side effect that service cannot be added or
    // loaded if something went wrong with connection.
    Set<Container> result = new HashSet<>();

    if (ensureClient()) {
      try {
        Result<Container[], Message> r = client.listContainers();
        if (r.ok) {
          result.addAll(Arrays.asList(r.first));
        }
        // TODO: Add user warning or log message about failed fetch
      } catch (IOException e) {
        // Default return value handles this for now.
      }
    }
    return result;
  }

  private boolean ensureClient() {
    if (client == null) {
      validate();
    }
    return client != null;
  }

  public boolean emptyContainer(Container c) {
    if (c.getBlueprint() != null) {
      if (!ensureClient()) {
        // TODO: log this error
        return false;
      }

      try {
        client.undeployBlueprint(c.getId());
      } catch (IOException e) {
        // TODO: log this error
        return false;
      }
    }

    return true;
  }

  public Blueprint deployBlueprint(Container c, File archive) {
    if (!ensureClient()) {
      // TODO: log this error
      return null;
    }

    try {
      Result<Blueprint, Message> res =
          client.deployBlueprint(c.getId(), archive);
      if (res.ok) {
        return res.first;
      }
    } catch (IOException e) {
      // TODO: log this error
    }

    return null;
  }

  @Override
  public String toString() {
    return String.format("DS(%s, %s)", this.name, this.address);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Service && ((Service) other).id.equals(id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

}
