package org.dice.deployments.datastore;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

  public static final String PLUGIN_ID = "org.dice.deployments.datastore";

  private static Activator INSTANCE;

  private ISecurePreferences prefs;

  static Activator getDefault() {
    return INSTANCE;
  }

  public static ISecurePreferences getPrefs() {
    return INSTANCE.prefs;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    Activator.INSTANCE = this;
    IPath prefsLocation = getStateLocation().append("preferences");
    prefs = SecurePreferencesFactory
        .open(new URL("file://" + prefsLocation.toString()), null);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    Activator.INSTANCE = null;
  }

}
