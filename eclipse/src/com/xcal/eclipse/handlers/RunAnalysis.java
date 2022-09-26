package com.xcal.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.xcal.eclipse.Activator;

/// Simple class that just schedules the scan job in the IDE
public class RunAnalysis extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		XcalScanJob scanJob = new XcalScanJob();
		scanJob.schedule();
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return !(Activator.getDefault().isScanRunning());
	}

}
