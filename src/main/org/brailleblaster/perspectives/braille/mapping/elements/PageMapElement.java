package org.brailleblaster.perspectives.braille.mapping.elements;

import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;


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
	    Text t=(Text)n;
	    t.setValue(removeWord(n.getValue()));
	    brailleList.add(new BrailleMapElement(brailleStart, brailleEnd, n));
	}
	
	public void setBraillePage(int brailleStart, int brailleEnd, Node n){
		brailleList.add(new BrailleMapElement(brailleStart, brailleEnd, n));
	}
	
	public void setBrailleOffsets(int start, int end){
		brailleList.getFirst().start = start;
		brailleList.getFirst().end = end;
	}
	
	//Remove word page or any word between ";" and "#" character
	private String removeWord(String str){
		int startRemove =0;
		int endRemove=0;		
		if((str.contains(";")) && (str.contains("#"))){
			startRemove=str.indexOf(";");
			endRemove=str.indexOf("#");
			String removedString=str.substring(startRemove+1, endRemove);
			str=str.replace(removedString, "");
			
		}
		return str;
		
	}
	
	@Override
	public Element parentElement(){
		return parent;
	}
}
