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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XPathContext;
import nu.xom.converters.DOMConverter;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.Zipper;
import org.w3c.dom.DOMImplementation;

//////////////////////////////////////////////////////////////////////////////////
// Prepares Nimas Archive for opening.
public class NimasArchiver extends Archiver {


	Set <String> allPaths;


	NimasArchiver(String docToPrepare) {
		super(docToPrepare);
		if(docToPrepare.endsWith(".zip"))
			unzip(docToPrepare);
		
		// Segment the single NIMAS file. This will make rendering 
		// faster in certain perspectives.
		//writeNimasSegments();
		
		currentConfig = getAutoCfg("nimas"); // Nimas document.
		filterNames = new String[] {"XML", "XML Zip", "BRF", "UTDML"};
		filterExtensions = new String[] {"*.xml", "*.zip", "*.brf", "*.utd"};
		allPaths=new HashSet<String>();
	}
	
	@Override
	public void save(BBDocument doc, String path) {
		FileUtils fu = new FileUtils();
		if(path == null)
			path = workingDocPath;
		
		if(fu.createXMLFile(doc.getNewXML(), path)){
			String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(path) + ".sem"; 
			copySemanticsFile(tempSemFile, fu.getPath(path) + BBIni.getFileSep() + fu.getFileName(path) + ".sem");
		}
		else {
			new Notify("An error occured while saving your document.  Please check your original document.");
		}
		
		// If the document came from a zip file, then rezip it.
		if(zippedPath.length() > 0)
			zipDocument();
	} // save()
	
	protected  void zipDocument(){
		// Create zipper.
		Zipper zpr = new Zipper();
		// Input string.
		String sp = BBIni.getFileSep();
		String inPath = BBIni.getTempFilesPath() + zippedPath.substring(zippedPath.lastIndexOf(sp), zippedPath.lastIndexOf(".")) + sp;
//		String inPath = zippedPath.substring(0, zippedPath.lastIndexOf(".")) + BBIni.getFileSep();
		// Zip it!
		zpr.Zip(inPath, zippedPath);
	}

	private void unzip(String filePath){
		// Create unzipper.
		Zipper unzipr = new Zipper();
		// Unzip and update "opened" file.
		// workingFilePath = unzipr.Unzip(fileName, fileName.substring(0, fileName.lastIndexOf(".")) + BBIni.getFileSep());
		String sp = BBIni.getFileSep();
		String tempOutPath = BBIni.getTempFilesPath() + filePath.substring(filePath.lastIndexOf(sp), filePath.lastIndexOf(".")) + sp;
		workingDocPath = unzipr.Unzip(filePath, tempOutPath);
		// Store paths.
		zippedPath = filePath;
	}

	private void newZipFile(String newZipPath){
		Zipper zpr = new Zipper();
		
		String sp = BBIni.getFileSep();
		String inPath = BBIni.getTempFilesPath() + zippedPath.substring(zippedPath.lastIndexOf(sp), zippedPath.lastIndexOf(".")) + sp;

		zpr.Zip(inPath, newZipPath);
		zippedPath = newZipPath;
		originalDocPath = zippedPath;
	}

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		if(ext.equals("xml"))
			saveAsNimas(doc, path);
		if(ext.equals("zip")){
			saveAsNimas(doc, workingDocPath);
			newZipFile(path);
		}
		else if(ext.equals("brf"))
			saveBrf(doc, path);
		else if(ext.equals("utd"))
			return saveAsUTD(doc, path);
					
		return this;
	}
	
	private NimasArchiver saveAsNimas(BBDocument doc, String path){
		FileUtils fu = new FileUtils();
		if(fu.createXMLFile(doc.getNewXML(), path)) {
	    	String tempSemFile; 			    
		    if(workingDocPath == null)
		    	tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName("outFile.utd") + ".sem";
		    else
		    	tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingDocPath) + ".sem";
		    
		    //String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem"; 
	    	String savedSemFile = fu.getPath(path) + BBIni.getFileSep() + fu.getFileName(path) + ".sem";   
	    
	    	//Save new semantic file to correct location and temp folder for further editing
	    	copySemanticsFile(tempSemFile, savedSemFile);
	    	copySemanticsFile(tempSemFile, BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(path) + ".sem");
	    	
			//update working file path to newly saved file
	    	workingDocPath = path;
	    	if(originalDocPath == null || originalDocPath.endsWith(".zip"))
	    		originalDocPath = path;
	    }
	    else {
	    	new Notify("An error occured while saving your document.  Please check your original document.");
	    }
		return this;
	}
	
	private UTDArchiver saveAsUTD(BBDocument doc, String path){
		UTDArchiver arch = new UTDArchiver(path, currentConfig);
		arch.save(doc, path);
		return arch;
	}

	/***
	 * Write to the disk once at time if the file is not there already
	 * @param index
	 * @return
	 */
	public String wrtieToDisk(int index){
		// Build string path.
		String outPath = workingDocPath.substring(0, workingDocPath.lastIndexOf(BBIni.getFileSep())) + BBIni.getFileSep() + Integer.toString(index) + ".xml";
		if(!(allPaths.contains(Integer.toString(index)))){

			Document curDoc=manageNimas(index);
			// Create file utility for saving our xml files.
			FileUtils fu = new FileUtils();

			// Write file.
			fu.createXMLFile( curDoc, outPath );
			allPaths.add(Integer.toString(index));
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
		if (index<allNode.size()){
			Node node=allNode.get(index);
			currentDoc=breakDocument(temp,node);
			
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
		Document tempDoc=new Document(new Element("root"));
		try{
			Builder parser=new Builder();
			tempDoc= parser.build(tempfile);
		}
		catch (ValidityException ex) {
			System.err.println("xml is not valid)");
		}
		catch (ParsingException ex) {
			System.err.println("couldnt parse it");
		}
		catch (IOException ex) {
//			System.err.println("some errors happened");
			System.err.println( ex.toString() );
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
	// Writes segmented NIMAS documents to disc and returns a list of their 
	// paths.
	public ArrayList<String> writeNimasSegments()
	{
        // Grab list of documents after breaking them up by level1's.
        ArrayList<Document> docs = manageNimas();
		
	    // Create file utility for saving our xml files.
		FileUtils fu = new FileUtils();
		
		// Path to xsl file. Add three slashes to avoid BS.
    	String xslPath = "file:///" + BBIni.getProgramDataPath() + BBIni.getFileSep() + "xsl" + BBIni.getFileSep() + "dtb2005html.xsl";
//    	
//    	// Build the xsl document.
    	Document xslDoc = null;
		Builder builder = new Builder();
        try { xslDoc = builder.build(xslPath); }
        catch (ValidityException e1) { e1.printStackTrace(); }
        catch (ParsingException e1) { e1.printStackTrace(); }
        catch (IOException e1) { e1.printStackTrace(); }
        
        // Create the transform.
        XSLTransform xslt = null;
        try { xslt = new XSLTransform(xslDoc); }
        catch (XSLException e1) { e1.printStackTrace(); }
    	
        // Loop through the documents, write to file, count images in each.
        for(int curDoc = 0; curDoc < docs.size(); curDoc++)
        {
        	// Finally transform the document.
            Nodes newDocNodes = null;
			try { newDocNodes = xslt.transform( docs.get(curDoc) ); }
			catch (XSLException e) { e.printStackTrace(); }
            Document transformedDoc = XSLTransform.toDocument(newDocNodes);
        	
        	// Build string path.
    		String outPath = workingDocPath.substring(0, workingDocPath.lastIndexOf(BBIni.getFileSep())) + BBIni.getFileSep() + Integer.toString(curDoc) + ".xhtml";
    		
    		// Add path to list.
    		epubFileList.add(outPath);
    		
    		// Count the images in this document.
    		addToNumImgsList(docs.get(curDoc));
    		
    		// Write file.
    		fu.createXMLFile( transformedDoc, outPath );
//    		fu.createXMLFile( docs.get(curDoc), outPath );
        	
        } // for(int curDoc...
        
        // Create the opf file.
        return epubFileList;
		
	} // writeNimasSegments()
	
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
