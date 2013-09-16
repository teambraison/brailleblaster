package org.brailleblaster.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.brailleblaster.BBIni;
import org.brailleblaster.util.FileUtils;

public class SemanticFileHandler {

	private String configPath;
	private String defaultSemanticsFiles;
	private FileUtils fu;
	private Logger log = BBIni.getLogger();
	private HashMap<String, String> defaults;
	
	public SemanticFileHandler(String configPath){
		fu = new FileUtils();
		this.configPath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + configPath);
		this.defaultSemanticsFiles = getSemanticsFile(this.configPath);
		this.defaults = new HashMap<String, String>();
		makeDefaultMap();
	}
	
	private String getSemanticsFile(String configPath){
		String currentLine;
		FileReader fr;
		BufferedReader reader;
		String defaultSem = "*";
		
		try {
			fr = new FileReader(new File(configPath));
			reader = new BufferedReader(fr);
		
			while((currentLine = reader.readLine()) != null){
				if(!currentLine.contains("#") && currentLine.contains("semanticFiles")){
					String [] tokens = currentLine.split(" ");
					defaultSem = tokens[1];
					break;
				}
			}
			fr.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.log(Level.SEVERE, "File Not Found Exception", e);
		}
		catch (IOException e) {
			e.printStackTrace();
			log.log(Level.SEVERE, "IO Exception", e);
		}
		
		return defaultSem;
	}
	
	private void makeDefaultMap(){		
		String [] tokens = defaultSemanticsFiles.split(",");
		for(int i = 0; i < tokens.length; i++){
			String defaultSemFile = BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + tokens[i];
			String currentLine;
			
			File f = new File(defaultSemFile);

			try {
				FileReader fr = new FileReader(f);
				BufferedReader br = new BufferedReader(fr);
				
				while ((currentLine = br.readLine()) != null) {
					if(!currentLine.contains("#") && !currentLine.equals("")){
						String [] tokens2 = currentLine.split(" ");
						defaults.put(tokens2[1], tokens2[0]);
					}
				}
				
				br.close();
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch(IOException e){
				e.printStackTrace();
			}	
		}
	}
	
	public String getSemanticsConfigSetting(String filePath){
		return checkForSemantics(filePath);
	}
	
	private String checkForSemantics(String filePath){
		String file = filePath.substring(0, filePath.lastIndexOf(".")) + ".sem";
		String fileName = fu.getFileName(filePath) + ".sem";
		String tempFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName;
		if(fu.exists(file)){
			if(!file.equals(tempFile))
				fu.copyFile(file, tempFile);
			
			return "semanticFiles " + defaultSemanticsFiles + "," + tempFile + "\n ";
		}
		else
			return "";
	}
	
	public void writeEntry(String path, String style, String element, String id){
		if(fu.exists(path)){
			appendEntry(path, style, element,id);
		}
		else {
			createNewSemanticsFile(path, style, element,id);
		}
	}
	
	private void appendEntry(String path, String style, String element, String id){
		String text = style + " " + element + ",id," + id + "\n";
		writeSemanticEntry(path, id, text);
	}
	
	private void createNewSemanticsFile(String path, String style, String element, String id){
		String text = style + " " + element + ",id," + id + "\n";
		fu.create(path);
		fu.writeToFile(path, text);
	}
	
	private void writeSemanticEntry(String fullPath, String id, String entry){
		String currentLine;
    	String [] tokens;
    	StringBuilder sb= new StringBuilder();
    	boolean found = false;
    	boolean entered = false;
    	
    	try {
    		FileReader file = new FileReader(fullPath);
			BufferedReader reader = new BufferedReader(file);
			while((currentLine = reader.readLine()) != null){
				tokens = currentLine.split(",");
				for(int i = 0; i < tokens.length && !found; i++){
					if(tokens[i].equals(id)){
						found = true;
					}
				}
				
				if(found && !entered){
					sb.append(entry);
					entered = true;
				}
				else {
					sb.append(currentLine + "\n");
				}
				
			}
			
			if(!found){
				sb.append(entry);
			}
			reader.close();
			fu.writeToFile(fullPath, sb);
    	}
    	catch(FileNotFoundException e){
    		e.printStackTrace();
    		log.log(Level.SEVERE, "File Not Found Exception", e);
    	}
    	catch(IOException e){
    		e.printStackTrace();
    		log.log(Level.SEVERE, "IO Exception", e);
    	}
	}
	
	public void resetSemanticHandler(String config){
		defaults.clear();
		configPath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + config);
		defaultSemanticsFiles = getSemanticsFile(this.configPath);
		makeDefaultMap();
	}
	
	public String getDefaultSemanticsFiles(){
		return defaultSemanticsFiles;
	}
	
	public String getDefault(String elementName){
		return defaults.get(elementName);
	}
}
