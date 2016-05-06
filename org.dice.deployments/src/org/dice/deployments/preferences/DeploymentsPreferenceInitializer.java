package org.dice.deployments.preferences;

import org.dice.deployments.DeploymentsActivator;
import org.dice.deployments.preferences.pages.DeploymentsPreferencesPage;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class DeploymentsPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = DeploymentsActivator.getDefault().getPreferenceStore();
		DeploymentsPreferencesPage.initDefaults(store);
	}

}
