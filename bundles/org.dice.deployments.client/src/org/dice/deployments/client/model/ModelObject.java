package org.dice.deployments.client.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ModelObject {
  private final PropertyChangeSupport change = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    change.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propName,
      PropertyChangeListener listener) {
    change.addPropertyChangeListener(propName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    change.removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(String propName,
      PropertyChangeListener listener) {
    change.removePropertyChangeListener(propName, listener);
  }

  protected void fireChange(String name, Object oldValue, Object newValue) {
    if (oldValue == newValue)
      return;

    if (oldValue == null || !oldValue.equals(newValue)) {
      change.firePropertyChange(name, oldValue, newValue);
    }
  }

}
