package org.brailleblaster.perspectives.braille.mapping.elements;

import nu.xom.Element;
import nu.xom.Node;


public class BrailleMapElement extends AbstractMapElement{
	public boolean pagenum;
	
	public BrailleMapElement(int start, int end, Node n) {
		super(start, end, n);
		pagenum = isPageNum(n);
	}
	
	public BrailleMapElement(Node n){
		super(n);
	}
	
	private boolean isPageNum(Node n){
		Element e = (Element)n.getParent().getParent();
		if(e.getLocalName().equals("span")){
			Element parent = (Element)e.getParent();
			if(parent.getLocalName().equals("brl")){
				Element grandParent = (Element)parent.getParent();
				if(!grandParent.getLocalName().equals("pagenum"))
					return true;
			}
		}
		
		return false;
	}
}
