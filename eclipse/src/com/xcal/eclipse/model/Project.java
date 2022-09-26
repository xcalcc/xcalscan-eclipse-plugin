package com.xcal.eclipse.model;

/// Simple wrapper to store projects from the server to be displayed in a dropdown
public class Project {
	public String projectName;
	public String projectId;
	
	public Project(String projectName, String projectId) {
		this.projectName = projectName;
		this.projectId = projectId;
	}
}
