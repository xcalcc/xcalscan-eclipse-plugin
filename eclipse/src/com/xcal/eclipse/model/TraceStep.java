package com.xcal.eclipse.model;

/// Simple wrapper of a issue trace step for UI display
public class TraceStep {
	public final String filePath;
	public final int lineNumber;
	public final int columnNumber;
	public final String message;
	
	public TraceStep(String filePath, int lineNumber, int columnNumber, String message) {
		this.filePath = filePath;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.message = message;
	}
}
