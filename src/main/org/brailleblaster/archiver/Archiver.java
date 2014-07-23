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
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
 */

package org.brailleblaster.archiver;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.converters.DOMConverter;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.Zipper;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

//////////////////////////////////////////////////////////////////////////////////
// Archiver gives methods for opening/handling particular document types. 
// Some of these types have special needs, and therefore, specific 
// implementations. This class is ABSTRACT, and is to be used as a 
// base for other archivers.
abstract public class Archiver {
	protected String[] filterNames;
	protected String[] filterExtensions;
	
	protected String originalDocPath;
	protected String zippedPath;
	protected String workingDocPath; 
	protected String currentConfig;
	
	protected boolean documentEdited;
	
	protected String opfFilePath = null;
	protected Document opfDoc = null;
	protected ArrayList<String> spineList = null;
	NodeList manifestElements;
	NodeList spineElements;

	// Every file that makes our epub doc.
	ArrayList<String> epubFileList = null;
	
	// Number of images in each file that makes up our document.
	// For every spine element we have, we're going to count the number of images 
	// in that file. This helps with image traversal.
	ArrayList<Integer> numImages = null;
	
	// Index of current file we're looking at in the browser.
	// Current file we're to load using the spine as a reference.
	// Spine is in .opf file in epub.
	int curSpineFileIdx = 0;
	
	//////////////////////////////////////////////////////////////////////////////////
	// Constructor. Stores path to document to prepare.
	Archiver(String docToPrepare)
	{
		// Store paths.
		originalDocPath = docToPrepare;
		workingDocPath = originalDocPath;
		zippedPath = "";
		documentEdited = false;
		opfFilePath = null;
		epubFileList = new ArrayList<String>();
		numImages = new ArrayList<Integer>();
	}
	
	// Get-er for original document path.
	public String getOrigDocPath() { return originalDocPath; }
	
	public String getWorkingFilePath(){
		return workingDocPath;
	}
	
	public String getCurrentConfig(){
		return currentConfig;
	}
	
	public String[]  getFileTypes(){
		return filterNames;
	}
	
	public String[] getFileExtensions(){
		return filterExtensions;
	}
	
	public void setCurrentConfig(String config){
		currentConfig = config;
	}
	
	////////////////////////////////////////////////////////////////
	// Opens our auto config settings file and determines 
	// what file is associated with the given file type.
	// 
	// Appropriate strings to pass so far are: epub, nimas, 
	public String getAutoCfg(String settingStr) {
		
		// Init and load properties.
		Properties props = new Properties();
		try {
			// Load it!
			props.load( new FileInputStream(BBIni.getAutoConfigSettings()) );
		}
		catch (IOException e) { e.printStackTrace(); }

		// Loop through the properties, and find the setting.
		for(String key : props.stringPropertyNames()){
			// Is this the string/setting we're looking for?
			if( key.compareTo(settingStr) == 0 )
				return props.getProperty(key);
		}

		// If we made it here, there was no setting by that name.
		return null;
		
	} // getAutoCfg()
	
	// lic void copySemanticsFile(String
	public void copySemanticsFile(String tempSemFile, String savedFilePath) {
		FileUtils fu = new FileUtils();
		
		if(fu.exists(tempSemFile)){
    		fu.copyFile(tempSemFile, savedFilePath);
    	}
	}
	
	protected void saveBrf(BBDocument doc, String path){
		if(!doc.createBrlFile(path)){
			new Notify("An error has occurred.  Please check your original document");
		}
	}

	public void setDocumentEdited(boolean documentEdited){
		this.documentEdited = documentEdited;
	}
	
	public boolean getDocumentEdited(){
		return documentEdited;
	}
	//////////////////////////////////////////////////////////////////////////////////
	// 
	public abstract void save(BBDocument doc, String path);
	
	public abstract Archiver saveAs(BBDocument doc, String path, String ext);
	
	//////////////////////////////////////////////////////////////////////////////////
	// Returns path to opf file if we found one with a prior call to findOPF().
	public String getOPFPath() {
		return opfFilePath;
	} // getOPF()
	
	//////////////////////////////////////////////////////////////////////////////////
	// Traverses the list of upzipped files and attempts to find an opf 
	// file.
	// 
	// Notes: zipper must have already been used to unzip an archive.
	// String is the path to the opf file. We also store it. 
	// Get it with getOPF().
	public String findOPF(Zipper zipper)
	{
		// Get paths to all unzipped files.
		ArrayList<String> unzippedPaths = zipper.getUnzippedFilePaths();
		
		// Find the .opf file.
		for(int curFile = 0; curFile < unzippedPaths.size(); curFile++)
		{
			// Does this file have an .opf extension?
			if(unzippedPaths.get(curFile).toLowerCase().endsWith(".opf") == true)
			{
				// Found it!
				return unzippedPaths.get(curFile).toLowerCase();
				
			} // If ends with opf
			
		} // for(int curFile...
		
		// Couldn't find it.
		return null;
		
	} // findOPF()
	
	//////////////////////////////////////////////////////////////////////////////////
	// Uses opf file to build a list of files used by the book/document.
	public ArrayList<String> parseOPFFile(String _opfPath)
	{
		// Build factory, and parse the opf.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	        factory.setNamespaceAware(true); // Needed, just in case manifest/spine are in a namespace.
			builder = factory.newDocumentBuilder();
			opfDoc = builder.parse(opfFilePath);
        } catch (Exception e) { e.printStackTrace(); }
        
		// Grab the spine elements and manifest elements.
        manifestElements = opfDoc.getElementsByTagNameNS("*", "item");
		spineElements = opfDoc.getElementsByTagNameNS("*", "itemref");
		
		// Filepath to current document.
		String curDocFilePath = null;
		
		// Loop through the spine and find the items in the manifest that we need. 
		for(int curSP = 0; curSP < spineElements.getLength(); curSP++)
		{
			// Get the attributes for this spine element.
			NamedNodeMap spineAtts = spineElements.item(curSP).getAttributes();
			
			// Get the ID of the item we need from the manifest.
			String fileID = spineAtts.getNamedItem("idref").getNodeValue();
			
			// Get the file path from the manifest.
			curDocFilePath = opfFilePath.substring( 0, opfFilePath.lastIndexOf(BBIni.getFileSep()) ) + BBIni.getFileSep();
			curDocFilePath += findHrefById(fileID).replace("/", BBIni.getFileSep());
			
			// Add this path to the list of document paths.
			epubFileList.add(curDocFilePath);
		}
		
		// Return list.
		return epubFileList;
		
	} // buildOPF()
	
	/////////////////////////////////////////////////////////////////////////
	// Finds the manifest element using the given id, 
	// and returns the href attribute value.
	String findHrefById(String id)
	{
		// Loop through the manifest items and find the file with this ID.
		for(int curMan = 0; curMan < manifestElements.getLength(); curMan++)
		{
			// Get the attributes for this manifest item.
			NamedNodeMap manAtts = manifestElements.item(curMan).getAttributes();
			
			// If this manifest item has the id we're looking for, time to open a file.
			if( manAtts.getNamedItem("id").getNodeValue().compareTo(id) == 0 )
			{
				// Get value of href; this is our local file path to the file. Return it.
				return manAtts.getNamedItem("href").getNodeValue();
			
			} // if manifestItem ID == fileID...
		
		} // for(int curMan...
		
		// Couldn't find it.
		return null;
	
	} // findHrefById()
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Takes in a document(W3C) and adds its image count to the list.
	public void addToNumImgsList(Document addMe)
	{
		// Create space big enough to hold our image integers if we haven't done so already.
		if(numImages == null)
			numImages = new ArrayList<Integer>();
		
		// Grab all <img> elements.
		NodeList imgElements = addMe.getElementsByTagName("img");
		
		// Add this value to the list.
		numImages.add(imgElements.getLength());
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Takes in a document(XOM) and adds its image count to the list.
	public void addToNumImgsList(nu.xom.Document addMe)
	{
		// Convert to DOM.
		Document w3cDoc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();
			w3cDoc = DOMConverter.convert(addMe, impl);
		}
		catch(Exception e) { e.printStackTrace(); }
		
		// Create space big enough to hold our image integers if we haven't done so already.
		if(numImages == null)
			numImages = new ArrayList<Integer>();
		
		// Grab all <img> elements.
		NodeList imgElements = w3cDoc.getElementsByTagName("img");
		
		// Add this value to the list.
		numImages.add(imgElements.getLength());
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////	
	// Returns the list of documents that make up this book.
	public ArrayList<String> getSpine() {
		return epubFileList;
	}
	///////////////////////////////////////////////////////////////////////////////////////////	
	// Returns a path from a particular spine element.
	public String getSpineFilePath(int idx) {
		if(epubFileList != null)
			if(epubFileList.size() > 0)
				return epubFileList.get(idx);
		return null;
	}
	
	//////////////////////////////////////////////////////////////////////
	// Returns list of image counts for files in spine.
	public ArrayList<Integer> getImgCountList() {
		return numImages;
	}
	
	//////////////////////////////////////////////////////////////////////
	// Return number of spine elements.
	public int getNumSpineElements()
	{
		// Returns size of spine list.
		return getSpine().size();
	
	} // getNumSpineElements()
	
	//////////////////////////////////////////////////////////////////////
	// If working with an epub document, returns current spine file.
	public String getCurSpineFilePath()
	{
		// Get current spine file path.
		return getSpineFilePath(curSpineFileIdx);
	
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
		// Get image counts for spine.
		ArrayList<Integer> imgCntList = getImgCountList();
		
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
		
		// Get image counts for spine.
		ArrayList<Integer> imgCntList = getImgCountList();
		
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
		// Go to next file path.
		curSpineFileIdx = idx;
		// If we've gone too far, wrap around.
		if(curSpineFileIdx >= getSpine().size())
			curSpineFileIdx = 0;
		if(curSpineFileIdx < 0 )
			curSpineFileIdx = getSpine().size() - 1;
		
		// Return the current spine file path.
		return getSpineFilePath(curSpineFileIdx);
	
	} // gotoSpineFilePath()
	
	//////////////////////////////////////////////////////////////////////
	// Moves index to current file to the next one we see in the spine.
	public String nextSpineFilePath()
	{
		// Go to next file path.
		curSpineFileIdx++;
		// If we've gone too far, wrap around.
		if(curSpineFileIdx >= getSpine().size())
			curSpineFileIdx = 0;
		
		// Return the current spine file path.
		return getSpineFilePath(curSpineFileIdx);
	
	} // nextSpineFile()
	
	//////////////////////////////////////////////////////////////////////
	// Moves index to current file to the previous one we see in the spine.
	public String prevSpineFilePath()
	{
		// Go to previous file path.
		curSpineFileIdx--;
		// If we've gone too far, wrap around.
		if(curSpineFileIdx < 0 )
			curSpineFileIdx = getSpine().size() - 1;
		
		// Return the current spine file path.
		return getSpineFilePath(curSpineFileIdx);
		
	} // prevSpineFile()
	
} // class Archiver