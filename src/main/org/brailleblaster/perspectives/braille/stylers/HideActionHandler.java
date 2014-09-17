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
			else if(!(list.getCurrent() instanceof BrlOnlyMapElement))
				hide(list.getCurrent());
			else if(!BBIni.debugging())
				new Notify("In order to hide a boxline both opening and closing boxlines must be selected");
		}
	}
	
	private void hideMultipleElements(){
		int start=text.getSelectedText()[0];
		int end=text.getSelectedText()[1];
		boolean invalid = false;
		
		Set<TextMapElement> itemSet = manager.getElementSelected(start, end);		
		Iterator<TextMapElement> itr = itemSet.iterator();
		
		ArrayList<TextMapElement> addToSet = new ArrayList<TextMapElement>();
		while(itr.hasNext()){
			TextMapElement tempElement= itr.next();
			if(tempElement instanceof BrlOnlyMapElement){
				BrlOnlyMapElement b = list.findJoiningBoxline((BrlOnlyMapElement)tempElement);
				if(b == null || b.start > end || b.end < start){
					invalid = true;
					if(!BBIni.debugging())
						new Notify("In order to hide a boxline both opening and closing boxlines must be selected");
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
		
		if(!invalid){
			itr = itemSet.iterator();
			while (itr.hasNext()) {
				TextMapElement tempElement= itr.next();	
				hide(tempElement);
			}			
		}
	}

	private void hide(TextMapElement t){
		Message m= new Message(null);
		Element parent = getParent(t);
		ArrayList<TextMapElement> itemList = list.findTextMapElements(list.indexOf(t), parent, true);
		m.put("element", parent);
		m.put("type", "action");
		m.put("action", "skip");
	
		int textLength = (itemList.get(itemList.size() - 1).end + 1) - itemList.get(0).start;
		int brailleLength = (itemList.get(itemList.size() - 1).brailleList.getLast().end + 1) - itemList.get(0).brailleList.getFirst().start;
		text.replaceTextRange(itemList.get(0).start, textLength, "");
		braille.replaceTextRange(itemList.get(0).brailleList.getFirst().start, brailleLength, "");
	
		int index = list.indexOf(itemList.get(0));

		for(int i = 0; i < itemList.size(); i++){
			Message message = new Message(null);
			message.put("element", parent);
			tree.removeItem(itemList.get(i), message);
			list.remove(itemList.get(i));
		}
	
		list.shiftOffsetsFromIndex(index, -textLength, -brailleLength, 0);
	
		manager.getDocument().applyAction(m);
		manager.dispatch(Message.createSetCurrentMessage(Sender.TREE, list.get(index).start, false));
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	}
	
	private Element getParent(TextMapElement current) {
		Element parent;
		if(current instanceof PageMapElement || current instanceof BrlOnlyMapElement)
			parent = current.parentElement();
		else
			parent = manager.getDocument().getParent(current.n, true);
		
		return parent;
	}
}
