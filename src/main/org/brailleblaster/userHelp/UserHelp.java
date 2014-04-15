/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.userHelp;

import org.brailleblaster.util.Notify;
import org.brailleblaster.BBIni;
import org.eclipse.swt.program.Program;

/**
* This class handles the items on the help menu.
*/
public class UserHelp {

	private String helpPath;
	//private Desktop desktop;

	/**
	 * constructor that accepts integer codes for help items
	 */
	public UserHelp (HelpOptions helpChoice) {
		helpPath = BBIni.getHelpDocsPath() + BBIni.getFileSep();
		
		switch (helpChoice) {
			case AboutBB:
				new Notify (BBIni.getProductName() + " version " + BBIni.getVersion() +  ", released on " + 
						BBIni.getReleaseDate() + ". "For more information or to report bugs visit http://www.brailleblaster.org.");
				break;
			case HelpInfo:
				showHelp ("helpinfo.html");
				break;
			case ReadTutorial:
				showHelp ("tutorial.html");
				break;
			case ReadManuals:
				showHelp ("manuals.html");
				break;
			case CheckUpdates:
				showHelp ("checkupdates.html");
			default:
				break;
		}
	}

	UserHelp (String url) {
		showHelp (url);
	}

	/**
	 * Display help documents in the local browser.
	 */
	private void showHelp (String fileName) {
		String us = "file:///" + helpPath.replace('\\','/') + fileName;
		String URIString = us.replace(" ", "%20");
		Program.launch(URIString);
	}
}

