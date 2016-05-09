package org.dice.deployments.handlers;

import org.dice.deployments.preferences.pages.DeploymentsPreferencesPage;
import org.dice.ui.handlers.AbstractOpenBrowserHandler;
import org.dice.ui.preferences.pages.AbstractOpenBrowserPreferencesPage;

public class DeploymentsHandler extends AbstractOpenBrowserHandler {

	@Override
	protected String getBrowserId() {
		return "DEPLOYMENTS_BROWSER_ID";
	}

	protected AbstractOpenBrowserPreferencesPage getOpenBrowserPreferencesPage() {
		return DeploymentsPreferencesPage.getSingleton();
	}

}
