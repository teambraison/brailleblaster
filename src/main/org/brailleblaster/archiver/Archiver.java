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
import java.util.Properties;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;

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
	
	//////////////////////////////////////////////////////////////////////////////////
	// Constructor. Stores path to document to prepare.
	Archiver(String docToPrepare)
	{
		// Store paths.
		originalDocPath = docToPrepare;
		workingDocPath = originalDocPath;
		zippedPath = "";
		documentEdited = false;
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
	
} // class Archiver