package org.brailleblaster.perspectives.braille.eventQueue;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class QueueManager {
	private static final int FIRST_ITEM_ID = 1;
	
	private EventQueue undoQueue, redoQueue;
	private boolean swapFrame = false;
	
	public QueueManager(){
		undoQueue = new UndoQueue();
		redoQueue = new RedoQueue();
	}

	public void addUndoEvent(EventFrame f){
		if(undoQueue.empty()){
			f.setId(FIRST_ITEM_ID);
			if(!redoQueue.empty() && !swapFrame)
				redoQueue.clear();
		}
		else{
			int id = undoQueue.peek().getId() + 1;
			if(!redoQueue.empty() && redoQueue.peek().getId() == id)
				redoQueue.clear();
			
			f.setId(id);
		}
		undoQueue.add(f);
	}
	
	public void addRedoEvent(EventFrame f){
		int id;
		if(undoQueue.empty())
			id = FIRST_ITEM_ID;
		else
			id = undoQueue.peek().getId() + 1;
		
		f.setId(id);
		redoQueue.add(f);
	}
	
	public void undo(ViewInitializer vi, BrailleDocument document, MapList list, Manager manager){
		undoQueue.popEvent(vi, document, list, manager);
	}
	
	public void redo(ViewInitializer vi, BrailleDocument document, MapList list, Manager manager){
		swapFrame = true;
		redoQueue.popEvent(vi, document, list, manager);
		swapFrame = false;
	}
	
	public EventFrame peekUndoEvent(){
		return undoQueue.peek();
	}
}
