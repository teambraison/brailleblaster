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

import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.brailleblaster.BBIni;
import org.brailleblaster.settings.Welcome;
import org.brailleblaster.util.YesNoChoice;
import java.util.LinkedList;

public class WPManager {
    /**
     * This is the controller for the whole word processing operation. It is the
     * entry point for the word processor, and therefore the only public class.
     */
    public static Display display;
    private Shell shell;
    private FormLayout layout;
    private TabFolder folder;
    private FormData location;
    private BBMenu bbMenu;
    private BBStatusBar statusBar;
    private BBToolBar toolBar;
    private LinkedList<DocumentManager> managerList;
    private StyleManager sm;
    
    private static final int MAX_NUM_DOCS = 4;//the max limit of total number of docs can have at the same time
    
    //This constructor is the entry point to the word processor. It gets things set up, handles multiple documents, etc.
    public WPManager(String fileName) {
    	checkLiblouisutdml();
    	 
        display = new Display();
    	this.shell = new Shell(display, SWT.SHELL_TRIM);
        this.shell.setText("BrailleBlaster"); 
		this.layout = new FormLayout();
		this.shell.setLayout(this.layout);
		
		this.folder = new TabFolder(this.shell, SWT.NONE);	
		this.location = new FormData();
	    this.location.left = new FormAttachment(0);
	    this.location.right = new FormAttachment(100);
	    this.location.top = new FormAttachment (20);
	    this.location.bottom = new FormAttachment(98);
	    this.folder.setLayoutData (this.location);
	    
	    this.statusBar = new BBStatusBar(this.shell);
	    this.bbMenu = new BBMenu(this);
	    
		this.sm = new StyleManager(this);
	    
	    // Toolbar.
	    toolBar = new BBToolBar(shell, this);
	    
	    this.managerList = new LinkedList<DocumentManager>();
	    this.managerList.add(new DocumentManager(this, null));
	    this.folder.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub			
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkToolbarSettings();
			}
	    });
	    
		this.shell.addListener(SWT.Close, new Listener() { 
	        public void handleEvent(Event event) { 
	           System.out.println("Main Shell handling Close event, about to dipose the main Display"); 
	           display.dispose();
	           bbMenu.writeRecentsToFile();
	        } 
	     });
		
		setShellScreenLocation(display, this.shell);
   
        new Welcome(); 
		this.shell.open();
		
        while (!display.isDisposed())  { 
	        try { 
	           if (!display.readAndDispatch()) { 
	              display.sleep(); 
	           } 
	        } 
	        catch (Exception e) { 
	           e.printStackTrace(); 
	        } 
	    } 
    }
	
	private void setShellScreenLocation(Display display, Shell shell){
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + ((bounds.width - rect.width) / 2);
		int y = bounds.y + ((bounds.height - rect.height) / 2);
		shell.setLocation(x, y);
	}	
	
	public void addDocumentManager(String fileName){
		this.managerList.add(new DocumentManager(this, fileName));
		setSelection();
		checkToolbarSettings();
	}

	public void setSelection(){
		int index = this.managerList.size() - 1;
		this.folder.setSelection(index);
	}
	
    void checkLiblouisutdml() {
        if (BBIni.haveLiblouisutdml()) {
            return;
        }
        if (new YesNoChoice("The Braille facility is not usable." + " See the log." + " Do you wish to continue?").result == SWT.NO) {
            System.exit(1);
        }
    }
    
    void checkToolbarSettings(){
    	boolean enabled = true;
    	
    	if(this.managerList.size() > 0){
    		int index = this.getFolder().getSelectionIndex();  
 
    		if(this.getFolder().getItem(index).getText().startsWith("Untitled")){
    			enabled = false;
    		}
    		this.toolBar.toggleCheckBox(enabled, this.getList().get(index).simBrailleDisplayed);
    	}
    	else {
    		enabled = false;
    		this.toolBar.toggleCheckBox(enabled, false);
    	}
    }

    static int getMaxNumDocs(){
        return MAX_NUM_DOCS;
    }
    
    public static Display getDisplay() {
		return display;
	}
    
    public Shell getShell(){
    	return shell;
    }
    
    public TabFolder getFolder(){
    	return this.folder;
    }
    
    public LinkedList<DocumentManager> getList(){
    	return this.managerList;
    }
    
    public BBStatusBar getStatusBar(){
    	return this.statusBar;
    }
    
    public BBMenu getMainMenu() {
    	return bbMenu;
    }
    
    public StyleManager getStyleManager() {
    	return sm;
    }
}