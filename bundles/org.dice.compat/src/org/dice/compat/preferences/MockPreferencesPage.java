package org.dice.compat.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MockPreferencesPage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

  @Override
  public void init(IWorkbench workbench) {}

  @Override
  protected void createFieldEditors() {}
}
