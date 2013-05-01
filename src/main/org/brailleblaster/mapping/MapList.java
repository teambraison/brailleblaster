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
	private int currentIndex;
	private int prevEnd, nextStart, prevBraille, nextBraille;
	
	public MapList(DocumentManager dm){
		this.dm = dm;
	}
	
	public int findClosest(Message message){
		int location = (Integer)message.getValue("offset");
		int nodeIndex = getNodeIndex((TextMapElement)message.getValue("selection"));
		
		if(location <= this.get(0).start){
			return 0;
		}
		else if(location >= this.getLast().end){
			return this.indexOf(this.getLast());
		}

		for(int i = 0; i < this.size(); i++){
			if(location >= this.get(i).start && location <= this.get(i).end){
				if(location == this.get(i).end && location == this.get(i + 1).start){
					if(checkForSpace(i)){
						return i + 1;
					}
					
					if(i == nodeIndex){
						return i;
					}
					else if( i + 1 == nodeIndex) {
						return i + 1;
					}
				}
				else{
					return i;
				}
			}
			else if(location > this.get(i).end && location < this.get(i + 1).start){
				if(location - this.get(i).end > this.get(i + 1).start - location){
					return i;
				}
				else if(location - this.get(i).end < this.get(i + 1).start - location){
					return i + 1;
				}
				else {
					if(i == nodeIndex){
						return i;
					}
					else if( i + 1 == nodeIndex) {
						return i + 1;
					}
					else {
						return i;
					}
				}
			}
		}
			
		return -1;
	}
	
	private boolean checkForSpace(int index){
		char firstChar = this.get(index).n.getValue().charAt(this.get(index).n.getValue().length() - 1);

		if(this.get(index + 1).n.getValue().length() > 0){
			char nextChar =  this.get(index + 1).n.getValue().charAt(0);
			if( firstChar == ' ' && nextChar != ' '){
				return true;
			}
		}
		
		return false;
	}
	
	public int findClosestBraille(Message message){
		int location = (Integer)message.getValue("offset");
		
		if(this.getFirst().brailleList.size() > 0 && location <= this.getFirst().brailleList.getFirst().start){
			return 0;
		}
		else if(this.getLast().brailleList.size() > 0 && location >= this.getLast().brailleList.getLast().end){
			return this.indexOf(this.getLast());
		}
		
		for(int i = 0; i < this.size(); i++){
			if(this.get(i).brailleList.size() > 0 && location >= this.get(i).brailleList.getFirst().start  && location <= this.get(i).brailleList.getLast().end){
				return i;
			}
		}
		return 0;
	}
		
	public void updateOffsets(int index, Message message){
		updateTextOffsets(index, (Integer)message.getValue("length"));
		
		if(message.getValue("brailleLength") != null){
			updateBrailleOffsets(index, message);
		}
	}
	
	private void updateTextOffsets(int nodeNum, int offset){
		this.get(nodeNum).end += offset;
		
		for(int i = nodeNum + 1; i < this.size(); i++){
			this.get(i).start +=offset;
			this.get(i).end += offset;
		}
	}
	
	public void shiftOffsetsFromIndex(int index, int offset){
		for(int i = index; i < this.size(); i++){
			this.get(i).start +=offset;
			this.get(i).end += offset;
			if(hasBraille(i)){
				for(int j = 0; j < this.get(i).brailleList.size(); j++){
					this.get(i).brailleList.get(j).start += offset;
					this.get(i).brailleList.get(j).end += offset;
				}
			}
		}
	}
	
	public void shiftOffsetsAfterIndex(int index, int offset){
		for(int i = index + 1; i < this.size(); i++){
			this.get(i).start +=offset;
			this.get(i).end += offset;
			
			if(hasBraille(i)){
				for(int j = 0; j < this.get(i).brailleList.size(); j++){
					this.get(i).brailleList.get(j).start += offset;
					this.get(i).brailleList.get(j).end += offset;
				}
			}
		}
	}
	
	public void adjustOffsets(int index, Message message){
		if(message.getValue("start") != null){
			this.get(index).start -= (Integer)message.getValue("start");
			if(this.get(index).brailleList.size() > 0)
				this.get(index).brailleList.getFirst().start -= (Integer)message.getValue("start");
		}
		
		if(message.getValue("end") != null){
			this.get(index).end += (Integer)message.getValue("end");
			if(this.get(index).brailleList.size() > 0)
				this.get(index).brailleList.getLast().end += (Integer)message.getValue("end");
		}
	}
	
	private void updateBrailleOffsets(int index, Message message){
		int total = (Integer)message.getValue("newBrailleLength") - (Integer)message.getValue("brailleLength");
		
		for(int i = index + 1; i < this.size(); i++){
			for(int j = 0; j < this.get(i).brailleList.size(); j++){
				this.get(i).brailleList.get(j).start += total;
				this.get(i).brailleList.get(j).end += total;
			}
		}
	}
	
	public void checkList(){
		for(int i = 0; i < this.size() - 1; i++){
			if(this.get(i).start == this.get(i + 1).start){
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
			if(this.get(i - 1).start + this.get(i - 1).n.getValue().length() + 1 == this.get(i + 1).start && this.get(i).n.getValue().length() == 0){
				Message m = new Message(BBEvent.REMOVE_NODE);
				m.put("index", i);
				m.put("length",  this.get(i).n.getValue().length());
				System.out.println("Node 1:\t" + this.get(i).n.getValue());
				System.out.println("Node 2:\t" + this.get(i + 1).n.getValue());
				this.dm.dispatch(m);
				break;
			}
		}
		
		if(this.size() > 0 && this.get(this.size() - 1).n.getValue().length() == 0){
			if(this.get(this.size() - 1).start == this.prevEnd || this.get(this.size() - 1).start == 0){
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
			if(this.get(i).brailleList.size() > 0 && this.get(i).brailleList.getFirst().start != -1)
				return this.get(i).brailleList.getFirst().start;
			i++;
		}
		
		i = index - 1;
		while(i >= 0){
			if(this.get(i).brailleList.size() > 0 && this.get(i).brailleList.getFirst().start != -1)
				return this.get(i).brailleList.getFirst().start;
		}
		return 0;
	}
	
	public void setCurrent(int index){
		this.current = this.get(index);
		this.currentIndex = index;
		
		if(index > 0)
			this.prevEnd = this.get(index -1).end;
		else
			this.prevEnd = -1;
		
		if(index != this.size() - 1)
			this.nextStart = this.get(index + 1).start;
		else
			this.nextStart = -1;
		
		this.nextBraille = getNextBraille(index);
		this.prevBraille = getPreviousBraille(index);
	}
	
	public TextMapElement getCurrent(){
		if(this.current == null){
			setCurrent(0);
			return this.current;
		}
		else {
			return this.current;
		}
	}
	public int getCurrentIndex(){
		if(this.current == null){
			Message message = new Message(BBEvent.SET_CURRENT);
			message.put("offset", this.getFirst().start);
			dm.dispatch(message);
			return this.currentIndex;
		}
		else {
			return this.currentIndex;
		}
	}
	
	private int getCurrentBrailleEnd(){
		if(this.current.brailleList.size() == 0)
			return 0;
		else
			return this.current.brailleList.getLast().end;
	}
	
	private int getCurrentBrailleOffset(){
		if(this.current.brailleList.size() == 0)
			return 0;
		else
			return this.current.brailleList.getFirst().start;
	}
	
	private int getNextBraille(int index){
		int localIndex = index + 1;
		
		while(localIndex < this.size() && this.get(localIndex).brailleList.size() == 0)
			index++;
		
		if(localIndex < this.size())
			return this.get(localIndex).brailleList.getFirst().start;
		
		return -1;
	}
	
	private int getPreviousBraille(int index){
		int localIndex = index - 1;
		
		while(localIndex >= 0 && this.get(localIndex).brailleList.size() == 0)
			localIndex--;
		
		if(localIndex >= 0)
			return this.get(localIndex).brailleList.getLast().end;
		
		return -1;
	}
	
	public void getCurrentNodeData(Message m){
		if(this.current == null){
			int index = findClosest(m);
			setCurrent(index);
		}
		
		m.put("start", this.current.start);
		m.put("end", this.current.end);
		m.put("previous", this.prevEnd);
		m.put("next", this.nextStart);
		m.put("brailleStart", getCurrentBrailleOffset());
		m.put("brailleEnd", getCurrentBrailleEnd());
		m.put("nextBrailleStart", this.nextBraille);
		m.put("previousBrailleEnd", this.prevBraille);
	}
	
	private int getNodeIndex(TextMapElement t){
		return this.indexOf(t);
	}
	
	public boolean hasBraille(int index){
		if(this.get(index).brailleList.size() > 0)
			return true;
		else
			return false;
	}
}
