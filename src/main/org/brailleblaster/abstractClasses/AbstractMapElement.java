package org.brailleblaster.abstractClasses;

import nu.xom.Node;

public class AbstractMapElement {
	public int offset;
	public Node n;
	
	public AbstractMapElement(int offset, Node n){
		this.offset = offset;
		this.n = n;
	}
}
