package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.ArrayList;

import nu.xom.Node;
import nu.xom.ParentNode;

public class ModelEvent extends Event{
	Node node;
	ParentNode parent;
	ArrayList<Integer> treeIndexes;
	int firstSectionIndex, listIndex, parentIndex;
	
	public ModelEvent(EventTypes eventType, Node node, int firstSectionIndex, int listIndex, 
			int textOffset, int brailleOffset, ArrayList<Integer> treeIndexes) {
		
		super(eventType, textOffset, brailleOffset);
		
		this.node = (Node)node.copy();
		this.parent = node.getParent();
		this.parentIndex = parent.indexOf(node);
		this.firstSectionIndex = firstSectionIndex;
		this.listIndex = listIndex;
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
	
	public int getFirstSectionIndex(){
		return firstSectionIndex;
	}
	
	public int getListIndex(){
		return listIndex;
	}
	
	public ArrayList<Integer> getTreeIndex(){
		return treeIndexes;
	}
}
