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
  * this program; see the file LICENSE.txt
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.perspectives.imageDescriber;

import java.io.File;
import java.util.ArrayList;

import nu.xom.Document;

import org.brailleblaster.BBIni;
import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.ArchiverFactory;
import org.brailleblaster.archiver.EPub3Archiver;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.imageDescriber.document.ImageDescriber;
import org.brailleblaster.perspectives.imageDescriber.views.ImageDescriberView;
import org.brailleblaster.util.ImageHelper;
import org.brailleblaster.util.YesNoChoice;
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
	
	// Index of current file we're looking at in the browser.
	// Current file we're to load using the spine as a reference.
	// Spine is in .opf file in epub.
	int curSpineFileIdx = 0;
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Constructor.
	public ImageDescriberController(WPManager wordProcesserManager, String fileName) {
		super(wordProcesserManager);
		
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
	
	public ImageDescriberController(WPManager wp, Document doc, TabItem tabItem, Archiver arch) {
		super(wp);
		this.arch = arch;
		
		imgDesc = new ImageDescriber(this, doc);
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
		if(fileName != null) 
			arch = ArchiverFactory.getArchive(fileName);
		else
			arch = ArchiverFactory.getArchive(templateFile);
			
		////////////////
		// Recent Files.
		addRecentFileEntry(fileName);		
		
		return imgDesc.startDocument(arch.getWorkingFilePath(), arch.getCurrentConfig(), null);
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
		if(arch.getOrigDocPath() == null)
			saveAs();
		else {
			if(arch != null) { // Save archiver supported file.
				if(arch.getOrigDocPath().endsWith("epub"))
					//imgDesc.getDOM().toXML().
					arch.save(imgDesc, null);
				else if(arch.getOrigDocPath().endsWith("xml") || arch.getOrigDocPath().endsWith(".zip"))
					arch.save(imgDesc, null);
				else if(arch.getOrigDocPath().endsWith("utd"))		
					arch.save(imgDesc, null);
				else if(arch.getOrigDocPath().endsWith(".brf"))
					arch.save(imgDesc, null);
			}
		
			arch.setDocumentEdited(false);
		}
	}
	
	public void saveAs(){
		BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.SAVE, arch.getFileTypes(), arch.getFileExtensions());
		String filePath = dialog.open();
		
		if(filePath != null){
			String ext = getFileExt(filePath);
			arch.saveAs(imgDesc, filePath, ext);
		   
			arch.setDocumentEdited(false);
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
		if(arch.getDocumentEdited()){
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"));
			if (ync.result == SWT.YES) {
				save();
			}
		}
		dispose();
		item.dispose();
	}
	
	// 
	public void setImageGoto(int index)
	{
		// Go to a particular image in the image describer.
		imgDesc.gotoImageElement(index);
		
		// Set image preview.
		idv.setMainImage();
		
		// Get prodnote text/image description.
		idv.setTextBox();

		// Show current image index and name.
		setImageInfo();
		
		// Set alt text.
		idv.setAltBox(imgDesc.getCurElmAttribute("alt"));
		
		// Scroll browser to current image.
//		idv.scrollBrowserToCurImg();
	}
	
	public void setImageToPrevious(){
		// Change main image to previous element image.
		imgDesc.prevImageElement();

		// Set image preview.
		idv.setMainImage();
		
		// Get prodnote text/image description.
		idv.setTextBox();

		// Show current image index and name.
		setImageInfo();
		
		// Set alt text.
		idv.setAltBox(imgDesc.getCurElmAttribute("alt"));
		
		// Scroll browser to current image.
//		idv.scrollBrowserToCurImg();
	}
	
	public void setImageToNext(){
		// Change main image to previous element image.
		imgDesc.nextImageElement();
		
		//Change current image in dialog.
		idv.setMainImage();
			
		// Get prodnote text/image description.
		idv.setTextBox();

		// Show current image index and name.
		setImageInfo();
		
		// Set alt text.
		idv.setAltBox(imgDesc.getCurElmAttribute("alt"));
		
		// Scroll browser to current image.
//		idv.scrollBrowserToCurImg();
	}
	
	// Undo-es current element/image changes.
	public void undo()
	{
		// Undo the changes.
		imgDesc.undoCurDescAndAlt();
		// Get prodnote text/image description.
		idv.setTextBox();
		// Set alt text.
		idv.setAltBox(imgDesc.getCurElmAttribute("alt"));
		
	}
	
	public void apply(){
		imgDesc.setDescription(idv.getTextBoxValue(), null, null, null);
		arch.setDocumentEdited(true);
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
			arch.setDocumentEdited(true);
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
	
	//////////////////////////////////////////////////////////////////////
	// Returns list of image counts for files in spine.
	// Returns null if there is no list. Don't call 
	// unless working with EPUB3 for now.
	public ArrayList<Integer> getImgCountList() {
		if(arch != null)
			if(arch instanceof EPub3Archiver)
				return ((EPub3Archiver)arch).getImgCounts();
		
		return null;
	}
	
	//////////////////////////////////////////////////////////////////////
	// Return number of spine elements.
	public int getNumSpineElements()
	{
		// If there is an archiver, and this archiver 
		// represents a multi-file document, return number of spine elements.
		if(arch != null)
			if(arch instanceof EPub3Archiver)
				return ((EPub3Archiver)arch).getSpine().size();
				
		// This isn't a multi-file document. Return 0.
		return 0;
		
	} // getNumSpineElements()
	
	//////////////////////////////////////////////////////////////////////
	// If working with an epub document, returns current spine file.
	public String getCurSpineFilePath()
	{
		// Get current spine file path.
		if(arch != null)
			if(arch instanceof EPub3Archiver)
				return ((EPub3Archiver)arch).getSpineFilePath(curSpineFileIdx);
		
		// If we get here, there is no spine path to return.
		return null;
		
	} // getCurSpineFile()
	
	//////////////////////////////////////////////////////////////////////
	// Returns the current spine index.
	public int getCurSpineIdx() {
		return curSpineFileIdx;
	} // getCurSpineIdx()
	
	//////////////////////////////////////////////////////////////////////
	// Converts image index to a local index, in reference to 
	// a page.
	// 
	// For example: We could be on the 56th image, but it could be the 
	// second image in this particular spine element/page.
	// 
	// Returns -1 if we can't find it, or if there isn't a supported 
	// archiver.
	public int getImageIndexInSpinePage(int imageIndex)
	{
		// Make sure there is an archiver.
		if(arch == null)
			return -1;
		if(arch instanceof EPub3Archiver == false)
			return -1;
		
		// Save some typing.
		EPub3Archiver ep3Arch = ((EPub3Archiver)arch);
		
		// Get image counts for spine.
		ArrayList<Integer> imgCntList = ep3Arch.getImgCounts();
		
		// Current image index in the spine we're testing against.
		int curImgIdx = 0;
		
		// Add up the spine/image counts
		for(int curS = 0; curS < curSpineFileIdx; curS++)
			curImgIdx += imgCntList.get(curS);
		
		// Is this image index within range of this particular spine element?
		if( imageIndex >= curImgIdx && imageIndex < curImgIdx + imgCntList.get(curSpineFileIdx) )
		{
			// Move to the spine element that we've found.
			return imageIndex - curImgIdx;
			
		} // if( within range )
		
		// If we're here, we couldn't find the spine or image. Return failure.
		return -1;
	}
	
	//////////////////////////////////////////////////////////////////////
	// Takes an image index, and finds the spine file 
	// that contains this image.
	public String setSpineFileWithImgIndex(int imgIndex)
	{
		// Make sure there is an archiver.
		if(arch == null)
			return null;
		if(arch instanceof EPub3Archiver == false)
			return null;
		
		// Save some typing.
		EPub3Archiver ep3Arch = ((EPub3Archiver)arch);
		
		// Get image counts for spine.
		ArrayList<Integer> imgCntList = ep3Arch.getImgCounts();
		
		// Current image index in the spine we're testing against.
		int curImgIdx = 0;
		
		// Loop through the spine/image counts, until we find one that contains this image.
		for(int curS = 0; curS < imgCntList.size(); curS++)
		{
			// Is this image index within range of this particular spine element?
			if( imgIndex >= curImgIdx && imgIndex < curImgIdx + imgCntList.get(curS) )
			{
				// Move to the spine element that we've found.
				return gotoSpineFilePath(curS);
				
			} // if( within range )
			
			// Move forward with the index.
			curImgIdx += imgCntList.get(curS);
				
		} // for(curS)
		
		// If we make it here, we couldn't find that particular spine file/element.
		return null;
		
	} // setSpineFileWithImgIndex()
	
	//////////////////////////////////////////////////////////////////////
	// Moves to a specific spine file using an index into the list.
	public String gotoSpineFilePath(int idx)
	{
		// Make sure there is an archiver.
		if(arch == null)
			return null;
		if(arch instanceof EPub3Archiver == false)
			return null;
		
		// Go to next file path.
		curSpineFileIdx = idx;
		// If we've gone too far, wrap around.
		if(curSpineFileIdx >= ((EPub3Archiver)arch).getSpine().size())
			curSpineFileIdx = 0;
		if(curSpineFileIdx < 0 )
			curSpineFileIdx = ((EPub3Archiver)arch).getSpine().size() - 1;
		
		// Return the current spine file path.
		return ((EPub3Archiver)arch).getSpineFilePath(curSpineFileIdx);
		
	} // gotoSpineFilePath()
	
	//////////////////////////////////////////////////////////////////////
	// Moves index to current file to the next one we see in the spine.
	public String nextSpineFilePath()
	{
		// Make sure there is an archiver.
		if(arch == null)
			return null;
		if(arch instanceof EPub3Archiver == false)
			return null;
		
		// Go to next file path.
		curSpineFileIdx++;
		// If we've gone too far, wrap around.
		if(curSpineFileIdx >= ((EPub3Archiver)arch).getSpine().size())
			curSpineFileIdx = 0;
		
		// Return the current spine file path.
		return ((EPub3Archiver)arch).getSpineFilePath(curSpineFileIdx);
		
	} // nextSpineFile()
	
	//////////////////////////////////////////////////////////////////////
	// Moves index to current file to the previous one we see in the spine.
	public String prevSpineFilePath()
	{
		// Make sure there is an archiver.
		if(arch == null)
			return null;
		if(arch instanceof EPub3Archiver == false)
			return null;
		
		// Go to previous file path.
		curSpineFileIdx--;
		// If we've gone too far, wrap around.
		if(curSpineFileIdx < 0 )
			curSpineFileIdx = ((EPub3Archiver)arch).getSpine().size() - 1;
		
		// Return the current spine file path.
		return ((EPub3Archiver)arch).getSpineFilePath(curSpineFileIdx);
		
	} // prevSpineFile()
	
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
		if((arch.getOrigDocPath() != null && imgDesc.getImageList().size() > 0) || arch.getDocumentEdited())
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
		openDocument(file);
		item.setText(file.substring(file.lastIndexOf(File.separatorChar) + 1));
		idv.setMainImage();
		idv.setBrowser();
		idv.setTextBox(imgDesc.getCurDescription());
		idv.setAltBox(imgDesc.getCurElmAttribute("alt"));

		if(arch != null){
			if( arch instanceof EPub3Archiver )
				idv.enableBrowserNavButtons();
			else
				idv.disableBrowserNavButtons();
		}
		else
			idv.disableBrowserNavButtons();
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
