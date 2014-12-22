package org.brailleblaster.perspectives.braille.eventQueue;

public class ViewEvent extends Event{
	
	public ViewEvent(EventTypes eventType, int textOffset, int brailleOffset) {
		
		super(eventType, textOffset, brailleOffset);
	}
}
