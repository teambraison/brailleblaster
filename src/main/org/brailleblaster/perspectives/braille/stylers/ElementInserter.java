package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.ParentNode;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.document.SemanticFileHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.eventQueue.Event;
import org.brailleblaster.perspectives.braille.mapping.elements.BrailleMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.util.FileUtils;

public class ElementInserter {

	BBDocument doc;
	MapList list;
	Manager manager;
	ViewInitializer vi;
	
	public ElementInserter(ViewInitializer vi, BBDocument doc, MapList list, Manager manager){
		this.vi = vi;
		this.doc = doc;
		this.list = list;
		this.manager = manager;
	}
	
	public void resetElement(Event f){
		Element replacedElement = replaceElement(f);
		updateSemanticEntry(replacedElement, f.getElement());
		
		ArrayList<TextMapElement> elList = constructMapElements(f.getElement());
		setViews(elList, f.getListIndex(), f.getTextOffset(), f.getBrailleOffset());
		
		restoreTree(elList, f.getTreeIndex());
		manager.dispatch(Message.createSetCurrentMessage(Sender.TREE, manager.getText().view.getCaretOffset(), false));
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	}
	
	private ArrayList<TextMapElement> constructMapElements(Element e){
		ArrayList<TextMapElement> elList = new ArrayList<TextMapElement>();
		
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				elList.add(new TextMapElement(e.getChild(i)));
			}
			else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl")){
				for(int j = 0; j < e.getChild(i).getChildCount(); j++){
					if(e.getChild(i).getChild(j) instanceof Text){
						elList.get(elList.size() - 1).brailleList.add(new BrailleMapElement(e.getChild(i).getChild(j)));
					}
				}
			}
			else if(e.getChild(i) instanceof Element){
				elList.addAll(constructMapElements((Element)e.getChild(i)));
			}
		}
		
		return elList;
	}
	
	private void setViews(ArrayList<TextMapElement> elList, int index, int textOffset, int brailleOffset ){
		if(elList.get(elList.size() - 1).parentElement().getAttributeValue("semantics").contains("style") || firstInLineElement(elList.get(0).parentElement())){
			manager.getText().insertText(textOffset, "\n");
			manager.getBraille().insertText(brailleOffset, "\n");
			list.shiftOffsetsFromIndex(index, 1, 1, 0);
		}
		Message m = new Message(null);
		int count = elList.size();
		
		for(int i = 0; i < count; i++){
			int brailleLength = 0;
		//	manager.getText().resetElement(m, vi, list, index, textOffset, elList.get(i));
			textOffset = elList.get(i).end;
			
			for(int j = 0; j < elList.get(i).brailleList.size(); j++){
		//		manager.getBraille().resetElement(m, list, list.get(index), elList.get(i).brailleList.get(j), brailleOffset);
				brailleOffset = elList.get(i).brailleList.get(j).end;
				brailleLength += (Integer)m.getValue("brailleLength");
			}
			
			int textLength =list.get(index).end - list.get(index).start;
			
			textLength = (Integer)m.getValue("textLength");
			list.shiftOffsetsFromIndex(index + 1, textLength, brailleLength, 0);
			index++;
		}

		for(int i = 0; i < list.size(); i++)
			System.out.println(list.get(i).getText() + " " + list.get(i).start + " " + list.get(i).end + " " + list.get(i).brailleList.getFirst().start + " " + list.get(i).brailleList.getLast().end);
		
		System.out.println();
	}
	
	private void restoreTree(ArrayList<TextMapElement>elList, int index){
		Element parent = elList.get(0).parentElement();
		for(int i = 1; i < elList.size(); i++){
			if(!elList.get(i).parentElement().equals(parent)){
				elList.remove(elList.get(i));
				i--;
			}
		}
		manager.getTreeView().newTreeItem(elList, index, 0);
	}
	
	private boolean hasId(Element e){
		if(e.getAttribute("id") != null)
			return true;
		else
			return false;
	}
	
	private boolean hasSameSemantics(Element e, Element newElement){
		Attribute sem1 = e.getAttribute("semantics");
		Attribute sem2 = newElement.getAttribute("semantics");
		if(sem1.equals(sem2))
			return true;
		else
			return false;
	}
	
	private boolean firstInLineElement(Element e){
		Element parent = (Element)e.getParent();
		if(parent.getAttribute("semantics") != null && parent.getAttributeValue("semantics").contains("style")){
			if(parent.indexOf(e) == 0)
				return true;
		}
		
		return false;
	}
	
	//returns element removed from DOM
	private Element replaceElement(Event f){
		ParentNode parent = f.getParent();
		Element replacedElement = (Element)parent.getChild(f.getParentIndex());
		parent.replaceChild(replacedElement, f.getElement());
		
		return replacedElement;
	}
	
	private void updateSemanticEntry(Element replacedElement, Element elementToInsert){
		if((hasId(replacedElement) && !hasId(elementToInsert) && !hasSameSemantics(replacedElement, elementToInsert)))
			removeSemanticEntry(replacedElement);
		else if(hasId(replacedElement) && hasId(elementToInsert) && !hasSameSemantics(replacedElement,elementToInsert))
			appendSemanticEntry(elementToInsert);
	}
	
	private void removeSemanticEntry(Element e){
		FileUtils fu = new FileUtils();
		SemanticFileHandler sfh = new SemanticFileHandler(manager.getCurrentConfig());
		String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(manager.getDocumentName()) + ".sem";
		String id = e.getAttributeValue("id");
		//sfh.removeSemanticEntry(file, id);
	}
	
	private void appendSemanticEntry(Element e){
		FileUtils fu = new FileUtils();
		SemanticFileHandler sfh = new SemanticFileHandler(manager.getCurrentConfig());
		String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(manager.getDocumentName()) + ".sem";
		String id = e.getAttributeValue("id");
	//	sfh.removeSemanticEntry(file, id);
		String [] tokens = e.getAttributeValue("semantics").split(",");
		sfh.writeEntry(file, tokens[1], e.getLocalName(), id);
	}
}
