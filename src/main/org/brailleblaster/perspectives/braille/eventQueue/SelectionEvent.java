package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.ArrayList;

import nu.xom.Node;

public class SelectionEvent extends ModelEvent{
	boolean insert;
	public SelectionEvent(EventTypes eventType, Node node,
			int firstSectionIndex, int listIndex, int textOffset,
			int brailleOffset, ArrayList<Integer> treeIndexes, boolean insert) {
		super(eventType, node, firstSectionIndex, listIndex, textOffset, brailleOffset,
				treeIndexes);
		this.insert = insert;
	}

	public boolean insert(){
		return insert;
	}
}
