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
 * this program; see the file LICENSE.txt
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by John J. Boyer john.boyer@abilitiessoft.com
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Dialog;
import org.brailleblaster.BBIni;
import org.eclipse.swt.widgets.MessageBox;
import org.brailleblaster.util.Notify;

/** This class works closely with classes in the louisutdml package to 
chose the correct liblouisutsml configuration file and other user 
preferences.
 */

public class SettingsDialog {
	private Shell shell;
	private Properties prop;
	private String userSettings;

	public SettingsDialog(){
		loadProperty();
	}
	public void open(){
		Display display = BBIni.getDisplay();
		shell = new Shell(display, SWT.DIALOG_TRIM);
		final Shell dialog =
				new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setLayout(new GridLayout(1,  true));
		dialog.setText("User's settings");
		final Button[] checkbox= new Button[2];
		checkbox[0] = new Button(dialog, SWT.CHECK);
		checkbox[0].setText(
		"Show welcome screen and this dialogue every time the program starts");
		checkbox[0].setSelection(showWelcome());
		checkbox[1] = new Button(dialog, SWT.CHECK);
		checkbox[1].setText("Read tutorial when finished settings");
		Button saveButton = new Button(dialog, SWT.PUSH);
		saveButton.setText("Save");
		GridData data = new GridData(SWT.END, SWT.BEGINNING, true, true);
		data.widthHint = 130;
		saveButton.setLayoutData(data);
		saveButton.pack();
		saveButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				String showWelcome; 
				if(checkbox[0].getSelection()){
					showWelcome = "yes";
				}
				else {
					showWelcome = "no";
				}
				setProperty("showWelcome", showWelcome);
				storeProperty();
				if(checkbox[1].getSelection()){
					//show tutorial
				}
				dialog.close();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		dialog.pack();
		dialog.open();
		// Move the dialog to the center of the top level shell.
//		Rectangle shellBounds = shell.getBounds();
//		Point dialogSize = dialog.getSize();
//		dialog.setLocation(
//				shellBounds.x + (shellBounds.width - dialogSize.x) / 2,
//				shellBounds.y + (shellBounds.height - dialogSize.y) / 2);
	}

	private void loadProperty(){
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

	public void setProperty(String key, String value){
		prop.setProperty(key, value);
	}

	public void storeProperty(){
		//no change will be made to the properties file until this method is called
		try {
			prop.store(new FileOutputStream(userSettings), null);
		} catch (FileNotFoundException e) {
			new Notify(e.getMessage());
		} catch (IOException e) {
			new Notify(e.getMessage());
		}
	}

	public boolean showWelcome(){
		return prop.getProperty("showWelcome").equals("yes")? true:false; 
	}
}

