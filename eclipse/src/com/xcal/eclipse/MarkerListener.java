package com.xcal.eclipse;

import org.eclipse.core.resources.IMarker;

/**
 * 
 * @author Logilica
 *
 * The interface to implement to subscribe to the IMarker selection listening service.
 * This allows the implementer to detect when the user's selected item from the problem window
 * changes.
 */
public interface MarkerListener {
	public void markerUpdate(IMarker marker);
}
