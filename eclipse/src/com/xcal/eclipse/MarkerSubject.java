package com.xcal.eclipse;

import java.util.LinkedList;

import org.eclipse.core.resources.IMarker;

/**
 *
 * @author Logilica
 * 
 * MarkerSubject is where a MarkerListener should subscribe to get
 * updated IMarker selections from the UI.
 *
 */
public class MarkerSubject {
	private LinkedList<MarkerListener> listeners;
	
	public MarkerSubject() {
		listeners = new LinkedList<MarkerListener>();
	}
	
	public void registerListener(MarkerListener l) {
		listeners.add(l);
	}
	public void deregisterListner(MarkerListener l) {
		listeners.remove(l);
	}
	public void notifyListeners(IMarker marker) {
		for (MarkerListener l : listeners) {
			l.markerUpdate(marker);
		}
	}

}
