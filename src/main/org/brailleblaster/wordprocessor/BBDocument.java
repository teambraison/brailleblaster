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
  * this program; see the file LICENSE.txt
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.wordprocessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.mapping.BrailleMapElement;
import org.brailleblaster.mapping.MapList;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.util.CheckLiblouisutdmlLog;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.liblouis.liblouisutdml;


public class BBDocument {
	private Document doc;
	private static String fileSep = BBIni.getFileSep();
	private liblouisutdml lutdml = liblouisutdml.getInstance();
	private FileUtils fu = new FileUtils();
	static Logger logger = BBIni.getLogger();
	private DocumentManager dm;
	private ArrayList<String>missingSemanticsList;
	
	public BBDocument(DocumentManager dm){		
		this.dm = dm;
		this.missingSemanticsList = new ArrayList<String>();
	}
	public boolean startDocument (InputStream inputStream, String configFile, String configSettings) throws Exception {
		String fileName = "xxx";
		return buildDOM(fileName);
	}
	
	public boolean startDocument (String completePath, String configFile, String configSettings) throws Exception {
			return setupFromFile (completePath, configFile, configSettings);
	}
	
	private boolean setupFromFile (String completePath, String configFile, String configSettings) throws Exception {
		String configFileWithPath = "temp";
		String configWithUTD;
		
		// Use the default; we don't have a local version.
		configFileWithPath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile);
		
		if (configSettings == null) {
			configWithUTD = "formatFor utd\n mode notUC\n printPages no\n";
		} 
		else {
			configWithUTD = configSettings + "formatFor utd\n mode notUC\n printPages no\n";
		}
		String outFile = BBIni.getTempFilesPath() + fileSep + "outFile.utd";
		String logFile = BBIni.getLogFilesPath() + fileSep + "liblouisutdml.log";
		int extPos = completePath.lastIndexOf (".") + 1;
		String ext = completePath.substring (extPos);
		if (ext.equalsIgnoreCase ("xml")) {
			if(lutdml.translateFile (configFileWithPath, completePath, outFile, logFile, configWithUTD, 0))
				return buildDOM(outFile);
		} 
		else if (ext.equalsIgnoreCase ("txt")) {
			if(lutdml.translateTextFile (configFileWithPath, completePath, outFile, logFile, configWithUTD, 0))
				return buildDOM(outFile);
		} 
		else if (ext.equalsIgnoreCase ("brf")) {
			if(lutdml.backTranslateFile (configFileWithPath, completePath, outFile, logFile, configWithUTD, 0))
				return buildDOM(outFile);
		} 
		else if (ext.equalsIgnoreCase ("utd")) {
			return buildDOM(completePath);
		} 
		else {
			throw new IllegalArgumentException (completePath + " not .xml, .txt, or .brf");
		}
		new CheckLiblouisutdmlLog().displayLog();
		
		
		return false;
	}
	
	private boolean buildDOM(String fileName) throws Exception{
		if (this.doc != null) {
			  throw new Exception ("Attempt to reuse instance");
		}
		
		File file = new File (fileName);
		Builder parser = new Builder();
		try {
			this.doc = parser.build (file);
			removeBraillePageNumber();
			return true;
		} 
		catch (ParsingException e) {
			new Notify("Problem processing " + fileName + " See stack trace.");
			e.printStackTrace();
			return false;
		} 
		catch (IOException e) {
			new Notify ("IO error occurred while parsing " + fileName + " See stack trace.");
			e.printStackTrace();
			return false;
		}
	}
	
	public void updateDOM(MapList list, Message message){
		switch(message.type){
			case UPDATE:
				updateNode(list, message);
				break;
			case REMOVE_NODE:
				removeNode(list.get((Integer)message.getValue("index")), message);
				break;
			default:
				System.out.println("No available operations for this message type");
			break;
		}
	}
	
	private void updateNode(MapList list, Message message){
		int total = 0;
		String text = (String)message.getValue("newText");
		text = text.replace("\n", "").replace("\r", "");
		message.put("newText", text);
		calculateDifference(list.getCurrent().n.getValue(), text, message);
		changeTextNode(list.getCurrent().n, text);
		
		if(text.equals("") || isWhitespace(text)){
			total = insertEmptyBrailleNode(list.getCurrent(), list.getNextBrailleOffset(list.getCurrentIndex()), message);
		}
		else if(list.getCurrent().brailleList.size() > 0){
			total = changeBrailleNodes(list.getCurrent(), message);
		}
		else {
			insertBrailleNode(list.getCurrent(), list.get(list.getCurrentIndex() + 1).brailleList.getFirst().start, text);
		}
		message.put("brailleLength", total);
	}

	private void changeTextNode(Node n, String text){
		Text temp = (Text)n;
		logger.log(Level.INFO, "Original Text Node Value: " + temp.getValue());
		temp.setValue(text);
		logger.log(Level.INFO, "New Text Node Value: " +  temp.getValue());
		System.out.println("New Node Value:\t" + temp.getValue());
	}
	
	private int changeBrailleNodes(TextMapElement t, Message message){
		Document d = getStringTranslation(t, (String)message.getValue("newText"));
		int total = 0;
		int startOffset = 0;
		String insertionString = "";
		Element e;
		Element brlParent = ((Element)d.getRootElement().getChild(0));
		Elements els = brlParent.getChildElements();
		
		if(els.get(0).getLocalName().equals("strong") || els.get(0).getLocalName().equals("em")){
			e = els.get(0).getChildElements().get(0);
			addNamespace(e);
			brlParent.getChildElements().get(0).removeChild(e);
		}
		else {
			e = brlParent.getChildElements("brl").get(0);
			addNamespace(e);
			brlParent.removeChild(e);
		}
		
	//	System.out.println(e.getValue());
		startOffset = t.brailleList.getFirst().start;
		String logString = "";
		
		for(int i = 0; i < t.brailleList.size(); i++){
			//total += t.brailleList.get(i).n.getValue().length();
			total += t.brailleList.get(i).end - t.brailleList.get(i).start;
			if(afterNewlineElement(t.brailleList.get(i).n) && i > 0){
				total++;
			}
			logString += t.brailleList.get(i).n.getValue() + "\n";
		}
		logger.log(Level.INFO, "Original Braille Node Value:\n" + logString);
			
		Element parent = (Element)t.n.getParent();
		Element child = (Element)t.brailleList.getFirst().n.getParent();
		while(!child.getParent().equals(parent)){
			child = (Element)child.getParent();
		};
		parent.replaceChild(child, e);	
		t.brailleList.clear();
		
		boolean first = true;
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				if(afterNewlineElement(e.getChild(i)) && !first){
					insertionString += "\n";
					startOffset++;
				}
				t.brailleList.add(new BrailleMapElement(startOffset, startOffset + e.getChild(i).getValue().length(),e.getChild(i)));
				startOffset += e.getChild(i).getValue().length();
				insertionString += t.brailleList.getLast().n.getValue();
				first =false;
			}
		}	
			
		logger.log(Level.INFO, "New Braille Node Value:\n" + insertionString);
		message.put("newBrailleText", insertionString);
		message.put("newBrailleLength", insertionString.length());
		return total;
	}
	
	private int insertEmptyBrailleNode(TextMapElement t, int offset, Message message){
			int startOffset = -1;	
			Element e = new Element("brl", this.doc.getRootElement().getNamespaceURI());
			Text textNode = new Text("");
			e.appendChild(textNode);
			
			int total = 0;
			String logString = "";
			
			if(t.brailleList.size() > 0){
				startOffset = t.brailleList.getFirst().start;
			
				boolean first = true;
				for(int i = 0; i < t.brailleList.size(); i++){
					String text = t.brailleList.get(i).n.getValue();
					total += t.brailleList.get(i).n.getValue().length();
					logString += t.brailleList.get(i).n.getValue() + "\n";
					if(afterNewlineElement(t.brailleList.get(i).n) && !first){
						total++;
					}
					first = false;
				}
			}
			logger.log(Level.INFO, "Original Braille Node Value:\n" + logString);
			
			Element parent = (Element)t.n.getParent();
			if(t.brailleList.size() > 0){
				Element child = (Element)t.brailleList.getFirst().n.getParent();
				while(!child.getParent().equals(parent)){
					child = (Element)child.getParent();
				};
				parent.replaceChild(child, e);	
			}
			else {
				t.n.getParent().appendChild(e);
			}
		
			t.brailleList.clear();
			t.brailleList.add(new BrailleMapElement(startOffset, startOffset + textNode.getValue().length(),textNode));
			logger.log(Level.INFO, "New Braille Node Value:\n" + textNode.getValue());
			message.put("newBrailleText", textNode.getValue());
			message.put("newBrailleLength", textNode.getValue().length());
			return total;
	}
	
	private void insertBrailleNode(TextMapElement t, int startingOffset, String text){
		Document d = getStringTranslation(t, text);
		Element e = d.getRootElement().getChildElements("brl").get(0);
		
		d.getRootElement().removeChild(e);
		t.n.getParent().appendChild(e);
		
		int newOffset = startingOffset;
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				t.brailleList.add(new BrailleMapElement(newOffset, newOffset + e.getChild(i).getValue().length(),e.getChild(i)));
				newOffset += e.getChild(i).getValue().length() + 1;
			}
		}
	}
	
	private void addNamespace(Element e){
		e.setNamespaceURI(this.doc.getRootElement().getNamespaceURI());
		
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			addNamespace(els.get(i));
		}
	}
	
	public Element getRootElement(){
		return doc.getRootElement();
	}
	
	public Document getStringTranslation(TextMapElement t, String text){
		Element parent = (Element)t.n.getParent();
		while(!parent.getAttributeValue("semantics").contains("style")){
			if(parent.getAttributeValue("semantics").equals("action,italicx")){
				text = "<em>" + text + "</em>";
				break;
			}
			else if(parent.getAttributeValue("semantics").equals("action,boldx")){
				text = "<strong>" + text + "</strong>";
				break;
			}
			parent = (Element)parent.getParent();
		}
		String xml = getXMLString(text);
		return getXML(xml);
	}

	private Document getXML(String xml){
		byte [] outbuf = new byte[xml.length() * 10];
		
		int total = translateString(xml, outbuf);   
		if( total != -1){
			xml = "";
			for(int i = 0; i < total; i++)
				xml += (char)outbuf[i];
			
			StringReader sr = new StringReader(xml);
			Builder builder = new Builder();
			try {
				return builder.build(sr);
				
			} catch (ParsingException e) {
				e.printStackTrace();
				return null;
			}
			catch(IOException e){
				e.printStackTrace();
				return null;
			}	
		}
		return null;
	}
	
	private String getXMLString(String text){
		text = text.replace("\n", "");
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><book><string>" + text + "</string></book>";
	}
	
	private int translateString(String text, byte[] outbuffer) {
		String logFile = BBIni.getLogFilesPath() + BBIni.getFileSep() + BBIni.getInstanceID() + BBIni.getFileSep() + "liblouisutdml.log";	
		String preferenceFile = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + BBIni.getDefaultConfigFile());
		
		byte[] inbuffer;
		try {
			inbuffer = text.getBytes("UTF-8");
			int [] outlength = new int[1];
			outlength[0] = text.length() * 10;
			
			if(lutdml.translateString(preferenceFile, inbuffer, outbuffer, outlength, logFile, "formatFor utd\n mode notUC\n printPages no\n", 0)){
				return outlength[0];
			}
			else {
				System.out.println("An error occurred while translating");
				return -1;
			}
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return -1;
		}	
	}
	
	public Document getNewXML(){
		Document d = new Document(this.doc);
		removeAllBraille(d.getRootElement());
		return d;
	}
	
	private void removeAllBraille(Element e){
		Elements els = e.getChildElements();
		
		if(e instanceof Element && e.getLocalName().equals("meta")){
			if(e.getAttributeValue("name").equals("utd"))
				e.getParent().removeChild(e);
			else {
				Attribute attr = e.getAttribute("semantics");
				e.removeAttribute(attr);
			}
				
		}
		else if(e instanceof Element && e.getAttribute("semantics") != null){
			Attribute attr = e.getAttribute("semantics");
			e.removeAttribute(attr);
		}
		
		for(int i = 0; i < els.size(); i++){
			if(els.get(i).getLocalName().equals("brl")){
				e.removeChild(els.get(i));
			}
			else {
				removeAllBraille(els.get(i));
			}
		}
	}
	
	private void removeNode(TextMapElement t, Message message){
		if(hasNonBrailleChildren((Element)t.n.getParent())){
			Element e = (Element)t.brailleList.getFirst().n.getParent();
			t.n.getParent().removeChild(e);
			t.n.getParent().removeChild(t.n);
		}
		else {
			Element parent = (Element)t.n.getParent();
			while(!parent.getAttributeValue("semantics").contains("style")){
				if(((Element)parent.getParent()).getChildElements().size() <= 1){
					parent = (Element)parent.getParent();
				}
				else
					break;
			}
			
			message.put("element", parent);
			parent.getParent().removeChild(parent);
		}
	}
	
	private boolean hasNonBrailleChildren(Element e){
		Elements els = e.getChildElements();
		for(int i = 0; i <els.size(); i++){
			if(!els.get(i).getLocalName().equals("brl")){
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isWhitespace(String text){
		if (text.trim().length() > 0) 
			return false;
		
		return true;
	}
	
	public Document getDOM(){
		return doc;
	}
	
	private boolean afterNewlineElement(Node n){
		Element parent = (Element)n.getParent();
		int index = parent.indexOf(n);
		if(parent.indexOf(n) > 0){
			if(parent.getChild(index - 1) instanceof Element && ((Element)parent.getChild(index - 1)).getLocalName().equals("newline")){
				return true;
			}
		}
		
		return false;
	}
	
	private void removeBraillePageNumber(){
		Elements e = this.doc.getRootElement().getChildElements();
		
		for(int i = 0; i < e.size(); i++){
			if(e.get(i).getAttributeValue("semantics").equals("style,document")){
				Elements els = e.get(i).getChildElements();
				for(int j = 0; j < els.size(); j++){
					if(els.get(j).getLocalName().equals("brl")){
						int index = e.get(i).indexOf(els.get(j));
						e.get(i).removeChild(index);
					}
				}
			}
		}
	}
	
	public boolean createBrlFile(String filePath){		
		Document temp = getNewXML();
		String inFile = createTempFile(temp);
		String config = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + BBIni.getDefaultConfigFile());
		String logFile = BBIni.getTempFilesPath() + fileSep + "liblouisutdml.log";
		
		
		if(inFile.equals(""))
			return false;
		 
		boolean result = lutdml.translateFile (config, inFile, filePath, logFile, "formatFor brf\n", 0);
		deleteTempFile(inFile);
		return result;
	}
	
	private String createTempFile(Document newDoc){
		String filePath = BBIni.getTempFilesPath() + BBIni.getFileSep() + "tempXML.xml";
		FileOutputStream os;
		try {
			os = new FileOutputStream(filePath);
			Serializer serializer;
			serializer = new Serializer(os, "UTF-8"); 
			serializer.write(newDoc);
			os.close();
			return filePath;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return "";
	}
	
	private void deleteTempFile(String filePath){
		File f = new File(filePath);
		f.delete();
	}
	
	private void calculateDifference(String oldString, String newString, Message m){
		String [] tokens1 = oldString.split(" ");
		String [] tokens2 = newString.split(" ");
		
		int diff = tokens2.length - tokens1.length;
		if(newString.equals("")){
			diff = 0 - tokens1.length;
		}
		
		m.put("diff", diff);
	}
	
	public boolean checkAttributeValue(Element e, String attribute, String value){
		try {
			if(e.getAttributeValue(attribute).equals(value))
					return true;	
			else
				return false;
		}
		catch(Exception ex){
			return false;
		}
	}
	
	public void checkSemantics(Element e){
		if(e.getAttributeValue("semantics") == null){
			//Notify errorMessage = new Notify("No semantic attribute exists for element \"" + e.getLocalName() + "\". Please consider editing the configuration files.");
			Attribute attr = new Attribute("semantics", "style,para");
			e.addAttribute(attr);
			if(!e.getLocalName().equals("meta") && !this.missingSemanticsList.contains(e.getLocalName()))
				this.missingSemanticsList.add(e.getLocalName());
		}
	}
	
	public void notifyUser(){
		if(this.missingSemanticsList.size() > 0){
			String text = "No semantic attribute exists for the following element(s): \n";
			for(int i = 0; i < this.missingSemanticsList.size(); i++){
				text += this.missingSemanticsList.get(i) + "\n";
			}
			text += "Please check your document and consider editing the configuration files.";
			new Notify(text);
		}
	}
	
	public String getOutfile(){
		return BBIni.getTempFilesPath() + fileSep + "outFile.utd";
	}
	
	public void deleteDOM(){
		this.doc = null;
		System.gc();
	}
}
