package org.brailleblaster;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private static Logger logger = LoggerFactory.getLogger(LogUncaughtExceptionHandler.class);
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.error("Uncaught exception detected", e);
	}

}
