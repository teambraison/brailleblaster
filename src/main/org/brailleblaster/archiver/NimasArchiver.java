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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.document.Resolver;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.Zipper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

//////////////////////////////////////////////////////////////////////////////////
// Prepares Nimas Archive for opening.
public class NimasArchiver extends Archiver {


	Set <String> allPaths;
	// The number of documents we COULD have if wrote them all to disk.
	int numPotentialFiles = 0;
	
	NimasArchiver(String docToPrepare) {
		
		super(docToPrepare);
		
		currentConfig = getAutoCfg("nimas"); // Nimas document.
		filterNames = new String[] {"XML", "XML Zip", "BRF", "UTDML"};
		filterExtensions = new String[] {"*.xml", "*.zip", "*.brf", "*.utd"};
		allPaths = new HashSet<String>();
		
		// Unzip file if needed.
		if(docToPrepare.endsWith(".zip")) {
			if( resumingPrevSession == false )
				unzip(docToPrepare);
			zippedPath = docToPrepare;
			zip = true;
		}
		else { 
			if( resumingPrevSession == false )
				copyFileToTemp(docToPrepare);
			zip = false;
		}
		
		// Segment the single NIMAS file. This will make rendering 
		// faster in certain perspectives.

		// Write the first file to disk.
		wrtieToDisk(0);
	}
	
	@Override
	public void save(BBDocument doc, String path) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
				
		
		FileUtils fu = new FileUtils();
		if(path == null)
			path = workingDocPath;
		
		//save working copy to temp folder and semantic files if part of a zip file
		if(fu.createXMLFile(doc.getNewXML(), path)){
			if(zip){
				String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(path) + ".sem";
				copySemanticsFile(tempSemFile, fu.getPath(path) + BBIni.getFileSep() + fu.getFileName(path) + ".sem");
			}
		}
		else {
			new Notify("An error occured while saving your document.  Please check your original document.");
		}
				
		// Zip the document back up or save to original document with semantics included?
		if(zip == true)
			zipDocument();
		else {
			fu.copyFile(workingDocPath, originalDocPath);
			String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(path) + ".sem";
			String newSem = fu.getPath(originalDocPath) + BBIni.getFileSep() + fu.getFileName(path) + ".sem";
			copySemanticsFile(tempSemFile, newSem);
		}					
				
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
	} // save()
	
	//////////////////////////////////////////////////////////////////////////////////
	// Each Archiver type opens and saves their content in a 
	// different way. They will implement this method to 
	// save their content to the temp folder.
	@Override
	public void backup(BBDocument __doc, String __path)
	{
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		FileUtils fu = new FileUtils();
		if(__path == null)
			__path = workingDocPath;
		
		//save working copy to temp folder and semantic files if part of a zip file
		if(fu.createXMLFile(__doc.getNewXML(), __path)){
			if(zip){
				String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(__path) + ".sem";
				copySemanticsFile(tempSemFile, fu.getPath(__path) + BBIni.getFileSep() + fu.getFileName(__path) + ".sem");
			}
		}
		else {
			new Notify("An error occured while saving your document.  Please check your original document.");
		}
				
		// Resume autosave feature.
		resumeAutoSave(__doc, __path);
		
	} // backup()
	
	protected  void zipDocument(){
		// Create zipper.
		Zipper zpr = new Zipper();
		// Zip it!
		zpr.Zip(zippedPath, originalDocPath);
	}

	private void unzip(String filePath){
		// Create unzipper.
		Zipper unzipr = new Zipper();
		// Unzip and update "opened" file.
		String sp = BBIni.getFileSep();
		String tempOutPath = BBIni.getTempFilesPath() + filePath.substring(filePath.lastIndexOf(sp), filePath.lastIndexOf(".")) + sp;
		workingDocPath = unzipr.Unzip(filePath, tempOutPath);
		
		// Store paths.
		zippedPath = tempOutPath;
	}

	private void newZipFile(String newZipPath){
		Zipper zpr = new Zipper();
		zpr.Zip(zippedPath, newZipPath);
		originalDocPath = newZipPath;
		zip = true;
	}

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		if(ext.equals("xml"))
			saveAsNimas(doc, path);
		if(ext.equals("zip"))
			saveAsZip(doc, path);
		else if(ext.equals("brf"))
			saveBrf(doc, path);
		else if(ext.equals("utd"))
			return saveAsUTD(doc, path);
					
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
		return this;
	}
	
	private void saveAsZip(BBDocument doc, String path){
		FileUtils fu = new FileUtils();
		fu.createXMLFile(doc.getNewXML(), workingDocPath);
		
		if(zippedPath != null){
			String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingDocPath) + ".sem";
			copySemanticsFile(tempSemFile, fu.getPath(workingDocPath) + BBIni.getFileSep() + fu.getFileName(workingDocPath) + ".sem");
		}
		
		if(zippedPath == null)
			zippedPath = BBIni.getTempFilesPath();
		
		newZipFile(path);
		zip = true;
	}
	
	private NimasArchiver saveAsNimas(BBDocument doc, String path){	
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		FileUtils fu = new FileUtils();
		if(fu.createXMLFile(doc.getNewXML(), path)) {
	    	//update semantics files for new file
			copySemanticsForNewFile(workingDocPath, path);
	    	
			//update working file path to newly saved file
	    	workingDocPath = BBIni.getTempFilesPath() + BBIni.getFileSep() + path.substring(path.lastIndexOf(BBIni.getFileSep()) + 1);	
	    	fu.copyFile(path, workingDocPath);
	    	
	    	//will be set by zipper
	    	if(!originalDocPath.endsWith(".zip") || (originalDocPath.endsWith(".zip") && path.endsWith(".xml")))
	    		originalDocPath = path;
	    }
	    else {
	    	new Notify("An error occured while saving your document.  Please check your original document.");
	    }
		
		if(zip == true)
			zip = false;
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
		return this;
	}
	
	private UTDArchiver saveAsUTD(BBDocument doc, String path){
		// Stop the autosave feature until we're done.
		pauseAutoSave();

		UTDArchiver arch = new UTDArchiver(workingDocPath, path, currentConfig);
		arch.save(doc, path);
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
		return arch;
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// Clears the list of path indices so we can once again create them on the fly.
	// Needed when we save a nimas file. We have to delete all of the temp files, 
	// zip, then recreate them for the user.
	public void resetDuplicatePathList() {
		allPaths.clear();
	}

	/////////////////////////////////////////////////////////////////////////////////
	// Resets the path list(resetDuplicatePathList()), and writes a 
	// chunked document to disk using the specified index.
	public void resetThenWrite(int idx) {
		resetDuplicatePathList();
		wrtieToDisk(idx);
	}

	/////////////////////////////////////////////////////////////////////////////////
	// Returns the number of potential files we would have 
	// if all of them were written to disk.
	public int getNumPotentialFiles() {
		return numPotentialFiles;
	}
	
	/***
	 * Write to the disk once at time if the file is not there already
	 * @param index
	 * @return
	 */
	public String wrtieToDisk(int index){
		// Build string path.
		String outPath = workingDocPath.substring(0, workingDocPath.lastIndexOf(BBIni.getFileSep())) + BBIni.getFileSep() + Integer.toString(index) + ".xml";
		if( !(allPaths.contains(Integer.toString(index))) ){

			// Break up document by level1 elements and retrieve the current one.
			Document curDoc = manageNimas(index);
			
			// Create file utility for saving our xml files.
			FileUtils fu = new FileUtils();

			// Write file.
			fu.createXMLFile( curDoc, outPath );
			allPaths.add(Integer.toString(index));
			
			// Add this file to the temp list so it will be deleted later.
			tempList.add(outPath);
		}

		return outPath;
	}

    /***
     * Get partition nimas book
     * @return ArrayList of Document which each entry of an array includes a nimas book
     */
	public Document manageNimas(int index)
	{
		 Document currentDoc =null;
		//Read xml template
		String sourcePath = BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "nimasTemplate.xml";
		File temp = new File(sourcePath);
		//get all level1 element
		Nodes allNode=getLevel1();
		
		// Store the number of files we would create if we 
		// ran through all of the indices.
		numPotentialFiles = allNode.size();
		
		if (index<allNode.size()){
			Node node=allNode.get(index);
			currentDoc=breakDocument(temp,node);
		}
		
		// Get the number of <img> elements.
		if(getImgCountList().size() == 0)
		{
			// Go through every <level1> element, and count the 
			// images.
			for(int curLvl1 = 0; curLvl1 < allNode.size(); curLvl1++)
			{
				// Add the count.
				Node nd = allNode.get(curLvl1);
				addToNumImgsList( new Document((Element)nd.copy()), curLvl1);
			}
			
			// Save paths to files that we may create.
			// Fill list.
			if(epubFileList.size() == 0) {
				for(int curF = 0; curF < numPotentialFiles; curF++) {
					epubFileList.add(workingDocPath.substring(0, workingDocPath.lastIndexOf(BBIni.getFileSep())) + BBIni.getFileSep()  + Integer.toString(curF) + ".xml");
				}
			}
		}
		
		
		return currentDoc;
	}


	/**
	 * Create an empty Document
	 * @param tempfile
	 * @return Document
	 */
	Document createDocument(File tempfile)
	{
		Logger logger = LoggerFactory.getLogger(NimasArchiver.class);
		Document tempDoc=new Document(new Element("root"));
		try{
			XMLReader xmlreader = XMLReaderFactory.createXMLReader();
			Resolver res = new Resolver();
			res.enableNimasPath();
			xmlreader.setEntityResolver(res);
			Builder parser = new Builder(xmlreader);
			tempDoc = parser.build(tempfile);
		}
		catch (ValidityException ex) {
			System.err.println("xml is not valid)");
			logger.error("XML is not valid", ex);
		}
		catch (ParsingException ex) {
			System.err.println("couldnt parse it");
			logger.error("Couldn't parse it", ex);
		}
		catch (IOException ex) {
			logger.error("IO Exception", ex);
		}
		catch(Exception ex) { 
			logger.error("Unforeseen exception", ex);
		}
		
		return tempDoc;
	}
	/**
	 * Create Namespase and get context
	 * @param doc
	 * @return context of document
	 */
	nu.xom.XPathContext getConetxt(Document doc)
	{
		// Namespace and context.
		String nameSpace = doc.getRootElement().getNamespaceURI();
		nu.xom.XPathContext context = new nu.xom.XPathContext("dtb", nameSpace);
		return context;

	}
	/***
	 * Get Node by using xpath
	 * @param qeuery
	 * @param doc
	 * @param context
	 * @return
	 */
	Nodes getXpath (String qeuery, Document doc,nu.xom.XPathContext context ) {
		return doc.query(qeuery,context);
	}


    /***
     * Get partition nimas book
     * @return ArrayList of Document which each entry of an array includes a nimas book
     */
	public ArrayList<Document> manageNimas()
	{
		ArrayList<Document> allDocs = new ArrayList<Document>();
		//Read xml template
		String sourcePath = BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "nimasTemplate.xml";
		File temp = new File(sourcePath);
		//get all level1 element
		Nodes allNode = getLevel1();
		for (int i = 0; i < allNode.size(); i++){
			allDocs.add( breakDocument(temp, allNode.get(i)) );
		}
		
		return allDocs;
		
	}

	
	/**
	 * Add Node to template
	 * @param addedNode
	 * @return small nimas document
	 */
	Document breakDocument(File temp,Node addedNode){
		Document tempDoc=createDocument(temp);
		nu.xom.XPathContext contextTemp=getConetxt(tempDoc);
		Nodes frontmatter = getXpath ("//dtb:frontmatter", tempDoc ,contextTemp );
		Nodes bodymatter = getXpath ("//dtb:bodymatter", tempDoc ,contextTemp );
		Nodes rearmatter = getXpath ("//dtb:rearmatter", tempDoc ,contextTemp );
		Element front = null;
		Element body = null;
		Element rear = null;
		if( frontmatter.size() > 0) front = (Element)frontmatter.get(0);
		if( bodymatter.size() > 0) body = (Element)bodymatter.get(0);
		if( rearmatter.size() > 0) rear = (Element)rearmatter.get(0);
		//get parent node
		Element parentNode=(Element)addedNode.getParent();
		addedNode.detach();
		if (parentNode.getLocalName().equals("frontmatter") && front != null)
			front.appendChild(addedNode);
		if (parentNode.getLocalName().equals("bodymatter") && body != null)
			body.appendChild(addedNode);
		if (parentNode.getLocalName().equals("rearmatter") && rear != null)
			rear.appendChild(addedNode);
		return tempDoc;

	}
	/***
	 * Get All level1 in current nimas file
	 * @return
	 */
	Nodes getLevel1(){
		//Read current nimas file
		File file=new File(workingDocPath);
		//create a document
		Document nimasDocument = createDocument(file);
		//get context
		nu.xom.XPathContext context= getConetxt(nimasDocument);
		// get all level one elements
		Nodes levelOnes=getXpath ("//dtb:level1", nimasDocument ,context );
		return levelOnes;

	}
	
	/////////////////////////////////////////////////////////////////////////////
	// Helper: Uses list created with manageNimas() to create an OPF file for 
	// EPUB conversion.
	private String _createOPFFromDocs(ArrayList<Document> docs)
	{
		// Create root package element.
		Element root = new Element("package");
		
		// Create document.
        Document document = new Document(root);
		
        // Create spine and manifest elements.
        Element spineElm = new Element("spine");
        Element manifestElm = new Element("manifest");
        
		// Loop through all documents and append to our OPF file.
        for(int curDoc = 0; curDoc < docs.size(); curDoc++)
        {
            // Create manifest entry.
            Element newManEntry = new Element("item");
            // Add attributes.
            newManEntry.addAttribute( new Attribute("id", Integer.toString(curDoc)) );
            newManEntry.addAttribute( new Attribute("href", Integer.toString(curDoc) + ".xhtml") );
            newManEntry.addAttribute( new Attribute("media-type", "application/xhtml+xml") );
            // Add to manifest.
            manifestElm.appendChild(newManEntry);
            
            // Create spine entry.
            Element newSpineEntry = new Element("itemref");
            // Add attributes.
            newSpineEntry.addAttribute( new Attribute("idref", Integer.toString(curDoc)) );
            // Add to spine.
            spineElm.appendChild(newSpineEntry);
        	
        } // for...
        
        // Put the filled spine and manifest into our package.
        root.appendChild(manifestElm);
        root.appendChild(spineElm);
        
		// Create file utility for saving opf.
		FileUtils fu = new FileUtils();
		// Build string path.
		String opfPath = workingDocPath.substring(0, workingDocPath.lastIndexOf(BBIni.getFileSep())) + BBIni.getFileSep() + "nimas2epub.opf";
		// Write opf.
		fu.createXMLFile( document, opfPath );
		
		// Return path to our shiny, new opf.
		return opfPath;
		
	} // _createOPFFromDocs()
} // class NimasArchiver