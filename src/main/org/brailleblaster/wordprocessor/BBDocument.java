package org.brailleblaster.wordprocessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Comment;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ProcessingInstruction;
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
	static Logger logger = BBIni.getLogger();;
	private DocumentManager dm;
	
	public BBDocument(DocumentManager dm){		
		this.dm = dm;
	}
	
	public boolean startDocument (InputStream inputStream, String configFile, String configSettings) throws Exception {
		String fileName = "xxx";
		return buildDOM(fileName);
	}
	
	public boolean startDocument (String completePath, String configFile, String configSettings) throws Exception {
			return setupFromFile (completePath, configFile, configSettings);
	}
	
	private boolean setupFromFile (String completePath, String configFile, String configSettings) throws Exception {
		String configFileWithPath = fu.findInProgramData ("liblouisutdml" + fileSep + "lbu_files" + fileSep + configFile);
		String configWithUTD;
		if (configSettings == null) {
			configWithUTD = "formatFor utd\n mode notUC\n";
		} 
		else {
			configWithUTD = configSettings + "formatFor utd\n mode notUC\n";
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
				removeNode(list.get((Integer)message.getValue("index")));
				break;
		default:
				System.out.println("No available operations for this mesage type");
			break;
		}
	}
	
	private void updateNode(MapList list, Message message){
		int total = 0;
		String text = (String)message.getValue("newText");
		changeTextNode(list.getCurrent().n, text);
		
		if(text.equals("") || isWhitespace(text)){
			total = insertEmptyBrailleNode(list.getCurrent(), list.getNextBrailleOffset(list.getCurrentIndex()));
		}
		else if(list.getCurrent().brailleList.size() > 0){
			total = changeBrailleNodes(list.getCurrent(), text);
		}
		else {
			insertBrailleNode(list.getCurrent(), list.get(list.getCurrentIndex() + 1).brailleList.getFirst().offset, text);
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
	
	private int changeBrailleNodes(TextMapElement t, String text){
		Document d = getStringTranslation(text);
		int total = 0;
		int startOffset = 0;
		String insertionString = "";
		Element e;
		
		e = d.getRootElement().getChildElements("brl").get(0);
		addNamespace(e);
		
		d.getRootElement().removeChild(e);
		
		startOffset = t.brailleList.getFirst().offset;
		String logString = "";
		for(int i = 0; i < t.brailleList.size(); i++){
			total += t.brailleList.get(i).n.getValue().length() + 1;
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
			
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				t.brailleList.add(new BrailleMapElement(startOffset, e.getChild(i)));
				startOffset += e.getChild(i).getValue().length() + 1;
				insertionString += t.brailleList.getLast().n.getValue() + "\n";
				System.out.println("Braille value:\t" + e.getChild(i).getValue());
			}
		}	
			
		logger.log(Level.INFO, "New Braille Node Value:\n" + insertionString);
		return total;
	}
	
	private int insertEmptyBrailleNode(TextMapElement t, int offset){
			int startOffset = -1;	
			Element e = new Element("brl", this.doc.getRootElement().getNamespaceURI());
			Text textNode = new Text("");
			e.appendChild(textNode);
			
			if(t.brailleList.size() > 0)
				startOffset = t.brailleList.getFirst().offset;
			
			int total = 0;
			
			String logString = "";
			for(int i = 0; i < t.brailleList.size(); i++){
				total += t.brailleList.get(i).n.getValue().length() + 1;
				logString += t.brailleList.get(i).n.getValue() + "\n";
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
			t.brailleList.add(new BrailleMapElement(startOffset, textNode));
			logger.log(Level.INFO, "New Braille Node Value:\n" + textNode.getValue());
			return total;
	}
	
	private void insertBrailleNode(TextMapElement t, int startingOffset, String text){
		Document d = getStringTranslation(text);
		
		Element e = d.getRootElement().getChildElements("brl").get(0);
		d.getRootElement().removeChild(e);

		t.n.getParent().appendChild(e);
		
		int newOffset = startingOffset;
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				t.brailleList.add(new BrailleMapElement(newOffset, e.getChild(i)));
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
	
	public Document getStringTranslation(String text){
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
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><string>" + text + "</string>";
	}
	
	private int translateString(String text, byte[] outbuffer) {
		String logFile = BBIni.getLogFilesPath() + BBIni.getFileSep() + BBIni.getInstanceID() + BBIni.getFileSep() + "liblouisutdml.log";	
		String preferenceFile = BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + 
				BBIni.getFileSep() + "preferences.cfg";
		
		byte[] inbuffer;
		try {
			inbuffer = text.getBytes("UTF-8");
			int [] outlength = new int[1];
			outlength[0] = text.length() * 10;
			
			if(liblouisutdml.getInstance().translateString(preferenceFile, inbuffer, outbuffer, outlength, logFile, "formatFor utd\n mode notUC\n", 0)){
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
	
	private void removeNode(TextMapElement t){
		if(hasNonBrailleChildren((Element)t.n.getParent())){
			Element e = (Element)t.brailleList.getFirst().n.getParent();
			t.n.getParent().removeChild(e);
			t.n.getParent().removeChild(t.n);
		}
		else {
			Element parent = (Element)t.n.getParent();
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
}
