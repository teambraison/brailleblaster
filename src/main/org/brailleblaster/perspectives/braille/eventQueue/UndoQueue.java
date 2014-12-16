package org.brailleblaster.perspectives.braille.eventQueue;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.stylers.ElementInserter;
import org.brailleblaster.perspectives.braille.stylers.StyleHandler;
import org.brailleblaster.perspectives.braille.stylers.TextUpdateHandler;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class UndoQueue extends EventQueue {
	
	private static final long serialVersionUID = 1658714661481118195L;

	public UndoQueue(){
		super();		
	}
	
	@Override
	protected void handleEvent(EventFrame frame, ViewInitializer vi, BrailleDocument doc, MapList list, Manager manager){
		for(int i = frame.size() - 1; frame.size() > 0 && i >= 0; i--){
			Event event = frame.get(i);
			switch(event.eventType){
				case Update:
					TextUpdateHandler tuh = new TextUpdateHandler(manager, vi, list);
					tuh.undoText(event);
					break;
				case Insert:
					break;
				case Delete:
					ElementInserter inserter = new ElementInserter(vi, doc, list, manager);
					inserter.insertElement(frame);
					break;
				case Hide:
					ElementInserter ei = new ElementInserter(vi, doc, list, manager);
					ei.resetElement(frame);
					break;
				case Style_Change:
					StyleHandler sh = new StyleHandler(manager, vi, list);
					sh.undoStyle(frame);
					break;
				default:
					break;
			}
		}
	}
}
