package org.brailleblaster.perspectives.braille.document;

import java.io.IOException;
import java.io.StringReader;
//import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.elements.BrailleMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.eclipse.swt.SWT;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class BrailleDocument extends BBDocument {
	//private static final Logger log = LoggerFactory.getLogger(BrailleDocument.class);
	private int idCount = -1;
	private BBSemanticsTable table;
	
	/**Base constructor for initializing a new document
	 * @param dm :Document Manager for interacting with views
	 * @param table :Semantics table containing style information
	 */
	public BrailleDocument(Manager dm, BBSemanticsTable table) {
		super(dm);
		this.table = table;
	}

	/** Base constructor for when perspectives are switched and the XOM Document is passed to a Document specific to the view
	 * @param dm :Document Manager for interacting with views
	 * @param doc :XOM Document, the DOM already built for the currently open document
	 * @param table :Semantics table containing style information
	 */
	public BrailleDocument(Manager dm, Document doc, BBSemanticsTable table) {
		super(dm, doc);
		this.table = table;
	}
	
	/** Method via which update and remove procedures are handled
	 * @param list : Maplist of be update after DOM changes
	 * @param message : Message containing pertinent information for the given procedure
	 */
	public void updateDOM(MapList list, Message message){
		switch(message.type){
			case UPDATE:
				updateNode(list, message);
				break;
			case REMOVE_NODE:
				removeNode(list, message);
				break;
			default:
				System.out.println("No available operations for this message type");
			break;
		}
	}
	
	/** Updates a node when it has been edited
	 * @param list : Maplist of be update after DOM changes
	 * @param message : Message containing pertinent information for the given procedure
	 */
	private void updateNode(MapList list, Message message){
		String text = (String)message.getValue("newText");
		text = text.replace("\n", "").replace("\r", "");
		message.put("newText", text);
		calculateDifference(list.getCurrent().n.getValue(), text, message);
		changeTextNode(list.getCurrent().n, text);
		
		Node block = engine.findTranslationBlock(list.getCurrent().n);
		findAndRemoveBrailleElement((Element)block);
		Nodes nodes = engine.translate(block);
		block.getParent().replaceChild(block, nodes.get(0));
		message.put("Element", nodes.get(0));
	}
	
	/** Inserts an Element into the DOM.  This is used for inserting transcribers notes, paragraphs when a user hits enter, etc.
	 * @param vi : View Initializer managing segments of a DOM 
	 * @param list : maplist currently visible in the views
	 * @param current : element used to determine insertion point in DOM
	 * @param textOffset : position in text view for start and end of text element inserted
	 * @param brailleOffset : position in braille view for start and end of braille element inserted into tet element 
	 * @param index : insertion index in maplist
	 * @param elem : Name of element to insert
	 */
	public void insertElement(ViewInitializer vi, MapList list, TextMapElement current, int textOffset, int brailleOffset, int index,String elem){
		String type = this.semHandler.getDefault(elem);
		Element p = makeElement(elem, "semantics", "style," + type);
		//Add new attribute for epub aside and for nimas prodnote
		if ((elem.equalsIgnoreCase("prodnote") )||( elem.equalsIgnoreCase("aside"))){
			p.addAttribute(new Attribute("render", "optional"));
			p.addAttribute(new Attribute("showin", "bxx"));
			p.addAttribute(new Attribute("class", "utd-trnote"));
		}
	
		p.appendChild(new Text(""));
		
		Element parent = current.parentElement();
		int nodeIndex = 0;
		if(table.getSemanticTypeFromAttribute(parent).equals("style")){
			parent = (Element)parent.getParent();
			nodeIndex = parent.indexOf(current.parentElement());
		}
		else {
			while(table.getSemanticTypeFromAttribute(parent).equals("action")){
				nodeIndex = parent.getParent().indexOf(parent);
				parent = (Element)parent.getParent();
			}
			nodeIndex = parent.getParent().indexOf(parent);
			parent = (Element)parent.getParent();
		}
		
		parent.insertChild(p, nodeIndex + 1);
		
		Element brl = appendBRLNode(p);
		TextMapElement t = new TextMapElement(textOffset, textOffset, p.getChild(0));
		t.brailleList.add(new BrailleMapElement(brailleOffset, brailleOffset, brl.getChild(0)));
		vi.addElementToSection(list, t, index);
	}
	
	private Element appendBRLNode(Element e){
		Element brl = new Element("brl");
		brl.appendChild(new Text(""));
		e.appendChild(brl);
		addNamespace(brl);
		
		return brl;
	}
	
	/** Updates the text of a given text node prior to translation
	 * @param n : Text node to update
	 * @param text : String containing new text for the node
	 */
	private void changeTextNode(Node n, String text){
		Text temp = (Text)n;
		temp.setValue(text);
	}
	
	/** Handles special cases when a node is updated and contains no text or is all spaces.
	 * @param t : TextMap to insert braille node.
	 * @param offset : Next braille offset
	 * @param message : message to contain offset information after update
	 * @return : returns the original length prior to update
	 */
	public void insertEmptyBrailleNode(Element parent, LinkedList<Integer>nodeIndexes){
		Element e = new Element("brl", this.doc.getRootElement().getNamespaceURI());
		Text textNode = new Text("");
		e.appendChild(textNode);		

 		while(nodeIndexes.size() > 1)
     		parent = (Element)e.getChild(nodeIndexes.remove());
 		
 		parent.insertChild(e, nodeIndexes.remove() + 1);
	}
	
	private void removeNode(MapList list, Message message){
		int index = (Integer)message.getValue("index");
		if(list.get(index).isMathML())
			removeMathML(list.get(index), message);
		else if(message.contains("element"))
			removeElement(message);
		else
			removeNode(list.get((Integer)message.getValue("index")));
	}
	
	private void removeElement(Message m){
		Element e = (Element)m.getValue("element");
		e.getParent().removeChild(e);
	}
	
	/** Removes a node from the DOM, checks whether other children exists, if not the entire element is removed
	 * @param t : TextMapElement containing node to remove
	 * @param message : message to put element information
	 */
	private void removeNode(TextMapElement t){
		Element e = (Element)t.brailleList.getFirst().n.getParent();
		t.parentElement().removeChild(e);
		t.parentElement().removeChild(t.n);
	}
	
	/** Removes MathML from DOM
	 * @param t : TextMapElement to remove
	 * @param m : message to contain offset information
	 */
	private void removeMathML(TextMapElement t, Message m){
		int length = t.brailleList.getLast().end - t.brailleList.getFirst().start;
		Nodes nodes = (Nodes)m.getValue("nodes");
		Element parent = (Element)t.parentElement();
		
		while(nodes.size() > 0)
			parent.removeChild(nodes.remove(0));
		
		if(parent.getChildElements().size() == 0)
			parent.getParent().removeChild(parent);
				
		m.put("newBrailleText", "");
		m.put("newBrailleLength", 0);
		m.put("brailleLength", length);
		m.put("diff", 0);
	}
	
	/** Splits an element in the DOM into two elements with the same tag
	 * @param list 
	 * @param t : TextMapElement to split
	 * @param m :message  containing split information
	 * @return : returns an arraylist containing both new elements
	 */
	public ArrayList<Element> splitElement(MapList list,TextMapElement t, Message m){
		ElementDivider divider = new ElementDivider(this, table, semHandler);
		if(m.getValue("atEnd").equals(true)){
			ArrayList<TextMapElement>elList = new ArrayList<TextMapElement>();
			elList.add(list.getCurrent());
			elList.add(list.get(list.getCurrentIndex() + 1));
			return divider.split(elList, m);
		}
		else if(m.getValue("atStart").equals(true)){
			ArrayList<TextMapElement>elList = new ArrayList<TextMapElement>();
			elList.add(list.get(list.getCurrentIndex() - 1));
			elList.add(list.getCurrent());
			return divider.split(elList, m);
		}
		else
			return divider.split(t, m);
	}
	
	/** Translates a string into braille
	 * @param t : TextMapElement used to determine markup information
	 * @param text : Text to translate
	 * @return returns a XOM document since LiblouisUTDMLs translateString returns a full XML document
	 */
	public Document getStringTranslation(TextMapElement t, String text){
		Element parent = t.parentElement();
		while(!parent.getAttributeValue("semantics").contains("style")){
			if(parent.getAttributeValue("semantics").equals("action,italicx")){
				if(attributeExists(parent, "id"))
					text = "<" + parent.getLocalName() + " id=\"" + parent.getAttributeValue("id")  + "\">" + text + "</" + parent.getLocalName() + ">";
				else
					text = "<" + parent.getLocalName() + ">" + text + "</" + parent.getLocalName() + ">";
			}
			else if(parent.getAttributeValue("semantics").equals("action,boldx")){
				if(attributeExists(parent, "id"))
					text = "<" + parent.getLocalName() + " id=\"" + parent.getAttributeValue("id")  + "\">" + text + "</" + parent.getLocalName() + ">";
				else
					text = "<" + parent.getLocalName() + ">" + text + "</" + parent.getLocalName() + ">";
			}
			else if(parent.getAttributeValue("semantics").equals("action,underlinex")){
				if(attributeExists(parent, "id"))
					text = "<" + parent.getLocalName() + " id=\"" + parent.getAttributeValue("id")  + "\">" + text + "</" + parent.getLocalName() + ">";
				else
					text = "<" + parent.getLocalName() + ">" + text + "</" + parent.getLocalName() + ">";
			}
			parent = (Element)parent.getParent();
		}
		
		text = "<" + parent.getLocalName() + ">" + text + "</" + parent.getLocalName() + ">";
		
		String xml = getXMLString(text);
		return getXML(xml);
	}
	
	/** Helper method of translatString that call liblouisutdml's translateString function and builds the document
	 * @param xml : String in the form of fully formed xml document
	 * @return XOM document if translation and parse was successful, null if failed
	 */
	private Document getXML(String xml){
		byte [] outbuf = new byte[xml.length() * 10];
		
		int total = translateString(xml, outbuf);   
		if( total != -1){
			xml = new String(outbuf, Charset.forName("UTF-8")).trim();
			
			StringReader sr = new StringReader(xml);
			Builder builder = new Builder();
			try {
				return builder.build(sr);			
			} 
			catch (ParsingException e) {
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
	
	/** Helper method of translate string that encapsulates string in xml body
	 * @param text : String for translation now in markup form
	 * @return string now in full xml document form
	 */
	private String getXMLString(String text){
		text = text.replace("\n", "");
		if(dm.getCurrentConfig().equals("epub.cfg"))
			return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><body>" + text + "</body>";
		else
			return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><book>" + text + "</book>";
	}
	
	/** Calls liblouisutdml translate string
	 * @param text : String in fully formed xml to translate
	 * @param outbuffer : LiblouisUTDML requires the size of the outbuffer before translating.  
	 * @return size of string, liblouisutdml returns -1 if it failed to translate
	 */
	private int translateString(String text, byte[] outbuffer) {
	//	String logFile = BBIni.getLogFilesPath() + BBIni.getFileSep() + BBIni.getInstanceID() + BBIni.getFileSep() + "liblouisutdml.log";	
	//	String preferenceFile = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + dm.getCurrentConfig());
		
//		byte[] inbuffer;
//		try {
//			inbuffer = text.getBytes("UTF-8");
//			int [] outlength = new int[1];
//			outlength[0] = text.length() * 10;
			
//			String semPath;
//			if(dm.getWorkingPath() == null)
//				semPath =  BBIni.getTempFilesPath() + BBIni.getFileSep() + "outFile.utd";
//			else 
//				semPath = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(dm.getWorkingPath()) + ".xml";
			
//			String configSettings = "formatFor utd\n mode notUC\n printPages no\n" + semHandler.getSemanticsConfigSetting(semPath);
//			if(lutdml.translateString(preferenceFile, inbuffer, outbuffer, outlength, logFile, configSettings + sm.getSettings(), 0)){
//				return outlength[0];
//			}
//			else {
//				System.out.println("An error occurred while translating");
//				return -1;
//			}
//			log.debug("TODO: Attempting to translate with libutdml, use UTD", new RuntimeException());
//			return -1;
//		} 
//		catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			logger.error("Unsupported Encoding Exception", e);
			return -1;
//		}	
	}
	
	/** Translates text contents of an element and children
	 * @param e : Element to translate
	 * @return translated element
	 */
	public Element translateElement(Element e){
		removeBraille(e);
		//removeSemantics(e);
		//Document d;
		
		//String xml = getXMLString(e.toXML().toString());
		//d = getXML(xml);
		//Element parent = d.getRootElement();
		Nodes nodes = engine.translate(e);
		Element transElement =  (Element)nodes.get(0);
		//addNamespace(transElement);
		return transElement;
	}
	
	/** Translates a group of elements
	 * @param list: list of elements to append to a document for translation
	 * @return : document with translation
	 */
	public Document translateElements(ArrayList<Element> list){
		Document d;
		String elString = "";
		for(int i = 0; i < list.size(); i++){
			Element e = (Element) list.get(i).copy();
			removeBraille(e);
			removeSemantics(e);
			elString += e.toXML().toString();
		}
		String xml = getXMLString(elString);
		d = getXML(xml);
		return d;
	}
	
	/** Checks if an element contains other elements other than brl
	 * @param e : Element to check
	 * @return true if non-braille children exist, false if not
	 */
	public boolean hasNonBrailleChildren(Element e){
		Elements els = e.getChildElements();
		for(int i = 0; i <els.size(); i++){
			if(!els.get(i).getLocalName().equals("brl")){
				return true;
			}
		}
		
		return false;
	}
	
	/** Recursively removes braille from an element and its children
	 * @param e : Element to remove braille
	 */
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
	
	/** Recursively removes semantic attribute from an element and its children
	 * @param e : Element to remove braille
	 */
	private void removeSemantics(Element e){
		e.removeAttribute(e.getAttribute("semantics"));
		
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Element && !((Element)e.getChild(i)).getLocalName().equals("brl"))
				removeSemantics((Element)e.getChild(i));
		}
	}
	
	/** Calculates word difference following editing
	 * @param oldString : Previous string before changes
	 * @param newString : String after editing
	 * @param m : message object containing the difference for use by manager to update statusbar
	 */
	private void calculateDifference(String oldString, String newString, Message m){
		String [] tokens1 = oldString.split(" ");
		String [] tokens2 = newString.split(" ");
		
		int diff = tokens2.length - tokens1.length;
		if(newString.equals("")){
			diff = 0 - tokens1.length;
		}
		
		m.put("diff", diff);
	}
	
	/** Changes the emphasis of an element
	 * @param fontType : swt enumerationvalue representing font type
	 * @param t : TextMapElement to change style
	 */
	public void changeTextStyle(int fontType, TextMapElement t){
		Element e = (Element)t.n.getParent();
		
		if(SWT.BOLD == fontType){
			e = checkParentFontStyle(e, "boldx");
			createSemanticEntry(e, "boldx");
		}
		else if(SWT.ITALIC == fontType){
			e = checkParentFontStyle(e, "italicx");
			createSemanticEntry(e, "italicx");
		}
		else if(SWT.UNDERLINE_SINGLE == fontType){
			e =  checkParentFontStyle(e, "underlinex");
			createSemanticEntry(e, "underlinex");
		}
	}
	
	/** Helper method for changeTextStyle that finds correct element to apply emphasis change to 
	 * @param e : Element to check
	 * @param style : Style to check if element matches
	 * @return element containing same style or element with an action semantic
	 */
	private Element checkParentFontStyle(Element e, String style){
		Element parent = (Element)e.getParent();
		while(table.getSemanticTypeFromAttribute(parent).equals("action")) {
			if(table.getSemanticTypeFromAttribute(parent).equals("action")){
				String parentStyle = table.getKeyFromAttribute(parent);
				if(parentStyle.equals(style)){
					return parent;
				}
			}
			parent = (Element)parent.getParent();
		} 
		
		return e;
	}
	
	/** Sets a semantic attribute on the element
	 * @param e : element to set value on
	 * @param fontStyle : style to ad
	 * @param removalItems : items to remove from element style, part of an early implementation of emphasis
	 */
	private void createSemanticEntry(Element e, String fontStyle){
		String elementStyle = table.getKeyFromAttribute(e);
		String type = table.getSemanticTypeFromAttribute(e);
		Attribute attr = e.getAttribute("semantics");
		
		if(attr.getValue().contains(fontStyle)){
			if(type.equals("action")){
				elementStyle = "generic";
				attr.setValue("action," + elementStyle);
			}
			else {
				elementStyle = elementStyle.replace(fontStyle, "");
				attr.setValue(type + "," + elementStyle);
			}
			
		}
		else {
			if(type.equals("action")){
				elementStyle = fontStyle;
				attr.setValue("action," + elementStyle);
			}
			else {
				//elementStyle =  fontStyle + elementStyle.replace(removalItems[0], "").replace(removalItems[1], "");
				attr.setValue("style," + elementStyle);
			}
		}
		
		addSemanticEntry(e, elementStyle);
	}
	
	public void applyAction(Message m){
		Element e = (Element)m.getValue("element");
		String type = (String)m.getValue("type");
		String value = (String)m.getValue("action");
		Attribute attr = e.getAttribute("semantics");
		attr.setValue(type + "," + value);
		addSemanticEntry(e, value);
	}
	
	/** Writes entry to semantic action file for document
	 * @param e : Element for entry
	 * @param name : Semantic name
	 */
	private void addSemanticEntry(Element e, String name){
		if(attributeExists(e, "id")){
			String fileName; 
			if(dm.getWorkingPath() == null)
				fileName = "outFile";
			else
				fileName = fu.getFileName(dm.getWorkingPath());
			
			String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem";
			semHandler.writeEntry(file, name, e.getLocalName(), e.getAttributeValue("id"));
		}
		else {
			addID(e);
			String fileName; 
			if(dm.getWorkingPath() == null)
				fileName = "outFile";
			else
				fileName = fu.getFileName(dm.getWorkingPath());
			
			String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem";
			semHandler.writeEntry(file, name,  e.getLocalName(), BBIni.getInstanceID() + "_" + idCount);			
		}
	}
	
	/** Updates an entry in a document's semantic action file if an entry exists
	 * @param m : Message containing style
	 * @param e : Element to update
	 */
	public void changeSemanticAction(Message m, Element e){
		org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles style = (org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles)m.getValue("Style");
		String name = style.getName();
		Attribute attr = e.getAttribute("semantics");
		while(attr.getValue().contains("action")){
			e = (Element)e.getParent();
			attr = e.getAttribute("semantics");
		}
		attr.setValue("style," + name);
		if(attributeExists(e, "id")){
			
			String fileName;
			if(dm.getWorkingPath() == null)
				fileName = "outFile";
			else
				fileName = fu.getFileName(dm.getWorkingPath());
			
			String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem";
			semHandler.writeEntry(file, name, e.getLocalName(), e.getAttributeValue("id"));
		}
		else {
			addID(e);
			
			String fileName;
			if(dm.getWorkingPath() == null)
				fileName = "outFile";
			else
				fileName = fu.getFileName(dm.getWorkingPath());
			
			String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem";
			semHandler.writeEntry(file, name,  e.getLocalName(), BBIni.getInstanceID() + "_" + idCount);			
		}
	}
	
	/** Find parent element of a text node
	 * @param n : Node of which to find parent
	 * @param ignoreInlineElement : if true, the method finds the block element
	 * @return returns parent either inline or block element depending on boolean flag
	 */
	public Element getParent(Node n, boolean ignoreInlineElement){
		Element parent = (Element)n.getParent();
		if(ignoreInlineElement){
			parent = (Element)engine.findTranslationBlock(n);
		}
		
		return parent;
	}
	
	/** Checks if an element has emphasis, currently used because emphasis can only be toggled on/off elements in bold, em, u tags
	 * @param t : TextMapElement to check
	 * @param fontType : swt constant for emphasis
	 * @return true if element is contained in a valid tag for toggling emphasis
	 */
	public boolean hasEmphasisElement(TextMapElement t, int fontType){
		String semantic;
		if(fontType == SWT.BOLD)
			semantic = "boldx";
		else if(fontType == SWT.ITALIC)
			semantic = "italicx";
		else
			semantic = "underlinex";
		
		Element parent = t.parentElement();
		
		while(!parent.getAttributeValue("semantics").contains("style")){
			if(parent.getAttributeValue("semantics").contains(semantic) || semHandler.getDefault(parent.getLocalName()).equals(semantic))
				return true;
			
			parent = (Element)parent.getParent();
		}
		
		return false;
	}
	
	/** Helper methods for methods that update text in the DOM during text editing
	 * @param element :Element to search
	 * @return BRL element if found, null if it does not exist
	 */
	private Element findAndRemoveBrailleElement(Element element){
		Element parent = element;
		
		while(!(parent.getChild(0) instanceof Text) && parent.getChildCount() > 0)
			parent = (Element)parent.getChild(0);
			
		if(parent.getChild(0) instanceof Text){
			for(int i = 0; i < parent.getChildCount(); i++){
				Element brl;
				if(parent.getChild(i) instanceof Element && ((Element)parent.getChild(i)).getLocalName().equals("brl")){
					brl = (Element)parent.getChild(i);
					addNamespace(brl);
					parent.removeChild(brl);
					return brl;
				}
			}	
		}
	
		return null;
	}
	
	/**
	 * @param parents :An arraylist containing elements to be enclosed within a new element, typically an aside or sidebar
	 * @param type :string defining the semantic attribute to apply, typically "boxline"
	 * @return The element newly inserted into the DOM, that encloses elements passed in the arraylist, null if invalid list of elements is passed
	 */
	public Element wrapElement(ArrayList<Element>parents, String type){
		Element boxline = new Element(semHandler.getElementBySemantic(type));	
		
		Element grandparent = (Element)parents.get(0).getParent();
		int grandParentIndex = grandparent.indexOf(parents.get(0));
		
		if(type.equals("boxline")){		
			boxline.addAttribute(new Attribute("semantics","style,boxline"));
			for(int i = 0; i < parents.size(); i++){	
				Element parent = (Element)parents.get(i).getParent();
				
				if(!parents.contains(parent))
					boxline.appendChild(parent.removeChild(parents.get(i)));
			}
			
			grandparent.insertChild(boxline, grandParentIndex);
			addNamespace(boxline);
			return boxline;
		}
		
		return null;
	}
	
	public Element mergeElements(Element originalParent, Element child){
		Element parent = (Element)originalParent.copy();
		removeBraille(parent);
		removeBraille(child);
		
		while(child.getChildCount() > 0){
			if(parent.getChild(parent.getChildCount() - 1) instanceof Text && child.getChild(0) instanceof Text){
				((Text)parent.getChild(parent.getChildCount() - 1)).setValue(parent.getChild(parent.getChildCount() - 1).getValue() + child.getChild(0).getValue());
				child.removeChild(0);
			}
			else if(parent.getChild(parent.getChildCount() - 1) instanceof Element && child.getChild(0) instanceof Element){
				Element e1 = (Element)parent.getChild(parent.getChildCount() - 1);
				Element e2 = (Element)child.getChild(0);
				boolean merged = false;
				if( table.getSemanticTypeFromAttribute(e1).equals("action") && table.getSemanticTypeFromAttribute(e1).equals("action")){
					if(table.getKeyFromAttribute(e1).equals(table.getKeyFromAttribute(e2))){
						if(e1.getChild(0) instanceof Text && e2.getChild(0) instanceof Text){
							((Text)e1.getChild(0)).setValue(e1.getChild(0).getValue() + e2.getChild(0).getValue());
							Elements els = e2.getChildElements();
							for(int i = 0; i < els.size(); i++)
								e1.appendChild(e2.removeChild(els.get(0)));
							
							e2.getParent().removeChild(e2);
							merged = true;
						}
					}
				}
				
				if(!merged)
					parent.appendChild(child.removeChild(0));
			}
			else
				parent.appendChild(child.removeChild(0));
		}
		
		child.getParent().removeChild(child);
		Element mergedElement = translateElement(parent);
	
		originalParent.getParent().replaceChild(originalParent, mergedElement);
		
		return mergedElement;
	}

	public void addTPage(Element tPageRoot){
		addNamespace(tPageRoot);
		Element fmNode = findFrontMatter(doc.getRootElement());
		
		if(fmNode!=null){
			fmNode.insertChild(tPageRoot,0);
		}		
	}
	
	public void editTPage(Element newTPage, Element prevTPage){
		addNamespace(newTPage);
		Element fmNode = findFrontMatter(doc.getRootElement());
		
		if(fmNode!=null){
			fmNode.removeChild(prevTPage);
			fmNode.insertChild(newTPage, 0);
		}
	}
	
	private Element findFrontMatter(Element parent){
		Element returnElement = null;
		Elements children = parent.getChildElements();
		for(int i = 0; i < children.size(); i++){
			if(children.get(i).getLocalName().equalsIgnoreCase("frontmatter")){
				returnElement = children.get(i);
				break;
			} else {
				returnElement = findFrontMatter(children.get(i));
			}
		}
		return returnElement;
	}
	
	public void addID(Element e){
		idCount++;
		e.addAttribute(new Attribute("id", BBIni.getInstanceID() + "_" + idCount));
	}
}
