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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.ImageHelper;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import static java.nio.file.StandardCopyOption.*;

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
	Button applyToAllBtn;
	Button clearAllBtn;
	Label mainImage;
	Text imgDescTextBox;
	Browser browser = null;
	
	// The image describer.
	ImageDescriber imgDesc;
	
	// Helps with managing and manipulating images.
	ImageHelper imgHelper;
	
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
	int applyAllBtnX = 0; // Apply All.
	int applyAllBtnY = okayBtnY + okayBtnH + 1;
	int applyAllBtnW = defBtnW;
	int applyAllBtnH = defBtnH;
	int clearAllBtnX = 0; // Clear All.
	int clearAllBtnY = applyAllBtnY + applyAllBtnH + 1;
	int clearAllBtnW = defBtnW;
	int clearAllBtnH = defBtnH;
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
	
	// True if usr hit okay. False if cancel.
	boolean msgBxBool = false;
	
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
			
		// Start the image describer.
		imgDesc = new ImageDescriber(curDocMan);
		
		// Image helper class. Image helper functions, and such.
		imgHelper = new ImageHelper();
			
		// Create shell, get display, etc.
		display = parent.getDisplay();
		imgDescShell = new Shell(parent, SWT.DIALOG_TRIM);
		imgDescShell.setText(lh.localValue("Image Describer"));
		
		// Resize window.
		setUIDimensions();
		imgDescShell.setSize(dialogWidth, dialogHeight);		
		
		// If there were no <img> elements found, there is no point in continuing.
		if(imgDesc.getNumImgElements() == 0) {
			
			// Show user No Images message.
			msgBx("NO IMAGES!", "There are no image elements in this document.");
			
			// Don't bother with the rest of image describer, there are no images.
			return;
		}
		
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
					mainImage.setImage( imgHelper.createScaledImage(curElmImage, imageWidth, imageHeight) );
				else
					mainImage.setImage( imgHelper.createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
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
					mainImage.setImage( imgHelper.createScaledImage(curElmImage, imageWidth, imageHeight) );
				else
					mainImage.setImage( imgHelper.createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
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
				
				// Warn user that all changes will be discarded.
				if( msgBx("Warning", "This will discard all changes, even the ones you Apply'd. Continue?") == true)
				{
					// Copy original elements back into main list. "Undo!"
					imgDesc.copyUndo2MainList();
					
					// Close the dialog without committing changes.
					imgDescShell.close();
				}
				
			} // widgetSelected()
			
		}); // cancelBtn.addSelectionListener...

		// Apply to all button. Finds every image with this name and changes description
		// to what was in the notes.
		applyToAllBtn = new Button(imgDescShell, SWT.PUSH);
		applyToAllBtn.setText("Apply To All");
		applyToAllBtn.setBounds(applyAllBtnX,  applyAllBtnY, applyAllBtnW, applyAllBtnH);
		applyToAllBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Warn user before doing this. It could take a while.
				if( msgBx("Warning", "Image Describer will update every image like this one with the given description. This could take a while. Continue?") == true)
				{
					// Apply what is in the edit box first.
					imgDesc.setCurElmProd(imgDescTextBox.getText(), null, null, null);
					
					// Current image path.
					String curImgPath = "";
					
					// Get current image path from src attribute.
					curImgPath = imgDesc.currentImageElement().getAttributeValue("src");
					
					// Get number of <img> elements.
					int numElms = imgDesc.getNumImgElements();
					
					// For each element, check if it has the same image path.
					for(int curImg = 0; curImg < numElms; curImg++)
					{
						// Is this <img> just like the current image path?
						if( imgDesc.getElementAtIndex(curImg).getAttributeValue("src").compareTo(curImgPath) == 0 )
						{
							// Change description to current prod text.
							imgDesc.setProdAtIndex(curImg, imgDescTextBox.getText(), null, null, null);
							
						} // if( imgDesc.getElementAtIndex...
						
					} // for(int curImg...
					
				} // if msgBx == true
				
			} // widgetSelected()
			
		}); // applyToAll.addSelectionListener...
		
		// Clear all button. Clears the prodnote and alt attribute.
		clearAllBtn = new Button(imgDescShell, SWT.PUSH);
		clearAllBtn.setText("Clear All");
		clearAllBtn.setBounds(clearAllBtnX,  clearAllBtnY, clearAllBtnW, clearAllBtnH);
		clearAllBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// Clear every description for this image, and clear alt text.
				if( msgBx("Warning", "All images like this one will have their description cleared, and alt text removed. This could take a while. Continue?") == true)
				{
					// Apply what is in the edit box first.
					imgDesc.setCurElmProd("", null, null, null);
					imgDescTextBox.setText("");
					
					// Current image path.
					String curImgPath = "";
					
					// Get current image path from src attribute.
					curImgPath = imgDesc.currentImageElement().getAttributeValue("src");
					
					// Get number of <img> elements.
					int numElms = imgDesc.getNumImgElements();
					
					// For each element, check if it has the same image path.
					for(int curImg = 0; curImg < numElms; curImg++)
					{
						// Is this <img> just like the current image path?
						if( imgDesc.getElementAtIndex(curImg).getAttributeValue("src").compareTo(curImgPath) == 0 )
						{
							// Change description to current prod text.
							imgDesc.setProdAtIndex(curImg, "", null, null, null);
							// Change alt text.
							imgDesc.setElementAttributesAtIndex(curImg, null, null, "");
							
						} // if( imgDesc.getElementAtIndex...
						
					} // for(int curImg...
					
				} // if msgBx == true
				
			} // widgetSelected()
			
		}); // clearAllBtn.addSelectionListener
		
		// Create image description text box.
		imgDescTextBox = new Text(imgDescShell, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		imgDescTextBox.setBounds(txtBoxX, txtBoxY, txtBoxW, txtBoxH);
		imgDescTextBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) { 
				
			} // modifyText()
			
		}); // addModifyListener(new ModiftyListener() { 
		
		
		
		// Get prodnote text/image description.
		imgDescTextBox.setText( imgDesc.getCurProdText() );
				
		// Setup main image.
		mainImage = new Label(imgDescShell, SWT.NONE);
		mainImage.setBounds(imageOffsetX, imageOffsetY, imageWidth, imageHeight);
		
		// Set main image to first image found. If the first <img> tag 
		Image curElmImage = imgDesc.getCurElementImage();
		if(curElmImage != null)
			mainImage.setImage( imgHelper.createScaledImage(curElmImage, imageWidth, imageHeight) );
		else
			mainImage.setImage( imgHelper.createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
														    imageWidth, 
														    imageHeight) );

		// Show current image index and name.
		imgDescShell.setText( "Image Describer - " + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );
		
		//////////////////
		// Browser Widget.
		
			// Setup browser window.
			browser = new Browser( imgDescShell, SWT.NONE );
			
			// Create copy of file as html and load into browser widget.
			File f  = new File(curDocMan.getWorkingPath());
			File f2 = new File(curDocMan.getWorkingPath().replaceAll(".xml", ".html"));
			Path src = f.toPath();
			Path dst = f2.toPath();
			try { Files.copy(src, dst, REPLACE_EXISTING ); }
			catch (IOException e1) { e1.printStackTrace(); }
			
			// Set url.
			 browser.setUrl( curDocMan.getWorkingPath().replaceAll(".xml", ".html") );
			// Set browser bounds.
			 browser.setBounds(browserX, browserY, browserW, browserH);

		 // Browser Widget.
		 //////////////////

	} // public void createUIelements()
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Resizes our widgets depending on screen resolution.
	public void setUIDimensions()
	{
		// Screen resolution.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// Overall dialog.
		dialogWidth = (int)(screenSize.getWidth() * 0.70f);
		dialogHeight = (int)(screenSize.getWidth() * 0.70f);
		// Client Area.
		clientWidth = imgDescShell.getClientArea().width;
		clientHeight = imgDescShell.getClientArea().height;
		// Buttons.
		defBtnW = dialogWidth / 15;
		defBtnH = dialogHeight / 25;
		prevBtnX = 0;
		prevBtnY = 0;
		prevBtnW = defBtnW;
		prevBtnH = defBtnH;
		nextBtnX = prevBtnW + prevBtnX + 1;
		nextBtnY = 0;
		nextBtnW = defBtnW;
		nextBtnH = defBtnH;
		applyBtnX = nextBtnW + nextBtnX + 1;
		applyBtnY = 0;
		applyBtnW = defBtnW;
		applyBtnH = defBtnH;
		okayBtnX = applyBtnW + applyBtnX + 1;
		okayBtnY = 0;
		okayBtnW = defBtnW;
		okayBtnH = defBtnH;
		cancelBtnX = okayBtnW + okayBtnX + 1;
		cancelBtnY = 0;
		cancelBtnW = defBtnW;
		cancelBtnH = defBtnH;
		applyAllBtnX = 0; // Apply All.
		applyAllBtnY = okayBtnY + okayBtnH + 1;
		applyAllBtnW = clientWidth / 12;
		applyAllBtnH = defBtnH;
		clearAllBtnX = applyAllBtnX + clientWidth / 12; // Clear All.
		clearAllBtnY = applyAllBtnY;
		clearAllBtnW = clientWidth / 12;
		clearAllBtnH = defBtnH;
		// Text box.
		txtBoxX = 0;
		txtBoxY = applyAllBtnY + applyAllBtnH + 1;
		txtBoxW = clientWidth / 3;
		txtBoxH = clientHeight / 4;
		// Main image.
		imageOffsetX = 0;
		imageOffsetY = txtBoxY + txtBoxH + 5;
		imageWidth = clientWidth / 3;
		imageHeight = clientWidth / 3;
		// Browser.
		browserX = imageWidth + 10;
		browserY = 0;
		browserW = clientWidth / 2;
		browserH = clientHeight;
		
	} // public void resizeUI()
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Simple message box for alerts and messages to user.
	public boolean msgBx(String cap, String msg)
	{
		// Tell user there are no image tags.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		msgBxBool = false;
		Display dlgDisp;
		final Shell dlgShl;
		dlgDisp = imgDescShell.getDisplay();
		dlgShl = new Shell(imgDescShell, SWT.WRAP);
		dlgShl.setText(lh.localValue(cap));
		Button okBtn = new Button(dlgShl, SWT.PUSH);
		okBtn.setText("Okay");
		okBtn.setBounds(0,  75, 100, 25);
		okBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// User hit okay.
				msgBxBool = true;
				
				// Close dialog.
				dlgShl.close();
				
			} // widgetSelected()
			
		}); // okBtn.addSelectionListener...
		Button canBtn = new Button(dlgShl, SWT.PUSH);
		canBtn.setText("Cancel");
		canBtn.setBounds(101,  75, 100, 25);
		canBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				// User hit okay.
				msgBxBool = false;
				
				// Close dialog.
				dlgShl.close();
				
			} // widgetSelected()
			
		}); // canBtn.addSelectionListener...
		Label alertText = new Label(dlgShl, SWT.WRAP);
		alertText.setBounds(0, 0, 250, 100);
		alertText.setText(msg);
		dlgShl.setSize((int)screenSize.getWidth() / 5, (int)screenSize.getWidth() / 12);
		dlgShl.open();
		while (!dlgShl.isDisposed()) {
			if (!dlgDisp.readAndDispatch())
				dlgDisp.sleep();
		}
		
		// Return message box boolean value. What did the user press...
		return msgBxBool;
		
	} // public void msgBx(String cap, string msg)
	
} // public class ImageDescriberDialog extends Dialog
