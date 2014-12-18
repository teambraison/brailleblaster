package org.brailleblaster.perspectives.braille.stylers;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

import nu.xom.Attribute;
import nu.xom.Element;

public abstract class Handler {
	protected static final String BOXLINE = "boxline";
	protected static final String FULLBOX = "fullBox";
	protected static final String TOPBOX = "topBox";
	protected static final String MIDDLEBOX = "middleBox";
	protected static final String BOTTOMBOX = "bottomBox";
	
	protected Manager manager;
	protected MapList list;
	protected ViewInitializer vi;
	
	public Handler(Manager manager, ViewInitializer vi, MapList list){
		this.manager = manager;
		this.vi = vi;
		this.list = list;
	}

	protected boolean onScreen(int pos){
		int textPos = manager.getText().view.getLineAtOffset(pos) * manager.getText().view.getLineHeight();
		int viewHeight = manager.getText().view.getClientArea().height;
		if(textPos > viewHeight)
			return false;
		
		return true;
	}
	
	protected boolean isBoxLine(Element e){
		if(checkSemanticsAttribute(e, BOXLINE) || checkSemanticsAttribute(e, TOPBOX) || checkSemanticsAttribute(e, MIDDLEBOX) 
				|| checkSemanticsAttribute(e, BOTTOMBOX) || checkSemanticsAttribute(e, FULLBOX))
			return true;
		else
			return false;
	}
	
	protected boolean checkSemanticsAttribute(Element e, String value){
		Attribute atr = e.getAttribute("semantics");
		
		if(atr == null || !atr.getValue().contains(value))
			return false;
		
		return true;
	}
	
	protected String getStyle(Element e){
		return e.getAttributeValue("semantics").split(",")[1];
	}
}
