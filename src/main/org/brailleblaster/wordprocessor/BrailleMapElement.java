package org.brailleblaster.wordprocessor;

import java.util.LinkedList;

import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractMapElement;

public class BrailleMapElement extends AbstractMapElement<BrailleMapElement>{
	
	//LinkedList<brailleMapElement>nestedList;
	
	public BrailleMapElement(int offset, Node n) {
		super(offset, n);
		this.list = new LinkedList<BrailleMapElement>();
	}

}
