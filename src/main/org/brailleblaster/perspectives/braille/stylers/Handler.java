package org.brailleblaster.perspectives.braille.stylers;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

import nu.xom.Attribute;
import nu.xom.Element;

public abstract class Handler {
	protected static final String SEMANTICS = "semantics";
	
	protected static final String BOXLINE = "boxline";
	protected static final String FULLBOX = "fullBox";
	protected static final String TOPBOX = "topBox";
	protected static final String MIDDLEBOX = "middleBox";
	protected static final String BOTTOMBOX = "bottomBox";
	
	protected Manager manager;
	protected MapList list;
	protected ViewInitializer vi;
	protected TextView text;
	protected BrailleView braille;
	protected BBTree tree;

	public Handler(Manager manager, ViewInitializer vi, MapList list){
		this.manager = manager;
		this.vi = vi;
		this.list = list;
		text = manager.getText();
		braille = manager.getBraille();
		tree = manager.getTreeView();
	}

	protected boolean onScreen(int pos){
		int textPos = manager.getText().view.getLineAtOffset(pos) * manager.getText().view.getLineHeight();
		int viewHeight = manager.getText().view.getClientArea().height;
		if(textPos > viewHeight)
			return false;
		
		return true;
	}
	
	protected boolean isBlockElement(TextMapElement t){
		if( t instanceof PageMapElement || t instanceof BrlOnlyMapElement)
			return true;
		else {
			if(t.parentElement().getAttributeValue("semantics").contains("style") && t.parentElement().indexOf(t.n) == 0)
				return true;
			else if(firstInLineElement(t.parentElement()) && t.parentElement().indexOf(t.n) == 0)
				return true;
		}
		return false;
	}
	
	protected boolean firstInLineElement(Element e){
		Element parent = (Element)e.getParent();
		if(parent.getAttribute(SEMANTICS) != null && parent.getAttributeValue(SEMANTICS).contains("style")){
			if(parent.indexOf(e) == 0)
				return true;
		}
		
		return false;
	}
	
	protected boolean isBoxLine(Element e){
		if(checkSemanticsAttribute(e, BOXLINE) || checkSemanticsAttribute(e, TOPBOX) || checkSemanticsAttribute(e, MIDDLEBOX) 
				|| checkSemanticsAttribute(e, BOTTOMBOX) || checkSemanticsAttribute(e, FULLBOX))
			return true;
		else
			return false;
	}
	
	protected boolean isHeading(Element e){
		Attribute atr = e.getAttribute(SEMANTICS);
		
		if(atr != null){
			if(atr.getValue().contains("heading"))
				return true;
		}
		
		return false;
	}
	
	protected boolean isInLine(Element e){
		Attribute atr = e.getAttribute(SEMANTICS);
		if(atr != null){
			String [] tokens = atr.getValue().split(",");
			if(tokens[0].equals("action"))
				return true;
		}
		return false;
	}
	
	protected boolean checkSemanticsAttribute(Element e, String value){
		Attribute atr = e.getAttribute(SEMANTICS);
		
		if(atr == null || !atr.getValue().contains(value))
			return false;
		
		return true;
	}
	
	protected String getSemanticAttribute(Element e){
		Attribute atr = e.getAttribute(SEMANTICS);
		if(atr != null){
			String val = atr.getValue();
			String[] tokens = val.split(",");
			if(tokens.length > 1)
				return tokens[1];
		}
		
		return null;
	}
	
	protected boolean isFirstInList(int index){
		if(index == 0)
			return true;
		else
			return false;
	}
	
	protected boolean isLastInList(int index){
		if(index == list.size() - 1)
			return true;
		else
			return false;
	}
	
	protected boolean readOnly(TextMapElement t){
		return t instanceof BrlOnlyMapElement || t instanceof PageMapElement;
	}
	
	protected boolean readOnly(Element e){
		String sem = getSemanticAttribute(e);
		
		return sem.equals("boxline") || sem.equals("pagenum");
	}
	
}
