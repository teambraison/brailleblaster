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
	int currentLineNumber;
	Manager manager;
	TextView text;
	
	public EditRecorder(Manager manager, TextView text) {
		this.manager = manager;
		this.text = text;
	}
	
	public void recordEditEvent(Message m){
		ExtendedModifyEvent e = (ExtendedModifyEvent)m.getValue("event");
		if(e.replacedText.length() > 0 || currentLine.length() == 0){
			recordSelectionEdit(e);
		}
		else {
			int lineStart = text.view.getOffsetAtLine(text.view.getLineAtOffset(e.start));
			int offset = e.start - lineStart;	
			int index = offset;
			String lineText = text.view.getLine(text.view.getLineAtOffset(e.start));	
		
			while(index < lineText.length() && (lineText.charAt(index) != ' ' || index < (offset + e.length)))
				index++;
		
			int wordEnd = index; 
			index = offset;
		
			if(isBlankSpace(text.view.getTextRange(e.start, e.length)))
				index--;
		
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
	}
	
	public void recordDeleteEvent(Message m){
		ExtendedModifyEvent e = (ExtendedModifyEvent)m.getValue("event");
		
		if(e.replacedText.equals("\n")){
			createEvent(e.start, e.start, "\n");
		}
		else {
			int lineStart = text.view.getOffsetAtLine(text.view.getLineAtOffset(e.start));
			int offset = e.start - lineStart;
			String lineText = currentLine;
			int index = offset;
	
			while(index < lineText.length()){
				if(lineText.charAt(index) == ' '){
					if(index > offset + e.replacedText.length())
						break;
				}
				index++;
			}
		
			int wordEnd = index;
			index = offset;
			while(index - 1 >= 0 && index < lineText.length() && lineText.charAt(index - 1) != ' ')
				index--;
		
			int wordStart = index;
			String recordedText = lineText.substring(wordStart, offset) + lineText.substring(offset + e.length, wordEnd);
			wordStart = lineStart + wordStart;
			wordEnd = lineStart + wordEnd - e.replacedText.length();
			createEvent(wordStart, wordEnd, recordedText);
		}
	}
	
	private void recordSelectionEdit(ExtendedModifyEvent e){
		int lineStart = text.view.getOffsetAtLine(text.view.getLineAtOffset(e.start));
		int offset = e.start - lineStart;	
		int index = offset;
		recordLine(e.start, e.start + e.length);
		String lineText = currentLine;	
		
		while(index < lineText.length() && (lineText.charAt(index) != ' ' || index < (offset + e.length)))
			index++;
	
		int wordEnd = index; 
		index = offset;
	
		if(isBlankSpace(text.view.getTextRange(e.start, e.length)))
			index--;
	
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
	
	public void recordLine(String currentLine, int currentLineNumber){
		this.currentLine = currentLine;
		this.currentLineNumber = currentLineNumber;
	}
	
	public void recordLine(int start, int end){
		int firstLine = text.view.getLineAtOffset(start);
		int lastLine = text.view.getLineAtOffset(end);
		int firstOffset = text.view.getOffsetAtLine(firstLine);
		int lastOffset = text.view.getOffsetAtLine(lastLine) + text.view.getLine(lastLine).length();
		currentLineNumber = firstLine;
		currentLine = text.view.getTextRange(firstOffset, lastOffset - firstOffset);
	}
	
	public String getCurrentLine(){
		return currentLine;
	}
	
	private void createEvent(int wordStart, int wordEnd, String recordedText){
		if(manager.peekUndoEvent() != null && manager.peekUndoEvent().peek().getEventType().equals(EventTypes.Edit)){
			ViewEvent ev = (ViewEvent)manager.peekUndoEvent().peek();
			if(sameWord(ev, wordStart, recordedText))
				manager.peekUndoEvent().addEvent(new ViewEvent(EventTypes.Edit, wordStart, wordEnd, 0, 0, recordedText));
			else 
				addEvent(wordStart, wordEnd, recordedText);
		}
		else {
			addEvent(wordStart, wordEnd, recordedText);
		}
	}
	
	private void addEvent(int wordStart, int wordEnd, String recordedText){
		frame = new EventFrame();
		frame.addEvent(new ViewEvent(EventTypes.Edit, wordStart, wordEnd, 0, 0, recordedText));
		manager.addUndoEvent(frame);
	}
	
	private boolean sameWord(ViewEvent e, int wordStart, String recordedText){
		int line = text.view.getLineAtOffset(wordStart);
	
		if(line == currentLineNumber && !isBlankSpace(recordedText)){
			int priorStart = e.getTextOffset();
				
			if(wordStart == priorStart)
				return true;
		}
		
		return false;
	}
	
	private boolean isBlankSpace(String text){
		int length = text.length();
		for(int i = 0; i < length; i++){
			if(text.charAt(i) != ' ')
				return false;
		}
		
		return true;
	}
}
