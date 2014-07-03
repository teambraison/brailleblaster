package org.brailleblaster.perspectives.braille.mapping.elements;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;
import nu.xom.Comment;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;

public class SectionElement {
	Element parent;
	MapList list;
	boolean inView;
	int chars;
	
	public SectionElement(Manager m, Element e){
		parent = e;
		list = new MapList(m);
		initializeViews(e, m, 0);
		inView = false;
	}
	
	public SectionElement(Manager m, Element e, int index){
		parent = e;
		list = new MapList(m);
		chars = 0;
		initializeViews(e, m, index);
		inView = false;
	}
	
	protected void initializeViews(Node current, Manager m, int index){
		if(current instanceof Text && !((Element)current.getParent()).getLocalName().equals("brl") && vaildTextElement(current, current.getValue())){
			list.add(new TextMapElement(current));
			chars += list.getLast().textLength();
		}
		
		for(int i = index; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Comment && ((Comment)current.getChild(i)).getValue().contains("BBBOOKMARK")){
				break;
			}
			else if(current.getChild(i) instanceof Element &&  ((Element)current.getChild(i)).getLocalName().equals("brl")){
				//Added to handle brl for side bar
				if (((Element)current.getChild(i).getParent()).getLocalName().equals("sidebar"))
				{
					//text.setBRLOnlyText(list, "\n",((Element)current.getChild(i).getParent()));
					//braille.setBRLOnlyBraille(list,current.getChild(i));
				}
				else
				{
					initializeBraille(m, current.getChild(i), list.getLast());
				}
			}
			else if(current.getChild(i) instanceof Element &&  ((Element)current.getChild(i)).getLocalName().equals("math")){
				//if math is empty skip next brl element
				if(validateMath((Element)current.getChild(i)))
					initializeMathML((Element)current.getChild(i), (Element)current.getChild(i + 1));
				else
					i++;
			}
			//Added this part for side bar
			else if(current.getChild(i) instanceof Element &&  ((Element)current.getChild(i)).getLocalName().equals("sidebar")){
				     initializeViews(current.getChild(i), m, 0);	
			}
			else {
				if(current.getChild(i) instanceof Element){
					if(((Element)current.getChild(i)).getLocalName().equals("pagenum")){
						initializePrintPage(m, (Element)current.getChild(i));
					}
					else {
						Element currentChild = (Element)current.getChild(i);
						m.getDocument().checkSemantics(currentChild);
						if(!currentChild.getLocalName().equals("meta") & !currentChild.getAttributeValue("semantics").contains("skip"))
							initializeViews(currentChild, m, 0);
					}
				}
				else if(!(current.getChild(i) instanceof Element)) {
					initializeViews(current.getChild(i), m, 0);
				}
			}
		}
	}
	
	protected void initializeBraille(Manager m, Node current, TextMapElement t){
		if(current instanceof Text && ((Element)current.getParent()).getLocalName().equals("brl")){
			Element grandParent = (Element)current.getParent().getParent();
			if(!(grandParent.getLocalName().equals("span") && m.getDocument().checkAttributeValue(grandParent, "class", "brlonly")))
				list.getLast().brailleList.add(new BrailleMapElement(current));
		}
		
		for(int i = 0; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Element){
				initializeBraille(m, current.getChild(i), t);
			}
			else {
				initializeBraille(m, current.getChild(i), t);
			}
		}
	}
	
	protected void initializePrintPage(Manager m, Element page){
		Node textNode = m.getDocument().findPrintPageNode(page);
		if(textNode != null){
			list.addPrintPage(new PageMapElement(textNode, list.size()));
		
			Node brailleText = m.getDocument().findBraillePageNode(page);
			list.getLastPage().setBraillePage(brailleText);
		}
	}
	
	protected void initializeMathML(Element math, Element brl){
		list.add(new TextMapElement(math));
	}
	
	protected boolean validateMath(Element math){
		int count = math.getChildCount();
		for(int i = 0; i < count; i++){
			if(math.getChild(i) instanceof Text)
				return true;
			else if(math.getChild(i) instanceof Element){
				if(validateMath((Element)math.getChild(i)))
					return true;
			}
		}
		
		return false;
	}
	
	protected boolean vaildTextElement(Node n , String text){
		Element e = (Element)n.getParent();
		int index = e.indexOf(n);
		int length = text.length();
		
		if(index == e.getChildCount() - 1 || !(e.getChild(index + 1) instanceof Element && ((Element)e.getChild(index + 1)).getLocalName().equals("brl")))
			return false;
		
		for(int i = 0; i < length; i++){
			if(text.charAt(i) != '\n' && text.charAt(i) != '\t')
				return true;
		}
		
		return false;
	}
	
	public void setInView(boolean inView){
		this.inView = inView;
	}
	
	public void resetList(){
		inView = false;
		list.resetList();
	}
	
	public boolean isVisible(){
		return inView;
	}
	
	public MapList getList(){
		return list;
	}
	
	public Element getParent(){
		return parent;
	}
	
	public int getCharCount(){
		return chars;
	}
}
