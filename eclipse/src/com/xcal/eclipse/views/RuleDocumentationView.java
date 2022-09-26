package com.xcal.eclipse.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.xcal.eclipse.Activator;

public class RuleDocumentationView extends IMarkerTrackingView {
	private Browser browserComponent = null;

	/// Opens a window with rendered HTML of a given rule description
	@Override
	protected void draw(Composite parent, IMarker marker) {
		if (browserComponent == null) {
			browserComponent = new Browser(parent, SWT.NONE);
			browserComponent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		if (marker == null || marker.getAttribute("xcal-id", null) == null) {
			// The parent class should take care of this, but just in case.
			return;
		}
		String ruleId = marker.getAttribute("xcal-rule-id", null);
		String content = Activator.getDefault().ruleDescManager.getHtmlForRuleId(ruleId);
		browserComponent.setText(content);
	}

	@Override
	protected void clean() {
		if (browserComponent == null) {
			return;
		}
		browserComponent.dispose();
		browserComponent = null;
	}

}
