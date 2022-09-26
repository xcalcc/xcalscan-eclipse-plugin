package com.xcal.eclipse.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.xcal.eclipse.Activator;
import com.xcal.eclipse.XcalWrapper;
import com.xcal.eclipse.services.XcalWarningManager;

/**
 * 
 * @author Logilica
 * 
 * The Eclipse job to run a scan.
 */
public class XcalScanJob extends Job {

	public XcalScanJob() {
		super("Xcalscan");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Activator.getDefault().setScanStart();
		XcalWarningManager manager = Activator.getDefault().warningManager;
		XcalWrapper wrapper = Activator.getDefault().wrapper;
		// The total number of ticks for the monitor is set slightly higher than 100
		// This is because the monitorProgress() call works in percentages, but doesn't
		// account for setup time or import time. If we set this to just 100 it appears as
		// though the process has hung.
		monitor.beginTask("Xcalscan", 120);
		IProject project = Activator.getDefault().getCurrentProject();
		String projectId = wrapper.startScan(project);
		monitor.worked(15);
		if (projectId == null) {
			Activator.getDefault().setScanEnd();
			return Status.CANCEL_STATUS;
		}
		String scanId = wrapper.monitorProgress(monitor, projectId);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		String results = wrapper.getScanResult(scanId);
		manager.clearXCalMarkers();
		manager.fromJson(results);
		monitor.worked(5);
		monitor.done();
		Activator.getDefault().setScanEnd();
		return Status.OK_STATUS;
	}

}
