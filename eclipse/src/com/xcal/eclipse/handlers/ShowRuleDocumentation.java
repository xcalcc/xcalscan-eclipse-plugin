package com.xcal.eclipse.handlers;

/// Opens the rule documentation view
public class ShowRuleDocumentation extends AbstractOpenXCalMarkerView {
	public static String RULE_DOC_VIEW_ID = "com.xcal.eclipse.ui.RuleDocumentation";

	@Override
	protected String getViewID() {
		return ShowRuleDocumentation.RULE_DOC_VIEW_ID;
	}
}