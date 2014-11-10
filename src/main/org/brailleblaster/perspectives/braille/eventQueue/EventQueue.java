package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.concurrent.LinkedBlockingDeque;

import org.brailleblaster.document.BBDocument;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.stylers.ElementInserter;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class EventQueue extends LinkedBlockingDeque<EventFrame> {

	private static final long serialVersionUID = -1072781074100832339L;
	private static final int SIZE = 20;
	public EventQueue(){
		super(SIZE);
	}
	
	public void popEvent(ViewInitializer vi, BBDocument doc, MapList list, Manager manager){
		if(size() > 0){
			EventFrame f = this.removeLast();
			handleEvent(f, vi, doc, list, manager);
		}
	}
	
	private void handleEvent(EventFrame f, ViewInitializer vi, BBDocument doc, MapList list, Manager manager){
		while(f.size() > 0){
			Event event = f.pop();
			switch(event.eventType){
				case Update:
					break;
				case Insert:
					break;
				case Delete:
					ElementInserter es = new ElementInserter(vi, doc, list, manager);
					es.resetElement(event);
					break;
				default:
					break;
			}
		}
	}
}
