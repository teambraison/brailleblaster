package org.brailleblaster.perspectives.braille.stylers;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ViewEvent;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class WhiteSpaceHandler {

	Manager manager;
	TextView text;
	BrailleView braille;
	MapList list;
	EventFrame eventFrame;
	
	public WhiteSpaceHandler(Manager manager, MapList list){
		this.manager = manager;
		this.text = manager.getText();
		this.braille = manager.getBraille();
		this.list = list;
	}
	
	public void removeWhitespace(Message message){
		eventFrame = new EventFrame();
		int brailleStart = 0;
		list.checkList();
		if(!list.empty()){		
			int start = (Integer)message.getValue("offset");
			int index = list.findClosest(message, 0, list.size() - 1);
			TextMapElement t = list.get(index);
			if(start < t.start){
				if(index > 0){
					if(t.brailleList.size() > 0)
						brailleStart = t.brailleList.getFirst().start + (Integer)message.getValue("length");
				}
				else{
					brailleStart = 0;
				}
			}
			else if(t.brailleList.size() > 0)
				brailleStart = t.brailleList.getLast().end;
		
			braille.removeWhitespace(brailleStart, (Integer)message.getValue("length"));
		
			if(start >= t.end && index != list.size() - 1 && list.size() > 1)
				list.shiftOffsetsFromIndex(index + 1, (Integer)message.getValue("length"), (Integer)message.getValue("length"));
			else if(index != list.size() -1 || (index == list.size() - 1 && start < t.start))
				list.shiftOffsetsFromIndex(index, (Integer)message.getValue("length"), (Integer)message.getValue("length"));
		}
		else
			braille.removeWhitespace(0,  (Integer)message.getValue("length"));
		
		eventFrame.addEvent(new ViewEvent(EventTypes.Whitespace, (Integer)message.getValue("offset"), brailleStart, (String)message.getValue("replacedText")));
		manager.addUndoEvent(eventFrame);
	}
	
	public void UndoDelete(EventFrame frame){
		eventFrame = new EventFrame();
		while(!frame.empty() && frame.peek().getEventType().equals(EventTypes.Whitespace)){
			ViewEvent ev = (ViewEvent)frame.pop();
			insertWhitespace(ev);
			eventFrame.addEvent(ev);
		}
		manager.addRedoEvent(eventFrame);
	}
	
	private void insertWhitespace(ViewEvent ev){
		text.insertText(ev.getTextOffset(), ev.getText());
		braille.insertText(ev.getBrailleOffset(), ev.getText());
		
		int pos = ev.getTextOffset() + ev.getText().length();
		
		if(pos != text.view.getCharCount()){
			Message curMessage = Message.createGetCurrentMessage(Sender.TREE, pos);
			manager.dispatch(curMessage);	
			list.shiftOffsetsFromIndex(list.getCurrentIndex(), ev.getText().length(),  ev.getText().length());
			
			text.view.setCaretOffset(list.getCurrent().start);
			text.refreshStyle(list.getCurrent());
			braille.refreshStyle(list.getCurrent());
			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
		}
	}
	
	public void redoDelete(EventFrame frame){
		while(!frame.empty() && frame.peek().getEventType().equals(EventTypes.Whitespace))
			removeWhitespace((ViewEvent)frame.pop());
	}
	
	private void removeWhitespace(ViewEvent ev){
		text.setCurrentSelection(ev.getTextOffset(), ev.getTextOffset() + ev.getText().length());
		text.cut();
	}
}
