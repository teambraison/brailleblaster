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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.sun.jna.Platform;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.commons.lang3.StringUtils;

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
		File bbPath = getBrailleblasterPath();
		System.out.println("BrailleBlaster path: " + bbPath);

		initLogback(bbPath);
		initSWT(bbPath);
		BBIni.initialize(args, bbPath);

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
	 * Find the programData folder, needed very early in initialization to
	 * automatically load required resources
	 * @return
	 */
	public static File getBrailleblasterPath() {
		// Option to use an environment variable (mostly for testing
		// with Eclipse)
		String url = System.getenv("BBLASTER_WORK");
		if (StringUtils.isBlank(url))
			url = System.getProperty("BBLASTER_WORK");
		if (url != null) {
//			if (BBIni.getPlatformName().equals("cocoa")
//					|| BBIni.getPlatformName().equals("gtk"))
//				url = "file://" + url;
//			else
//				url = "file:/" + url;
			return new File(url).getAbsoluteFile();
		}

		//Attempt to find the programData folder
		String knownFile = "programData/settings/about.properties";
		File file;

		//Developer, working directory = project root
		file = new File("dist", knownFile);
		if (file.exists()) {
			return new File("dist").getAbsoluteFile();
		}

		//User, working directory = dist/ folder
		file = new File(knownFile);
		if (file.exists()) {
			return new File("").getAbsoluteFile();
		}

		//Origional BB code, not sure what this does
		url = Main.class.getClass().getResource(
				"/" + Main.class.getClass().getName().replaceAll("\\.", "/") + ".class"
		).toString();
		url = url.substring(url.indexOf("file")).replaceFirst(
				"/[^/]+\\.jar!.*$", "/");

		try {
			File dir = new File(new URL(url).toURI());
			return dir.getAbsoluteFile();
		} catch (Exception e) {
			throw new RuntimeException("Failed to find path with url " + url, e);
		}
	}

	public static void initLogback(File bbPath) {
		if(System.getProperty("logback.configurationFile") != null)
			//User passed explicit config, logback will pick it up automatically
			return;
		
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			File logbackConf = new File(bbPath, "programData/settings/logback.xml");
			System.out.println("Logback conf: " + logbackConf);
			configurator.doConfigure(logbackConf);
		} catch (JoranException je) {
			// StatusPrinter will handle this
		} catch (Exception ex) {
			ex.printStackTrace(); // Just in case, so we see a stacktrace
		}
		// Internal status data is printed in case of warnings or errors.
		StatusPrinter.printInCaseOfErrorsOrWarnings(context); 
	}

	/**
	 * Loads SWT by generating platform-specific JAR name and adding to the classpath.
	 * Needed as different bit versions of the same OS overwrite each others libraries
	 */
	public static void initSWT(File bbPath) {
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
		File swtFile = new File(bbPath, "lib/" + swtFileName).getAbsoluteFile();
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
