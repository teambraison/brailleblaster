package org.brailleblaster.perspectives.braille.mapping.elements;

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
	
	public AbstractMapElement(Node n){
		this.n = n;
	}
	
	public Element parentElement(){
		return (Element)n.getParent();
	}
	
	public String value(){
		return n.getValue();
	}
	
	public void setOffsets(int start, int end){
		this.start = start;
		this.end = end;
	}
}
