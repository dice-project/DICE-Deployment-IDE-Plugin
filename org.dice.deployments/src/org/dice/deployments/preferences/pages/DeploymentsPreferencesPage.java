package org.dice.deployments.preferences.pages;

import org.dice.deployments.DeploymentsActivator;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DeploymentsPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String TITLE = "Deployment Tool";
	private static final String DESCRIPTION = "Preferences for Deployment Tool";

	public static final String PROTOCOL = "protocol";
	public static final String SERVER = "server";
	public static final String PORT = "port";


	public DeploymentsPreferencesPage() {
		this(GRID);
	}
	
	public DeploymentsPreferencesPage(int style) {
		this(TITLE, style);
	}

	public DeploymentsPreferencesPage(String title, int style) {
		super(title, null, style);
	}

	public DeploymentsPreferencesPage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(DeploymentsActivator.getDefault().getPreferenceStore());
		setDescription(DESCRIPTION);
	}

	@Override
	protected void createFieldEditors() {
		addField(new ComboFieldEditor(PROTOCOL, "Protocol",
				new String[][] { { "http", "http" }, { "https", "https" } }, getFieldEditorParent()));
		addField(new StringFieldEditor(SERVER, "Server", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PORT, "Port", getFieldEditorParent(), 5));
	}

	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(PROTOCOL, "http");
		store.setDefault(SERVER, "localhost");
		store.setDefault(PORT, 7080);
	}

}
