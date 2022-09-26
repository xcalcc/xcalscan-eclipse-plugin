package com.xcal.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.xcal.eclipse.Activator;

/**
 * 
 * @author Logilica
 * An abstract class for actions that depend on an Xcalscan issue being selected.
 * 
 * An implementing class must implement getViewId to return the ID of the view to show when
 * a valid selection has been made.
 */

public abstract class AbstractOpenXCalMarkerView extends AbstractHandler {
	
	abstract protected String getViewID();
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(getViewID());
		} catch (PartInitException e) {
			return null;
		}
		return null;
	}
	@Override
	public boolean isEnabled() {
		//These views will be valid only if an Xcalscan IMarker is selected.
		// This is checked here and the action is disabled where this is not true.
		Activator activator = Activator.getDefault();
		if (activator == null) {
			 return false;
		}
		IMarker marker = activator.getCurrentMarker();
		return marker != null && marker.getAttribute("xcal-id", null) != null;
	}
}
