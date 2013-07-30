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

package org.brailleblaster.imagedescriber;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

///////////////////////////////////////////////////////////////////////////////////////////
// Simple dialog that displays images in a document, and allows one
// to modify the descriptions.
public class ImageDescriberDialog extends Dialog {
	
	// Dialog stuff.
	static Display display;
	static Shell configShell;
	LocaleHandler lh = new LocaleHandler();
	WPManager wpm;
	
	// UI Elements.
	Button nextBtn;
	Button prevBtn;
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Constructor.
	public ImageDescriberDialog(Shell parent, int style, WPManager wordProcesserManager) {
		
		// SUPER!
		super(parent, style);
		
		// Create shell, get display, etc.
		wpm = wordProcesserManager;
		display = wpm.getDisplay();
		Display display = parent.getDisplay();
		configShell = new Shell(parent, SWT.DIALOG_TRIM);
		configShell.setText(lh.localValue("Image Describer"));
		
		////////////////////////////
		// Grab current doc manager.
		
			DocumentManager dm = null;
			int index= wpm.getFolder().getSelectionIndex();
			if(index == -1){
				wpm.addDocumentManager(null);
				dm = wpm.getList().getFirst();
			}
			else {
				dm = wpm.getList().get(index);
			}

		// Grab current doc manager.
		////////////////////////////
			
			
		// Create all of the buttons, edit boxes, etc.
		createUIelements();
		
		// Start the image describer.
//		TODO: MMOVE -> ImageDescriber imgDesc = new ImageDescriber(dm);
		
		
		///////////////////
		// Run this dialog.
		
			// show the SWT window
			configShell.pack();
			
			// Resize window.
			configShell.setSize(700, 700);
			
			// Open and Run!
			configShell.open();
			while (!configShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

		// Run this dialog.
		///////////////////
			
		// Shutdown.
		configShell.dispose();
		
	} // public ImageDescriberDialog(Shell arg0, int arg1)
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Creates all buttons, boxes, checks, etc.
	public void createUIelements()
	{
		// Create next button.
		nextBtn = new Button(configShell, SWT.PUSH);
		nextBtn.setText("Next");
		nextBtn.setBounds(0,  0, 100, 100);
		nextBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				
				
			} // widgetSelected()
			
		}); // nextBtn.addSelectionListener...
		
		// Create previous button.
		prevBtn = new Button(configShell, SWT.PUSH);
		prevBtn.setText("Previous");
		prevBtn.setBounds(101,  0, 100, 100);
		prevBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				
				
			} // widgetSelected()
			
		}); // prevBtn.addSelectionListener...
		
	} // public void createUIelements()
	
} // public class ImageDescriberDialog extends Dialog
