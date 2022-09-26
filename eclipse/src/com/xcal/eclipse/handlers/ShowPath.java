package com.xcal.eclipse.handlers;

/// Opens the trace viewer
public class ShowPath extends AbstractOpenXCalMarkerView {
	public static String PATH_VIEW_ID = "com.xcal.eclipse.ui.PathView";

	@Override
	protected String getViewID() {
		return ShowPath.PATH_VIEW_ID;
	}
	
}
