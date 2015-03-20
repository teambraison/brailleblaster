package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.eventQueue.Event;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ModelEvent;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class RemoveElementHandler extends Handler{

	EventFrame eventFrame;
	
	public RemoveElementHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);
	}

	public void removeNode(Message m){
		eventFrame = new EventFrame();
		eventFrame.addEvent(addEvent(m));
		
		findRemovalMethod(m);
		
		if(manager.peekUndoEvent() != null && manager.peekUndoEvent().get(0).getEventType().equals(EventTypes.Whitespace)){
			while(!eventFrame.empty())
				manager.peekUndoEvent().addEvent(manager.peekUndoEvent().size() - 1,eventFrame.pop());
		}
		else
			manager.addUndoEvent(eventFrame);
	}
	
	public void removeNode(EventFrame frame){
		eventFrame = new EventFrame();
		while(!frame.empty() && frame.peek().getEventType().equals(EventTypes.Delete)){
			ModelEvent ev = (ModelEvent)frame.pop();
		
			int length = list.get(ev.getListIndex()).end - list.get(ev.getListIndex()).end; 
			Message m = Message.createRemoveNodeMessage(ev.getListIndex(), length);
			eventFrame.addEvent(addEvent(m));
			
			findRemovalMethod(m);
		
			if(ev.getNode() instanceof Element && isBlockElement(list.get(ev.getListIndex())) && ev.getListIndex() <= list.size() - 1 && list.get(ev.getListIndex()).start != ev.getTextOffset()){
				text.replaceTextRange(ev.getTextOffset(), 1, "");
				braille.replaceTextRange(ev.getBrailleOffset(), 1, "");
				list.shiftOffsetsFromIndex(ev.getListIndex(), -1, -1);
			}
	
			if(!list.empty())
				list.setCurrent(ev.getListIndex());
		
			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
		}
		
		if(manager.peekUndoEvent() != null && manager.peekUndoEvent().get(0).getEventType().equals(EventTypes.Whitespace)){
			while(!eventFrame.empty())
				manager.peekUndoEvent().addEvent(manager.peekUndoEvent().size() - 1,eventFrame.pop());
		}
		else
			manager.addUndoEvent(eventFrame);
	}
	
	public void undoInsert(EventFrame frame){
		eventFrame = new EventFrame();
		while(!frame.empty() && frame.peek().getEventType().equals(EventTypes.Insert)){
			ModelEvent ev = (ModelEvent)frame.pop();
		
			int length = list.get(ev.getListIndex()).end - list.get(ev.getListIndex()).end; 
			Message m = Message.createRemoveNodeMessage(ev.getListIndex(), length);
			m.put("element",  ev.getParent().getChild(ev.getParentIndex()));
			eventFrame.addEvent(new ModelEvent(EventTypes.Insert, ev.getParent().getChild(ev.getParentIndex()), ev.getFirstSectionIndex(), ev.getListIndex(), ev.getTextOffset(), ev.getBrailleOffset(), tree.getItemPath()));
			
			findRemovalMethod(m);
		
			if(ev.getNode() instanceof Element && isBlockElement(list.get(ev.getListIndex()))){
				text.replaceTextRange(ev.getTextOffset(), 1, "");
				braille.replaceTextRange(ev.getBrailleOffset(), 1, "");
				list.shiftOffsetsFromIndex(ev.getListIndex(), -1, -1);
			}
	
			if(!list.empty())
				list.setCurrent(ev.getListIndex());
		
			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
		}
		manager.addRedoEvent(eventFrame);
	}
	
	private void findRemovalMethod(Message m){
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
		
		if(list.empty())
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
		
		if(list.empty())
			disableViews();
	}
	
	private Event addEvent(Message message){
		int index = (Integer)message.getValue("index");
		ArrayList<Integer>treeIndex = tree.getItemPath();
	
		Node node;
		if(list.get(index).isMathML())
			node = findMathElement(list.get(index), message);
		else
			node = findElement(list.get(index));
		
		if(node instanceof Element)
			message.put("element", node);
		
		return new ModelEvent(EventTypes.Delete, node, vi.getStartIndex(), index, list.get(index).start, list.get(index).brailleList.getFirst().start, treeIndex);
	}
	
	private Node findElement(TextMapElement t){		
		if(manager.getDocument().hasNonBrailleChildren(t.parentElement()) && !(t.n instanceof Element)){
			return t.n;
		}
		else {
			Element e = t.parentElement();
			while(!e.getAttributeValue(SEMANTICS).contains("style")){
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
}
