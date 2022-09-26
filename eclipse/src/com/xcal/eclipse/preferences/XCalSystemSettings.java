package com.xcal.eclipse.preferences;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.xcal.eclipse.Activator;
import com.xcal.eclipse.Messages;

/**
 * 
 * @author Logilica
 * A preference pane to set global settings for Xcalscan in Eclipse
 *
 */
public class XCalSystemSettings extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static String InstallDirVar = "INSTALL_DIR"; //$NON-NLS-1$

    public XCalSystemSettings() {
        super(GRID);
    }
 
	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		Label desc = new Label(parent, SWT.NONE);
		desc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1)); 
		desc.setText(Messages.XCalSystemSettings_1);
		addField(new DirectoryFieldEditor(InstallDirVar, Messages.XCalSystemSettings_2, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

}
