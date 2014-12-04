package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.eventQueue.Event;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
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
		addEvent(m);
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
		
		if(emptyList())
			disableViews();
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
		
		if(emptyList())
			disableViews();
	}
	
	private void addEvent(Message message){
		int index = (Integer)message.getValue("index");
		ArrayList<Integer>treeIndex = tree.getItemPath();
		EventFrame f = new EventFrame();
		Node node;
		if(list.get(index).isMathML())
			node = findMathElement(list.get(index), message);
		else
			node = findElement(list.get(index));
		
		if(node instanceof Element)
			message.put("element", node);
		
		f.addEvent(new Event(EventTypes.Delete, node, vi.getStartIndex(), index, list.get(index).start, list.get(index).brailleList.getFirst().start, treeIndex));
		manager.addUndoEvent(f);
	}
	
	private Node findElement(TextMapElement t){		
		if(manager.getDocument().hasNonBrailleChildren(t.parentElement()) && !(t.n instanceof Element)){
			return t.n;
		}
		else {
			Element e = t.parentElement();
			while(!e.getAttributeValue("semantics").contains("style")){
				if(((Element)e.getParent()).getChildElements().size() <= 1)
					e = (Element)e.getParent();
				else
					break;
			}

			return e;
		}
	}
	
	private Node findMathElement(TextMapElement t, Message m){
		Nodes nodes = new Nodes();
		Element parent = t.parentElement();
		int index = parent.indexOf(t.n) + 1;
		int count = parent.getChildCount() - 1;
		
		nodes.append(t.n);
		while(index < parent.getChildCount() && parent.getChild(index) instanceof Element && ((Element)parent.getChild(index)).getLocalName().equals("brl")){
			nodes.append(parent.getChild(index));
			index++;
			count--;
		}
		
		m.put("nodes", nodes);
		if(count == 0)
			return parent;
				
		return t.n;
	}
	
	private void disableViews(){
		text.removeListeners();
		braille.removeListeners();
		tree.removeListeners();
		list.clearList();
		text.view.setEditable(false);
	}
	
	private boolean emptyList(){
		return list.size() == 0;
	}
}
