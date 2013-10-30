package org.brailleblaster.perspectives.imageDescriber.views;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.brailleblaster.perspectives.imageDescriber.document.ImageDescriber;
import org.brailleblaster.util.ImageHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ImageDescriberView {
	// UI Positioning and Sizes.
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
	int undoBtnX = okayBtnW + okayBtnX + 1;
	int undoBtnY = 0;
	int undoBtnW = defBtnW;
	int undoBtnH = defBtnH;
	int applyAllBtnX = 0; // Apply All.
	int applyAllBtnY = okayBtnY + okayBtnH + 1;
	int applyAllBtnW = defBtnW;
	int applyAllBtnH = defBtnH;
	int clearAllBtnX = 0; // Clear All.
	int clearAllBtnY = applyAllBtnY + applyAllBtnH + 1;
	int clearAllBtnW = defBtnW;
	int clearAllBtnH = defBtnH;
	
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
	
	ImageDescriber imgDesc;
	ImageDescriberController idd;
	Group group;
	Button prevBtn, nextBtn, applyBtn, okayBtn, undoAllBtn, applyToAllBtn, clearAllBtn;
	Text imgDescTextBox;
	Browser browser;
	ImageHelper imgHelper;
	Label mainImage;
	
	public ImageDescriberView(Group group, ImageDescriber imgDesc, ImageDescriberController idd){
		this.group = group;
		this.imgDesc = imgDesc;
		this.imgHelper = new ImageHelper();
		this.idd = idd;
		setUIDimensions();
		createUIelements();
		toggleUI();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Creates all buttons, boxes, checks, etc.
	public void createUIelements()
	{
		
		// Create previous button.
		prevBtn = new Button(group, SWT.PUSH);
		prevBtn.setText("Previous");
		//prevBtn.setBounds(prevBtnX,  prevBtnY, prevBtnW, prevBtnH);
		setFormData(prevBtn, 0, 7, 0, 5);
		prevBtn.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {

			// Change main image to previous element image.
			imgDesc.prevImageElement();

			//Change current image in dialog.
			Image curElmImage = imgDesc.getCurElementImage();
			if(curElmImage != null)
				mainImage.setImage( imgHelper.createScaledImage(curElmImage, imageWidth, imageHeight) );
			else
				mainImage.setImage( imgHelper.createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
						imageWidth, imageHeight) );

			// Get prodnote text/image description.
			imgDescTextBox.setText( imgDesc.getCurProdText() );

			idd.setImageInfo();
			// Show current image index and name.
			//imgDescShell.setText("Image Describer - " + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );
			
		} // widgetSelected()

		}); // prevBtn.addSelectionListener...

		// Create next button.
		nextBtn = new Button(group, SWT.PUSH);
		nextBtn.setText("Next");
		//nextBtn.setBounds(nextBtnX,  nextBtnY, nextBtnW, nextBtnH);
		setFormData(nextBtn, 7, 14, 0, 5);
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
							imageWidth, imageHeight) );

				// Get prodnote text/image description.
				imgDescTextBox.setText( imgDesc.getCurProdText() );

				idd.setImageInfo();
				//Show current image index and name.
			//	imgDescShell.setText("Image Describer - " + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );

			} // widgetSelected()

		}); // nextBtn.addSelectionListener...

		// Create apply button.
		applyBtn = new Button(group, SWT.PUSH);
		applyBtn.setText("Apply");
		//applyBtn.setBounds(applyBtnX,  applyBtnY, applyBtnW, applyBtnH);
//		setFormData(applyBtn, 14, 21, 0, 5);
		setFormData(applyBtn, 21, 28, 0, 5);
		applyBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				// Set image's description.
				imgDesc.setCurElmProd(imgDescTextBox.getText(), null, null, null);
				idd.setDocumentEdited(true);
			} // widgetSelected()

		}); // applyBtn.addSelectionListener...

		// Create okay button.
		/*
		okayBtn = new Button(group, SWT.PUSH);
		okayBtn.setText("Okay");
		//okayBtn.setBounds(okayBtnX,  okayBtnY, okayBtnW, okayBtnH);
		setFormData(okayBtn, 21, 28, 0, 5);
		okayBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				// Set image's description.
				imgDesc.setCurElmProd(imgDescTextBox.getText(), null, null, null);

				//Close the dialog.
				//imgDescShell.close();

			} // widgetSelected()

		}); // okayBtn.addSelectionListener...
		*/

		// Create undo all button.
		undoAllBtn = new Button(group, SWT.PUSH);
		undoAllBtn.setText("Undo All");
		//undoAllBtn.setBounds(undoBtnX,  undoBtnY, undoBtnW, undoBtnH);
		setFormData(undoAllBtn, 28, 35, 0, 5);
		undoAllBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				// Warn user that all changes will be discarded.
				if( msgBx("Warning", "This will discard all changes, even the ones you Apply'd. Continue?") == true)
				{
					// Copy original elements back into main list. "Undo!"
					imgDesc.copyUndo2MainList();

					// Close the dialog without committing changes.
					//imgDescShell.close();
				}

			} // widgetSelected()

		}); // undoAllBtn.addSelectionListener...

		// Apply to all button. Finds every image with this name and changes description
		// to what was in the notes.
		applyToAllBtn = new Button(group, SWT.PUSH);
		applyToAllBtn.setText("Apply To All");
		//applyToAllBtn.setBounds(applyAllBtnX,  applyAllBtnY, applyAllBtnW, applyAllBtnH);
		setFormData(applyToAllBtn, 35, 42, 0, 5);
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
					idd.setDocumentEdited(true);
				} // if msgBx == true

			} // widgetSelected()

		}); // applyToAll.addSelectionListener...

		// Clear all button. Clears the prodnote and alt attribute.
		clearAllBtn = new Button(group, SWT.PUSH);
		clearAllBtn.setText("Clear All");
		//clearAllBtn.setBounds(clearAllBtnX,  clearAllBtnY, clearAllBtnW, clearAllBtnH);
		setFormData(clearAllBtn, 42, 49, 0, 5);
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
		imgDescTextBox = new Text(group, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		//imgDescTextBox.setBounds(txtBoxX, txtBoxY, txtBoxW, txtBoxH);
		setFormData(imgDescTextBox, 0, 49, 0, 40);
		imgDescTextBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) { 

			} // modifyText()

		}); // addModifyListener(new ModiftyListener() { 

		
		//set TextBox
		setTextBox();
		
		// Setup main image.
		mainImage = new Label(group, SWT.CENTER | SWT.BORDER);
		//mainImage.setBounds(imageOffsetX, imageOffsetY, imageWidth, imageHeight);
		setFormData(mainImage, 0, 49, 40, 100);
		setMainImage();

		// Show current image index and name.
		//imgDescShell.setText( "Image Describer - " + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );

		//////////////////
		// Browser Widget.

		// Setup browser window.
		browser = new Browser(group, SWT.BORDER );
		setBrowser();
		
	} // public void createUIelements()
	
	public void setTextBox(){
		// Get prodnote text/image description.
		if(imgDesc.getImageList().size() > 0)
			imgDescTextBox.setText( imgDesc.getCurProdText() );
	}
	
	public void setMainImage(){
		// Set main image to first image found. If the first <img> tag 
		Image curElmImage = imgDesc.getCurElementImage();
		if(curElmImage != null)
			mainImage.setImage( imgHelper.createScaledImage(curElmImage, imageWidth, imageHeight) );
		else
			mainImage.setImage( imgHelper.createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
				imageWidth, imageHeight) );
		
		idd.setImageInfo();
	}
	
	public void setBrowser(){
		// Create copy of file as html and load into browser widget.
		if(idd.getWorkingPath() != null && imgDesc.getImageList().size() > 0) {
			// Make copy of the file.
		    File fin = new File(idd.getWorkingPath());
		    File fout = new File(idd.getWorkingPath().replaceAll(".xml", ".html"));

            try
            {
    		    InputStream input = null;
    	        OutputStream output = null;
				input = new FileInputStream(fin);
				output = new FileOutputStream(fout);
		        byte[] buf = new byte[1024];
		        int bytesRead;
				while ((bytesRead = input.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
				}
				input.close();
	            output.close();
			}
			catch (FileNotFoundException e1) { e1.printStackTrace();} 
            catch (IOException e1) { e1.printStackTrace(); }

			// Set url.
			browser.setUrl( idd.getWorkingPath().replaceAll(".xml", ".html") );
			// Set browser bounds.
			//browser.setBounds(browserX, browserY, browserW, browserH);
			setFormData(browser, 49, 100, 0, 100);
			// Browser Widget.
			//////////////////
		}
		else {
			// Set browser bounds.
			//	browser.setUrl("www.google.com");
			//browser.setBounds(browserX, browserY, browserW, browserH);
			browser.setText("<h1>Empty Document</h1><h1>Browser View Currently Disabled</h1>");
			setFormData(browser, 49, 100, 0, 100);
		}
	}
	
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
		clientWidth = group.getShell().getBounds().width;
		clientHeight = group.getShell().getBounds().height;
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
		undoBtnX = okayBtnW + okayBtnX + 1;
		undoBtnY = 0;
		undoBtnW = defBtnW;
		undoBtnH = defBtnH;
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
		LocaleHandler lh = new LocaleHandler();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		msgBxBool = false;
		Display dlgDisp;
		final Shell dlgShl;
		dlgDisp = group.getDisplay();
		dlgShl = new Shell(group.getShell(), SWT.WRAP);
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
	
	private void setFormData(Control c, int left, int right, int top, int bottom){
		FormData data = new FormData();
		data.left = new FormAttachment(left);
		data.right = new FormAttachment(right);
		data.top = new FormAttachment(top);
		data.bottom = new FormAttachment(bottom);
		c.setLayoutData(data);
	}
	
	public void disposeUI(){
		group.dispose();
	}
	
	private void toggleUI(){
		boolean enabled = false;
		
		if(imgDesc.getImageList().size() > 0)
			enabled = true;
		
	//	prevBtn.setEnabled(enabled);
	//	nextBtn.setEnabled(enabled);
	//	applyBtn.setEnabled(enabled);
	//	okayBtn.setEnabled(enabled);
	//	cancelBtn.setEnabled(enabled);
	//	applyToAllBtn.setEnabled(enabled);
	//	clearAllBtn.setEnabled(enabled);
		imgDescTextBox.setEditable(enabled);
	}
	
	public String getTextBoxValue(){
		return imgDescTextBox.getText();
	}
	
	public void setTextBox(String text){
		imgDescTextBox.setText(text);
	}
	
	public void resetViews(ImageDescriber imgDesc){
		this.imgDesc = imgDesc;
		setMainImage();
		setTextBox();
		setBrowser();
		toggleUI();
	}
}
