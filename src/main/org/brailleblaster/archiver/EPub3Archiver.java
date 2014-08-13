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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Zipper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

class Response {
	   String  list_of_suggestions;
	   }

//////////////////////////////////////////////////////////////////////////////////
// Prepares an EPub3 document for opening.
public class EPub3Archiver extends Archiver {

	// Path to .opf file.
	String opfPath;

	// The main document we'll be appending to.
	Document mainDoc = null;
	// Body element of main doc.
	NodeList mainBodyElement = null;
	// Html element of main doc.
	NodeList mainHtmlElement = null;
	// Opf document.
	Document opfDoc = null;
	
	// The last bookmark we were at.
	String bkMarkStr = null;
	
	public EPub3Archiver(String docToPrepare) {
		super(docToPrepare);
		open(null);
		currentConfig = getAutoCfg("epub");
		filterNames = new String[] {"EPUB", "BRF", "UTDML"};
		filterExtensions = new String[] {"*.epub","*.brf", "*.utd"};
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// 
	private String open(String _opath) {
		
		// Init variables.
		mainDoc = null;
		mainBodyElement = null;
		mainHtmlElement = null;
		opfDoc = null;
		
		// First things first, we have to unzip the EPub doc.
		
		/////////
		// Unzip.

			// Get ready to unzip.
			Zipper zpr = null;	
		
			// If zipped.
			if( _opath == null ) {
				// Create unzipper.
				zpr = new Zipper();
				
				// Unzip.
				String sep = BBIni.getFileSep();
				String nameStr = originalDocPath.substring(originalDocPath.lastIndexOf(sep) + 1, originalDocPath.length());
				String outPath = BBIni.getTempFilesPath() + sep + nameStr.substring( 0, nameStr.lastIndexOf(".") ) + sep;
				zpr.Unzip( originalDocPath, outPath );
			}
		// Unzip.
		/////////
			
		// Get ready to look for the opf file.
		opfPath = _opath;
			
		
		// If zipped.
		if( opfPath == null ) {
			// Get paths to all unzipped files.
			ArrayList<String> unzippedPaths = zpr.getUnzippedFilePaths();
			
			// Find the .opf file.
			for(int curFile = 0; curFile < unzippedPaths.size(); curFile++)
			{
				// Does this file have an .opf extension?
				if(unzippedPaths.get(curFile).toLowerCase().endsWith(".opf") == true)
				{
					// Found it!
					opfPath = unzippedPaths.get(curFile).toLowerCase();
					
					// Found it, take a break.
					break;
					
				} // If ends with opf
				
			} // for(int curFile...
			
		} // if( opfPath == null )
		
		// If we couldn't find the opf file, no point in continuing.
		if(opfPath == null)
			return null;
		
		// Parse opf. Find all pages of our document.
		try {
			// Build factory, and parse the opf.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder;
	        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	        factory.setNamespaceAware(true); // Needed, just in case manifest/spine are in a namespace.
			builder = factory.newDocumentBuilder();
			opfDoc = builder.parse(opfPath);
			
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
				curDocFilePath = opfPath.substring( 0, opfPath.lastIndexOf(BBIni.getFileSep()) ) + BBIni.getFileSep();
				curDocFilePath += findHrefById(fileID).replace("/", BBIni.getFileSep());
				
				// Add this path to the list of document paths.
				epubFileList.add(curDocFilePath);
				 
				// If this is the first file, we'll use it as a base to add to.
				if(curSP == 0)
				{
					// Get main/first document.
					try
					{
						mainDoc = builder.parse(curDocFilePath);
					}
					catch(java.io.FileNotFoundException fnfe)
					{
						msgBx("Error", "Missing file in spine/manifest!", 200, 125);
						return null;
					}
					
					// Get body element.
					mainBodyElement = mainDoc.getElementsByTagName("body");
					// Get html element.
					mainHtmlElement = mainDoc.getElementsByTagName("html");
					
					// Add image count to list.
					addToNumImgsList(mainDoc, curSP);
					
					// We have the base document, skip to a document that we'll be adding to 
					// this one.
					continue;
					
				} // if(curSP == 0)
				
				// Parse this new document.
				Document nextDoc = builder.parse(curDocFilePath);
				
				// Get new document's body.
				NodeList newBodyElm = nextDoc.getElementsByTagName("body");
				
				// Add image count to list.
				addToNumImgsList(nextDoc, curSP);
				
				//////////////
				// Namespaces.
				
					// Next document's html element.
					NodeList newHtmlElm = nextDoc.getElementsByTagName("html");
					
					// Get attributes for this html element.
					NamedNodeMap newHtmlAtts = newHtmlElm.item(0).getAttributes();
					
					// Loop through this new document's attributes. If it has one that 
					// the main document doesn't have, add it.
					for(int curNewAtt = 0; curNewAtt < newHtmlAtts.getLength(); curNewAtt++)
					{
						// Grab potentially new attribute.
						String newAtt = newHtmlAtts.item(curNewAtt).toString();
						
						// Grab main doc's attributes.
						NamedNodeMap mainHtmlAtts = mainHtmlElement.item(0).getAttributes();
						
						// Loop through our main document's attributes. Is this 
						// new one in there somewhere? If so, don't add it.
						boolean skipThis = false;
						for(int curMainAtt = 0; curMainAtt < mainHtmlAtts.getLength(); curMainAtt++) {
							if(mainHtmlAtts.item(curMainAtt).toString().compareTo(newAtt) == 0) {
								skipThis = true;
								break;
							} // if(mainHtmlAtts...
						} // for(int curMainAtt...
						
						// If we make it here, then we couldn't find the new attribute 
						// in the main doc. Add it.
						if(skipThis == false){
							Node atty = mainDoc.importNode(newHtmlAtts.item(curNewAtt), true);
							mainHtmlElement.item(0).getAttributes().setNamedItem(atty);
						}
							
					} // for(int curNewAtt...

				// Namespaces.
				//////////////
					
				// Get children of body element.
				NodeList bodyChilds = newBodyElm.item(0).getChildNodes();
				
				// Add our bookmark comment. When we save later, this helps us save to the appropriate files.
				Comment comment = mainDoc.createComment( "BBBOOKMARK - " + findHrefById(fileID).replace("/", BBIni.getFileSep()) );
				mainBodyElement.item(0).appendChild(comment);
				
				// Loop through all of the children and add them to the main document.
				for(int curChild = 0; curChild < bodyChilds.getLength(); curChild++)
				{
					// Add child to main doc.
					Node newNode = mainDoc.importNode(bodyChilds.item(curChild), true);
					mainBodyElement.item(0).appendChild( newNode );
					
				} // for(int curChild = 0...
				
				
// 
//				/ \_/ \
//	      >>>---|   ----->
//				 \   /
//				   V
// 
			} // for(int curSP...
			
			// Save file concatenation.
			
			// Output filename.
			String outFileName = curDocFilePath.substring( 0, curDocFilePath.lastIndexOf(BBIni.getFileSep()) ) + BBIni.getFileSep() + "temp.xml";
			
			// Get DOM Implementation.
			DOMImplementationLS domImplementationLS =
		    (DOMImplementationLS) mainDoc.getImplementation().getFeature("LS","3.0");
			
			// Prepare output stream.
			LSOutput lsOutput = domImplementationLS.createLSOutput();
			FileOutputStream outputStream = new FileOutputStream( outFileName );
			lsOutput.setByteStream( outputStream );
			
			// Create the serializer, serialize/output xml, then close the stream.
			LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
			lsSerializer.write( mainDoc, lsOutput );
			outputStream.close();
			
			workingDocPath = outFileName;
			// Return path to new document.
			return outFileName;
			
		} // try/catch
		catch (ParserConfigurationException pce) { pce.printStackTrace(); }
		catch (SAXException sxe) { sxe.printStackTrace(); }
		catch (IOException ioe) { ioe.printStackTrace(); }
		
		// An error occurred; we don't have a file path to return.
		return null;
		
	} // open()
	
	@Override
	public void save(BBDocument doc, String path, boolean zip)
	{
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		try
		{
			// Create XOM builder.
			nu.xom.Builder parser = new nu.xom.Builder();
			
			// Namespace and context.
			String nameSpace = doc.getDOM().getRootElement().getNamespaceURI();
			nu.xom.XPathContext context = new nu.xom.XPathContext("dtb", nameSpace);
			
			// Get xom document.
			nu.xom.Document mainDoc = doc.getNewXML();
			// Body element.
			nu.xom.Nodes currentMainElement = mainDoc.query("//dtb:body[1]", context);
			
			// Get the number of children our source has.
			int curCh = 0;
			int numChilds = currentMainElement.get(0).getChildCount();
			
			// Loop through epub docs, open them, then update their body's.
			for(int curPath = 0; curPath < epubFileList.size(); curPath++)
			{
				// Parse this epub document.
			
				nu.xom.Document curDoc;
				//check for if it is mac or non mac
				if(BBIni.getPlatformName().equals("cocoa"))
				      curDoc = parser.build(  epubFileList.get(curPath) );
				else
					  curDoc = parser.build(  "File:" + BBIni.getFileSep()+epubFileList.get(curPath) );
					
				
				// Get epub document's body.
				nu.xom.Nodes bodyNodes = curDoc.getRootElement().query("//dtb:body[1]", context);
				nu.xom.Element bodyElement = ( (nu.xom.Element)(bodyNodes.get(0)) );
				
				// Remove all children of the body element.
				bodyElement.removeChildren();
				
				// For every child, append it to the destination.
				for( ; curCh < numChilds; curCh++)
				{
					// If this is a bookmark, skip it and move onto next file.
					if( currentMainElement.get(0).getChild(curCh).toString().contains("BBBOOKMARK - ") ) {
						curCh++;
						break;
					}
					
					// Append the current child.
					bodyElement.appendChild( currentMainElement.get(0).getChild(curCh).copy() );
					
				} // for(int curCh...
				
				// Save this xhtml document.
				FileUtils fu = new FileUtils();
				fu.createXMLFile(curDoc, epubFileList.get(curPath));
				
			} // for(int curPath...
		
		} // try/catch
		catch (ValidityException ve) { ve.printStackTrace(); }
		catch (ParsingException pe) { pe.printStackTrace(); }
		catch (IOException ioe) { ioe.printStackTrace(); }
		
		// All documents saved. Rezip.
		
		///////
		// Zip.

			if(zip == true) {
				
				// Create unzipper.
				Zipper zpr = new Zipper();
				
				// Create paths.
				String sep = BBIni.getFileSep();
				String nameStr = originalDocPath.substring(originalDocPath.lastIndexOf(sep) + 1, originalDocPath.length());
				String inputDir = BBIni.getTempFilesPath() + sep + nameStr.substring( 0, nameStr.lastIndexOf(".") ) + sep;
				String outFilePath = originalDocPath.substring( 0, originalDocPath.lastIndexOf(BBIni.getFileSep()) ) + BBIni.getFileSep() + nameStr;
				
				// If we are to save somewhere else... "Save As"
				if(path != null){
					outFilePath = path;
					originalDocPath = outFilePath;
				}
				
				// Zip.
				zpr.Zip(inputDir, outFilePath);
			}
			
		// Zip.
		///////
			
		// Resume autosave feature.
		resumeAutoSave(doc, path);
			
	} // save()
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Simple message box for alerts and messages to user.
	public void msgBx(String cap, String msg, int w, int h)
	{
		// Tell user there are no image tags.
		Display dlgDisp;
		final Shell dlgShl;
		dlgDisp = Display.getDefault();
		dlgShl = new Shell(dlgDisp, SWT.WRAP);
		dlgShl.setText(cap);
		Button okBtn = new Button(dlgShl, SWT.PUSH);
		okBtn.setText("Okay");
		okBtn.setBounds(w / 2 - 100 / 2, h - 60, 100, 25);
		okBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Close dialog.
				dlgShl.close();
			
			} // widgetSelected()
		
		}); // okBtn.addSelectionListener...
		
		Label alertText = new Label(dlgShl, SWT.WRAP);
		alertText.setBounds(0, 0, 250, 100);
		alertText.setText(msg);
		dlgShl.setSize(w, h);
		dlgShl.open();
		while (!dlgShl.isDisposed()) {
			if (!dlgDisp.readAndDispatch())
			dlgDisp.sleep();
		}
	
	} // public void msgBx(String cap, string msg)

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		if(ext.equals("epub"))
			save(doc, path, true);
		else if(ext.equals("brf"))
			saveBrf(doc, path);
		else if(ext.equals("utd"))
			return saveAsUTD(doc, path);
		
		// Restart autosave feature.
		resumeAutoSave(doc, path);
		
		return this;
	}
	
	private UTDArchiver saveAsUTD(BBDocument doc, String path){
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		UTDArchiver arch = new UTDArchiver(path, currentConfig);
		arch.save(doc, path, true);
		
		// Restart autosave feature.
		resumeAutoSave(doc, path);
		
		return arch;
	}
	
	
} // class EPubArchiver
