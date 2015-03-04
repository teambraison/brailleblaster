package org.brailleblaster.perspectives.braille.eventQueue;

import org.brailleblaster.utd.Style;


public class StyleEvent extends Event {
	int listIndex, sectionIndex;
	Style style;
	String config;
	
	public StyleEvent(int listIndex, int sectionIndex, int textOffset, int brailleOffset, Style style, String config){
		super(EventTypes.Style_Change, textOffset, brailleOffset);
		this.listIndex = listIndex;
		this.sectionIndex = sectionIndex;
		this.style = style;
		this.config = config;
	}
	
	public Style getStyle(){
		return style;
	}
	
	public int getListIndex(){
		return listIndex;
	}
}
