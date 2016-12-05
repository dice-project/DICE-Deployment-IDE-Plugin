package org.dice.deployments.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ModelObject {

  private final PropertyChangeSupport change = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    change.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    change.removePropertyChangeListener(listener);
  }

  protected void firePropertyChange(String propertyName, Object oldValue,
                                    Object newValue) {
    change.firePropertyChange(propertyName, oldValue, newValue);
  }

}
