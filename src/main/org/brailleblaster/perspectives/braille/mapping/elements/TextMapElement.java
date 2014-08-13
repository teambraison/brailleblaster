package org.brailleblaster.perspectives.braille.mapping.elements;

import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Node;


public class TextMapElement extends AbstractMapElement {
	public LinkedList<BrailleMapElement>brailleList;
	
	public TextMapElement(int start, int end, Node n) {
		super(start, end, n);
		this.brailleList = new LinkedList<BrailleMapElement>();		
	}
	
	public TextMapElement(Node n){
		super(n);
		this.brailleList = new LinkedList<BrailleMapElement>();		
	}
	
	public void setNode(Node n){
		this.n = n;
	}
	
	public String getText(){
		return n.getValue();
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
	
	public boolean isMathML(){
		if(n instanceof Element && ((Element) n).getLocalName().equals("math")){
			return true;
		}
		else
			return false;
	}
}
