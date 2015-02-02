package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.ArrayList;


public class EventFrame {
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
	
	public Event get(int index){
		return eventList.get(index);
	}
}
