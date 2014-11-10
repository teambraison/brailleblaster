package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.ArrayList;


public class EventFrame {
	public static final int FIRST = 0;
	ArrayList<Event>eventList;
	
	public EventFrame(){
		eventList = new ArrayList<Event>();
	}
	
	public void addEvent(Event event){
		eventList.add(event);
	}
	
	public int size(){
		return eventList.size();
	}
	
	public Event pop(){
		if(eventList.size() > 0)
			return getLast();
		else
			return null;
	}
	
	private Event getLast(){
		return eventList.remove(eventList.size() - 1);
	}
}
