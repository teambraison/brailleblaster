package org.brailleblaster.perspectives.braille.eventQueue;

import nu.xom.Element;
import nu.xom.ParentNode;

public class Event {
	EventTypes eventType;
	Element element;
	int listIndex, parentIndex, textOffset, brailleOffset, treeIndex;
	ParentNode parent;
	
	public Event(EventTypes eventType, Element e, int listIndex, int textOffset, int brailleOffset, int treeIndex){
		this.eventType = eventType;
		this.element = (Element)e.copy();
		this.parent = e.getParent();
		this.parentIndex = parent.indexOf(e);
		this.listIndex = listIndex;
		this.textOffset = textOffset;
		this.brailleOffset = brailleOffset;
		this.treeIndex = treeIndex;
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
	
	public int getTreeIndex(){
		return treeIndex;
	}

}
