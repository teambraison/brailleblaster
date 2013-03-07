package org.brailleblaster.abstractClasses;

import java.util.LinkedList;

import nu.xom.Node;

public class AbstractMapElement<T> {
	public int offset;
	public Node n;
	public LinkedList<T>list;
	
	public AbstractMapElement(int offset, Node n){
		this.offset = offset;
		this.n = n;
	}
}
