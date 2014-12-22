package org.brailleblaster.perspectives.braille.eventQueue;

public class ViewEvent extends Event{
	
	String text;
	public ViewEvent(EventTypes eventType, int textOffset, int brailleOffset, String text) {
		
		super(eventType, textOffset, brailleOffset);
		this.text = text;
	}
	
	public String getText(){
		return text;
	}
}
