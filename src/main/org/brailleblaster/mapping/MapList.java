package org.brailleblaster.mapping;

import java.util.LinkedList;

import org.brailleblaster.wordprocessor.BBEvent;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.Message;
import org.eclipse.swt.SWT;

public class MapList extends LinkedList<TextMapElement>{
	 
	private static final long serialVersionUID = 1L;
	DocumentManager dm;
	
	public MapList(DocumentManager dm){
		this.dm = dm;
	}
	
	public int findClosest(Message message){
		int location = (Integer)message.getValue("offset"); 
		for(int i = 0; i < this.size() - 1; i++){
			if(location >= this.get(i).offset && location < this.get(i + 1).offset){
				if(message.type == BBEvent.TEXT_DELETION){
					return checkConditions(i, message);
				}
				else {
					return i;
				}
			}
		}
		if(location > this.get(this.size() - 1).offset){
					return this.size() - 1;
		}
		else {
			return -1;
		}
	}
	
	private int checkConditions(int index, Message message){
		if(checkBoundary(index, (Integer)message.getValue("offset"))){
			if(hasNoBreak(index, (Integer)message.getValue("length"))){
				if((Integer)message.getValue("deletionType") == SWT.BS){
					index -= 1;
				}
			}
			else {
				System.out.println("No changes made, adjust offsets");
				this.updateOffsets(index - 1, message);
				this.checkList();
				return -1;
			}
		}
		return index;
	}
	
	private boolean checkBoundary(int index, int offset){
		if(this.get(index).offset == offset)
			return true;
		else
			return false;
	}
	
	private boolean hasNoBreak(int index, int offset){
		if(this.get(index - 1).offset + this.get(index - 1).n.getValue().length() == this.get(index).offset)
			return true;
		else 
			return false;
	}
	
	public void updateOffsets(int index, Message message){
		updateTextOffsets(index, (Integer)message.getValue("length"));
		if(message.getValue("brailleLength") != null && (Integer)message.getValue("brailleLength") != 0){
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
				System.out.println(m.getValue("length"));
				System.out.println("Node 1:\t" + this.get(i).n.getValue());
				System.out.println("Node 2:\t" + this.get(i + 1).n.getValue());
				this.dm.dispatch(m);
				break;
			}
		}
	}
	
	public int getNextBrailleOffset(int index){
		int i = index + 1;
		while(i < this.size()){
			i++;
			if(this.get(i).brailleList.size() > 0 && this.get(i).brailleList.getFirst().offset != -1)
				return this.get(i).brailleList.getFirst().offset;
		}
		
		i = index - 1;
		while(i >= 0){
			if(this.get(i).brailleList.size() > 0 && this.get(i).brailleList.getFirst().offset != -1)
				return this.get(i).brailleList.getFirst().offset;
		}
		return 0;
	}
	
	/*
	public TextMapElement getTextMapElement(int index){
		return this.get(index);
	}
	
	public int getTextOffset(int index){
		return this.get(index).offset;
	}
	
	public Node getTextNode(int index){
		return this.get(index).n;
	}
	
	public String getTextValue(int index){
		return this.get(index).n.getValue();
	}
	
	public BrailleMapElement getBrailleMapElement(int textIndex, int brailleIndex){
		return this.get(textIndex).brailleList.get(brailleIndex);
	}
	
	public int getBrailleOffset(int textIndex, int brailleIndex){
		return this.get(textIndex).brailleList.get(brailleIndex).offset;
	}
	
	public Node getBrailleNode(int textIndex, int brailleIndex){
		return this.get(textIndex).brailleList.get(brailleIndex).n;
	}
	*/
}
