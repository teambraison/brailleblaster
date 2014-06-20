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

import javax.xml.parsers.DocumentBuilderFactory;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XPathContext;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.Zipper;

//////////////////////////////////////////////////////////////////////////////////
// Prepares Nimas Archive for opening.
public class NimasArchiver extends Archiver {

	NimasArchiver(String docToPrepare) {
		super(docToPrepare);
		if(docToPrepare.endsWith(".zip"))
			unzip(docToPrepare);
		
		currentConfig = getAutoCfg("nimas"); // Nimas document.
		filterNames = new String[] {"XML", "XML Zip", "BRF", "UTDML"};
		filterExtensions = new String[] {"*.xml", "*.zip", "*.brf", "*.utd"};
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
     * Get partition nimas book
     * @return ArrayList of Document which each entry of an array includes a nimas book
     */
	public ArrayList<Document> manageNimas()
	{
		ArrayList<Document> allDocs =new ArrayList<Document>();
		//Read xml template
		String sourcePath=BBIni.getProgramDataPath() + BBIni.getFileSep()+"xmlTemplates"+BBIni.getFileSep() +"nimasTemplate.xml";
		File temp = new File(sourcePath);
		//get all level1 element
		Nodes allNode=getLevel1();
		for (int i=0;i<allNode.size();i++){
			Node node=allNode.get(i);
			allDocs.add(breakDocument(temp,node));
			
		}
		
		return allDocs;
		
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
			System.err.println("some errors happened");
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
		nu.xom.XPathContext context = new nu.xom.XPathContext("dyb", nameSpace);
		return context;

	}
	/***
	 * Get Node by using xpath
	 * @param qeuery
	 * @param doc
	 * @param context
	 * @return
	 */
	Nodes getXpath (String qeuery, Document doc,nu.xom.XPathContext context )
	{
		Nodes result = doc.query(qeuery,context);
		return result;

	}

	/**
	 * Add Node to template
	 * @param addedNode
	 * @return small nimas document
	 */
	Document breakDocument(File temp,Node addedNode){
		Document tempDoc=createDocument(temp);
		nu.xom.XPathContext contextTemp=getConetxt(tempDoc);
		Nodes frontmatter =getXpath ("//dyb:frontmatter", tempDoc ,contextTemp );
		Nodes bodymatter =getXpath ("//dyb:bodymatter", tempDoc ,contextTemp );
		Nodes rearmatter =getXpath ("//dyb:rearmatter", tempDoc ,contextTemp );
		Element front=(Element) frontmatter.get(0);
		Element body=(Element) bodymatter.get(0);
		Element rear=(Element) rearmatter.get(0);
		//get parent node
		Element parentNode=(Element)addedNode.getParent();
		addedNode.detach();
		if (parentNode.getLocalName().equals("frontmatter"))
			front.appendChild(addedNode);
		if (parentNode.getLocalName().equals("bodymatter"))
			body.appendChild(addedNode);
		if (parentNode.getLocalName().equals("rearmatter"))
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
		Nodes levelOnes=getXpath ("//dyb:level1", nimasDocument ,context );
		return levelOnes;

	}

	
	
} // class NimasArchiver
