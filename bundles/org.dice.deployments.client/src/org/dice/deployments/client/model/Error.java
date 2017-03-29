package org.dice.deployments.client.model;

import com.google.gson.annotations.Expose;

public class Error extends ModelObject {

  @Expose
  private String id;
  @Expose
  private String message;



  public String getId() {
    return id;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return String.format("Error[%s, %s]", id, message);
  }

}
