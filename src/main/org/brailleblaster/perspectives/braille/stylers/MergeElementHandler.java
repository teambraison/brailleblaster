package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ModelEvent;
import org.brailleblaster.perspectives.braille.mapping.elements.BrailleMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.messages.Message;

public class MergeElementHandler extends Handler{
	BrailleDocument document;
	EventFrame frame;
	
	public MergeElementHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);
		document = manager.getDocument();
	}
	
	public void merge(TextMapElement t1, TextMapElement t2){
		frame = new EventFrame();
		Element mergeTo = getBlockElement(t1.n);
		Element merging = getBlockElement(t2.n);
		
		ArrayList<TextMapElement> mergeList1 = list.findTextMapElements(list.indexOf(t1), mergeTo, true);
		ArrayList<TextMapElement> mergeList2 = list.findTextMapElements(list.indexOf(t2), merging, true);
		saveOriginalElements(mergeTo, mergeList1.get(0), merging, mergeList2.get(0));
		
		int startIndex = list.indexOf(mergeList1.get(0));
		int textStart = mergeList1.get(0).start;
		int brailleStart = mergeList1.get(0).brailleList.getFirst().start;
				
		Element mergedElement = document.mergeElements(mergeTo, merging);
		clearListRange(startIndex, mergeList1.size() + mergeList2.size());
		int size = repopulateRange(mergedElement, startIndex);

		list.setCurrent(list.getCurrentIndex());
		clearViewRanges(mergeList1.get(0), mergeList2.get(mergeList2.size() - 1), startIndex + size);	
	
		ArrayList<TextMapElement>textList = getListRange(startIndex, size);	
		setViews(textList,startIndex, textStart, brailleStart);
		resetTree(mergeTo, merging, mergedElement, textList);
		text.setCurrentElement(text.view.getCaretOffset());
	
		createUndoEvent();
	}
	
	public void undoMerge(EventFrame f){
		frame = new EventFrame();
		removeMergedElement((ModelEvent)f.peek());
		
		while(f.size() > 0 && f.peek().getEventType().equals(EventTypes.Merge))
			resetViews( (ModelEvent)f.pop() );
		
		manager.addRedoEvent(frame);
	}
	
	public void redoMerge(EventFrame f){
		frame = new EventFrame();
		removeOriginalElement((ModelEvent)f.peek());
		
		while(f.size() > 0 && f.peek().getEventType().equals(EventTypes.Merge))
			resetViews( (ModelEvent)f.pop() );	
		
		createUndoEvent();
	}
	
	private void removeMergedElement(ModelEvent ev){
		if(ev.getEventType().equals(EventTypes.Merge)){
			Element e = getBlockElement(list.get(ev.getListIndex()).n); 
			ArrayList<TextMapElement> elList = list.findTextMapElements(ev.getListIndex(), e, true);
			frame.addEvent(new ModelEvent(EventTypes.Merge, e, vi.getStartIndex(), list.indexOf(elList.get(0)), elList.get(0).start, elList.get(0).brailleList.getFirst().start, tree.getItemPath()));
			int startIndex = list.indexOf(elList.get(0));
			clearListRange(startIndex, elList.size());
			clearViewRanges(elList.get(0), elList.get(elList.size() - 1), startIndex);
			removeTreeItem(elList.get(0), e);
			e.getParent().removeChild(e);		
		}
	}
	
	private void removeOriginalElement(ModelEvent ev){
		if(ev.getEventType().equals(EventTypes.Merge)){		
			Element mergeTo = document.getParent(list.get(ev.getListIndex()).n, true);
			Element merging = (Element)mergeTo.getParent().getChild(mergeTo.getParent().indexOf(mergeTo) + 1);
			
			ArrayList<TextMapElement> mergeList1 = list.findTextMapElements(ev.getListIndex(), mergeTo, true);
			ArrayList<TextMapElement> mergeList2 = list.findTextMapElements(list.indexOf(mergeList1.get(mergeList1.size() - 1)) + 1, merging, true);			
			saveOriginalElements(mergeTo, mergeList1.get(0), merging, mergeList2.get(0));
			
			int index = ev.getListIndex();
			Element e = document.getParent(list.get(index).n, true);
			int startIndex = list.indexOf(mergeList1.get(0));			
			list.setCurrent(list.getCurrentIndex());
			clearListRange(startIndex, mergeList1.size() + mergeList2.size());
			clearViewRanges(mergeList1.get(0), mergeList2.get(mergeList2.size() - 1), startIndex);	
		
			removeTreeItem(mergeList2.get(0), merging);
			e.getParent().removeChild(merging);
			
			removeTreeItem(mergeList1.get(0), merging);
			e.getParent().removeChild(mergeTo);
		}
	}
	
	private void setViews(ArrayList<TextMapElement> elList, int index, int textOffset, int brailleOffset ){
		Message m = new Message(null);
		int count = elList.size();
		
		for(int i = 0; i < count; i++){
			int brailleLength = 0;
			
			text.mergeElement(m, vi, list, index, textOffset, elList.get(i));
			textOffset = elList.get(i).end;
			
			for(int j = 0; j < elList.get(i).brailleList.size(); j++){
				braille.mergeElement(m, list, list.get(index), elList.get(i).brailleList.get(j), brailleOffset);
				brailleOffset = (Integer)m.getValue("brailleOffset");
				brailleLength += (Integer)m.getValue("brailleLength");
			}
			
			int textLength = list.get(index).end - list.get(index).start;
			
			textLength = (Integer)m.getValue("textLength");
			textOffset = (Integer)m.getValue("textOffset");
			list.shiftOffsetsFromIndex(index + 1, textLength, brailleLength);
			index++;
		}
	}
	
	private void resetViews(ModelEvent ev){
		ev.getParent().insertChild(ev.getNode(), ev.getParentIndex());
		int size = repopulateRange((Element)ev.getNode(), ev.getListIndex());
		ArrayList<TextMapElement>textList = getListRange(ev.getListIndex(), size);	
		setViews(textList, ev.getListIndex(), ev.getTextOffset(), ev.getBrailleOffset());
		tree.rebuildTree(ev.getTreeIndex());
		text.setCurrentElement(ev.getTextOffset());		
	}
	
	private void resetTree(Element e1, Element e2, Element newParent, ArrayList<TextMapElement>textList){
		ArrayList<Element>elList = new ArrayList<Element>();
		elList.add(e1);
		elList.add(e2);
		
		for(int i = 0; i < textList.size(); i++){
			if(!textList.get(i).parentElement().equals(newParent)){
				textList.remove(i);
				i--;
			}
		}
		
		tree.merge(textList, elList);
	}
	
	private Element getBlockElement(Node n){
		return document.getParent(n, true);
	}
	
	private void clearListRange(int start, int length){
		for(int i = 0; i < length; i++)
			vi.remove(list, start);
	}
	
	private int repopulateRange(Element e, int index){
		ArrayList<TextMapElement> elList = 	constructMapElement(e);
		for(int i = 0; i < elList.size(); i++, index++)
			vi.addElementToSection(list, elList.get(i), index);
		
		return elList.size();
	}
	
	private ArrayList<TextMapElement> constructMapElement(Element e){
		ArrayList<TextMapElement> elList = new ArrayList<TextMapElement>();
		
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				elList.add(new TextMapElement(e.getChild(i)));
			}
			else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && !isBoxLine(e)){
				for(int j = 0; j < e.getChild(i).getChildCount(); j++){
					if(e.getChild(i).getChild(j) instanceof Text){
						elList.get(elList.size() - 1).brailleList.add(new BrailleMapElement(e.getChild(i).getChild(j)));
					}
				}
			}
			else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && isBoxLine(e))
				elList.add(new BrlOnlyMapElement(e.getChild(i), e));
			else if(e.getChild(i) instanceof Element)
				elList.addAll(constructMapElement((Element)e.getChild(i)));
		}
		
		return elList;
	}
	
	private void clearViewRanges(TextMapElement first, TextMapElement last, int index){
		int start = first.start;
		int end = last.end;
		
		int brailleStart = first.brailleList.getFirst().start;
		int brailleEnd = last.brailleList.getLast().end;
		
		text.replaceTextRange(start, end - start, "");
		braille.replaceTextRange(brailleStart, brailleEnd - brailleStart, "");
		
		list.shiftOffsetsFromIndex(index, -(end - start), -(brailleEnd - brailleStart));
	}
	
	private ArrayList<TextMapElement>getListRange(int start, int length){
		ArrayList<TextMapElement>textList = new ArrayList<TextMapElement>();
		for(int i = start; i < start + length; i++)
			textList.add(list.get(i));
		
		return textList;
	}
	
	private void createUndoEvent(){
		if(manager.peekUndoEvent() != null && manager.peekUndoEvent().peek().getEventType().equals(EventTypes.Whitespace)){
			while(!frame.empty())
				manager.peekUndoEvent().addEvent(frame.push());
		}
		else {
			manager.addUndoEvent(frame);
		}
	}
	
	private void removeTreeItem(TextMapElement t, Element e){
		Message m = new Message(null);
		m.put("element", e);
		tree.removeItem(t, m);
	}
	
	private void saveOriginalElements(Element mergeTo, TextMapElement t1, Element merging, TextMapElement t2 ){
		int pos = text.view.getCaretOffset();
		frame.addEvent(new ModelEvent(EventTypes.Merge, merging, vi.getStartIndex(), list.indexOf(t2), t2.start, t2.brailleList.getFirst().start, tree.getItemPath()));
		text.setCurrentElement(t1.start);
		frame.addEvent(new ModelEvent(EventTypes.Merge, mergeTo, vi.getStartIndex(), list.indexOf(t1), t1.start, t1.brailleList.getFirst().start, tree.getItemPath()));
		text.setCurrentElement(pos);
	}
}
