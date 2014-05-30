/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org www.aph.org
  *
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknowledged.
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

package org.brailleblaster.wordprocessor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;

public class FontManager {

	private Font simBrailleFont;
	private Font daisyFont;
	private boolean SimBraille = false;
	private boolean Courier = false;
	private String altFont = "unibraille29";
	private String courierFont = "Courier New";
	private int daisyFontHeight = 12;
	private int brailleFontHeight = 14;
	static LocaleHandler lh = new LocaleHandler();
	static Logger logger = BBIni.getLogger();
	private boolean displayBrailleFont = false;
	
	private Manager m;
	public FontManager(Manager m){
		this.m = m;
	}
	
	public void setFontWidth(boolean simBrailleDisplayed){	
		m.getText().setcharWidth();
		if(SimBraille && !simBrailleDisplayed){
			setSimBraille();
			m.getBraille().setcharWidth();
			
			setDaisyFont();
		}
		else {
			m.getBraille().setcharWidth();
		}
	}
	
	public void setShellFonts(Shell shell, boolean showSimBraille){
		displayBrailleFont = showSimBraille;
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
			else {
				SimBraille = true;
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

		m.getText().view.setFont(daisyFont);
		if(SimBraille && displayBrailleFont) {
			setBrailleFont(true);
		}
		else {
			setDaisyFont();
		}
		m.getBraille().view.setEditable(false);

		String loc = lh.getLocale().toString();
		if (!loc.contentEquals(lh.localValue("localeResource"))) {
			logger.log(Level.WARNING, "Locale resource for " + loc + " not found. Using default.");
			// new Notify("Locale resource for '" + lh.getLocale().getDisplayName() + "' not found.");
		}	
	}
	
	public void increaseFont() {
		if (daisyFontHeight + (daisyFontHeight / 4) <= 48) {
			daisyFontHeight += daisyFontHeight / 4;
			replaceDaisyFont();

			brailleFontHeight += brailleFontHeight / 4;
			if (displayBrailleFont && SimBraille) 
				replaceSimBraille();
			else 
				setDaisyFont();
		}
	}

	public void decreaseFont() {	
		if (daisyFontHeight - (daisyFontHeight / 5) >= 8) {
			daisyFontHeight -= daisyFontHeight / 5;
			replaceDaisyFont();
			
			brailleFontHeight -= brailleFontHeight / 5;
			if (displayBrailleFont && SimBraille) 
				replaceSimBraille();
			else 
				setDaisyFont();
		}
	}
	
	public void toggleBrailleFont(boolean showSimBraille) {
		displayBrailleFont = showSimBraille;
		setBrailleFont(showSimBraille);
	}

	private void setBrailleFont(boolean toggle) {
		if (toggle  && SimBraille)
			setSimBraille();
		else 
			setDaisyFont();
	}	
	
	public Font getFont(){
		return daisyFont;
	}
	
	private void setSimBraille(){
		if(simBrailleFont == null || simBrailleFont.isDisposed())
			simBrailleFont = new Font(WPManager.getDisplay(), "SimBraille", brailleFontHeight, SWT.NORMAL);
		
		m.getBraille().view.setFont(simBrailleFont);
	}
	
	private void replaceSimBraille(){
		Font temp = simBrailleFont;
		simBrailleFont = new Font(WPManager.getDisplay(), "SimBraille", brailleFontHeight, SWT.NORMAL);
		m.getBrailleView().setFont(simBrailleFont);
		temp.dispose();
	}
	
	private void replaceDaisyFont(){
		Font temp = daisyFont;
		if (Courier) {
			daisyFont = new Font(WPManager.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
		} 
		else {
			daisyFont = new Font(WPManager.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
		}
		m.getTextView().setFont(daisyFont);
		
		if (!(displayBrailleFont && SimBraille))
			setDaisyFont();
		
		temp.dispose();
	}
	
	private void setDaisyFont(){
		m.getBraille().view.setFont(daisyFont);
	}
	
	public void disposeFonts(){
		if(!simBrailleFont.isDisposed())
			simBrailleFont.dispose();
		
		if(!daisyFont.isDisposed())
			daisyFont.dispose();
	}
}