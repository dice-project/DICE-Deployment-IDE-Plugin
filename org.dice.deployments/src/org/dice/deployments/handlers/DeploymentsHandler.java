package org.dice.deployments.handlers;

import java.net.MalformedURLException;
import java.net.URL;

import org.dice.deployments.DeploymentsActivator;
import org.dice.deployments.preferences.pages.DeploymentsPreferencesPage;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class DeploymentsHandler extends AbstractHandler {

	private static final String BROWSER_ID = "DEPLOYMENTS_BROWSER_ID";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			int style = IWorkbenchBrowserSupport.AS_EDITOR
					  | IWorkbenchBrowserSupport.LOCATION_BAR
					  | IWorkbenchBrowserSupport.NAVIGATION_BAR;
			IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport()
					.createBrowser(style, BROWSER_ID, null, "");
			IPreferenceStore store = DeploymentsActivator.getDefault()
					.getPreferenceStore();
			String protocol = store.getString(DeploymentsPreferencesPage.PROTOCOL_KEY);
			String server = store.getString(DeploymentsPreferencesPage.SERVER_KEY);
			Integer port = store.getInt(DeploymentsPreferencesPage.PORT_KEY);
			String container = store.getString(DeploymentsPreferencesPage.CONTAINER_KEY);
			URL url = new URL(protocol, server, port, "?container-id=" + container);
			browser.openURL(url);
		} catch (PartInitException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(),
					"Error while opening the Web Browser",
					"An error occurred while opening the Web Browser. Try restarting the IDE. More details:\n"
							+ e.getMessage());
		} catch (MalformedURLException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(),
					"Error while opening the Web Browser",
					"An error occurred while opening the URL. More details:\n" + e.getMessage());
		}

		return null;
	}

}
