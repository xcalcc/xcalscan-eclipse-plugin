package com.xcal.eclipse;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author Logilica
 * Listens for changes in selections, both for issues in the Problems window
 * as well as the currently selected Project.
 * 
 */

public class SelectionListener {
	private final ISelectionListener listener;
	private IProject currentProject = null;
	private IMarker currentMarker = null;
	
	/**
	 * 
	 * @param lastProjectName The last project name selected, retrieved in Activator from the PrefenceStore
	 */
	public SelectionListener(String lastProjectName) {
		listener = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				setMarker(selection);
				setProject(selection);
			}
		};
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow win : windows) {
			win.getSelectionService().addSelectionListener(listener);
		}
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {

			@Override
			public void windowActivated(IWorkbenchWindow window) {
				return;
			}

			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
				return;
			}

			@Override
			public void windowClosed(IWorkbenchWindow window) {
				window.getSelectionService().removeSelectionListener(listener);
			}

			@Override
			public void windowOpened(IWorkbenchWindow window) {
				window.getSelectionService().addSelectionListener(listener);
			}
		});
		// When Eclipse first starts, there is no project selected. To work around this,
		// we remember the last project selected in the PrefenceStore, or if that is not set,
		// take the first one.
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IProject matchingProj = null;
		for (IProject p : projects) {
			if (p.getName().equals(lastProjectName)) {
				matchingProj = p;
			}
		}
		if (matchingProj != null) {
			currentProject = matchingProj;
		} else if (projects.length > 0) {
			currentProject = projects[0];
		}
	}
	
	public IProject getProject() {
		return currentProject;
	}
	
	public IMarker getMarker() {
		return currentMarker;
	}
	
	private void setMarker(ISelection selection) {
		if (selection == null || !(selection instanceof IStructuredSelection)) {
			return;
		}
		Object elem = ((IStructuredSelection)selection).getFirstElement();
		if (elem == null) {
			return;
		}
		if (elem instanceof IAdaptable) {
			IMarker mark = ((IAdaptable) elem).getAdapter(IMarker.class);
			if (mark != null) {
				currentMarker = mark;
				// Here we need to notify the marker listener as well, to update
				// the two issue views (trace and description)
				Activator.getDefault().markerManager.notifyListeners(currentMarker);
			}
		}
	}
	
	private void setProject(ISelection selection) {
		if (selection == null || !(selection instanceof IStructuredSelection)) {
			return;
		}
		Object elem = ((IStructuredSelection)selection).getFirstElement();
		if (elem == null) {
			return;
		}
		IResource res;
		if (elem instanceof IResource) {
			res = (IResource)elem;
		} else {
			if (!(elem instanceof IAdaptable)) {
				return;
			}
			Object adapter = ((IAdaptable)elem).getAdapter(IResource.class);
			if (adapter == null || !(adapter instanceof IResource)) {
				return;
			}
			res = (IResource)adapter;
		}
		currentProject = res.getProject();
		if (currentProject != null) {
			// Update the preference store with the currently selected project, to reuse on IDE re-launch
			// (see comment in constructor)
			Activator.getDefault().getPreferenceStore().putValue("last-project", currentProject.getName());
		}
	}
	
}
