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
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.PropertyFileManager;
import org.eclipse.swt.SWT;
import org.liblouis.liblouisutdml;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;


/**
 * Determine and set initial conditions. This class takes care of most platform
 * dependencies. Its get methods should be used rather than coding platform
 * determining code in other classes.
 */

public final class BBIni {

	private static BBIni bbini;

	/**
	 * Calls a private constructor, making this class a singleton.
	 */
	public static BBIni initialize(String[] args) {
		if (bbini == null)
			bbini = new BBIni(args);
		return bbini;
	}
	
	private static boolean debug = false;
	private static boolean gotGui = true;
	private static boolean multipleSubcommands = false;
	private static Logger logger;
	private static final String productName = "BrailleBlaster ND";
	private static final String BBVersion = "2014.04.17";
	private static final String releaseDate = "April 17, 2014";
	private static String brailleblasterPath; // FO
	private static String osName;
	private static String osVersion;
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
	private static String stylePath;
	public final static String propExtension = ".properties";
	private static boolean hSubcommands = false;
	private static boolean hLiblouisutdml = false;
	private static FileHandler logFile;
	private static final String BBID = "brlblst";
	private static String instanceId;
	private static String defaultCfg;
	private static String autoConfigSettings;
	private static PropertyFileManager propManager;
	
	private BBIni(String[] args) {
		long seconds = System.currentTimeMillis() / 1000;
		instanceId = Long.toString(seconds, 32);
		platformName = SWT.getPlatform();
		Main m = new Main();
		brailleblasterPath = getBrailleblasterPath(m);
		osName = System.getProperty("os.name");
		osVersion = System.getProperty("os.version");
		fileSep = System.getProperty("file.separator");
		String userHome = System.getProperty("user.home");
		String BBHome;
		programDataPath = brailleblasterPath + fileSep + "programData";
		helpDocsPath = brailleblasterPath + fileSep + "helpDocs";
		if (platformName.equals("win32")) {
			BBHome = System.getenv("APPDATA") + fileSep + BBID;
			nativeLibrarySuffix = ".dll";
		} 
		else if (platformName.equals("cocoa")) {
			BBHome = userHome + fileSep + "." + BBID;
			nativeLibrarySuffix = ".dylib";
		} 
		else {
			BBHome = userHome + fileSep + "." + BBID;
			nativeLibrarySuffix = ".so";
		}
		nativeLibraryPath = brailleblasterPath + fileSep + "native" + fileSep + "lib";
		FileUtils fu = new FileUtils();
		userProgramDataPath = BBHome + fileSep + "programData";
		File userData = new File(userProgramDataPath);
		if (!userData.exists()) {
			userData.mkdirs();
		}
		makeUserProgramData();
		userSettings = userProgramDataPath + fileSep + "settings" + fileSep + "user_settings.properties";
		if (!fu.exists(userSettings)) {
			fu.copyFile(programDataPath + fileSep + "settings" + fileSep + "user_settings.properties", userSettings);
		}
		propManager = new PropertyFileManager(userSettings);
		
		recentDocs = userProgramDataPath + fileSep + "recent_documents.txt";
		fu.create(recentDocs);
		stylePath = userProgramDataPath + fileSep + "styles";
		File styleDir = new File(stylePath);
		if (!styleDir.exists()){
			styleDir.mkdirs();
		}
		
		String dictPath = userProgramDataPath + fileSep + "dictionaries";
		File dictDir = new File(dictPath);
		if(!dictDir.exists())
			dictDir.mkdir();
		
		// File associations and settings for when we automatically switch config files.
		autoConfigSettings = userProgramDataPath + fileSep + "liblouisutdml" + fileSep + "lbu_files" + fileSep + "autoConfigSettings.txt";
		if (!fu.exists(autoConfigSettings)) {
			fu.copyFile(programDataPath + fileSep + "liblouisutdml" + fileSep + "lbu_files" + fileSep + "autoConfigSettings.txt", autoConfigSettings);
		}
		
		///////////////////////
		// Default Config File.
		
			// Get default config file.
			Properties props = new Properties();
			try
			{
				// Load it!
				props.load(new FileInputStream(BBIni.getUserSettings()));
			}
			catch (IOException e) { e.printStackTrace(); }
			
			// Store file name.
			defaultCfg = props.getProperty("defaultConfigFile");
			
			// If that key doesn't exist, then we need to put it there.
			if(defaultCfg == null)
			{
				// Add it, then store it.
				
				// Add to hash.
				props.setProperty("defaultConfigFile", "nimas.cfg");
				
				// Record as default.
				defaultCfg = "nimas.cfg";
				
				// Store.
				try
				{
					// Store to file.
					props.store( new FileOutputStream(BBIni.getUserSettings()), null );
				}
				catch (IOException e) { e.printStackTrace(); }
				
			} // if(defaultCfgFileName == null)

		// Default Config File.
		///////////////////////
			
		//Temporary fix, should be removed once log file handle issue is resolved
		String tempFolder = BBHome + fileSep + "temp";
		fu.deleteDirectory(new File(tempFolder));
		
		tempFilesPath = BBHome + fileSep + "temp" + fileSep + instanceId;
		File temps = new File(tempFilesPath);
			
		if (!temps.exists()){
			temps.mkdirs();
		}
		logFilesPath = BBHome + fileSep + "log";
		File logPath = new File(logFilesPath);
		if (!logPath.exists()) {
			logPath.mkdirs();
		}
		logger = Logger.getLogger("org.brailleblaster");
		try {
			logFile = new FileHandler(logFilesPath + fileSep + "log.xml");
		} 
		catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "cannot open logfile", e);
			e.printStackTrace();
		}
		if (logFile != null) {
			logger.addHandler(logFile);
		}
		// disable output to console
		logger.setUseParentHandlers(false);
		if (args.length > 0) {
			int i = 0;
			while (i < args.length) {
				if (args[i].charAt(0) != '-') {
					break;
				}
				if (args[i].equals("-debug")) {
					debug = true;
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
			liblouisutdml.loadLibrary(nativeLibraryPath, nativeLibrarySuffix);
			liblouisutdml.initialize(programDataPath, tempFilesPath, "liblouisutdml.log");
			hLiblouisutdml = true;
		} 
		catch (UnsatisfiedLinkError e) {
			logger.log(Level.SEVERE, "Problem with liblouisutdml library", e);
		} 
		catch (Exception e) {
			logger.log(Level.WARNING, "This shouldn't happen", e);
		}
	}

	private String getBrailleblasterPath(Object classToUse)  {
		//Option to use an environment variable (mostly for testing withEclipse)
		String url = System.getenv("BBLASTER_WORK");
		
		if (url != null) {
			if (BBIni.getPlatformName().equals("cocoa"))
				url = "file://" + url;
			else
				url = "file:/" + url;
		} 
		else {
			url = classToUse.getClass().getResource("/"+ classToUse.getClass().getName().replaceAll("\\.", "/") + ".class").toString();
			url = url.substring(url.indexOf("file")).replaceFirst("/[^/]+\\.jar!.*$", "/");
		}

		try {
			File dir = new File(new URL(url).toURI());
			url = dir.getAbsolutePath();
		} 
		catch (MalformedURLException mue) {
			url = null;
		} 
		catch (URISyntaxException ue) {
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

	public static boolean haveLiblouisutdml() {
		return hLiblouisutdml;
	}

	public static boolean haveSubcommands() {
		return hSubcommands;
	}

	
	public static String getProductName() {
		return productName;
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

	public static String getRecentDocs() {
		return recentDocs;
	}

	public static Logger getLogger() {
		return logger;
	}
	
	public static PropertyFileManager getPropertyFileManager(){
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
