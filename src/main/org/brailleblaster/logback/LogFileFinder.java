package org.brailleblaster.logback;

import java.io.File;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.spi.PropertyDefiner;
import ch.qos.logback.core.status.Status;

import org.eclipse.swt.SWT;

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
