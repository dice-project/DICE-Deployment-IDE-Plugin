package org.dice.deployments.preferences;

import org.dice.deployments.preferences.pages.DeploymentsPreferencesPage;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class DeploymentsPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		DeploymentsPreferencesPage.getSingleton().initDefaults();
	}

}
