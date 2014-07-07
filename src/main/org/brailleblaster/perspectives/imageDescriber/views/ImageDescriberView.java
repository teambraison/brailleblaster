package org.brailleblaster.perspectives.imageDescriber.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//<<<<<<< local
//import java.nio.file.Files;
//import java.nio.file.Path;
//=======
//>>>>>>> other
import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.brailleblaster.BBIni;
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
import org.eclipse.swt.widgets.Text;

public class ImageDescriberView {
	
	// Font sizes for our controls.
	public int IDV_FONTSIZE_AUTO = 13;
	public int IDV_FONTSIZE_12 = 12;
	public int IDV_FONTSIZE_11 = 11;
	public int IDV_FONTSIZE_10 = 10;
	public int IDV_FONTSIZE_09 = 9;
	public int IDV_FONTSIZE_08 = 8;
	public int IDV_FONTSIZE_07 = 7;
	public int IDV_FONTSIZE_06 = 6;
	public int IDV_FONTSIZE_05 = 5;
	public int IDV_FONTSIZE_04 = 4;
	
	// Tells setFormData to not adjust the size of the control. If any(left/right/top/bottom) have 
	// this specified, it won't adjust it. Font will still be changed, though.
	public int IDV_CTRL_NOCHANGE = -1;
	
	int imageWidth = 500;
	int imageHeight = 500;
	// Client Area.
	int clientWidth = -1;
	int clientHeight = -1;
	
	ImageDescriber imgDesc;
	ImageDescriberController idd;
	Group group;
	Button prevBtn, nextBtn, useSimpleStylesCheck, undoBtn, applyToAllBtn, clearAllBtn;
	Button nextPage, prevPage;
	Label descLabel;
	Text imgDescTextBox;
	Label altLabel;
	Text altBox;
	boolean refreshOnce = true;
	Browser browser;
	ImageHelper imgHelper;
	Label mainImage;
	String oldDescText = "";
	String oldAltText = "";
	String oldCssHref = null;
	String curBrowserFilePath = null;
	
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
		setFormData(prevBtn, 0, 7, 0, 5, IDV_FONTSIZE_AUTO);
		prevBtn.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			
			// Move to previous element.
			idd.setImageToPrevious();
			
			// Grab index of current image.
			int imgIndex = idd.getImageDescriber().getCurrentElementIndex();
			
			// Is it time to move to another page/chapter?
			String newPath = idd.setSpineFileWithImgIndex(imgIndex);
			
			// If the page needs to move/change, update.
			// Otherwise, don't move the page yet.
			if(newPath != null && newPath.compareTo(curBrowserFilePath) != 0) {
				// Store path.
				curBrowserFilePath = newPath;
				// Update browser view.
				setBrowser();
			}
			
			// Force a scroll.
			scrollBrowserToCurImg();
			
		} // widgetSelected()

		}); // prevBtn.addSelectionListener...

		// Create next button.
		nextBtn = new Button(group, SWT.PUSH);
		nextBtn.setText("Next");
		setFormData(nextBtn, 7, 14, 0, 5, IDV_FONTSIZE_AUTO);
		nextBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Move to next element.
				idd.setImageToNext();
				
				// Grab index of current image.
				int imgIndex = idd.getImageDescriber().getCurrentElementIndex();
				
				// Is it time to move to another page/chapter?
				String newPath = idd.setSpineFileWithImgIndex(imgIndex);
				
				// If the page needs to move/change, update.
				// Otherwise, don't move the page yet.
				if(newPath != null && newPath.compareTo(curBrowserFilePath) != 0) {
					// Store path.
					curBrowserFilePath = newPath;
					// Update browser view.
					setBrowser();
				}

				// Scroll to current image.
				scrollBrowserToCurImg();

			} // widgetSelected()

		}); // nextBtn.addSelectionListener...
		
		// Check box for simple style sheets. When checked, 
		// we'll use our own style sheet instead of the given 
		// one. This can help with documents not being displayed 
		// properly in the browser view. In particular, 
		// overlapping text.
		useSimpleStylesCheck = new Button(group, SWT.CHECK);
		useSimpleStylesCheck.setText("Simple Style");
		setFormData(useSimpleStylesCheck, 17, 28, 0, 5, IDV_FONTSIZE_AUTO);
		useSimpleStylesCheck.setVisible(false);
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

		// Set up browser navigation buttons.
		
		// Previous page/chapter/file button.
		prevPage = new Button(group, SWT.PUSH);
		prevPage.setText("<< Previous Page"); 
		setFormData(prevPage, 16, 26, 0, 5, IDV_FONTSIZE_AUTO);
		prevPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Previous page.
				curBrowserFilePath = idd.prevSpineFilePath();
				
				// Grab image count list.
				ArrayList<Integer> imgCntList = idd.getImgCountList();
				
				// Count images in each page until we get to the first one 
				// on the current page/chapter.
				int curImgIdx = 0;
				for(int curP = 0; curP < idd.getCurSpineIdx(); curP++)
					curImgIdx += imgCntList.get(curP);
					
				// Set current image.
				idd.setImageGoto(curImgIdx);

				// Update current page if needed.
				setBrowser();
				
				// Scroll to first image.
				scrollBrowserToCurImg();
			}
		});
		
		// Next page/chapter/file button.
		nextPage = new Button(group, SWT.PUSH);
		nextPage.setText("Next Page >>");
		setFormData(nextPage, 26, 36, 0, 5, IDV_FONTSIZE_AUTO);
		nextPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Move to next page.
				curBrowserFilePath = idd.nextSpineFilePath();
				
				// Grab image count list.
				ArrayList<Integer> imgCntList = idd.getImgCountList();
				
				// Count images in each page until we get to the first one 
				// on the current page/chapter.
				int curImgIdx = 0;
				for(int curP = 0; curP < idd.getCurSpineIdx(); curP++)
					curImgIdx += imgCntList.get(curP);
					
				// Set current image.
				idd.setImageGoto(curImgIdx);

				// Update current page if needed.
				setBrowser();
				
				// Scroll to first image.
				scrollBrowserToCurImg();
			}
		});
		
		// Create undo button.
		undoBtn = new Button(group, SWT.PUSH);
		undoBtn.setText("Undo");
		setFormData(undoBtn, 40, 47, 0, 5, IDV_FONTSIZE_AUTO);
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
		setFormData(applyToAllBtn, 47, 54, 0, 5, IDV_FONTSIZE_AUTO);
		applyToAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Message box to let user know that things are to be changed.
				org.eclipse.swt.widgets.MessageBox msgB = new org.eclipse.swt.widgets.MessageBox(group.getShell(), SWT.OK | SWT.CANCEL);
				msgB.setMessage("Image Describer will update every image like this one with the given description. CANNOT UNDO! Continue?");
				
				// Warn user before doing this. It could take a while.
				if( msgB.open() == SWT.OK)
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
					
				} // if msgBx == OK

			} // widgetSelected()

		}); // applyToAll.addSelectionListener...

		// Clear all button. Clears the prodnote and alt attribute.
		clearAllBtn = new Button(group, SWT.PUSH);
		clearAllBtn.setText("Clear All");
		setFormData(clearAllBtn, 54, 61, 0, 5, IDV_FONTSIZE_AUTO);
		clearAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Message box to let user know that things are to be changed.
				org.eclipse.swt.widgets.MessageBox msgB = new org.eclipse.swt.widgets.MessageBox(group.getShell(), SWT.OK | SWT.CANCEL);
				msgB.setMessage("Image Describer will update every image like this one with NO DESCRIPTION. CANNOT UNDO! Continue?");
				
				// Warn user before doing this. It could take a while.
				if( msgB.open() == SWT.OK)
				{
					// Update edit boxes.
					altBox.setText("");
					imgDescTextBox.setText("");
					// Change descriptions.
					imgDesc.setDescription("", null, null, "");

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
		altLabel.setText("Alt Text: ");
		setFormData(altLabel, 0, 3, 5, 7, IDV_FONTSIZE_AUTO);
		
		// The alt box is for updating the "alt text" in an image element.
		altBox = new Text(group, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		setFormData(altBox, 0, 20, 8, 15, IDV_FONTSIZE_AUTO);
		if(imgDesc.getCurElmAttribute("alt") != null)
			altBox.setText(imgDesc.getCurElmAttribute("alt"));
		altBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) { 
					imgDesc.setCurElmImgAttributes( null, null, altBox.getText() );
					idd.setDocumentEdited(true);
			} // modifyText()

		}); // addModifyListener(new ModiftyListener() { 
		
		// Label for description box.
		descLabel = new Label(group, SWT.NONE);
		descLabel.setText("Description: ");
		setFormData(descLabel, 0, 6, 17, 19, IDV_FONTSIZE_AUTO);
		
		// Create image description text box.
		imgDescTextBox = new Text(group, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		setFormData(imgDescTextBox, 0, 20, 20, 40, IDV_FONTSIZE_AUTO);
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
		setFormData(mainImage, 0, 20, 40, 100, IDV_FONTSIZE_AUTO);
		setMainImage();

		// Setup browser window.
		browser = new Browser(group, SWT.BORDER);
		setBrowser();
		
	} // public void createUIelements()
	
	// Sets the font size for all buttons in our view.
	public void setButtonsFont(int fntSz)
	{
		setFormData(prevBtn, IDV_CTRL_NOCHANGE, 0, 0, 0, fntSz);
		setFormData(nextBtn, IDV_CTRL_NOCHANGE, 0, 0, 0, fntSz);
		setFormData(undoBtn, IDV_CTRL_NOCHANGE, 0, 0, 0, fntSz);
		setFormData(applyToAllBtn, IDV_CTRL_NOCHANGE, 0, 0, 0, fntSz);
		setFormData(clearAllBtn, IDV_CTRL_NOCHANGE, 0, 0, 0, fntSz);
		setFormData(nextPage, IDV_CTRL_NOCHANGE, 0, 0, 0, fntSz);
		setFormData(prevPage, IDV_CTRL_NOCHANGE, 0, 0, 0, fntSz);
		
	} // setButtonsFont()
	
	// Sets font size for all edit boxes in the view.
	public void setEditBoxesFont(int fntSz)
	{
		setFormData(imgDescTextBox, IDV_CTRL_NOCHANGE, 0, 0, 0, fntSz);
		setFormData(altBox, IDV_CTRL_NOCHANGE, 0, 0, 0, fntSz);
		
	} // setEditBoxesFont()
	
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
		if(imgDesc.getCurDescription() != null)
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
			
			// Release old image.
			curElmImage.dispose();
		}
		else
			mainImage.setImage( imgHelper.createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
				imageWidth, imageHeight) );
		
		idd.setImageInfo();
	}

	public void setBrowser(){
		
		// Create copy of file as html and load into browser widget.
		if(idd.getWorkingPath() != null && imgDesc.getImageList().size() > 0) {
			
				// Creates an HTML file from our xml file.
				createHTMLFile();

        		// Progress listener. Adds javascript code that will modify our img elements with 
        	    // height information.
        	    browser.addProgressListener(new ProgressListener() 
        	    {
        	        @Override
        			public void changed(ProgressEvent event) { }
        	        
        	        @Override
        			public void completed(ProgressEvent event) {
        	        	
        	        	// Refresh the view one time. This fixes 
        	        	// an issue with certain documents 
        	        	// not displaying properly in the browser 
        	        	// view.
        	        	if(refreshOnce == true) {
        	        		
        	        		// Don't do it again!
        	        		refreshOnce = false;
            	        	
            	        	// Refresh.
        	        		browser.refresh();
        	        	}
        	        	
        	        	// Resize images so they fit our screen.
        	        	// We call it here instead of before the refresh above.
        	        	// Otherwise, it doesn't take.
        	        	script_resizeImages();
        	        	
        	        	// By putting this here, we force the page to scroll after our 
        	        	// first refresh on load.
        	        	scrollBrowserToCurImg();
        	        	
        	        } // completed()
        	        
        	    }); // addProgressListener()
        	
        	// Finally, jam the file into the browser widget.
        	browser.setUrl( curBrowserFilePath );
        	
			// Set browser bounds.
        	setFormData(browser, 21, 100, 6, 100, IDV_FONTSIZE_AUTO);
		}
		else {
			// Set browser bounds.
			browser.setText("<h1>Empty Document</h1><h1>Browser View Currently Disabled</h1>");
			setFormData(browser, 21, 100, 6, 100, IDV_FONTSIZE_AUTO);
		}
	}
	
	////////////////////////////////////////////////////////////
	// Scrolls browser view to current image.
	public void scrollBrowserToCurImg()
	{
		// Index of the image.
		int imgIdx = imgDesc.getCurrentElementIndex();
		
		// If we're dealing with a multi-file document, 
		// get the index on the PAGE!!!!
		if(idd.getNumSpineElements() > 0)
			imgIdx = idd.getImageIndexInSpinePage(imgIdx);
		
		// Get the index of the current element.
		String indexStr = Integer.toString( imgIdx );

		// Create script.
		String s = "var allLinks = document.getElementsByTagName('img'); " +
                "elm = allLinks[" + indexStr + "];" +
                "var x = elm.offsetLeft;" +
                "var y = elm.offsetTop;" + 
                "while (elm = elm.offsetParent) {" +
                	"x += elm.offsetLeft;" + 
                	"y += elm.offsetTop;" + 
                "}" + 
                "window.scrollTo(x, y);";
   	 
		// Execute script.
		browser.execute(s);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Scrolls browser to a particular position.
	public void scrollBrowserToXY(int x, int y)
	{
		// Get the index of the current element.
		String strX = Integer.toString( x );
		String strY = Integer.toString( y );

		// Create script.
		String s = "window.scrollTo(" + strX + ", " + strY + ");";
   	 
		// Execute script.
		browser.execute(s);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Via Javascript, we traverse the DOM and resize images so they better fit in the window.
	public void script_resizeImages()
	{
		// Create script.
		String s = "var allLinks = document.getElementsByTagName('img');" + 
				   "for (var i = 0, max = allLinks.length; i < max; i++)" + 
				   "{" + 
				   "	allLinks[i].style.cssText = 'max-width: 100%';" + 
				   "	allLinks[i].style.cssText = 'max-height: 100%';" +
				   "}";
		
		// Execute script.
		browser.execute(s);
		
	} // script_resizeImages()
	
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
	// Sets size and font for control.
	// Defines for this method: 
	// IDV_CTRL_NOCHANGE - left|right|top|bottom - Won't change control size.
	// IDV_FONTSIZE_AUTO - fontSize - Automatically adjusted. 
	// IDV_FONTSIZE_12-4 - fontSize - Specifies a size.
	public void setFormData(Control c, int left, int right, int top, int bottom, int fontSize){
		
		if(left != IDV_CTRL_NOCHANGE && right != IDV_CTRL_NOCHANGE && top != IDV_CTRL_NOCHANGE && bottom != IDV_CTRL_NOCHANGE) {
			FormData data = new FormData();
			data.left = new FormAttachment(left);
			data.right = new FormAttachment(right);
			data.top = new FormAttachment(top);
			data.bottom = new FormAttachment(bottom);
			c.setLayoutData(data);
		}
		
		// If the font size needs to be auto-sized, use the screen resolution.
		FontData[] oldFontData = c.getFont().getFontData();
		if( fontSize == IDV_FONTSIZE_AUTO) {
			Monitor mon[] = Display.getDefault().getMonitors();
			Rectangle screenSize = mon[0].getBounds();
			if( screenSize.width >= 1920)
				oldFontData[0].setHeight(10);
			else if( screenSize.width >= 1600)
				oldFontData[0].setHeight(9);
			else if( screenSize.width >= 1280)
				oldFontData[0].setHeight(7);
			else if( screenSize.width >= 1024)
				oldFontData[0].setHeight(5);
			else if( screenSize.width >= 800)
				oldFontData[0].setHeight(4);
			c.setFont( new Font(null, oldFontData[0]) );
			
		} // if( fontSize == IDV_FONTSIZE_AUTO)
		else {
			oldFontData[0].setHeight(fontSize);
			c.setFont( new Font(null, oldFontData[0]) );
		}
			
		
	} // setFormData()
	
	// Copy's the xml file and creates an html file from it.
	public void createHTMLFile()
	{
        // If there are spine paths, that means there are multiple 
        // files to load, and we don't need to create an html file(EPUB).
    	if(idd.getCurSpineFilePath() != null) {
    		curBrowserFilePath = idd.getCurSpineFilePath();
    		return;
    	}
		
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
        
		// Get path to full file.
        curBrowserFilePath = fout.getAbsolutePath();
    	
	} //createHTMLFile()
	
	// Removes temporary HTML from our unzipped directory.
	public void disposeHTMLFile()
	{
		// If there are spine paths, then this is more than likely an EPUB document.
		// Nothing to delete.
    	if(idd.getCurSpineFilePath() != null) {
    		curBrowserFilePath = idd.getCurSpineFilePath();
    		return;
    	}
		
		// Delete the html file we created.
		if(curBrowserFilePath != null)
			new File(curBrowserFilePath).delete();
    	curBrowserFilePath = null;
		
	} // disposeHTMLFile()
	
	public void disposeUI(){
		
		// Dispose UI stuff.
		mainImage.getImage().dispose();
		group.dispose();
		
		// Get rid of temp HTML file.
		disposeHTMLFile();
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