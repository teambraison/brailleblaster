package org.brailleblaster.logback;

import java.io.File;

import org.eclipse.swt.SWT;

import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.spi.PropertyDefiner;

public class LogFileFinder extends PropertyDefinerBase implements PropertyDefiner {

	private File logPath;
	public LogFileFinder() {		
		File bbHome = new File(System.getProperty("user.home"), ".brlblst");
		if (SWT.getPlatform().equals("win32")) {
			bbHome = new File(System.getenv("APPDATA"), "brlblst");
		}
		logPath = new File(bbHome, "log");
		if (!logPath.exists()) {
			logPath.mkdirs();
		}
	}
	@Override
	public String getPropertyValue() {
		return logPath.getAbsolutePath();
	}

}
