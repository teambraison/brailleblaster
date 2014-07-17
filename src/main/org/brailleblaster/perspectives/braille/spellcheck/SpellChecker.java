package org.brailleblaster.perspectives.braille.spellcheck;

import org.slf4j.Logger;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;

public class SpellChecker {
	
	private static native int openDict(String dictPath, String affPath);
    private static native int checkWord(String wd);
	private static native int addWord(String wd);
    private static native Suggestions checkSug(String wd);
    private static native void closeDict();
	
    private Logger logger;
    public boolean active;
    private String dictPath, affPath;
    
	public SpellChecker(String dictPath, String affPath){
		logger = BBIni.getLogger();
		this.dictPath = dictPath;
		this.affPath = affPath;
		
		try {
			System.load(BBIni.getNativeLibraryPath() + BBIni.getFileSep() + "bbhunspell" + BBIni.getNativeLibrarySuffix());
		}
		catch(UnsatisfiedLinkError e){
			e.printStackTrace();
			logger.error("DLL did not load", e);
			active = false;
			return;
		}
		catch(Exception e){
			e.printStackTrace();
			logger.error("DLL did not load", e);
			active = false;
			return;
		}
		
		int result = open(dictPath, affPath);
		if(result != 1){
			active = false;
		}	
		else
			active = true;
	}
	
	private int open(String dictPath, String affPath){
		try {
			return openDict(dictPath, affPath);
		}
		catch(UnsatisfiedLinkError e){
			e.printStackTrace();
			logger.error("openDict Unsatisfied Link Error", e);
			return -1;
		}
	}
	
	public void open(){
		open(dictPath, affPath);
	}
	
	public void close(){
		closeDict();
	}
	
	public boolean checkSpelling(String word){
		int result = checkWord(word);
		if(result > 0)
			return true;
		else
			return false;
	}
	
	//hunspell's addWord function only adds to the runtime dictionary
	public void addToDictionary(String word){
		addWord(word);
	}
	
	public String[] getSuggestions(String word){
		Suggestions s = checkSug(word);
		String[] list = s.makeArray();
		return list;
	}
	
	public boolean isActive(){
		return active;
	}
}

class Suggestions {
	String suggestionList;

	String[] makeArray(){			
		if(suggestionList.length() == 0){
			LocaleHandler lh = new LocaleHandler();
			return new String[]{lh.localValue("noSuggestion")};
		}
		else
			return suggestionList.split(" ");
	}
}
