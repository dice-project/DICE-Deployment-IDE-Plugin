package org.dice.deployments.client.model;

import com.google.gson.annotations.Expose;

public class Container extends ModelObject {

  private static final int SLEEP_INTERVAL_MS = 1000;

  @Expose
  private String id;
  @Expose
  private String description;
  @Expose
  private Boolean busy;
  @Expose
  private Blueprint blueprint;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    fireChange("description", this.description,
        this.description = description);
  }

  public Boolean getBusy() {
    return busy;
  }

  public void setBusy(Boolean busy) {
    fireChange("busy", this.busy, this.busy = busy);
  }

  public Blueprint getBlueprint() {
    return blueprint;
  }

  public void setBlueprint(Blueprint blueprint) {
    fireChange("blueprint", this.blueprint, this.blueprint = blueprint);
    fireChange("state", null, getState());
  }

  public String getId() {
    return id;
  }

  public String getState() {
    return blueprint == null ? "empty" : blueprint.getStateName();
  }

  public void update(Container c) {
    if (!c.id.equals(id)) {
      return;
    }

    setDescription(c.description);
    setBusy(c.busy);

    // Updating blueprint is a bit tricky, because it can be replaced.
    if (blueprint != null && blueprint.equals(c.blueprint)) {
      blueprint.update(c.blueprint);
    } else {
      setBlueprint(c.blueprint);
    }
    fireChange("state", null, getState());
  }

  @Override
  public String toString() {
    return String.format("Container[%s, %b, %s]", id, busy, blueprint);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Container && ((Container) other).id.equals(id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public boolean waitWhileBusy() {
    while (busy) {
      try {
        Thread.sleep(SLEEP_INTERVAL_MS);
      } catch (InterruptedException e) {
      }
    }

    return blueprint == null || !blueprint.getInError();
  }

}
