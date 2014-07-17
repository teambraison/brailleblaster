package org.brailleblaster.louisutdml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.liblouis.LogCallback;
import org.liblouis.LogLevel;

public class LogHandler implements LogCallback {

	private static final Logger logger = LoggerFactory.getLogger(LogHandler.class);
	@Override
	public void logMessage(int level, String message) {
		if (level >= LogLevel.ERROR) {
			logger.error(message);
		} else if (level >= LogLevel.WARNING) {
			logger.warn(message);
		} else if (level >= LogLevel.INFO) {
			logger.info(message);
		} else if (level >= LogLevel.DEBUG) {
			logger.debug(message);
		}
	}

}
