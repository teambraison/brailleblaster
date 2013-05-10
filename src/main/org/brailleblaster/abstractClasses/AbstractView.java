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

package org.brailleblaster.abstractClasses;

import nu.xom.Element;
import nu.xom.Node;

import org.brailleblaster.wordprocessor.BBEvent;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.Message;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;

public abstract class AbstractView {
	public StyledText view;
	public boolean hasFocus = false;
	public boolean hasChanged = false;
	protected int total;
	protected int spaceBeforeText, spaceAfterText;
	public int positionFromStart;
	public static int cursorOffset, currentLine, words;
	
	public AbstractView() {
	}

	public AbstractView(Group group, int left, int right, int top, int bottom) {
		view = new StyledText(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);

		FormData location = new FormData();
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		view.setLayoutData(location);

		view.addModifyListener(viewMod);
	}

	// Better use a ModifyListener to set the change flag.
	ModifyListener viewMod = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			hasChanged = true;
		}
	};
	
	protected void sendIncrementCurrent(DocumentManager dm){
		Message message = new Message(BBEvent.INCREMENT);
		dm.dispatch(message);
		setViewData(message);
	}
	
	protected void sendDecrementCurrent(DocumentManager dm){
		Message message = new Message(BBEvent.DECREMENT);
		dm.dispatch(message);
		setViewData(message);
	}
	
	protected void insertAfter(int position, String text){
		int previousPosition = view.getCaretOffset();
		this.view.setCaretOffset(position);
		this.view.insert(text);
		this.spaceAfterText += text.length();
		this.view.setCaretOffset(previousPosition);
	}
	
	protected void insertBefore(int position, String text){
		int previousPosition = view.getCaretOffset();
		this.view.setCaretOffset(position);
		this.view.insert(text);
		this.spaceBeforeText += text.length();
		this.view.setCaretOffset(previousPosition);
	}
	
	protected String makeInsertionString(int length, char c){
		String insertionString = "";
		for(int i = 0; i < length; i++){
			insertionString += c;
		}
		
		return insertionString;
	}
	
	protected void setFontRange(int start, int length, int style){
		StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = length;
		styleRange.fontStyle = style;
		this.view.setStyleRange(styleRange);
	}
	
	protected void updateRange(StyleRange style, int start, int length){
		style.start = start;
		style.length = length;
		this.view.setStyleRange(style);
	}
	
	protected int getFontWidth(){
		GC gc = new GC(this.view);
		FontMetrics fm =gc.getFontMetrics();
		return fm.getAverageCharWidth();
	}
	
	protected Element getBrlNode(Node n){
		Element e = (Element)n.getParent();
		int index = e.indexOf(n);
		if(index != e.getChildCount() - 1)
			return (Element)e.getChild(index + 1);
		else
			return null;
	}
	
	protected int[] getIndexArray(Element e){
		int[] indexArray;
		try {
			String arr = e.getAttributeValue("index");
			String[] tokens = arr.split(" ");
			indexArray = new int[tokens.length];
			for(int i = 0; i < tokens.length; i++){
				indexArray[i] = Integer.valueOf(tokens[i]);
			}
			return indexArray;
		}
		catch(Exception ex){
			return null;
		}
	}
	
	public int getWordCount(){
		String text = view.getText();
		int wordCount = 0;
		int i = 0;
		while(i < text.length() && text.charAt(i) == ' '){
			i++;
		}
		
		for(;i < text.length(); i++){
			if(text.charAt(i) == ' '){
				wordCount++;
				while(i < text.length() && text.charAt(i) == ' '){
					i++;
				}
			}
		}
		
		if(text.charAt(text.length() - 1) == ' ')
			wordCount--;
		
		return wordCount;
	}
	
	protected abstract void setViewData(Message message);
}
