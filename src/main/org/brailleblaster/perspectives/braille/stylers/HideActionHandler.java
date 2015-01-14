package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import nu.xom.Element;

import org.brailleblaster.BBIni;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ModelEvent;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class HideActionHandler extends Handler{	
	
	EventFrame eventFrame;
	boolean boxlineAdded;
	
	public HideActionHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);
	}
	
	public void hideText(){
		eventFrame = new EventFrame();
		if(list.size() > 0 && text.view.getCharCount() > 0){
			if(text.isMultiSelected()){
				boolean valid = hideMultipleElements();
				if(valid)
					manager.addUndoEvent(eventFrame);
			}
			else if(!(list.getCurrent() instanceof BrlOnlyMapElement)){
				hideSingleElement();
				manager.addUndoEvent(eventFrame);
			}
			else 
				invalidSelection();
		}
	}
	
	public void hideText(EventFrame f){
		eventFrame = new EventFrame();
		while(!f.empty() && f.peek().getEventType().equals(EventTypes.Hide)){
			ModelEvent ev = (ModelEvent)f.pop();
			manager.dispatch(Message.createSetCurrentMessage(Sender.TREE, list.get(ev.getListIndex()).start, false));
		
			//resets selection to recreate hide event by user
			if(list.getCurrent() instanceof BrlOnlyMapElement){
				BrlOnlyMapElement b = list.findJoiningBoxline((BrlOnlyMapElement)list.getCurrent());
				int start =list.getCurrent().start;
				int end = b.end;
				text.setCurrentSelection(start, end);
			}
		
			redoHide();
		}
		manager.addUndoEvent(eventFrame);
	}
	
	private void redoHide(){
		if(list.size() > 0 && text.view.getCharCount() > 0){
			if(text.isMultiSelected())
				hideMultipleElements();
			else if(!(list.getCurrent() instanceof BrlOnlyMapElement))
				hideSingleElement();
			else 
				invalidSelection();
		}
	}
	
	private void hideSingleElement(){
		text.update(false);
		int index = list.getCurrentIndex();
		boxlineAdded = false;
		hide(list.getCurrent());
		updateCurrentElement(index);
	}
	
	private boolean hideMultipleElements(){
		int start=text.getSelectedText()[0];
		int end=text.getSelectedText()[1];
		boolean invalid = false;
		
		Set<TextMapElement> itemSet = manager.getElementInSelectedRange(start, end);		
		Iterator<TextMapElement> itr = itemSet.iterator();
		invalid = checkSelection(itemSet, start, end);
		Integer index = null;
		if(!invalid){
			itr = itemSet.iterator();
			boxlineAdded = false;
			while (itr.hasNext()) {
				TextMapElement tempElement= itr.next();
				if(index == null)
					index = list.indexOf(tempElement);
				
				if(BBIni.getPlatformName().equals("gtk"))
					checkLinuxTree(tempElement);
				
				hide(tempElement);
			}
			text.clearSelection();
			updateCurrentElement(index);
			return true;
		}
		
		return invalid;
	}
	
	private void updateCurrentElement(int index){
		if(index >= list.size())
			manager.dispatch(Message.createSetCurrentMessage(Sender.TREE, list.get(index - 1).start, false));
		else
			manager.dispatch(Message.createSetCurrentMessage(Sender.TREE, list.get(index).start, false));
		
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	}

	/** Helper method for hideMultipleElemetns method to check whether selection is valid
	 * @param itemSet : set containing elements in selection
	 * @param start : start of selection
	 * @param end : end of selection
	 * @return true if valid selection, false if invalid
	 */
	private boolean checkSelection(Set<TextMapElement> itemSet, int start, int end){
		boolean invalid = false;
		ArrayList<TextMapElement> addToSet = new ArrayList<TextMapElement>();
		Iterator<TextMapElement> itr = itemSet.iterator();
		while(itr.hasNext()){
			TextMapElement tempElement= itr.next();
			if(tempElement instanceof BrlOnlyMapElement){
				BrlOnlyMapElement b = list.findJoiningBoxline((BrlOnlyMapElement)tempElement);
				if(b == null || b.start > end || b.end < start){
					invalid = true;
					invalidSelection();
					break;
				}
				else if(!itemSet.contains(b))
					addToSet.add(b);
			}
		}
		
		if(addToSet.size() > 0){
			for(int i = 0; i < addToSet.size(); i++)
				itemSet.add(addToSet.get(i));
		}
		
		return invalid;
	}
	
	private void hide(TextMapElement t){
		Message m = new Message(null);
		Element parent = getParent(t);
		ArrayList<TextMapElement> itemList = list.findTextMapElements(list.indexOf(t), parent, true);
		m.put("element", parent);
		m.put("type", "action");
		m.put("action", "skip");
		//int treeIndex = manager.getTreeView().getTree().getSelection()[0].getParentItem().indexOf(manager.getTreeView().getTree().getSelection()[0]);
		ArrayList<Integer> treeIndexes = tree.getItemPath();
		boolean collapseBefore =  collapseSpaceBefore(itemList.get(0));
		boolean collapseAfter = collapseSpaceAfter(itemList.get(itemList.size() - 1));
		
		int start = getStart(itemList.get(0), collapseBefore);
		int end = getEnd(itemList.get(itemList.size() - 1), collapseAfter);
		
		int textLength = end - start;	
		text.replaceTextRange(start, end - start, "");
		
		int brailleStart = getBrailleStart(itemList.get(0), collapseBefore);
		int brailleEnd = getBrailleEnd(itemList.get(itemList.size() - 1), collapseAfter);
		int brailleLength = brailleEnd - brailleStart;
		braille.replaceTextRange(brailleStart, brailleEnd - brailleStart, "");
		
		int index = list.indexOf(itemList.get(0));
		
		int startPos, brailleStartPos;
		int lastIndex = list.indexOf(itemList.get(itemList.size() - 1));
		if(collapseSpaceBefore(itemList.get(0)) && index != 0){
			if(lastIndex == list.size() - 1){
				startPos = text.view.getCharCount() + 1;
				brailleStartPos = braille.view.getCharCount() + 1;
			}
			else {
				startPos =  list.get(lastIndex + 1).start - textLength;
				brailleStartPos =  list.get(lastIndex + 1).brailleList.getFirst().start - brailleLength;
			}
		}
		else {
			startPos = itemList.get(0).start;
			brailleStartPos = itemList.get(0).brailleList.getFirst().start;
		}
		
		if(!boxlineAdded)
			eventFrame.addEvent(new ModelEvent(EventTypes.Hide, parent, vi.getStartIndex(), list.indexOf(itemList.get(0)),  startPos, brailleStartPos, treeIndexes));
		
		if(parent.getLocalName().equals("sidebar"))
			boxlineAdded = true;
		
		for(int i = 0; i < itemList.size(); i++){
			Message message = new Message(null);
			message.put("removeAll", true);
			if(i == itemList.size() - 1 && removeParent(itemList.get(i)))
				message.put("element", itemList.get(i).parentElement().getParent());
			tree.removeItem(itemList.get(i), message);
			vi.remove(list, list.indexOf(itemList.get(i)));
		}
	
		list.shiftOffsetsFromIndex(index, -textLength, -brailleLength);
	
		manager.getDocument().applyAction(m);
	}
	
	private int getStart(TextMapElement t, boolean collapse){
		int index = list.indexOf(t);
		
		if(index > 0 && list.get(index - 1).end != t.start){
			int linesBefore= t.start - list.get(index - 1).end;
			if(linesBefore > 1 & !collapse)
				return list.get(index - 1).end + 1;
			else
				return list.get(index - 1).end;
		}
		else {
			if(t.start > 0)
				return t.start - 1;
			else
				return t.start;
		}
	}
	
	private int getEnd(TextMapElement t, boolean collapse){
		int index = list.indexOf(t);
		
		if(index < (list.size() - 1) && list.get(index + 1).start  > t.end){
			int linesAfter =  list.get(index + 1).start - t.end;
			if(linesAfter > 1 && !collapse)
				return t.end;
			else
				return list.get(index + 1).start - 1;
		}
		else {	
			if(isLastInList(index))
				return text.view.getCharCount();
			else
				return t.end;
		}
	}
	
	private int getBrailleStart(TextMapElement t, boolean collapse){
		int index = list.indexOf(t);
		
		if(index > 0 && list.get(index - 1).brailleList.getLast().end != t.brailleList.getFirst().start){
			int linesBefore = t.brailleList.getFirst().start -   list.get(index - 1).brailleList.getLast().end ;
			if(linesBefore > 1 && !collapse)
				return list.get(index - 1).brailleList.getLast().end + 1;
			else
				return list.get(index - 1).brailleList.getLast().end;
		}
		else{ 
			if(t.start > 0)
				return t.brailleList.getFirst().start - 1;
			else
				return  t.brailleList.getFirst().start;
		}
	}
	
	private int getBrailleEnd(TextMapElement t, boolean collapse){
		int index = list.indexOf(t);
		
		if(index < (list.size() - 1) && list.get(index + 1).brailleList.getFirst().start  > t.brailleList.getLast().end){
			int linesAfter = list.get(index + 1).brailleList.getFirst().start  - t.brailleList.getLast().end;
			if(linesAfter > 1 && !collapse)
				return t.brailleList.getLast().end;
			else
				return list.get(index + 1).brailleList.getFirst().start - 1;
		}
		else {
			if(isLastInList(list.indexOf(t)))
				return manager.getBrailleView().getCharCount();
			else
				return t.brailleList.getLast().end;
		}
	}
	
	private Element getParent(TextMapElement current) {
		Element parent;
		if(current instanceof PageMapElement || current instanceof BrlOnlyMapElement)
			parent = current.parentElement();
		else
			parent = manager.getDocument().getParent(current.n, true);
		
		return parent;
	}
	
	private void invalidSelection(){
		if(!BBIni.debugging())
			manager.notify("In order to hide a boxline both opening and closing boxlines must be selected");
	}
	
	private boolean collapseSpaceBefore(TextMapElement t){
		int index = list.indexOf(t);
		if(isFirstInList(index))
			return true;
		else {
			if(isHeading(manager.getDocument().getParent(t.n, true))){
				if(list.get(index -  1) instanceof PageMapElement)
					return true;
				
				Element prevParent = manager.getDocument().getParent(list.get(index - 1).n, true);
				String sem = getSemanticAttribute(prevParent);
				if(sem != null && !manager.getStyleTable().get(sem).contains(StylesType.linesAfter))
					return true;
			}
		}
		return false;
	}
	
	private boolean collapseSpaceAfter(TextMapElement t){
		int index = list.indexOf(t);
		if(isLastInList(index))
			return true;
		else {
			if(isHeading(manager.getDocument().getParent(t.n, true))){
				TextMapElement nextItem =  list.get(index + 1);
				Element nextParent;
				
				if(nextItem instanceof BrlOnlyMapElement)
					return true;
				else
					nextParent = manager.getDocument().getParent(nextItem.n, true);
				
				String sem = getSemanticAttribute(nextParent);
				if(sem.equals("skip") || sem != null && !manager.getStyleTable().get(sem).contains(StylesType.linesBefore))
					return true;
			}
		}
		
		return false;
	}
	
	private boolean removeParent(TextMapElement t){
		if(!(t instanceof PageMapElement) && isInLine(t.parentElement()))
			return true;
		else
			return false;
	}
	
	private void checkLinuxTree(TextMapElement t){
		if(!boxlineAdded && !(t instanceof BrlOnlyMapElement)){
			if(tree.getTree().getSelection().length == 0)
				tree.setSelection(t);
		}
	}
}