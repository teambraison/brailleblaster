package org.brailleblaster.perspectives.braille.document;

import java.util.ArrayList;

import org.brailleblaster.document.SemanticFileHandler;
import org.brailleblaster.perspectives.braille.mapping.MapList;
import org.brailleblaster.perspectives.braille.mapping.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class ElementDivider {
	BrailleDocument doc;
	BBSemanticsTable table;
	SemanticFileHandler handler;
	String attribute;
	int nodeIndex;
	int index;
	
	public ElementDivider(BrailleDocument doc, BBSemanticsTable table, SemanticFileHandler handler){
		this.doc = doc;
		this.table = table;
		this.handler = handler;
	}
	
	public ArrayList<Element>split(MapList list, TextMapElement t, Message m){
		ArrayList<Element>els = new ArrayList<Element>();
		Element e = t.parentElement();
		Element parent = (Element)e.getParent();
		setGlobalVariables(parent, e, t);
		
		int position = (Integer)m.getValue("position");
		Element firstElement = createFirstElement(t, parent, e, position);
		Element secondElement = createSecondElement(t, parent, e, position);
		
		if(attribute.equals("action")){
			while(parent.getAttributeValue("semantics").contains("action")){
				parent = (Element)parent.getParent();
			}
			
			Element grandParent = (Element)parent.getParent();
			int parentIndex = grandParent.indexOf(parent);
			checkElementStyle(parent, firstElement, secondElement);
			replaceElement(els, grandParent, parent, firstElement);
			insertElement(els, grandParent, secondElement, parentIndex + 1);
		}
		else if(e.getChildCount() > 2 && nodeIndex > 0){
			checkElementStyle(e, firstElement, secondElement);
			replaceElement(els, parent, e, firstElement);
			insertElement(els, parent, secondElement, index + 1);
		}
		else {
			checkElementStyle(e, firstElement, secondElement);
			replaceElement(els, parent, e, firstElement);
			insertElement(els, parent,secondElement, index + 1);
		}
		
		return els;
	}
	
	public ArrayList<Element>split(ArrayList<TextMapElement>elList, MapList list, Message m){
		ArrayList<Element>els = new ArrayList<Element>();
		
		TextMapElement t1 = elList.get(0);
		TextMapElement t2 = elList.get(1);
		
		Element e = (Element)t1.n.getParent();
		Element parent = (Element)e.getParent();
		setGlobalVariables(parent, e, t1);
		Element firstElement = createFirstElement(t1, parent, e, t1.textLength());
		
		e = (Element)t2.n.getParent();
		parent = (Element)e.getParent();
		setGlobalVariables(parent, e, t2);
		Element secondElement = createSecondElement(t2, parent, e, 0);
		
		if(m.getValue("atStart").equals(true)){
			e = (Element)t2.n.getParent();
			parent = (Element)e.getParent();
		}
		else{
			e = (Element)t1.n.getParent();
			parent = (Element)e.getParent();
		}
		
		while(parent.getAttributeValue("semantics").contains("action")){
			parent = (Element)parent.getParent();
		}

		Element grandParent = (Element)parent.getParent();
		int parentIndex = grandParent.indexOf(parent);

		checkElementStyle(parent, firstElement, secondElement);
		
		replaceElement(els, grandParent, parent, firstElement);
		insertElement(els, grandParent, secondElement, parentIndex + 1);
		
		return els;
	}
	
	private void checkElementStyle(Element parent, Element firstElement, Element secondElement){
		if(!isDefaultStyle(parent)){
			firstElement.addAttribute(new Attribute("id", parent.getAttributeValue("id")));
			Message styleMessage = new Message(null);
			styleMessage.put("Style", table.get(table.getKeyFromAttribute(parent)));
			doc.changeSemanticAction(styleMessage, secondElement);
		}
	}
	
	private Element createFirstElement(TextMapElement t, Element parent, Element e, int textPosition){
		Element firstHalf = makeElementWithTextNode(e, t, 0, textPosition);				
		
		if(attribute.equals("action")){
			Element p = doc.makeElement(parent.getLocalName(), "semantics", parent.getAttributeValue("semantics"));
			copyElements(parent, p, 0, index);
			p.appendChild(firstHalf);
			
			if(parent.getAttributeValue("semantics").contains("action"))
				p = copyBlockElement((Element)parent.getParent(), parent, p, true);
			
			return p;
		}
		else if(e.getChildCount() > 2 && nodeIndex > 0){
			Element p = doc.makeElement(e.getLocalName(), "semantics", e.getAttributeValue("semantics"));
			copyElements(e, p, 0, nodeIndex);
			p.appendChild(firstHalf.removeChild(0));
			
			return p;
		}
		
		return firstHalf;
	}
	
	private Element createSecondElement(TextMapElement t, Element parent, Element e, int textPosition){	
		Element secondHalf = makeElementWithTextNode(e, t, null, textPosition);
				
		if(attribute.equals("action")){
			Element p = doc.makeElement(parent.getLocalName(), "semantics", parent.getAttributeValue("semantics"));
			p.appendChild(secondHalf);
			copyElements(parent, p, index + 1, parent.getChildCount());
			if(parent.getAttributeValue("semantics").contains("action"))
				p = copyBlockElement((Element)parent.getParent(), parent, p, false);
			
			return p;
		}
		else if(e.getChildCount() > 2 && nodeIndex >= 0){
			copyElements(e, secondHalf, nodeIndex + 2, e.getChildCount());
		}
		
		return secondHalf;
	}
	
	//Replaces element in the DOM and adds it to element list to be returned
	private void replaceElement(ArrayList<Element>list, Element parent, Element replace, Element replacement){
		replacement = doc.translateElement(replacement);
		parent.replaceChild(replace, replacement);
		list.add(replacement);
	}
	
	//Inserts element into DOM and adds it to the element list to be returned
	private void insertElement(ArrayList<Element>list, Element parent, Element e, int index){
		e = doc.translateElement(e);
		parent.insertChild(e, index);
		list.add(e);
	}
	
	//Creates the base element with the partial text from the node being split
	private Element makeElementWithTextNode(Element e, TextMapElement t, Integer start, int position){
		Element splitElement = doc.makeElement(e.getLocalName(), "semantics", e.getAttributeValue("semantics"));
		if(start != null)
			insertTextNode(splitElement, t.value().substring(0, position));
		else
			insertTextNode(splitElement, t.value().substring(position));
		
		return splitElement;
	}
	
	//Traverses up the DOM to copy the entire block element before or after the given inline element
	private Element copyBlockElement(Element parent, Element child, Element childCopy, boolean before){
		int index;
		Element localParent = doc.makeElement(parent.getLocalName(), "semantics", parent.getAttributeValue("semantics"));
		if(childCopy != null)
			localParent.appendChild(childCopy);
		
		while(parent.getAttributeValue("semantics").contains("action")){
			child = parent;
			parent = (Element)parent.getParent();
			Element temp = doc.makeElement(parent.getLocalName(), "semantics", parent.getAttributeValue("semantics"));
			temp.appendChild(localParent);
			localParent = temp;
		}
		
		index = parent.indexOf(child);
		if(before && index != -1){
			for(int i = 0; i < index; i++){
				localParent.insertChild(parent.getChild(i).copy(), parent.indexOf(parent.getChild(i)));
	//			localParent.appendChild(parent.getChild(i).copy());
			}
		}
		else if(index != -1) {
			for(int i = index + 1; i < parent.getChildCount(); i++){
				localParent.appendChild(parent.getChild(i).copy());
			}
		}
		return localParent;
	}
	
	//Copies additional nodes and elements at the same depth in the DOM
	private void copyElements(Element parent, Element e, int startIndex, int lastIndex){
		for(int i = startIndex; i < lastIndex; i++){
			if(!(parent.getChild(i) instanceof Element && ((Element)parent.getChild(i)).getLocalName().equals("brl"))){
				Node temp = parent.getChild(i).copy();
				temp.toXML().toString();
				e.appendChild(temp);
			}
		}
	}
	
	private void insertTextNode(Element e, String text){
		if(text != null)
			e.appendChild(new Text(text));
		else
			e.appendChild(new Text(""));
	}
	
	private void setGlobalVariables(Element parent, Element e, TextMapElement t){
		attribute = table.getSemanticTypeFromAttribute(e);
		nodeIndex = e.indexOf(t.n);
		index = parent.indexOf(e);
	}
	
	private boolean isDefaultStyle(Element e){
		String style = table.getKeyFromAttribute(e);

		if(style.equals(handler.getDefault(e.getLocalName())))
			return true;
		else
			return false;
	}
}
