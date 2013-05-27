/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.txt
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.mapping;

import java.util.ArrayList;
import java.util.LinkedList;

import nu.xom.Text;

import org.brailleblaster.wordprocessor.BBEvent;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.Message;

public class MapList extends LinkedList<TextMapElement>{
	 
	private static final long serialVersionUID = 1L;
	DocumentManager dm;
	private TextMapElement current;
	private int currentIndex = -1;
	private int prevEnd, nextStart, prevBraille, nextBraille;
		
	public MapList(DocumentManager dm){
		this.dm = dm;
	}
	
	public int findClosest(Message message, int low, int high){
		int location = (Integer)message.getValue("offset");
		int nodeIndex = getNodeIndex((TextMapElement)message.getValue("selection"));
		
		if(location <= this.get(0).start){
			return 0;
		}
		else if(location >= this.getLast().end){
			return this.indexOf(this.getLast());
		}
		
		int mid = low  + ((high - low) / 2);
		
		TextMapElement currentElement = this.get(mid);
		if(location >= currentElement.start && location <= currentElement.end){
			if(location == currentElement.end && location == this.get(mid + 1).start){
				if(checkForSpace(mid)){
					return mid + 1;
				}
				
				if(mid == nodeIndex){
					return mid;
				}
				else if( mid + 1 == nodeIndex) {
					return mid + 1;
				}
			}
			else if(location == currentElement.start && location == this.get(mid - 1).end){
				return mid - 1;
			}
			else{
				return mid;
			}
		}
		else if(location > currentElement.end && location < this.get(mid + 1).start){
			if(location - currentElement.end < this.get(mid + 1).start - location){
				return mid;
			}
			else if(location - currentElement.end > this.get(mid + 1).start - location){
				return mid + 1;
			}
			else {
				if(message.contains("char") && message.getValue("char").equals("\t")){
					return mid + 1;
				}
				if(mid == nodeIndex){
					return mid;
				}
				else if( mid + 1 == nodeIndex) {
					return mid + 1;
				}
				else {
					return mid;
				}
			}
		}
			
		if(low > high){
			return -1;
		}
		else if(location < this.get(mid).start)
			return findClosest(message, low, mid - 1);
		else
			return findClosest(message, mid + 1, high);
	}
	
	private boolean checkForSpace(int index){
		if(this.get(index).n.getValue().length() == 0)
			return true;
		
		char firstChar = this.get(index).n.getValue().charAt(this.get(index).n.getValue().length() - 1);
		String nextElementText = this.get(index + 1).n.getValue(); 
		if(nextElementText.length() > 0){
			char nextChar =  nextElementText.charAt(0);
			if( firstChar == ' ' && nextChar != ' '){
				return true;
			}
		}
		
		return false;
	}
	
	public int findClosestBraille(Message message){
		int location = (Integer)message.getValue("offset");
		int nodeIndex = getNodeIndex((TextMapElement)message.getValue("selection"));
		
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
			else if(this.get(i).brailleList.size() > 0 && location > this.get(i).brailleList.getLast().end  && location < this.get(i + 1).brailleList.getFirst().start){
				if(location -  this.get(i).brailleList.getLast().end > this.get(i + 1).brailleList.getFirst().start - location){
					return i + 1;
				}
				else if(location -  this.get(i).brailleList.getLast().end < this.get(i + 1).brailleList.getFirst().start - location)  {
					return i;
				}
				else {
					if(message.contains("char") && message.getValue("char").equals("\t")){
						return i + 1;
					}
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
		return 0;
	}
		
	public void updateOffsets(int index, Message message){
		updateTextOffsets(index, (Integer)message.getValue("length"));
		
		if(message.contains("brailleLength")){
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
	
	private void updateBrailleOffsets(int index, Message message){
		int total = (Integer)message.getValue("newBrailleLength") - (Integer)message.getValue("brailleLength");
		
		for(int i = index + 1; i < this.size(); i++){
			for(int j = 0; j < this.get(i).brailleList.size(); j++){
				this.get(i).brailleList.get(j).start += total;
				this.get(i).brailleList.get(j).end += total;
			}
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
		if(message.contains("start")){
			this.get(index).start -= (Integer)message.getValue("start");
			if(this.get(index).brailleList.size() > 0){
				this.get(index).brailleList.getFirst().start -= (Integer)message.getValue("start");
			}
		}
		
		if(message.contains("end")){
			this.get(index).end += (Integer)message.getValue("end");
			if(this.get(index).brailleList.size() > 0)
				this.get(index).brailleList.getLast().end += (Integer)message.getValue("end");
		}
	}

	public void checkList(){
		if(this.currentIndex != -1){
			int index = this.currentIndex;
			int next = index + 1;
			int previous = index - 1;
		
			if(next < this.size()){	
				if(this.get(index).start == this.get(next).start){
					Message m = new Message(BBEvent.REMOVE_NODE);
					m.put("index", index);
					m.put("length",  this.get(index).n.getValue().length());
					System.out.println("Node 1:\t" + this.get(index).n.getValue());
					System.out.println("Node 2:\t" + this.get(next).n.getValue());
					this.dm.dispatch(m);
				}
			}
		
			if(previous >= 0 && next < this.size()){
				if((this.get(previous).start + this.get(previous).n.getValue().length() + 1 == this.get(next).start && this.get(index).n.getValue().length() == 0)
						|| (this.get(previous).end == this.get(index).start && this.get(index).n.getValue().length() == 0)){
					Message m = new Message(BBEvent.REMOVE_NODE);
					m.put("index", index);
					m.put("length",  this.get(index).n.getValue().length());
					System.out.println("Node 1:\t" + this.get(index).n.getValue());
					System.out.println("Node 2:\t" + this.get(next).n.getValue());
					this.dm.dispatch(m);
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
			localIndex++;
		
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
	
	private ArrayList<BrailleMapElement> getPageRanges(){
		ArrayList<BrailleMapElement> list = new ArrayList<BrailleMapElement>();
		for(int i = 0; i < this.current.brailleList.size(); i++){
			if(this.current.brailleList.get(i).pagenum){
				list.add(this.current.brailleList.get(i));
			}
		}
		
		return list;
	}
	
	public void getCurrentNodeData(Message m){
		if(this.current == null){
			int index = findClosest(m, 0, this.size() - 1);
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
		m.put("pageRanges", getPageRanges());
	}
	
	private int getNodeIndex(TextMapElement t){
		return this.indexOf(t);
	}
	
	public void incrementCurrent(Message message){
		if(this.currentIndex < this.size() - 1){
			setCurrent(this.currentIndex + 1);
			getCurrentNodeData(message);
		}
	}
	
	public void decrementCurrent(Message message){
		if(this.currentIndex > 0){
			setCurrent(this.currentIndex - 1);
			getCurrentNodeData(message);
		}
	}
	
	public boolean hasBraille(int index){
		if(this.get(index).brailleList.size() > 0)
			return true;
		else
			return false;
	}
	
	@SuppressWarnings("unchecked")
	public void findTextMapElements(Message message){
		ArrayList<Text>textList = (ArrayList<Text>)message.getValue("nodes");
		ArrayList<TextMapElement> itemList = (ArrayList<TextMapElement>)message.getValue("itemList");
		
		int pos = 0;
		for(int i = 0; i < textList.size(); i++){
			for(int j = pos; j < this.size(); j++){
				if(textList.get(i).equals(this.get(j).n)){
					itemList.add(this.get(j));
					pos = j + 1;
					break;
				}
			}
		}
	}
}
