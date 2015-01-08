package org.brailleblaster.perspectives.braille.stylers;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.eventQueue.Event;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ModelEvent;
import org.brailleblaster.perspectives.braille.eventQueue.ViewEvent;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class TextUpdateHandler extends Handler {

	BrailleDocument document;

	public TextUpdateHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);
		document = manager.getDocument();
	}
	
	public void updateText(Message message){
		message.put("selection", tree.getSelection(list.getCurrent()));
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
	
	public void undoText(EventFrame f){
		while(!f.empty() && f.peek().getEventType().equals(EventTypes.Update)){
			ModelEvent ev = (ModelEvent)f.pop();
			list.setCurrent(ev.getListIndex());
			text.view.setCaretOffset(list.getCurrent().start);
			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
			int changes = calculateEditDifference(ev, list.getCurrent());
			addRedoEvent();
			Message m = Message.createUpdateMessage(list.getCurrent().start, ev.getNode().getValue(), changes);
			resetModelEvent(m);
			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
			text.adjustCurrentElementValues(-changes);
		}
	}
	
	public void redoText(EventFrame f){
		while(!f.empty() && f.peek().getEventType().equals(EventTypes.Update)){
			ModelEvent ev = (ModelEvent)f.pop();
			list.setCurrent(ev.getListIndex());
			text.view.setCaretOffset(list.getCurrent().start);
			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
			addUndoEvent();
			int changes = calculateEditDifference(ev, list.getCurrent());
			Message m = Message.createUpdateMessage(list.getCurrent().start, ev.getNode().getValue(), changes);
			resetModelEvent(m);
			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
			//text.adjustCurrentElementValues(-changes);
		}
	}

	private int calculateEditDifference(ModelEvent ev, TextMapElement t){
		int evLength = ev.getNode().getValue().length();
		int viewLength = text.view.getTextRange(t.start, t.end - t.start).replace("\n", "").length();
		
		return evLength - viewLength;
	}
	
	private void resetModelEvent(Message message){
		document.updateDOM(list, message);
		braille.updateBraille(list.getCurrent(), message);
		list.updateOffsets(list.getCurrentIndex(), message);
		list.checkList();
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
		Event e = new ModelEvent(EventTypes.Update, t.n, vi.getStartIndex(), list.getCurrentIndex(), t.start, 
				t.brailleList.getFirst().start, tree.getItemPath());
		f.addEvent(e);
		
		return f;
	}
	
	public void undoEdit(EventFrame f){
		EventFrame frame = recreateEditEvent(f);
		manager.addRedoEvent(frame);
	}
	
	public void redoEdit(EventFrame f){
		EventFrame frame = recreateEditEvent(f);
		manager.addUndoEvent(frame);
	}
	
	private EventFrame recreateEditEvent(EventFrame f){
		EventFrame frame = new EventFrame();
		while(!f.empty() && f.peek().getEventType().equals(EventTypes.Edit)){
			ViewEvent ev = (ViewEvent)f.pop();
			
			if(ev.getTextOffset() >= text.getCurrentStart() && ev.getTextOffset() <= text.getCurrentEnd())
				text.view.setCaretOffset(ev.getTextOffset());	
			else
				text.setCurrentElement(ev.getTextOffset());
			
			int start = ev.getTextOffset();
			int end =  ev.getTextOffset() + ev.getText().length();
			String replacedText = text.view.getTextRange(ev.getTextOffset(), ev.getTextEnd() - ev.getTextOffset());
		
			frame.addEvent(new ViewEvent(EventTypes.Edit, start, end, 0,0, replacedText));
			text.undoEdit(ev.getTextOffset(), ev.getTextEnd() - ev.getTextOffset(), ev.getText());
		}
		
		return frame;
	}
}
