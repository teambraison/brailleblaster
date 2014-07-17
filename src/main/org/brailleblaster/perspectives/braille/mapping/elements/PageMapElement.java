package org.brailleblaster.perspectives.braille.mapping.elements;

import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Node;


public class PageMapElement extends TextMapElement {

	public int brailleStart, brailleEnd;
	public Node brailleNode;
	private Element parent;
	
	public PageMapElement(int start, int end, Node n, Element parent) {
		super(start, end, n);
		this.parent = parent;
		this.brailleList = new LinkedList<BrailleMapElement>();		
	}
	
	public PageMapElement(Element parent, Node n){
		super(n);
		this.parent = parent;
		this.brailleList = new LinkedList<BrailleMapElement>();		
	}
	
	public void setBraillePage(Node n){
		brailleList.add(new BrailleMapElement(n));
	}
	
	public void setBraillePage(int brailleStart, int brailleEnd, Node n){
		brailleList.add(new BrailleMapElement(brailleStart, brailleEnd, n));
	}
	
	public void setBrailleOffsets(int start, int end){
		brailleList.getFirst().start = start;
		brailleList.getFirst().end = end;
	}
	
	@Override
	public Element parentElement(){
		return parent;
	}
}
