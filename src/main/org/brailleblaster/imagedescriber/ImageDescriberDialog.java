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
import org.eclipse.swt.browser.Browser;

///////////////////////////////////////////////////////////////////////////////////////////
// Simple dialog that displays images in a document, and allows one
// to modify the descriptions.
public class ImageDescriberDialog extends Dialog {
	
	// Dialog stuff.
	static Display display;
	static Shell imgDescShell;
	LocaleHandler lh = new LocaleHandler();
	WPManager wpm;
	DocumentManager curDocMan;
	
	// UI Elements.
	Button nextBtn;
	Button prevBtn;
	Button cancelBtn;
	Button applyBtn;
	Button okayBtn;
	Label mainImage;
	Text imgDescTextBox;
	Browser browser = null;
	
	// The image describer.
	ImageDescriber imgDesc;
	
	// UI Positioning and Sizes.
	
	// Overall dialog.
	int dialogWidth = 1000;
	int dialogHeight = 700;
	// Main image.
	int imageOffsetX = 0;
	int imageOffsetY = 250;
	int imageWidth = 500;
	int imageHeight = 500;
	// Client Area.
	int clientWidth = -1;
	int clientHeight = -1;
	// Buttons.
	int defBtnW = 100;
	int defBtnH = 50;
	int prevBtnX = 0;
	int prevBtnY = 0;
	int prevBtnW = defBtnW;
	int prevBtnH = defBtnH;
	int nextBtnX = prevBtnW + prevBtnX + 1;
	int nextBtnY = 0;
	int nextBtnW = defBtnW;
	int nextBtnH = defBtnH;
	int applyBtnX = nextBtnW + nextBtnX + 1;
	int applyBtnY = 0;
	int applyBtnW = defBtnW;
	int applyBtnH = defBtnH;
	int okayBtnX = applyBtnW + applyBtnX + 1;
	int okayBtnY = 0;
	int okayBtnW = defBtnW;
	int okayBtnH = defBtnH;
	int cancelBtnX = okayBtnW + okayBtnX + 1;
	int cancelBtnY = 0;
	int cancelBtnW = defBtnW;
	int cancelBtnH = defBtnH;
	// Text box.
	int txtBoxX = 0;
	int txtBoxY = 55;
	int txtBoxW = 400;
	int txtBoxH = 150;
	// Browser.
	int browserX = 505;
	int browserY = 0;
	int browserW = -1;
	int browserH = -1;
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Constructor.
	public ImageDescriberDialog(Shell parent, int style, WPManager wordProcesserManager) {
		
		// SUPER!
		super(parent, style);
	
		// Store word processor.
		wpm = wordProcesserManager;
		
		// Make sure a document is open before we do anything.
		
		////////////////////////////
		// Grab current doc manager.
		
			curDocMan = null;
			int index= wpm.getFolder().getSelectionIndex();
			if(index == -1){
				wpm.addDocumentManager(null);
				curDocMan = wpm.getList().getFirst();
			}
			else {
				curDocMan = wpm.getList().get(index);
			}
		
		// Grab current doc manager.
		////////////////////////////
			
		// Create shell, get display, etc.
		display = wpm.getDisplay();
		display = parent.getDisplay();
		imgDescShell = new Shell(parent, SWT.DIALOG_TRIM);
		imgDescShell.setText(lh.localValue("Image Describer"));
		
		// Resize window.
		imgDescShell.setSize(dialogWidth, dialogHeight);
		clientWidth = imgDescShell.getClientArea().width;
		clientHeight = imgDescShell.getClientArea().height;
		
		// Start the image describer.
		imgDesc = new ImageDescriber(curDocMan);
			
		// Create all of the buttons, edit boxes, etc.
		createUIelements();
		
		///////////////////
		// Run this dialog.
		
			// show the SWT window
			imgDescShell.pack();
			
			// Open and Run!
			imgDescShell.open();
			while (!imgDescShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

		// Run this dialog.
		///////////////////
			
		// Shutdown.
		imgDescShell.dispose();
		imgDesc.disposeImages();
		mainImage.dispose();
		
	} // public ImageDescriberDialog(Shell arg0, int arg1)
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Creates all buttons, boxes, checks, etc.
	public void createUIelements()
	{
		// Setup main image.
		mainImage = new Label(imgDescShell, SWT.NONE);
		mainImage.setBounds(imageOffsetX, imageOffsetY, imageWidth, imageHeight);
		
		// Set main image to first image found. If the first <img> tag 
		Image curElmImage = imgDesc.getCurElementImage();
		if(curElmImage != null)
			mainImage.setImage( createScaledImage(curElmImage, imageWidth, imageHeight) );
		else
			mainImage.setImage( createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
												  imageWidth, 
												  imageHeight) );

		// Show current image index and name.
		imgDescShell.setText( "Image Describer - " + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );
		
		// Create image description text box.
		imgDescTextBox = new Text(imgDescShell, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		imgDescTextBox.setBounds(txtBoxX, txtBoxY, txtBoxW, txtBoxH);
		imgDescTextBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				
				
				
			} // modifyText()
			
		}); // addModifyListener(new ModiftyListener() { 
		
		
		// Get prodnote text/image description.
		imgDescTextBox.setText( imgDesc.getCurProdText() );
		
		
		// Create previous button.
		prevBtn = new Button(imgDescShell, SWT.PUSH);
		prevBtn.setText("Previous");
		prevBtn.setBounds(prevBtnX,  prevBtnY, prevBtnW, prevBtnH);
		prevBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Change main image to previous element image.
				imgDesc.prevImageElement();
				
				// Change current image in dialog.
				Image curElmImage = imgDesc.getCurElementImage();
				if(curElmImage != null)
					mainImage.setImage( createScaledImage(curElmImage, imageWidth, imageHeight) );
				else
					mainImage.setImage( createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
														  imageWidth, 
														  imageHeight) );
				
				// Get prodnote text/image description.
				imgDescTextBox.setText( imgDesc.getCurProdText() );
				
				// Show current image index and name.
				imgDescShell.setText("Image Describer - " + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );
				
			} // widgetSelected()
			
		}); // prevBtn.addSelectionListener...
		
		// Create next button.
		nextBtn = new Button(imgDescShell, SWT.PUSH);
		nextBtn.setText("Next");
		nextBtn.setBounds(nextBtnX,  nextBtnY, nextBtnW, nextBtnH);
		nextBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Change main image to next element image.
				imgDesc.nextImageElement();
				
				// Change current image in dialog.
				Image curElmImage = imgDesc.getCurElementImage();
				if(curElmImage != null)
					mainImage.setImage( createScaledImage(curElmImage, imageWidth, imageHeight) );
				else
					mainImage.setImage( createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
														  imageWidth, 
														  imageHeight) );
				
				// Get prodnote text/image description.
				imgDescTextBox.setText( imgDesc.getCurProdText() );
				
				// Show current image index and name.
				imgDescShell.setText("Image Describer - " + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );
				
			} // widgetSelected()
			
		}); // nextBtn.addSelectionListener...
		
		// Create apply button.
		applyBtn = new Button(imgDescShell, SWT.PUSH);
		applyBtn.setText("Apply");
		applyBtn.setBounds(applyBtnX,  applyBtnY, applyBtnW, applyBtnH);
		applyBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Set image's description.
				imgDesc.setCurElmProd(imgDescTextBox.getText(), null, null, null);
				
			} // widgetSelected()
			
		}); // applyBtn.addSelectionListener...
		
		// Create okay button.
		okayBtn = new Button(imgDescShell, SWT.PUSH);
		okayBtn.setText("Okay");
		okayBtn.setBounds(okayBtnX,  okayBtnY, okayBtnW, okayBtnH);
		okayBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Set image's description.
				imgDesc.setCurElmProd(imgDescTextBox.getText(), null, null, null);
				
				// Close the dialog.
				imgDescShell.close();
				
			} // widgetSelected()
			
		}); // okayBtn.addSelectionListener...
		
		// Create cancel button.
		cancelBtn = new Button(imgDescShell, SWT.PUSH);
		cancelBtn.setText("Cancel");
		cancelBtn.setBounds(cancelBtnX,  cancelBtnY, cancelBtnW, cancelBtnH);
		cancelBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Close the dialog without committing changes.
				imgDescShell.close();
				
			} // widgetSelected()
			
		}); // cancelBtn.addSelectionListener...
		
		// Setup browser window.
		browser = new Browser( imgDescShell, SWT.NONE );
		browser.setUrl( curDocMan.getWorkingPath() );
		browserW = clientWidth;
		browserH = clientHeight;
		browser.setBounds(browserX, browserY, browserW, browserH);
		
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
