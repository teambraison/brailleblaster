package org.brailleblaster.perspectives.braille.mapping.elements;

import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Node;

public class BrlOnlyMapElement extends TextMapElement {
	Element parent=null;

	public BrlOnlyMapElement(int start, int end, Node n,Element parent) {
		super(start, end, n);
		this.parent=parent;
		// TODO Auto-generated constructor stub
	}
	public BrlOnlyMapElement(Node n,Element parent){
		super(n);
		this.brailleList = new LinkedList<BrailleMapElement>();
		this.parent=parent;
	}
	public String getText(){
		return "/n";
	}
	
	public int textLength(){
		return 1;
	}
	
	@Override
	public Element parentElement(){
		return this.parent;
	}

}
