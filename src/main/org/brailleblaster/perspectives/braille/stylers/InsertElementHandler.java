package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParentNode;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.SemanticFileHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ModelEvent;
import org.brailleblaster.perspectives.braille.mapping.elements.BrailleMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.util.FileUtils;

public class InsertElementHandler extends Handler{

	BrailleDocument doc;
	
	EventFrame frame;
	
	public InsertElementHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);
		
		this.doc = manager.getDocument();
	}
	
	public void insertElement(Message m){
		frame = new EventFrame();
		if(m.getValue("atStart").equals(true))
			insertElementAtBeginning(m);
		else
			insertElementAtEnd(m);
		manager.addUndoEvent(frame);
	}
	
	public void insertElement(EventFrame f){
		frame = new EventFrame();
		while(f.size() > 0 && f.peek().getEventType().equals(EventTypes.Delete)){
			ModelEvent ev= (ModelEvent)f.pop();
			insertElement(ev);
			frame.addEvent(new ModelEvent(EventTypes.Delete, ev.getParent().getChild(ev.getParentIndex()), vi.getStartIndex(), ev.getListIndex(), ev.getTextOffset(), ev.getBrailleOffset(), tree.getItemPath()));
		}
		manager.addRedoEvent(frame);
	}
	
	public void redoInsert(EventFrame f){
		frame = new EventFrame();
		while(f.size() > 0 && f.peek().getEventType().equals(EventTypes.Insert)){
			ModelEvent ev= (ModelEvent)f.pop();
			insertElement(ev);
			frame.addEvent(new ModelEvent(EventTypes.Insert, ev.getParent().getChild(ev.getParentIndex()), vi.getStartIndex(), ev.getListIndex(), ev.getTextOffset(), ev.getBrailleOffset(), tree.getItemPath()));
		}
		manager.addUndoEvent(frame);
	}
	
	private void insertElement(ModelEvent ev){
		ParentNode p = ev.getParent();
		if(ev.getNode() instanceof Text){
			p.insertChild(ev.getNode(), ev.getParentIndex());
			Element brl = new Element("brl");
			brl.appendChild(new Text(""));
			p.insertChild(brl, ev.getParentIndex() + 1);
			((Element)p.getChild(ev.getParentIndex() + 1)).setNamespaceURI(doc.getRootElement().getNamespaceURI());
		}
		else
			p.insertChild(ev.getNode(), ev.getParentIndex());
	
		if(ev.getNode() instanceof Element && ((Element)ev.getNode()).getAttributeValue(SEMANTICS).contains("style")){
			ArrayList<TextMapElement>elList = constructMapElements((Element)ev.getNode(), 0);
		
			if(!list.empty() && ev.getListIndex() > 0 && list.get(ev.getListIndex() - 1).end == ev.getTextOffset())
				insertInList(elList, ev.getListIndex(), ev.getTextOffset() + 1, ev.getBrailleOffset() + 1);
			else
				insertInList(elList, ev.getListIndex(), ev.getTextOffset(), ev.getBrailleOffset());
			
			if(list.size() - 1 != ev.getListIndex() + 1)
				list.shiftOffsetsFromIndex(ev.getListIndex() + 1, 1, 1);
		
			text.insertLineBreak(ev.getTextOffset());
			braille.insertLineBreak(ev.getBrailleOffset());
			tree.rebuildTree(ev.getTreeIndex());
		}
		else {
			ArrayList<TextMapElement>elList;
			if(ev.getNode() instanceof Element)
				elList = constructMapElements((Element)ev.getNode(), 0);
			else
				elList = constructMapElement((Element)ev.getParent(), ev.getParentIndex());
		
			insertInList(elList, ev.getListIndex(), ev.getTextOffset(), ev.getBrailleOffset());
		
			tree.rebuildTree(ev.getTreeIndex());
		}
	
		list.setCurrent(ev.getListIndex());
		text.refreshStyle(list.getCurrent());
		braille.refreshStyle(list.getCurrent());
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	}
	
	public void resetElement(EventFrame f){
		frame = new EventFrame();
		while(!f.empty() && f.peek().getEventType().equals(EventTypes.Hide)){
			resetElement((ModelEvent)f.pop());
		}
		manager.addRedoEvent(frame);
	}
	
	private void resetElement(ModelEvent event){
		if(vi.getStartIndex() != event.getFirstSectionIndex())
			list = vi.resetViews(event.getFirstSectionIndex());
	
		Element replacedElement = replaceElement(event);
		updateSemanticEntry(replacedElement, (Element)event.getNode());
	
		ArrayList<TextMapElement> elList = constructMapElements((Element)event.getNode(), 0);
		setViews(elList, event.getListIndex(), event.getTextOffset(), event.getBrailleOffset());
	
		manager.getTreeView().rebuildTree(event.getTreeIndex());
		manager.dispatch(Message.createSetCurrentMessage(Sender.TREE, list.get(event.getListIndex()).start, false));
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	
		if(!onScreen(event.getTextOffset()))
			setTopIndex(event.getTextOffset());
		
		frame.addEvent(new ModelEvent(EventTypes.Hide, event.getNode(), vi.getStartIndex(), event.getListIndex(), list.get(event.getListIndex()).start, list.get(event.getListIndex()).brailleList.getFirst().start, tree.getItemPath()));
	}
	
	private void insertInList(ArrayList<TextMapElement>elList, int index, int textOffset, int brailleOffset){
		for(int i = 0; i < elList.size(); i++, index++){
			list.add(index, elList.get(i));
			list.get(index).setOffsets(textOffset, textOffset);
			for(int j = 0; j < list.get(index).brailleList.size(); j++)
				list.get(index).brailleList.get(j).setOffsets(brailleOffset, brailleOffset);
		}
	}
	
	private void insertElementAtBeginning(Message m){
		if(list.getCurrentIndex() > 0 && list.getCurrent().start != 0)
			doc.insertElement(vi, list, list.getCurrent(),  list.getCurrent().start - 1, list.getCurrent().brailleList.getFirst().start - 1,list.getCurrentIndex(),(String) m.getValue("elementName"));
		else
			doc.insertElement(vi, list, list.getCurrent(), list.getCurrent().start, list.getCurrent().brailleList.getFirst().start, list.getCurrentIndex(),(String) m.getValue("elementName"));
			
		if(list.size() - 1 != list.getCurrentIndex() - 1){
			if(list.getCurrentIndex() == 0)
				list.shiftOffsetsFromIndex(list.getCurrentIndex() + 1, 1, 1);
			else
				list.shiftOffsetsFromIndex(list.getCurrentIndex(), 1, 1);
		}
		int index = tree.getSelectionIndex();
		
		m.put("length", 1);
		m.put("newBrailleLength", 1);
		m.put("brailleLength", 0);

		braille.insertLineBreak(list.getCurrent().brailleList.getFirst().start - 1);
			
		tree.newTreeItem(list.get(list.getCurrentIndex()), index, 0);
		TextMapElement t = list.get(list.getCurrentIndex());
		frame.addEvent(new ModelEvent(EventTypes.Insert, t.parentElement(), vi.getStartIndex(), list.getCurrentIndex(), t.start, t.brailleList.getFirst().start, tree.getItemPath()));
	}
	
	private void insertElementAtEnd(Message m){
		doc.insertElement(vi, list, list.getCurrent(), list.getCurrent().end + 1, list.getCurrent().brailleList.getLast().end + 1, list.getCurrentIndex() + 1,(String) m.getValue("elementName"));
		if(list.size() - 1 != list.getCurrentIndex() + 1)
			list.shiftOffsetsFromIndex(list.getCurrentIndex() + 2, 1, 1);
		
		int index = tree.getSelectionIndex();
		
		m.put("length", 1);
		m.put("newBrailleLength", 1);
		m.put("brailleLength", 0);

		int newItemIndex = list.getCurrentIndex() + 1;
		braille.insertLineBreak(list.getCurrent().brailleList.getLast().end);
		tree.newTreeItem(list.get(newItemIndex), index, 1);
		TextMapElement t = list.get(newItemIndex);
		frame.addEvent(new ModelEvent(EventTypes.Insert, t.parentElement(), vi.getStartIndex(), newItemIndex, t.start, t.brailleList.getFirst().start, tree.getItemPath()));
	}
	
	private ArrayList<TextMapElement> constructMapElements(Element e, int index){
		ArrayList<TextMapElement> elList = new ArrayList<TextMapElement>();
		if(e.getAttributeValue(SEMANTICS).contains("pagenum"))
			elList.add(makePageMapElement(e));
		else {
			for(int i = index; i < e.getChildCount(); i++){
				if(e.getChild(i) instanceof Text)
					elList.add(new TextMapElement(e.getChild(i)));
				else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && !isBoxLine(e)){
					for(int j = 0; j < e.getChild(i).getChildCount(); j++){
						if(e.getChild(i).getChild(j) instanceof Text)
							elList.get(elList.size() - 1).brailleList.add(new BrailleMapElement(e.getChild(i).getChild(j)));
					}
				}
				else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && isBoxLine(e))
					elList.add(new BrlOnlyMapElement(e.getChild(i), e));
				else if(e.getChild(i) instanceof Element)
					elList.addAll(constructMapElements((Element)e.getChild(i), 0));
			}
		}
		
		return elList;
	}
	
	private ArrayList<TextMapElement> constructMapElement(Element e, int index){
		ArrayList<TextMapElement> elList = new ArrayList<TextMapElement>();
		if(e.getAttributeValue(SEMANTICS).contains("pagenum"))
			elList.add(makePageMapElement(e));
		else {
			for(int i = index; i < index + 2; i++){
				if(e.getChild(i) instanceof Text)
					elList.add(new TextMapElement(e.getChild(i)));
				else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && !isBoxLine(e)){
					for(int j = 0; j < e.getChild(i).getChildCount(); j++){
						if(e.getChild(i).getChild(j) instanceof Text)
							elList.get(elList.size() - 1).brailleList.add(new BrailleMapElement(e.getChild(i).getChild(j)));
					}
				}
				else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && isBoxLine(e))
					elList.add(new BrlOnlyMapElement(e.getChild(i), e));
			}
		}
		
		return elList;
	}
	
	private void setViews(ArrayList<TextMapElement> elList, int index, int textOffset, int brailleOffset ){
		Message m = new Message(null);
		int count = elList.size();
		
		if(shouldInsertBlankLine(elList))
			createBlankLine(textOffset, brailleOffset, index);
		
		for(int i = 0; i < count; i++){
			if(i > 0 && (isBlockElement(elList.get(i)) || afterLineBreak(elList.get(i)))){
				createBlankLine(textOffset, brailleOffset, index);
				textOffset++;
				brailleOffset++;
			}
			
			int brailleLength = 0;
			
			manager.getText().resetElement(m, vi, list, index, textOffset, elList.get(i));
			textOffset = elList.get(i).end;
			
			for(int j = 0; j < elList.get(i).brailleList.size(); j++){
				manager.getBraille().resetElement(m, list, list.get(index), elList.get(i).brailleList.get(j), brailleOffset);
				brailleOffset = (Integer)m.getValue("brailleOffset");
				brailleLength += (Integer)m.getValue("brailleLength");
			}
			
			int textLength =list.get(index).end - list.get(index).start;
			
			textLength = (Integer)m.getValue("textLength");
			textOffset = (Integer)m.getValue("textOffset");
			list.shiftOffsetsFromIndex(index + 1, textLength, brailleLength);
			index++;
		}
	}
	
	private boolean hasId(Element e){
		if(e.getAttribute("id") != null)
			return true;
		else
			return false;
	}
	
	private boolean hasSameSemantics(Element e, Element newElement){
		Attribute sem1 = e.getAttribute(SEMANTICS);
		Attribute sem2 = newElement.getAttribute(SEMANTICS);
		if(sem1.equals(sem2))
			return true;
		else
			return false;
	}
	
	//returns element removed from DOM
	private Element replaceElement(ModelEvent f){
		ParentNode parent = f.getParent();
		Element replacedElement = (Element)parent.getChild(f.getParentIndex());
		parent.replaceChild(replacedElement, f.getNode());
		
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
		String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(manager.getWorkingPath()) + ".sem";
		String id = e.getAttributeValue("id");
		sfh.removeSemanticEntry(file, id);
	}
	
	private void appendSemanticEntry(Element e){
		FileUtils fu = new FileUtils();
		SemanticFileHandler sfh = new SemanticFileHandler(manager.getCurrentConfig());
		String file = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(manager.getWorkingPath()) + ".sem";
		String id = e.getAttributeValue("id");
		sfh.removeSemanticEntry(file, id);
		String [] tokens = e.getAttributeValue(SEMANTICS).split(",");
		sfh.writeEntry(file, tokens[1], e.getLocalName(), id);
	}
	
	private PageMapElement makePageMapElement(Element e){
		Node textNode = doc.findPrintPageNode(e);
		Node brailleNode = doc.findBraillePageNode(e);
		PageMapElement p = new PageMapElement(e, textNode);
		p.setBraillePage(brailleNode);
		return p;
	}
	
	private boolean shouldInsertBlankLine(ArrayList<TextMapElement>elList){
		return elList.get(elList.size() - 1).parentElement().getAttributeValue(SEMANTICS).contains("style") 
				|| firstInLineElement(elList.get(0).parentElement()) || elList.get(0) instanceof PageMapElement || elList.get(0) instanceof BrlOnlyMapElement;
	}
	
	//checks for a rare case if a line break element occurs within a block element
	private boolean afterLineBreak(TextMapElement t){
		if(t instanceof PageMapElement || t instanceof BrlOnlyMapElement)
			return false;
		else if(t.parentElement().indexOf(t.n) > 0){
			int index = t.parentElement().indexOf(t.n);
			if(t.parentElement().getChild(index - 1) instanceof Element && ((Element)t.parentElement().getChild(index - 1)).getLocalName().equals("br"))
				return true;
		}
		
		return false;
	}
	
	private void createBlankLine(int textOffset, int brailleOffset, int index){
		manager.getText().insertText(textOffset, "\n");
		manager.getBraille().insertText(brailleOffset, "\n");
		list.shiftOffsetsFromIndex(index, 1, 1);
	}
	
	private void setTopIndex(int pos){
		int line = manager.getTextView().getLineAtOffset(pos);
		manager.getTextView().setTopIndex(line);
	}
}