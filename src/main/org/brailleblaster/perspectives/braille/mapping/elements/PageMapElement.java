package org.brailleblaster.perspectives.braille.mapping.elements;

import nu.xom.Node;
import nu.xom.Text;


public class PageMapElement extends AbstractMapElement {

	public int brailleStart, brailleEnd, index, listIndex;
	public Node brailleNode;
	
	public PageMapElement(int start, int end, Node n) {
		super(start, end, n);
	}
	
	public PageMapElement(Node n, int index){
		super(n);
		this.index = index;
		this.listIndex = index;
	}
	
	public void setBraillePage(Node n){
	    Text t=(Text)n;
	    t.setValue(removeWord(n.getValue()));
	    n=(Node)t;
		brailleNode = n;
	}
	
	public void setBraillePage(int brailleStart, int brailleEnd, Node n){
		this.brailleStart = brailleStart;
		this.brailleEnd = brailleEnd;
		brailleNode = n;
	}
	
	public void setBrailleOffsets(int start, int end){
		brailleStart = start;
		brailleEnd = end;
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
}
