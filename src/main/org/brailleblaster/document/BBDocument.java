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
  * this program; see the file LICENSE.txt
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.document;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.settings.SettingsManager;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.brailleblaster.utd.config.XMLConfigHandler;
import org.brailleblaster.util.CheckLiblouisutdmlLog;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.liblouis.LibLouisUTDML;


public class BBDocument {
	private static final Map<FileTypes, List<String>> SUPPORTED_FILE_TYPES;
	static {
		Map<FileTypes, List<String>> temp = new EnumMap<FileTypes, List<String>>(FileTypes.class);
		temp.put(FileTypes.XML, Arrays.asList("xml", "xht", "xhtm", "xhtml", "htm", "html"));
		temp.put(FileTypes.TXT, Arrays.asList("txt"));
		temp.put(FileTypes.BRF, Arrays.asList("brf"));
		temp.put(FileTypes.UTD, Arrays.asList("utd"));
		SUPPORTED_FILE_TYPES = Collections.unmodifiableMap(temp);
	}
	protected Controller dm;
	protected Document doc;
	private static String fileSep = BBIni.getFileSep();
	protected LibLouisUTDML lutdml = LibLouisUTDML.getInstance();
	protected FileUtils fu = new FileUtils();
	protected static Logger logger = LoggerFactory.getLogger(BBDocument.class);
	private ArrayList<String>missingSemanticsList;
	private ArrayList<String>mistranslationList;
	private String systemId;
	private String publicId;
	protected SettingsManager sm;
	protected SemanticFileHandler semHandler;
	protected LocaleHandler lh;
	protected final UTDTranslationEngine engine;
	protected final XMLConfigHandler utdConfig;
	
	//Style TODO: Once this class is unit tested absorb the other constructors
	private BBDocument(Controller dm, boolean unused) {
		this.dm = dm;
		
		try { 
			//Style TODO: Somehow automagically load the correct config
			utdConfig = new XMLConfigHandler();
			engine = utdConfig.loadEngine(new File("utd-config/utdengine.xml"));
			engine.setStyleDefinitions(utdConfig.loadStyleDefinitions(new File("utd-config/styleDefs.xml")));
			utdConfig.loadMappings(engine, new File("utd-config"), "nimas");
		} catch(Exception e) {
			throw new RuntimeException("Could not initialize UTD", e);
		}
	}
	
	/** Base constructor for initializing a new document
	 * @param dm: Document Manager for relaying information between DOM and view
	 */
	public BBDocument(Controller dm){		
		this(dm, true);
		lh = new LocaleHandler();
		missingSemanticsList = new ArrayList<String>();
		mistranslationList = new ArrayList<String>();
		semHandler = new SemanticFileHandler(dm.getCurrentConfig());
		sm = new SettingsManager(dm.getCurrentConfig());
		engine.getBrailleSettings().setMainTranslationTable(BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouis" + BBIni.getFileSep() + "tables" + BBIni.getFileSep() +  "en-us-g2.ctb");
		engine.getBrailleSettings().setUseAsciiBraille(true);
	}
	
	/** Base constructor for when perspectives are switched and the XOM Document is passed to a Document specific to the view
	 * @param dm: Document Manager for relaying information between DOM and view
	 * @param doc: XOM Document, the DOM already built for the currently open document
	 */
	public BBDocument(Controller dm, Document doc){
		this(dm, true);
		this.doc = doc;
		lh = new LocaleHandler();
		missingSemanticsList = new ArrayList<String>();
		mistranslationList = new ArrayList<String>();
		semHandler = new SemanticFileHandler(dm.getCurrentConfig());
		sm = new SettingsManager(dm.getCurrentConfig());
	}
	
	/** Public method for beginning the translation process of a file
	 * @param completePath: Path to input file
	 * @param configFile: Path to configuration file to be used
	 * @param configSettings: A string containing the document settings, can be set to null, in which case values from configuration file is used
	 * @return: Returns a boolean value representing whether a document was successfully translated
	 * @throws Exception
	 */
	public boolean startDocument (String completePath, String configFile, String configSettings) throws Exception {
		return setupFromFile (completePath, configFile, configSettings);
	}
	
	/** Private method containing translation implementation
	 * @param completePath: Path to input file
	 * @param configFile:  Path to configuration file to be used
	 * @param configSettings: A string containing the document settings, can be set to null, in which case values from configuration file is used
	 * @return Returns a boolean value representing whether a document was successfully translated
	 * @throws Exception
	 */
	private boolean setupFromFile (String completePath, String configFile, String configSettings) throws Exception {
		//String configFileWithPath = "temp";
				//String configWithUTD;
				
				// Use the default; we don't have a local version.
				//configFileWithPath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile);
				
				//if (configSettings == null) 
				//	configWithUTD = "formatFor utd\n mode notUC\n printPages yes\n";
				//else 
				//	configWithUTD = configSettings + "formatFor utd\n mode notUC\n printPages yes\n";
				
				//if(dm.getWorkingPath() != null)
				//	configWithUTD += semHandler.getSemanticsConfigSetting(completePath);			
				//else 
				//	configFileWithPath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + "nimas.cfg");
				
				//String outFile = BBIni.getTempFilesPath() + fileSep + "outFile.utd";
				//String logFile = BBIni.getLogFilesPath() + fileSep + "liblouisutdml.log";
				int extPos = completePath.lastIndexOf (".") + 1;
				String ext = completePath.substring (extPos).toLowerCase();
				if (BBDocument.SUPPORTED_FILE_TYPES.get(FileTypes.XML).contains(ext)) {
					String tempPath = BBIni.getTempFilesPath() + completePath.substring(completePath.lastIndexOf(BBIni.getFileSep()), completePath.lastIndexOf(".")) + "_temp.xml";
					if( normalizeFile(completePath, tempPath) && buildDOM(tempPath)){
						Document result = engine.translateAndFormatDocument(doc);	
						if( result != null ) {
							doc = result;
							deleteFile(tempPath);
							return true;
						}
						else {
			//				new CheckLiblouisutdmlLog().displayLog();
							return false;
						}
					}
				} 
				else if (BBDocument.SUPPORTED_FILE_TYPES.get(FileTypes.TXT).contains(ext)) {
				//	if(lutdml.translateTextFile (configFileWithPath, completePath, outFile, logFile, configWithUTD + sm.getSettings(), 0))
				//		return buildDOM(outFile);
				} 
				else if (BBDocument.SUPPORTED_FILE_TYPES.get(FileTypes.BRF).contains(ext)) {
				//	if(lutdml.backTranslateFile (configFileWithPath, completePath, outFile, logFile, configWithUTD + sm.getSettings(), 0))
				// buildDOM(outFile);
				} 
				else if (BBDocument.SUPPORTED_FILE_TYPES.get(FileTypes.UTD).contains(ext)) {
					String tempPath = BBIni.getTempFilesPath() + completePath.substring(completePath.lastIndexOf(BBIni.getFileSep()), completePath.lastIndexOf(".")) + "_temp.utd";
					normalizeFile(completePath, tempPath);
					return buildDOM(tempPath);
				} 
				else {
					throw new IllegalArgumentException (completePath + " not .xml, .txt, or .brf");
				}
			//	new CheckLiblouisutdmlLog().displayLog();
				
				return false;
	}
	
	private String configureConfigurationFiles(String completPath, String configFile){
		String configPath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile);
		String documentConfig = fu.getPath(completPath) + BBIni.getFileSep() + fu.getFileName(completPath) + ".cfg";
		if(fu.exists(documentConfig))
			configPath += "," + documentConfig;
		
		return configPath;
	}
	
	/**
	 * @param fileName:  File to be used to build the DOM, typically a UTD, 
	 * called by setupfromfile method after translation is successful 
	 * @return: a Boolean value representing whether the DOM was successfully built
	 * @throws Exception
	 */
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
			new Notify(lh.localValue("connectionError"));
			e.printStackTrace();
			logger.error("Connections Error", e);
			return false;
		}
		catch(UnknownHostException e){
			new Notify(lh.localValue("connectionError"));
			e.printStackTrace();
			logger.error("Unknown Host Error", e);
			return false;
		}
		catch (ParsingException e) {
			new Notify(lh.localValue("processingProb") + fileName + "\n" + lh.localValue("seeTrace"));
			new CheckLiblouisutdmlLog().displayLog();
			e.printStackTrace();
			logger.error("Parse Error", e);
			return false;
		} 
		catch (IOException e) {
			new Notify (lh.localValue("ioProb") + fileName + "\n" + lh.localValue("seeTrace"));
			e.printStackTrace();
			logger.error("IO Error", e);
			return false;
		}
	}
	
	/** Called by setup from file prior to translation.  This method removes empty text nodes containing spaces or tabs
	 * since liblouisutdml has translation issues with nodes full of spaces. This method essentially removes pretty print formatting
	 * and creates a temp file for translation
	 * @param originalFilePath: File path of document to be translated
	 * @param tempFilePath: path to temp file created in temp folder post normalization and used by liblouisutdml
	 * @return a Boolean value representing whether the DOM was successfully built and normalized
	 */
	private boolean normalizeFile(String originalFilePath, String tempFilePath){
		Normalizer n = new Normalizer(this, originalFilePath);
		return n.createNewNormalizedFile(tempFilePath);
	}
	
	/** Creates an element and adds document namespace
	 * @param name: Element name
	 * @param attribute: attribute key: typically of type semantic, but can be others
	 * @param value: value of attribute typically a semantic-action value
	 * @return: Element created
	 */
	public Element makeElement(String name, String attribute, String value){
		Element e = new Element(name);
		addNamespace(e);
		if(attribute != null){
			e.addAttribute(new Attribute(attribute, value));
		}
		return e;
	}
	
	/**Adds the document namespace to the element being inserted into the DOM.
	 * Elements created and inserted into the XOM document do not have an initial namespace
	 * and may be skipped by the XOM api in certain cases.
	 * @param e: Element to which it and all child elements will have the document uri added
	 */
	protected void addNamespace(Element e){
		e.setNamespaceURI(doc.getRootElement().getNamespaceURI());
		
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			addNamespace(els.get(i));
		}
	}
	
	/** Gets the root element of the DOM
	 * @return Root element of DOM
	 */
	public Element getRootElement(){
		return doc.getRootElement();
	}
	
	/** Creates a DOM containing no brl nodes, used for saving an xml file 
	 * and when views are refreshed and the document is re-translated in its entirity
	 * @return: A XOM DOcument containing no brl nodes
	 */
	public Document getNewXML(){
		Document d = new Document(this.doc);
		setOriginalDocType(d);
		removeAllBraille(d.getRootElement());
		return d;
	}
	
	/** recursive method that strips brl nodes, helper method used by getNewXML method
	 * @param e: Element which braille will be removed
	 */
	private void removeAllBraille(Element e){
		Elements els = e.getChildElements();
		
		if(e instanceof Element && e.getLocalName().equals("meta")){
			if(attributeExists(e, "name") && e.getAttributeValue("name").equals("utd"))
				e.getParent().removeChild(e);
			else {
				if(attributeExists(e, "semantics")){
					Attribute attr = e.getAttribute("semantics");
					e.removeAttribute(attr);
				}
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
	
	/** Gets the XOM document, typically for either passing to another methods or file writers
	 * @return XOM Document
	 */
	public Document getDOM(){
		return doc;
	}
	
	/** LiblouisUTDML adds a brl element that functions as a control character to the end of each document.
	 *  This element does not follow the conventions of other brl elements and has proved problematic.  
	 *  This method removes that character following a translation.  
	 */
	private void removeBraillePageNumber(){
		Elements e = this.doc.getRootElement().getChildElements();
		
		for(int i = 0; i < e.size(); i++){
			if(attributeExists(e.get(i), "semantics") && e.get(i).getAttributeValue("semantics").equals("style,document")){
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
	
	/** Method used to create and output a brf file when a user saves or for display in the braille preview
	 * @param filePath: Path for the file to be output
	 * @return a boolean value representing whether the file was successfully created
	 */
	public boolean createBrlFile(String filePath){		
		Document temp = getNewXML();
		String inFile = createTempFile(temp);
		String config = configureConfigurationFiles(dm.getWorkingPath(), dm.getCurrentConfig());
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
		
		boolean result = lutdml.translateFile (config, inFile, filePath, logFile, semFile + "formatFor brf\n" + sm.getSettings(), 0);
		deleteFile(inFile);
		return result;
	}
	
	/**
	 * @param newDoc: Creates a temporary xml file used to create a brf.  LiblouisUTDML cannot create a brf
	 * from a DOM containing brl nodes, so a new temp document is created without brl nodes
	 * @return Path to temp file or empty string if it fails
	 */
	private String createTempFile(Document newDoc){
		String filePath = BBIni.getTempFilesPath() + BBIni.getFileSep() + "tempXML.xml";
		if(fu.createXMLFile(newDoc, filePath))
			return filePath;
		else	    
			return "";
	}
	
	/** Deletes a file, typically temporary files used in normalization or brf creation
	 * @param filePath
	 */
	private void deleteFile(String filePath){
		File f = new File(filePath);
		f.delete();
	}
	
	/** Checks whether an element attribute value matches a specified value
	 * @param e:Element to check
	 * @param attribute:attribute name
	 * @param value: attribute value
	 * @return true if attribute contains that value, false if attribute does not exist or value is different
	 */
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
	
	/** Checks whether an element contains a specified attribute
	 * @param e: Element to check
	 * @param attribute: String value of attribute name to check
	 * @return: true if elements contains the attribute, false if not
	 */
	public boolean attributeExists(Element e, String attribute){
		if(e.getAttribute(attribute) != null)
			return true;
		else
			return false;
	}
	
	/** Checks whether an element contains a semantics attribute.
	 * If not, then a default value of para is applied and the element name is added to the missingSemantics list
	 * to notify the developer once the initialization of the section map has occurred
	 * @param e: Element to check
	 */
	public void checkSemantics(Element e){
		if(e.getAttributeValue("semantics") == null){
			Attribute attr = new Attribute("semantics", "style,para");
			e.addAttribute(attr);
			if(!e.getLocalName().equals("meta") && !missingSemanticsList.contains(e.getLocalName()))
				missingSemanticsList.add(e.getLocalName());
		}
	}
	
	/** Used to notify developers via the console if missing semantics and mistranslated print page numbers which
	 * LibLouisUTDML refuses to translate when the braille page corresponds with a print page
	 */
	public void notifyUser(){
		if(!BBIni.debugging()){
			if(missingSemanticsList.size() > 0){
				String text = lh.localValue("missingSem");
				for(int i = 0; i < missingSemanticsList.size(); i++){
					text += missingSemanticsList.get(i) + "\n";
					System.out.println(text);
				}
				text += lh.localValue("checkConfig");
				System.out.println(text);
				//new Notify(text);
			}
		
			if(mistranslationList.size() > 0){
				String text = lh.localValue("mistransError");
				for(int i = 0; i < mistranslationList.size(); i++){
					text += mistranslationList.get(i) + "\n";
					System.out.println(text);
				}
				//new Notify(text);
			}
		}
		missingSemanticsList.clear();
		mistranslationList.clear();
	}
	
	/** Searches UTDML markup to find the print page translation within a pagenum element
	 * this is the text representation in the UTDML markup, not the braille representation 
	 * @param e: Element to search
	 * @return the text node containing the print page translation
	 */
	public Node findPrintPageNode(Element e){	
		Node n = findPrintPageNodeHelper(e);
		if (n == null) {
			mistranslationList.add(e.toXML().toString());
			return null;
		} else
			return n;		
	}
	
	/** private helper method used to search a pagenum element in UTDML markup
	 * @param e :Element to search
	 * @return the text node containing the page representation, null if not found
	 */
	private Node findPrintPageNodeHelper(Element e){
		int count = e.getChildCount();
		for(int i = 0; i < count; i++){
			if(e.getChild(i) instanceof Element && (((Element)e.getChild(i)).getLocalName().equals("span") || ((Element)e.getChild(i)).getLocalName().equals("brl"))){
				return findPrintPageNodeHelper((Element)e.getChild(i));
			}
			else if(e.getChild(i) instanceof Text && ((Element)e.getChild(i).getParent()).getLocalName().equals("span")){
				return e.getChild(i);
			}
		}
	
		return null;
	}
	
	/**Searches UTDML markup to find the print page translation within a pagenum element
	 * this is the braille representation in the UTDML markup, not the text representation 
	 * @param e: Element to search
	 * @return the text node containing the page representation, null if not found
	 */
	public Node findBraillePageNode(Element e){
		Node n = findBraillePageNodeHelper(e);
		if (n == null) {
			mistranslationList.add(e.toXML().toString());
			return null;
		} else
			return n;
		
	}
	
	/** private helper method used to search a pagenum element in UTDML markup
	 * @param e :Element to search
	 * @return The text node containing the page representation, null if not found
	 */
	private Node findBraillePageNodeHelper(Element e){
		int count = e.getChildCount();
		for(int i = 0; i < count; i++){
			if(e.getChild(i) instanceof Element && (((Element)e.getChild(i)).getLocalName().equals("span") || ((Element)e.getChild(i)).getLocalName().equals("brl"))){
				return findBraillePageNodeHelper((Element)e.getChild(i));
			}
			else if(e.getChild(i) instanceof Text && ((Element)e.getChild(i).getParent()).getLocalName().equals("brl")){
				return e.getChild(i);
			}
		}
		
		return null;
	}
	
	public Text findBoxlineTextNode(Element brl){
		int count = brl.getChildCount();
		for(int i = 0; i < count; i++){
			if(brl.getChild(i) instanceof Text)
				return (Text)brl.getChild(i);
		}
		return null;
	}
	
	/**
	 * @return the path to outfile in the temp folder containing the UTDML translation
	 */
	public String getOutfile(){
		return BBIni.getTempFilesPath() + fileSep + "outFile.utd";
	}
	
	/** Resets the object by deleting the DOM and loading a new configuration file 
	 * into internal objects such as settings manager and semantic file handler
	 * @param config Name of configuration file to load, not complete path, for example "nimas.cfg"
	 */
	public void resetBBDocument(String config){
		deleteDOM();
		sm = new SettingsManager(config);
		semHandler.resetSemanticHandler(config);
	}
	
	
	/** Sets the DOM to null, used when refreshing the views an closing tabs
	 */
	public void deleteDOM(){
		this.doc = null;
		System.gc();
	}
	
	/** stores original public id for use when saving a file and changing back from the local reference
	 * @param id: original public id
	 */
	public void setPublicId(String id){
		publicId = id;
	}
	
	/** stores original system id for use when saving a file and changing back from the local reference
	 * @param id: original system id
	 */
	public void setSystemId(String id){
		systemId = id;
	}
	
	/** The doctype containing the public and system id is changed to a local reference during normalization,
	 * it is changed back when be saved to correspond to the original document 
	 * @param d: XOM Document to be manipulated prior to saving
	 */
	public void setOriginalDocType(Document d) {
		if((publicId != null && systemId != null))
			d.setDocType(new DocType(this.getRootElement().getLocalName(), publicId, systemId));
		else if(publicId == null && systemId != null)
			d.setDocType(new DocType(this.getRootElement().getLocalName(), systemId));
	}
	
	public SemanticFileHandler getSemanticFileHandler(){
		return semHandler;
	}
	
	/** Queries the document using xpath
	 * @param query: xpath query
	 * @return NodeList cotaining query result
	 */
	public Nodes query(String query){
		XPathContext context = XPathContext.makeNamespaceContext(doc.getRootElement());
		return doc.query(query, context);
	}
	
	public SettingsManager getSettingsManager(){
		return sm;
	}
	
	public int getLinesPerPage(){
		return sm.getLinesPerPage();
	}
	
	public UTDTranslationEngine getEngine() {
		return engine;
	}
}
