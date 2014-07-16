package org.brailleblaster.louisutdml;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

import org.liblouis.LogCallback;
import org.liblouis.LogLevel;

public class LogHandler implements LogCallback {

	private final static Map<Integer, Level> LEVEL_MAPPINGS = new HashMap<Integer, Level>();
	static {
		LEVEL_MAPPINGS.put(LogLevel.FATAL, Level.SEVERE);
		LEVEL_MAPPINGS.put(LogLevel.ERROR, Level.SEVERE);
		LEVEL_MAPPINGS.put(LogLevel.WARNING, Level.WARNING);
		LEVEL_MAPPINGS.put(LogLevel.INFO, Level.INFO);
		LEVEL_MAPPINGS.put(LogLevel.DEBUG, Level.FINE);
		LEVEL_MAPPINGS.put(LogLevel.OFF, Level.OFF);
		LEVEL_MAPPINGS.put(LogLevel.ALL, Level.ALL);
	}
	private Logger logger;
	public LogHandler() {
		this.logger = Logger.getLogger("org.brailleblaster.louisutdml");
	}
	@Override
	public void logMessage(int level, String message) {
		Level javaLogLevel = LEVEL_MAPPINGS.get(level);
		this.logger.log(javaLogLevel, message);
	}

}
