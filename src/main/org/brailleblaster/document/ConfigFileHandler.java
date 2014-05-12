package org.brailleblaster.document;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.brailleblaster.BBIni;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.util.FileUtils;

public class ConfigFileHandler {
	String configFile;
	FileUtils fu;
	
	public ConfigFileHandler(String configFile){
		this.configFile = configFile;
		this.fu = new FileUtils();
	}
	
	public void updateStyle(org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles newStyle){
		String path = BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile;
		if(!fu.exists(path))
			fu.copyFile(BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile, path);
		
		String key = "style " + newStyle.getName();
		String entry = formatStyleEntry(newStyle);
		
		String fileString = getFileContentsAsString(path);
		
		if(fileString != null){
			int startIndex = fileString.indexOf(key);
			int endIndex = fileString.substring(startIndex + 5).indexOf("style");
			
			if(endIndex != -1)
				fileString = fileString.replace(fileString.substring(startIndex, startIndex + endIndex + 5), entry);
			else
				fileString = fileString.replace(fileString.substring(startIndex), entry);
			
			fu.writeToFile(path, fileString);
		}
	}
	
	public void appendStyle(org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles style){
		String path = BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile;
		if(!fu.exists(path))
			fu.copyFile(BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile, path);
		
		String entry = formatStyleEntry(style);
		
		fu.appendToFile(path, entry);
	}
	
	private String formatStyleEntry(org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles style){
		String entry = "style " + style.getName() + "\n"; 
		
		Set<StylesType> set = style.getKeySet();
    	for(StylesType type : set)
    		entry += "\t" + type.toString() + " " + style.get(type) + "\n";
		
    	return entry;
	}
	
	private String getFileContentsAsString(String path){
		 BufferedReader reader = null;

		try {
			reader = new BufferedReader( new FileReader (path));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while( ( line = reader.readLine() ) != null ) {
				stringBuilder.append( line );
				stringBuilder.append( ls );
			}

			return stringBuilder.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		finally {
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void deleteStyle(String style){
		String path = BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile;
		if(!fu.exists(path))
			fu.copyFile(BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile, path);
		
		String key = "style " + style;
		
		String fileString = getFileContentsAsString(path);
		
		if(fileString != null){
			int startIndex = fileString.indexOf(key);
			int endIndex = fileString.substring(startIndex + 5).indexOf("style");
			
			if(endIndex != -1)
				fileString = fileString.replace(fileString.substring(startIndex, startIndex + endIndex + 5), "");
			else
				fileString = fileString.replace(fileString.substring(startIndex), "");
			
			fu.writeToFile(path, fileString);
		}
	}
	
	public void restoreDefaults(){
		String path = BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile;
		if(fu.exists(path))
			fu.deleteFile(path);	
	}
	
	public void saveDocumentSettings(HashMap<String, String>map){
		String path = BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile;
		if(!fu.exists(path))
			fu.copyFile(BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configFile, path);
		
		String fileString = getFileContentsAsString(path);
		
		if(fileString != null){
			for(Entry<String, String>entry : map.entrySet()){
				int startIndex = fileString.indexOf(entry.getKey());
				if(startIndex != -1){
					int endIndex = startIndex + fileString.substring(startIndex).indexOf('\n');
				
				
					if(endIndex != -1)
						fileString = fileString.replace(fileString.substring(startIndex, endIndex), entry.getKey() + " " + entry.getValue());
					else
						fileString = fileString.replace(fileString.substring(startIndex), entry.getKey() + " " + entry.getValue());
				}
				else
					fileString = insertDocumentSetting(fileString, entry.getKey() + " " + entry.getValue());
			}
			
			fu.writeToFile(path, fileString);
		}
	}
	
	private String insertDocumentSetting(String fileString, String entry){
		String heading = "outputFormat";
		int startIndex = fileString.indexOf(heading);
		int endIndex = startIndex + fileString.substring(startIndex).indexOf("\n");
		
		fileString = fileString.substring(0, endIndex + 1) + "\t" + entry + fileString.substring(endIndex);
		return fileString;
	}
}
