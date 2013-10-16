package org.brailleblaster.perspectives.braille.document;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.BrailleMapElement;
import org.brailleblaster.perspectives.braille.mapping.MapList;
import org.brailleblaster.perspectives.braille.mapping.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;

public class BrailleDocument extends BBDocument {
	private int idCount = 0;
	private BBSemanticsTable table;
	
	public BrailleDocument(Manager dm, BBSemanticsTable table) {
		super(dm);
		this.table = table;
	}

	public BrailleDocument(Manager dm, Document doc, BBSemanticsTable table) {
		super(dm, doc);
		this.table = table;
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
		
		Element brl = new Element("brl");
		brl.appendChild(new Text(""));
		p.appendChild(brl);
		addNamespace(brl);
		list.get(index).brailleList.add(new BrailleMapElement(brailleOffset, brailleOffset, brl.getChild(0)));
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
	
	private String getXMLString(String text){
		text = text.replace("\n", "");
		if(dm.getCurrentConfig().equals("epub.cfg"))
			return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><body><string>" + text + "</string></body>";
		else
			return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><book><string>" + text + "</string></book>";
	}
	
	private int translateString(String text, byte[] outbuffer) {
		String logFile = BBIni.getLogFilesPath() + BBIni.getFileSep() + BBIni.getInstanceID() + BBIni.getFileSep() + "liblouisutdml.log";	
		String preferenceFile = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + dm.getCurrentConfig());
		
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
	
	public Element translateElement(Element e){
		removeBraille(e);
		removeSemantics(e);
		Document d;
		
		String xml = getXMLString(e.toXML().toString());
		d = getXML(xml);
		Element parent = (Element)d.getRootElement().getChild(0);
		return (Element)parent.removeChild(0);
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
				if(((Element)parent.getParent()).getChildElements().size() <= 1)
					parent = (Element)parent.getParent();
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
	
	private void calculateDifference(String oldString, String newString, Message m){
		String [] tokens1 = oldString.split(" ");
		String [] tokens2 = newString.split(" ");
		
		int diff = tokens2.length - tokens1.length;
		if(newString.equals("")){
			diff = 0 - tokens1.length;
		}
		
		m.put("diff", diff);
	}
	
	public void changeSemanticAction(Message m, Element e){
		org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles style = (org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles)m.getValue("Style");
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
	
	public Element getParent(Node n, boolean ignoreInlineElement){
		Element parent = (Element)n.getParent();
		if(ignoreInlineElement){
			while(checkAttribute(parent, "semantics") && parent.getAttribute("semantics").getValue().contains("action")){
				parent = (Element)parent.getParent();
			}
		}
		
		return parent;
	}
}
