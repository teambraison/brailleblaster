package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.ArrayList;

import nu.xom.Node;

public class SelectionEvent extends ModelEvent{
	boolean insert, format;
	int selStart, selEnd;
	public SelectionEvent(EventTypes eventType, Node node,
			int firstSectionIndex, int listIndex, int textOffset,
			int brailleOffset, ArrayList<Integer> treeIndexes, boolean insert, boolean format, int startPos, int endPos) {
		super(eventType, node, firstSectionIndex, listIndex, textOffset, brailleOffset,
				treeIndexes);
		this.insert = insert;
		this.format = format;
		this.selStart = startPos;
		this.selEnd = endPos;
	}

	public boolean insert(){
		return insert;
	}
	
	public boolean format(){
		return format;
	}
	
	public int getSelectionStart(){
		return selStart;
	}
	
	public int getSelectionEnd(){
		return selEnd;
	}
}
