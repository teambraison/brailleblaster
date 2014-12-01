package org.brailleblaster.settings;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Elements;

public class TranslationConfiguration {
	private final int NAMEINDEX = 0;
	private final int FILEINDEX = 1;
	String locale, language, mathTable;
	ArrayList<Table>tableList;
	ArrayList<Table>compBrailleList;
	
	public TranslationConfiguration(Element e){
		tableList = new ArrayList<Table>();
		compBrailleList = new ArrayList<Table>();
		parseElement(e);
	}
	
	private void parseElement(Element e){
		Elements els = e.getChildElements();
		int count = els.size();
		for(int i = 0; i < count; i++){
			if(els.get(i).getLocalName().equals("locale"))
				locale = els.get(i).getValue();
			else if(els.get(i).getLocalName().equals("language"))
				language =  els.get(i).getValue();
			else if(els.get(i).getLocalName().equals("literaryBraille"))
				parseTable(els.get(i), tableList);
			else if(els.get(i).getLocalName().equals("math"))
				parseMath(els.get(i));
			else if(els.get(i).getLocalName().equals("computerBraille"))
				parseTable(els.get(i), compBrailleList);
		}
	}
	
	private void parseTable(Element e, ArrayList<Table>list){
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			Elements children = els.get(i).getChildElements();
			list.add(new Table(children.get(NAMEINDEX).getValue(), children.get(FILEINDEX).getValue()));
		}
	}
	
	private void parseMath(Element e){
		mathTable = e.getChildElements().get(0).getValue();
	}
	
	public String getLocale(){
		return locale;
	}
	
	public String getLanguage(){
		return language;
	}
	
	public String getMathTable(){
		return mathTable;
	}
	
	public ArrayList<Table>getTableList(){
		return tableList;
	}
	
	public ArrayList<Table>getCompBrailleList(){
		return compBrailleList;
	}
}
