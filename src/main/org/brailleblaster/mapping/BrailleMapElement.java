package org.brailleblaster.mapping;

import nu.xom.Element;
import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractMapElement;

public class BrailleMapElement extends AbstractMapElement{
	public boolean pagenum;
	
	public BrailleMapElement(int start, int end, Node n) {
		super(start, end, n);
		pagenum = isPageNum(n);
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
