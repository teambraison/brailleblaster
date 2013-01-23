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
			String fontPath = BBIni.getBrailleblasterPath() + "/programData/fonts/SimBraille.ttf";
			String platform = SWT.getPlatform();
			if (platform.equals("win32") || platform.equals("wpf")) {
				fontPath = BBIni.getBrailleblasterPath() + "\\programData\\fonts\\SimBraille.ttf";
			}
			if (!shell.getDisplay().loadFont(fontPath)) {
				new Notify(lh.localValue("fontNotLoadedBraille"));
			}
		}

		if (Courier) {
			daisyFont = new Font(shell.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
		} 
		else {
			String fontPath = BBIni.getBrailleblasterPath() + "/programData/fonts/" + altFont + ".ttf";
			String platform = SWT.getPlatform();
			if (platform.equals("win32") || platform.equals("wpf")) {
				fontPath = BBIni.getBrailleblasterPath() + "\\programData\\fonts\\" + altFont + ".ttf";
			}
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
				daisyFont = new Font(wp.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
			} else {
				daisyFont = new Font(wp.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
			}
			dm.getDaisyView().setFont(daisyFont);

			brailleFontHeight += brailleFontHeight / 4;
			if (displayBrailleFont) {
				simBrailleFont = new Font(wp.getDisplay(), "SimBraille", brailleFontHeight, SWT.NORMAL);
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
				daisyFont = new Font(wp.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
			} 
			else {
				daisyFont = new Font(wp.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
			}
			dm.getDaisyView().setFont(daisyFont);

			brailleFontHeight -= brailleFontHeight / 5;
			if (displayBrailleFont) {
				simBrailleFont = new Font(wp.getDisplay(), "SimBraille", brailleFontHeight, SWT.NORMAL);
				dm.getBrailleView().setFont(simBrailleFont);
			} 
			else {
				dm.getBrailleView().setFont(daisyFont);
			}
		}
	}
}
