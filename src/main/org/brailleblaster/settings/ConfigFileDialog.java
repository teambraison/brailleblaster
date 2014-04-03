// Chuck M. - Displays a dialogue that allows one to edit configuration files for BrailleBlaster.

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

package org.brailleblaster.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



public class ConfigFileDialog extends Dialog {
	
	// Dialog stuff.
	static Display display;
	static Shell configShell;
	LocaleHandler lh = new LocaleHandler();

	
	// Word Processor Manager.
	WPManager dm;
	
	// UI.
	Button defaultCfgChk;
	Button apply;
	Button okayBtn;
	Button saveAsBtn;
	Button cancelBtn;
	Button restoreDefaultBtn;
	
	Text txt;
	Combo fileNameCombo;
	Combo variableCombo;
	Combo valueCombo;

	// File Utility.
	FileUtils fu = new FileUtils();
	
	// List of config files.
	ArrayList<ConfigFile> fileList;
	
	// List of settings/ranges of values in a config file.
	ArrayList<String> configSettingsList;
	
	// User properties for BrailleBlaster
	Properties props;
	// The default config file.
	String defaultCfgFileName = null;
	
	/////////////////////////////////////////////////////////
	// Constructor.
	public ConfigFileDialog(Shell parent, int style, WPManager fdm) {
		
		// Set up dialog.
		super(parent, style);
		
		// Create shell, get display, etc.
		dm = fdm;
		display = WPManager.getDisplay();
		Display display = parent.getDisplay();
		configShell = new Shell(parent, SWT.DIALOG_TRIM);
		configShell.setText(lh.localValue("editTranslationTemplates"));
		
		fileList = new ArrayList<ConfigFile>();
		
		// Init and load properties.
		props = new Properties();
		try
		{
			// Load it!
			props.load(new FileInputStream(BBIni.getUserSettings()));
		}
		catch (IOException e) { e.printStackTrace(); } 
		
		// Create user interface.
		createUIelements();
		
		// Load config file settings. Variable ranges and such.
		loadConfigFileSettings();
		// Load all config files.
		loadConfigFiles();
		
		// Fill filename combo box with loaded names.
		fillFilenameComboBox();
		
		// Go through all of the config files and select the one that is the default.
		defaultCfgFileName = BBIni.getDefaultConfigFile();
		for(int curFN = 0; curFN < fileNameCombo.getItemCount(); curFN++)
		{
			// If this item in the combobox is the default, stop and update our UI.
			if( defaultCfgFileName.compareTo(fileNameCombo.getItem(curFN)) == 0 )
			{
				// Set current combo selection.
				fileNameCombo.select(curFN);
				
				// Check our default config checkbox.
				defaultCfgChk.setSelection(true);
				
				// Fill and select our other combos.
				fillVariableComboBox(fileList.get(curFN));
				fillValueComboBox(variableCombo.getItem(0));
				selectValueComboBox(txt.getText());
				
			} // if( defaultCfgFileName...
			
		} // for(int curFN = 0; curFN...
		
		// show the SWT window
		configShell.pack();
		// Resize window.
		configShell.setSize(200, 370);
		configShell.open();
		while (!configShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		// tear down the SWT window
		configShell.dispose();
	}
	
	///////////////////////////////////////////////////////////
	// Create UI elements(buttons, combos, etc.)
	public void createUIelements()
	{
		// Layout.
		FormLayout layout = new FormLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		configShell.setLayout(layout);
		
		// Screen Position.
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = configShell.getBounds();
		int x = bounds.x + ((bounds.width - rect.width) / 2) + 10;
		int y = bounds.y + ((bounds.height - rect.height) / 2) + 10;
		configShell.setLocation (x, y);

		// Create grid layout.
		GridLayout gridLayout = new GridLayout ();
		configShell.setLayout (gridLayout);
		gridLayout.marginTop = 10;
		gridLayout.marginBottom = 10;
		gridLayout.marginLeft = 3;
		gridLayout.marginRight = 3;
		gridLayout.numColumns = 1;
		//gridLayout.horizontalSpacing = 15;
		gridLayout.makeColumnsEqualWidth = false;
		
		// Label next to file name combo box.
		Label name = new Label(configShell, SWT.HORIZONTAL);
		name.setText(lh.localValue("&TranslationTemplates"));
		
		// So our combos and buttons take up whole width of dialog.
		GridData fillGD = new GridData(GridData.FILL_HORIZONTAL);
		
		// Combo box that houses filenames.
		fileNameCombo = new Combo (configShell, SWT.DROP_DOWN);
		fileNameCombo.setLayoutData(fillGD);

		// Create Default Config File Checkbox.
		defaultCfgChk = new Button(configShell, SWT.CHECK);
		
		// Restores the original config file before there were changes.
		restoreDefaultBtn = new Button(configShell, SWT.PUSH);
		restoreDefaultBtn.setText("Restore Default Cfg");
//		restoreDefaultBtn.setLayoutData(data);
		restoreDefaultBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Point to user's version of cfg file.
				String configDir = BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep();
				File deleteMe = new File(configDir + fileNameCombo.getText());
				String deletedFileName = fileNameCombo.getText();
				
				// Make sure the file exists. If it doesn't, don't bother.
				if(!deleteMe.exists())
					return;
				
				// Delete this config file.
				deleteMe.delete();
				
				// Now, reload the config files.
				fileList.clear();
				loadConfigFiles();
				
				// Repopulate the UI.
				fillFilenameComboBox();
				fillVariableComboBox(fileList.get(0));
				fillValueComboBox(variableCombo.getItem(0));
				selectValueComboBox(txt.getText());

				// If this file was the default, change to another if it didn't have an original.
				if(deletedFileName.compareTo(defaultCfgFileName) == 0)
				{
					// Default config directory.
					String defCfgDir = BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep();
					
					// Is there an original?
					File origFile = new File(defCfgDir + deletedFileName);
					if(!origFile.exists())
					{
						// No file with this name existed as an original; reset default config file.
						defaultCfgFileName = fileNameCombo.getText();
						defaultCfgChk.setEnabled(true);
						
						// Save new settings.
						saveSettings();
						
					} // if(!origFile.exists())
					
				} // if(fileNameCombo...
				
			} // public void widgetSelected()
			
		}); // restoreDefaultBtn.addSelectionListener(new SelectionAdapter() {
		
		// Label next to variable name combo box.
		Label name2 = new Label(configShell, SWT.HORIZONTAL);
		name2.setText("Variable Name");
		
		// Combo box that houses variable names.
		variableCombo = new Combo (configShell, SWT.DROP_DOWN);
		GridData varFillGD = new GridData(GridData.FILL_HORIZONTAL);
		variableCombo.setLayoutData(varFillGD);
		
		// Label next to variable value combo box.
		Label name3 = new Label(configShell, SWT.HORIZONTAL);
		name3.setText("Variable Value");
		
		// Combo box for variable values.
		valueCombo = new Combo(configShell, SWT.DROP_DOWN);
		valueCombo.setLayoutData(varFillGD);
		
		// Holds value associated with variable in variable name box.
		txt = new Text(configShell, SWT.SINGLE);
		txt.setLayoutData(fillGD);
		
		// Accessible names.
		Accessible accName = name.getAccessible();
		Accessible accNameCombo = fileNameCombo.getAccessible();
		accName.addRelation(ACC.RELATION_LABEL_FOR, accNameCombo);
		accNameCombo.addRelation(ACC.RELATION_LABELLED_BY, accName);
		//////////////////////////////
		Accessible accName2 = name2.getAccessible();
		Accessible accNameCombo2 = variableCombo.getAccessible();
		accName2.addRelation(ACC.RELATION_LABEL_FOR, accNameCombo2);
		accNameCombo2.addRelation(ACC.RELATION_LABELLED_BY, accName2);
		//////////////////////////////
		Accessible accName3 = name3.getAccessible();
		Accessible accNameCombo3 = txt.getAccessible();
		accName3.addRelation(ACC.RELATION_LABEL_FOR, accNameCombo3);
		accNameCombo3.addRelation(ACC.RELATION_LABELLED_BY, accName3);
		
		// Grid Data.
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		
		// Set up default cfg check box.
		defaultCfgChk.setText("Set As Default");
		defaultCfgChk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// If the checkbox isn't checked, get current  
				// filename in filename combo, then store it 
				// as the default config file, and finally check our box.
				// Else... uncheck the box, and restore the default.
				if(defaultCfgChk.getSelection() == true) {
					defaultCfgFileName = fileNameCombo.getItem( fileNameCombo.getSelectionIndex() );
				}
				else {
					defaultCfgFileName = BBIni.getDefaultConfigFile();
					
				} //if(defaultCfgChk...
				
			} // widgetSelected()
			
		}); // defaultCfgChk.addSelectionListener...
		
		// Set up Apply Button.
		apply = new Button(configShell, SWT.PUSH);
		apply.setText("Apply");
		apply.setLayoutData(data);
		apply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Save settings, but don't close the window.
				saveSettings();
				
				// Refresh the Braille Blaster text and braille views.
				refreshViews();
				
			} // public void widgetSelected()
			
		}); // apply.addSelectionListener(new SelectionAdapter() {
		
		// Set up Okay Button.
		okayBtn = new Button(configShell, SWT.PUSH);
		okayBtn.setText("Okay");
		okayBtn.setLayoutData(data);
		okayBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Save settings.
				saveSettings();
				
				// Close dialog.
				configShell.dispose();
				
				// Refresh the Braille Blaster text and braille views.
				refreshViews();
				
			} // widgetSelected()
			
		}); // okayBtn.addSelectionListener...
		
		// Setup Save-As Button.
		saveAsBtn = new Button(configShell, SWT.PUSH);
		saveAsBtn.setText("Save As");
		saveAsBtn.setLayoutData(data);
		saveAsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Create and open Save-As Dialog.
				FileDialog dialog = new FileDialog(configShell, SWT.SAVE);
			    dialog.setFilterNames( new String[] {"Configuration Files", "All Files (*.*)"} );
			    dialog.setFilterExtensions(new String[] { "*.cfg", "*.*" });
			    dialog.setFilterPath( BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" );
			    String cfgPath = dialog.open();
			    
			    // If the path is null, the user cancelled.
			    if(cfgPath == null)
			    	return;
			    
			    // Copy the original config file data to a new one. Change filename to 
			    // what the user chose.
			    ConfigFile origConfigFile = fileList.get( fileNameCombo.getSelectionIndex() );
			    ConfigFile newCFG = new ConfigFile();
			    newCFG.configFilePath = cfgPath;
				newCFG.lines = new ArrayList<ConfigEntry>();
				for(int curLine = 0; curLine < origConfigFile.lines.size(); curLine++)
				{
					ConfigEntry newEntry = new ConfigEntry();
					newEntry.line = origConfigFile.lines.get(curLine).line;
					newEntry.lineNumber = origConfigFile.lines.get(curLine).lineNumber;
					newEntry.name = origConfigFile.lines.get(curLine).name;
					newEntry.value = origConfigFile.lines.get(curLine).value;
					newEntry.comboIndex = -1;
					newCFG.lines.add(newEntry);
				}
			
				// Add this new config file.
				fileList.add(newCFG);
				
				// Update the user interface.
				fillFilenameComboBox();
				fileNameCombo.select(fileList.size() - 1);
				fillVariableComboBox( fileList.get(fileList.size() - 1) );
				fillValueComboBox(variableCombo.getItem(0));
				selectValueComboBox(txt.getText());
				
				// Set default config.
				defaultCfgFileName = fileNameCombo.getText();
				defaultCfgChk.setEnabled(true);
				
				// Save new settings.
				saveSettings();
				
			} // widgetSelected()
			
		}); // saveAsBtn.addSelectionListener...
		
		// Set up Cancel Button.
		cancelBtn = new Button(configShell, SWT.PUSH);
		cancelBtn.setText("Cancel");
		cancelBtn.setLayoutData(data);
		cancelBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Close dialog.
				configShell.dispose();
				
			} // widgetSelected()
			
		}); // cancelBtn.addSelectionListener...
		
		// Filename Combo Listener.
		fileNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Fill variable name combo box.
				fillVariableComboBox( fileList.get(fileNameCombo.getSelectionIndex()) );
				fillValueComboBox(variableCombo.getItem(variableCombo.getSelectionIndex()));
				selectValueComboBox(txt.getText());
				
				// If the selected file is the default, check our box. Else, uncheck.
				if( defaultCfgFileName.compareTo(fileNameCombo.getItem(fileNameCombo.getSelectionIndex())) == 0)
					defaultCfgChk.setSelection(true);
				else
					defaultCfgChk.setSelection(false);
				
			} // public void widgetSelected()
		
		}); // fileNameCombo.addSelectionListener
		
		// Variable Name Combo Listener.
		variableCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// We have to search the variable list for one that 
				// is on this combo index.
				
				// Get current config file.
				ConfigFile cfgf = fileList.get(fileNameCombo.getSelectionIndex());
				
				// Search the config file's variables.
				for(int curVar = 0; curVar < cfgf.lines.size(); curVar++)
				{
					// Is this the entry we're looking for?
					if(cfgf.lines.get(curVar).comboIndex == variableCombo.getSelectionIndex())
					{
						// Set value text.
						txt.setText(cfgf.lines.get(curVar).value);
						
						// Also update the value combo box.
						fillValueComboBox(variableCombo.getItem(variableCombo.getSelectionIndex()));
						selectValueComboBox(cfgf.lines.get(curVar).value);
						
						// Found it. Break.
						break;
						
					} // if(cfgf.lines.get(curVar)...
					
				} // for(int curVar = 0...;
				
			} // public void widgetSelected()
		
		}); // fileNameCombo.addSelectionListener
		
		// Variable Value Combo Listener.
		valueCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Set text to this.
				txt.setText(valueCombo.getItem(valueCombo.getSelectionIndex()));
				
			} // public void widgetSelected()
		
		}); // valueCombo.addSelectionListener
	}
	
	/////////////////////////////////////////////////////////////////
	// Loads all config files.
	void loadConfigFiles()
	{
		// File separator.
		String sp = BBIni.getFileSep();
		
		// Config file directories.
		String configDir = BBIni.getProgramDataPath() + sp + "liblouisutdml" + sp + "lbu_files" + sp;
		String usrCfgDir = BBIni.getUserProgramDataPath() + sp + "liblouisutdml" + sp + "lbu_files" + sp;
		
		// Grab files in default and local locations.
		File dir = new File(configDir);
		File usrDir = new File(usrCfgDir);
		
		///////////////////////////////
		// Build one list of all files.

			// Create list.
			ArrayList<File> tempFileList = new ArrayList<File>();
			// Fill it with the local files first.
			for(int curCh = 0; curCh < usrDir.listFiles().length; curCh++)
				tempFileList.add(usrDir.listFiles()[curCh]);
			
			// Now add the default files while checking for locals.
			int numDefFiles = dir.listFiles().length;
			int numLocFiles = usrDir.listFiles().length;
			boolean addMe = true;
			for(int curDef = 0; curDef < numDefFiles; curDef++)
			{
				// Assume this isn't already a local file.
				addMe = true;
				
				// Filename of current default file.
				String fileNameDefault = dir.listFiles()[curDef].getPath().substring( dir.listFiles()[curDef].getPath().lastIndexOf(sp) );
				
				// Check against every local file. If there is a duplicate, then don't add this 
				// default file to the list.
				for(int curLoc = 0; curLoc < numLocFiles; curLoc++)
				{
					// Get local file name.
					String fileNameLocal = tempFileList.get(curLoc).getPath().substring( tempFileList.get(curLoc).getPath().lastIndexOf(sp) );
					
					// If there is a default and local file with the same filename, then just keep the local.
					if(fileNameLocal.compareTo(fileNameDefault) == 0) {
						addMe = false;
						break;
						
					} // if(fileNameLocal.compareTo...
					
				} // for(int curLoc = 0...
				
				// Should we add this to the list?
				if(addMe == true)
					tempFileList.add(dir.listFiles()[curDef]);
				
			} // for(int curF = 0...
		
		// Build one list of all files.
		///////////////////////////////
		
		// Iterate over every file and populate our filename combo box.
		for(int curCh = 0; curCh < tempFileList.size(); curCh++)
		{
			// Point to current file in list.
			File child = tempFileList.get(curCh);
			
			// If this is a config file, load it.
			if( !child.getPath().endsWith(".cfg") )
				continue;
			
			// Create config file.
			ConfigFile newCFG = new ConfigFile();
			newCFG.lines = new ArrayList<ConfigEntry>();
				
			// Get full path.
			newCFG.configFilePath = child.getPath();
			
			// Load file, line by line.
			BufferedReader br;
			try
			{
				// Open file.
				br = new BufferedReader( new FileReader(new File(newCFG.configFilePath)) );
			
				// Read every line.
				int curLineNumber = 0;
				String temp = new String();
				while ( (temp = br.readLine()) != null ) {
					ConfigEntry newEntry = new ConfigEntry();
					newEntry.line = temp;
					newEntry.lineNumber = curLineNumber;
					newEntry.name = extractVarName(newEntry.line);
					newEntry.value = extractVarValue(newEntry.line);
					newEntry.comboIndex = -1;
					newCFG.lines.add(newEntry);
					
					// TODO:  Possibly add this code to remove headings/labels?
					// If the previous line contained no spaces or tabs, it could have been a heading/label.
//					if(curLineNumber > 0)
//					{
//						if( newCFG.lines.get(curLineNumber - 1).line.contains("#") == false )
//						{
//							if( newCFG.lines.get(curLineNumber - 1).line.contains("\t") == false )
//						}
//					}
					
					// Get ready for next line.
					curLineNumber++;
				}
				
				// Close file.
				br.close();
			}
			catch (FileNotFoundException e) { e.printStackTrace(); }
			catch (IOException ioe) { ioe.printStackTrace(); }
			
			// Put config file into list.
			fileList.add(newCFG);
			
		} // for(File child : dir.listFiles())
	}
	
	//////////////////////////////////////////////////////////////
	// Loads settings for config files. These settings give us a 
	// range of values that the user is allowed to input.
	void loadConfigFileSettings()
	{
		// If there is a list, clear it. Otherwise, create a new one.
		if(configSettingsList != null)
			configSettingsList.clear();
		else
			configSettingsList = new ArrayList<String>();
		
		// Config file directory.
		String configDir = BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep();
		String settingsPath = configDir + "configSettings.txt";
		
		// Load file, line by line.
		BufferedReader br;
		try
		{
			// Open file.
			br = new BufferedReader( new FileReader(new File(settingsPath)) );
		
			// Read every line.
			String temp = new String();
			while ( (temp = br.readLine()) != null ) {
				
				// Add this line to the list if it isn't a comment or whitespace.
				if( !temp.startsWith("#") && !temp.startsWith(" ") && !temp.startsWith("\t") && !temp.startsWith("\n") && !temp.startsWith("\r") && temp.length() > 1)
					configSettingsList.add(temp);
				
			} // while ( (temp = br.readLine...
			
			// Close file.
			br.close();
			
		} // try
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException ioe) { ioe.printStackTrace(); }
	}
	
	//////////////////////////////////////////////////////////////
	// Takes the name of a config setting/action, and returns an 
	// array of string options.
	// If null is returned, then the action/setting wasn't found.
	String[] getActionSettings(String actionName)
	{
		// Create new string array.
		String[] options = null;
		
		// Loop through every action until we find the one given.
		for(int curAck = 0; curAck < configSettingsList.size(); curAck++)
		{
			// Split this up into tokens.
			String line[] = configSettingsList.get(curAck).split("\\|");
			
			// Is this the one?
			if(line[0].toLowerCase().compareTo(actionName.toLowerCase()) == 0)
			{
				// Make room for the option tokens. 
				options = new String[line.length - 1];
				
				// Copy all but the first.
				for(int curOp = 0; curOp < options.length; curOp++)
				{
					// Copy it.
					options[curOp] = line[curOp + 1];
					
				} // for(int curOp...
				
				// Found it; leave loop.
				break;
				
			} // if(configSettingsList COMPARETO
			
		} // for(int curAck = 0... 

		// Return our list of options... or null if we didn't find it.
		return options;
	}
	
	//////////////////////////////////////////////////////////////
	// Pulls the variable name from the config line.
	String extractVarName(String varLine)
	{
		// Don't bother with comments.
		if(varLine.contains("#"))
			return null;
		
		// Grab tokens.
		String [] tokens = varLine.split("\\s+");
		
		// Don't bother with labels.
		if(tokens.length <= 1)
			return null;
		
		// Skip spaces and tabs.
		int indx = 0;
		for( ; indx < tokens.length; indx++) {
			if(tokens[indx].compareTo("") == 0 || tokens[indx].compareTo(" ") == 0 || tokens[indx].compareTo("\t") == 0)
				continue;
			else
				break;
		}
		
		// Return variable name.
		return tokens[indx];
	}
	
	//////////////////////////////////////////////////////////////
	// Pulls variable value from config line.
	String extractVarValue(String varLine)
	{
		// Don't bother with comments.
		if(varLine.contains("#"))
			return null;
		
		// Grab tokens.
		String [] tokens = varLine.split("\\s+");
		
		// Don't bother with labels.
		if(tokens.length <= 1)
			return null;
		
		// Skip spaces and tabs.
		int indx = 0;
		for( ; indx < tokens.length; indx++) {
			if(tokens[indx].compareTo("") == 0 || tokens[indx].compareTo(" ") == 0 || tokens[indx].compareTo("\t") == 0)
				continue;
			else
				break;
		}
		
		// Create new string that will contain the variable value.
		String varVal = new String();
		for(int curTok = indx + 1; curTok < tokens.length; curTok++) {
			
			// Add all tokens together.
			varVal += tokens[curTok];
			
			// If there were spaces in the variable values, add them back.
			if(tokens.length > 3 && curTok < tokens.length - 1)
				varVal += " ";
		}
		
		// Return variable name.
		return varVal;
	}
	
	//////////////////////////////////////////////////////////////
	// Fills filename combo box.
	void fillFilenameComboBox()
	{
		// Clear the combobox first.
		fileNameCombo.removeAll();

		// Loop through every file.
		for(int curFile = 0; curFile < fileList.size(); curFile++)
		{
			// Get file name(no path).
			String path = fileList.get(curFile).configFilePath;
			String fileName = path.substring(path.lastIndexOf(BBIni.getFileSep()) + 1, path.length());
			fileNameCombo.add(fileName);
		}
		
		// TODO: Alphabetize.
		
		// Select first item.
		fileNameCombo.select(0);
	}
	
	//////////////////////////////////////////////////////////////
	// Fills variable name combo box.
	void fillVariableComboBox(ConfigFile cfgFile)
	{
		// Vars.
		int curComboIdx = 0;
		int firstEntryIndex = -1;
		
		// Clear the box first.
		variableCombo.removeAll();
		
		// Add lines to combo box.
		for(int curEntry = 0; curEntry < cfgFile.lines.size(); curEntry++)
		{
			// If this is a variable, and not a comment or label, add to combo box.
			if(cfgFile.lines.get(curEntry).name != null) {
				variableCombo.add(cfgFile.lines.get(curEntry).name);
				cfgFile.lines.get(curEntry).comboIndex = curComboIdx;
				curComboIdx++;
				
				// Record first valid entry with a value.
				if(firstEntryIndex == -1)
					firstEntryIndex = curEntry;
				
			} // if(cfgFile.lines.get...
			
		} // for(int curEntry = 0; curEntry...
		
		// Select first item.
		variableCombo.select(0);
		// Set value.
		txt.setText(cfgFile.lines.get(firstEntryIndex).value);
		
	} // void fillVariableComboBox(ConfigFile cfgFile)
	
	//////////////////////////////////////////////////////////////
	// Fill value combo box with range of options associated 
	// with given action.
	void fillValueComboBox(String actionName)
	{
		// Clear the combo box first.
		valueCombo.removeAll();
		
		// Grab values that we are allowed to use.
		String[] options = getActionSettings(actionName);
		
		// If we got null back, we couldn't find the given action.
		if(options == null)
			return;
		
		// Put all values into the combo box.
		for(int curOp = 0; curOp < options.length; curOp++)
		{
			// Add it.
			valueCombo.add(options[curOp]);
			
		} // for(int curAck...
		
		// Select first option.
		valueCombo.select(0);
		
		// Update the value UI elements.
		updateValueWidgets();
		
	} // void fillValueComboBox(String actionName)
	
	// Searches values in the value combo for value given, 
	// and sets combo to that index.
	void selectValueComboBox(String valueToSelect)
	{
		// Get number of items in combo box.
		int numItems = valueCombo.getItemCount();
		
		// Check every item for equality with value given.
		for(int curVal = 0; curVal < numItems; curVal++)
		{
			// Is this item the same as the value given?
			if(valueCombo.getItem(curVal).compareTo(valueToSelect) == 0)
			{
				// Set combo to this index.
				valueCombo.select(curVal);
				
				// Found it; break.
				break;
				
			} // if(valueCombo.getItem(curVal) COMPARE
			
		} // for(int curVal = 0...
		
	} // void selectValueComboBox(String valueToSelect)
	
	//////////////////////////////////////////////////////////////
	// Checks the values in our value combo box.
	// Depending on what's in there, we disable/enable 
	// the appropriate boxes so the user only has to deal 
	// with one widget.
	void updateValueWidgets()
	{
		// Get the current value of our value combo box.
		String curStrValue = valueCombo.getItem(0);
		
		// If this value is # or TXT, allow user access to 
		// the edit box.
		if( curStrValue.compareTo("#") == 0 || curStrValue.compareTo("TXT") == 0 )
		{
			// Disable the value combo box. Enable edit box.
			valueCombo.setEnabled(false);
			txt.setEnabled(true);
		}
		else
		{
			// Enable combo, disable edit.
			valueCombo.setEnabled(true);
			txt.setEnabled(false);
			
		} // if( curStrValue.compareTo("#")... else
		
	} // void updateValueWidgets()
	
	
	//////////////////////////////////////////////////////////////
	// Saves config file settings to file.
	void saveSettings()
	{
		fileNameCombo.getSelectionIndex();
		
		// Save changes to file.
		
		// Get current config file.
		ConfigFile cfgf = fileList.get(fileNameCombo.getSelectionIndex());
		
		// Search the config file's variables. Update the value.
		for(int curV = 0; curV < cfgf.lines.size(); curV++)
		{
			// Is this the entry we're looking for?
			if(cfgf.lines.get(curV).comboIndex == variableCombo.getSelectionIndex())
			{
				// Set value from text box.
				cfgf.lines.get(curV).value = txt.getText();
				
				// Found it. Break.
				break;
				
			} // if(cfgf.lines.get(curVar)...
			
		} // for(int curVar = 0...
		
		try {
			// Open file for writing.
			String sp = BBIni.getFileSep();
			String filePath = cfgf.configFilePath;
			String fileName = filePath.substring(filePath.lastIndexOf(sp), filePath.length());
			filePath = BBIni.getUserProgramDataPath() + sp + "liblouisutdml" + sp + "lbu_files" + fileName;
			BufferedWriter bw = new BufferedWriter( new FileWriter(new File(filePath), false) );
			
			// For every line, save it to file.
			for(int curVar = 0; curVar < cfgf.lines.size(); curVar++)
			{
				// Is this a comment? Write it, then skip to next line.
				if(cfgf.lines.get(curVar).line.contains("#")) {
					bw.write(cfgf.lines.get(curVar).line);
					bw.newLine();
					continue;
				}
				
				// Does this line need tabs?
				if(cfgf.lines.get(curVar).line.contains("\t") == true/* || 
				   cfgf.lines.get(curVar).line.charAt(0) == ' '*/) {
						bw.write("\t");
				}
				// Write the line.
				if(cfgf.lines.get(curVar).name != null)
				{
					bw.write(cfgf.lines.get(curVar).name);
					if(cfgf.lines.get(curVar).value.length() > 0)
						bw.write(" " + cfgf.lines.get(curVar).value);
					bw.newLine();
				}
				else {
					bw.write(cfgf.lines.get(curVar).line);
					bw.newLine();
				}
			} // for(int curVar = 0; curVar...
			
			// Close output file.
			bw.close();
			
			///////////////////////
			// Default Config File.

				// Set default config.
				props.setProperty("defaultConfigFile", defaultCfgFileName);
				// Now write the properties back to the user settings file.
				props.store( new FileOutputStream(BBIni.getUserSettings()), null );
				// Update BBIni.
				BBIni.setDefaultConfigFile(defaultCfgFileName);
			
			// Default Config File.
			///////////////////////
		}
		catch(IOException ioe) { ioe.printStackTrace(); }
		
	} // void saveSettings()
	
	//////////////////////////////////////////////////////////////
	// Refreshes views in current perspective.
	void refreshViews()
	{
		int index = dm.getFolder().getSelectionIndex();
		if(index  > -1) {
			dm.getList().get(index).setCurrentConfig(defaultCfgFileName);
			if(dm.getList().get(index) instanceof Manager)
				((Manager)dm.getList().get(index)).refresh();
		}
		
	} // void refreshViews()
	
} // public class ConfigFileDialog... 
