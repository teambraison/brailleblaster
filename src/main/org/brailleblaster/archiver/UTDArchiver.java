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

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;

public class UTDArchiver extends Archiver{

	UTDArchiver(String docToPrepare, boolean restore) {
		super(docToPrepare, restore);
		filterNames = new String[] { "BRF", "UTDML working document"};
		filterExtensions = new String[] { "*.brf", "*.utd"};
		currentConfig = findConfig();
		copyFileToTemp(docToPrepare);
		zip = false;
	}
	
	/** This constructor is used when saveAs is called
	 * @param oldPath: Path to previous file name used to create a new semantic file
	 * @param docToPrepare: name of document with new name
	 * @param config: configuration file to use
	 */
	UTDArchiver(String oldPath, String docToPrepare, String config, boolean restore){
		super(docToPrepare, restore);
		currentConfig = config;
		filterNames = new String[] { "BRF", "UTDML working document"};
		filterExtensions = new String[] { "*.brf", "*.utd"};
    	
    	copySemanticsForNewFile(oldPath, docToPrepare);
    	
		workingDocPath = BBIni.getTempFilesPath() + BBIni.getFileSep() + docToPrepare.substring(docToPrepare.lastIndexOf(BBIni.getFileSep()) + 1);
		zip = false;
	}

	@Override
	public void save(BBDocument doc, String path) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		FileUtils fu = new FileUtils();
		if(path == null)
			path = workingDocPath;
		
		doc.setOriginalDocType(doc.getDOM());
		setMetaData(doc);
		if(fu.createXMLFile(doc.getDOM(), workingDocPath)){
			fu.copyFile(workingDocPath, originalDocPath);
			String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(path) + ".sem";
			String newSemFile = fu.getPath(originalDocPath) + BBIni.getFileSep() + fu.getFileName(originalDocPath) + ".sem";
			copySemanticsFile(tempSemFile, newSemFile);
		}
		else {
			new Notify("An error occured while saving your document.  Please check your original document.");
		}
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
	}
	
	private void saveAsUtd(BBDocument doc, String path){
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		String tempFilePath = BBIni.getTempFilesPath() + BBIni.getFileSep() + path.substring(path.lastIndexOf(BBIni.getFileSep()) + 1);
		
		FileUtils fu = new FileUtils();
		doc.setOriginalDocType(doc.getDOM());
		setMetaData(doc);
		if(fu.createXMLFile(doc.getDOM(), tempFilePath)){
			fu.copyFile(tempFilePath, path);
			copySemanticsForNewFile(workingDocPath, path);
		}
		
		originalDocPath = path;
		workingDocPath = tempFilePath;
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
	}
	
	private void setMetaData(BBDocument doc){
		if(findMetaElement(doc.getDOM()) == null){
			Element root = doc.getRootElement();
			Element head = root.getChildElements().get(0);
			Element meta = doc.makeElement("meta", "configurationFile", currentConfig);
			head.appendChild(meta);
		}
	}

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		if(ext.equals("brf"))
			saveBrf(doc, path);
		else if(ext.equals("utd"))
			saveAsUtd(doc, path);
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
		return this;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// Each Archiver type opens and saves their content in a 
	// different way. They will implement this method to 
	// save their content to the temp folder.
	@Override
	public void backup(BBDocument __doc, String __path) {}
	
	private String findConfig(){
		File file = new File (workingDocPath);
		Builder parser = new Builder();
		
		try {
			Document doc = parser.build(file);
			Element meta = findMetaElement(doc);
			if(meta != null){
				String config = meta.getAttributeValue("configurationFile");
				return config;
			}
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		//return nimas if no element, this means utd was created before changes
		return "nimas.cfg";
	}
	
	private Element findMetaElement(Document doc){
		Element root = doc.getRootElement();
		Element head = root.getChildElements().get(0);
		Elements children = head.getChildElements();
		
		for(int i = 0; i < children.size(); i++){
			if(children.get(i).getAttribute("configurationFile") != null)
				return children.get(i);
		}
		
		return null;
	}
}
