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

package org.brailleblaster.wordprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.brailleblaster.BBIni;
import org.brailleblaster.util.FileUtils;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



public class ConfigFileDialog extends Dialog {
	
	static Display display;
	static Shell configShell;
	
	WPManager dm;
	
	Button apply;
	Text txt;
	Combo fileNameCombo;
	Combo variableCombo;

	FileUtils fu = new FileUtils();
	
	ArrayList<Style> styleList; //styles
	ArrayList<String> displayArr; //Styles' names for display
	HashMap<String, String> configMap = new HashMap<String, String>();
	
	ArrayList<ConfigFile> fileList;
	
	/////////////////////////////////////////////////////////
	// Constructor.
	public ConfigFileDialog(Shell parent, int style, WPManager fdm) {
		
		// Set up dialog.
		super(parent, style);
		
		// Create shell, get display, etc.
		dm = fdm;
		display = dm.getDisplay();
		Display display = parent.getDisplay();
		configShell = new Shell(parent, SWT.DIALOG_TRIM);
		configShell.setText("Update Config File");
		
		fileList = new ArrayList<ConfigFile>();
		
		// Create user interface.
		createUIelements();
		
		// Load info into combobox.
//		combo.setItems((String[])displayArr.toArray(new String[displayArr.size()]));
		loadConfigFiles();
		fillFilenameComboBox();
		fillVariableComboBox(fileList.get(0));
		
		// show the SWT window
		configShell.pack();
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
		gridLayout.marginTop = 15;
		gridLayout.marginBottom = 15;
		gridLayout.marginLeft = 10; 
		gridLayout.marginRight = 10; 
		gridLayout.numColumns = 1;
		//gridLayout.horizontalSpacing = 15;
		gridLayout.makeColumnsEqualWidth = false;
		
		// Label next to file name combo box. 
		Label name = new Label(configShell, SWT.HORIZONTAL);
		name.setText("Config Filename");

		// Combo box that houses filenames.
		fileNameCombo = new Combo (configShell, SWT.DROP_DOWN);
		
		// Label next to variable name combo box. 
		Label name2 = new Label(configShell, SWT.HORIZONTAL);
		name2.setText("Variable Name");
		
		// Combo box that houses variable names.
		variableCombo = new Combo (configShell, SWT.DROP_DOWN);
		
		// Label next to variable value combo box. 
		Label name3 = new Label(configShell, SWT.HORIZONTAL);
		name3.setText("Variable Value");
		
		// Holds value associated with variable in variable name box.
		txt = new Text(configShell, SWT.SINGLE);
		GridData textGD = new GridData(GridData.FILL_HORIZONTAL);
		txt.setLayoutData(textGD);
		
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
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		
		// Set up Apply Button.
		apply = new Button(configShell, SWT.PUSH);
		apply.setText("Apply");
		apply.setLayoutData(data);
		apply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
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
					// Open file fore writing.
					BufferedWriter bw = new BufferedWriter( new FileWriter(new File(cfgf.configFilePath), false) );
					
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
							bw.write(cfgf.lines.get(curVar).name + " ");
							bw.write(cfgf.lines.get(curVar).value);
							bw.newLine();
						}
						else {
							bw.write(cfgf.lines.get(curVar).line);
							bw.newLine();
						}
					} // for(int curVar = 0; curVar...
					
					// Close output file.
					bw.close();
				}
				catch(IOException ioe) { ioe.printStackTrace(); }
				
			} // public void widgetSelected()
			
		}); // apply.addSelectionListener(new SelectionAdapter() {
		
		// Filename Combo Listener.
		fileNameCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Fill variable name combo box.
				fillVariableComboBox( fileList.get(fileNameCombo.getSelectionIndex()) );
				
			} // public void widgetSelected()
		
		}); // fileNameCombo.addSelectionListener
		
		// Variable Name Combo Listener.
		variableCombo.addSelectionListener(new SelectionAdapter() {
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
						
						// Found it. Break.
						break;
						
					} // if(cfgf.lines.get(curVar)...
					
				} // for(int curVar = 0...
				
			} // public void widgetSelected()
		
		}); // fileNameCombo.addSelectionListener
	}
	
	void loadConfigFiles()
	{
		// Config file directory. 
		String configDir = BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep();
				
		// Iterate over every file and populate our filename combo box.
		File dir = new File(configDir);
		for(File child : dir.listFiles())
		{
			// If this is a config file, load it.
			if( !child.getPath().endsWith(".cfg") )
				continue;
			
			// Create new config.
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
		for(int curTok = indx + 1; curTok < tokens.length; curTok++)
			varVal += tokens[curTok];
		
		// Return variable name.
		return varVal;
	}
	
	//////////////////////////////////////////////////////////////
	// Fills filename combo box.
	void fillFilenameComboBox()
	{
		// Loop through every file.
		for(int curFile = 0; curFile < fileList.size(); curFile++)
		{
			// Get file name(no path).
			String path = fileList.get(curFile).configFilePath;
			String fileName = path.substring(path.lastIndexOf(BBIni.getFileSep()) + 1, path.length());
			fileNameCombo.add(fileName);
		}
		
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
	
} // public class ConfigFileDialog... 
