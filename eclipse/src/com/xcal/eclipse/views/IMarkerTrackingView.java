package com.xcal.eclipse.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.xcal.eclipse.Activator;
import com.xcal.eclipse.MarkerListener;
import com.xcal.eclipse.Messages;

/**
 * 
 * @author Logilica
 *
 * An abstract class for views that rely on the currently selected IMarker (Xcalscan issue).
 *
 */
public abstract class IMarkerTrackingView extends ViewPart {
	private MarkerListener listener;
	private Label currentLabel = null;
	
	/// Removes the "no issue selected"
	private void clearCurrentLabel(Composite parent) {
		if (currentLabel != null) {
			currentLabel.dispose();
			currentLabel = null;
			parent.requestLayout();
		}
	}
	
	/// Clears the parent and draws a label saying that the current selection is not an Xcalscan result
	private void drawNoXcalResult(Composite parent) {
		clearCurrentLabel(parent);
		currentLabel = new Label(parent, SWT.NONE);
		currentLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		currentLabel.setText(Messages.IMarkerTrackingView_0);
	}
	
	/// Handles when the current marker has changed
	private void doUpdate(Composite parent, IMarker marker) {
		if (marker == null || marker.getAttribute("xcal-id", null) == null) { //$NON-NLS-1$
			clean();
			parent.requestLayout();
			drawNoXcalResult(parent);
			return;
		}
		clearCurrentLabel(parent);
		draw(parent, marker);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		final Activator activator = Activator.getDefault();
		if (activator == null) {
			return;
		}
		IMarker currentMarker = activator.getCurrentMarker();
		listener = new MarkerListener() {
			@Override
			public void markerUpdate(IMarker marker) {
				doUpdate(parent, marker);
			}
		};
		activator.markerManager.registerListener(listener);
		doUpdate(parent, currentMarker);
	}
	
	@Override
	public void dispose() {
		Activator.getDefault().markerManager.deregisterListner(listener);
		super.dispose();
	}
	
	/// Implementing classes should draw their UI here
	protected abstract void draw(Composite parent, IMarker marker);
	
	/// Implementing classes should use this to cleanup after a marker has changed
	protected abstract void clean();
	
	@Override
	public void setFocus() {

	}

}
