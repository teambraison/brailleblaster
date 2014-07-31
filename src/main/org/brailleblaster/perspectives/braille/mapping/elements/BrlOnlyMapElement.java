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
		this.brailleList.add(new BrailleMapElement(n));
		this.parent=parent;
	}
	public String getText(){
		String str = "";
		int num=this.n.getValue().length();
		for (int i=0;i<num;i++)
			str=str+'-';
		return str;
	}
	
	public int textLength(){
		return getText().length();
	}
	
	@Override
	public Element parentElement(){
		return this.parent;
	}
	
	public void setBrailleOffsets(int start, int end){
		this.brailleList.getFirst().start = start;
		this.brailleList.getFirst().end = end;
	}
}
