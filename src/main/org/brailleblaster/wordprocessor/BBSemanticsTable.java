package org.brailleblaster.wordprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.brailleblaster.BBIni;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;

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
		newlineAfter,
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
		try {
			this.table = new HashMap<String, Styles>();
			FileReader file = new FileReader(BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + "preferences.cfg");
			BufferedReader reader = new BufferedReader(file);
			makeHashTable(reader);
			reader.close();
			makeStylesObject("italicx");
			insertValue("italicx","\tFont italic");
		}
		catch(Exception e){
			e.printStackTrace();
			new Notify("The application failed to load due to errors in preferences.cfg");
		}
	}
	
	private void makeHashTable(BufferedReader reader) throws IOException{
		String currentLine;
		String styleName;
		
		while ((currentLine = reader.readLine()) != null) {
			if(currentLine.length() > 0 && currentLine.charAt(0) != '#'){				
				if(currentLine.length() >= 5 && currentLine.substring(0, 5).equals("style")){
					styleName = currentLine.substring(6, currentLine.length()).trim();
					makeStylesObject(styleName);
					while((currentLine  = reader.readLine()) != null && currentLine.length() > 0){
						if(currentLine.length() >= 5 && currentLine.substring(0, 5).equals("style")){
							styleName = currentLine.substring(6, currentLine.length()).trim();
							makeStylesObject(styleName);
						}
						else if(!currentLine.contains("#"))
							insertValue(styleName, currentLine);
					}
				}
			}
		}
	}
	
	private void makeStylesObject(String key){
		Styles temp = new Styles(key);
		this.table.put(key, temp);
	}
	
	private void insertValue(String styleName, String keyValuePair){
		Styles temp = this.table.get(styleName);
		String [] tokens = keyValuePair.split(" ");
		
		if(tokens[0].substring(1).equals("format") && tokens[1].equals("centered")){
			tokens[1] = String.valueOf(SWT.CENTER);
		}
		temp.put(StylesType.valueOf(tokens[0].substring(1)), tokens[1]);
	}
	
	public boolean containsKey(String key){
		return this.table.containsKey(key);
	}
	
	public Styles get(String key){
		return this.table.get(key);
	}
	
	public String getKeyFromAttribute(Element e){
		String pair = e.getAttributeValue("semantics");
		
		if(pair == null){
			return "no";
		}
		else {
			String[] tokens = pair.split(",");
			return tokens[1];	
		}
	}
	
	public Styles makeStylesElement(String key, Node n){
		Styles temp = this.table.get(key);
		
		if(temp != null){
			Element e = (Element)n.getParent();
			String nextKey = getKeyFromAttribute(e);
			while(!nextKey.equals("document")){
				if(this.table.containsKey(nextKey)){
					makeComposite(nextKey,temp);
				}
				e = (Element)e.getParent();
				nextKey = getKeyFromAttribute(e);
			}
		}
		else {
			temp = new Styles(key);
		}
		
		return temp;
	}
	
	private void makeComposite(String key, Styles st){
		Styles newStyle = this.table.get(key);
		for (StylesType styleType : newStyle.getKeySet()) {
			if(!st.contains(styleType)){
				st.put(styleType, (String)newStyle.get(styleType));
			}
		}
	}
}
