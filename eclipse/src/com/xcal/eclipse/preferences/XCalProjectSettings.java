package com.xcal.eclipse.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

import com.xcal.eclipse.Activator;
import com.xcal.eclipse.Messages;
import com.xcal.eclipse.model.Project;

/**
 * 
 * @author Logilica
 * View for setting an individual projects settings
 */
public class XCalProjectSettings extends PropertyPage implements IWorkbenchPropertyPage {
	// These are referred to else where to do lookup of the IEclipsePreferences
	public static String BuildCmdVar = "BUILD_CMD"; //$NON-NLS-1$
	public static String PrebuildCmdVar = "PREBUILD_CMD"; //$NON-NLS-1$
	public static String BuildDirVar = "BUILD_DIR"; //$NON-NLS-1$
	public static String ProjectVar = "PROJECT_ID"; //$NON-NLS-1$
	public static String UploadSourceVar = "UPLOAD_SRC"; //$NON-NLS-1$
	private IEclipsePreferences prefs;
	
	// Makes a heading in the preference pane
	private void makeHeader(Composite parent, String heading) {
		Label header = new Label(parent, SWT.NONE);
		header.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 3, 1));
		header.setText(heading);
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
	}
	
	// Makes a label for a field
	private void makeLabel(Composite parent, String name) {
		Label fieldName = new Label(parent, SWT.NONE);
		fieldName.setText(name);
		fieldName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
	}
	
	/**
	 * Creates a text box to add to the parent component
	 * @param parent The component to add to
	 * @param name The text to appear next to the text box
	 * @param prefsKey The key to use to store/retrieve from the IEclipsePreferences for this project
	 * @param defaultVal The default value if no value is found in the current store
	 * @param len The grid length
	 * @return The text box
	 */
	private Text getTextField(Composite parent, String name, String prefsKey, String defaultVal, int len) {
		makeLabel(parent, name);
		Text box = new Text(parent, SWT.NONE);
		box.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, len, 1));
		box.setText(prefs.get(prefsKey, defaultVal));
		return box;
	}
	
	/**
	 * Adds a text box to the parent component with a listener for value changes
	 * @param parent The component to add to
	 * @param name The text to appear next to the text box
	 * @param prefsKey The key to use to store/retrieve from the IEclipsePreferences for this project
	 * @param defaultVal The default value if no value is found in the current store
	 */
	private void makeTextField(Composite parent, String name, String prefsKey, String defaultVal) {
		Text box = getTextField(parent, name, prefsKey, defaultVal, 2);
		box.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				prefs.put(prefsKey, box.getText()); 
			}
		});
	}
	
	/**
	 * Makes a directory field, with a text box for manual entry, or a button to open a directory browser
	 * @param parent The component to add the directory browser to
	 * @param name The text to appear next to the text box/button
	 * @param prefsKey The key to use to store/retrieve from the IEclipsePreferences for this project
	 */
	private void makeDirectoryField(Composite parent, String name, String prefsKey) {
		Text fieldDir = getTextField(parent, name, prefsKey, "", 1); //$NON-NLS-1$
		Button dirButton = new Button(parent, SWT.NONE);
		DirectoryDialog dir = new DirectoryDialog(parent.getShell());
		dirButton.setText(Messages.XCalProjectSettings_5);
		dirButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		dirButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				String path = dir.open();
				fieldDir.setText(path);
				prefs.put(prefsKey, path);
			}
		});
		fieldDir.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				prefs.put(prefsKey, fieldDir.getText()); 
			}
		});
	}
	
	/**
	 * Makes a dropdown of available projects in the scan server, and adds it to the parnet
	 * @param parent The component to add to
	 * @param name The text to appear next to the dropdown
	 * @param prefsKey The key to use to store/retrieve from the IEclipsePreferences for this project
	 * @param defaultVal The default value if no value is found in the current store
	 * @param projects The projects to list in the dropdown
	 */
	private void makeProjectSelector(Composite parent, 
			String name, 
			String prefsKey, 
			String defaultVal, 
			List<Project> projects) 
	{	
		final String addNewProjectText = Messages.XCalProjectSettings_6;
		ArrayList<String> values = new ArrayList<String>();
		for (Project p : projects) {
			values.add(p.projectName);
		}
		values.add(addNewProjectText);
		makeLabel(parent, name);
		Combo selector = new Combo(parent, SWT.NONE);
		String[] valuesArr = new String[1];
		selector.setItems(values.toArray(valuesArr));
		String currentValue = prefs.get(prefsKey, defaultVal);
		if (currentValue == null || currentValue.equals("")) { //$NON-NLS-1$
			selector.setText(addNewProjectText);
		} else {
			for (Project p : projects) {
				if (p.projectId.equals(currentValue)) {
					selector.setText(p.projectName);
				}
			}		
		}
		selector.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String currentValue = selector.getText();
				if (currentValue.equals(addNewProjectText)) {
					prefs.put(prefsKey, ""); //$NON-NLS-1$
					return;
				}
				for (Project p : projects) {
					if (p.projectName.equals(currentValue)) {
						prefs.put(prefsKey, p.projectId);
					}
				}
			}
		});
	}
	
	/**
	 * Makes a checkbox and adds it to the parent
	 * @param parent The component to add to
	 * @param name The text to appear next to the checkbox
	 * @param prefsKey The key to use to store/retrieve from the IEclipsePreferences for this project
	 * @param defaultVal The default value if no value is found in the current store
	 */
	private void makeCheckbox(Composite parent, String name, String prefsKey, boolean defaultVal) {
		Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.setText(name);
		checkbox.setSelection(prefs.getBoolean(prefsKey, defaultVal));
		checkbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		checkbox.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				prefs.putBoolean(prefsKey, checkbox.getSelection()); 
			}
		});
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite prefsComposite = new Composite(parent, SWT.NONE);
		
		IAdaptable elem = getElement();
		IProject project;
		if (elem instanceof IProjectNature) {
			project = ((IProjectNature)elem).getProject();
		} else if (elem instanceof IProject) {
			project = (IProject)elem;
		} else {
			return null;
		}
		prefs = Activator.getPrefsForProject((IProject)project);
		boolean isJava = Activator.getDefault().projectIsJava(project);
		GridLayout grid = new GridLayout();
		grid.numColumns = 3;
		prefsComposite.setLayout(grid);
		makeHeader(prefsComposite, Messages.XCalProjectSettings_9);
		if (!isJava) {
			makeTextField(prefsComposite, Messages.XCalProjectSettings_0, PrebuildCmdVar, ""); //$NON-NLS-2$
		}
		makeTextField(prefsComposite, Messages.XCalProjectSettings_10, BuildCmdVar, ""); //$NON-NLS-2$ //$NON-NLS-1$
		makeDirectoryField(prefsComposite, Messages.XCalProjectSettings_12, BuildDirVar);
		makeHeader(prefsComposite, Messages.XCalProjectSettings_13);
		List<Project> opts = Activator.getDefault().wrapper.getProjects();
		makeProjectSelector(prefsComposite, Messages.XCalProjectSettings_14, ProjectVar, null, opts);
		makeCheckbox(prefsComposite, Messages.XCalProjectSettings_15, UploadSourceVar, true);
		return prefsComposite;
	}
	
	@Override
	public boolean performOk() {
		try {
			// On ok or apply we flush the prefs, to save the state
			prefs.flush();
		} catch (BackingStoreException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public void performApply() {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			return;
		}
	}
	 
}
