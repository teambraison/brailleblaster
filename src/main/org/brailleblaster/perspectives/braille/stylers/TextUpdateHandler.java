package org.brailleblaster.perspectives.braille.stylers;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.eventQueue.Event;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class TextUpdateHandler extends Handler {

	TextView text;
	BrailleView braille;
	BBTree treeView;
	BrailleDocument document;

	public TextUpdateHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);
	
		text = manager.getText();
		braille = manager.getBraille();
		treeView = manager.getTreeView();
		document = manager.getDocument();
	}
	
	public void updateText(Message message){
		message.put("selection", treeView.getSelection(list.getCurrent()));
		if(list.getCurrent().isMathML()){
			manager.dispatch(Message.createRemoveNodeMessage(list.getCurrentIndex(), list.getCurrent().end - list.getCurrent().start));
			message.put("diff", 0);
		}
		else {
			addUndoEvent();
			resetText(message);
		}
		manager.getArchiver().setDocumentEdited(true);
	}
	
	public void undoText(Event ev){
		list.setCurrent(ev.getListIndex());
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
		addRedoEvent();
		Message m = Message.createUpdateMessage(list.getCurrent().start, ev.getNode().getValue(), list.getCurrent().end - list.getCurrent().start);
		resetText(m);
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	}
	
	public void redoText(Event ev){
		list.setCurrent(ev.getListIndex());
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
		addUndoEvent();
		Message m = Message.createUpdateMessage(list.getCurrent().start, ev.getNode().getValue(), list.getCurrent().end - list.getCurrent().start);
		resetText(m);
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	}

	private void resetText(Message message){
		document.updateDOM(list, message);
		braille.updateBraille(list.getCurrent(), message);
		text.reformatText(list.getCurrent().n, message, manager);
		list.updateOffsets(list.getCurrentIndex(), message);
		list.checkList();
	}
	
	private void addUndoEvent(){
		manager.addUndoEvent(addEvent());
	}
	
	private void addRedoEvent(){
		manager.addRedoEvent(addEvent());
	}
	
	private EventFrame addEvent(){
		EventFrame f = new EventFrame();
		TextMapElement t = list.getCurrent();
		Event e = new Event(EventTypes.Update, t.n, vi.getStartIndex(), list.getCurrentIndex(), t.start, 
				t.brailleList.getFirst().start, treeView.getItemPath());
		f.addEvent(e);
		
		return f;
	}
}
