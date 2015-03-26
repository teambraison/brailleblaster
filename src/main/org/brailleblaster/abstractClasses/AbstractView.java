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
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
 */

package org.brailleblaster.abstractClasses;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.utd.IStyle;
import org.brailleblaster.utd.actions.IAction;
import org.eclipse.swt.custom.SashForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractView {
	public boolean hasChanged = false;
	protected int total;
	protected int spaceBeforeText, spaceAfterText;
	public int positionFromStart, cursorOffset, words;
	protected boolean locked;
	protected SashForm sash;
	protected Manager manager;
	protected static Logger logger = LoggerFactory.getLogger(AbstractView.class);
	
	public AbstractView() {
	}

	/** Base constructor for views
	 * @param manager : manager for relaying information to models
	 * @param group : group in which to embed view
	 */
	public AbstractView(Manager manager, SashForm sash) {
		this.manager = manager;
		this.sash = sash;
	}
	
	/**Increments from currently selected element to next element
	 */
	public void incrementCurrent(){
		Message message = Message.createIncrementMessage();
		manager.dispatch(message);
		setViewData(message);
	}
	
	/** Decrements from currently selected element to previous element
	 */
	public void decrementCurrent(){
		Message message = Message.createDecrementMessage();
		manager.dispatch(message);
		setViewData(message);
	}
	
	/** Finds the corresponding braille node of a standard text node
	 * @param n : node to check
	 * @return braille element if markup is correct, null if braille cannot be found
	 */
	protected Element getBrlNode(Node n){
		Element e = (Element)n.getParent();
		int index = e.indexOf(n);
		if(index != e.getChildCount() - 1){
			if(((Element)e.getChild(index + 1)).getLocalName().equals("brl"))
				return (Element)e.getChild(index + 1);
		}
		
		return null;
	}
	
	/** Calculates word count of view
	 * @param text : full text from view
	 * @return int representing total words in the text
	 */
	public int getWordCount(String text){		
		String [] tokens = text.split(" ");
		return tokens.length;
	}
	
	/** Sets a lock to pause event listeners
	 * @param setting : true to lock, false to unlock
	 */
	protected void setListenerLock(boolean setting){
		locked = setting;
	}
	
	/** Returns state of listener lock
	 * @return true if locked, false if unlocked
	 */
	protected boolean getLock(){
		return locked;
	}
	
	/** Check if node is an element
	 * @param n : element to check
	 * @return True is an Element, False if not an element
	 */
	protected boolean isElement(Node n){
		return (n instanceof Element);
	}
	
	/** Checks if a node is a text node
	 * @param n : node to check
	 * @return True if a text node, False if not a text node
	 */
	protected boolean isText(Node n){
		return (n instanceof Text);
	}
	
	/** Sets the offset of the cursor from the starting position of the element
	 * used to keep cursors accurate when switching between views.
	 * @param offset : number of caret position from start to place cursor when view obtains focus
	 */
	public void setCursorOffset(int offset){
		cursorOffset = offset;
	}
	
	/** Returns the offset from start of the cursor
	 * @return the offset from start of the cursor
	 */
	public int getCursorOffset(){
		return cursorOffset;
	}
	
	/** Sets the total chars during initialization of a view
	 * @param total : total chars 
	 */
	public void setTotal(int total){
		this.total = total;
	}
	
	protected IAction getAction(Node n){
		return manager.getDocument().getEngine().getActionMap().findValue(n);	
	}
	
	protected IStyle getStyle(Node n){
		return manager.getDocument().getEngine().getStyle(n);
	}
	
	protected abstract void setViewData(Message message);
}
