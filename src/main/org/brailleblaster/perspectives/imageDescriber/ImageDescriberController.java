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

package org.brailleblaster.perspectives.imageDescriber;

import java.io.File;

import nu.xom.Document;

import org.brailleblaster.BBIni;
import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.ArchiverFactory;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.imageDescriber.document.ImageDescriber;
import org.brailleblaster.perspectives.imageDescriber.views.ImageDescriberView;
import org.brailleblaster.util.ImageHelper;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.YesNoChoice;
import org.brailleblaster.util.Zipper;
import org.brailleblaster.wordprocessor.BBFileDialog;
import org.brailleblaster.wordprocessor.BBStatusBar;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabItem;



///////////////////////////////////////////////////////////////////////////////////////////
// Simple dialog that displays images in a document, and allows one
// to modify the descriptions.
public class ImageDescriberController extends Controller {
	
	// Utils
	LocaleHandler lh = new LocaleHandler();
	
	// UI Elements.
	ImageDescriberView idv;
	Group group;
	// The image describer.
	ImageDescriber imgDesc;
	
	// Helps with managing and manipulating images.
	ImageHelper imgHelper;
	
	// Archiver.
	Archiver arch = null;
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Constructor.
	public ImageDescriberController(WPManager wordProcesserManager, String fileName) {
		super(wordProcesserManager, fileName);
		
		this.item = new TabItem(wp.getFolder(), 0);
		this.group = new Group(wp.getFolder(), SWT.NONE);
		this.group.setLayout(new FormLayout());
		
		this.imgDesc = new ImageDescriber(this);
		
		// Start the image describer and build the DOM	
		if(fileName != null){
			if(openDocument(fileName)) 
				item.setText(fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1));
		}
		else {
			if(openDocument(null)){
				docCount++;
				if(docCount > 1)
					item.setText("Untitled #" + docCount);
				else
					item.setText("Untitled");
			}
		}
		
		//Set the views in the tab area
		idv = new ImageDescriberView(this.group, imgDesc, this);
		this.item.setControl(this.group);
		
		// Image helper class. Image helper functions, and such.
		imgHelper = new ImageHelper();
	}
	
	public ImageDescriberController(WPManager wp, String docName, Document doc, TabItem tabItem) {
		super(wp, docName);
		if(docName != null){
			workingFilePath = docName;
		}
		imgDesc = new ImageDescriber(this, docName, doc);
		
		this.item = tabItem;
		this.group = new Group(wp.getFolder(), SWT.NONE);
		this.group.setLayout(new FormLayout());
		idv = new ImageDescriberView(this.group, imgDesc, this);
		this.item.setControl(this.group);
		// Image helper class. Image helper functions, and such.
		imgHelper = new ImageHelper();
		idv.setTextBox(imgDesc.getCurDescription());
	}
	
	public boolean openDocument(String fileName){
		
		// Create archiver and massage document if necessary.
		String archFileName = null;
		if(fileName != null) {
			arch = ArchiverFactory.getArchive(fileName);
			if(arch != null)
				archFileName = arch.open();
			
			// Potentially massaged archiver file, or just pass to BB.
			if(archFileName != null) {
				workingFilePath = archFileName;
				zippedPath = "";
			}
			else
				workingFilePath = fileName;
		}
		
		// Change current config based on file type.
		if(arch != null)
		{
			// Is this an epub document?
			if( arch.getOrigDocPath().endsWith(".epub") == true )
				currentConfig = getAutoCfg("epub");
		}
		else if( workingFilePath.endsWith(".xml") )
			currentConfig = getAutoCfg("nimas"); // Nimas document.
		else if( workingFilePath.endsWith(".xhtml") )
			currentConfig = getAutoCfg("epub");
		
		////////////////
		// Recent Files.
		addRecentFileEntry(fileName);
		
		if(fileName == null){
			return imgDesc.startDocument(BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "dtbook.xml", currentConfig, null);
		}
		else if(fileName.endsWith(".zip")){
			openZipFile(fileName);
			return imgDesc.startDocument(workingFilePath, currentConfig, null);
		}
		else {
//			workingFilePath = fileName;
			return imgDesc.startDocument(workingFilePath, currentConfig, null);
		}
	}
	
	private void openZipFile(String file){
		Zipper unzipr = new Zipper();
		// Unzip and update "opened" file.
//		workingFilePath = unzipr.Unzip(fileName, fileName.substring(0, fileName.lastIndexOf(".")) + BBIni.getFileSep());
		String sp = BBIni.getFileSep();
		String tempOutPath = BBIni.getTempFilesPath() + file.substring(file.lastIndexOf(sp), file.lastIndexOf(".")) + sp;
		workingFilePath = unzipr.Unzip(file, tempOutPath);
		// Store paths.
		zippedPath = file;
	}

	public void fileOpenDialog(){
		String tempName;

//		
		String[] filterNames = new String[] { "XML", "XML ZIP", "EPUB", "XHTML", "HTML","HTM","UTDML working document"};
		String[] filterExtensions = new String[] { "*.xml", "*.zip", "*.epub", "*.xhtml","*.html", "*.htm", "*.utd"};
		BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.OPEN, filterNames, filterExtensions);
		
		tempName = dialog.open();
		
		// Don't do any of this if the user failed to choose a file.
		if(tempName != null) {
			// Open it.
			if(!canReuseTab())
				wp.addDocumentManager(tempName);
			else 
				reuseTab(tempName);
			
			addRecentFileEntry(tempName);
		} // if(tempName != null)
		
		// Zip and Recent Files.
		////////////////////////
	}
	
	public void save(){
		if(workingFilePath == null)
			saveAs();
		else {
			if(arch != null) { // Save archiver supported file.
				if(arch.getOrigDocPath().endsWith("epub"))
					//imgDesc.getDOM().toXML().
					arch.save(imgDesc, null);
				else if(arch.getOrigDocPath().endsWith("xml"))
				{
					if(fu.createXMLFile(imgDesc.getNewXML(), workingFilePath)){
						String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem"; 
						copySemanticsFile(tempSemFile, fu.getPath(workingFilePath) + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem");
					}
					else {
						new Notify("An error occured while saving your document.  Please check your original document.");
					}
				}
			}
			else if(workingFilePath.endsWith("xml")){
				if(fu.createXMLFile(imgDesc.getNewXML(), workingFilePath)){
					String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem"; 
					copySemanticsFile(tempSemFile, fu.getPath(workingFilePath) + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem");
				}
				else {
					new Notify("An error occured while saving your document.  Please check your original document.");
				}
			}
			else if(workingFilePath.endsWith("utd")) {		
				imgDesc.setOriginalDocType(imgDesc.getDOM());
				if(fu.createXMLFile(imgDesc.getDOM(), workingFilePath)){
					String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem"; 
					copySemanticsFile(tempSemFile, fu.getPath(workingFilePath) + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem");
				}
				else {
					new Notify("An error occured while saving your document.  Please check your original document.");
				}
			}
			else if(workingFilePath.endsWith("brf")){
				if(!imgDesc.createBrlFile(this, workingFilePath)){
					new Notify("An error has occurred.  Please check your original document");
				}
			}
			
			// If the document came from a zip file, then rezip it.
			if(zippedPath.length() > 0)
				zipDocument();
		
			documentEdited = false;
		}
	}
	
	public void saveAs(){
		String[] filterNames = new String[] {"XML", "BRF", "UTDML"};
		String[] filterExtensions = new String[] {".xml","*.brf", "*.utd"};
		BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.SAVE, filterNames, filterExtensions);
		String filePath = dialog.open();
		
		if(filePath != null){
//			String ext = fu.getFileExt(filePath);
			
			if(workingFilePath.endsWith("epub")) { // Save archiver supported file.
				if(filePath.endsWith("epub"))
					arch.save(imgDesc, filePath);
				else
					System.out.println("Can only save epub files as epub files... for now.");
			}
			else if(filePath.endsWith("brf")){
				if(!imgDesc.createBrlFile(this, filePath)){
					new Notify("An error has occurred.  Please check your original document");
				}
			}
			else if(filePath.endsWith("xml")){
			    if(fu.createXMLFile(imgDesc.getNewXML(), filePath)) {
			    	setTabTitle(filePath);
					//documentName = filePath;
			    
			    	String tempSemFile; 			    
				    if(workingFilePath == null)
				    	tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName("outFile.utd") + ".sem";
				    else
				    	tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem";
				    
				    //String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem"; 
			    	String savedSemFile = fu.getPath(filePath) + BBIni.getFileSep() + fu.getFileName(filePath) + ".sem";   
			    
			    	//Save new semantic file to correct location and temp folder for further editing
			    	copySemanticsFile(tempSemFile, savedSemFile);
			    	copySemanticsFile(tempSemFile, BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(filePath) + ".sem");
			    	
					//update working file path to newly saved file
			    	workingFilePath = filePath;				    
			    }
			    else {
			    	new Notify("An error occured while saving your document.  Please check your original document.");
			    }
			}
			else if(filePath.endsWith("utd")) {				
				imgDesc.setOriginalDocType(imgDesc.getDOM());
				if(fu.createXMLFile(imgDesc.getDOM(), filePath)){
					setTabTitle(filePath);
			    	//documentName = filePath;
				    
				    String fileName;
			    	if(workingFilePath == null)
				    	fileName = "outFile";
				    else
				    	fileName = fu.getFileName(workingFilePath);
				    
				    String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem"; 
			    	String savedTempFile = fu.getPath(filePath) + BBIni.getFileSep() + fu.getFileName(filePath) + ".sem";
				    
			    	copySemanticsFile(tempSemFile, savedTempFile);
			    	copySemanticsFile(tempSemFile, BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(filePath) + ".sem");

			    	workingFilePath = filePath;
				}
				else {
			    	new Notify("An error occured while saving your document.  Please check your original document.");
			    }
			}
		   
			documentEdited = false;
		}
	}
	
	@Override
	public void restore(WPManager wp) {
		
	}

	@Override
	public void dispose() {
		imgDesc.disposeImages();
		idv.disposeUI();
	}

	@Override
	public void close() {
		if(documentEdited){
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"));
			if (ync.result == SWT.YES) {
				save();
			}
		}
		dispose();
		item.dispose();
	}
	
	public void setImageToPrevious(){
		// Change main image to previous element image.
		imgDesc.prevImageElement();
		idv.setMainImage();
		
		// Get prodnote text/image description.
		idv.setTextBox();
		//imgDescTextBox.setText( imgDesc.getCurProdText() );

		// Show current image index and name.
		setImageInfo();
		//imgDescShell.setText("Image Describer - " + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );
		
		idv.setAltBox(imgDesc.getCurElmAttribute("alt"));
	}
	
	public void setImageToNext(){
		// Change main image to previous element image.
		imgDesc.nextImageElement();
		
		//Change current image in dialog.
		idv.setMainImage();
		/*
		Image curElmImage = imgDesc.getCurElementImage();
		if(curElmImage != null)
			mainImage.setImage( imgHelper.createScaledImage(curElmImage, imageWidth, imageHeight) );
		else
			mainImage.setImage( imgHelper.createScaledImage(new Image(null, BBIni.getProgramDataPath() + BBIni.getFileSep() + "images" + BBIni.getFileSep() + "imageMissing.png"), 
				imageWidth, imageHeight) );
	*/
			
		// Get prodnote text/image description.
		idv.setTextBox();
		
		//imgDescTextBox.setText( imgDesc.getCurProdText() );

		// Show current image index and name.
		setImageInfo();
		//imgDescShell.setText("Image Describer - " + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );
		
		idv.setAltBox(imgDesc.getCurElmAttribute("alt"));
	}
	
	public void apply(){
		imgDesc.setDescription(idv.getTextBoxValue(), null, null, null);
		setDocumentEdited(true);
	}
	
	public void cancel(){
		// Warn user that all changes will be discarded.
		if(idv.msgBx("Warning", "This will discard all changes, even the ones you Apply'd. Continue?") == true)
		{
			// Copy original elements back into main list. "Undo!"
			imgDesc.copyUndo2MainList();

			// Close the dialog without committing changes.
			//imgDescShell.close();
		}
	}
	
	public void applyToAll(){
		// Warn user before doing this. It could take a while.
		if( idv.msgBx("Warning", "Image Describer will update every image like this one with the given description. This could take a while. Continue?") == true)
		{
			// Apply what is in the edit box first.
			imgDesc.setDescription(idv.getTextBoxValue(), null, null, null);

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
					imgDesc.setDescAtIndex(curImg, idv.getTextBoxValue(), null, null, null);

				} // if( imgDesc.getElementAtIndex...

			} // for(int curImg...
			setDocumentEdited(true);
		} // if msgBx == true
	}
	
	public void clearAll(){
		// Clear every description for this image, and clear alt text.
		if( idv.msgBx("Warning", "All images like this one will have their description cleared, and alt text removed. This could take a while. Continue?") == true)
		{
			// Apply what is in the edit box first.
			imgDesc.setDescription("", null, null, null);
			idv.setTextBox("");

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
	}

	//////////////////////////////////////////////////////////////////////
	// Returns Archiver. Could be null if we didn't need it 
	// to open a file.
	public Archiver getArchiver() {
		return arch;
	}
	
	/////////////////////////////////////////////////////////////////
	// Returns the image desciber "document"
	public ImageDescriber getDocument() {
		return imgDesc;
	}
	
	@Override
	public Document getDoc() {
		return imgDesc.getDOM();
	}

	public void setImageInfo(){
		setStatusBarText(wp.getStatusBar());
	}
	
	@Override
	public void setStatusBarText(BBStatusBar statusBar) {
		statusBar.setText("Image #" + imgDesc.getCurrentElementIndex() + " - " + imgDesc.currentImageElement().getAttributeValue("src") );	
	}

	@Override
	public boolean canReuseTab() {
		if((workingFilePath != null && imgDesc.getImageList().size() > 0) || documentEdited)
			return false;
		else
			return true;
	}

	public ImageDescriber getImageDescriber(){
		return imgDesc;
	}
	
	@Override
	public void reuseTab(String file) {
		closeUntitledDocument();
		String currentPath = workingFilePath;
		openDocument(file);
		idv.setMainImage();
		idv.setBrowser();
		idv.setTextBox(imgDesc.getCurDescription());
		idv.setAltBox(imgDesc.getCurElmAttribute("alt"));
//		if(file.endsWith(".zip")){
//			openZipFile(file);
//		}
//		else {
//			workingFilePath = file;
//		}
//		
//		if(imgDesc.startDocument(workingFilePath, BBIni.getDefaultConfigFile(), null)){
//			item.setText(file.substring(file.lastIndexOf(File.separatorChar) + 1));
//			currentConfig = BBIni.getDefaultConfigFile();
//			idv.resetViews(imgDesc);
//		}
//		else {
//			workingFilePath = currentPath;
//			currentConfig = "nimas.cfg";
//			if(currentPath != null)
//				imgDesc.startDocument(currentPath, currentConfig, null);
//			else
//				imgDesc.startDocument(BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "dtbook.xml", currentConfig, null);
//		}
	}
	
	private void closeUntitledDocument(){
		imgDesc.deleteDOM();
		imgDesc.resetCurrentIndex();
	}
	
} // public class ImageDescriberDialog extends Dialog
