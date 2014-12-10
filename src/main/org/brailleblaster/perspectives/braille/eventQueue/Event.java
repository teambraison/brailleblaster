package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.ArrayList;

import nu.xom.Node;
import nu.xom.ParentNode;

public class Event {
	EventTypes eventType;
	Node node;
	int firstSectionIndex, listIndex, parentIndex, textOffset, brailleOffset;
	ArrayList<Integer> treeIndexes;
	ParentNode parent;
	
	public Event(EventTypes eventType, Node node, int firstSectionIndex, int listIndex, int textOffset, int brailleOffset, ArrayList<Integer> treeIndexes){
		this.eventType = eventType;
		this.node = (Node)node.copy();
		this.parent = node.getParent();
		this.parentIndex = parent.indexOf(node);
		this.firstSectionIndex = firstSectionIndex;
		this.listIndex = listIndex;
		this.textOffset = textOffset;
		this.brailleOffset = brailleOffset;
		this.treeIndexes = treeIndexes;
	}
	
	public Node getNode(){
		return node;
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
	
	public EventTypes getEventType(){
		return eventType;
	}
}
