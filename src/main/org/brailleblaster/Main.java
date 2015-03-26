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

import com.sun.jna.Platform;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.brailleblaster.util.FileUtils;
import org.brailleblaster.wordprocessor.WPManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.liblouis.LibLouisUTDML;

/**
 * This class contains the main method. If there are no arguments it passes
 * control directly to the word processor. If there are arguments it passes them
 * first to BBIni and then to Subcommands. It will do more processing as the
 * project develops.
 * 
 * Note on Mac to initialize SWT properly you must pass -XstartOnFirstThread or
 * you will get "SWTException: Invalid Thread Access"
 */
public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) {
		BBIni.initialize(args);

		if (BBIni.haveSubcommands()) {
			new Subcommands(args);
		} else {
			new WPManager(null);
		}

		if (BBIni.haveLiblouisutdml()) {
			//		LibLouisUTDML louisutdml = LibLouisUTDML.getInstance();
			//		louisutdml.free();
			FileUtils fu = new FileUtils();
			fu.deleteDirectory(new File(BBIni.getTempFilesPath()));
		}
	}

	/**
	 * Loads SWT by generating platform-specific JAR name and adding to the classpath.
	 * Needed as different bit versions of the same OS overwrite each others libraries
	 */
	public static void initSWT() {
		//Attempt to guess the filename
		String swtFileUi;
		String swtFileOs;
		String swtFileArch = "x" + (Platform.is64Bit() ? "86_64" : "86");
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			swtFileUi = "win32";
			swtFileOs = "win32";
		} else if (osName.contains("mac")) {
			swtFileUi = "cocoa";
			swtFileOs = "macosx";
			//32-bit jar doesn't have arch for some reason
			if (swtFileArch.equals("x86"))
				swtFileArch = "";
		} else if (osName.contains("linux")) {
			swtFileUi = "gtk";
			swtFileOs = "linux";
		} else
			throw new SWTLoadFailed("Unknown os " + osName + ", arch " + swtFileArch);
		String swtFileName = "org.eclipse.swt."
				+ swtFileUi
				+ "."
				+ swtFileOs
				+ "."
				+ swtFileArch
				+ ".jar";

		//Verify
		File swtFile = new File(BBIni.getProgramDataPath("..", "lib", swtFileName)).getAbsoluteFile();
		if (!swtFile.exists())
			throw new SWTLoadFailed("Cannot find SWT jar at " + swtFile);
		log.debug("Attempting to load SWT jar " + swtFile);

		//Load
		try {
			URL url = swtFile.toURI().toURL();
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Class<?> urlClass = URLClassLoader.class;
			Method method = urlClass.getDeclaredMethod("addURL", new Class<?>[]{URL.class});
			method.setAccessible(true);
			method.invoke(urlClassLoader, new Object[]{url});
		} catch (Exception e) {
			throw new SWTLoadFailed("Could not add SWT to classpath", e);
		}
		log.debug("SWT Successfully loaded");
	}

	private static class SWTLoadFailed extends RuntimeException {
		private SWTLoadFailed(String xiMessage) {
			super(xiMessage);
		}

		public SWTLoadFailed(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
