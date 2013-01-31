/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org
  *
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
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.wordprocessor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;

public class FontManager {

	private static Font simBrailleFont;
	private static Font daisyFont;
	static boolean SimBraille = false;
	static boolean Courier = false;
	static String altFont = "unibraille29";
	static String courierFont = "Courier New";
	static int daisyFontHeight = 10;
	static int brailleFontHeight = 14;
	static LocaleHandler lh = new LocaleHandler();
	static Logger logger = BBIni.getLogger();
	static boolean displayBrailleFont = false;
	
	public static void setShellFonts(Shell shell, DocumentManager dm){
		FontData[] fd = shell.getDisplay().getFontList(null, true);
		String fileSep = BBIni.getFileSep();
		String fn;

		for (int i = 0; i < fd.length; i++) {
			fn = fd[i].getName();
			if (fn.contentEquals("SimBraille")) {
				SimBraille = true;
				break;
			}
			if (fn.contentEquals(courierFont)) {
				Courier = true;
				break;
			}
		}

		if (!SimBraille) {
			String fontPath = BBIni.getProgramDataPath() + fileSep + "fonts" + fileSep + "SimBraille.ttf";
			if (!shell.getDisplay().loadFont(fontPath)) {
				new Notify(lh.localValue("fontNotLoadedBraille"));
			}
		}

		if (Courier) {
			daisyFont = new Font(shell.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
		} 
		else {
			String fontPath = BBIni.getProgramDataPath() + fileSep +  "fonts" + fileSep + altFont + ".ttf";
			if (!shell.getDisplay().loadFont(fontPath)) {
				new Notify(lh.localValue("fontNotLoadedText"));
			}
			daisyFont = new Font(shell.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
		}

		
		dm.daisy.view.setFont(daisyFont);
		dm.braille.view.setFont(daisyFont);
		dm.braille.view.setEditable(false);

		String loc = lh.getLocale().toString();
		if (!loc.contentEquals(lh.localValue("localeResource"))) {
			logger.log(Level.WARNING, "Locale resource for " + loc + " not found. Using default.");
			// System.err.println("Locale resource for '" + lh.getLocale().getDisplayName() + "' not found. Using default.");
			// new Notify("Locale resource for '" + lh.getLocale().getDisplayName() + "' not found.");
		}	
	}
	
	public static void increaseFont(WPManager wp, DocumentManager dm) {
		if (daisyFontHeight + (daisyFontHeight / 4) <= 48) {
			daisyFontHeight += daisyFontHeight / 4;
			if (Courier) {
				daisyFont = new Font(WPManager.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
			} else {
				daisyFont = new Font(WPManager.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
			}
			dm.getDaisyView().setFont(daisyFont);

			brailleFontHeight += brailleFontHeight / 4;
			if (displayBrailleFont) {
				simBrailleFont = new Font(WPManager.getDisplay(), "SimBraille", brailleFontHeight, SWT.NORMAL);
				dm.getBrailleView().setFont(simBrailleFont);
			} 
			else {
				dm.getBrailleView().setFont(daisyFont);
			}
		}
	}

	public static void decreaseFont(WPManager wp, DocumentManager dm) {	
		if (daisyFontHeight - (daisyFontHeight / 5) >= 8) {
			daisyFontHeight -= daisyFontHeight / 5;
			if (Courier) {
				daisyFont = new Font(WPManager.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
			} 
			else {
				daisyFont = new Font(WPManager.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
			}
			dm.getDaisyView().setFont(daisyFont);

			brailleFontHeight -= brailleFontHeight / 5;
			if (displayBrailleFont) {
				simBrailleFont = new Font(WPManager.getDisplay(), "SimBraille", brailleFontHeight, SWT.NORMAL);
				dm.getBrailleView().setFont(simBrailleFont);
			} 
			else {
				dm.getBrailleView().setFont(daisyFont);
			}
		}
	}
	
	void toggleBrailleFont(WPManager wp, DocumentManager dm) {
		if (displayBrailleFont) {
			displayBrailleFont = false;
		} else {
			displayBrailleFont = true;
		}
		setBrailleFont(wp, dm, displayBrailleFont);
	}

	void setBrailleFont(WPManager wp, DocumentManager dm, boolean toggle) {
		if (toggle) {
			simBrailleFont = new Font(WPManager.getDisplay(),
					"SimBraille", brailleFontHeight, SWT.NORMAL);
			dm.braille.view.setFont(simBrailleFont);

		} 
		else {
			if (Courier) {
				simBrailleFont = new Font(WPManager.getDisplay(),
						courierFont, daisyFontHeight, SWT.NORMAL);
			} else {
				simBrailleFont = new Font(WPManager.getDisplay(), altFont,
						daisyFontHeight, SWT.NORMAL);
			}
			dm.braille.view.setFont(simBrailleFont);
		}
	}	
}
