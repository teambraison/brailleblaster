package org.brailleblaster.perspectives.braille.document;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.brailleblaster.BBIni;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

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
		newlineAfter;
	}
	
	public class Styles{
		String elementName;
		TreeMap<StylesType, Object> map;
		
		public Styles(String elementName){
			this.elementName = elementName;
			map = new TreeMap<StylesType, Object>();
		}
		
		public void put(StylesType key, StyleRange value){	
			map.put(key, value);
		}
		
		public void put(StylesType key, String value){	
			map.put(key, value);
		}
		
		public Object get(StylesType st){
			return map.get(st);
		}
		
		public Set<StylesType> getKeySet(){
			return map.keySet(); 
		}
		
		public Set<Entry<StylesType, Object>> getEntrySet(){
			return map.entrySet();
		}
		
		public boolean contains(StylesType key){
			return map.containsKey(key);
		}
		
		public String getName(){
			return elementName;
		}
	}
	
	Document doc;
	TreeMap<String,Styles> table;
	FileUtils fu = new FileUtils();
	String config;
	static Logger logger = BBIni.getLogger();
	
	public BBSemanticsTable(String config){
		try {
			table = new TreeMap<String, Styles>();
			this.config = config;
			String filePath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + config);
			FileReader file = new FileReader(filePath);
			BufferedReader reader = new BufferedReader(file);
			makeHashTable(reader);
			reader.close();
			makeStylesObject("italicx");
			insertValue("italicx","\temphasis italicx");
			makeStylesObject("boldx");
			insertValue("boldx","\temphasis boldx");
			makeStylesObject("underlinex");
			insertValue("underlinex","\temphasis underlinex");
		}
		catch(Exception e){
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
	
	public Styles getNewStyle(String name){
		return new Styles(name);
	}
	
	private void makeStylesObject(String key){
		Styles temp = new Styles(key);
		table.put(key, temp);
	}
	
	private void insertValue(String styleName, String keyValuePair){
		Styles temp = table.get(styleName);
		String [] tokens = keyValuePair.split(" ");
		
		if(tokens[0].substring(1).equals("format") && tokens[1].equals("centered"))
			temp.put(StylesType.valueOf(tokens[0].substring(1)), String.valueOf(SWT.CENTER));
		else if(tokens[0].substring(1).equals("format") && tokens[1].equals("rightJustified"))
			temp.put(StylesType.valueOf(tokens[0].substring(1)), String.valueOf(SWT.RIGHT));
		else if(tokens[0].substring(1).equals("format") && tokens[1].equals("leftJustified"))
			temp.put(StylesType.valueOf(tokens[0].substring(1)), String.valueOf(SWT.LEFT));
		else if(tokens[0].substring(1).equals("emphasis") && tokens[1].equals("boldx"))
			setFontAttributes(temp, tokens[0].substring(1), SWT.BOLD, false);
		else if(tokens[0].substring(1).equals("emphasis") && tokens[1].equals("italicx"))
			setFontAttributes(temp, tokens[0].substring(1), SWT.ITALIC, false);
		else if(tokens[0].substring(1).equals("emphasis") && tokens[1].equals("underlinex"))
			setFontAttributes(temp, tokens[0].substring(1), SWT.UNDERLINE_SINGLE, true);
		else
			temp.put(StylesType.valueOf(tokens[0].substring(1)), tokens[1]);
	}
	
	//Used to set a new SWT SyleRange object to a styles map.  StyleRange controls fontstyle which is an integer and underline which is boolean
	private void setFontAttributes(Styles style, String key, int value, boolean underline){
		StyleRange 	sr = new StyleRange();
		
		if(underline == true)
			sr.underline = true;
		else
			sr.fontStyle = value;
		
		style.put(StylesType.valueOf(key), sr);
	}
	
	public boolean containsKey(String key){
		return table.containsKey(key);
	}
	
	public Styles get(String key){
		return table.get(key);
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
			while(!nextKey.equals("document") && !nextKey.equals("markhead")){
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
		Styles newStyle = table.get(key);
		if(newStyle != null){
			for (StylesType styleType : newStyle.getKeySet()) {
				if(!st.contains(styleType)){
					if(styleType.equals(StylesType.emphasis))
						st.put(styleType, (StyleRange)newStyle.get(styleType));
					else {
						st.put(styleType, (String)newStyle.get(styleType));
					}
				}
				else if(st.contains(styleType) && styleType.equals(StylesType.emphasis)){
					st.put(styleType, (combineFontStyles(((StyleRange)st.get(styleType)), ((StyleRange)newStyle.get(styleType)))));
				}
			}
		}
	}
	
	private StyleRange combineFontStyles(StyleRange font1, StyleRange font2){
		StyleRange newStyle = new StyleRange();
		newStyle.fontStyle = font1.fontStyle;
		
		if(font1.fontStyle != font2.fontStyle && font1.fontStyle + font2.fontStyle <= 3) 
			newStyle.fontStyle = font1.fontStyle + font2.fontStyle;
		
		if(font2.underline == true)
			newStyle.underline = true;
		
		return newStyle;
	}
	
	public boolean isBlockElement(Element e){
		if(getSemanticTypeFromAttribute(e).equals("style"))
			return true;
		else
			return false;
	}
	
	public void resetStyleTable(String configFile){
		table.clear();
		String filePath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile);
		this.config = configFile;
		
		try {
			FileReader file = new FileReader(filePath);
			BufferedReader reader = new BufferedReader(file);
			makeHashTable(reader);
			reader.close();
			makeStylesObject("italicx");
			insertValue("italicx","\temphasis italicx");
			makeStylesObject("boldx");
			insertValue("boldx","\temphasis boldx");
			makeStylesObject("underlinex");
			insertValue("underlinex","\temphasis underlinex");
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Set<String>getKeySet(){
		return table.keySet();
	}
	
	public String getConfig(){
		return config;
	}
}
