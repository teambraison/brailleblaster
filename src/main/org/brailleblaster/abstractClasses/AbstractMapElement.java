package org.brailleblaster.abstractClasses;

import nu.xom.Element;
import nu.xom.Node;

public class AbstractMapElement {
	public int start, end;
	public Node n;
	
	public AbstractMapElement(int start, int end, Node n){
		this.start = start;
		this.end = end;
		this.n = n;
	}
	
	public Element parentElement(){
		return (Element)n.getParent();
	}
	
	public String value(){
		return n.getValue();
	}
}
