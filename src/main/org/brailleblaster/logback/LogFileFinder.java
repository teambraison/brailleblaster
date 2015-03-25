package org.brailleblaster.logback;

import java.io.File;

import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.spi.PropertyDefiner;
import org.brailleblaster.BBIni;

public class LogFileFinder extends PropertyDefinerBase implements PropertyDefiner {

	private final File logPath;
	public LogFileFinder() {		
		File bbHome;
		if (BBIni.isWindows()) {
			bbHome = new File(System.getenv("APPDATA"), "brlblst");
		} else {
			bbHome = new File(System.getProperty("user.home"), ".brlblst");
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
