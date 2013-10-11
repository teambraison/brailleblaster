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
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by John J. Boyer john.boyer@abilitiessoft.com
 */

package org.brailleblaster.archiver;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.brailleblaster.BBIni;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.util.Zipper;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

//////////////////////////////////////////////////////////////////////////////////
// Prepares an EPub document for opening.
public class EPubArchiver extends Archiver {

	// Path to .opf file.
	String opfPath;
	// Manifest and spine elements.
	NodeList manifestElements;
	NodeList spineElements;
	
	EPubArchiver(String docToPrepare) {
		super(docToPrepare);
	}


	
	//////////////////////////////////////////////////////////////////////////////////
	// 
	@Override
	public String open() {
		
		// First things first, we have to unzip the EPub doc.
		
		/////////
		// Unzip.

			// Create unzipper.
			Zipper zpr = new Zipper();
			
			// Unzip.
			zpr.Unzip( originalDocPath, originalDocPath.substring(0, originalDocPath.lastIndexOf(".")) + BBIni.getFileSep() );
		
		// Unzip.
		/////////
			
		// Get ready to look for the opf file.	
		opfPath = null;
			
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
		
		// If we couldn't find the opf file, no point in continuing.
		if(opfPath == null)
			return null;
		
		// Parse opf. Find all pages of our document.
		try {
			// Build factory, and parse the opf.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document opfDoc = builder.parse(opfPath);

			// The main document we'll be appending to.
			Document mainDoc = null;
			
			// Body element of main doc.
			NodeList mainBodyElement = null;
			// Html element of main doc.
			NodeList mainHtmlElement = null;
			
			// Grab the spine elements and manifest elements.
			manifestElements = opfDoc.getElementsByTagName("item");
			spineElements = opfDoc.getElementsByTagName("itemref");
			
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
				 
				// If this is the first file, we'll use it as a base to add to.
				if(curSP == 0)
				{
					// Get main/first document.
					mainDoc = builder.parse(curDocFilePath);
					
					// Get body element.
					mainBodyElement = mainDoc.getElementsByTagName("body");
					// Get html element.
					mainHtmlElement = mainDoc.getElementsByTagName("html");
					
					// We have the base document, skip to a document that we'll be adding to 
					// this one.
					continue;
					
				} // if(curSP == 0)
				
				// Parse this new document.
				Document nextDoc = builder.parse(curDocFilePath);
				
				// Get new document's body.
				NodeList newBodyElm = nextDoc.getElementsByTagName("body");
				
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
				Comment comment = mainDoc.createComment("BBBOOKMARK");
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
			lsOutput.setByteStream( (OutputStream) outputStream );
			
			// Create the serializer, serialize/output xml, then close the stream.
			LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
			lsSerializer.write( mainDoc, lsOutput );
			outputStream.close();
			
			// Return path to new document.
			return outFileName;
			
		} // try/catch
		catch (ParserConfigurationException pce) { pce.printStackTrace(); }
		catch (SAXException sxe) { sxe.printStackTrace(); }
		catch (IOException ioe) { ioe.printStackTrace(); }
		
		// An error occurred; we don't have a file path to return.
		return null;
		
	} // open()

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
	
	@Override
	public void save(Manager dm, String path) {
		
		
		
	} // save()
	
} // class EPubArchiver
