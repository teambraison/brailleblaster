package org.brailleblaster.wordprocessor;

import java.util.LinkedList;

import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractMapElement;

public class TextMapElement extends AbstractMapElement<BrailleMapElement>{
	LinkedList<BrailleMapElement>brailleList;
	
	public TextMapElement(int offset, Node n) {
		super(offset, n);
		this.brailleList = new LinkedList<BrailleMapElement>();		
	}
}
