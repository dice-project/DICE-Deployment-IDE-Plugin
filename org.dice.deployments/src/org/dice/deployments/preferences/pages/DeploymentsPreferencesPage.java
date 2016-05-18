package org.dice.deployments.preferences.pages;

import org.dice.deployments.DeploymentsActivator;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DeploymentsPreferencesPage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public static final String DESCRIPTION = "Preferences for Deployment Tool";

	public static final String PROTOCOL_KEY = "deployments_protocol";
	public static final String PROTOCOL_VAL = "https";
	public static final String PROTOCOL_DSC = "Protocol";

	public static final String SERVER_KEY = "deployments_server";
	public static final String SERVER_VAL = "109.231.122.46";
	public static final String SERVER_DSC = "Server";

	public static final String PORT_KEY = "deployments_port";
	public static final int    PORT_VAL = 8000;
	public static final String PORT_DSC = "Port";

	public static final String CONTAINER_KEY = "deployments_container";
	public static final String CONTAINER_VAL = "<container-id>";
	public static final String CONTAINER_DSC = "Container id";

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(DeploymentsActivator.getDefault().getPreferenceStore());
		setDescription(DESCRIPTION);
	}

	@Override
	protected void createFieldEditors() {
		String[][] protocols = {{"http", "http"}, {"https", "https"}};
		addField(new ComboFieldEditor(PROTOCOL_KEY, PROTOCOL_DSC, protocols, getFieldEditorParent()));
		addField(new StringFieldEditor(SERVER_KEY, SERVER_DSC, getFieldEditorParent()));
		addField(new IntegerFieldEditor(PORT_KEY, PORT_DSC, getFieldEditorParent()));
		addField(new StringFieldEditor(CONTAINER_KEY, CONTAINER_DSC, getFieldEditorParent()));
	}

	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(PROTOCOL_KEY, PROTOCOL_VAL);
		store.setDefault(SERVER_KEY, SERVER_VAL);
		store.setDefault(PORT_KEY, PORT_VAL);
		store.setDefault(CONTAINER_KEY, CONTAINER_VAL);
	}

}
