package org.brailleblaster.perspectives.braille.eventQueue;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.stylers.ElementInserter;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class UndoQueue extends EventQueue {
	
	private static final long serialVersionUID = 1658714661481118195L;

	public UndoQueue(){
		super();		
	}
	
	@Override
	protected void handleEvent(EventFrame f, ViewInitializer vi, BrailleDocument doc, MapList list, Manager manager){
		for(int i = f.size() - 1; i >= 0; i--){
			Event event = f.get(i);
			switch(event.eventType){
				case Update:
					break;
				case Insert:
					break;
				case Hide:
					ElementInserter es = new ElementInserter(vi, doc, list, manager);
					es.resetElement(event);
					break;
				case Delete:
					break;
				default:
					break;
			}
		}
	}
}
