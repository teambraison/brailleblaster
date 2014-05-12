package org.brailleblaster.settings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.ConfigFileHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.settings.ui.ConfigPanel;
import org.brailleblaster.settings.ui.Page;
import org.brailleblaster.util.FileUtils;

public class SettingsManager {
	private static final String NONMETRIC_COUTNRY = Locale.getDefault().getCountry();
	private boolean isMetric;
	private final Page [] standardPages = {new Page("Letter",8.5 ,11), new Page("Legal", 8.5, 14), new Page("A3", 11.69, 16.54),
			new Page("A4",8.27,11.69), new Page("A5", 5.83, 8.27)};
	
	private ConfigPanel configPanel;
	private HashMap<String, String>outputMap;
	private String config;
	public SettingsManager(String config){
		this.config = config;
		if(Locale.getDefault().getCountry().equals(NONMETRIC_COUTNRY))
			isMetric = false;
		else
			isMetric = true;
		
		outputMap = new HashMap<String, String>();
		openConfig(config);
	}
	
	public void open(Manager m){
		if(configPanel == null)
			configPanel = new ConfigPanel(this, m);
	}
	
	public void close(){
		configPanel.close();
		configPanel = null;
	}
	
	private void openConfig(String config){
		FileUtils fu = new FileUtils();
		String path = fu.findInProgramData("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + config);
		FileReader file;
		try {
			file = new FileReader(path);
			BufferedReader reader = new BufferedReader(file);
			makeHashTable(reader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void makeHashTable(BufferedReader reader) throws IOException{
		String currentLine;
		
		while ((currentLine = reader.readLine()) != null) {
			if(currentLine.length() > 0 && currentLine.charAt(0) != '#'){				
				if(currentLine.equals("outputFormat")){
					while((currentLine  = reader.readLine()) != null && currentLine.length() > 0){
						if(currentLine.length() > 0 && currentLine.charAt(0) != '#'){	
							setPageSettings(currentLine);
						}
					}
				}
			}	
		}
	}
	
	private void setPageSettings(String line){
		String [] tokens = line.trim().split(" ");
		if(tokens[0].equals("paperWidth"))
			setMapValue(tokens[0], tokens[1]);
		else if(tokens[0].equals("paperHeight"))
			setMapValue(tokens[0], tokens[1]);
		else if(tokens[0].equals("cellsPerLine"))
			setMapValue(tokens[0], tokens[1]);
		else if(tokens[0].equals("topMargin"))
			setMapValue(tokens[0], tokens[1]);
		else if(tokens[0].equals("bottomMargin"))
			setMapValue(tokens[0], tokens[1]);
		else if(tokens[0].equals("rightMargin"))
			setMapValue(tokens[0], tokens[1]);
		else if(tokens[0].equals("leftMargin"))
			setMapValue(tokens[0], tokens[1]);
		else if(tokens[0].equals("cellsPerLine"))
			setMapValue(tokens[0], tokens[1]);
		else if(tokens[0].equals("linesPerPage"))
			setMapValue(tokens[0], tokens[1]);
	}
	
	private void setMapValue(String key, String value){
		if(isMetric)
			outputMap.put(key, String.valueOf(inchesToMM(Double.valueOf(value))));
		else
			outputMap.put(key, value);
	}
	
	public String getSettings(){
		
		String settingsString = "";
		for(Entry<String, String> entry : outputMap.entrySet()){
			settingsString += entry.getKey() + " " + entry.getValue() +"\n ";
		}
		return settingsString;
	}
	
	public Page[] getStandardSizes(){
		return standardPages;
	}
	
	public void saveConfiguration(HashMap<String, String>map){
		outputMap.clear();
		outputMap.putAll(map);
		ConfigFileHandler handler = new ConfigFileHandler(config);
		handler.saveDocumentSettings(outputMap);
	}
	
	//public HashMap<String, String> getOutputMap(){
	//	return outputMap;
	//}
	
	public HashMap<String, String> getMapClone(){
		HashMap<String, String>temp = new HashMap<String, String>();
		temp.putAll(outputMap);
		return temp;
	}

	private double inchesToMM(double inches){
		double denominator = 0.039370;
		return Math.round((inches / denominator) * 10.0) / 10.0;
	}
	
	/*
	private double mmToInches(int mm){
		double multiplier = 0.039370;
		return Math.round((mm * multiplier) * 100.0) / 100.0;
	}
	*/
	
	public boolean isMetric(){
		return isMetric;
	}
}
