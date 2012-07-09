/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2010, 2012
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
 * All rights reserved
 *
 * This file may contain code borrowed from files produced by various 
 * Java development teams. These are gratefully acknoledged.
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
 * Maintained by John J. Boyer john.boyer@abilitiessoft.com
 */

package org.brailleblaster;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.BrailleblasterPath;
import org.brailleblaster.util.FileUtils;
import org.liblouis.liblouisutdml;
import java.lang.UnsatisfiedLinkError;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.io.IOException;
import java.io.File;
import org.brailleblaster.util.FileUtils;

/**
 * Determine and set initial conditions.
 */

public final class BBIni {

	private static BBIni bbini;

	public static BBIni initialize (String[] args) {
		if (bbini == null)
			bbini = new BBIni(args);
		return bbini;
	}

	private static boolean debug = false;
	private static boolean gotGui = true;
	private static boolean utd = false;
	private static Logger logger;
	private static Display display = null;
	private static String BBVersion;
	private static String releaseDate;
	private static String brailleblasterPath;
	private static String osName;
	private static String osVersion;
	private static String fileSep;
	private static String nativeCommandPath;
	private static String nativeLibraryPath;
	private static String programDataPath;
	private static String helpDocsPath;
	private static String nativeCommandSuffix;
	private static String nativeLibrarySuffix;
	private static String recentDocs;
	private static String settingsPath;
	private static String tempFilesPath;
	private static String platformName;
	private static String userSettings;
private static boolean hSubcommands = false;
	private static boolean hLiblouisutdml = false;
	private static FileHandler logFile;
	static final String BBID = "brlblst";

	private BBIni(String[] args) {
		LocaleHandler lh = new LocaleHandler();
		Main m = new Main();
		brailleblasterPath = BrailleblasterPath.getPath (m);
		osName = System.getProperty ("os.name");
		osVersion = System.getProperty ("os.version");
		fileSep = System.getProperty ("file.separator");
		platformName = SWT.getPlatform();
		String userHome = System.getProperty ("user.home");
		String BBHome;
programDataPath = brailleblasterPath + fileSep + "programData";
helpDocsPath = brailleblasterPath + fileSep + "helpDocs";
if (platformName.equals("win32")) {
BBHome = System.getenv ("APPDATA") + fileSep + BBID;
nativeLibrarySuffix = ".dll";
}
else if (platformName.equals ("cocoa")) {
BBHome = userHome + fileSep + BBID;
nativeLibrarySuffix = ".dylib";
}
else {
BBHome = userHome + fileSep + BBID;
nativeLibrarySuffix = ".so";
}
nativeLibraryPath = brailleblasterPath + fileSep + "native" + fileSep + 
"lib";
FileUtils fu = new FileUtils();
settingsPath = BBHome + fileSep + "settings";
File settings = new File (settingsPath);
if (!settings.exists())
settings.mkdirs();
userSettings = settingsPath + fileSep + 
"user_settings.properties";
if (!fu.exists (userSettings)) {
			fu.copyFile (programDataPath + fileSep + "settings" + fileSep + 
					"user_settings.properties", userSettings);
		}
		//this part initialize recent_documents.txt
		recentDocs = settingsPath + fileSep + "recent_documents.txt";
		fu.create(recentDocs);
		tempFilesPath = BBHome + fileSep + "temp";
		File temps = new File (tempFilesPath);
		if (!temps.exists())
			temps.mkdirs();
		logger = Logger.getLogger ("org.brailleblaster");
		try {
			logFile = new FileHandler 
					(tempFilesPath + fileSep + "log.xml");
		} catch (IOException e) {
			e.printStackTrace();
			logger.log (Level.SEVERE, "cannot open logfile", e);
			e.printStackTrace();
		}
		if (logFile != null) {
			logger.addHandler (logFile);
		}
		if (args.length > 0) {
int i = 0;
while (i < args.length) {
if (args[i].charAt(0) != '-') {
break;
}
				if (args[i].equals ("-debug")) {
					debug = true;
				}
				else if (args[i].equals ("-nogui")) {
					gotGui = false;
				}
				else if (args[i].equals ("-utd")) {
					utd = true;
				}
				else {
					System.out.println ("Bad option " + args[i]);
				}
i++;
			}
if (i < args.length) {
hSubcommands = true;
}
		}
		if (gotGui) {
			try {
				display = new Display();
			} catch (SWTError e) {
				logger.log (Level.SEVERE, "Can't find GUI", e);
			}
		}
try {
liblouisutdml.loadLibrary (nativeLibraryPath, nativeLibrarySuffix);
liblouisutdml.initialize (programDataPath, tempFilesPath, 
"liblouisutdml.log");
hLiblouisutdml = true;
} catch (UnsatisfiedLinkError e) {
			logger.log (Level.SEVERE, "Problem with liblouisutdml library", e);
		}
		catch (Exception e) {
			logger.log (Level.WARNING, "This shouldn't happen", e);
		}
	}

	public static boolean debugging() {
		return debug;
	}

	public static boolean haveGui() {
		return gotGui;
	}

	public static Display getDisplay()
	{
		return display;
	}

	public static boolean haveLiblouisutdml()
	{
		return hLiblouisutdml;
	}

public static boolean haveSubcommands() {
return hSubcommands;
}

	public static void setVersion (String version) {
		BBVersion = version;
	}

	public static String getVersion() {
		return BBVersion;
	}

	public static void setReleaseDate (String relDate) {
		releaseDate = relDate;
	}

	public static String getReleaseDate () {
		return releaseDate;
	}

	public static String getBrailleblasterPath()
	{
		return brailleblasterPath;
	}

	public static String getFileSep()
	{
		return fileSep;
	}

	public static String getNativeCommandPath()
	{
		return nativeCommandPath;
	}

	public static String getNativeLibraryPath()
	{
		return nativeLibraryPath;
	}

	public static String getProgramDataPath()
	{
		return programDataPath;
	}

	public static String getHelpDocsPath() {
		return helpDocsPath;
	}

	public static String getNativeCommandSuffix()
	{
		return nativeCommandSuffix;
	}

	public static String getNativeLibrarySuffix()
	{
		return nativeLibrarySuffix;
	}

	public static String getSettingsPath()
	{
		return settingsPath;
	}

	public static String getTempFilesPath () {
		return tempFilesPath;
	}

	public static String getPlatformName() {
		return platformName;
	}
	
	public static String getUserSettings(){
		return userSettings;
	}
	
	public static String getRecentDocs(){
	    return recentDocs;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static boolean useUtd () {
		return utd;
	}

}
