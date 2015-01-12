package org.brailleblaster.perspectives.braille.eventQueue;

import java.util.ArrayList;

public class EventFrame {
	ArrayList<Event>eventList;
	int sequenceId;
	
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
	
	public void setId(int sequenceId){
		this.sequenceId = sequenceId;
	}
	
	public int getId(){
		return sequenceId;
	}
	
	public Event peek(){
		if(!empty())
			return get(eventList.size() - 1);
		else
			return null;
	}
	
	public Event pop(){
		if(!empty())
			return eventList.remove(eventList.size() - 1);
		else
			return null;
	}
	
	public boolean empty(){
		return eventList.size() == 0;
	}
}
