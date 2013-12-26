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
import nu.xom.Text;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.messages.Message;
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
	protected int charWidth;
	protected int spaceBeforeText, spaceAfterText;
	public int positionFromStart, cursorOffset, words;
	public static int currentLine;
	protected boolean locked;
	protected static int currentAlignment;
	protected static int topIndex;
	protected Group group;
	
	public AbstractView() {
	}

	public AbstractView(Group group, int left, int right, int top, int bottom) {
		this.group = group;
		view = new StyledText(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		setLayout(left, right, top, bottom);
		view.addModifyListener(viewMod);
	}

	// Better use a ModifyListener to set the change flag.
	ModifyListener viewMod = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			hasChanged = true;
		}
	};
	
	protected void setLayout(int left, int right, int top, int bottom){
		FormData location = new FormData();
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		view.setLayoutData(location);
	}
	
	public void increment(Manager dm){
		sendIncrementCurrent(dm);
	}
	
	protected void sendIncrementCurrent(Manager dm){
		Message message = Message.createIncrementMessage();
		dm.dispatch(message);
		setViewData(message);
	}
	
	public void decrement(Manager dm){
		sendDecrementCurrent(dm);
	}
	
	protected void sendDecrementCurrent(Manager dm){
		Message message = Message.createDecrementMessage();
		dm.dispatch(message);
		setViewData(message);
	}
	
	protected void insertAfter(int position, String text){
		int previousPosition = view.getCaretOffset();
		view.setCaretOffset(position);
		view.insert(text);
		this.spaceAfterText += text.length();
		view.setCaretOffset(previousPosition);
	}
	
	protected void insertBefore(int position, String text){
		int previousPosition = view.getCaretOffset();
		view.setCaretOffset(position);
		view.insert(text);
		this.spaceBeforeText += text.length();
		view.setCaretOffset(previousPosition);
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
		
		if(style == SWT.UNDERLINE_SINGLE)
			styleRange.underline = true;
		else
			styleRange.underline = false;
			
		view.setStyleRange(styleRange);
	}
	
	protected void updateRange(StyleRange style, int start, int length){
		style.start = start;
		style.length = length;
		view.setStyleRange(style);
	}
	
	protected int getFontWidth(){
		GC gc = new GC(this.view);
		FontMetrics fm =gc.getFontMetrics();
		gc.dispose();
		return fm.getAverageCharWidth();
	}
	
	protected int getFontHeight(){
		GC gc = new GC(this.view);
		FontMetrics fm =gc.getFontMetrics();
		gc.dispose();
		return fm.getHeight();
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
	
	protected void saveStyleState(int start){
		int line = this.view.getLineAtOffset(start);
		currentAlignment = view.getLineAlignment(line);
	}
	
	protected void restoreStyleState(int start, int end){
		int line = view.getLineAtOffset(start);
		int lineCount = view.getLineAtOffset(end) - view.getLineAtOffset(start);
		view.setLineAlignment(line, lineCount + 1, currentAlignment);
	}
	
	protected void handleLineWrap(int pos, String text, int indent, boolean skipFirstLine){
		int newPos;
		int i = 0;
		
		while(i < text.length() && text.charAt(i) == '\n'){
			i++;
		}
		
		if(!skipFirstLine){
			view.setLineIndent(view.getLineAtOffset(pos + i), 1, indent * charWidth);
		}
	
		for(; i < text.length(); i++){
			if(text.charAt(i) == '\n' && i != text.length() - 1){
				i++;
				newPos = pos + i;
				//this.view.setLineIndent(this.view.getLineAtOffset(newPos), 1, this.view.getLineIndent(this.view.getLineAtOffset(newPos)) + (indent * this.charWidth));
				view.setLineIndent(view.getLineAtOffset(newPos), 1, indent * charWidth);
			}
		}
	}
	
	protected void checkForLineBreak(Element parent, Node n){
		if(parent.indexOf(n) > 0){
			int priorIndex = parent.indexOf(n) - 1;
			if(isElement(parent.getChild(priorIndex)) && ((Element)parent.getChild(priorIndex)).getLocalName().equals("br")){
				insertBefore(this.spaceBeforeText + this.total, "\n");
			}
		}
		else if(parent.indexOf(n) == 0 && parent.getAttributeValue("semantics").contains("action")){
			Element child = parent;
			Element newParent = (Element)parent.getParent();
			if(newParent.indexOf(child) > 0){
				int priorIndex = newParent.indexOf(child) - 1;
				if(isElement(newParent.getChild(priorIndex)) && ((Element)newParent.getChild(priorIndex)).getLocalName().equals("br")){
					insertBefore(this.spaceBeforeText + this.total, "\n");
				}
			}
			else if(newParent.indexOf(child) == 0 && newParent.getAttributeValue("semantics").contains("action"))
				checkForLineBreak((Element)newParent.getParent(), newParent);
		}
	}
	
	protected int getLineNumber(int startOffset, String text){
		int startLine = view.getLineAtOffset(startOffset);
		int endLine = view.getLineAtOffset(startOffset + text.length());
		
		return (endLine - startLine) + 1;
	}
	
	public void setcharWidth(){
		charWidth = getFontWidth();
	}
	
	public void setTopIndex(int line){
		setListenerLock(true);
		view.setTopIndex(line);
		topIndex = line;
		setListenerLock(false);
	}
	
	public void resetCursor(int pos){
		setListenerLock(true);
		view.setFocus();
		view.setCaretOffset(pos);
		setListenerLock(false);
	}
	
	protected void recreateView(Group group, int left, int right, int top, int bottom){
		view.dispose();
		view = new StyledText(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		view.addModifyListener(viewMod);
		setLayout(left, right, top, bottom);
		view.getParent().layout();
	}
	
	public void positionScrollbar(int topIndex){
		setListenerLock(true);
		group.setRedraw(false);
		view.setTopIndex(topIndex);
		group.setRedraw(true);
		group.getDisplay().update();
		setListenerLock(false);
	}
	
	protected void sendStatusBarUpdate(Manager dm, int line){
		String statusBarText = "Line: " + String.valueOf(line + 1);
		
		if(view.getLineIndent(line) > 0){
			statusBarText += " Indent: " + ((view.getLineIndent(line) / charWidth)); 
		}
		
		if(view.getLineAlignment(line) != SWT.LEFT){
			if(view.getLineAlignment(line) == SWT.CENTER)
				statusBarText += " Alignment: Center";
			else if(view.getLineAlignment(line) == SWT.RIGHT)
				statusBarText += " Alignment: Right";
		}
		
		Message statusMessage = Message.createUPdateStatusbarMessage(statusBarText + " Words: " + words);
		dm.dispatch(statusMessage);
		currentLine = view.getLineAtOffset(view.getCaretOffset());
	}
	
	public void checkStatusBar(String sender, Manager dm){
		if(!getLock()){
			if(topIndex != view.getTopIndex()){
				topIndex = view.getTopIndex();
				Message scrollMessage = Message.createUpdateScollbarMessage(sender, view.getOffsetAtLine(topIndex));
				dm.dispatch(scrollMessage);
			}
		}
	}
	
	public void insertText(int start, String text){
		setListenerLock(true);
		int originalPosition = view.getCaretOffset();
		view.setCaretOffset(start);
		view.insert(text);
		view.setCaretOffset(originalPosition);
		setListenerLock(false);
	}
	
	protected void setLinesBefore(int start, Styles style){
		String textBefore = makeInsertionString(Integer.valueOf((String)style.get(StylesType.linesBefore)),'\n');
		insertBefore(start, textBefore);
	}
	
	protected void setLinesAfter(int start, Styles style){
		String textAfter = makeInsertionString(Integer.valueOf((String)style.get(StylesType.linesAfter)), '\n');
		insertAfter(start, textAfter);
	}
	
	protected void setFirstLineIndent(int start, Styles style){
		int margin = 0;
		int indentSpaces = Integer.valueOf((String)style.get(StylesType.firstLineIndent));
		
		if(style.contains(StylesType.leftMargin)){
			margin = Integer.valueOf((String)style.get(StylesType.leftMargin));
			indentSpaces = margin + indentSpaces; 
		}
		int startLine = view.getLineAtOffset(start);
		view.setLineIndent(startLine, 1, indentSpaces * charWidth);
	}
	
	protected void setAlignment(int start, int end, Styles style){
		int startLine = view.getLineAtOffset(start);
		view.setLineAlignment(startLine, getLineNumber(start, view.getTextRange(start, (end - start))),  Integer.valueOf((String)style.get(StylesType.format)));	
	}
	
	public void clearRange(int start, int length){
		setListenerLock(true);
		view.replaceTextRange(start, length, "");
		setListenerLock(false);
	}
	
	protected boolean isElement(Node n){
		return (n instanceof Element);
	}
	
	protected boolean isText(Node n){
		return (n instanceof Text);
	}
	
	protected abstract void setViewData(Message message);
	public abstract void resetView(Group group);
	public abstract void initializeListeners(final Manager dm);
}
