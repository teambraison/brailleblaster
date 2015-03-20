package org.brailleblaster.settings;

import java.io.File;
import java.io.IOException;
import org.brailleblaster.BBIni;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.brailleblaster.utd.config.UTDConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages loading and saving of UTD
 *
 * @author lblakey
 */
public class SettingsManager {
	private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);
	private static final String UTD_FOLDER = "utd";
	private static final String STYLE_DEFS_NAME = "styleDefs.xml";
	private static final String BRAILLE_SETTINGS_NAME = "brailleSettings.xml";
	private static final String PAGE_SETTINGS_NAME = "pageSettings.xml";

	public SettingsManager() {
	}

	public void changeMappings(UTDTranslationEngine engine, String config) {
		try {
			//Given nimas.cfg first then later setDefault is called with epub.cfg
			log.debug("setting config {}", config, new RuntimeException("Who called me?!"));

			//TODO: Weird since files are split
			//TODO: Hardcoded nimas
			File file = BBIni.loadAutoProgramDataFile("utd", "nimas.actionMap.xml");
			UTDConfig.loadMappings(engine, file.getParentFile(), "nimas");
		} catch (IOException ex) {
			throw new RuntimeException("Could not load UTD mappings", ex);
		}
	}

	public void loadEngine(UTDTranslationEngine engine, String config) {
		try {
			//Style TODO: Somehow automagically load the correct config
			engine.setPageSettings(
					UTDConfig.loadPageSettings(BBIni.loadAutoProgramDataFile(UTD_FOLDER, PAGE_SETTINGS_NAME)));
			engine.setBrailleSettings(
					UTDConfig.loadBrailleSettings(BBIni.loadAutoProgramDataFile(UTD_FOLDER, BRAILLE_SETTINGS_NAME)));
			engine.setStyleDefinitions(
					UTDConfig.loadStyleDefinitions(BBIni.loadAutoProgramDataFile(UTD_FOLDER, STYLE_DEFS_NAME)));

			changeMappings(engine, config);
		} catch (Exception e) {
			throw new RuntimeException("Could not initialize UTD", e);
		}
	}

	public void createUserUTDFolder() {
		File location = new File(BBIni.getUserProgramDataPath(UTD_FOLDER));
		if (!location.exists())
			location.mkdir();
	}

	public File getUserPageSettingsFile() {
		return new File(BBIni.getUserProgramDataPath(UTD_FOLDER, PAGE_SETTINGS_NAME));
	}

	public File getUserBrailleSettingsFile() {
		return new File(BBIni.getUserProgramDataPath(UTD_FOLDER, BRAILLE_SETTINGS_NAME));
	}

	public File getUserStyleDefinitionsFile() {
		return new File(BBIni.getUserProgramDataPath(UTD_FOLDER, STYLE_DEFS_NAME));
	}

}
