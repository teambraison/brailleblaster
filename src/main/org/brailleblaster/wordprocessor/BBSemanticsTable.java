package org.brailleblaster.wordprocessor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.brailleblaster.BBIni;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class BBSemanticsTable {
	public enum StylesType{
		linesBefore,
		linesAfter,
		leftMargin,
		firstLineIndent,
		skipNumberLines,
		format,
		newPageBefore,
		newPageAfter,
		righthandPage,
		braillePageNumberFormat,
		keepWithNext,
		dontSplit,
		orphanControl,
		newLineAfter,
		Font;
	}
	
	public class Styles{
		String elementName;
		HashMap<StylesType, String> map;
		
		public Styles(String elementName){
			this.elementName = elementName;
			this.map = new HashMap<StylesType, String>();
		}
		
		public void put(StylesType key, String value){	
			this.map.put(key, value);
		}
		
		public Object get(StylesType st){
			return this.map.get(st);
		}
		
		public Set<StylesType> getKeySet(){
			return this.map.keySet(); 
		}
		
		public boolean contains(StylesType key){
			return this.map.containsKey(key);
		}
		
		public String getName(){
			return this.elementName;
		}
	}
	
	Document doc;
	HashMap<String,Styles> table;
	
	public BBSemanticsTable(){
		File file = new File(BBIni.getProgramDataPath() + BBIni.getFileSep() + "styles" + BBIni.getFileSep() + "BBStyles.xml");
		Builder builder = new Builder();
		try {
			this.doc = builder.build(file);
			this.table = new HashMap<String, Styles>();
			makeHashTable(this.doc.getRootElement().getFirstChildElement("styles"));
			
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void makeHashTable(Element e){
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			makeStylesObject(els.get(i));
		}
	}
	
	private void makeStylesObject(Element e){
		Styles temp = new Styles(e.getLocalName());
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			temp.put(StylesType.valueOf(els.get(i).getLocalName()), els.get(i).getValue());
		}
		
		this.table.put(e.getLocalName(), temp);
	}
	
	public boolean containsKey(String key){
		return this.table.containsKey(key);
	}
	
	public Styles get(String key){
		return this.table.get(key);
	}
	
	public String getKeyFromAttribute(Node n){
		String pair = ((Element)n.getParent()).getAttributeValue("semantics");
		
		if(pair == null){
			return null;
		}
		else {
			String[] tokens = pair.split(",");
			return tokens[1];	
		}
	}
}
