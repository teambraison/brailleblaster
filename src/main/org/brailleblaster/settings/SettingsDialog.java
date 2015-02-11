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

package org.brailleblaster.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Group;
import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;
import org.brailleblaster.wordprocessor.WPManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import org.liblouis.LibLouis;
import org.liblouis.LibLouisUTDML;

/** This class works closely with classes in the louisutdml package to 
chose the correct liblouisutdml configuration file and other user 
preferences.
 */

public class SettingsDialog {
	private Shell shell;
	private Properties prop;
	private String userSettings;
	LocaleHandler lh = new LocaleHandler();
	//Reference the root logger in order to change its level
	private ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	public SettingsDialog() {
		loadProperties();
	}
	public void open() {
		Display display = WPManager.getDisplay();
		shell = new Shell(display, SWT.DIALOG_TRIM);
		final Shell dialog =
				new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setLayout(new GridLayout(2,  true));
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, true,2,1);
		dialog.setText(lh.localValue("settingsTitle"));
		
		final Button[] checkbox= new Button[2];
		checkbox[0] = new Button(dialog, SWT.CHECK);
		checkbox[0].setText(lh.localValue("settingsWelcome"));
		checkbox[0].setSelection(showWelcome());
		checkbox[0].setLayoutData(data);
		data = new GridData(SWT.FILL, SWT.BEGINNING, true, true, 2, 1);
		checkbox[1] = new Button(dialog, SWT.CHECK);
		checkbox[1].setText(lh.localValue("settingsTutorial"));
		checkbox[1].setLayoutData(data);
		
		if(getProperty("logLevel")==null){ //If the user settings properties file has no key for logLevel, make it
			setProperty("logLevel", "" + Level.ERROR_INT);
		}
		int logLevel = Integer.parseInt(getProperty("logLevel"));
		final Group logLevelGroup = new Group(dialog, SWT.SHADOW_IN);
		final Button logDebug = new Button(logLevelGroup, SWT.RADIO);
		logDebug.setSelection(logLevel==Level.DEBUG_INT);
		final Button logInfo = new Button(logLevelGroup, SWT.RADIO);
		logInfo.setSelection(logLevel==Level.INFO_INT);
		final Button logWarn = new Button(logLevelGroup, SWT.RADIO);
		logWarn.setSelection(logLevel==Level.WARN_INT);
		final Button logError = new Button(logLevelGroup, SWT.RADIO);
		logError.setSelection(logLevel==Level.ERROR_INT);
		logLevelGroup.setText("Log Level");
		logLevelGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
		data = new GridData(SWT.BEGINNING, SWT.FILL, true, true, 2, 1);
		logLevelGroup.setLayoutData(data);
		logDebug.setText("Debug");
		logInfo.setText("Info");
		logWarn.setText("Warning");
		logError.setText("Error (Recommended)");
		
		Button saveButton = new Button(dialog, SWT.PUSH);
		saveButton.setText(lh.localValue("&Save"));
		data = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);		
		data.widthHint = 100;
		saveButton.setLayoutData(data);
		saveButton.pack();
		saveButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String showWelcome; 
				if(checkbox[0].getSelection()) {
					showWelcome = "yes";
				}
				else {
					showWelcome = "no";
				}
				setProperty("showWelcome", showWelcome);	
				if(checkbox[1].getSelection()) {
					//show tutorial
					//new CallOutsideWP().showTutorial();
				}
				
				int newLevel;
				if(logDebug.getSelection()){
					newLevel = Level.DEBUG_INT;
				}
				else if(logInfo.getSelection()){
					newLevel = Level.INFO_INT;
				}
				else if(logWarn.getSelection()){
					newLevel = Level.WARN_INT;
				}
				else{
					newLevel = Level.ERROR_INT;
				}
				rootLogger.setLevel(Level.toLevel(newLevel));
				LibLouis.getInstance().setLogLevel(newLevel);
				setProperty("logLevel", "" + newLevel);
				storeProperty();
				dialog.close();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		data = new GridData(SWT.LEFT, SWT.BOTTOM, true, true, 1, 1);
		data.widthHint = 100;
		Button cancelButton = new Button(dialog, SWT.PUSH);
		cancelButton.setText(lh.localValue("&Cancel"));
		cancelButton.setLayoutData(data);
		cancelButton.pack();
		cancelButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dialog.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		dialog.pack(true);
		// Move the dialog to the center of the top level shell.
		Rectangle shellBounds = shell.getBounds();
		Point dialogSize = dialog.getSize();
		dialog.setLocation(
				shellBounds.x + (shellBounds.width - dialogSize.x) / 2,
				shellBounds.y + (shellBounds.height - dialogSize.y) / 2);
		
		dialog.open();
	}

	private void loadProperties() {
		userSettings= BBIni.getUserSettings();
		prop = new Properties();
		try {
			prop.load(new FileInputStream(userSettings));
		} catch (FileNotFoundException e) {
			new Notify(e.getMessage());
		} catch (IOException e) {
			new Notify(e.getMessage());
		}
	}

	public void setProperty(String key, String value) {
		prop.setProperty(key, value);
	}

	///////////////////////////////////////////////////////////
	// Searches our key/value pairs and returns the value 
	// associated with given key.
	public String getProperty(String key)
	{
		// Return the property!
		return prop.getProperty(key);
	}
	
	public void storeProperty() {
		//no change will be made to the properties file until this method is called
		try {
			prop.store(new FileOutputStream(userSettings), null);
		} catch (FileNotFoundException e) {
			new Notify(e.getMessage());
		} catch (IOException e) {
			new Notify(e.getMessage());
		}
	}

	public boolean showWelcome() {
		return prop.getProperty("showWelcome").equals("yes")? true:false; 
	}
}

