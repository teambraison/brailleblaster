package org.brailleblaster.settings.ui;

import org.brailleblaster.utd.UTDTranslationEngine;

/**
 *
 * @author lblakey
 */
interface SettingsUITab {
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
