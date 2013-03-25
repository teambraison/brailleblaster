package org.brailleblaster.mapping;

import java.util.LinkedList;

import org.brailleblaster.wordprocessor.BBEvent;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.Message;
import org.eclipse.swt.SWT;

public class MapList extends LinkedList<TextMapElement>{
	 
	private static final long serialVersionUID = 1L;
	DocumentManager dm;
	private TextMapElement current;
	private int currentIndex, currentLength, prevEnd, nextStart;
	
	public MapList(DocumentManager dm){
		this.dm = dm;
	}
	
	public int findClosest(Message message){
		int location = (Integer)message.getValue("offset"); 
		for(int i = 0; i < this.size() - 1; i++){
			if(location >= this.get(i).offset && location < this.get(i + 1).offset){
				if(message.type == BBEvent.SET_CURRENT || message.type == BBEvent.TEXT_DELETION){
					return checkConditions(i, message);
				}
				else {
					return i;
				}
			}
		}
		if(location >= this.get(this.size() - 1).offset){
			if(message.type == BBEvent.SET_CURRENT || message.type == BBEvent.TEXT_DELETION){
				return checkConditions(this.size() - 1, message);
			}
			else {
				return this.size() - 1;
			}
		}
		else {
			return -1;
		}
	}
	
	private int checkConditions(int index, Message message){
		if(checkBoundary(index, (Integer)message.getValue("offset"))){
			if(message.type == BBEvent.SET_CURRENT && hasNoBreak(index)){
				if(message.getValue("selection") != null && !(message.getValue("selection")).equals(this.get(index))){
					return index - 1;
				}
			}
			else if(message.type == BBEvent.TEXT_DELETION){
				if(hasNoBreak(index)){
					if((Integer)message.getValue("deletionType") == SWT.BS){
						message.put("update", true);
						return index - 1;
					}
				}
				else {
					System.out.println("No changes made, adjust offsets");
					this.updateOffsets(index - 1, message);
					this.checkList();
					message.put("update", false);
					return -1;
				}
			}
		}
		return index;
	}
	
	private boolean checkBoundary(int index, int offset){
		if(this.get(index).offset == offset && offset != 0)
			return true;
		else
			return false;
	}
	
	private boolean hasNoBreak(int index){
		if(this.get(index - 1).offset + this.get(index - 1).n.getValue().length() == this.get(index).offset)
			return true;
		else 
			return false;
	}
	
	public void updateOffsets(int index, Message message){
		if(index == this.size() - 1 && (Integer)message.getValue("newPosition") != null){
			if((Integer)message.getValue("newPosition") != this.current.offset){
				updateTextOffsets(index - 1, -1);
			}
			else {
				updateTextOffsets(index, (Integer)message.getValue("length"));
			}
		}
		else {
			updateTextOffsets(index, (Integer)message.getValue("length"));
		}
		if(message.getValue("brailleLength") != null && this.get(index).brailleList.getFirst().offset != 1){
			updateBrailleOffsets(index, (Integer)message.getValue("brailleLength"));
		}
	}
	
	private void updateTextOffsets(int nodeNum, int offset){
		for(int i = nodeNum + 1; i < this.size(); i++)
			this.get(i).offset +=offset;
	}
	
	private void updateBrailleOffsets(int index, int originalLength){
		int total = 0;
		for(int i = 0; i < this.get(index).brailleList.size(); i++){
			if(this.get(index).brailleList.get(i).offset != -1)
			total += this.get(index).brailleList.get(i).n.getValue().length() + 1;
		}
		
		total -= originalLength;
		
		for(int i = index + 1; i < this.size(); i++){
			for(int j = 0; j < this.get(i).brailleList.size(); j++){
				this.get(i).brailleList.get(j).offset += total;
			}
		}
	}
	
	public void checkList(){
		for(int i = 0; i < this.size() - 1; i++){
			if(this.get(i).offset == this.get(i + 1).offset){
				Message m = new Message(BBEvent.REMOVE_NODE);
				m.put("index", i);
				m.put("length",  this.get(i).n.getValue().length());
				System.out.println("Node 1:\t" + this.get(i).n.getValue());
				System.out.println("Node 2:\t" + this.get(i + 1).n.getValue());
				this.dm.dispatch(m);
				break;
			}
		}
		
		for(int i = 1; i < this.size() - 2; i++){
			if(this.get(i - 1).offset + this.get(i - 1).n.getValue().length() + 1 == this.get(i + 1).offset){
				Message m = new Message(BBEvent.REMOVE_NODE);
				m.put("index", i);
				m.put("length",  this.get(i).n.getValue().length());
				System.out.println("Node 1:\t" + this.get(i).n.getValue());
				System.out.println("Node 2:\t" + this.get(i + 1).n.getValue());
				this.dm.dispatch(m);
				break;
			}
		}
		
		if(this.get(this.size() - 1).n.getValue().length() == 0){
			if(this.get(this.size() - 1).offset == this.prevEnd || this.get(this.size() - 1).offset == 0){
				Message m = new Message(BBEvent.REMOVE_NODE);
				m.put("index", this.size() - 1);
				m.put("length",  this.get(this.size() - 1).n.getValue().length());
				System.out.println("Node 1:\t" + this.get(this.size() - 1).n.getValue());
				System.out.println("Node 2:\t none");
				this.dm.dispatch(m);
			}
		}
	}
	
	public int getNextBrailleOffset(int index){
		int i = index + 1;
		while(i < this.size()){
			//i++;
			if(this.get(i).brailleList.size() > 0 && this.get(i).brailleList.getFirst().offset != -1)
				return this.get(i).brailleList.getFirst().offset;
			i++;
		}
		
		i = index - 1;
		while(i >= 0){
			if(this.get(i).brailleList.size() > 0 && this.get(i).brailleList.getFirst().offset != -1)
				return this.get(i).brailleList.getFirst().offset;
		}
		return 0;
	}
	
	public void setCurrent(int index){
		this.current = this.get(index);
		this.currentIndex = index;
		this.currentLength = this.current.n.getValue().length();
		if(index > 0)
			this.prevEnd = this.get(index -1).offset + this.get(index - 1).n.getValue().length();
		else
			this.prevEnd = -1;
		
		if(index != this.size() - 1)
			this.nextStart = this.get(index + 1).offset;
		else
			this.nextStart = -1;
	}
	
	public TextMapElement getCurrent(){
		if(this.current == null){
			Message message = new Message(BBEvent.SET_CURRENT);
			message.put("offset", this.getFirst().offset);
			dm.dispatch(message);
			return this.current;
		}
		else {
			return this.current;
		}
	}
	public int getCurrentIndex(){
		if(this.current == null){
			Message message = new Message(BBEvent.SET_CURRENT);
			message.put("offset", this.getFirst().offset);
			dm.dispatch(message);
			return this.currentIndex;
		}
		else {
			return this.currentIndex;
		}
	}
	
	private int getCurrentOffset(){
		return this.current.offset;
	}
	
	private int getCurrentBrailleLength(){
		int length = 0;
		for(int i = 0; i < this.current.brailleList.size(); i++){
			length += this.current.brailleList.get(i).n.getValue().length();	
		}
		
		return length;
	}
	
	private int getCurrentBrailleOffset(){
		if(this.current.brailleList.size() == 0)
			return 0;
		else
			return this.current.brailleList.getFirst().offset;
	}
	
	public void getCurrentNodeData(Message m){
		if(this.current == null){
			int index = findClosest(m);
			setCurrent(index);
		}
		
		m.put("start", this.current.offset);
		m.put("end", this.current.offset + this.currentLength);
		m.put("previous", this.prevEnd);
		m.put("next", this.nextStart);
		m.put("brailleStart", getCurrentBrailleOffset());
		m.put("brailleEnd", getCurrentBrailleLength());
	}
}
