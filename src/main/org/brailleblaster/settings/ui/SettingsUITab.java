/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brailleblaster.settings.ui;

import org.brailleblaster.utd.UTDTranslationEngine;

/**
 *
 * @author lblakey
 */
public interface SettingsUITab {
	/**
	 * Validate tab data
	 * @return NULL if no errors, i18n key of error message if there is an error
	 */
	public String validate();
	
	/**
	 * When all other tab validation has passed, update the engine
	 * @param engine
	 * @return True if values were changed and need to be saved, false if not
	 */
	public boolean updateEngine(UTDTranslationEngine engine);
}
