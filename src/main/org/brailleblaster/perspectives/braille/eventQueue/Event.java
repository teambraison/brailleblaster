package org.brailleblaster.perspectives.braille.eventQueue;

public class Event {
	EventTypes eventType;
	
	int textOffset, brailleOffset;
	
	public Event(EventTypes eventType, int textOffset, int brailleOffset){
		this.eventType = eventType;		
	
		this.textOffset = textOffset;
		this.brailleOffset = brailleOffset;
	}
	
	public int getTextOffset(){
		return textOffset;
	}
	
	public int getBrailleOffset(){
		return brailleOffset;
	}
	
	public EventTypes getEventType(){
		return eventType;
	}
}
