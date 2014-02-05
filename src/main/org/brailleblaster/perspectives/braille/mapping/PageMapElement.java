package org.brailleblaster.perspectives.braille.mapping;

import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractMapElement;

public class PageMapElement extends AbstractMapElement {

	public int brailleStart, brailleEnd;
	public Node brailleNode;
	
	public PageMapElement(int start, int end, Node n) {
		super(start, end, n);
	}
	
	public void setBraillePage(int brailleStart, int brailleEnd, Node n){
		this.brailleStart = brailleStart;
		this.brailleEnd = brailleEnd;
		brailleNode = n;
	}
	
}
