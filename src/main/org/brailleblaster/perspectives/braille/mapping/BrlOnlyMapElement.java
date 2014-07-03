package org.brailleblaster.perspectives.braille.mapping;

import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;

import nu.xom.Element;
import nu.xom.Node;

public class BrlOnlyMapElement extends TextMapElement {
	Element parent=null;

	public BrlOnlyMapElement(int start, int end, Node n,Element parent) {
		super(start, end, n);
		this.parent=parent;
		// TODO Auto-generated constructor stub
	}
	public String getText(){
		return "";
	}
	
	public int textLength(){
		return 0;
	}
	
	@Override
	public Element parentElement(){
		return this.parent;
	}

}
