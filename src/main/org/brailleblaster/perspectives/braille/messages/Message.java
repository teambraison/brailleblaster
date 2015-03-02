/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknowledged.
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
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.perspectives.braille.messages;

import java.util.ArrayList;
import java.util.HashMap;

import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.eclipse.swt.custom.ExtendedModifyEvent;

import nu.xom.Text;


//Passes data between different views and the parent DocumentManager class
public class Message {
	public BBEvent type;
	private HashMap<String, Object> args;
	
	public Message(BBEvent type){
		this.type = type;
		this.args = new HashMap<String, Object>();
	}
	
	public static Message createAdjustAlignmentMessage(Sender sender, int alignment){
		Message m = new Message(BBEvent.ADJUST_ALIGNMENT);
		m.put("sender", sender);
		m.put("alignment", alignment);
		
		return m;
	}
	
	public static Message createAdjustIndentMessage(Sender sender, int indent, int line){
		Message m = new Message(BBEvent.ADJUST_INDENT);
		m.put("sender", sender);
		m.put("indent", indent);
		m.put("line", line);
		return m;
	}
	
	public static Message createAdjustMarginMessager(Sender sender, int margin, int line){
		Message m = new Message(BBEvent.ADJUST_MARGIN);
		m.put("sender", sender);
		m.put("margin", margin);
		m.put("line", line);
		return m;
	}
	
	public static Message createAdjustLinesMessage(Sender sender, boolean linesBefore, int lines){
		Message m = new Message(BBEvent.ADJUST_LINES);
		m.put("sender", sender);
		m.put("linesBefore", linesBefore);
		m.put("lines", lines);
		
		return m;
	}
	
	public static Message createAdjustRange(String type, int position){
		Message m = new Message(BBEvent.ADJUST_RANGE);
		m.put(type, position);
		
		return m;
	}
	
	public static Message createInsertNodeMessage(boolean split, boolean atStart, boolean atEnd, String elementName){
		Message m = new Message(BBEvent.INSERT_NODE);
		m.put("split", split);
		m.put("atStart", atStart);
		m.put("atEnd", atEnd);
		m.put("elementName", elementName);
		
		return m;
	}
	
	public static Message createMergeElementMessage(boolean isFirst){
		Message m = new Message(BBEvent.MERGE);
		m.put("isFirst", isFirst);
		
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
	
	public static Message createUpdateScollbarMessage(Sender sender, int offset){
		Message m = new Message(BBEvent.UPDATE_SCROLLBAR);
		m.put("sender", sender);
		m.put("offset", offset);
		
		return m;
	}
	
	public static Message createUpdateCursorsMessage(Sender sender){
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
	
	public static Message createSelectionMessage(String replacementText, int start, int end){
		Message m = new Message(BBEvent.SELECTION);
		m.put("replacementText", replacementText);
		m.put("start", start);
		m.put("end", end);
		
		return m;
	}
	
	public static Message createUpdateStatusbarMessage(String line){
		Message m = new Message(BBEvent.UPDATE_STATUSBAR);
		m.put("line", line);
		
		return m;
	}
	
	public static Message createGetCurrentMessage(Sender sender, int offset){
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
	
	public static Message createSetCurrentMessage(Sender sender, int offset, boolean isBraille){
		Message m = new Message(BBEvent.SET_CURRENT);
		m.put("sender", sender);
		m.put("offset", offset);
		m.put("isBraille", isBraille);
		
		return m;
	}
	
	public static Message createTextDeletionMessage(int offset, int length, String replacedText, boolean update){
		Message m = new Message(BBEvent.WHITESPACE_DELETION);
		m.put("offset", offset);
		m.put("length", length);
		m.put("replacedText", replacedText);
		m.put("update", update);
		
		return m;
	}
	
	public static Message createSplitTreeMessage(int firstElementIndex, int secondElementIndex, int currentIndex, int treeIndex){
		Message m = new Message(BBEvent.SPLIT_TREE);
		m.put("firstElementIndex", firstElementIndex);
		m.put("secondElementIndex", secondElementIndex);
		m.put("currentIndex", currentIndex);
		m.put("treeIndex",treeIndex);
		
		return m;
	}
	
	/**
	 * Create a Message object base on multiple selection is true or false
	 * @param style: Style to add, remove, or adjust 
	 * @param multiSelect: signifies whether multiple elements have been selected
	 * @param isBoxline: signifies whether selection is adding or removing a boxline, since boxline are handled differently than other styles
	 * @return
	 */
	public static Message createUpdateStyleMessage(Styles style, boolean multiSelect, boolean isBoxline){
		Message m = new Message(BBEvent.UPDATE_STYLE);
		m.put("Style", style);
		m.put("multiSelect", multiSelect);
		m.put("isBoxline", isBoxline);
		return m;
	}
	
	public static Message createEditEventMesag(ExtendedModifyEvent e){
		Message m = new Message(BBEvent.EDIT);
		m.put("event", e);
		
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
