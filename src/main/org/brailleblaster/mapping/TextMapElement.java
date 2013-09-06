package org.brailleblaster.mapping;

import java.util.LinkedList;

import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractMapElement;

public class TextMapElement extends AbstractMapElement {
	public LinkedList<BrailleMapElement>brailleList;
	
	public TextMapElement(int start, int end, Node n) {
		super(start, end, n);
		this.brailleList = new LinkedList<BrailleMapElement>();		
	}
	
	public void setNode(Node n){
		this.n = n;
	}
	
	public int textLength(){
		return n.getValue().length();
	}
	
	public int brailleLength(){
		if(brailleList.size() == 0)
			return -1;
		else
			return brailleList.getLast().end - brailleList.getFirst().start;
	}
}
