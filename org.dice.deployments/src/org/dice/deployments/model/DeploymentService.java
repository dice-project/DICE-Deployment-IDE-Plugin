package org.dice.deployments.model;

import java.util.UUID;

public class DeploymentService extends ModelObject {
  private String id;
  private String name;

  private String address;
  private String container;
  private String username;
  private String password;

  public DeploymentService(String id, String name, String address,
                           String container, String username,
                           String password) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.container = container;
    this.username = username;
    this.password = password;
  }

  public DeploymentService(String name, String address, String container,
                           String username, String password) {
    this(UUID.randomUUID().toString(), name, address, container, username,
         password);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    firePropertyChange("name", this.name, this.name = name);
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    firePropertyChange("address", this.address, this.address = address);
  }

  public String getContainer() {
    return container;
  }

  public void setContainer(String container) {
    firePropertyChange("container", this.container,
                       this.container = container);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    firePropertyChange("username", this.username, this.username = username);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    firePropertyChange("password", this.password, this.password = password);
  }

  @Override
  public String toString() {
    return String.format("DS(%s, %s)", this.name, this.address);
  }
}
