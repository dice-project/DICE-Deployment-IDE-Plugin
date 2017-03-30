package org.dice.deployments.client.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dice.deployments.client.exception.ClientError;
import org.dice.deployments.client.http.Client;
import org.dice.deployments.client.http.Result;

public class Service extends ModelObject {

  private String id;
  private String name;
  private String address;
  private String container;
  private String username;
  private String password;
  private String keystoreFile;
  private String keystorePass;

  private Client client;

  public Service(String id, String name, String address, String container,
      String username, String password, String keystoreFile,
      String keystorePass) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.container = container;
    this.username = username;
    this.password = password;
    this.keystoreFile = keystoreFile;
    this.keystorePass = keystorePass;
  }

  public Service() {
    this(UUID.randomUUID().toString(), "", "", "", "", "", "", "");
  }

  public Service(Service s) {
    this(s.id, s.name, s.address, s.container, s.username, s.password,
        s.keystoreFile, s.keystorePass);
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

  public String getKeystoreFile() {
    return keystoreFile;
  }

  public void setKeystoreFile(String keystoreFile) {
    closeClient();
    fireChange("keystoreFile", this.keystoreFile,
        this.keystoreFile = keystoreFile);
  }

  public String getKeystorePass() {
    return keystorePass;
  }

  public void setKeystorePass(String keystorePass) {
    closeClient();
    fireChange("keystorePass", this.keystorePass,
        this.keystorePass = keystorePass);
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
    setKeystoreFile(s.keystoreFile);
    setKeystorePass(s.keystorePass);
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
      client = new Client(uri, 2000, keystoreFile, keystorePass);
      validateService(client);
      validateAuth(client);
    } catch (RuntimeException e) {
      msg = e.getMessage();
    } catch (ClientError e) {
      msg = e.getMessage();
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

  private void validateService(Client c) {
    try {
      if (c.getHeartbeat()) {
        return;
      }
    } catch (ClientError e) {
    }
    throw new RuntimeException("Service is not responding. Check address.");
  }

  private void validateAuth(Client c) {
    try {
      if (c.authenticate(username, password)) {
        return;
      }
    } catch (ClientError e) {
    }
    throw new RuntimeException("Invalid username and/or password.");
  }

  public Set<Container> listContainers() throws ClientError {
    // TODO: This is quite bad, but should suffice for now. In the future, it
    // would be best not to allow service to exist if validation fails, but
    // this would have a nasty side effect that service cannot be added or
    // loaded if something went wrong with connection.
    Set<Container> result = new HashSet<>();

    ensureClient();
    Result<Container[], Message> r = client.listContainers();
    if (r.ok) {
      result.addAll(Arrays.asList(r.first));
    }
    return result;
  }

  private void ensureClient() throws ClientError {
    if (client != null) {
      return;
    }

    String msg = validate();
    if (msg != null) {
      throw new ClientError(msg);
    }
  }

  public Result<Blueprint, Message> emptyContainer(Container c)
      throws ClientError {
    if (c.getBlueprint() == null) {
      return null;
    }
    ensureClient();
    return client.undeployBlueprint(c.getId());
  }

  public Result<Blueprint, Message> deployBlueprint(Container c, File archive)
      throws ClientError {
    ensureClient();
    return client.deployBlueprint(c.getId(), archive);
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
