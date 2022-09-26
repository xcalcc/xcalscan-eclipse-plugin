package com.xcal.eclipse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.console.MessageConsoleStream;
import org.json.*;
import org.osgi.service.prefs.BackingStoreException;
import java.util.UUID;

import com.xcal.eclipse.model.Project;
import com.xcal.eclipse.preferences.XCalProjectSettings;
import com.xcal.eclipse.preferences.XCalSystemSettings;

/**
 * 
 * @author Logilica
 * 
 * A wrapper around the two xcalagent tools used in the plugin. This is
 * the single point where all interaction with external xcaliscan tools should occur.
 *
 */
public class XcalWrapper {
	
	/// Generates the project configuration file for xcal-scanner from the eclipse project info
	private String getProjectConfiguration(IProject project) {
		IEclipsePreferences projPreferences = Activator.getPrefsForProject(project);
		if (projPreferences == null) {
			return null;
		}
		JSONObject config = new JSONObject();
		JSONObject scanConfig = new JSONObject();
		String projectName = project.getName();
		boolean isJava = Activator.getDefault().projectIsJava(project);
		IPath projectLocation = project.getLocation();
		String projectId = projPreferences.get(XCalProjectSettings.ProjectVar, null);
		if (projectId == null || projectId.equals("")) { //$NON-NLS-1$
			String suffix = UUID.randomUUID().toString();
			projectId = projectName.replace(' ', '_')+"-"+suffix; //$NON-NLS-1$
			// Update the project preferences with the new project id
			projPreferences.put(XCalProjectSettings.ProjectVar, projectId);
			try {
				projPreferences.flush();
			} catch (BackingStoreException e) {
			}
		}
		try {
			config.put("projectId", projectId); //$NON-NLS-1$
			config.put("projectName", projectName);
			config.put("projectPath", projectLocation.toOSString());
			String buildPath = projPreferences.get(XCalProjectSettings.BuildDirVar, "");
			if (buildPath.equals("")) {
				Activator.makeMessagebox(Messages.XcalWrapper_7, 0);
				return null;
			}
			config.put("buildPath", projPreferences.get(XCalProjectSettings.BuildDirVar, projectLocation.toOSString())); //$NON-NLS-1$
			config.put("uploadSourceCode", projPreferences.getBoolean(XCalProjectSettings.UploadSourceVar, true)); //$NON-NLS-1$
			String prebuildCmd = projPreferences.get(XCalProjectSettings.PrebuildCmdVar, null);
			if (!isJava && prebuildCmd != null && !prebuildCmd.equals("")) {
				scanConfig.put("configureCommand", prebuildCmd);
			}
			scanConfig.put("lang", isJava ? "java" : "c++"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			scanConfig.put("build", projPreferences.get(XCalProjectSettings.BuildCmdVar, isJava ? "mvn" : "make")); //$NON-NLS-1$ //$NON-NLS-2$
			config.put("scanConfig", scanConfig); //$NON-NLS-1$
		} catch (JSONException e) {
			return ""; //$NON-NLS-1$
		}
		return config.toString();
	}
	
	// Executes xcal-scanner and reports the projectId back from the console output
	private String runXcalScanner(Path projectConf, Path scanConfPath, Path xcalScanPath) throws IOException {
		List<String> cmd = new ArrayList<String>();
		cmd.add(xcalScanPath.toString());
		cmd.add("--scanner-conf"); //$NON-NLS-1$
		cmd.add(scanConfPath.toString());
		cmd.add("--project-conf"); //$NON-NLS-1$
		cmd.add(projectConf.toString());
		cmd.add("--debug"); //$NON-NLS-1$
		final ProcessBuilder builder = new ProcessBuilder(cmd);
		Map<String,String> env = builder.environment();
		System.out.println(env);
		builder.redirectErrorStream(true);
		final Process p = builder.start();
		InputStreamReader out = new InputStreamReader(p.getInputStream());
		BufferedReader outputReader = new BufferedReader(out);
		MessageConsoleStream consoleOut = Activator.getDefault().getConsole();
		
		String projectId = null;
		String outLine;
		Pattern scanObjectLine = Pattern.compile(".*main, scan_task_obj: (.*)$"); //$NON-NLS-1$
		while ((outLine = outputReader.readLine()) != null) {
			consoleOut.println(outLine);
			Matcher match = scanObjectLine.matcher(outLine);
			if (match.find()) {
				String objStr = match.group(1);
				try {
					JSONObject obj = new JSONObject(objStr);
					projectId = obj.getString("projectUuid"); //$NON-NLS-1$
				} catch (JSONException e) {
					continue;
				}
			}
		}
		
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace(); 
			return null;
		}
		

		return projectId;
	}
	
	// Wrapper to get the install directory that will display an error if it is not set in the preference window
	private String getInstallDirectory() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		String installPathStr = prefs.getString(XCalSystemSettings.InstallDirVar);
		if (installPathStr == null || installPathStr.equals("")) { //$NON-NLS-1$
			Activator.makeMessagebox(Messages.XcalWrapper_23, SWT.OK);
			return null;
		}
		return installPathStr;
	}
	
	/// Public function to start a scan
	public String startScan(IProject project) {
		String projectConfContents = getProjectConfiguration(project);
		if (projectConfContents == null) {
			return null;
		}
		try {
			Path projectConf = Files.createTempFile("xcal-project", ".conf"); //$NON-NLS-1$ //$NON-NLS-2$
			Files.write(projectConf, projectConfContents.getBytes(), StandardOpenOption.WRITE);
			String installPathStr;
			if ((installPathStr = getInstallDirectory()) == null) {
				return null;
			}
			boolean isWindows = System.getProperty("os.name").matches("Windows.*");
			String exeName = isWindows ? "xcal-scanner.exe" : "xcal-scanner";
			Path scanConfPath = Paths.get(installPathStr, "workdir", "run.conf"); //$NON-NLS-1$ //$NON-NLS-2$
			Path xcalScanPath = Paths.get(installPathStr, "tools", exeName); //$NON-NLS-2$
			return runXcalScanner(projectConf, scanConfPath, xcalScanPath);
		} catch (IOException e) {
			return null;
		}
	}
	
	// Wrapper around calling xcal-command. Handles localisation of strings from server as well.
	private String runXcalCommand(String installPath, String command, List<String> args) {
		try {
			String localeTag = Locale.getDefault().toLanguageTag();
			String locale = "zh-CN"; //$NON-NLS-1$
			if (localeTag.startsWith("en-")) { //$NON-NLS-1$
				locale = "en"; //$NON-NLS-1$
			}
			boolean isWindows = System.getProperty("os.name").matches("Windows.*");
			String exeName = isWindows ? "xcal-commands.exe" : "xcal-commands";
			Path xcalScanPath = Paths.get(installPath, "tools", exeName); //$NON-NLS-2$
			Path outputFile = Files.createTempFile("xcal-command", ".out"); //$NON-NLS-1$ //$NON-NLS-2$
			Path scanConfPath = Paths.get(installPath, "workdir", "run.conf"); //$NON-NLS-1$ //$NON-NLS-2$
			List<String> cmd = new ArrayList<String>();
			cmd.add(xcalScanPath.toString());
			cmd.add("--output"); //$NON-NLS-1$
			cmd.add(outputFile.toString());
			cmd.add("--scanner-conf"); //$NON-NLS-1$
			cmd.add(scanConfPath.toString());
			cmd.add("--locale"); //$NON-NLS-1$
			cmd.add(locale);
			cmd.add(command);
			if (args != null) {
				cmd.addAll(args);
			}
			MessageConsoleStream consoleOut = Activator.getDefault().getConsole();
			final ProcessBuilder builder = new ProcessBuilder(cmd);
			builder.redirectErrorStream(true);
			final Process p = builder.start();
			InputStreamReader out = new InputStreamReader(p.getInputStream());
			BufferedReader outputReader = new BufferedReader(out);
			p.waitFor();
			String line;
			while ((line = outputReader.readLine()) != null) {
				consoleOut.println(line);
			}
			return String.join("\n", Files.readAllLines(outputFile)); //$NON-NLS-1$
		} catch (IOException e) {
			return null;
		} catch (InterruptedException e) {
			return null;
		}
		
	}
	
	/// Updates the progress monitor from the IDE with the scan progress from the server
	public String monitorProgress(IProgressMonitor monitor, String projectId) {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		String installPathStr = prefs.getString(XCalSystemSettings.InstallDirVar);
		List<String> extraArgs = new ArrayList<String>();
		extraArgs.add("--project");  //$NON-NLS-1$
		extraArgs.add(projectId);
		int work = 0;
		String scanId = null;
		while (work < 100) {
			try {
				String res = runXcalCommand(installPathStr, "get-scan-task", extraArgs); //$NON-NLS-1$
				JSONObject obj = new JSONObject(res);
				
				int newValue = Math.round((float)obj.getDouble("percentage")); //$NON-NLS-1$
				scanId = obj.getString("scanTaskId"); //$NON-NLS-1$
				monitor.worked(newValue - work);
				work = newValue;
				Thread.sleep(1000);
			} catch (JSONException je) {
				je.printStackTrace();
				work = 100;
			} catch (InterruptedException e) {
				e.printStackTrace();
				work = 100;
			}
		}
		return scanId;
	}
	
	/// Fetches rule description from the server
	public String getRuleInfo(String ruleId) {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		String installPathStr = prefs.getString(XCalSystemSettings.InstallDirVar);
		List<String> args = new ArrayList<String>();
		args.add("--rule-id"); //$NON-NLS-1$
		args.add(ruleId);
		return runXcalCommand(installPathStr, "rule-description", args); //$NON-NLS-1$
		
	}
	
	/// Fetches scan results from the scan server
	public String getScanResult(String scanId) {
		String installPathStr;
		if ((installPathStr = getInstallDirectory()) == null) {
			return null;
		}
		List<String> args = new ArrayList<String>();
		args.add("--scan-id"); //$NON-NLS-1$
		args.add(scanId);
		return runXcalCommand(installPathStr, "results",  args); //$NON-NLS-1$

	}
	
	/// Gets a list of projects on the xcal server
	public List<Project> getProjects() {
		String installPathStr;
		if ((installPathStr = getInstallDirectory()) == null) {
			return null;
		}
		String commandOutput = runXcalCommand(installPathStr, "get-projects", null); //$NON-NLS-1$
		if (commandOutput == null) {
			return null;
		}
		List<Project> projects = new ArrayList<Project>();
		try {
			JSONObject output = new JSONObject(commandOutput);
			JSONArray projectJson = output.getJSONArray("content"); //$NON-NLS-1$
			for (int i = 0; i < projectJson.length(); i++) {
				JSONObject obj = projectJson.getJSONObject(i);
				projects.add(new Project(obj.getString("name"), obj.getString("projectId"))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (JSONException e) {
			
		}
		return projects;
	}
	
}
