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

package org.brailleblaster.messages;

import java.util.ArrayList;
import java.util.HashMap;

import org.brailleblaster.mapping.TextMapElement;

import nu.xom.Text;


//Passes data between different views and the parent DocumentManager class
public class Message {
	public BBEvent type;
	private HashMap<String, Object> args;
	
	public Message(BBEvent type){
		this.type = type;
		this.args = new HashMap<String, Object>();
	}
	
	public static Message createAdjustAlignmentMessage(String sender, int alignment){
		Message m = new Message(BBEvent.ADJUST_ALIGNMENT);
		m.put("sender", sender);
		m.put("alignment", alignment);
		
		return m;
	}
	
	public static Message createAdjustIndentMessage(String sender, int indent, int line){
		Message m = new Message(BBEvent.ADJUST_INDENT);
		m.put("sender", sender);
		m.put("indent", indent);
		m.put("line", line);
		return m;
	}
	
	public static Message createAdjustRange(String type, int position){
		Message m = new Message(BBEvent.ADJUST_RANGE);
		m.put(type, position);
		
		return m;
	}
	
	public static Message createInsertNodeMessage(boolean split, boolean atStart, boolean atEnd){
		Message m = new Message(BBEvent.INSERT_NODE);
		m.put("split", split);
		m.put("atStart", atStart);
		m.put("atEnd", atEnd);
		
		return m;
	}
	
	public static Message createIncrementMessage(){
		Message m = new Message(BBEvent.INCREMENT);
		return m;
	}
	
	public static Message createDecrementMessage(){
		Message m = new Message(BBEvent.DECREMENT);
		return m;
	}
	
	public static Message createUpdateScollbarMessage(String sender, int offset){
		Message m = new Message(BBEvent.UPDATE_SCROLLBAR);
		m.put("sender", sender);
		m.put("offset", offset);
		
		return m;
	}
	
	public static Message createUpdateCursorsMessage(String sender){
		Message m = new Message(BBEvent.UPDATE_CURSORS);
		m.put("sender", sender);
		
		return m;
	}
	
	public static Message createUpdateMessage(int offset, String newText, int length){
		Message m = new Message(BBEvent.UPDATE);
		m.put("offset", offset);
		m.put("newText", newText);
		m.put("length", length);
		
		return m;
	}
	
	public static Message createUPdateStatusbarMessage(String line){
		Message m = new Message(BBEvent.UPDATE_STATUSBAR);
		m.put("line", line);
		
		return m;
	}
	
	public static Message createGetCurrentMessage(String sender, int offset){
		Message m = new Message(BBEvent.GET_CURRENT);
		m.put("sender", sender);
		m.put("offset", offset);
		
		return m;
	}
	
	public static Message createGetTextMapElementsMessage(ArrayList<Text> nodes, ArrayList<TextMapElement> itemList){
		Message m = new Message(BBEvent.GET_TEXT_MAP_ELEMENTS);
		m.put("nodes", nodes);
		m.put("itemList", itemList);
		
		return m;
	}
	
	public static Message createRemoveNodeMessage(int index, int length){
		Message m = new Message(BBEvent.REMOVE_NODE);
		m.put("index", index);
		m.put("length",  length);
		
		return m;
	}
	
	public static Message createSetCurrentMessage(String sender, int offset, boolean isBraille){
		Message m = new Message(BBEvent.SET_CURRENT);
		m.put("sender", sender);
		m.put("offset", offset);
		m.put("isBraille", isBraille);
		
		return m;
	}
	
	public static Message createTextDeletionMessage(int length, int deletionType, boolean update){
		Message m = new Message(BBEvent.TEXT_DELETION);
		m.put("length", length);
		m.put("deletionType", deletionType);
		m.put("update", update);
		
		return m;
	}
	
	public void put(String key, Object value){
		args.put(key, value);
	}
	
	public <T> Object getValue(String key){
		return args.get(key);
	}
	
	public void clearMessage(){
		this.args.clear();
	}
	
	public boolean contains(String key){
		return args.containsKey(key);
	}
	
	public void remove(String key){
		args.remove(key);
	}
}
