package com.xcal.eclipse;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.xcal.eclipse.services.XcalRuleDescriptionManager;
import com.xcal.eclipse.services.XcalWarningManager;

/**
 * 
 * @author Logilica
 * Activator is the core of the Xcalscan Eclipse plugin. It is instantiated on
 * eclipse start up and provides global resources to the other components of the
 * plugin.
 *
 */

public class Activator extends AbstractUIPlugin implements IStartup {

	public static Activator plugin = null;
	private SelectionListener selectionListner;
	public MarkerSubject markerManager;
	public XcalWarningManager warningManager;
	public XcalRuleDescriptionManager ruleDescManager;
	public XcalWrapper wrapper;
	private boolean scanIsRunning = false;
	
	public Activator() {
		if (plugin != null) {
			return;
		}
		plugin = this;
		String lastProjectName = this.getPreferenceStore().getString("last-project");
		selectionListner = new SelectionListener(lastProjectName);
		markerManager = new MarkerSubject();
		warningManager = new XcalWarningManager();
		ruleDescManager = new XcalRuleDescriptionManager();
		wrapper = new XcalWrapper();
	}
	
	/// Returns the currently selected project in the IDE as identified by the selection listener
	public IProject getCurrentProject() {
		return selectionListner.getProject();
	}
	
	/// Returns the currently selected marker in the IDE as identified by the selection listener
	public IMarker getCurrentMarker() {
		return selectionListner.getMarker();
	}
	
	/**
	 *  The Xcalscan plugin implemented earlyStartup to add a window listener to the IDE
	 *  window. This means we can clear out the Xcalscan warnings on shutdown to prevent issues
	 *  with them again when the IDE is restarted. Eclipse persists IMarkers across sessions,
	 *  however we lose the additional information stored about the such as their trace on shutdown.
	 */
	@Override
	public void earlyStartup() {
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
			@Override
			public void windowClosed(IWorkbenchWindow window) {
				//Do this in a listener so the IMarker is still available to remove
				//we remove these to avoid inconsistencies between Eclipse launches
				// (e.g. a new scan happens and the findings are invalid)
				Activator.getDefault().warningManager.clearXCalMarkers();	
			}
			@Override
			public void windowActivated(IWorkbenchWindow window) {}
			@Override
			public void windowDeactivated(IWorkbenchWindow window) {}
			@Override
			public void windowOpened(IWorkbenchWindow window) {}
			
		});
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	/// The primary way to get the Activator instance from other parts of the plugin
	public static Activator getDefault()   {
		return plugin;
	}
    
    public static IEclipsePreferences getPrefsForProject(IProject project) {
    	ProjectScope ps = new ProjectScope(project);
		return ps.getNode("com.xcal.eclipse");
    } 
 
    /// Creates a new event in the UI thread to show a message box - this fails unless
    /// it is run in UI thread.
    public static void makeMessagebox(String message, int flags) {
    	Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
		    	IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		    	Shell shell = window.getShell();
		    	MessageBox box = new MessageBox(shell, flags);
		    	box.setMessage(message);
		    	box.open();
			}
    		
    	});
    }
    public MessageConsoleStream getConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager man = plugin.getConsoleManager();
		MessageConsole console = null;
		for (IConsole c : man.getConsoles()) {
			if (c.getName().equals("Xcalscan") && c instanceof MessageConsole) {
				console = (MessageConsole)c;
			}
		}
		if (console == null) {
			console = new MessageConsole("Xcalscan", null);
			man.addConsoles(new IConsole[] {console});
		}
		console.activate();
    	return console.newMessageStream();
    }
    
    public void setScanStart() {
    	scanIsRunning = true;
    }
    public void setScanEnd() {
    	scanIsRunning = false;
    }
    public boolean isScanRunning() {
    	return scanIsRunning;
    }
    public boolean projectIsJava(IProject project) {
    	boolean isJava = false;
		try {
			isJava = project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			isJava = false;
		}
		return isJava;
    }
}
