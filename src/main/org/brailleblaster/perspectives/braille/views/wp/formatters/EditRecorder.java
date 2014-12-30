package org.brailleblaster.perspectives.braille.views.wp.formatters;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ViewEvent;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.eclipse.swt.custom.ExtendedModifyEvent;

public class EditRecorder {

	EventFrame frame;
	String currentLine;
	Manager manager;
	TextView text;
	
	public EditRecorder(Manager manager, TextView text) {
		this.manager = manager;
		this.text = text;
	}
	
	public void recordEditEvent(Message m){
		EventFrame frame = new EventFrame();
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
		String recordedText = lineText.substring(wordStart, offset) + e.replacedText + lineText.substring(offset + e.length, wordEnd);
		wordStart = lineStart + wordStart;
		wordEnd = lineStart + wordEnd;
		frame.addEvent(new ViewEvent(EventTypes.Edit, wordStart, wordEnd, 0, 0, recordedText));
		manager.addUndoEvent(frame);
	}
	
	public void recordDeleteEvent(Message m){
		EventFrame frame = new EventFrame();
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
		frame.addEvent(new ViewEvent(EventTypes.Edit, wordStart, wordEnd, 0, 0, recordedText));
		manager.addUndoEvent(frame);
	}
	
	public void recordLine(String currentLine){
		this.currentLine = currentLine;
	}
	
	public void recordLine(int start, int end){
		int firstLine = text.view.getLineAtOffset(start);
		int lastLine = text.view.getLineAtOffset(end);
		currentLine = text.view.getLine(firstLine);
		if(firstLine != lastLine){
			do{
				firstLine++;
				this.currentLine += "\n";
				this.currentLine += text.view.getLine(firstLine);
			} while(firstLine <= lastLine);
		}
	}
	
	public String getCurrentLine(){
		return currentLine;
	}
}
