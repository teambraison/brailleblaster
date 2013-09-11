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

package org.brailleblaster.document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Text;
import nu.xom.XPathContext;

import org.brailleblaster.BBIni;
import org.brailleblaster.mapping.BrailleMapElement;
import org.brailleblaster.mapping.MapList;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.messages.Message;
import org.brailleblaster.util.CheckLiblouisutdmlLog;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.liblouis.liblouisutdml;


public class BBDocument {
	private DocumentManager dm;
	private Document doc;
	private static String fileSep = BBIni.getFileSep();
	private liblouisutdml lutdml = liblouisutdml.getInstance();
	private FileUtils fu = new FileUtils();
	static Logger logger = BBIni.getLogger();
	private ArrayList<String>missingSemanticsList;
	private String systemId;
	private String publicId;
	private int idCount = 0;
	private BBSemanticsTable table;
	private SemanticFileHandler semHandler;
	
	public BBDocument(DocumentManager dm, BBSemanticsTable table){		
		this.dm = dm;
		this.missingSemanticsList = new ArrayList<String>();
		this.semHandler = new SemanticFileHandler(BBIni.getDefaultConfigFile());
		this.table = table;
	}
	
	public boolean createNewDocument(){
		return startNewDocument();
	}
	
	private boolean startNewDocument(){
		String template = BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "document.xml";
		String tempFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + "newDoc.xml";
		
		fu.copyFile(template, tempFile);
		
		Builder builder = new Builder();
		try {
			doc = builder.build(new File(tempFile));
			return true;
		} 
		catch (ParsingException e) {
			e.printStackTrace();
			new Notify("Framework is malformed");
			return false;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
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
			configWithUTD = "formatFor utd\n mode notUC\n printPages no\n" + semHandler.getSemanticsConfigSetting(completePath);
		} 
		else {
			configWithUTD = configSettings + "formatFor utd\n mode notUC\n printPages no\n" + semHandler.getSemanticsConfigSetting(completePath);
		}
		String outFile = BBIni.getTempFilesPath() + fileSep + "outFile.utd";
		String logFile = BBIni.getLogFilesPath() + fileSep + "liblouisutdml.log";
		int extPos = completePath.lastIndexOf (".") + 1;
		String ext = completePath.substring (extPos);
		if (ext.equalsIgnoreCase ("xml")) {
			String tempPath = BBIni.getTempFilesPath() + completePath.substring(completePath.lastIndexOf(BBIni.getFileSep()), completePath.lastIndexOf(".")) + "_temp.xml";
			if(normalizeFile(completePath, tempPath) && lutdml.translateFile (configFileWithPath, tempPath, outFile, logFile, configWithUTD, 0)){
				deleteFile(tempPath);
				return buildDOM(outFile);
			}
			else 
				return false;
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
			String tempPath = BBIni.getTempFilesPath() + completePath.substring(completePath.lastIndexOf(BBIni.getFileSep()), completePath.lastIndexOf(".")) + "_temp.utd";
			normalizeUTD(completePath, tempPath);
			return buildDOM(tempPath);
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
		catch(ConnectException e){
			new Notify("Brailleblaster failed to access necessary materials from online.  Please check your internet connection and try again.");
			e.printStackTrace();
			logger.log(Level.SEVERE, "Connections Error", e);
			return false;
		}
		catch(UnknownHostException e){
			new Notify("Brailleblaster failed to access necessary materials from online.  Please check your internet connection and try again.");
			e.printStackTrace();
			logger.log(Level.SEVERE, "Unknown Host Error", e);
			return false;
		}
		catch (ParsingException e) {
			new Notify("Problem processing " + fileName + " See stack trace.");
			new CheckLiblouisutdmlLog().displayLog();
			e.printStackTrace();
			logger.log(Level.SEVERE, "Parse Error", e);
			return false;
		} 
		catch (IOException e) {
			new Notify ("IO error occurred while parsing " + fileName + " See stack trace.");
			e.printStackTrace();
			logger.log(Level.SEVERE, "IO Error", e);
			return false;
		}
	}
	
	private boolean normalizeFile(String originalFilePath, String tempFilePath){
		Normalizer n = new Normalizer(this, originalFilePath);
		return n.createNewNormalizedFile(tempFilePath);
	}
	
	private boolean normalizeUTD(String originalFilePath, String tempFilePath){
		Normalizer n = new Normalizer(this, originalFilePath);
		return n.createNewUTDFile(tempFilePath);
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
			if(list.size() > 1 && list.getCurrentIndex() < list.size() - 1)
				insertBrailleNode(message, list.getCurrent(), list.get(list.getCurrentIndex() + 1).brailleList.getFirst().start, text);
			else {
				if(list.getCurrentIndex() > 0)
					insertBrailleNode(message, list.getCurrent(), list.get(list.getCurrentIndex() - 1).brailleList.getLast().end + 1, text);
				else
					insertBrailleNode(message, list.getCurrent(), 0, text);
			}
		}
		message.put("brailleLength", total);
	}

	public void insertEmptyTextNode(MapList list, TextMapElement current, int textOffset, int brailleOffset, int index){
		Element p = makeElement("p", "semantics", "style,para");
		p.appendChild(new Text(""));
		
		Element parent = (Element)current.parentElement().getParent();
		int nodeIndex = parent.indexOf(current.parentElement());
		parent.insertChild(p, nodeIndex + 1);
		
		list.add(index, new TextMapElement(textOffset, textOffset, p.getChild(0)));
		
	//	if(index < list.size() - 1){
		Element brl = new Element("brl");
		brl.appendChild(new Text(""));
		p.appendChild(brl);
		addNamespace(brl);
		list.get(index).brailleList.add(new BrailleMapElement(brailleOffset, brailleOffset, brl.getChild(0)));
//		}
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
		
		startOffset = t.brailleList.getFirst().start;
		String logString = "";
		
		for(int i = 0; i < t.brailleList.size(); i++){
			total += t.brailleList.get(i).end - t.brailleList.get(i).start;
			if(afterNewlineElement(t.brailleList.get(i).n) && i > 0){
				total++;
			}
			logString += t.brailleList.get(i).n.getValue() + "\n";
		}
		logger.log(Level.INFO, "Original Braille Node Value:\n" + logString);
			
		Element parent = t.parentElement();
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
					total += t.brailleList.get(i).n.getValue().length();
					logString += t.brailleList.get(i).n.getValue() + "\n";
					if(afterNewlineElement(t.brailleList.get(i).n) && !first){
						total++;
					}
					first = false;
				}
			}
			logger.log(Level.INFO, "Original Braille Node Value:\n" + logString);
			
			Element parent = t.parentElement();
			if(t.brailleList.size() > 0){
				Element child = (Element)t.brailleList.getFirst().n.getParent();
				while(!child.getParent().equals(parent)){
					child = (Element)child.getParent();
				};
				parent.replaceChild(child, e);	
			}
			else {
				t.parentElement().appendChild(e);
			}
		
			t.brailleList.clear();
			t.brailleList.add(new BrailleMapElement(startOffset, startOffset + textNode.getValue().length(),textNode));
			logger.log(Level.INFO, "New Braille Node Value:\n" + textNode.getValue());
			message.put("newBrailleText", textNode.getValue());
			message.put("newBrailleLength", textNode.getValue().length());
			return total;
	}
	
	private void insertBrailleNode(Message m, TextMapElement t, int startingOffset, String text){
		Document d = getStringTranslation(t, text);
		Element e;
		Element brlParent = ((Element)d.getRootElement().getChild(0));
		Elements els = brlParent.getChildElements();
		String insertionString = "";
		
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
		
		t.parentElement().appendChild(e);
		int newOffset = startingOffset;
		
		boolean first = true;
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				if(afterNewlineElement(e.getChild(i)) && !first){
					insertionString += "\n";
					newOffset++;
				}
				
				t.brailleList.add(new BrailleMapElement(newOffset, newOffset + e.getChild(i).getValue().length(), e.getChild(i)));
				newOffset += e.getChild(i).getValue().length();
				insertionString += t.brailleList.getLast().n.getValue();
				first = false;
			}
		}
		
		m.put("newBrailleText", insertionString);
		m.put("brailleLength", 0);
		m.put("newBrailleLength", insertionString.length());
	}
	
	public ArrayList<Element> splitElement(MapList list,TextMapElement t, Message m){
		ElementDivider divider = new ElementDivider(this, table, semHandler);
		if(m.getValue("atEnd").equals(true)){
			ArrayList<TextMapElement>elList = new ArrayList<TextMapElement>();
			elList.add(list.getCurrent());
			elList.add(list.get(list.getCurrentIndex() + 1));
			return divider.split(elList, list, m);
		}
		else if(m.getValue("atStart").equals(true)){
			ArrayList<TextMapElement>elList = new ArrayList<TextMapElement>();
			elList.add(list.get(list.getCurrentIndex() - 1));
			elList.add(list.getCurrent());
			return divider.split(elList, list, m);
		}
		else
			return divider.split(list, t, m);
	}
	
	public Element makeElement(String name, String attribute, String value){
		Element e = new Element(name);
		addNamespace(e);
		if(attribute != null){
			e.addAttribute(new Attribute(attribute, value));
		}
		return e;
	}
	
	private void addNamespace(Element e){
		e.setNamespaceURI(doc.getRootElement().getNamespaceURI());
		
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			addNamespace(els.get(i));
		}
	}
	
	public Element getRootElement(){
		return doc.getRootElement();
	}
	
	public Document getStringTranslation(TextMapElement t, String text){
		Element parent = t.parentElement();
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
			xml = new String(outbuf, Charset.forName("UTF-8")).trim();
			
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
			
			String semPath;
			if(dm.getWorkingPath() == null){
				semPath =  BBIni.getTempFilesPath() + BBIni.getFileSep() + "outFile.utd";
			}
			else {
				semPath = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(dm.getWorkingPath()) + ".xml";
			}
			String configSettings = "formatFor utd\n mode notUC\n printPages no\n" + semHandler.getSemanticsConfigSetting(semPath);
			
			if(lutdml.translateString(preferenceFile, inbuffer, outbuffer, outlength, logFile, configSettings, 0)){
				return outlength[0];
			}
			else {
				System.out.println("An error occurred while translating");
				return -1;
			}
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Unsupported Encoding Exception", e);
			return -1;
		}	
	}
	
	public Document getNewXML(){
		Document d = new Document(this.doc);
		setOriginalDocType(d);
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
		if(hasNonBrailleChildren(t.parentElement())){
			Element e = (Element)t.brailleList.getFirst().n.getParent();
			t.parentElement().removeChild(e);
			t.parentElement().removeChild(t.n);
		}
		else {
			Element parent = t.parentElement();
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
	
	private void removeBraille(Element e){
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			if(els.get(i).getLocalName().equals("brl")){
				e.removeChild(els.get(i));
			}
			else
				removeBraille(els.get(i));
		}
	}
	
	private void removeSemantics(Element e){
		e.removeAttribute(e.getAttribute("semantics"));
		
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Element && !((Element)e.getChild(i)).getLocalName().equals("brl"))
				removeSemantics((Element)e.getChild(i));
		}
	}
	
	public boolean createBrlFile(DocumentManager dm, String filePath){		
		Document temp = getNewXML();
		String inFile = createTempFile(temp);
		String config = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + BBIni.getDefaultConfigFile());
		String logFile = BBIni.getTempFilesPath() + fileSep + "liblouisutdml.log";
		String semFile = "";
		
		if(inFile.equals(""))
			return false;
		
		String fileName;
		if(dm.getWorkingPath() == null)
			fileName = "outFile.utd";
		else
			fileName = dm.getWorkingPath();
		
		if(fu.exists(BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(fileName) + ".sem")){
			semFile = "semanticFiles "+ semHandler.getDefaultSemanticsFiles() + "," + BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(fileName) + ".sem" + "\n";
		}
		
		boolean result = lutdml.translateFile (config, inFile, filePath, logFile, semFile + "formatFor brf\n", 0);
		deleteTempFile(inFile);
		return result;
	}
	
	private String createTempFile(Document newDoc){
		String filePath = BBIni.getTempFilesPath() + BBIni.getFileSep() + "tempXML.xml";
		if(fu.createXMLFile(newDoc, filePath))
			return filePath;
		else	    
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
			logger.log(Level.SEVERE, "Exception", ex);
			return false;
		}
	}
	
	public boolean checkAttribute(Element e, String attribute){
			if(e.getAttribute(attribute) != null)
				return true;
			else
				return false;
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
	
	private void deleteFile(String path){
		File f = new File(path);
		f.delete();
	}
	
	public void setPublicId(String id){
		publicId = id;
	}
	
	public void setSystemId(String id){
		systemId = id;
	}
	
	public void setOriginalDocType(Document d) {
		if(this.doc.getDocType() == null)
			d.setDocType(new DocType(this.getRootElement().getLocalName(), publicId, systemId));
	}
	
	public void changeSemanticAction(Message m, Element e){
		org.brailleblaster.document.BBSemanticsTable.Styles style = (org.brailleblaster.document.BBSemanticsTable.Styles)m.getValue("Style");
		String name = style.getName();
		//Element e = (Element)t.n.getParent();
		Attribute attr = e.getAttribute("semantics");
		while(attr.getValue().contains("action")){
			e = (Element)e.getParent();
			attr = e.getAttribute("semantics");
		}
		attr.setValue("style," + name);
		if(checkAttribute(e, "id")){
			
			String fileName;
			if(dm.getWorkingPath() == null)
				fileName = "outFile";
			else
				fileName = fu.getFileName(dm.getWorkingPath());
			
			String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem";
			semHandler.writeEntry(file, name, e.getLocalName(), e.getAttributeValue("id"));
		}
		else {
			e.addAttribute(new Attribute("id", BBIni.getInstanceID() + "_" + idCount));
			
			String fileName;
			if(dm.getWorkingPath() == null)
				fileName = "outFile";
			else
				fileName = fu.getFileName(dm.getWorkingPath());
			
			String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem";
			semHandler.writeEntry(file, name,  e.getLocalName(), BBIni.getInstanceID() + "_" + idCount);			
			idCount++;
		}
	}
	
	public SemanticFileHandler getSemanticFileHandler(){
		return semHandler;
	}
	
	public Element getParent(Node n, boolean ignoreInlineElement){
		Element parent = (Element)n.getParent();
		if(ignoreInlineElement){
			while(checkAttribute(parent, "semantics") && parent.getAttribute("semantics").getValue().contains("action")){
				parent = (Element)parent.getParent();
			}
		}
		
		return parent;
	}
	
	public Element translateElement(Element e){
		removeBraille(e);
		removeSemantics(e);
		Document d;
		
		String xml = getXMLString(e.toXML().toString());
		d = getXML(xml);
		Element parent = (Element)d.getRootElement().getChild(0);
		return (Element)parent.removeChild(0);
	}
	
	public Nodes query(String query){
		XPathContext context = XPathContext.makeNamespaceContext(doc.getRootElement());
		return doc.query(query, context);
	}
}
