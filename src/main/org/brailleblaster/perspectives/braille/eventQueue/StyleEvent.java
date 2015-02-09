package org.brailleblaster.perspectives.braille.eventQueue;

import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;

public class StyleEvent extends Event {
	int listIndex, sectionIndex;
	Styles style;
	String config;
	
	public StyleEvent(int listIndex, int sectionIndex, int textOffset, int brailleOffset, Styles style, String config){
		super(EventTypes.Style_Change, textOffset, brailleOffset);
		this.listIndex = listIndex;
		this.sectionIndex = sectionIndex;
		this.style = style;
		this.config = config;
	}
	
	public Styles getStyle(){
		return style;
	}
	
	public int getListIndex(){
		return listIndex;
	}
}
