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
 * @author lblakey
 */
public class SettingsManager {
	private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);
	
	public SettingsManager(){
	}
	
	public void changeMappings(UTDTranslationEngine engine, String config) {
		try {
			//Given nimas.cfg first then later setDefault is called with epub.cfg
			log.debug("setting config {}", config, new RuntimeException());
			
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
					UTDConfig.loadPageSettings(BBIni.loadAutoProgramDataFile("utd", "pageSettings.xml")));
			engine.setBrailleSettings(
					UTDConfig.loadBrailleSettings(BBIni.loadAutoProgramDataFile("utd", "brailleSettings.xml")));
			engine.setStyleDefinitions(
					UTDConfig.loadStyleDefinitions(BBIni.loadAutoProgramDataFile("utd", "styleDefs.xml")));
			
			changeMappings(engine, config);
		} catch(Exception e) {
			throw new RuntimeException("Could not initialize UTD", e);
		}
	}
	
	public void saveEngine(UTDTranslationEngine engine) {
		throw new UnsupportedOperationException("Not finished");
	}
}
