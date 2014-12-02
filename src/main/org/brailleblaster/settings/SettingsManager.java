package org.brailleblaster.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import org.brailleblaster.BBIni;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.settings.ui.ConfigPanel;
import org.brailleblaster.settings.ui.Page;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.PropertyFileManager;

public class SettingsManager {
	private static final String NONMETRIC_COUTNRY = "US";
	private static final String USER_SETTINGS = BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + "utdmlSettings.properties";
			
	private boolean isMetric;
	private final Page [] standardPages = {new Page("Standard",11.5 ,11),new Page("Letter",8.5 ,11), new Page("Legal", 8.5, 14), new Page("A3", 11.69, 16.54),
			new Page("A4",8.27,11.69), new Page("A5", 5.83, 8.27)};
	
	private ConfigPanel configPanel;
	private HashMap<String, String>outputMap;
	private boolean compress;
	public SettingsManager(String config){
		if(Locale.getDefault().getCountry().equals(NONMETRIC_COUTNRY))
			isMetric = false;
		else
			isMetric = true;
		
		compress = false;
		outputMap = new HashMap<String, String>();
		setDefault(config);
	}
	
	public void open(Manager m){
		if(configPanel == null)
			configPanel = new ConfigPanel(this, m);
	}
	
	public void close(){
		configPanel.close();
		configPanel = null;
	}
	
	
	private void setDefault(String config){
		File f = new File(USER_SETTINGS);
		if(!f.exists())
			openConfig(config);
		else
			openPropertiesFile();
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
	
	private void openPropertiesFile(){
		PropertyFileManager pfm = new PropertyFileManager(USER_SETTINGS);
		Enumeration<?> en = pfm.getKeySet();
		while(en.hasMoreElements()){
			String key = (String) en.nextElement();
			String value = pfm.getProperty(key);
			outputMap.put(key, value);
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
				if(currentLine.equals("translation")){
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
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("paperHeight"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("cellsPerLine"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("topMargin"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("bottomMargin"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("rightMargin"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("leftMargin"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("cellsPerLine"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("linesPerPage"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("literaryTextTable")){
			String correctTable = extractTable(tokens[1]);
			outputMap.put(tokens[0], correctTable);
		}
		else if(tokens[0].equals("mathexprTable"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("compbrlTable"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("numberBraillePages"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("printPages"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("continuePages"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("printPageNumberAt"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("braillePageNumberAt"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("printPageNumberRange"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("interpoint"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("minLinesPerPage"))
			outputMap.put(tokens[0], tokens[1]);
		else if(tokens[0].equals("minCellsPerLine"))
			outputMap.put(tokens[0], tokens[1]);
	}
	
	//do not include compress.cti because removing spaces should be left up to the user to remove
	private String extractTable(String string) {
		String table = null;
		String[] tokens = string.split(",");
		for(int i = 0; i < tokens.length; i++){
			if(!tokens[i].equals("compress.cti"))
				table = tokens[i];
			else if(tokens[i].equals("compress.cti"))
				compress = true;
		}
		
		return table;
	}

	public String getSettings(){		
		String settingsString = "";
		for(Entry<String, String> entry : outputMap.entrySet()){
			if(entry.getKey().equals("literaryTextTable") && compress)
				settingsString += entry.getKey() + " " + "compress.cti," + entry.getValue() +"\n";
			else
				settingsString += entry.getKey() + " " + entry.getValue() +"\n ";
		}
		return settingsString;
	}
	
	public Page[] getStandardSizes(){
		return standardPages;
	}
	
	public void saveConfiguration(HashMap<String, String>map){
		outputMap.clear();
		resetMap(map);
		saveSettings();
	}
	
	private void saveSettings(){
		File f = new File(USER_SETTINGS);
		
			try {
				if(!f.exists())
					f.createNewFile();
				
				PropertyFileManager pfm = new PropertyFileManager(USER_SETTINGS);
				for(Entry<String, String>entry : outputMap.entrySet()){
					pfm.save(entry.getKey(), entry.getValue());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public HashMap<String, String> getMapClone(){
		HashMap<String, String>temp = new HashMap<String, String>();
		
		for(Entry<String, String>entry : outputMap.entrySet()){
			if(isMetric && numericEntry(entry.getKey()))
				temp.put(entry.getKey(), String.valueOf(inchesToMM(Double.valueOf(entry.getValue()))));
			else
				temp.put(entry.getKey(), entry.getValue());
		}
		
		return temp;
	}
	
	private void resetMap(HashMap<String, String>newMap){
		for(Entry<String, String>entry : newMap.entrySet()){
			if(isMetric && numericEntry(entry.getKey()))
				outputMap.put(entry.getKey(), String.valueOf(mmToInches(Double.valueOf(entry.getValue()))));
			else
				outputMap.put(entry.getKey(), entry.getValue());
		}
	}

	private double inchesToMM(double inches){
		double denominator = 0.039370;
		return Math.round((inches / denominator) * 10.0) / 10.0;
	}
	
	
	private double mmToInches(double mm){
		double multiplier = 0.039370;
		return Math.round((mm * multiplier) * 100.0) / 100.0;
	}
	
	private boolean numericEntry(String key){
		if(key.equals("topMargin"))
			return true;
		else if(key.equals("bottomMargin"))
			return true;
		else if(key.equals("rightMargin"))
			return true;
		else if(key.equals("leftMargin"))
			return true;
		else if(key.equals("paperWidth"))
			return true;
		else if(key.equals("paperHeight"))
			return true;
		
		return false;
	}
	
	public boolean isMetric(){
		return isMetric;
	}
	
	/***
	 * Calculate numbers of lines per page
	 * @return Number of line per page
	 */
	private int calculateLinesPerPage(double pHeight){
		double cellHeight = 0.393701;
		
 		if(outputMap.containsKey("topMargin"))
			pHeight -= Double.valueOf(outputMap.get("topMargin"));
		
		if(outputMap.containsKey("bottomMargin"))
			pHeight -= Double.valueOf(outputMap.get("bottomMargin"));
		
		return (int)(pHeight / cellHeight);
	}
	
	/*
	 * Calculate the height of the page from the lines it contains.
	 * This method will receive the number of lines per page and return
	 * a double measurement of height for the whole page.
	 */
	public double calculateHeightFromLength(int linesPerPage){
		double cellHeight;
		if (isMetric())
			cellHeight = 0.393701;
		else
			cellHeight = 10;
		return cellHeight*linesPerPage;
	}
	/**
	 * Find indicator location
	 */
	public int getLinesPerPage()
	{
		return calculateLinesPerPage(Double.valueOf(outputMap.get("paperHeight")));
	}
	
	public int getCellsPerLine() {
		return Integer.valueOf( outputMap.get("cellsPerLine") );
	}

	public int getMinCellsPerLine() {
		return Integer.valueOf( outputMap.get("minCellsPerLine") );
	}
	
	public int getMinLinesPerPage() {
		return Integer.valueOf( outputMap.get("minLinesPerPage") );
	}
}
