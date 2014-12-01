package org.brailleblaster.perspectives.braille.stylers;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class ElementRemover {
	Manager manager;
	MapList list;
	TextView text;
	BrailleView braille;
	BBTree tree;
	ViewInitializer vi;
	EventFrame eventFrame;
	
	public ElementRemover(Manager manager, MapList list, ViewInitializer vi){
		this.manager = manager;
		this.list = list;
		this.vi = vi;
		text = manager.getText();
		braille = manager.getBraille();
		tree = manager.getTreeView();
	}

	public void removeNode(Message m){
		int index = (Integer)m.getValue("index");
		if(list.get(index).isMathML() )
			removeMathMLElement(m);
		else
			removeElement(m);
	}
	
	private void removeElement(Message message){
		int index = (Integer)message.getValue("index");
		tree.removeItem(list.get(index), message);
		manager.getDocument().updateDOM(list, message);
		list.get(index).brailleList.clear();
		vi.remove(list, index);
					
		if(list.size() == 0){
			text.removeListeners();
			braille.removeListeners();
			tree.removeListeners();
			list.clearList();
			text.view.setEditable(false);
		}
	}
	
	private void removeMathMLElement(Message m){
		int index = (Integer)m.getValue("index");
		TextMapElement t = list.get(index);
		m.put("start", t.start);
		manager.getDocument().updateDOM(list, m);
		braille.removeMathML(t);
		text.removeMathML(m);
		tree.removeMathML(t);
		index = list.indexOf(t);
		list.updateOffsets(index, m);
		vi.remove(list, index);
		
		if(list.size() == 0){
			text.removeListeners();
			braille.removeListeners();
			tree.removeListeners();
			list.clearList();
			text.view.setEditable(false);
		}
	}
}
