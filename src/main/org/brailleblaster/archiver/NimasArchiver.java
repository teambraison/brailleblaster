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
} // class NimasArchiver
