package com.xcal.eclipse.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mylyn.wikitext.markdown.MarkdownLanguage;
import org.eclipse.mylyn.wikitext.parser.MarkupParser;
import org.json.JSONException;
import org.json.JSONObject;

import com.xcal.eclipse.XcalWrapper;

import com.xcal.eclipse.Activator;

/**
 * 
 * @author Logilica
 * This implements a wrapper around the rule description API calls
 * to cache results. This minimizes calls out to the server and saves
 * time when processing results.
 */
public class XcalRuleDescriptionManager {
	private Map<String, JSONObject> cache;
	
	public XcalRuleDescriptionManager() {
		cache = new HashMap<String, JSONObject>();
	}
	
	private JSONObject getRuleObj(String ruleId) {
		if (cache.containsKey(ruleId)) {
			return cache.get(ruleId);
		} 
		XcalWrapper wrapper = Activator.getDefault().wrapper;
		String ruleDesc = wrapper.getRuleInfo(ruleId);
		try {
			JSONObject obj = new JSONObject(ruleDesc);
			cache.put(ruleId, obj);
			return obj;
		} catch (JSONException e) {
			return null;
		}
	}
	
	/// Parses the Markdown of the rule description and returns HTML
	public String getHtmlForRuleId(String ruleId) {
		JSONObject obj = getRuleObj(ruleId);
		if (obj == null) {
			return "";
		}
		try {
			String markdown = obj.getString("detail");
			MarkupParser parser = new MarkupParser();
			parser.setMarkupLanguage(new MarkdownLanguage());
			return parser.parseToHtml(markdown);
		} catch (JSONException e) {
			return "";
		}
	}
	
	public String getRuleDescription(String ruleId) {
		JSONObject obj = getRuleObj(ruleId);
		if (obj == null) {
			return "";
		}
		try {
			return obj.getString("description");
		} catch (JSONException e) {
			return "";
		}
	}
}
