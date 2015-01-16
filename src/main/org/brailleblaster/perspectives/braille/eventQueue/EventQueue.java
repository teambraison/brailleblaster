package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.concurrent.LinkedBlockingDeque;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public abstract class EventQueue extends LinkedBlockingDeque<EventFrame> {

	private static final long serialVersionUID = 4008887366963927827L;
	private static final int SIZE = 20;
	
	public EventQueue(){
		super(SIZE);
	}
	
	public EventFrame popEvent(ViewInitializer vi, BrailleDocument doc, MapList list, Manager manager){
		if(size() > 0){
			EventFrame f = removeLast();
			handleEvent(f, vi, doc, list, manager);
			return f;
		}
		
		return null;
	}
	
	@Override
	public boolean add(EventFrame f) {
		if(size() == SIZE)
			removeLast();
		
		return super.add(f);
	}
	
	protected abstract void handleEvent(EventFrame f, ViewInitializer vi, BrailleDocument doc, MapList list, Manager manager);
}
