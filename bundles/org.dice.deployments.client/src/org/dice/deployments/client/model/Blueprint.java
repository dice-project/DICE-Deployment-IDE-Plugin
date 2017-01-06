package org.dice.deployments.client.model;

import com.google.gson.annotations.Expose;

public class Blueprint extends ModelObject {

  @Expose
  private String id;
  @Expose
  private String stateName;
  @Expose
  private Boolean inError;

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    fireChange("stateName", this.stateName, this.stateName = stateName);
  }

  public Boolean getInError() {
    return inError;
  }

  public void setInError(Boolean inError) {
    fireChange("inError", this.inError, this.inError = inError);
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return String.format("Blueprint[%s, %s, %b]", id, stateName, inError);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Blueprint && ((Blueprint) other).id.equals(id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public void update(Blueprint b) {
    if (!id.equals(b.id)) {
      return;
    }

    setStateName(b.stateName);
    setInError(b.inError);
  }

}
