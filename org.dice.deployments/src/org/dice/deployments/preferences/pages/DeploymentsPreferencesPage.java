package org.dice.deployments.preferences.pages;

import org.dice.deployments.DeploymentsActivator;
import org.dice.ui.preferences.pages.AbstractOpenBrowserPreferencesPage;
import org.eclipse.jface.preference.IPreferenceStore;

public class DeploymentsPreferencesPage extends AbstractOpenBrowserPreferencesPage {

	private static DeploymentsPreferencesPage singleton;

	public static DeploymentsPreferencesPage getSingleton() {
		if (singleton == null) {
			singleton = new DeploymentsPreferencesPage();
		}
		return singleton;
	}

	@Override
	public IPreferenceStore getPluginPreferenceStore() {
		return DeploymentsActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected String getPageDescription() {
		return "Preferences for Deployment Tool";
	}

	@Override
	public String getProtocolIdProperty() {
		return "deployments_protocol";
	}

	@Override
	public String getServerIdProperty() {
		return "deployments_server";
	}

	@Override
	public String getPortIdProperty() {
		return "deployments_port";
	}

	@Override
	protected String getDefaultProtocol() {
		return PROTOCOL.HTTP.name();
	}

	@Override
	protected String getDefaultServer() {
		return "109.231.122.46";
	}

	@Override
	protected int getDefaultPort() {
		return 8000;
	}

}
