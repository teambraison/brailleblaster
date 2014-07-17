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

import org.slf4j.Logger;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

public abstract class AbstractView {
	public boolean hasFocus = false;
	public boolean hasChanged = false;
	protected int total;
	protected int spaceBeforeText, spaceAfterText;
	public int positionFromStart, cursorOffset, words;
	protected boolean locked;
	protected Group group;
	protected Manager manager;
	protected static Logger logger = BBIni.getLogger();
	
	public AbstractView() {
	}

	public AbstractView(Manager manager, Group group) {
		this.manager = manager;
		this.group = group;
	}
	
	protected void setLayout(Control c, int left, int right, int top, int bottom){
		FormData location = new FormData();
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		c.setLayoutData(location);
	}
	
	public void incrementCurrent(){
		Message message = Message.createIncrementMessage();
		manager.dispatch(message);
		setViewData(message);
	}
	
	public void decrementCurrent(){
		Message message = Message.createDecrementMessage();
		manager.dispatch(message);
		setViewData(message);
	}
	
	protected Element getBrlNode(Node n){
		Element e = (Element)n.getParent();
		int index = e.indexOf(n);
		if(index != e.getChildCount() - 1){
			if(((Element)e.getChild(index + 1)).getLocalName().equals("brl"))
				return (Element)e.getChild(index + 1);
		}
		
		return null;
	}
	
	public int getWordCount(String text){		
		String [] tokens = text.split(" ");
		return tokens.length;
	}
	
	protected void setListenerLock(boolean setting){
		locked = setting;
	}
	
	protected boolean getLock(){
		return locked;
	}
	
	protected boolean isElement(Node n){
		return (n instanceof Element);
	}
	
	protected boolean isText(Node n){
		return (n instanceof Text);
	}
	
	public void setCursorOffset(int offset){
		cursorOffset = offset;
	}
	
	public int getCursorOffset(){
		return cursorOffset;
	}
	
	public void setTotal(int total){
		this.total = total;
	}
	
	protected abstract void setViewData(Message message);
}
