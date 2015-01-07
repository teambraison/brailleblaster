package org.brailleblaster.perspectives.braille.views.wp.formatters;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.eventQueue.Event;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ViewEvent;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.eclipse.swt.custom.ExtendedModifyEvent;

public class EditRecorder {

	EventFrame frame;
	String currentLine;
	int currentLineNumber;
	Manager manager;
	TextView text;
	
	public EditRecorder(Manager manager, TextView text) {
		this.manager = manager;
		this.text = text;
	}
	
	public void recordEditEvent(Message m){
		ExtendedModifyEvent e = (ExtendedModifyEvent)m.getValue("event");
		
		int lineStart = text.view.getOffsetAtLine(text.view.getLineAtOffset(e.start));
		int offset = e.start - lineStart;
		
		String lineText = text.view.getLine(text.view.getLineAtOffset(e.start));
		int index = offset;
		while(index < lineText.length() && lineText.charAt(index) != ' ')
			index++;
		
		int wordEnd = index;
		index = offset;
		while(index > 0 && lineText.charAt(index) != ' ')
			index--;
		
		int wordStart = index;
		
		//handles case where char is space
		if(wordStart == wordEnd && e.length == 1)
			wordEnd++;
		
		String recordedText = lineText.substring(wordStart, offset) + e.replacedText + lineText.substring(offset + e.length, wordEnd);
		wordStart = lineStart + wordStart;
		wordEnd = lineStart + wordEnd;
		createEvent(wordStart, wordEnd, recordedText);
	}
	
	public void recordDeleteEvent(Message m){
		ExtendedModifyEvent e = (ExtendedModifyEvent)m.getValue("event");
		
		int lineStart = text.view.getOffsetAtLine(text.view.getLineAtOffset(e.start));
		int offset = e.start - lineStart;
		String lineText = currentLine;
		int index = offset;
		while(index < lineText.length() && (lineText.charAt(index) != ' ' || index < (offset + e.replacedText.length())))
			index++;
		
		int wordEnd = index;
		index = offset;
		while(index > 0 && lineText.charAt(index) != ' ')
			index--;
		
		int wordStart = index;
		String recordedText = lineText.substring(wordStart, offset) + lineText.substring(offset + e.length, wordEnd);
		wordStart = lineStart + wordStart;
		wordEnd = lineStart + wordEnd - e.replacedText.length();
		createEvent(wordStart, wordEnd, recordedText);
	}
	
	public void recordLine(String currentLine, int currentLineNumber){
		this.currentLine = currentLine;
		this.currentLineNumber = currentLineNumber;
	}
	
	public void recordLine(int start, int end){
		int firstLine = text.view.getLineAtOffset(start);
		int lastLine = text.view.getLineAtOffset(end);
		
		currentLineNumber = firstLine;
		currentLine = text.view.getLine(firstLine);
		if(firstLine != lastLine){
			do{
				firstLine++;
				currentLine += "\n";
				currentLine += text.view.getLine(firstLine);
			} while(firstLine < lastLine);
		}
	}
	
	public String getCurrentLine(){
		return currentLine;
	}
	
	private void createEvent(int wordStart, int wordEnd, String recordedText){
		if(manager.peekEvent() != null && manager.peekEvent().peek().getEventType().equals(EventTypes.Edit)){
			ViewEvent ev = (ViewEvent)manager.peekEvent().peek();
			if(sameWord(ev, wordStart)){
				manager.peekEvent().addEvent(new ViewEvent(EventTypes.Edit, wordStart, wordEnd, 0, 0, recordedText));
			}
			else {
				frame = new EventFrame();
				frame.addEvent(new ViewEvent(EventTypes.Edit, wordStart, wordEnd, 0, 0, recordedText));
				manager.addUndoEvent(frame);
			}
		}
		else {
			frame = new EventFrame();
			frame.addEvent(new ViewEvent(EventTypes.Edit, wordStart, wordEnd, 0, 0, recordedText));
			manager.addUndoEvent(frame);
		}
	}
	
	private boolean sameWord(Event e, int wordStart){
		int line = text.view.getLineAtOffset(wordStart);
		
		if(line == currentLineNumber){
			int priorStart = e.getTextOffset();
				
			if(wordStart == priorStart)
				return true;
		}
		
		return false;
	}
}
