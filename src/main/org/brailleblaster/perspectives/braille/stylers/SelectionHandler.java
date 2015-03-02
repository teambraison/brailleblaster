package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;
import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParentNode;
import nu.xom.Text;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.elements.BrailleMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class SelectionHandler extends Handler {

    public SelectionHandler(Manager manager, ViewInitializer vi, MapList list) {
        super(manager, vi, list);
    }
    
    public void removeSelection(Message m){
    	int startPos = (Integer)m.getValue("start");
        int endPos = (Integer)m.getValue("end");
        String replacementText = (String)m.getValue("replacementText");
        int startIndex = getIndex(startPos);
        int endIndex = getIndex(endPos);
        Element firstEl = getBlockElement(startIndex);
        Element lastEl = getBlockElement(endIndex);
        boolean emptyNode = false;
        LinkedList<Integer>nodes = null;
        ArrayList<TextMapElement> firstList = getBlockMapElements(startIndex, firstEl);
     	TextMapElement first = list.get(startIndex);
     	TextMapElement last = list.get(endIndex);
     	int textStart = first.start;
     	int brailleStart = first.brailleList.getFirst().start;
     	
        if(firstEl.equals(lastEl)){ 
         	if(!readOnly(first)){
         		if(startPos != first.end){
         			if(first.start > startPos){
         				if(endPos >= first.end){
         					clearText(first);
         					emptyNode = true;
         					nodes = nodeIndexes(first.n, firstEl);
         				}
         				else
         					updateElement(first, endPos, first.end, replacementText);
         			}
         			else
         				updateElement(first, first.start, startPos, replacementText);
         		}
         		
         		list.setCurrent(endIndex);
         		if(!first.equals(last))
         			updateElement(last, endPos, last.end, "");
         	}
         	
         	for(int i = startIndex + 1; i < endIndex; i++)
         		removeElement(i);
           
         	if(textStart > startPos){
         		brailleStart -= textStart - startPos;
         		textStart = startPos;
         	}
         	
         	clearViewRanges(textStart, brailleStart, last, startIndex, endPos - startPos, endPos, replacementText);
         	ArrayList<Integer> indexes = tree.getItemPath();
         	removeTreeItem(startIndex, firstEl);
         	clearListItems(firstList);
         	
         	ArrayList<TextMapElement> mapList;
         	if(emptyNode){
         		Element e = firstEl;
         		
         		while(nodes.size() > 0)
             		e = (Element)e.getChild(nodes.remove());
         		
         		mapList = recreateElement(e, startIndex);
         	}
         	else
         		mapList = recreateElement(firstEl, startIndex);
         	
         	list.setCurrent(startIndex);
         	setViews(mapList,startIndex, textStart, brailleStart);
         	tree.rebuildTree(indexes);
     		text.setCurrentElement(text.view.getCaretOffset());
         }
         else {
        	boolean clearAll = false;	 
            ArrayList<TextMapElement> lastList = getBlockMapElements(endIndex, lastEl);
         	
         	if(list.indexOf(first) == 0 && list.indexOf(last) == list.size() - 1){
         		if(endPos >= last.end)
         			clearAll = true;
         	}    		
         	
         	if(!readOnly(first)){
         		if(startPos != first.end || replacementText.length() > 0){
         			if(first.start > startPos && replacementText.length() == 0){
         				if(endPos > first.end){
         					clearText(first);
         					emptyNode = true;
     						nodes = nodeIndexes(first.n, firstEl);
         				}
         				else {
         					updateElement(first, endPos, first.end, replacementText);
         				}
         			}
         			else {
         				if(startPos < first.start)
         					updateElement(first, first.start, first.start, replacementText);
         				else
         					updateElement(first, first.start, startPos, replacementText);
         			}
         		}
         		int removed = clearElement(list.indexOf(first) + 1, list.indexOf(firstList.get(firstList.size() - 1)) + 1);
         		endIndex -= removed;
         	}
         	
         	 if(startIndex > list.indexOf(firstList.get(0))){
             		startIndex = list.indexOf(firstList.get(0));
             		textStart = list.get(startIndex).start;
             		brailleStart = list.get(startIndex).brailleList.getFirst().start;
         	 }
                 
         	ArrayList<TextMapElement> readOnly = new ArrayList<TextMapElement>();
         	if(list.indexOf(firstList.get(firstList.size() - 1)) != list.indexOf(lastList.get(0)) - 1){
         		readOnly = removeElements(list.indexOf(firstList.get(firstList.size() - 1)) + 1, list.indexOf(lastList.get(0))); 
         		endIndex = endIndex - readOnly.size();
         		for(int j = 0; j < readOnly.size(); j++){
         			if(!(readOnly.get(j) instanceof BrlOnlyMapElement) && !(readOnly.get(j) instanceof PageMapElement)){
         				readOnly.remove(j);
         				j--;
         			}
         				
         		}
         	}
         	
         	if(!readOnly(last) && !clearAll){
         		list.setCurrent(endIndex);
         		updateElement(last, endPos, last.end, "");	
         		int removed = clearElement(list.indexOf(lastList.get(0)), list.indexOf(last));
         		endIndex -= removed;
         	}
         	else if(clearAll){
         		removeElement(list.indexOf(last));
         	}
         	
         	 if(endIndex < list.indexOf(lastList.get(lastList.size() - 1)))
          		endIndex = list.indexOf(lastList.get(lastList.size() - 1));
         	 
         	 if(textStart > startPos){
         		brailleStart -= textStart - startPos;
          		textStart = startPos;
          	}
         	
         	clearViewRanges(textStart, brailleStart, list.get(endIndex), startIndex, endPos - startPos, endPos, replacementText);
         	ArrayList<Integer> indexes = tree.getItemPath();
         	list.setCurrent(endIndex);
         	ArrayList<Integer> indexes2 = tree.getItemPath();
         	list.setCurrent(startIndex);
         	tree.setSelection(list.getCurrent());
         	clearListItems(firstList);
         	
         	clearListItems(lastList);	
         	ArrayList<TextMapElement>mapList = new ArrayList<TextMapElement>(); 
         	if(readOnly(first)){
         		mapList.add(first);
         		repopulateReadOnly(mapList, startIndex);
         	}
         	else {
         		if(emptyNode){
             		Element e = firstEl;
             		
             		while(nodes.size() > 0)
                 		e = (Element)e.getChild(nodes.remove());
             		
             		mapList = recreateElement(e, startIndex);
             	}
             	else
             		mapList.addAll(recreateElement(firstEl, startIndex));
         	}
         	
         	if(readOnly.size() > 0){
         		repopulateReadOnly(readOnly, startIndex + mapList.size());
         		mapList.addAll(readOnly);
         	}
         	
         	int pos = startIndex + mapList.size();
         	if(readOnly(last)){
         		mapList.add(last);
         		repopulateReadOnly(last, pos);
         	}
         	else if(!clearAll)
         		mapList.addAll(recreateElement(lastEl, pos));
         	
         	if(!list.empty())
         		list.setCurrent(startIndex);
         	
         	setViews(mapList,startIndex, textStart, brailleStart);
         	         	
         	tree.rebuildTree(indexes);
         	if(!clearAll)
         		tree.rebuildTree(indexes2); 
         	
         	text.setCurrentElement(startPos);
         }
    }
    
    private ArrayList<TextMapElement> recreateElement(Element e, int startIndex){  
    	e = replaceElement(e);
    	ArrayList<TextMapElement>textList;
    	if(e.getChildCount() == 0){
    		textList = new ArrayList<TextMapElement>();
    		Text node = new Text("");
    		e.insertChild(node, 0);
    		TextMapElement t = new TextMapElement(0, 0, node);
    		Element brl = new Element("brl");
    		Text textNode = new Text("");
    		brl.appendChild(textNode);
    		e.appendChild(brl);
    		t.brailleList.add(new BrailleMapElement(0,0, textNode));
    		textList.add(t);
    		vi.addElementToSection(list, t, startIndex);
    	}
    	else {
    		int size = repopulateRange(e, startIndex);
    		textList = getListRange(startIndex, size);
    	}
    	return textList;
    }
    
    private void updateElement(TextMapElement t, int start, int end, String replacementText){
    	int offset = start - t.start;
    	if(end < start)
    		start = 0;
    	
    	int linebreaks;
    	if(start != end)
    		linebreaks = (t.end - t.start) - t.textLength();
    	else
    		linebreaks = 0;
    	
    	String newText = t.getText().substring(offset, offset + (end - start) - linebreaks) + replacementText;
    	Text textNode = (Text)t.n;
    	textNode.setValue(newText);    	
    }
    
    private void clearText(TextMapElement t){
    	Text textNode = (Text)t.n;
    	textNode.setValue(""); 
    }
    
    private int clearElement(int startIndex, int endIndex){
    	int removed = 0;
    	for(int i = startIndex; i < endIndex; i++){
    		removeElement(i);
    		removed++;
    	}
    	
    	return removed;
    }
    
    private Element replaceElement(Element e){
    	Element newEl = manager.getDocument().translateElement(e);
    	ParentNode p = e.getParent();
    	p.replaceChild(e, newEl);
    	return newEl;
    }
    
    private void removeElement(int listIndex){
    	TextMapElement t = list.get(listIndex);
    	if(!readOnly(t)){
    		Element e = t.parentElement();
   			Message m = Message.createRemoveNodeMessage(listIndex, t.end - t.start);
   			manager.getDocument().updateDOM(list, m);
    		if(e.getChildCount() == 0){
    			if(e.getAttributeValue("semantics").contains("action")){
    				if(e.getParent().getChildCount() == 1)
    					e = (Element)e.getParent();
    			}
   				e.getParent().removeChild(e);
    		}
    	}
    }
    
    private ArrayList<TextMapElement> removeElements(int start, int end){
    	int index = start;
    	ArrayList<TextMapElement> elList = new ArrayList<TextMapElement>();
    	while(index < end){
    		if(!readOnly(list.get(index))){
    			Element e = getBlockElement(index);
    			elList.addAll(getBlockMapElements(index, e)); 		
    			removeTreeItem(index, e);
    			index = list.indexOf(elList.get(elList.size() - 1)) + 1;
    		}
    		else {
    			elList.add(list.get(index));
    			index++;
    		}
    			
    	}
    	
    	for(int i = 0; i < elList.size(); i++)
    		removeElement(list.indexOf(elList.get(i)));
    	
    	clearListItems(elList);
    	return elList;
    }
    
    private void removeTreeItem(int index, Element e){
    	Message message = new Message(null);
    	message.put("element", e);
    	message.put("removeAll", true);
    	tree.removeItem(list.get(index), message);
    }
    
    private void clearListItems(ArrayList<TextMapElement> elList){   
    	for(int i = 0; i < elList.size(); i++)
    		vi.remove(list, list.indexOf(elList.get(i)));
    }
    
	private void clearViewRanges(int start, int brailleStart, TextMapElement last, int index, int length, int endPos, String replacementText){
		int end = last.end;
		int brailleEnd = last.brailleList.getLast().end;
		if(endPos > end){
			brailleEnd += endPos - end;
			end = endPos;
		}
	
		text.replaceTextRange(start, (end - length) - start + replacementText.length(), "");
		braille.replaceTextRange(brailleStart, brailleEnd - brailleStart, "");
		list.shiftOffsetsFromIndex(index, -(end - start), -(brailleEnd - brailleStart));
	}
	
	private void setViews(ArrayList<TextMapElement> elList, int index, int textOffset, int brailleOffset ){
		Message m = new Message(null);
		int count = elList.size();
		
		for(int i = 0; i < count; i++){
			int brailleLength = 0;
			
			text.resetSelectionElement(m, vi, list, index, textOffset, elList.get(i));
			textOffset = elList.get(i).end;
			
			for(int j = 0; j < elList.get(i).brailleList.size(); j++){
				braille.resetSelectionElement(m, list, list.get(index), elList.get(i).brailleList.get(j), brailleOffset);
				brailleOffset = (Integer)m.getValue("brailleOffset");
				brailleLength += (Integer)m.getValue("brailleLength");
			}
			
			//int textLength = list.get(index).end - list.get(index).start;		
			int textLength = (Integer)m.getValue("textLength");
			textOffset = (Integer)m.getValue("textOffset");
			list.shiftOffsetsFromIndex(index + 1, textLength, brailleLength);
			index++;
		}
	}
    
    private int repopulateRange(Element e, int index){
		ArrayList<TextMapElement> elList = 	constructMapElement(e);
		for(int i = 0; i < elList.size(); i++, index++)
			vi.addElementToSection(list, elList.get(i), index);
		
		return elList.size();
	}
    
    private void repopulateReadOnly(ArrayList<TextMapElement>elList, int index){
    	for(int i = 0; i < elList.size(); i++, index++)
    		repopulateReadOnly(elList.get(i), index);
    }
	
    private void repopulateReadOnly(TextMapElement t, int index){
    	t.setOffsets(0, 0);
    	t.brailleList.get(0).setOffsets(0, 0);
    	vi.addElementToSection(list, t, index);
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
	
	private ArrayList<TextMapElement>getListRange(int start, int length){
		ArrayList<TextMapElement>textList = new ArrayList<TextMapElement>();
		for(int i = start; i < start + length; i++)
			textList.add(list.get(i));
		
		return textList;
	}
    
    private int getIndex(int pos){
        Message m = new Message(null);
        m.put("offset", pos);
        m.put("selection", manager.getTreeView().getSelection(list.getCurrent()));
        return list.findClosest(m, 0, list.size() - 1);
    }
    
    private Element getBlockElement(int index){
        return manager.getDocument().getParent(list.get(index).n, true);
    }
    
    private ArrayList<TextMapElement> getBlockMapElements(int index, Element el){
        return list.findTextMapElements(index, el, true);
    }
    
    private LinkedList<Integer> nodeIndexes(Node n, Element parent){
    	LinkedList<Integer> nodes = new LinkedList<Integer>();
    	ParentNode  e = n.getParent();
    	
    	while(!e.equals(parent)){
    		n = e;
    		e = e.getParent();
    		nodes.push(e.indexOf(n));
    	}
    	
    	return nodes;
    }
}