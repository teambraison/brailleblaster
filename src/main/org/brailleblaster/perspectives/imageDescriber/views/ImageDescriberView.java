package org.brailleblaster.perspectives.imageDescriber.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.imageDescriber.Falconator;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.brailleblaster.perspectives.imageDescriber.document.ImageDescriber;
import org.brailleblaster.util.ImageHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ImageDescriberView {
	
	int imageWidth = 500;
	int imageHeight = 500;
	// Client Area.
	int clientWidth = -1;
	int clientHeight = -1;
	
	// True if usr hit okay. False if cancel.
	boolean msgBxBool = false;
	
	ImageDescriber imgDesc;
	ImageDescriberController idd;
	Group group;
	Button prevBtn, nextBtn, useSimpleStylesCheck, applyBtn, undoBtn, applyToAllBtn, clearAllBtn;
	Button nextPage, prevPage;
	Text imgDescTextBox;
	Label altLabel;
	Text altBox;
	Browser browser;
	ImageHelper imgHelper;
	Label mainImage;
	String oldDescText = "";
	String oldAltText = "";
	String oldCssHref = null;
	String curBrowserFilePath = null;
	Falconator hw = new Falconator("C:\\APPS\\cygwin\\home\\cmyers\\brailleblaster\\src\\main\\org\\brailleblaster\\perspectives\\imageDescriber\\Falconator.dll");
	
	// Script that will add "positions" to <img> tags.
	String posScript = 
	"var allLinks = document.getElementsByTagName('img');" + 
    "for (var i=0, il=allLinks.length; i<il; i++)" + 
    "{ elm = allLinks[i]; elm.setAttribute('posY', elm.getBoundingClientRect().top); }";
	
	public ImageDescriberView(Group group, ImageDescriber imgDesc, ImageDescriberController idd){
		this.group = group;
		this.imgDesc = imgDesc;
		this.imgHelper = new ImageHelper();
		this.idd = idd;
		setUIDimensions();
		createUIelements();
//		toggleUI();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Creates all buttons, boxes, checks, etc.
	public void createUIelements()
	{
		
		// Create previous button.
		prevBtn = new Button(group, SWT.PUSH);
		prevBtn.setText("Previous");
		setFormData(prevBtn, 0, 7, 0, 5);
		prevBtn.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {

			// Move to previous element.
			idd.setImageToPrevious();
			
		} // widgetSelected()

		}); // prevBtn.addSelectionListener...

		// Create next button.
		nextBtn = new Button(group, SWT.PUSH);
		nextBtn.setText("Next");
		setFormData(nextBtn, 7, 14, 0, 5);
		nextBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Move to next element.
				idd.setImageToNext();

			} // widgetSelected()

		}); // nextBtn.addSelectionListener...
		
		// Check box for simple style sheets. When checked, 
		// we'll use our own style sheet instead of the given 
		// one. This can help with documents not being displayed 
		// properly in the browser view. In particular, 
		// overlapping text.
		useSimpleStylesCheck = new Button(group, SWT.CHECK);
		useSimpleStylesCheck.setText("Simple Style");
		setFormData(useSimpleStylesCheck, 17, 28, 0, 5);
		useSimpleStylesCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				 // Script to replace current style sheet.
	            String script = 
   		        "var allsuspects=document.getElementsByTagName(\"link\")" + 
   		        "for (var i=allsuspects.length; i>=0; i--){" + 
       		        "if (allsuspects[i] && allsuspects[i].getAttribute(\"href\")!=null && allsuspects[i].getAttribute(\"href\").indexOf(OLDFILENAME)!=-1){" + 
       		        "var newelement=document.createElement(\"link\");" +
       		        "newelement.setAttribute(\"rel\", \"stylesheet\");" +
       		        "newelement.setAttribute(\"type\", \"text/css\")" + 
       		        "newelement.setAttribute(\"href\", NEWFILENAME)" + 
       		        "allsuspects[i].parentNode.replaceChild(newelement, allsuspects[i]);" + 
       		        "}" + 
   		        "}";
	            
	         // Get namespace and context so we can search for "link."
	            String nameSpace = idd.getDoc().getRootElement().getDocument().getRootElement().getNamespaceURI();
	    		XPathContext context = new XPathContext("dtb", nameSpace);
	    		// Search for first "link" element.
	            Nodes linkElms = idd.getDoc().getRootElement().query("//dtb:link[1]", context);
	            
	            // If there are no link elements, just leave.
	            if(linkElms == null)
	            	return;
	            if(linkElms.size() == 0)
	            	return;
	            
	            // Get the name/path to the css file.
	            if(oldCssHref == null)
	            	oldCssHref = ((Element)linkElms.get(0)).getAttributeValue("href");
	            
	            // Check/uncheck.
				Button button = (Button)e.widget;
		        if (button.getSelection())
		        {
		        	// Check the box.
		            button.setSelection(true);
	        		
		            // Insert the file name at the beginning of the script.
		            script = 
		            "var OLDFILENAME=\"" + oldCssHref + "\";" + 
		            "var NEWFILENAME=\"IDONOTEXIST.css\";" + 
		            script;
		            
		            // Execute script to remove css.
		            browser.execute(script);
		        }
		        else
		        {
		        	// Uncheck the box.
		        	button.setSelection(false);
		            
		            // Insert the file name at the beginning of the script.
		            script = 
		            "var OLDFILENAME=\"IDONOTEXIST.css\";" + 
		            "var NEWFILENAME=\"" + oldCssHref + "\";" + 
		            script;
		        	
		        	// Execute script to remove css.
		            browser.execute(script);
		        }
			}
		});

		// Create undo button.
		undoBtn = new Button(group, SWT.PUSH);
		undoBtn.setText("Undo");
		setFormData(undoBtn, 28, 35, 0, 5);
		undoBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Undo current element changes.
				idd.undo();

			} // widgetSelected()

		}); // undoBtn.addSelectionListener...

		// Apply to all button. Finds every image with this name and changes description
		// to what was in the notes.
		applyToAllBtn = new Button(group, SWT.PUSH);
		applyToAllBtn.setText("Apply To All");
		setFormData(applyToAllBtn, 35, 42, 0, 5);
		applyToAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Warn user before doing this. It could take a while.
				if( msgBx("Warning", "Image Describer will update every image like this one with the given description. This could take a while. Continue?") == true)
				{
					// Apply what is in the edit box first.
					imgDesc.setDescription(imgDescTextBox.getText(), null, null, null);

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
							imgDesc.setDescAtIndex(curImg, imgDescTextBox.getText(), null, null, null);

						} // if( imgDesc.getElementAtIndex...

					} // for(int curImg...
					idd.setDocumentEdited(true);
					
				} // if msgBx == true

			} // widgetSelected()

		}); // applyToAll.addSelectionListener...

		// Clear all button. Clears the prodnote and alt attribute.
		clearAllBtn = new Button(group, SWT.PUSH);
		clearAllBtn.setText("Clear All");
		setFormData(clearAllBtn, 42, 49, 0, 5);
		clearAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Clear every description for this image, and clear alt text.
				if( msgBx("Warning", "All images like this one will have their description cleared, and alt text removed. This could take a while. Continue?") == true)
				{
					// Apply what is in the edit box first.
					imgDesc.setDescription("", null, null, null);
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
							imgDesc.setDescAtIndex(curImg, "", null, null, null);
							// Change alt text.
							imgDesc.setElementAttributesAtIndex(curImg, null, null, "");

						} // if( imgDesc.getElementAtIndex...

					} // for(int curImg...

				} // if msgBx == true

			} // widgetSelected()

		}); // clearAllBtn.addSelectionListener

		// Label for alt box.
		altLabel = new Label(group, SWT.NONE);
		altLabel.setText("Alt Text ->");
		setFormData(altLabel, 1, 6, 5, 9);
		
		// The alt box is for updating the "alt text" in an image element.
		altBox = new Text(group, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		setFormData(altBox, 6, 49, 5, 9);
		if(imgDesc.getCurElmAttribute("alt") != null)
			altBox.setText(imgDesc.getCurElmAttribute("alt"));
		altBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) { 
					imgDesc.setCurElmImgAttributes( null, null, altBox.getText() );
					idd.setDocumentEdited(true);
			} // modifyText()

		}); // addModifyListener(new ModiftyListener() { 

		// Create image description text box.
		imgDescTextBox = new Text(group, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		setFormData(imgDescTextBox, 0, 49, 9, 40);
		imgDescTextBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) { 
					imgDesc.setDescription(imgDescTextBox.getText(), null, null, null);
					idd.setDocumentEdited(true);
			} // modifyText()

		}); // addModifyListener(new ModiftyListener() { 
		
		//set TextBox
		setTextBox(imgDesc.getCurDescription());
		
		// Setup main image.
		mainImage = new Label(group, SWT.CENTER | SWT.BORDER);
		setFormData(mainImage, 0, 49, 40, 100);
		setMainImage();
		
		// Set up browser navigation buttons.
		
		// Next page/chapter/file button.
		nextPage = new Button(group, SWT.PUSH);
		nextPage.setText("Next Page >>");
		setFormData(nextPage, 75, 85, 0, 5);
		nextPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				curBrowserFilePath = idd.nextSpineFilePath();
				setBrowser();
				idd.setImageToNext();
			}
		});
		
		// Previous page/chapter/file button.
		prevPage = new Button(group, SWT.PUSH);
		prevPage.setText("<< Previous Page"); 
		setFormData(prevPage, 65, 75, 0, 5);
		prevPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				curBrowserFilePath = idd.prevSpineFilePath();
				setBrowser();
				idd.setImageToPrevious();
			}
		});

		// Setup browser window.
		browser = new Browser(group, SWT.BORDER);
		setBrowser();
		setFormData(browser, 49, 100, 6, 100);
		
	} // public void createUIelements()
	
	// Enables the browser navigation buttons.
	public void enableBrowserNavButtons() {
		nextPage.setEnabled(true);
		prevPage.setEnabled(true);
	} // enableBrowserNavButtons()
	
	// Disables the browser navigation buttons.
	public void disableBrowserNavButtons() {
		nextPage.setEnabled(false);
		prevPage.setEnabled(false);
	} // disableBrowserNavButtons()
	
	// Set text in image description text box UI.
	public void setTextBox(){
		// Get prodnote text/image description.
		if(imgDesc.getImageList().size() > 0)
			imgDescTextBox.setText( imgDesc.getCurDescription() );
	}
	
	// Get text in alt box UI.
	public String getAltBox()
	{
		return altBox.getText();
	}
	
	// Set text in alt box.
	public void setAltBox(String str)
	{
		if(str == null)
			return;
		altBox.setText(str);
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Sets the main image to the current in the image list from the document.
	public void setMainImage(){
		
		// Set image widget to current element's image. 
		Image curElmImage = imgDesc.getCurElementImage();
		
		// If this is a valid image, set the widget. Else, display error image.
		if(curElmImage != null) {
			// If the image is larger than the widget size, scale down. Otherwise, just display it.
			if(curElmImage.getBounds().width > imageWidth || curElmImage.getBounds().height > imageHeight)
				mainImage.setImage( imgHelper.createScaledImage(curElmImage, imageWidth, imageHeight) );
			else
				mainImage.setImage( imgHelper.createScaledImage(curElmImage, curElmImage.getBounds().width, curElmImage.getBounds().height) );
		}
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
			catch (FileNotFoundException e1) { e1.printStackTrace(); } 
            catch (IOException e1) { e1.printStackTrace(); }

            // Progress listener. Adds javascript code that will modify our img elements with 
            // height information.
            browser.addProgressListener(new ProgressListener() 
            {
                @Override
				public void changed(ProgressEvent event) { }
                
                @Override
				public void completed(ProgressEvent event) {
	            	
	            	 // Execute the script.
	                 browser.execute(posScript);
                }
            });
            
			// Get path to full file.
            curBrowserFilePath = fout.getAbsolutePath();
            
            // If there are spine paths, that means there are multiple 
            // files to load.
        	if(idd.getCurSpineFilePath() != null)
        		curBrowserFilePath = idd.getCurSpineFilePath();
        	
        	// Finally, jam the file into the browser widget.
            browser.setUrl( curBrowserFilePath );
            
			// Set browser bounds.
            setFormData(browser, 49, 100, 6, 100);
		}
		else {
			// Set browser bounds.
			browser.setText("<h1>Empty Document</h1><h1>Browser View Currently Disabled</h1>");
			setFormData(browser, 49, 100, 6, 100);
		}
	}
	
	////////////////////////////////////////////////////////////
	// Scrolls browser view to current image.
	public void scrollBrowserToCurImg()
	{
		// Get the index of the current element.
		String indexStr = Integer.toString( imgDesc.getCurrentElementIndex() );
		
		// Create script.
		String s = "var allLinks = document.getElementsByTagName('img'); " +
                "elm = allLinks[" + indexStr + "];" +
                "window.scrollTo(0, elm.posY);";
   	 
		// Execute script.
        browser.execute(s);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Resizes our widgets depending on screen resolution.
	public void setUIDimensions()
	{
		// Client Area.
		clientWidth = group.getShell().getBounds().width;
		clientHeight = group.getShell().getBounds().height;
		// Main image.
		imageWidth = clientWidth / 4;
		imageHeight = clientWidth / 4;
		
	} // public void resizeUI()


	///////////////////////////////////////////////////////////////////////////////////////////
	// Simple message box for alerts and messages to user.
	public boolean msgBx(String cap, String msg)
	{
		// Tell user there are no image tags.
		LocaleHandler lh = new LocaleHandler();
		Monitor mon[] = Display.getDefault().getMonitors();
		Rectangle screenSize = mon[0].getBounds();
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
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// User hit okay.
				msgBxBool = true;
				
				// Close dialog.
				dlgShl.close();
				
			} // widgetSelected()
			
		}); // okBtn.addSelectionListener...
		Button canBtn = new Button(dlgShl, SWT.PUSH);
		canBtn.setText("Cancel");
		canBtn.setBounds(101, 75, 100, 25);
		canBtn.addSelectionListener(new SelectionAdapter() {
			@Override
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
		dlgShl.setSize((int)screenSize.width / 5, (int)screenSize.width / 12);
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
		
		// Change font size depending on screen resolution.
		Monitor mon[] = Display.getDefault().getMonitors();
		Rectangle screenSize = mon[0].getBounds();
		FontData[] oldFontData = c.getFont().getFontData();
		if( (int)screenSize.width >= 1920)
			oldFontData[0].setHeight(10);
		else if( (int)screenSize.width >= 1600)
			oldFontData[0].setHeight(9);
		else if( (int)screenSize.width >= 1280)
			oldFontData[0].setHeight(7);
		else if( (int)screenSize.width >= 1024)
			oldFontData[0].setHeight(5);
		else if( (int)screenSize.width >= 800)
			oldFontData[0].setHeight(4);
		c.setFont( new Font(null, oldFontData[0]) );
	}
	
	public void disposeUI(){
		group.dispose();
	}
	
	private void toggleUI(){
		boolean enabled = false;
		
		if(imgDesc.getImageList().size() > 0)
			enabled = true;

		imgDescTextBox.setEditable(enabled);
	}
	
	public String getTextBoxValue(){
		return imgDescTextBox.getText();
	}
	
	public void setTextBox(String text){
		if(text != null)
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