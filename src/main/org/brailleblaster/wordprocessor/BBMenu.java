/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.falcon.FalconController;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.brailleblaster.perspectives.webView.WebViewController;
import org.brailleblaster.userHelp.HelpOptions;
import org.brailleblaster.userHelp.UserHelp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;


public abstract class BBMenu {
	//This class contains all the menus.
	protected Menu menuBar;

	/* All the menu items are member fields so they can be accessed outside the
	 * constructor. This might be done for example with setEnabled(false) to
	 */
	MenuItem recentItem;
	MenuItem languageItem;
	MenuItem exitItem;
	
	Menu perspectiveMenu;
	public MenuItem perspectiveItem;
	protected MenuItem selectedPerspective;
	MenuItem brailleEditorItem;
	MenuItem imageDescriberItem;
	MenuItem falconItem;
        MenuItem webViewItem;
	
	MenuItem readManualItem;
	MenuItem helpInfoItem;
	MenuItem tutorialsItem;
	MenuItem checkUpdatesItem;
	MenuItem aboutItem;
	
	protected MenuItem fileItem;
	protected Menu fileMenu;
	protected Menu viewMenu;
	protected WPManager wordProc;
	ArrayList<String> recentDocsList = null;
	Menu subMen;
	final int maxRecentFiles = 5;
	protected LocaleHandler lh;
	
	public BBMenu(final WPManager wp) {
		lh = new LocaleHandler();

		/*
		 * Note that the values in the setText methods are keys for
		 * localization. They are not intended to be seen by the user.
		 * Capitalization should follow the convention for names in Java, but
		 * this is not always consistent. Values, with proper capitalization,
		 * are shown in the files in the dist/programData/lang subdirectory.
		 */

		// Hold onto word processor so other functions have access.
		wordProc = wp;

    	// Init recent doc list.
    	recentDocsList = new ArrayList<String>();
		
		// Set up menu bar
    	menuBar = new Menu(wp.getShell(), SWT.BAR);
		fileItem = new MenuItem(menuBar, SWT.CASCADE);
		fileItem.setText(lh.localValue("&File"));
		MenuItem viewItem = new MenuItem(menuBar, SWT.CASCADE);
		viewItem.setText(lh.localValue("&View"));
		MenuItem helpItem = new MenuItem(menuBar, SWT.CASCADE);
		helpItem.setText(lh.localValue("&Help"));

		// Set up file menu
		fileMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		
		recentItem = new MenuItem (fileMenu, SWT.CASCADE);
		recentItem.setText(lh.localValue("&Recent"));
		
		// Setup recent files submenu.
		subMen = new Menu(wordProc.getShell(), SWT.DROP_DOWN);
		recentItem.setMenu(subMen);
		
		// Reads recent file list... from file.
		readRecentFiles();
		
		
		languageItem = new MenuItem(fileMenu, SWT.PUSH);
		languageItem.setText(lh.localValue("&Language"));
		languageItem.setEnabled(false); /* FO */
		languageItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		
		if (!BBIni.getPlatformName().equals("cocoa")) {
			exitItem = new MenuItem(fileMenu, SWT.PUSH);
			exitItem.setText(lh.localValue("e&xit") + "\tCtrl + Q");
			exitItem.setAccelerator(SWT.MOD1 + 'Q');
			exitItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					e.doit = wp.close();
				}
			});
		}
		fileItem.setMenu(fileMenu);

		// Set up view menu
	
		viewMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);

		perspectiveItem = new MenuItem(viewMenu, SWT.CASCADE);
		perspectiveItem.setText(lh.localValue("&Perspectives"));
		perspectiveMenu = new Menu(wordProc.getShell(), SWT.DROP_DOWN);
		perspectiveItem.setMenu(perspectiveMenu);
						
		brailleEditorItem = new MenuItem(perspectiveMenu, SWT.CHECK);
		brailleEditorItem.setText(lh.localValue("Braille Editor"));
		brailleEditorItem.setSelection(true);
		brailleEditorItem.setData(Manager.class);
		brailleEditorItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(brailleEditorItem.getSelection() == true && !selectedPerspective.equals(brailleEditorItem)){
					selectedPerspective = brailleEditorItem;
					imageDescriberItem.setSelection(false);
					wp.swapPerspectiveController((Class<?>)brailleEditorItem.getData());
				}
			}
		});
						
		imageDescriberItem = new MenuItem(perspectiveMenu, SWT.CHECK);
		imageDescriberItem.setText(lh.localValue("Image Describer"));
		imageDescriberItem.setSelection(false);
		imageDescriberItem.setData(ImageDescriberController.class);
		imageDescriberItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(imageDescriberItem.getSelection() == true && !selectedPerspective.equals(imageDescriberItem)){
					selectedPerspective = imageDescriberItem;
					brailleEditorItem.setSelection(false);
					wp.swapPerspectiveController((Class<?>)imageDescriberItem.getData());
				}
			}
		});
		
		falconItem = new MenuItem(perspectiveMenu, SWT.CHECK);
		falconItem.setText(lh.localValue("Novint Falcon"));
		falconItem.setSelection(false);
		falconItem.setData(FalconController.class);
		falconItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(falconItem.getSelection() == true && !selectedPerspective.equals(falconItem)){
					selectedPerspective = falconItem;
					brailleEditorItem.setSelection(false);
					imageDescriberItem.setSelection(false);
					wp.swapPerspectiveController((Class<?>)falconItem.getData());
				}
			}
		});
		
		webViewItem = new MenuItem(perspectiveMenu, SWT.CHECK);
		webViewItem.setText(lh.localValue("Web View"));
		webViewItem.setSelection(false);
		webViewItem.setData(WebViewController.class);
		webViewItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(webViewItem.getSelection() == true && !selectedPerspective.equals(webViewItem)){
					selectedPerspective = webViewItem;
					brailleEditorItem.setSelection(false);
					wp.swapPerspectiveController((Class<?>)webViewItem.getData());
				}
			}		
		});		
         viewItem.setMenu(viewMenu);

		// Set up help menu
		Menu helpMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		aboutItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutItem.setText(lh.localValue("&About"));
		aboutItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(HelpOptions.AboutBB);
			}
		});
		helpInfoItem = new MenuItem(helpMenu, SWT.PUSH);
		helpInfoItem.setText(lh.localValue("&helpInfo"));
		helpInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(HelpOptions.HelpInfo);
			}
		});
		tutorialsItem = new MenuItem(helpMenu, SWT.PUSH);
		tutorialsItem.setText(lh.localValue("&Tutorials"));
		tutorialsItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(HelpOptions.ReadTutorial);
			}
		});
		readManualItem = new MenuItem(helpMenu, SWT.PUSH);
		readManualItem.setText(lh.localValue("&ReadManuals"));
		readManualItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(HelpOptions.ReadManuals);
			}
		});
		checkUpdatesItem = new MenuItem(helpMenu, SWT.PUSH);
		checkUpdatesItem.setText(lh.localValue("&CheckUpdates"));
		checkUpdatesItem.setEnabled(false); /* FO */
		checkUpdatesItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(HelpOptions.CheckUpdates);
			}
		});
		MenuItem viewLogItem = new MenuItem(helpMenu, SWT.PUSH);
		viewLogItem.setText(lh.localValue("View&Log"));
		viewLogItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LogViewerDialog	logDialog = new LogViewerDialog(wp.getShell());
				logDialog.setText(lh.localValue("LogViewer.Title"));
				logDialog.open();
			}
		});
		helpItem.setMenu(helpMenu);
				
		// Activate menus when documentWindow shell is opened
		wp.getShell().setMenuBar(menuBar);
	}
	
	//////////////////////////////////////////////////////////////////////////	
	// Returns ArrayList<String> of recent documents list.
	public ArrayList<String> getRecentDocumentsList() {
		return recentDocsList;
	}
	
	//////////////////////////////////////////////////////////////////////////
	// Looks for recent files document, then updates 
	// the main menu's recent files list.
	public void readRecentFiles()
	{
		////////////////////////////
		// Populate Recent Documents
		
			// Temp buffer to hold entries.
	    	ArrayList<String> tempStrList = new ArrayList<String>();
		
			// Open file and populate!
	        try
	        {
	        	// Open File.
	        	BufferedReader reader = new BufferedReader(new FileReader( BBIni.getRecentDocs() ));
	        	
	        	// Record every line into our list.
	            String line = null;
	            while ((line = reader.readLine()) != null)
	            	tempStrList.add(line);
	            
	            // Close the document.
	            reader.close();
	        }
	        catch (FileNotFoundException e) { /*new Notify(e.getMessage());*/ }
	        catch (IOException ioe) { /*new Notify(ioe.getMessage());*/ }
			
			// For every document that has been previously opened, create a menu entry.
	        for(int curLine = 0; curLine < tempStrList.size() && curLine < maxRecentFiles; curLine++)
	        {
	        	// Add new entry.
	        	addRecentEntry(tempStrList.get(curLine));
				
	        } // for(int curLine = 0; curLine...
	        
		// Populate Recent Documents
		////////////////////////////
	}
	
	//////////////////////////////////////////////////////////////////////////	
	// Adds an entry to recent files menu.
	public void addRecentEntry(String path)
	{
		// Construct strings from path. These are added to the recent docs list and menu items.
		// Current line.
		String fileName = path.substring( path.lastIndexOf(BBIni.getFileSep()) + 1, path.length() );
		final String curStr = fileName + "  [" + path + "]";
		final String curStr2 = path;
		
		// Make sure the file exists.
		// If not, don't add it.
		File f = new File(path);
		if( !f.exists() || f.isDirectory() )
			return;
		
		// Add path to recent document list.
		recentDocsList.add(0, path);
		
		// Create new item under sub menu.
		MenuItem newItem = new MenuItem(subMen, SWT.PUSH, 0);
		// Set its text.
		newItem.setText( curStr );
		
		// Add action!
		newItem.addSelectionListener(new SelectionAdapter()
		{
			// Action to perform when widget selected!
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int index= wordProc.getFolder().getSelectionIndex();
				if(index == -1){
					wordProc.addDocumentManager(curStr2);
					//wordProc.getList().getFirst().openDocument( curStr2 );
				}
				else {
					if(!wordProc.getList().get(index).canReuseTab())
					{
						wordProc.addDocumentManager( curStr2 );
					}
					else
					{
						wordProc.getList().get(index).reuseTab(curStr2);
					}
				}
				
			} // public void widgetSelected(SelectionEvent e) {
			
		}); // newItem.addSelectionListener(new SelectionAdapter() {
	}
	
	//////////////////////////////////////////////////////////////////////////
	// Writes contents of recent file list to disk.
	public void writeRecentsToFile()
	{
		try
		{
			// Open file for writing.
			BufferedWriter bw = new BufferedWriter( new FileWriter( BBIni.getRecentDocs() ) );
			
			// List of recent documents.
			ArrayList<String> recdocs = wordProc.getMainMenu().getRecentDocumentsList();
			
			// Starting index.
			int startIndex = recentDocsList.size() - 1;
			if(startIndex >= maxRecentFiles) startIndex = maxRecentFiles - 1;
			
			// Add the file path.
			for(int curLine = startIndex; curLine >= 0; curLine--) {
				bw.write( recdocs.get(curLine) );
				bw.newLine();
	        }
			
			// Close the file.
			bw.close();
		}
		catch(IOException ioe) { ioe.printStackTrace(); }
		
	} // public void writeRecentToFile()
	
	//////////////////////////////////////////////////////////////////////////
	// Returns 'recent item' menu object.
	public Menu getRecentItemSubMenu() {
		return recentItem.getMenu();
	} // getRecentItemSubMenu()
	
	public void dispose(){
		menuBar.dispose();
	}
	
	protected void setPerspectiveMenuItem(int index){
		Menu menu = viewMenu.getItem(0).getMenu();
		int count = menu.getItemCount();
		for(int i = 0; i < count; i++){
			if(i == index){
				menu.getItem(i).setSelection(true);
				selectedPerspective = menu.getItem(i);
			}
			else
				menu.getItem(i).setSelection(false);
		}
	}
	
	public abstract void setCurrent(Controller controller);
	public abstract Controller getCurrent();
}
