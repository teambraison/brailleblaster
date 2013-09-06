package org.brailleblaster.document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.brailleblaster.BBIni;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;


public class BBSemanticsTable {
	public enum StylesType{
		emphasis,
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
		
		public Set<Entry<StylesType, String>> getEntrySet(){
			return this.map.entrySet();
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
	FileUtils fu = new FileUtils();
	static Logger logger = BBIni.getLogger();
	
	public BBSemanticsTable(){
		try {
			this.table = new HashMap<String, Styles>();
			String filePath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + BBIni.getDefaultConfigFile());
			FileReader file = new FileReader(filePath);
			BufferedReader reader = new BufferedReader(file);
			makeHashTable(reader);
			reader.close();
			makeStylesObject("italicx");
			insertValue("italicx","\tFont " + SWT.ITALIC);
			makeStylesObject("boldx");
			insertValue("boldx","\tFont " + SWT.BOLD);
		}
		catch(Exception e){
			e.printStackTrace();
			new Notify("The application failed to load due to errors in " + BBIni.getDefaultConfigFile());
			logger.log(Level.SEVERE, "Config File Error", e);
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
		else if(tokens[0].substring(1).equals("format") && tokens[1].equals("rightJustified")){
			tokens[1] = String.valueOf(SWT.RIGHT);
		}
		else if(tokens[0].substring(1).equals("format") && tokens[1].equals("leftJustified")){
			tokens[1] = String.valueOf(SWT.LEFT);
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
	
	public String getSemanticTypeFromAttribute(Element e){
		String pair = e.getAttributeValue("semantics");
		
		if(pair == null){
			return null;
		}
		else {
			String[] tokens = pair.split(",");
			return tokens[0];	
		}
	}
	
	public Styles makeStylesElement(String key, Node n){
		Styles temp = new Styles(key);
		makeComposite(key, temp);
		
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
		
		return temp;
	}
	
	public Styles makeStylesElement(Element e, Node n){
		String key = getKeyFromAttribute(e);
		return makeStylesElement(key, n);
	}
	
	private void makeComposite(String key, Styles st){
		Styles newStyle = this.table.get(key);
		if(newStyle != null){
			for (StylesType styleType : newStyle.getKeySet()) {
				if(!st.contains(styleType)){
					st.put(styleType, (String)newStyle.get(styleType));
				}
				else if(st.contains(styleType) && styleType.equals(StylesType.Font)){
					st.put(styleType, (String.valueOf(combineFontStyles((String)st.get(styleType), (String)newStyle.get(styleType)))));
				}
			}
		}
	}
	
	private int combineFontStyles(String font1, String font2){
		if(font1.equals(font2)){
			return Integer.valueOf(font1);
		}
		else {
			return Integer.valueOf(font1) + Integer.valueOf(font2);
		}
	}
	
	public boolean isBlockElement(Element e){
		if(getSemanticTypeFromAttribute(e).equals("style"))
			return true;
		else
			return false;
	}
}
