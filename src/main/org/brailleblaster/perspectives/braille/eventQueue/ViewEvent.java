package org.brailleblaster.perspectives.braille.eventQueue;

public class ViewEvent extends Event{
	
	String text;
	int textEnd, brailleEnd;
	
	public ViewEvent(EventTypes eventType, int textStart, int textEnd, int brailleOffset, int brailleEnd, String text) {
		
		super(eventType, textStart, brailleOffset);
		this.text = text;
		this.textEnd = textEnd;
		this.brailleEnd = brailleEnd;
	}
	
	public String getText(){
		return text;
	}
	
	public int getTextEnd(){
		return textEnd;
	}
	
	public int getBrailleEnd(){
		return brailleEnd;
	}
}
