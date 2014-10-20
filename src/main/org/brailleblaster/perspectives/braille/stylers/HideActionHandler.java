package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import nu.xom.Element;

import org.brailleblaster.BBIni;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.brailleblaster.util.Notify;

public class HideActionHandler {

	Manager manager;
	MapList list;
	TextView text;
	BrailleView braille;
	BBTree tree;
	
	public HideActionHandler(Manager manager, MapList list){
		this.manager = manager;
		this.list = list;
		text = manager.getText();
		braille = manager.getBraille();
		tree = manager.getTreeView();
	}
	
	public void hideText(){
		if(list.size() > 0 && text.view.getCharCount() > 0){
			if(text.isMultiSelected())
				hideMultipleElements();
			else if(!(list.getCurrent() instanceof BrlOnlyMapElement)){
				int index = list.getCurrentIndex();
				hide(list.getCurrent());
				updateCurrentElement(index);
			}
			else {
				invalidSelection();
			}
		}
	}
	
	private void hideMultipleElements(){
		int start=text.getSelectedText()[0];
		int end=text.getSelectedText()[1];
		boolean invalid = false;
		
		Set<TextMapElement> itemSet = manager.getElementSelected(start, end);		
		Iterator<TextMapElement> itr = itemSet.iterator();
		invalid = checkSelection(itemSet, start, end);
		Integer index = null;
		if(!invalid){
			itr = itemSet.iterator();
			while (itr.hasNext()) {
				TextMapElement tempElement= itr.next();
				if(index == null)
					index = list.indexOf(tempElement);
				hide(tempElement);
			}
			updateCurrentElement(index);
		}
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
		
		int start = getStart(itemList.get(0));
		int end = getEnd(itemList.get(itemList.size() - 1));
		
		int textLength = end - start;	
		text.replaceTextRange(start, end - start, "");
		
		int brailleStart = getBrailleStart(itemList.get(0));
		int brailleEnd = getBrailleEnd(itemList.get(itemList.size() - 1));
		int brailleLength = brailleEnd - brailleStart;
		braille.replaceTextRange(brailleStart, brailleLength, "");
	
		int index = list.indexOf(itemList.get(0));
		for(int i = 0; i < itemList.size(); i++){
			Message message = new Message(null);
			message.put("removeAll", true);
			tree.removeItem(itemList.get(i), message);
			list.remove(itemList.get(i));
		}
	
		list.shiftOffsetsFromIndex(index, -textLength, -brailleLength, 0);
	
		manager.getDocument().applyAction(m);
	}
	
	private int getStart(TextMapElement t){
		int index = list.indexOf(t);
		
		if(index > 0 && list.get(index - 1).end != t.start)
			return list.get(index - 1).end;
		else {
			if(t.start > 0)
				return t.start - 1;
			else
				return t.start;
		}
	}
	
	private int getEnd(TextMapElement t){
		int index = list.indexOf(t);
		
		if(index < (list.size() - 1) && list.get(index + 1).end  > t.start)
			return list.get(index + 1).start - 1;
		else	
			return t.end;
	}
	
	private int getBrailleStart(TextMapElement t){
		int index = list.indexOf(t);
		
		if(index > 0 && list.get(index - 1).brailleList.getLast().end != t.brailleList.getFirst().start)
			return list.get(index - 1).brailleList.getLast().end;
		else{ 
			if(t.start > 0)
				return t.brailleList.getFirst().start - 1;
			else
				return  t.brailleList.getFirst().start;
		}
	}
	
	private int getBrailleEnd(TextMapElement t){
		int index = list.indexOf(t);
		
		if(index < (list.size() - 1) && list.get(index + 1).brailleList.getLast().end  > t.brailleList.getFirst().start)
			return list.get(index + 1).brailleList.getFirst().start - 1;
		else	
			return t.brailleList.getLast().end;
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
			new Notify("In order to hide a boxline both opening and closing boxlines must be selected");
	}
}
