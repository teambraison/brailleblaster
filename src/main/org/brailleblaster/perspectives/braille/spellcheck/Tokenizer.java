package org.brailleblaster.perspectives.braille.spellcheck;

public class Tokenizer {
	
	private String text;
	private int startPos, endPos, splitPos;
	private boolean complete, capFlag;
	
	public Tokenizer(String text){
		startPos = 0;
		endPos = 0;
		capFlag = false;
		this.text = text.replaceAll("\u2019", "'");
		complete = false;
	}
	
	public Tokenizer(String text, int startPos, int endPos){
		this.startPos = startPos;
		this.endPos = endPos;
		this.text = text.replaceAll("\u2019", "'");
		complete = false;
	}
	
	private void setStartPos(){
		if(endPos > 0)
			startPos = endPos + 1;
		
		while(startPos < text.length() && (!Character.isLetter(text.charAt(startPos)) && !Character.isDigit(text.charAt(startPos)))){
			startPos++;
		}
	}
	
	private void setEndPos(){
		endPos = startPos;
		splitPos = 0;
		String punctuation = ".,;:?!"; //List of punctuation marks that require a space after
		String sentenceEnd = ".?!"; //List of punctuation that ends a sentence
		
		while(endPos < text.length() && ((Character.isLetter(text.charAt(endPos))|| Character.isDigit(text.charAt(endPos))) || text.charAt(endPos) == '\'')){
			endPos++;
			if(endPos+2 < text.length()){
				if(punctuation.contains(Character.toString(text.charAt(endPos)))){ //We are at a punctuation mark.
					if(text.charAt(endPos+1) != ' '){
						//If the next character isn't a space, something might be wrong
						if(text.charAt(endPos+2) != '.'){
							//If it is initials, ignore it
							if(text.charAt(endPos+1) != '"' && text.charAt(endPos+1) != '\'' && text.charAt(endPos+1) != '”'){
								//If it is the end of a quote, ignore it
								endPos++; //Set endPos past the period - user likely forgot a space
								splitPos = endPos - startPos; //Denotes location of period for SpellCheckManager
							//}
							}
						}
					} else { //If it is a space, the following character needs to be capitalized
						if(sentenceEnd.contains(Character.toString(text.charAt(endPos)))){ //Make sure it isn't a comma
							if(Character.isLowerCase(text.charAt(endPos+2))){
									capFlag = true;
							}
						}
					}
				}
			}
		}
	}
	
	public void resetText(String text){
		this.text = text.replaceAll("\u2019", "'");
		setEndPos();
	}
	
	public String getCurrentWord(){
		return text.substring(startPos, endPos);
	}
	
	public boolean next(){
		setStartPos();
		setEndPos();
		
		if(startPos < text.length())
			return true;
		else {
			complete = true;
			return false;
		}
	}

	public boolean isComplete(){
		return complete;
	}
	
	public int getStartPos(){
		return startPos;
	}
	
	public int getEndPos(){
		return endPos;
	}
	
	public int getSplitPos(){
		return splitPos;
	}
	
	public void setCapFlag(boolean newFlag){
		capFlag = newFlag;
	}
	
	public boolean getCapFlag(){
		return capFlag;
	}
}
