package org.brailleblaster.perspectives.braille.mapping.elements;

import nu.xom.Node;


public class PageMapElement extends AbstractMapElement {

	public int brailleStart, brailleEnd, index, listIndex;
	public Node brailleNode;
	
	public PageMapElement(int start, int end, Node n) {
		super(start, end, n);
	}
	
	public PageMapElement(Node n, int index){
		super(n);
		this.index = index;
		this.listIndex = index;
	}
	
	public void setBraillePage(Node n){
		brailleNode = n;
	}
	
	public void setBraillePage(int brailleStart, int brailleEnd, Node n){
		this.brailleStart = brailleStart;
		this.brailleEnd = brailleEnd;
		brailleNode = n;
	}
	
	public void setBrailleOffsets(int start, int end){
		brailleStart = start;
		brailleEnd = end;
	}
}
