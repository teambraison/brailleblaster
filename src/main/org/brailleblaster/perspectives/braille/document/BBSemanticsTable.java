package org.brailleblaster.perspectives.braille.document;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.brailleblaster.BBIni;
import org.brailleblaster.util.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import org.apache.commons.lang3.BooleanUtils;
import org.brailleblaster.utd.Style;
import org.brailleblaster.utd.config.StyleDefinitions;

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
		topBoxline,
		bottomBoxline,
		name;
	}
	
	public class Styles implements Cloneable {
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
			if (map.containsKey(st))
			   return map.get(st);
			else
				return null;	
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
		
		public void setName(String elementName){
			this.elementName = elementName;
		}
		
		public String getName(){
			return elementName;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Styles clone() throws CloneNotSupportedException {
			Styles clone = (Styles)super.clone();
			clone.map = (TreeMap<StylesType, Object>)clone.map.clone();
			clone.elementName = new String(clone.elementName);
			return clone;
		}
	}
	private static final Logger log = LoggerFactory.getLogger(BBSemanticsTable.class);
	Document doc;
	TreeMap<String,Styles> table;
	FileUtils fu = new FileUtils();
	String config, fileConfig;
	static Logger logger = LoggerFactory.getLogger(BBSemanticsTable.class);
	
	public BBSemanticsTable(String config){
		table = new TreeMap<String, Styles>();
		this.config = config;
		String filePath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + config);
		populateTable(filePath);
		
		//Port to definitions
		log.debug("----------------- Init with config " + config);
		try {
			StyleDefinitions styleDefs = new StyleDefinitions();
			for(Map.Entry<String, Styles> curEntry : table.entrySet()) {
				String curStyleKey = curEntry.getKey();
				Styles curStyle = curEntry.getValue();
				Style newStyle = styleDefs.addStyle(curStyleKey);
				
				log.debug("Style " + curStyleKey);
				for(StylesType typeKey : curStyle.getKeySet()) {
					String typeValue = curStyle.get(typeKey).toString();

					//missing rightMargin, keepWithPrevious
					switch (typeKey) {
						case linesBefore:
							newStyle.setLinesBefore(Integer.parseInt(typeValue));
							break;
						case linesAfter:
							newStyle.setLinesAfter(Integer.parseInt(typeValue));
							break;
						case leftMargin:
							newStyle.setLeftMargin(Integer.parseInt(typeValue));
							break;
						case firstLineIndent:
							newStyle.setFirstLineIndent(Integer.parseInt(typeValue));
							break;
						case skipNumberLines:
							boolean skipNumberLinesOpt = BooleanUtils.toBoolean(typeValue);
							newStyle.setSkipNumberLines(skipNumberLinesOpt);
							//log.debug(" - skipNumberLines " + typeValue + " " + skipNumberLinesOpt);
							break;
						case newPageBefore:
							boolean newPageBeforeOpt = BooleanUtils.toBoolean(typeValue);
							newStyle.setNewPageBefore(newPageBeforeOpt);
							//log.debug(" - newPageBefore " + typeValue + " " + newPageBeforeOpt);
							break;
						case newPageAfter:
							boolean newPageAfterOpt = BooleanUtils.toBoolean(typeValue);
							newStyle.setNewPageAfter(newPageAfterOpt);
							//log.debug(" - newPageAfter " + typeValue + " " + newPageAfterOpt);
							break;
						case righthandPage:
							boolean righthandPageOpt = BooleanUtils.toBoolean(typeValue);
							newStyle.setRightHandPage(righthandPageOpt);
							//log.debug(" - righthandPage " + typeValue + " " + righthandPageOpt);
							break;
						case keepWithNext:
							boolean keepWithNextOpt = BooleanUtils.toBoolean(typeValue);
							newStyle.setKeepWithNext(keepWithNextOpt);
							//log.debug(" - keepWithNext " + typeValue + " " + keepWithNextOpt);
							break;
						case dontSplit:
							boolean dontSplitOpt = BooleanUtils.toBoolean(typeValue);
							newStyle.setDontSplit(dontSplitOpt);
							//log.debug(" - dontSplit " + typeValue + " " + dontSplitOpt);
							break;
						case orphanControl:
							int orphanControlValue = Integer.parseInt(typeValue);
							if(orphanControlValue == 0) {
								log.debug(" - orphanControl is 0, converting to 1 on style " + curStyleKey);
								orphanControlValue = 1;
							}
							newStyle.setOrphanControl(orphanControlValue);
							break;
						case format:
							Style.Format styleFormat;
							if(curStyleKey.equals("document"))
								//Fuck you java
								styleFormat = Style.Format.LEFT_JUSTIFIED;
							else if(curStyleKey.equals("heading1"))
								//Fuck you java
								styleFormat = Style.Format.CENTERED;
							else if(typeValue.equals("alignColumnsLeft"))
								styleFormat = Style.Format.ALIGN_COLUMNS_LEFT;
							else
								styleFormat = Style.Format.valueOf(typeValue);
							log.debug(" - Format " + curStyle.get(typeKey) + " converted to " + styleFormat);
							newStyle.setFormat(styleFormat);
							break;
						case name:
							log.debug(" - Style name is " + curStyleKey + " but name value is " + typeValue);
							newStyle.setName(typeValue);
							break;
						default:
							log.debug(" - Unknown " + typeKey + " " + typeValue);
					}
				}
			}
			styleDefs.save(new FileOutputStream("C:\\Users\\lblakey\\Desktop\\projects\\brailleblaster\\styleDefsNew.xml"));
		} catch (Exception ex) {
			log.error("!!!!Dump failed!!!!!", ex);
		}
		
		System.exit(1);
		
		setEmphasisValues();
		log.debug("-----------------");
	}
	
	private void populateTable(String filePath){
		FileReader file;
		try {
			file = new FileReader(filePath);
			BufferedReader reader = new BufferedReader(file);
			makeHashTable(reader);
			reader.close();
		}catch (FileNotFoundException e) {
			logger.error("File Not Found Exception Config: " + config, e);
		} catch (IOException e) {
			logger.error("IO Exception Config: " + config, e);
		}
	}
	
	private void setEmphasisValues(){
		makeStylesObject("italicx");
		insertValue("italicx","\temphasis italicx");
		makeStylesObject("boldx");
		insertValue("boldx","\temphasis boldx");
		makeStylesObject("underlinex");
		insertValue("underlinex","\temphasis underlinex");
	}
	
	private void makeHashTable(BufferedReader reader) throws IOException{
		String currentLine;
		String preferredName=null;
		String styleName = null;
		String Names[]=new String[2];

		while ((currentLine = reader.readLine()) != null) {
			if(currentLine.length() > 0 && currentLine.charAt(0) != '#'){				
				if(currentLine.length() >= 5 && currentLine.substring(0, 5).equals("style")){
					Names = FindName(currentLine.substring(6, currentLine.length()));
					styleName=Names[0];
					makeStylesObject(styleName);
					// if there was any preferredName in configuration file then insert it here
					if(Names.length>1)
					{
						preferredName=Names[1];	
						String temp="name "+preferredName;
						//Add another entry to table with key name and value prefereedName
						insertValue(styleName,temp); 
					}

				}
				else if(!currentLine.contains("#") && styleName!=null){
					insertValue(styleName, currentLine);
				}
			}
		}
	}
	/**
	 * Find name of style and its preferred name
	 * @param currentLine
	 * @return
	 */
	private String[] FindName(String currentLine){
		String Names[]=new String[2];
		Names[1]=null;
		Names=currentLine.split("\\s+");
		return Names;
		
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
		// for preferred name
		if(tokens[0].substring(0).equals("name") )
			temp.put(StylesType.valueOf(tokens[0].substring(0)), formatName(tokens[1]));
		else if(tokens[0].substring(1).equals("format") && tokens[1].equals("centered"))
			temp.put(StylesType.valueOf(tokens[0].substring(1)), String.valueOf(SWT.CENTER));
		else if(tokens[0].substring(1).equals("format") && tokens[1].equals("rightJustified"))
			temp.put(StylesType.valueOf(tokens[0].substring(1)), String.valueOf(SWT.RIGHT));
		else if(tokens[0].substring(1).equals("format") && (tokens[1].equals("leftJustified") || tokens[1].equals("computerCoded") || tokens[1].equals("contents")))
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
	
	private String formatName(String name){
		String[] tokens = name.split("(?=\\p{Upper})");
		
		if(tokens.length > 0){
			name = "";
			for(int i = 0; i < tokens.length; i++){
				String[] tokens2 = tokens[i].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
				if(tokens2.length > 0){
					for(int j = 0; j < tokens2.length; j++){
						name += tokens2[j] + " ";
					}
				}
				else
					name += tokens[i] + " ";
			}
		}
		
		return name.trim();
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
		this.config = configFile;
		
		String filePath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile);
		populateTable(filePath);
		setEmphasisValues();
	}
	
	public void resetStyleTable(String configFile, String fileName){
		resetStyleTable(configFile);
		
		String docConfig = fu.getFileName(fileName) + ".cfg";
		String localConfig = fu.getPath(fileName) + BBIni.getFileSep() + docConfig;
		if(fu.exists(localConfig)){
			populateTable(localConfig);
			this.fileConfig = docConfig;
		}
	}
	
	public Set<String>getKeySet(){
		return table.keySet();
	}
	
	public boolean hasDocumentConfig(){
		return fileConfig == null;
	}
	
	public String getDocumentConfig(){
		return fileConfig;
	}
	
	public String getConfig(){
		return config;
	}
}
