package org.brailleblaster.perspectives.braille.spellcheck;

public class Tokenizer {
	
	private String text;
	private int startPos, endPos;
	private boolean complete;
	
	public Tokenizer(String text){
		startPos = 0;
		endPos = 0;
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
		
		while(endPos < text.length() && ((Character.isLetter(text.charAt(endPos))|| Character.isDigit(text.charAt(endPos))) || text.charAt(endPos) == '\'')){
			endPos++;
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
}
