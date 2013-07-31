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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

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
	Label mainImage;
	Text imgDescTextBox; 
	
	// The image describer.
	ImageDescriber imgDesc;
	
	// Main image.
	int imageOffsetX = 0;
	int imageOffsetY = 150;
	int clientWidth = -1;
	int clientHeight = -1;
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Constructor.
	public ImageDescriberDialog(Shell parent, int style, WPManager wordProcesserManager) {
		
		// SUPER!
		super(parent, style);
		
		// Create shell, get display, etc.
		wpm = wordProcesserManager;
		display = wpm.getDisplay();
		display = parent.getDisplay();
		configShell = new Shell(parent, SWT.DIALOG_TRIM);
		configShell.setText(lh.localValue("Image Describer"));
		
		// Resize window.
		configShell.setSize(700, 700);
		clientWidth = configShell.getClientArea().width;
		clientHeight = configShell.getClientArea().height;
		
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
		
		// Start the image describer.
		imgDesc = new ImageDescriber(dm);
			
		// Create all of the buttons, edit boxes, etc.
		createUIelements();
		
		///////////////////
		// Run this dialog.
		
			// show the SWT window
			configShell.pack();
			
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
		imgDesc.disposeImages();
		mainImage.dispose();
		
	} // public ImageDescriberDialog(Shell arg0, int arg1)
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Creates all buttons, boxes, checks, etc.
	public void createUIelements()
	{
		// Setup main image.
		mainImage = new Label(configShell, SWT.NONE);
		mainImage.setBounds(imageOffsetX, imageOffsetY, clientWidth - imageOffsetX, (clientHeight - imageOffsetY));
		mainImage.setImage( createScaledImage(imgDesc.getCurElementImage(), clientWidth, clientHeight) );

		// Create image description text box.
		imgDescTextBox = new Text(configShell, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		imgDescTextBox.setBounds(250, 8, clientWidth - 250, 200);
		imgDescTextBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				
				// Set image's description.
				imgDesc.setCurElmProd(imgDescTextBox.getText(), null, null, null);
				
			} // modifyText()
			
		}); // addModifyListener(new ModiftyListener() { 
		
		
		// Get prodnote text/image description.
		imgDescTextBox.setText( imgDesc.getCurProdText() );
		
		
		// Create previous button.
		prevBtn = new Button(configShell, SWT.PUSH);
		prevBtn.setText("Previous");
		prevBtn.setBounds(101,  0, 100, 100);
		prevBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Change main image to previous element image.
				imgDesc.prevImageElement();
				mainImage.setImage( createScaledImage(imgDesc.getCurElementImage(), clientWidth, clientHeight - imageOffsetY) );
				
				// Get prodnote text/image description.
				imgDescTextBox.setText( imgDesc.getCurProdText() );
				
			} // widgetSelected()
			
		}); // prevBtn.addSelectionListener...
		
		// Create next button.
		nextBtn = new Button(configShell, SWT.PUSH);
		nextBtn.setText("Next");
		nextBtn.setBounds(0,  0, 100, 100);
		nextBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Change main image to next element image.
				imgDesc.nextImageElement();
				mainImage.setImage( createScaledImage(imgDesc.getCurElementImage(), clientWidth, clientHeight - imageOffsetY) );
				
				// Get prodnote text/image description.
				imgDescTextBox.setText( imgDesc.getCurProdText() );
				
			} // widgetSelected()
			
		}); // nextBtn.addSelectionListener...
		
	} // public void createUIelements()
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Creates an image from another to fit a particular resolution, without 
	// changing the aspect ratio. Scales up or down to match values given.
	public Image createScaledImage(Image img, int maxWidth, int maxHeight)
	{
		// Calc percentage needed to match max width.
		float percW = ((maxWidth * 100) / img.getImageData().width) / 100.0f;
		
		// Store new dimensions.
		int newWidth = maxWidth;
		int newHeight = (int)(img.getImageData().height * percW);
		
		// Calculate new dimensions based on height.
		if(newHeight > maxHeight)
		{
			// Calc percentage needed to match max height.
			float percH = ((maxHeight * 100) / img.getImageData().height) / 100.0f;
			
			// Store new dimensions.
			newWidth = (int)(img.getImageData().width * percH);
			newHeight = maxHeight;
			
		} // if(newHeight > maxHeight)

		// Return a new, scaled image.
		return new Image( null, img.getImageData().scaledTo(newWidth, newHeight) );
		
	} // createScaledImage()
	
} // public class ImageDescriberDialog extends Dialog
