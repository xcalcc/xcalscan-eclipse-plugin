package com.xcal.eclipse.services;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.json.*;

import com.xcal.eclipse.Activator;
import com.xcal.eclipse.Messages;
import com.xcal.eclipse.model.TraceStep;

/**
 * 
 * @author Logilica
 * 
 * Manages the issues from Xcalscan. Tracks the current Xcalscan related IMarkers
 * and stores issue traces.
 *
 */
public class XcalWarningManager {
	private LinkedList<IMarker> currentMarkers;
	private HashMap<String,TraceStep[]> traces;
	
	public XcalWarningManager() {
		currentMarkers = new LinkedList<IMarker>();
		traces = new HashMap<String,TraceStep[]>();
	}
	
	/// Removes all current Xcalscan related markers from the IDE
	public void clearXCalMarkers() {
		for (IMarker m : currentMarkers) {
			try {
				m.delete();
			} catch (CoreException e) {
			}
		}
		currentMarkers.clear();
		traces.clear();
	}
	
	// Fetches the corresponding issue trace for a given marker
	public TraceStep[] traceForMarker(IMarker marker) {
		String issueId = marker.getAttribute("xcal-id", null); //$NON-NLS-1$
		if (issueId == null || !traces.containsKey(issueId)) {
			return new TraceStep[0];
		}
		return traces.get(issueId);
	}
	
	// Generates a issue trace consumable by the IDE later
	private TraceStep[] traceFromJson(JSONArray traceSteps) {
		int traceSize = traceSteps.length();
		TraceStep[] trace = new TraceStep[traceSize];
		for (int i = 0; i < traceSize; ++i) {
			try {
				JSONObject jStep = traceSteps.getJSONObject(i);
				TraceStep step = new TraceStep(
						jStep.getString("relativePath"), //$NON-NLS-1$
						jStep.getInt("lineNo"), //$NON-NLS-1$
						jStep.getInt("columnNo"), //$NON-NLS-1$
						jStep.getString("message")); //$NON-NLS-1$
				trace[i] = step;
			} catch (JSONException e) {
				continue;
			}
		}
		return trace;
	}
	
	// Mapping between Xcalscan severity and Eclipse severity
	private int markerSeverityFromXcalSeverity(String severity) {
		if (severity.equals("LOW")) { //$NON-NLS-1$
			return IMarker.PRIORITY_LOW;
		} else if (severity.equals("MEDIUM")) { //$NON-NLS-1$
			return IMarker.PRIORITY_NORMAL;
		} else {
			return IMarker.PRIORITY_HIGH;
		}
	}
	
	/// Converts from JSON supplied by the Xcalscan server to Eclipse markers
 	public void fromJson(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			JSONArray issueContent = obj.getJSONArray("issues"); //$NON-NLS-1$
			IProject project = Activator.getDefault().getCurrentProject();
			if (project == null) {
				return;
			}
			for (int i = 0; i < issueContent.length(); ++i) {
				try {
					JSONObject issue = issueContent.getJSONObject(i);
					if (issue == null) {
						continue;
					}
					IFile file = project.getFile(issue.getString("relativePath")); //$NON-NLS-1$
					IMarker m = file.createMarker(IMarker.PROBLEM);
					String issueID = issue.getString("id"); //$NON-NLS-1$
					String ruleId = issue.getString("ruleInformationId"); //$NON-NLS-1$
					String desc = Activator.getDefault().ruleDescManager.getRuleDescription(ruleId);
					m.setAttribute(IMarker.LINE_NUMBER, issue.getInt("lineNo")); //$NON-NLS-1$
					String ruleset = issue.getString("ruleSet"); //$NON-NLS-1$
					String issueCode = issue.getString("issueCode"); //$NON-NLS-1$
					// Includes issueCode and ruleset in the message to allow users to filter
					String severity = issue.getString("severity");
					String message = "[" + severity + "] " + "["+ruleset+" "+issueCode+"] "+desc;
					m.setAttribute(IMarker.MESSAGE, message);
					m.setAttribute("xcal-id", issueID); //$NON-NLS-1$
					m.setAttribute("xcal-rule-id", ruleId); //$NON-NLS-1$
					m.setAttribute(IMarker.SEVERITY, markerSeverityFromXcalSeverity(severity)); //$NON-NLS-1$
					JSONArray traceInfos = issue.getJSONArray("issueTraceInfos"); //$NON-NLS-1$
					if (traceInfos.length() > 0) {
						JSONObject firstTrace = traceInfos.getJSONObject(0);
						TraceStep[] trace = traceFromJson(firstTrace.getJSONArray("issueTraces")); //$NON-NLS-1$
						traces.put(issueID, trace);
					}
					
					currentMarkers.add(m);
				} catch (CoreException e) {
					System.out.println(Messages.XcalWarningManager_22);
					e.printStackTrace();
					return;
				}
			}
		} catch (JSONException e1) {
			return;
		}
	}
}
