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
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by John J. Boyer john.boyer@abilitiessoft.com
 */

package org.brailleblaster.views;

import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractContent;
import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.mapping.BrailleMapElement;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.wordprocessor.BBEvent;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.Message;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Group;

public class BrailleView extends AbstractView {
	public int total;
	private int currentStart, currentEnd;
	
	public BrailleView(Group documentWindow) {
		super(documentWindow, 58, 100, 0, 100);
		this.total = 0;
	}
	
	public void initializeListeners(final DocumentManager dm){
		view.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				Message message = new Message(BBEvent.GET_CURRENT);
				dm.dispatch(message);
				currentStart = (Integer)message.getValue("brailleStart");
				currentEnd = (Integer)message.getValue("brailleEnd");
				view.setCaretOffset(currentStart);
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub	
			}
		});
	}
	
	public void setBraille(Node n, TextMapElement t){
		view.append(n.getValue() + "\n");
		t.brailleList.add(new BrailleMapElement(this.total, n));
		this.total += n.getValue().length() + 1;
	}
	
	public void updateBraille(TextMapElement t, int total){
		String insertionString = "";
		for(int i = 0; i < t.brailleList.size(); i++){
			insertionString += t.brailleList.get(i).n.getValue() + "\n";
		}	
		if(t.brailleList.getFirst().offset != -1){
			view.replaceTextRange(t.brailleList.getFirst().offset, total, insertionString);
		}
	}
	
	public void removeWhitespace(int offset){
		view.replaceTextRange(offset, 1, "");
	}
	
	/*
	 * This is a derivative work from org.eclipse.swt.custom.DefaultContent.java
	 */
	class BrailleContent extends AbstractContent {
	}
}
