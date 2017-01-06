package org.dice.deployments.datastore.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.dice.deployments.client.model.Blueprint;
import org.dice.deployments.client.model.ModelObject;

import com.google.gson.annotations.Expose;

public class Deployment extends ModelObject {

  @Expose
  private final ZonedDateTime created;

  @Expose
  private String id;
  @Expose
  private String stateName;
  @Expose
  private Boolean inError;

  // TODO: Add SCM revision number to creation

  public Deployment() {
    created = ZonedDateTime.now(ZoneOffset.UTC);
  }

  public Deployment(Blueprint blueprint) {
    this();

    update(blueprint);
  }

  public void update(Blueprint blueprint) {
    id = blueprint.getId();
    stateName = blueprint.getStateName();
    inError = blueprint.getInError();
  }

  @Override
  public String toString() {
    return String.format("Deloyment[%s, %s]", id, stateName);
  }

  public ZonedDateTime getCreated() {
    return created;
  }

  public String getId() {
    return id;
  }

  public String getStateName() {
    return stateName;
  }

  public Boolean getInError() {
    return inError;
  }

}
