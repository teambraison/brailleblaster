package org.brailleblaster.mapping;

import java.util.LinkedList;

import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractMapElement;

public class TextMapElement extends AbstractMapElement {
	public LinkedList<BrailleMapElement>brailleList;
	
	public TextMapElement(int offset, Node n) {
		super(offset, n);
		this.brailleList = new LinkedList<BrailleMapElement>();		
	}
}
