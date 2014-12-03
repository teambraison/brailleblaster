/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2014
 * American Printing House for the Blind, Inc. www.aph.org
 * and
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
 * and
 * American Printing House for the Blind, Inc. www.aph.org www.aph.org
 *
 * All rights reserved
 *
 * This file may contain code borrowed from files produced by various 
 * Java development teams. These are gratefully acknowledged.
 *
 * This file is free software; you can redistribute it and/or modify it
 * under the terms of the Apache 2.0 License, as given at
 * http://www.apache.org/licenses/
 *
 * This file is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
 * See the Apache 2.0 License for more details.
 *
 * You should have received a copy of the Apache 2.0 License along with 
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
 */

package org.brailleblaster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.PropertyFileManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.liblouis.LibLouis;
import org.liblouis.LibLouisUTDML;
import org.liblouis.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determine and set initial conditions. This class takes care of most platform
 * dependencies. Its get methods should be used rather than coding platform
 * determining code in other classes.
 */

/**
 * @author cmyers
 *
 */
/**
 * @author cmyers
 * 
 */
public final class BBIni {

	private static BBIni bbini;

	/**
	 * Calls a private constructor, making this class a singleton.
	 */
	public static BBIni initialize(String[] args) {
		if (bbini == null || BBIni.debugging())
			bbini = new BBIni(args);
		return bbini;
	}

	private static boolean debug = false;
	private static String debugFilePath, debugSavePath;
	private static boolean gotGui = true;
	private static boolean multipleSubcommands = false;
	private final static Logger logger = LoggerFactory.getLogger(BBIni.class);
	//private static final String productName = "BrailleBlaster ND";
	//private static final String BBVersion = "0.07 Alpha";
	//private static final String releaseDate = "October, 31, 2014";
	
	private static String productName;
	private static String BBVersion;
	private static String releaseDate;
	
	private static String brailleblasterPath; // FO
	private static String fileSep;
	private static String nativeCommandPath;
	private static String nativeLibraryPath;
	private static String programDataPath;
	private static String userProgramDataPath;
	private static String helpDocsPath;
	private static String nativeCommandSuffix;
	private static String nativeLibrarySuffix;
	private static String recentDocs;
	private static String tempFilesPath;
	private static String logFilesPath;
	private static String platformName;
	private static String userSettings;
	private static String aboutProject;
	private static String stylePath;
	public final static String propExtension = ".properties";
	private static boolean hSubcommands = false;
	private static boolean hLiblouisutdml = false;
	// private static FileHandler logFile;
	private static final String BBID = "brlblst";
	private static String instanceId;
	private static String defaultCfg;
	private static String autoConfigSettings;
	private static PropertyFileManager propManager;

	private static String liblouisutdmlVersion;

	private BBIni(String[] args) {
		long seconds = System.currentTimeMillis() / 1000;
		instanceId = Long.toString(seconds, 32);
		platformName = SWT.getPlatform();
		Main m = new Main();
		brailleblasterPath = getBrailleblasterPath(m);
		fileSep = System.getProperty("file.separator");
		String userHome = System.getProperty("user.home");
		String BBHome;
		programDataPath = brailleblasterPath + fileSep + "programData";
		helpDocsPath = brailleblasterPath + fileSep + "helpDocs";
		if (platformName.equals("win32")) {
			BBHome = System.getenv("APPDATA") + fileSep + BBID;
			nativeLibrarySuffix = ".dll";
		} else if (platformName.equals("cocoa")) {
			BBHome = userHome + fileSep + "." + BBID;
			nativeLibrarySuffix = ".dylib";
		} else {
			BBHome = userHome + fileSep + "." + BBID;
			nativeLibrarySuffix = ".so";
		}
		nativeLibraryPath = brailleblasterPath + fileSep + "native" + fileSep
				+ "lib";
		FileUtils fu = new FileUtils();
		userProgramDataPath = BBHome + fileSep + "programData";
		File userData = new File(userProgramDataPath);
		if (!userData.exists()) {
			userData.mkdirs();
		}
		makeUserProgramData();
		userSettings = userProgramDataPath + fileSep + "settings" + fileSep
				+ "user_settings.properties";
		if (!fu.exists(userSettings)) {
			fu.copyFile(programDataPath + fileSep + "settings" + fileSep
					+ "user_settings.properties", userSettings);
		}
		propManager = new PropertyFileManager(userSettings);

		// Receive about.properties
		aboutProject = userProgramDataPath + fileSep + "settings" + fileSep
				+ "about.properties";
		if (!fu.exists(aboutProject)) {
			fu.copyFile(programDataPath + fileSep + "settings" + fileSep
					+ "about.properties", aboutProject);
		}

		//Load values
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(BBIni.getAbout()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Store data
		productName = prop.getProperty("name");
		BBVersion = prop.getProperty("version");
		releaseDate = prop.getProperty("date");
		
		recentDocs = userProgramDataPath + fileSep + "recent_documents.txt";
		fu.create(recentDocs);
		stylePath = userProgramDataPath + fileSep + "styles";
		File styleDir = new File(stylePath);
		if (!styleDir.exists()) {
			styleDir.mkdirs();
		}

		String dictPath = userProgramDataPath + fileSep + "dictionaries";
		File dictDir = new File(dictPath);
		if (!dictDir.exists())
			dictDir.mkdir();

		// File associations and settings for when we automatically switch
		// config files.
		autoConfigSettings = userProgramDataPath + fileSep + "liblouisutdml"
				+ fileSep + "lbu_files" + fileSep + "autoConfigSettings.txt";
		if (!fu.exists(autoConfigSettings)) {
			fu.copyFile(programDataPath + fileSep + "liblouisutdml" + fileSep
					+ "lbu_files" + fileSep + "autoConfigSettings.txt",
					autoConfigSettings);
		}

		// /////////////////////
		// Default Config File.

		// Get default config file.
		Properties props = new Properties();
		try {
			// Load it!
			props.load(new FileInputStream(BBIni.getUserSettings()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Store file name.
		defaultCfg = props.getProperty("defaultConfigFile");

		// If that key doesn't exist, then we need to put it there.
		if (defaultCfg == null) {
			// Add it, then store it.

			// Add to hash.
			props.setProperty("defaultConfigFile", "nimas.cfg");

			// Record as default.
			defaultCfg = "nimas.cfg";

			// Store.
			try {
				// Store to file.
				props.store(new FileOutputStream(BBIni.getUserSettings()), null);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} // if(defaultCfgFileName == null)

		// Default Config File.
		// /////////////////////

		// Temporary fix, should be removed once log file handle issue is
		// resolved
		// Only delete the files if we weren't working on something.
		// A value here means there was an abrupt shutdown.
		String prevWkFilePathStr = props.getProperty("prevWorkingFile");
		if (prevWkFilePathStr == null) {
			props.setProperty("prevWorkingFile", "");
			props.setProperty("originalDocPath", "");
			props.setProperty("zippedPath", "");
			props.setProperty("opfPath", "");
			props.setProperty("currentConfig", "");
			prevWkFilePathStr = props.getProperty("prevWorkingFile");
		}

		// If there's a working path, we had a previous session going.
		// Ask user to load it?
		boolean deleteTempDir = true;
		if (prevWkFilePathStr.length() > 0) {
			if (swtMsgBox("Restore previous session?") == SWT.YES) {
				File tempDir = new File(BBHome + fileSep + "temp" + fileSep);
				tempFilesPath = BBHome + fileSep + "temp" + fileSep
						+ tempDir.list()[0];
				deleteTempDir = false;
			}
		}

		// Delete temp dir and reset session?
		if (deleteTempDir == true) {
			// Reset session.
			props.setProperty("prevWorkingFile", "");
			props.setProperty("originalDocPath", "");
			props.setProperty("zippedPath", "");
			props.setProperty("opfPath", "");
			props.setProperty("currentConfig", "");
			prevWkFilePathStr = props.getProperty("prevWorkingFile");

			// Delete old temp directories.
			String tempFolder = BBHome + fileSep + "temp";
			fu.deleteDirectory(new File(tempFolder));
			// Give loaded files a new home.
			tempFilesPath = BBHome + fileSep + "temp" + fileSep + instanceId;
		}

		File temps = new File(tempFilesPath);

		if (!temps.exists()) {
			temps.mkdirs();
		}
		logFilesPath = BBHome + fileSep + "log";
		File logPath = new File(logFilesPath);
		if (!logPath.exists()) {
			logPath.mkdirs();
		}
		// an UncaughtExceptionHandler for uncaught exceptions, is this needed
		// as there is a try block around the event loop?
		LogUncaughtExceptionHandler bbUncaughtExceptionHandler = new LogUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(bbUncaughtExceptionHandler);

		if (args.length > 0) {
			int i = 0;
			while (i < args.length) {
				if (args[i].charAt(0) != '-') {
					break;
				}
				if (args[i].equals("-debug")) {
					debug = true;
					i++;
					String[] tokens = args[i].split(",");

					debugFilePath = getProgramDataPath() + fileSep
							+ "testFiles" + fileSep + tokens[0];

					if (tokens.length > 1)
						debugSavePath = getProgramDataPath() + fileSep
								+ "testFiles" + fileSep + tokens[1];

				} else if (args[i].equals("-nogui")) {
					gotGui = false;
				} else if (args[i].equals("-multcom")) {
					multipleSubcommands = true;
				} else {
					System.out.println("Bad option '" + args[i] + "'");
				}
				i++;
			}
			if (i < args.length) {
				hSubcommands = true;
			}
		}
		try {
			LibLouisUTDML.loadLibrary(nativeLibraryPath, nativeLibrarySuffix);
			LibLouisUTDML.getInstance().setLogLevel(LogLevel.ERROR);
			org.brailleblaster.louisutdml.LogHandler louisutdmlLogHandler = new org.brailleblaster.louisutdml.LogHandler();
			LibLouis.getInstance().registerLogCallback(louisutdmlLogHandler);
			LibLouisUTDML.getInstance().registerLogCallback(
					louisutdmlLogHandler);
			LibLouisUTDML.initialize(programDataPath, tempFilesPath,
					"liblouisutdml.log");
			liblouisutdmlVersion = LibLouisUTDML.getInstance().version();
			hLiblouisutdml = true;
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			logger.error("Problem with liblouisutdml library", e);
		} catch (Exception e) {
			logger.warn("This shouldn't happen", e);
		}
	}

	public int swtMsgBox(String messageStr) {
		Display d = new Display();
		Shell s = new Shell(d);
		org.eclipse.swt.widgets.MessageBox msgB = new org.eclipse.swt.widgets.MessageBox(
				s, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
		msgB.setMessage(messageStr);
		int result = msgB.open();
		d.dispose();
		return result;
	}

	private String getBrailleblasterPath(Object classToUse) {
		// Option to use an environment variable (mostly for testing
		// withEclipse)
		String url = System.getenv("BBLASTER_WORK");

		if (url != null) {
			if (BBIni.getPlatformName().equals("cocoa")
					|| BBIni.getPlatformName().equals("gtk"))
				url = "file://" + url;
			else
				url = "file:/" + url;
		} else {
			url = classToUse
					.getClass()
					.getResource(
							"/"
									+ classToUse.getClass().getName()
											.replaceAll("\\.", "/") + ".class")
					.toString();
			url = url.substring(url.indexOf("file")).replaceFirst(
					"/[^/]+\\.jar!.*$", "/");
		}

		try {
			File dir = new File(new URL(url).toURI());
			url = dir.getAbsolutePath();
		} catch (MalformedURLException mue) {
			url = null;
		} catch (URISyntaxException ue) {
			url = null;
		}

		return url;
	}

	private void makeUserProgramData() {
		String basePath = userProgramDataPath + fileSep;
		helpMakeUPD(basePath + "liblouis" + fileSep + "tables");
		helpMakeUPD(basePath + "liblouisutdml" + fileSep + "lbu_files");
		helpMakeUPD(basePath + "lang");
		helpMakeUPD(basePath + "semantics");
		helpMakeUPD(basePath + "styles");
		helpMakeUPD(basePath + "settings");
		helpMakeUPD(basePath + "fonts");
	}

	private void helpMakeUPD(String pathName) {
		File file = new File(pathName);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static boolean debugging() {
		return debug;
	}

	public static String getDebugFilePath() {
		return debugFilePath;
	}

	public static String getDebugSavePath() {
		return debugSavePath;
	}

	public static boolean haveLiblouisutdml() {
		return hLiblouisutdml;
	}

	public static boolean haveSubcommands() {
		return hSubcommands;
	}

	public static String getProductName() {
		return productName;
	}

	public static String getLiblouisutdmlVersion() {
		return liblouisutdmlVersion;
	}

	public static String getVersion() {
		return BBVersion;
	}

	public static String getReleaseDate() {
		return releaseDate;
	}

	public static String getFileSep() {
		return fileSep;
	}

	public static String getNativeCommandPath() {
		return nativeCommandPath;
	}

	public static String getNativeLibraryPath() {
		return nativeLibraryPath;
	}

	public static String getProgramDataPath() {
		return programDataPath;
	}

	public static String getHelpDocsPath() {
		return helpDocsPath;
	}

	public static String getNativeCommandSuffix() {
		return nativeCommandSuffix;
	}

	public static String getNativeLibrarySuffix() {
		return nativeLibrarySuffix;
	}

	public static String getUserProgramDataPath() {
		return userProgramDataPath;
	}

	public static String getTempFilesPath() {
		return tempFilesPath;
	}

	public static String getLogFilesPath() {
		return logFilesPath;
	}

	public static String getPlatformName() {
		return platformName;
	}

	public static String getUserSettings() {
		return userSettings;
	}

	public static String getAbout() {
		return aboutProject;
	}

	public static String getRecentDocs() {
		return recentDocs;
	}

	public static PropertyFileManager getPropertyFileManager() {
		return propManager;
	}

	public static boolean multCommands() {
		return multipleSubcommands;
	}

	public static String getStylePath() {
		return stylePath;
	}

	public static String getInstanceID() {
		return instanceId;
	}

	public static String getDefaultConfigFile() {
		return defaultCfg;
	}

	public static void setDefaultConfigFile(String configFileName) {
		defaultCfg = configFileName;
	}

	public static String getAutoConfigSettings() {
		return autoConfigSettings;
	}
}
