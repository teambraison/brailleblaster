package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.ParentNode;

public class Event {
	EventTypes eventType;
	Element element;
	int firstSectionIndex, listIndex, parentIndex, textOffset, brailleOffset;
	ArrayList<Integer> treeIndexes;
	ParentNode parent;
	
	public Event(EventTypes eventType, Element e, int firstSectionIndex, int listIndex, int textOffset, int brailleOffset, ArrayList<Integer> treeIndexes){
		this.eventType = eventType;
		this.element = (Element)e.copy();
		this.parent = e.getParent();
		this.parentIndex = parent.indexOf(e);
		this.firstSectionIndex = firstSectionIndex;
		this.listIndex = listIndex;
		this.textOffset = textOffset;
		this.brailleOffset = brailleOffset;
		this.treeIndexes = treeIndexes;
	}
	
	public Element getElement(){
		return element;
	}
	
	public ParentNode getParent(){
		return parent;
	}
	
	public int getParentIndex(){
		return parentIndex;
	}
	
	public int getListIndex(){
		return listIndex;
	}
	
	public int getTextOffset(){
		return textOffset;
	}
	
	public int getBrailleOffset(){
		return brailleOffset;
	}
	
	public ArrayList<Integer> getTreeIndex(){
		return treeIndexes;
	}
	
	public int getFirstSectionIndex(){
		return firstSectionIndex;
	}
}
