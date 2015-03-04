package org.brailleblaster.perspectives.braille.spellcheck;

import java.util.Vector;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;

public class SpellCheckManager {
	private SpellChecker sc;
	private SpellCheckView view;
	private Tokenizer tokenizer;
	private Vector<String> ignoreList = new Vector<String>();
    private String dictPath, affPath, dictLang;
    private FileUtils fu;
    private Manager m;
    private boolean correctSpelling;
    private LocaleHandler lh;
    
    public SpellCheckManager(Manager m) {
    	fu = new FileUtils();
    	lh = new LocaleHandler();
    	this.m = m;
    	if(!m.getIgnoreList().isEmpty()){
	    	for(String dc : m.getIgnoreList()){
	    		ignoreList.add(dc);
	    	}
    	}
	  	try {
	  		dictLang = lh.localValue("dictionary");
	  		dictPath = fu.findInProgramData("dictionaries" + BBIni.getFileSep() + dictLang + ".dic");
	  		affPath = fu.findInProgramData("dictionaries" + BBIni.getFileSep() + dictLang + ".aff");
	  		
	  		if(dictPath != null && affPath != null){
	  			sc = new SpellChecker(dictPath, affPath);
        		if(sc.isActive()){
        			view = new SpellCheckView(m.getDisplay(), this);
        			tokenizer = new Tokenizer(m.getText().view.getText().replace("\n", " "));
        			checkWord();
        		}
        		else {
        			new Notify(lh.localValue("spellCheckError"));
        		}
	  		}
	  		else
	  			new Notify(lh.localValue("spellCheckError"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void setWord(String word, String [] suggestions){ 	
    		view.setWord(word, suggestions);
    		m.getText().highlight(tokenizer.getStartPos(), tokenizer.getEndPos());	
    }
    
    public void checkWord(){
    	correctSpelling = true;
    	
    	while(correctSpelling && tokenizer.next()){
    		if(!ignoreList.contains(tokenizer.getCurrentWord())){
    			correctSpelling = sc.checkSpelling(tokenizer.getCurrentWord());
    			if(!correctSpelling){
    				String [] suggestions = sc.getSuggestions(tokenizer.getCurrentWord());
    				if(tokenizer.getSplitPos()!=0){ //Caught a word that probably needs a space
    					String word1, word2;
    					word1 = tokenizer.getCurrentWord().substring(0, tokenizer.getSplitPos());
    					word2 = tokenizer.getCurrentWord().substring(tokenizer.getSplitPos());
    					String[] newSuggestions = new String[suggestions.length+1]; // Make a new suggestions array that includes existing words with space
    					newSuggestions[0] = word1 + " " + word2;
    					System.arraycopy(suggestions, 0, newSuggestions, 1, suggestions.length);
    					setWord(tokenizer.getCurrentWord(), newSuggestions);
    				} else {
    					setWord(tokenizer.getCurrentWord(), suggestions);
    				}
    			}		
    		}
    	}
    	
    	if(tokenizer.isComplete()) {
    		view.close();
    		new Notify(lh.localValue("checkComplete"));
    	}
    }
		
	public void addWord(String word){
		String userDictPath = BBIni.getUserProgramDataPath() + BBIni.getFileSep() +"dictionaries" + BBIni.getFileSep() + dictLang + ".dic";
		
		if(fu.exists(userDictPath)){
			sc.addToDictionary(word);
			fu.appendToFile(dictPath, word);
		}
		else {
			String oldPath = dictPath;
			dictPath = BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "dictionaries" + BBIni.getFileSep() + dictLang + ".dic";
			String oldAffPath = affPath;
			affPath = BBIni.getUserProgramDataPath() + BBIni.getFileSep() + "dictionaries" + BBIni.getFileSep() + dictLang + ".aff";
			fu.create(dictPath);
			fu.copyFile(oldPath, dictPath);
			
			fu.create(affPath);
			fu.copyFile(oldAffPath, affPath);
			
			fu.appendToFile(dictPath, word);
			sc.addToDictionary(word);
		}
	}
	
	public void ignoreWord(String word){
		m.newIgnore(word);
		ignoreList.add(word);
	}
	
	public void replace(String text){
		m.getText().copyAndPaste(text, tokenizer.getStartPos(), tokenizer.getEndPos());		
		tokenizer.resetText(m.getText().view.getText().replace("\n", " "));
	}
	
	public void replaceAll(String oldWord, String newWord){
		sc.addToDictionary(newWord);

		if(!oldWord.equals(newWord)){
			Tokenizer tk = new Tokenizer(m.getText().view.getText().replace("\n", " "), tokenizer.getStartPos(), tokenizer.getEndPos());
		
			do{
				if(tk.getCurrentWord().equals(oldWord)){
					m.getText().copyAndPaste(newWord, tk.getStartPos(), tk.getEndPos());
					tk.resetText(m.getText().view.getText().replace("\n", " "));
				}
				tk.next();
			} while(!tk.isComplete());
		
			tokenizer.resetText(m.getText().view.getText().replace("\n", " "));
		}
	}
	
	public void closeSpellChecker(){
		if(sc.isActive())
			sc.close();

		int pos = m.getText().view.getCaretOffset();
		m.getText().view.setSelection(pos, pos);
		m.dispatch(Message.createSetCurrentMessage(Sender.TEXT, pos, false));
	}
}
