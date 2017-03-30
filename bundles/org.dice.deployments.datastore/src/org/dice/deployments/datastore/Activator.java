package org.dice.deployments.datastore;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

  public static final String PLUGIN_ID = "org.dice.deployments.datastore";

  private static Activator INSTANCE;

  private ISecurePreferences prefs;
  private List<Job> jobs;

  static Activator getDefault() {
    return INSTANCE;
  }

  public static ISecurePreferences getPrefs() {
    return INSTANCE.prefs;
  }

  public static void log(int status, String message) {
    log(status, message, null);
  }

  public static void log(int status, String message, Exception exception) {
    INSTANCE.getLog().log(new Status(status, PLUGIN_ID, message));
  }

  @Override
  public void start(BundleContext context) throws Exception {
    Activator.INSTANCE = this;
    IPath prefsLocation = getStateLocation().append("preferences");
    prefs = SecurePreferencesFactory
        .open(prefsLocation.toFile().toURI().toURL(), null);
    jobs = new ArrayList<>();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    Activator.INSTANCE = null;
    // Wait for jobs to terminate
    for (Job j : jobs) {
      if (!j.cancel()) {
        j.wait();
      }
    }
  }

  public static void registerJob(Job job) {
    INSTANCE.jobs.add(job);
  }

}
