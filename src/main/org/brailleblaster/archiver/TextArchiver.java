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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Text;
import nu.xom.ValidityException;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;

public class TextArchiver extends Archiver{
	private FileUtils fu;
	private Document doc;
	private final String TEMPLATEPATH = BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "textFileTemplate.html";
	private final int BODYELEMENTINDEX = 1;
	
	TextArchiver(String docToPrepare) {
		super(docToPrepare);
		fu = new FileUtils();
		currentConfig = getAutoCfg("epub");
		filterNames = new String[] { "HTML","TEXT", "BRF", "UTDML working document"};
		filterExtensions = new String[] { "*.html", "*.txt", "*.brf", "*.utd"};
		open();
	}

	private String open() {
		formatDocument();
		return this.workingDocPath;
	}

	@Override
	public void save(BBDocument doc, String path) {
		PrintWriter out = null;
		Element root = doc.getRootElement();
		String text = getDocumentText(root, "");
		try {
			out = new PrintWriter(path);
			out.println(text);
			if(!path.equals("originalDocPath"))
				originalDocPath = path;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			if(out != null)
				out.close();
		}
		
	}
	
	private String getDocumentText(Element e, String text){
		int count = e.getChildCount();
		for(int i = 0; i < count; i++){
			if(e.getChild(i) instanceof Element && !((Element)e.getChild(i)).getLocalName().equals("brl"))
				text = getDocumentText((Element)e.getChild(i), text);
			else if(e.getChild(i) instanceof Text)
				text += e.getChild(i).getValue() + "\n";
		}
		
		return text;
	}
	
	private void formatDocument(){
		buildTemplate();
		Element body = getStartingNode();
		
		String currentLine;
		 
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(originalDocPath));
			
			while ((currentLine = br.readLine()) != null) {
				if(currentLine.length() > 0)
					addToBody(body, currentLine);
			}
			
			br.close();
			createWorkingFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	private void buildTemplate(){
		File f = new File(TEMPLATEPATH);
		Builder builder = new Builder();
		
		try {
			doc = builder.build(f);
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void addToBody(Element body, String text){
		Element p = new Element("p");
		p.appendChild(new Text(text));
		p.setNamespaceURI(doc.getRootElement().getNamespaceURI());
		body.appendChild(p);
	}
	
	private void createWorkingFile(){
		String tempPath = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(originalDocPath) + ".xml";
		fu.createXMLFile(doc, tempPath);
		workingDocPath = tempPath;
	}
	
	private Element getStartingNode(){
		return doc.getRootElement().getChildElements().get(BODYELEMENTINDEX);
	}

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		if(ext.equals("html"))
			return saveAsWeb(doc, path);
		else if(ext.equals("utd"))
			return saveAsUTD(doc, path);
		else if(ext.equals("txt"))
			save(doc, path);
		else if(ext.equals("brf"))
			saveBrf(doc, path);
		
		return this;
	}
	
	private UTDArchiver saveAsUTD(BBDocument doc, String path){
		UTDArchiver arch = new UTDArchiver(path, currentConfig);
		arch.save(doc, path);
		return arch;
	}
	
	public WebArchiver saveAsWeb(BBDocument doc, String path){
		WebArchiver arch = new WebArchiver(path);
		arch.save(doc, path);
		return arch;
	}
}
