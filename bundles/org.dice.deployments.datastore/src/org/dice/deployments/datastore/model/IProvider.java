package org.dice.deployments.datastore.model;

import org.eclipse.core.databinding.observable.list.WritableList;

public interface IProvider {

  public WritableList<?> get();

}
