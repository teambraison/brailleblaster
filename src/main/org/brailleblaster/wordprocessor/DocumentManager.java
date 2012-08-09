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
 * Maintained by John J. Boyer john.boyer@abilitiessoft.com
 */

package org.brailleblaster.wordprocessor;

import org.eclipse.swt.widgets.*;

import org.eclipse.swt.custom.*;

import org.eclipse.swt.layout.*;
import org.brailleblaster.BBIni;
import org.eclipse.swt.printing.*;
import org.eclipse.swt.events.*;

import nu.xom.*;
import nu.xom.Text;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.liblouis.liblouisutdml;
import org.brailleblaster.util.Notify;
import java.io.File;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.printing.*;
import javax.print.PrintException;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Color;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.settings.Welcome;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.YesNoChoice;
// FO
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
//import org.apache.log4j.Level;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.apache.tika.sax.ToTextContentHandler;

import org.xml.sax.SAXException;

enum Encodings {
	ISO_8859_1("ISO-8859-1"), UTF_8("UTF-8"), WINDOWS_1252("WINDOWS-1252"), US_ASCII(
			"US-ASCII");

	private final String encoding;

	Encodings(String encoding) {
		this.encoding = encoding;
	}

	public String encoding() {
		return encoding;
	}
}

/**
 * This class manages each document in an MDI environment. It controls the
 * braille View and the daisy View.
 */
class DocumentManager {
	final Display display;
	final Shell documentWindow;
	final int documentNumber;
	final String docID;
	int action;
	int returnReason = 0;
	FormLayout layout;
	String documentName = null;
	boolean documentChanged = false;
	BBToolBar toolBar;
	BBMenu menu;
	RecentDocuments rd;
	UTD utd;
	AbstractView activeView;
	DaisyView daisy;
	BrailleView braille;
	BBStatusBar statusBar;
	boolean exitSelected = false;
	Document doc = null;
	NewDocument newDoc;
	String configFileList = null;
	static String tempPath = null;
	boolean haveOpenedFile = false;
	String translatedFileName = null;
	String brailleFileName = null; // FO
	String daisyWorkFile = null; // FO
	String tikaWorkFile = null; // FO
	String newDaisyFile = "utdml-doc"; // FO
	boolean textAndBraille = false;
	boolean saveUtdml = false;
	boolean metaContent = false;
	StyleManager sm; // separate styles from DM

	liblouisutdml louisutdml;
	String logFile = "Translate.log";
	String configSettings = null;
	int mode = 0;
	boolean finished = false;
	private volatile boolean stopRequested = false;
	static final boolean[] flags = new boolean[WPManager.getMaxNumDocs()];
	// static final String[] runningFiles = new
	// String[WPManager.getMaxNumDocs()];
	static String recentFileName = null;
	static int recentFileNameIndex = -1;
	int numLines;
	int numChars;
	// FO
	private Font simBrailleFont, daisyFont;
	boolean displayBrailleFont = false;
	int bvLineCount;
	private boolean firstTime;
	static int daisyFontHeight = 10;
	static int brailleFontHeight = 14;
	static boolean SimBraille = false;
	static boolean Courier = false;
	static String altFont = "unibraille29";
	static String courierFont = "Courier New";
	LocaleHandler lh = new LocaleHandler();
	StringBuilder brailleLine = new StringBuilder(8192);
	StringBuilder daisyLine = new StringBuilder(8192);
	// character encoding for import
	static String encoding = null;
	
	MediaType mediaType;

	/**
	 * Constructor that sets things up for a new document.
	 */
	DocumentManager(Display display, int documentNumber, int action,
			String documentName) {
		this.display = display;
		this.documentNumber = documentNumber;
		docID = new Integer(documentNumber).toString();
		this.action = action;
		this.documentName = documentName;
		tempPath = BBIni.getTempFilesPath() + BBIni.getFileSep();
		louisutdml = liblouisutdml.getInstance();
		documentWindow = new Shell(display, SWT.SHELL_TRIM);

		// FO setup WP window
		layout = new FormLayout();
		documentWindow.setLayout(layout);
		rd = new RecentDocuments(this);
		sm = new StyleManager(this);
		utd = new UTD(this);
		menu = new BBMenu(this);
		toolBar = new BBToolBar(this);
		/* text window is on the left */
		daisy = new DaisyView(documentWindow);
		braille = new BrailleView(documentWindow);

		// activeView = (ProtoView)daisy;
		statusBar = new BBStatusBar(documentWindow);
		documentWindow.setSize(1000, 700);
		documentWindow.layout(true, true);

		documentWindow.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				setReturn(WP.DocumentClosed);
				// this way clicking close box is equivalent to the 'close' item
				// on the menu
			}
		});

		documentWindow.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				handleShutdown(event);
			}
		});

		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = documentWindow.getBounds();
		int x = bounds.x + ((bounds.width - rect.width) / 2)
				+ (documentNumber * 30);
		int y = bounds.y + ((bounds.height - rect.height) / 2)
				+ (documentNumber * 30);
		documentWindow.setLocation(x, y);
		documentWindow.open();
		setWindowTitle(" untitled");

		// daisy.view.addModifyListener(daisyMod);
		/**
		 * for later use braille.view.addModifyListener(brailleMod);
		 **/
		// + FO
		/* Find out which scalable fonts are installed */
		FontData[] fd = documentWindow.getDisplay().getFontList(null, true);
		String fn;

		for (int i = 0; i < fd.length; i++) {
			fn = fd[i].getName();
			if (fn.contentEquals("SimBraille")) {
				SimBraille = true;
				break;
			}
			;
			if (fn.contentEquals(courierFont)) {
				Courier = true;
				break;
			}
			;
		}

		if (!SimBraille) {
			String fontPath = BBIni.getBrailleblasterPath()
					+ "/programData/fonts/SimBraille.ttf";
			String platform = SWT.getPlatform();
			if (platform.equals("win32") || platform.equals("wpf")) {
				fontPath = BBIni.getBrailleblasterPath()
						+ "\\programData\\fonts\\SimBraille.ttf";
			}
			if (!documentWindow.getDisplay().loadFont(fontPath)) {
				new Notify(lh.localValue("fontNotLoadedBraille"));
			}
		}
		;

		if (Courier) {
			daisyFont = new Font(documentWindow.getDisplay(), courierFont,
					daisyFontHeight, SWT.NORMAL);
		} else {

			String fontPath = BBIni.getBrailleblasterPath()
					+ "/programData/fonts/" + altFont + ".ttf";
			String platform = SWT.getPlatform();
			if (platform.equals("win32") || platform.equals("wpf")) {
				fontPath = BBIni.getBrailleblasterPath()
						+ "\\programData\\fonts\\" + altFont + ".ttf";
			}
			if (!documentWindow.getDisplay().loadFont(fontPath)) {
				new Notify(lh.localValue("fontNotLoadedText"));
			}
			daisyFont = new Font(documentWindow.getDisplay(), altFont,
					daisyFontHeight, SWT.NORMAL);
		}

		daisy.view.setFont(daisyFont);
		braille.view.setFont(daisyFont);
		braille.view.setEditable(false);

		if (documentNumber == 0) {
			new Welcome(); // This then calls the settings dialogs.
		}

		switch (action) {
		case WP.OpenDocumentGetFile:
			fileOpen();
			break;
		case WP.DocumentFromCommandLine:
			openDocument(documentName);
			break;
		case WP.OpenDocumentGetRecent:
			// FO 04
			String ext = getFileExt(documentName);
			if (ext.contentEquals("utd") || ext.contentEquals("xml")) {
				brailleFileName = getBrailleFileName();
				openDocument(documentName);
				if (ext.contentEquals("utd")) {
					BBIni.setUtd(true);
					utd.displayTranslatedFile(documentName, brailleFileName);
					braille.hasChanged = false;
				}
				
				daisy.hasChanged = false;

			} else {
				if (ext.contentEquals("brf")) {
					openBrf(documentName);
					setWindowTitle("untitled");
				} else {	
				    parseImport(documentName, getEncodingString());
					setWindowTitle(documentName);
				}
				braille.view.setEditable(false);
				daisy.hasChanged = true;
			}
			daisy.view.setFocus();
			break;

		case WP.ImportDocument:
			importDocument();
			break;
		}

		boolean stop = false;
		daisy.view.setFocus();

		while (!documentWindow.isDisposed() && (!stop) && (returnReason == 0)) {
			if (!display.readAndDispatch())
				display.sleep();
			for (boolean b : flags) {
				stop |= b;
			}
		}
		// get here if the window is disposed, or someone has a reason
		if (flags[documentNumber]) {
			WPManager.setCurDoc(documentNumber);
			flags[documentNumber] = false; // all should be false now
		}
		// Then back to WPManager
	}

	/**
	 * Handle application shutdown signal from OS;
	 */
	void handleShutdown(Event event) {
		if (daisy.hasChanged) {
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"));
			if (ync.result == SWT.YES) {
				fileSave();
			} else {
				daisy.hasChanged = false;
				braille.hasChanged = false;
			}
		}
		event.doit = true;
	}

	/**
	 * Clean up before closing the document.
	 */
	void finish() {
		documentWindow.dispose();
		finished = true;
	}

	/**
	 * Checks if a return request is valid and does any necessary processing.
	 */
	boolean setReturn(int reason) {
		switch (reason) {
		case WP.SwitchDocuments:
			if (WPManager.haveOtherDocuments()) {
				// System.out.println("Switching to next");
				returnReason = reason;
				flags[documentNumber] = true;// this fires the interrupt
				return true;
			}
			new Notify(lh.localValue("oneDocument"));
			return false;
		case WP.NewDocument:
			returnReason = reason;
			break;
		case WP.OpenDocumentGetFile:
			returnReason = reason;
			break;
		case WP.DocumentClosed:
			returnReason = reason;
			break;
		case WP.BBClosed:
			returnReason = reason;
			break;
		default:
			break;
		}
		WPManager.setCurDoc(documentNumber); // FO 27
		flags[documentNumber] = true;// this fires the interrupt
		return true;
	}

	/**
	 * This method is called to resume processing on this document after working
	 * on another.
	 */
	void resume() {
		if (documentWindow.isDisposed())
			return;
		documentWindow.forceActive();
		boolean stop = false;
		while (!documentWindow.isDisposed() && (!stop)) {
			if (!documentWindow.getDisplay().readAndDispatch())
				documentWindow.getDisplay().sleep();
			for (boolean b : DocumentManager.getflags()) {
				stop |= b;
			}
		}
	}

	private void setWindowTitle(String pathName) {
		int index = pathName.lastIndexOf(File.separatorChar);
		if (index == -1) {
			documentWindow.setText("BrailleBlaster " + pathName);
		} else {
			documentWindow.setText("BrailleBlaster "
					+ pathName.substring(index + 1));
		}
	}

	void fileDocument() {
		newDoc = new NewDocument();
		newDoc.fillOutBody(daisy.view);
		doc = newDoc.getDocument();
	}

	void fileNew() {
		// FO
		if (!daisy.view.isVisible()) {
			activateViews(true);
			activateMenus(true);
			daisy.hasChanged = false;
			braille.hasChanged = false;
			haveOpenedFile = false;
			brailleFileName = null;
			documentName = null;
			daisy.hasChanged = false;
			braille.hasChanged = false;
			doc = null;
			BBIni.setUtd(false);
			setWindowTitle(" untitled");
			daisy.view.setFocus();
		} else {

			// if (doc != null){
			returnReason = WP.NewDocument;
			flags[documentNumber] = true;
			// return;
			// }
		}
	}

	/* UTD or XML DOCUMENT */
	void fileOpen() {

		if (!daisy.view.isVisible()) {
			activateViews(true);
			activateMenus(true);
			daisy.hasChanged = false;
			braille.hasChanged = false;
			haveOpenedFile = false;
			brailleFileName = null;
			documentName = null;
			daisy.hasChanged = false;
			braille.hasChanged = false;
			doc = null;
			BBIni.setUtd(false);
			setWindowTitle(" untitled");
			daisy.view.setFocus();
		}
		;

		if ((doc != null) || daisy.hasChanged) {
			returnReason = WP.OpenDocumentGetFile;
			flags[documentNumber] = true;
			return;
		}
		haveOpenedFile = false;
		metaContent = false;

		Shell shell = new Shell(display, SWT.DIALOG_TRIM);
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		String filterPath = "/";
		String[] filterNames = new String[] { "UTDML", "XML" };
		String[] filterExtensions = new String[] { "*.utd", "*.xml" };

		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterPath = System.getProperty("user.home");
			if (filterPath == null)
				filterPath = "c:\\";
		}
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFilterPath(filterPath);

		documentName = dialog.open();
		shell.dispose();

		if (documentName != null) {
			openDocument(documentName);
			String ext = getFileExt(documentName);
			if (ext.contentEquals("utd")) {
				BBIni.setUtd(true);
				metaContent = true;
				// }
				haveOpenedFile = true;
				brailleFileName = getBrailleFileName();
				utd.displayTranslatedFile(documentName, brailleFileName);
			}
			braille.view.setEditable(false);
			daisy.hasChanged = false;
			braille.hasChanged = false;
			// daisy.view.addModifyListener(daisyMod);
			setWindowTitle(documentName);
			daisy.view.setFocus();
		}
	}

	void recentOpen(String path) {

		if (!daisy.view.isVisible()) {
			activateViews(true);
			activateMenus(true);
			daisy.hasChanged = false;
			braille.hasChanged = false;
			haveOpenedFile = false;
			brailleFileName = null;
			documentName = null;
			daisy.hasChanged = false;
			braille.hasChanged = false;
			doc = null;
			BBIni.setUtd(false);
		}

		if (doc != null) {
			// see if this recent document is already opened in current windows
			// set
			recentFileNameIndex = WPManager.isRunning(path);
			if (recentFileNameIndex != -1) {
				returnReason = WP.SwitchDocuments;
				flags[documentNumber] = true;
				return;
			}
			recentFileName = path;
			returnReason = WP.OpenDocumentGetRecent;
			flags[documentNumber] = true;
			return;
		}
		documentName = path;

		String ext = getFileExt(documentName);
		if (ext.contentEquals("utd") || ext.contentEquals("xml")) {
			brailleFileName = getBrailleFileName();
			openDocument(documentName);
			if (ext.contentEquals("utd")) {
				BBIni.setUtd(true);
				utd.displayTranslatedFile(documentName, brailleFileName);
				braille.hasChanged = false;
			}

			daisy.hasChanged = false;

		} else {
			if (ext.contentEquals("brf")) {
				openBrf(documentName);
				setWindowTitle("untitled");
			} else {	
			    parseImport(documentName, getEncodingString());
				setWindowTitle(documentName);
			} 
			braille.view.setEditable(false);
		}
		daisy.view.setFocus();
	}

	void openDocument(String fileName) {

		// daisy.view.removeModifyListener(daisyMod);

		Builder parser = new Builder();
		try {
			doc = parser.build(fileName);
		} catch (ParsingException e) {
			new Notify(lh.localValue("malformedDocument"));
			return;
		} catch (IOException e) {
			new Notify(lh.localValue("couldNotOpen") + " " + documentName);
			return;
		}
		// add this file to recentDocList
		if (! fileName.contains("-tempdoc.xml")) {
		    rd.addDocument(fileName);
		}
		if (getFileExt(documentName).contentEquals("brf")) { 
			setWindowTitle("untitled");
		} else {
		    setWindowTitle(documentName);
		    haveOpenedFile = true;
		}
		numLines = 0;
		numChars = 0;
		statusBar
		.setText(lh.localValue("loadingDocument") + " " + documentName);
		final Element rootElement = doc.getRootElement();// this needs to be
															// final, because it
															// will be used by a
															// different thread

		// new Thread() {
		// public void run() {
		while (!stopRequested) {
			walkTree(rootElement);
		}
		// }
		// }
		// .start();
		// daisy.view.addModifyListener(daisyMod);
	}

	void fileClose() {
		if (daisy.view == null) {
			System.err.println("fileCLose() - something wrong!!!");
			return;
		}
		activateMenus(false);
		if (daisy.hasChanged || braille.hasChanged) {
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"));
			if (ync.result == SWT.YES) {
				fileSave();
			}
		}
		activateViews(false);
		haveOpenedFile = false;
		brailleFileName = null;
		documentName = null;
		doc = null;
		BBIni.setUtd(false);
		stopRequested = false;
		statusBar.setText("");
		daisy.view.setText("");
		daisy.hasChanged = false;
		braille.view.setText("");
		braille.hasChanged = false;
		setWindowTitle(" untitled");
	}

	private void walkTree(Node node) {
		String ext = getFileExt(documentName);
		final Boolean isUtd = ext.contentEquals("utd");
		Node newNode;
		
		for (int i = 0; i < node.getChildCount(); i++) {
			newNode = node.getChild(i);

			if (newNode instanceof Element) {
				walkTree(newNode);
			} else if (newNode instanceof Text) {

				String nname = ((Element) node).getLocalName();
				if (!(nname.matches("span") || nname.matches("brl") || nname.matches("body") || nname.matches("title"))) {
					final String value;
					if (isUtd) {
						value = newNode.getValue() + "\n";
					} else {
						value = newNode.getValue();
					}
					numLines++;
					numChars += value.length();

					daisyLine.append(value);
				}
				
				// the main thread gets to execute the block inside syncExec()
				if (daisyLine.length() > 4096 || i == node.getChildCount() - 1) {
					display.syncExec(new Runnable() {
						public void run() {
							daisy.view.append(daisyLine.toString());
							statusBar.setText("Read " + numChars
									+ " characters.");
						}
					});
					daisyLine.delete(0, daisyLine.length());
				}
			}
		}
		stopRequested = true;
	}

	void openTikaDocument(String fileName) {

		// daisy.view.removeModifyListener(daisyMod);
		stopRequested = false;
		Builder parser = new Builder();
		try {
			doc = parser.build(fileName);

		} catch (ParsingException e) {
			new Notify("Tika: " + lh.localValue("malformedDocument"));
			e.getStackTrace();
			return;
		} catch (IOException e) {
			new Notify(lh.localValue("couldNotOpen") + " " + documentName);
			return;
		}

		numLines = 0;
		numChars = 0;
		statusBar
		.setText(lh.localValue("loadingDocument") + " " + documentName);
		final Element rootElement = doc.getRootElement();// this needs to be
															// final, because it
															// will be used by a
															// different thread

		while (!stopRequested) {
			walkTikaTree(rootElement);
		}
	}

	private void walkTikaTree(Node node) {
		Node newNode;
		numLines = 0;
		numChars = 0;

		String tags[] = { "title", "p", "i", "b", "u", "strong", "span" };

		for (int i = 0; i < node.getChildCount(); i++) {
			newNode = node.getChild(i);

			if (newNode instanceof Element) {
				walkTikaTree(newNode); // down one level
			} else if (newNode instanceof Text) {

				String nname = ((Element) node).getLocalName();
				Boolean match = false;
				int j = 0;
				while (!match && (j < tags.length)) {
					if (nname.matches(tags[j++]))
						match = true;
				}
				if (match) {
					String value = newNode.getValue() + "\n";

					// replace Unicode with matching codepoints
					Matcher matcher = Pattern.compile("\\\\u((?i)[0-9a-f]{4})")
							.matcher(value);
					StringBuffer sb = new StringBuffer();
					int codepoint;
					while (matcher.find()) {
						codepoint = Integer.valueOf(matcher.group(1), 16);
						matcher.appendReplacement(sb,
								String.valueOf((char) codepoint));
					}
					matcher.appendTail(sb);
					value = sb.toString();

					numLines++;
					numChars += value.length();

					daisyLine.append(value);
				}
				;
				// the main thread gets to execute the block inside syncExec()
				if (daisyLine.length() > 4096 || i == node.getChildCount() - 1) {
					display.syncExec(new Runnable() {
						public void run() {
							daisy.view.append(daisyLine.toString());
							statusBar.setText("Read "
									+ daisy.view.getCharCount()
									+ " characters.");
						}
					});
					daisyLine.delete(0, daisyLine.length());
				}
			}
		}
		stopRequested = true;
	}

	void fileSave() {
		if (!(daisy.hasChanged || braille.hasChanged)) {
			new Notify(lh.localValue("noChange"));
			return;
		}
		;

		String ext = "";
		if (documentName != null)
			ext = getFileExt(documentName);

		/** no open file then do a Save As **/
		if ((!haveOpenedFile) || ext.contentEquals("xml")) {
			fileSaveAs();
		} else {
			saveDaisyWorkFile();
			String fileName = new File(documentName).getName();
			statusBar.setText(lh.localValue("savingFile") + " " + fileName);

			/* save utdml file */
			if (!metaContent) {
				System.out.println(fileName + " "
						+ lh.localValue("saveTextOnly"));
				new FileUtils().copyFile(daisyWorkFile, documentName);
				statusBar.setText(lh.localValue("fileSaved"));
				new Notify(lh.localValue("fileSaved"));
			} else if (translatedFileName != null) {
				YesNoChoice ync = new YesNoChoice(
						lh.localValue("confirmTranslationSaved"));
				if (ync.result == SWT.YES) {
					// System.out.println(fileName + " " +
					// lh.localValue("saveTextBraille"));
					new FileUtils().copyFile(translatedFileName, documentName);
					statusBar.setText(lh.localValue("fileSaved"));
					new Notify(lh.localValue("fileSaved"));
					daisy.hasChanged = false;
					braille.hasChanged = false;
				} else {
					new Notify(lh.localValue("noChangeSaved"));
				}

			} else {
				fileSaveAs();
			}
		}
	}

	void fileSaveAs() {
		// FO
		if (daisyWorkFile == null) {
			/* create the utd text file */
			saveDaisyWorkFile();
		}

		textAndBraille = false;
		saveUtdml = false;

		/* dialog asking for the type of UTDML file to save */

		if (braille.view.getCharCount() != 0) {
			SaveOptionsDialog saveChoice = new SaveOptionsDialog(documentWindow);
			saveChoice.setText(lh.localValue("optionSelect"));
			SaveSelection result = saveChoice.open();
			if (result.equals(SaveSelection.TEXT_AND_BRAILLE)) {
				textAndBraille = true;
			} else if (result.equals(SaveSelection.CANCELLED)) {
				return;
			}
			saveUtdml = true;
		}

		Shell shell = new Shell(display);
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String filterPath = System.getProperty("user.home");
		// FO 04
		String[] filterNames = null;
		String[] filterExtensions = null;
		if (textAndBraille) {
			filterNames = new String[] { "UTDML file" };
			filterExtensions = new String[] { "*.utd" };
		} else {
			filterNames = new String[] { "XML file" };
			filterExtensions = new String[] { "*.xml" };
		}

		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			if (filterPath == null)
				filterPath = "c:\\";
		}
		dialog.setFilterPath(filterPath);
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);

		if (haveOpenedFile) {
			int i = documentName.lastIndexOf(".");
			String fn = documentName.substring(0, i);
			if (getFileExt(documentName).contentEquals("xml")) {
				if (textAndBraille)
					documentName = fn + ".utd";
				else
					documentName = fn + ".xml";
			}
			dialog.setFileName(documentName);
		} else {
			if (documentName != null) {
			  if (getFileExt(documentName).contentEquals("brf")) {
				saveDaisyWorkFile();
				int i = documentName.lastIndexOf(".");
				String fn = documentName.substring(0, i);
				newDaisyFile = fn + ".xml";
			  }
			}
			dialog.setFileName(newDaisyFile);
		}

		String saveTo = dialog.open();
		shell.dispose();
		if (saveTo == null) {
			return;
		}

		String fileName = new File(saveTo).getName();
		statusBar.setText(lh.localValue("savingFile") + " " + fileName);
		
		/* text and braille utd */
		if (textAndBraille) {
			if (translatedFileName == null) {
				new Notify(lh.localValue("noXlation"));
				return;
			}

			new FileUtils().copyFile(translatedFileName, saveTo);

		} else {
			new FileUtils().copyFile(daisyWorkFile, saveTo);
		}

		// add this file to recentDocList
		rd.addDocument(saveTo);
		statusBar.setText(lh.localValue("fileSaved") + " " + saveTo);
		documentWindow.setText("BrailleBlaster " + fileName);

		
		if (!textAndBraille) {
			translatedFileName = null;
			braille.view.replaceTextRange(0, braille.view.getCharCount(), "");
		}
		daisy.hasChanged = false;
		braille.hasChanged = false;
	}

	void showBraille() {

		BufferedReader translation = null;
		try {
			translation = new BufferedReader(new FileReader(translatedFileName));
		} catch (FileNotFoundException e) {
			new Notify(lh.localValue("couldNotFindFile") + " "
					+ translatedFileName);
		}
		numLines = 0;
		numChars = 0;
		// FO
		firstTime = true;
		boolean eof = false;
		String line;

		while (!eof) {
			try {
				line = translation.readLine();
			} catch (IOException e) {
				new Notify(lh.localValue("problemReading") + " "
						+ translatedFileName);
				return;
			}

			if (line == null) {
				eof = true;
			} else {
				brailleLine.append(line);
				brailleLine.append('\n');
				numChars += line.length();
				numLines++;
			}

			if ((brailleLine.length() > 4096) || (eof)) {
				display.syncExec(new Runnable() {

					public void run() {
						// FO
						/*
						 * Make sure we replace the braille view with the
						 * content of the file
						 */
						if (firstTime) {
							braille.view.replaceTextRange(0,
									braille.view.getCharCount(),
									brailleLine.toString());
							firstTime = false;
						} else {
							braille.view.append(brailleLine.toString());
						}

						brailleLine.delete(0, brailleLine.length());
						statusBar.setText(lh.localValue("textTranslated") + " "
								+ numLines + " " + lh.localValue("textLines")
								+ ", " + numChars + " "
								+ lh.localValue("textCharacters"));
					}
				});
			}
		} // end while

		try {
			translation.close();
		} catch (IOException e) {
			new Notify(lh.localValue("problemReading") + " "
					+ translatedFileName);
		}
	}

	void saveDaisyWorkFile() {

		createWorkDocument();

		if (doc == null) {
			new Notify(lh.localValue("noOpenFile"));
			return;
		}
		/* UTD text file */
		daisyWorkFile = tempPath + docID + "-tempdoc.xml";
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(daisyWorkFile);
		} catch (FileNotFoundException e) {
			new Notify(lh.localValue("cannotOpenFileW") + " " + daisyWorkFile);
			return;
		}
		Serializer outputDoc = new Serializer(writer);
		try {
			outputDoc.write(doc);
			outputDoc.flush();
		} catch (IOException e) {
			new Notify(lh.localValue("cannotWriteFile"));
			return;
		}
	}

	void createWorkDocument() {
		newDoc = new NewDocument();
		newDoc.fillOutBody(daisy.view);
		doc = newDoc.getDocument();
	}

	void translate(boolean display) {

		if (daisy.view.getCharCount() == 0) {
			return;
		}

		saveDaisyWorkFile();

		if (!BBIni.useUtd()) {
			YesNoChoice ync = new YesNoChoice(lh.localValue("askUtdml"));
			if (ync.result == SWT.YES) {
				BBIni.setUtd(true);
			}
		}

		configFileList = "preferences.cfg";
		configSettings = null;
		if (BBIni.useUtd()) {
			configSettings = "formatFor utd\n" + "mode notUC\n";
		}

		translatedFileName = getTranslatedFileName();

		boolean result = louisutdml.translateFile(configFileList,
				daisyWorkFile, translatedFileName, logFile, configSettings,
				mode);

		if (!result) {
			new Notify(lh.localValue("translationFailed"));
			return;
		}

		if (!BBIni.useUtd()) {
			brailleFileName = getBrailleFileName();
			new FileUtils().copyFile(translatedFileName, brailleFileName);
		}

		metaContent = true;
		braille.hasChanged = true;

		if (display) {
			if (BBIni.useUtd()) {
				brailleFileName = getBrailleFileName();
				utd.displayTranslatedFile(translatedFileName, brailleFileName);
			} else {
				new Thread() {
					public void run() {
						showBraille();
					}
				}.start();
			}
		}
	}

	String getTranslatedFileName() {
		String s = tempPath + docID + "-doc.brl";
		return s;
	}

	String getBrailleFileName() {
		String s = tempPath + docID + "-doc-brl.brl";
		return s;
	}

	String getTikaTranslatedFileName() {
		String s = tempPath + docID + "-doc.html";
		return s;
	}

	int getCount() {
		return documentNumber;
	}

	void fileEmbossNow() {
		if ((brailleFileName == null) || (braille.view.getCharCount() == 0)) {
			new Notify(lh.localValue("noXlationEmb"));
			return;
		}
		Shell shell = new Shell(display, SWT.DIALOG_TRIM);
		PrintDialog embosser = new PrintDialog(shell);
		PrinterData data = embosser.open();
		shell.dispose();
		if (data == null || data.equals("")) {
			return;
		}
		File translatedFile = new File(brailleFileName);
		PrinterDevice embosserDevice;
		try {
			embosserDevice = new PrinterDevice(data.name, true);
			embosserDevice.transmit(translatedFile);
		} catch (PrintException e) {
			new Notify(lh.localValue("cannotEmboss") + ": " + data.name);
		}
	}

	void toggleBrailleFont() {
		if (displayBrailleFont) {
			displayBrailleFont = false;
		} else {
			displayBrailleFont = true;
		}
		setBrailleFont(displayBrailleFont);
	}

	void setBrailleFont(boolean toggle) {
		if (toggle) {
			simBrailleFont = new Font(documentWindow.getDisplay(),
					"SimBraille", brailleFontHeight, SWT.NORMAL);
			braille.view.setFont(simBrailleFont);

		} else {
			// FontData[] fd =
			// (documentWindow.getDisplay().getSystemFont()).getFontData();
			if (Courier) {
				simBrailleFont = new Font(documentWindow.getDisplay(),
						courierFont, daisyFontHeight, SWT.NORMAL);
			} else {
				simBrailleFont = new Font(documentWindow.getDisplay(), altFont,
						daisyFontHeight, SWT.NORMAL);
			}
			braille.view.setFont(simBrailleFont);
		}
	}

	void increaseFont() {

		daisyFontHeight += daisyFontHeight / 4;

		if (daisyFontHeight >= 48) {
			return;
		}

		if (Courier) {
			daisyFont = new Font(documentWindow.getDisplay(), courierFont,
					daisyFontHeight, SWT.NORMAL);
		} else {
			daisyFont = new Font(documentWindow.getDisplay(), altFont,
					daisyFontHeight, SWT.NORMAL);
		}
		daisy.view.setFont(daisyFont);

		brailleFontHeight += brailleFontHeight / 4;
		if (displayBrailleFont) {
			simBrailleFont = new Font(documentWindow.getDisplay(),
					"SimBraille", brailleFontHeight, SWT.NORMAL);
			braille.view.setFont(simBrailleFont);
		} else {
			braille.view.setFont(daisyFont);
		}
	}

	void decreaseFont() {

		daisyFontHeight -= daisyFontHeight / 5;
		if (daisyFontHeight <= 8) {
			return;
		}

		if (Courier) {
			daisyFont = new Font(documentWindow.getDisplay(), courierFont,
					daisyFontHeight, SWT.NORMAL);
		} else {
			daisyFont = new Font(documentWindow.getDisplay(), altFont,
					daisyFontHeight, SWT.NORMAL);
		}
		daisy.view.setFont(daisyFont);

		brailleFontHeight -= brailleFontHeight / 5;
		if (displayBrailleFont) {
			simBrailleFont = new Font(documentWindow.getDisplay(),
					"SimBraille", brailleFontHeight, SWT.NORMAL);
			braille.view.setFont(simBrailleFont);
		} else {
			braille.view.setFont(daisyFont);
		}
	}

	void importDocument() {

		if (!daisy.view.isVisible()) {
			activateViews(true);
			activateMenus(true);
			daisy.hasChanged = false;
			braille.hasChanged = false;
			haveOpenedFile = false;
			brailleFileName = null;
			documentName = null;
			daisy.hasChanged = false;
			doc = null;
			BBIni.setUtd(false);
			setWindowTitle(" untitled");
			daisy.view.setFocus();
		}

		if ((doc != null) || daisy.hasChanged) {
			returnReason = WP.ImportDocument;
			flags[documentNumber] = true;
			return;
		}

		haveOpenedFile = false;
		metaContent = false;

		Shell shell = new Shell(display, SWT.DIALOG_TRIM);
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		String filterPath = System.getProperty("user.home");
		String[] filterNames = new String[] { "ASCII Text", "Word Doc", "Word Docx",
				"Rich Text Format", "Braille BRF", "OpenOffice ODT", "PDF", "XML", 
				"All Files (*)" };
		String[] filterExtensions = new String[] { "*.txt", "*.doc", "*.docx", 
				"*.rtf", "*.brf", "*.odt", "*.pdf", "*.xml", "*" };

		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterNames[filterNames.length-1] = "All Files (*.*)";
			filterExtensions[filterExtensions.length-1] = "*.*";
			filterPath = System.getProperty("user.home");
			if (filterPath == null)
				filterPath = "c:\\";
		}
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFilterPath(filterPath);

		/* turn off change tracking */
		// daisy.view.removeModifyListener(daisyMod);

		documentName = dialog.open();
		shell.dispose();

		if (documentName == null)
			return;

		// add this file to recentDocList
		rd.addDocument(documentName);

		// special case
		if (getFileExt(documentName).contentEquals("brf")) {
			openBrf(documentName);
			setWindowTitle("untitled");
		} else {
			parseImport(documentName, getEncodingString());
			setWindowTitle(documentName);
		}
		braille.view.setEditable(false);
		daisy.hasChanged = true;
		daisy.view.setFocus();
	}

	void parseImport(String fileName, String encoding) {
		/** Tika extract **/
		try {
			useTikaParser(fileName, encoding);
		} catch (Exception e) {
			System.out.println("Error importing: " + fileName);
			return;
		}

		openTikaDocument(tikaWorkFile);
	}

	/** Tika importer that creates a HTML formatted file **/
	void useTikaParser(String docName, String encoding) throws Exception {

		String fn = new File(docName).getName();
		int dot = fn.lastIndexOf(".");

		tikaWorkFile = tempPath + docID + "-" + fn.substring(0, dot) + ".html";

		File workFile = new File(tikaWorkFile);
		try {
			workFile.createNewFile();
		} catch (IOException e) {
			System.out
					.println("useTikaHtmlParser: Error creating Tika workfile "
							+ e);
			return;
		}

		InputStream stream = TikaInputStream.get(new File(docName));

		Metadata metadata = new Metadata();
		metadata.add("Content-Encoding", encoding);

		ParseContext context = new ParseContext();

		Parser parser = new AutoDetectParser();

		OutputStream output = new FileOutputStream(workFile);

		try {
			ToXMLContentHandler handler = new ToXMLContentHandler(output,
					encoding);
			parser.parse(stream, handler, metadata, context);
		} catch (IOException e) {
			System.err.println("useTikaHtmlParser IOException: "
					+ e.getMessage());
		} catch (SAXException e) {
			System.err.println("useTikaHtmlParser SAXException: "
					+ e.getMessage());
		} catch (TikaException e) {
			System.err.println("useTikaHtmlParser TikaException: "
					+ e.getMessage());
		} finally {
			stream.close();
			output.close();
		}
	}

	void brailleSave() {

		if ((braille.view.getCharCount() == 0) || brailleFileName == null) {
			new Notify(lh.localValue("noXlation"));
			return;
		}

		Shell shell = new Shell(display);
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String filterPath = System.getProperty("user.home");
		// FO 04
		String[] filterNames = new String[] { "BRF file" };
		String[] filterExtensions = new String[] { "*.brf" };

		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			if (filterPath == null)
				filterPath = "c:\\";
		}
		dialog.setFilterPath(filterPath);
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);

		if (haveOpenedFile) {
			int i = documentName.lastIndexOf(".");
			String fn = documentName.substring(0, i);
			documentName = fn + ".brf";
		} else {
			documentName = "document-" + docID + ".brf";
		}
		dialog.setFileName(documentName);

		String saveTo = dialog.open();
		shell.dispose();
		if (saveTo == null) {
			return;
		}

		String fileName = new File(saveTo).getName();
		statusBar.setText(lh.localValue("savingFile") + " " + fileName);

		new FileUtils().copyFile(brailleFileName, saveTo);
	}

	// plain BRF file to back-translate
	void openBrf(String fileName) {
		translatedFileName = getBrailleFileName();
		new FileUtils().copyFile (fileName, translatedFileName);
		showBraille();
		
		// backtranslate

		BBIni.setUtd(false) ;
		configFileList = "backtranslate.cfg";
		configSettings = "backFormat html\n" + "mode notUc\n" ;

		daisyWorkFile = tempPath + docID + "-tempdoc.xml";
		
		boolean result = louisutdml.backTranslateFile(configFileList,
				fileName, daisyWorkFile, logFile, configSettings,
				mode);

		if (!result) {
			new Notify(lh.localValue("translationFailed"));
			return;
		}
		
		openDocument(daisyWorkFile);
		haveOpenedFile = false;
	}

	void showDaisy(String content) {
		/* Make sure we replace the existing view with the content of the file */
		daisy.view.replaceTextRange(0, daisy.view.getCharCount(), content
				+ "\n");
	}

	void placeholder() {
		new Notify(lh.localValue("funcNotAvail"));
	}

	boolean isFinished() {
		return finished;
	}

	void recentDocuments() {
		rd.open();
	}

	// 5/3
	void switchDocuments() {

	}

	static boolean[] getflags() {
		return flags;
	}

	static void setflags(int i, boolean b) {
		flags[i] = b;
	}

	static void printflags() {
		for (boolean b : flags)
			System.out.print(b + ", ");
	}

	static String getRecentFileName() {
		return recentFileName;
	}

	boolean getDaisyHasChanged() {
		return daisy.hasChanged;
	}

	private String getFileExt(String fileName) {
		String ext = "";
		String fn = fileName.toLowerCase();
		int dot = fn.lastIndexOf(".");
		if (dot > 0) {
			ext = fn.substring(dot + 1);
		}
		return ext;
	}

	void activateViews(boolean state) {
		activateDaisyView(state);
		activateBrailleView(state);
	}

	void activateDaisyView(boolean active) {
		if (active) {
			daisy.view.setVisible(true);
		} else {
			daisy.view.setVisible(false);
		}
	}

	void activateBrailleView(boolean active) {
		if (active) {
			braille.view.setVisible(true);
		} else {
			braille.view.setVisible(false);
		}
	}

	void activateMenus(boolean state) {
		String itemToFind[] = { "Close", "Save", "Emboss Now", "Print",
				"Translate" };
		Menu mb = documentWindow.getMenuBar();
		MenuItem mi[] = mb.getItems();
		String t;
		int i;
		for (i = 0; i < mi.length; i++) {
			t = mi[i].getText().replace("&", "");
			if (t.contains("File")) {
				break;
			}
		}
		Menu f = mi[i].getMenu();
		MenuItem ii[] = f.getItems();
		for (i = 0; i < ii.length; i++) {
			t = ii[i].toString().replaceAll("&", "");
			for (int j = 0; j < itemToFind.length; j++) {
				if (t.contains(itemToFind[j])) {
					ii[i].setEnabled(state);
				}
			}
		}
		Control tb[] = documentWindow.getChildren();
		for (i = 0; i < tb.length; i++) {
			t = tb[i].toString().replace("&", "");
			if (t.contains("ToolBar")) {
				break;
			}
		}
		ToolBar ttb = (ToolBar) tb[i];
		ToolItem ti[] = ttb.getItems();
		for (i = 0; i < ti.length; i++) {
			for (int j = 0; j < itemToFind.length; j++) {
				if (ti[i].getText().contains(itemToFind[j])) {
					ti[i].setEnabled(state);
				}
			}
		}
		String vb = lh.localValue("viewBraille");
		for (i = 0; i < tb.length; i++) {
			t = tb[i].toString().replace("&", "");
			if (t.contains(vb)) {
				tb[i].setEnabled(state);
				break;
			}
		}
		if (tb[i] instanceof Button) {
			Button b = (Button) tb[i];
			b.setSelection(false);
			displayBrailleFont = false;
			setBrailleFont(displayBrailleFont);
		}
	}

	StyleManager getStyleManager() {
		return sm;
	}

	// encoding for Import
	public String getEncodingString() {

		encoding = null;

		// final Shell selShell = new Shell(display, SWT.DIALOG_TRIM |
		// SWT.APPLICATION_MODAL | SWT.CENTER);
		final Shell selShell = new Shell(display, SWT.DIALOG_TRIM | SWT.CENTER);
		selShell.setText(lh.localValue("specifyEncoding"));
		selShell.setMinimumSize(250, 100);

		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.marginWidth = 8;
		selShell.setLayout(layout);
		Composite radioGroup = new Composite(selShell, SWT.NONE);
		radioGroup.setLayout(new RowLayout(SWT.VERTICAL));

		Button b1 = new Button(radioGroup, SWT.RADIO);
		b1.setText(lh.localValue("encodeUTF8"));
		b1.setSelection(true); // default
		b1.pack();

		Button b2 = new Button(radioGroup, SWT.RADIO);
		b2.setText(lh.localValue("encodeISO88591"));
		b2.pack();

		Button b3 = new Button(radioGroup, SWT.RADIO);
		b3.setText(lh.localValue("encodeWINDOWS1252"));
		b3.pack();

		Button b4 = new Button(radioGroup, SWT.RADIO);
		b4.setText(lh.localValue("encodeUSASCII"));
		b4.pack();

		// if (SWT.getPlatform().equals("win32") ||
		// SWT.getPlatform().equals("wpf")) {
		// b1.setSelection(false);
		// b3.setSelection(true);
		// }

		radioGroup.pack();

		Composite c = new Composite(selShell, SWT.NONE);
		RowLayout clayout = new RowLayout();
		clayout.type = SWT.HORIZONTAL;
		// clayout.spacing = 30;
		// clayout.marginWidth = 8;
		clayout.marginTop = 20;
		clayout.center = true;
		clayout.pack = true;
		clayout.justify = true;
		c.setLayout(clayout);

		Button bsel = new Button(c, SWT.PUSH);
		bsel.setText(lh.localValue("encodeSelect"));
		bsel.pack();

		c.pack();

		Control tabList[] = new Control[] { radioGroup, c, radioGroup };
		try {
			selShell.setTabList(tabList);
		} catch (IllegalArgumentException e) {
			System.err.println("setTabList exception " + e.getMessage());
		}

		selShell.setDefaultButton(bsel);

		selShell.pack();

		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = selShell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		selShell.setLocation(x, y);

		b1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				encoding = Encodings.UTF_8.encoding();
			}
		});

		b2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				encoding = Encodings.ISO_8859_1.encoding();
			}
		});

		b3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				encoding = Encodings.WINDOWS_1252.encoding();
			}
		});

		b4.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				encoding = Encodings.US_ASCII.encoding();
			}
		});

		/* Select */
		bsel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selShell.dispose();
			}
		});

		selShell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false; // must pick a value
			}
		});

		selShell.open();

		while (!selShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
			// nothing clicked
			if (encoding == null) {
				if (b1.getSelection())
					encoding = Encodings.UTF_8.encoding();
				else if (b2.getSelection())
					encoding = Encodings.ISO_8859_1.encoding();
				else if (b3.getSelection())
					encoding = Encodings.WINDOWS_1252.encoding();
				else if (b4.getSelection())
					encoding = Encodings.US_ASCII.encoding();
			}
			;
		}
		return encoding;
	}
}
