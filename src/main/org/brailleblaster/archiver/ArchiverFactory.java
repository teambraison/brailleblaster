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

//////////////////////////////////////////////////////////////////////////////////
// Archiver Factory determines file type of document, and gives appropriate 
// object for opening/handling.
public class ArchiverFactory {
	
	//////////////////////////////////////////////////////////////////////////////////	
	// Constructor.
	ArchiverFactory() {  }
	
	//////////////////////////////////////////////////////////////////////////////////
	// Pass a filepath to a document, and getArchive() will determine the file type, 
	// and return the appropriate Archive.
	public static Archiver getArchive(String filePath)
	{
		// If the archive is contained within a zip file, go ahead and unzip it.
//		if( filePath.toLowerCase().endsWith(".zip") )
//		{
//			// Create zipper utility.
//			Zipper zipr = new Zipper();
//			zipr.Unzip( filePath, filePath.substring(0, filePath.lastIndexOf(".")) + BBIni.getFileSep());
//			filePath = zipr.GetXmlPath();
//			
//		} // UNZIP
		
		// Is this EPub3?
		if( isEPUB3(filePath) )
			return new EPub3Archiver(filePath);
		// Is this EPub?
		if( isEPUB(filePath) )
			return new EPub3Archiver(filePath);
		// Is this Nimas?
		if(isTextFile(filePath))
			return new TextArchiver(filePath);
		if( isNIMAS(filePath) )
			return new NimasArchiver(filePath);
		if(isUTD(filePath))
			return new UTDArchiver(filePath);
		if(isHTML(filePath))
			return new WebArchiver(filePath);
		if(isBRF(filePath))
			return new BrfArchiver(filePath);
		
		// Could not determine file type.
		return null;
		
	} // getArchive()
	
	//////////////////////////////////////////////////////////////////////////////////
	// Is this an ePub document?
	static boolean isEPUB(String pathToDoc)
	{
		
	
		// This isn't an EPUB document.
		return false;
	
	} // isEPUB()
	
	//////////////////////////////////////////////////////////////////////////////////
	// Is this an ePub3 document?
	static boolean isEPUB3(String pathToDoc)
	{
		// Does it end with .epub?
		if(pathToDoc.toLowerCase().endsWith(".epub"))
			return true;
		
		// This isn't an EPUB document.
		return false;
		
	} // isEPUB3()
	
	//////////////////////////////////////////////////////////////////////////////////
	// Is this a Nimas document?
	static boolean isNIMAS(String pathToDoc)
	{
		// TODO: Add proper code to determine this doc type.
		if(pathToDoc.toLowerCase().endsWith(".xml") || pathToDoc.toLowerCase().endsWith(".zip"))
			return true;
		
		// This isn't a Nimas document.
		return false;
		
	} // isNIMAS()
	
	static boolean isTextFile(String pathToDoc){
		if(pathToDoc.endsWith(".txt"))
			return true;
		else
			return false;
	}
	
	static boolean isUTD(String path){
		if(path.endsWith(".utd"))
			return true;
		else
			return false;		
	}
	
	static boolean isHTML(String path){
		if(path.endsWith(".html") || path.endsWith(".htm") || path.endsWith(".xhtml"))
			return true;
		else
			return false;
	}
	
	static boolean isBRF(String path){
		if(path.endsWith(".brf"))
			return true;
		else
			return false;
	}
} // class ArchiverFactory