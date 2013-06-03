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

package org.brailleblaster.views;

import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.wordprocessor.BBEvent;
import org.brailleblaster.wordprocessor.BBSemanticsTable;
import org.brailleblaster.wordprocessor.BBSemanticsTable.Styles;
import org.brailleblaster.wordprocessor.BBSemanticsTable.StylesType;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Group;

public class TextView extends AbstractView {
	private int oldCursorPosition = -1;
	private int currentChar;
	private int currentStart, currentEnd, previousEnd, nextStart, selectionStart, selectionLength;
	private int currentChanges = 0;
	private int escapeChars;
	private boolean textChanged;
	private BBSemanticsTable stylesTable;
	private StyleRange range;
	private int[] selectionArray;
	private SelectionListener selectionListener;
	private VerifyKeyListener verifyListener;
	private ExtendedModifyListener modListener;
	private FocusListener focusListener;
	private CaretListener caretListener;
	private TraverseListener traverseListener;
	private MouseListener mouseListener;
	private int originalStart, originalEnd;
	private String charAtOffset;
	
	public TextView (Group documentWindow, BBSemanticsTable table) {
		super (documentWindow, 16, 57, 0, 100);
		this.stylesTable = table;
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
		this.escapeChars = 0;
	}

	public void initializeListeners(final DocumentManager dm){	
		view.addSelectionListener(selectionListener = new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionArray = view.getSelectionRanges();
				if(selectionArray[1] > 0){
					setSelection(selectionArray[0], selectionArray[1]);
					charAtOffset = view.getText(selectionArray[0], selectionArray[0]);
				}
			}			
		});
		
		view.addVerifyKeyListener(verifyListener = new VerifyKeyListener(){
			@Override
			public void verifyKey(VerifyEvent e) {
				oldCursorPosition = view.getCaretOffset();
				currentChar = e.keyCode;
				
				if(e.stateMask == SWT.CONTROL && e.keyCode == 'a'){
					selectAll();
				}
				
				if(oldCursorPosition == currentStart && oldCursorPosition != previousEnd && e.character == SWT.BS && view.getLineAlignment(view.getLineAtOffset(currentStart)) != SWT.LEFT ){
					Message message = new Message(BBEvent.ADJUST_ALIGNMENT);
					message.put("sender", "text");
					if(view.getLineAlignment(view.getLineAtOffset(currentStart)) == SWT.RIGHT){
						view.setLineAlignment(view.getLineAtOffset(currentStart), 1, SWT.CENTER);
						message.put("alignment", SWT.CENTER);
					}
					else {
						view.setLineAlignment(view.getLineAtOffset(currentStart), 1, SWT.LEFT);
						message.put("alignment", SWT.LEFT);
					}
					dm.dispatch(message);
					e.doit = false;
					setSelection(-1, -1);
				}
				/*
				if(oldCursorPosition == currentStart && oldCursorPosition != previousEnd && e.character == SWT.BS && view.getLineIndent(view.getLineAtOffset(currentStart)) != 0 && currentStart != currentEnd){
					Message message = new Message(BBEvent.ADJUST_INDENT);
					message.put("sender", "text");
					message.put("indent", 0);
					view.setLineIndent(view.getLineAtOffset(currentStart), 1, 0);
					dm.dispatch(message);
					e.doit = false;
				}
				*/
				if(selectionLength > 0){
					saveStyleState(selectionStart);
				}
				else
					saveStyleState(currentStart);
			}		
		});
		
		view.addExtendedModifyListener(modListener = new ExtendedModifyListener(){
			@Override
			public void modifyText(ExtendedModifyEvent e) {
				if(!getLock()){
					if(e.length > 0){
						handleTextEdit(dm, e);
					}
					else {
						handleTextDeletion(dm, e);
					}
				}
			}
		});	
		
		view.addFocusListener(focusListener = new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				Message message = new Message(BBEvent.GET_CURRENT);
				message.put("sender", "text");
			    message.put("offset",view.getCaretOffset());
				dm.dispatch(message);
				setViewData(message);
				if(oldCursorPosition == -1 && positionFromStart == 0){
					view.setCaretOffset((Integer)message.getValue("start"));
				}

				sendStatusBarUpdate(dm);
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(textChanged == true)
					sendUpdate(dm);	
				
				setPositionFromStart();
				Message message = new Message(BBEvent.UPDATE_CURSORS);
				message.put("sender", "text");
				dm.dispatch(message);
			}
		});
		
		view.addCaretListener(caretListener = new CaretListener(){
			@Override
			public void caretMoved(CaretEvent e) {
				if(!getLock()){
					if(currentChar == SWT.ARROW_DOWN || currentChar == SWT.ARROW_LEFT || currentChar == SWT.ARROW_RIGHT || currentChar == SWT.ARROW_UP || currentChar == SWT.PAGE_DOWN || currentChar == SWT.PAGE_UP){
						if(e.caretOffset >= currentEnd || e.caretOffset < currentStart){
							if(textChanged == true){
								sendUpdate(dm);
							}
							if(view.getCaretOffset() > currentEnd && view.getCaretOffset() < nextStart)
								charAtOffset = view.getText(view.getCaretOffset(), view.getCaretOffset());
							setCurrent(dm);
						}
					}
				}
				
				if(view.getLineAtOffset(view.getCaretOffset()) != currentLine){
					sendStatusBarUpdate(dm);
				}
			}
		});
		
		view.addMouseListener(mouseListener = new MouseListener(){
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if( (view.getCaretOffset() > currentEnd || view.getCaretOffset() < currentStart) && textChanged == true){
					sendUpdate(dm);
				}
				
				if(view.getCaretOffset() > currentEnd || view.getCaretOffset() < currentStart){
					setCurrent(dm);
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub			
			}		
		});
		
		view.addTraverseListener(traverseListener = new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.stateMask == SWT.MOD1 && e.keyCode == SWT.ARROW_DOWN && nextStart != -1){
					if(textChanged == true){
						sendUpdate(dm);
					}
					sendIncrementCurrent(dm);
					view.setCaretOffset(currentStart);
					e.doit = false;
				}
				else if(e.stateMask == SWT.MOD1 && e.keyCode == SWT.ARROW_UP && previousEnd != -1){
					if(textChanged == true){
						sendUpdate(dm);
					}
					sendDecrementCurrent(dm);
					view.setCaretOffset(currentStart);
					e.doit = false;
				}
			}
		});

		setListenerLock(false);
	}
	
	public void removeListeners(){
		view.removeSelectionListener(selectionListener);
		view.removeExtendedModifyListener(modListener);
		view.removeFocusListener(focusListener);
		view.removeVerifyKeyListener(verifyListener);
		view.removeMouseListener(mouseListener);
		view.removeTraverseListener(traverseListener);
		view.removeCaretListener(caretListener);
	}
	
	private void sendUpdate(DocumentManager dm){
			Message updateMessage = new Message(BBEvent.UPDATE);
			updateMessage.put("offset", view.getCaretOffset());
			updateMessage.put("newText", getString(currentStart, currentEnd - currentStart));
			updateMessage.put("length", originalEnd - originalStart);
		//	updateMessage.put("length", currentChanges);
			dm.dispatch(updateMessage);
			words += (Integer)updateMessage.getValue("diff");
			sendStatusBarUpdate(dm);
			currentChanges = 0;
			textChanged = false;
			restoreStyleState(currentStart);
	}
	
	private void setCurrent(DocumentManager dm){
		Message message = new Message(BBEvent.SET_CURRENT);
		message.put("offset", view.getCaretOffset());
		if(charAtOffset != null)
			message.put("char", charAtOffset);
		dm.dispatch(message);
		setViewData(message);
		charAtOffset = null;
	}
	
	private void sendDeleteSpaceMessage(DocumentManager dm, int offset, int key){
		Message message = new Message(BBEvent.TEXT_DELETION);
		message.put("length", offset);
		message.put("deletionType", key);
		message.put("update", false);
		dm.dispatch(message);
		
		if(message.getValue("update").equals(true)){								
			currentChanges += offset;
			if(textChanged == true){
				sendUpdate(dm);
			}
			setCurrent(dm);
			currentEnd += offset;
			incrementNext(offset);
			textChanged = true;
		}
	}
	
	private void sendAdjustRangeMessage(DocumentManager dm, String type, int position){
		Message adjustmentMessage = new Message(BBEvent.ADJUST_RANGE);
		adjustmentMessage.put(type, position);
		dm.dispatch(adjustmentMessage);
		
		if(type.equals("start")){
			currentStart -= position;
			originalStart -= position;
		}
		else {
			currentEnd += position;
			nextStart += position;
		}
	}
	
	private void sendStatusBarUpdate(DocumentManager dm){
		Message statusMessage = new Message(BBEvent.UPDATE_STATUSBAR);
		statusMessage.put("line", "Line: " + String.valueOf(view.getLineAtOffset(view.getCaretOffset()) + 1) + " Words: " + words);
		dm.dispatch(statusMessage);
		currentLine = view.getLineAtOffset(view.getCaretOffset());
	}
	
	protected void setViewData(Message message){
		currentStart = (Integer)message.getValue("start");
		currentEnd = (Integer)message.getValue("end");
		previousEnd = (Integer)message.getValue("previous");
		nextStart = (Integer)message.getValue("next");
		
		originalStart = currentStart;
		originalEnd = currentEnd;
		
		if(currentStart < view.getCharCount()){
			range = getStyleRange();
		}
	}
	
	private void makeTextChange(int offset){
		currentEnd += offset;
		incrementNext(offset);
		currentChanges += offset;
		textChanged = true;
	}
	
	private void incrementNext(int offset){
		if(nextStart != -1){
			nextStart += offset;
		}
	}
	
	private void shiftLeft(int offset){
		currentStart += offset;
		currentEnd += offset;
		nextStart+= offset;
	}
	
	public void setCursor(int offset){
		view.setFocus();
		view.setCaretOffset(offset);
	}

	public void setText(Node n, LinkedList<TextMapElement>list){
		String key = this.stylesTable.getKeyFromAttribute((Element)n.getParent());
		Styles style = this.stylesTable.makeStylesElement(key, n);
		
		view.append(n.getValue());
		handleStyle(style, n);
		
		list.add(new TextMapElement(this.spaceBeforeText + this.total, this.spaceBeforeText + this.total + n.getValue().length() + this.escapeChars,n));
		this.total += this.spaceBeforeText + n.getValue().length() + this.spaceAfterText + this.escapeChars;
		this.spaceAfterText = 0;
		this.spaceBeforeText = 0;
		this.escapeChars = 0;
		view.setCaretOffset(0);
		words += getWordCount(n.getValue());
	}
	
	public void reformatText(Node n, Message message, DocumentManager dm){
		int pos = view.getCaretOffset();
		setListenerLock(true);

		int indent = view.getLineIndent(view.getLineAtOffset(currentStart));
		StyleRange range = getStyleRange();
		//String reformattedText =  appendToView(n).substring(1);

		view.replaceTextRange(currentStart, currentEnd - currentStart, n.getValue());
		message.put("length", (n.getValue().length() + this.spaceAfterText) - (Integer)message.getValue("length"));
		view.setLineIndent(view.getLineAtOffset(currentStart), 1, indent);
		checkStyleRange(range);
	
		view.setCaretOffset(pos);		
		this.spaceAfterText = 0;
		this.spaceBeforeText = 0;
		this.escapeChars = 0;
		setListenerLock(false);
	}
	
	private String appendToView(Node n){
		String text = "";
		Element brl = getBrlNode(n);
		int start = 0;
		int end = 0;
		int totalLength = 0;
		
		if(brl != null){
			int[] indexes = getIndexArray(brl);
			if(indexes != null){
				for(int i = 0; i < brl.getChildCount(); i++){
					if(brl.getChild(i) instanceof Text){
						if(i > 0 && ((Element)brl.getChild(i - 1)).getLocalName().equals("newline")){
							String brltext = brl.getChild(i).getValue();						
							totalLength += brltext.length();
							end = indexes[totalLength - 1];
							if(totalLength == indexes.length){
								text += "\n" + n.getValue().substring(start);
								if(start == 0)
									this.spaceBeforeText++;
								else
									this.escapeChars++;
							}
							else{
								    end += indexes[totalLength] - end;
									text += "\n" + n.getValue().substring(start, end);
									if(start == 0)
										this.spaceBeforeText++;
									else
										this.escapeChars++;
							}
							start = end;
						}
						else {
							String brltext = brl.getChild(i).getValue();
							totalLength += brltext.length();
							end = indexes[totalLength - 1];
							if(totalLength == indexes.length){
								text += n.getValue().substring(start);
							}
							else{
								end += indexes[totalLength] - end;
								text += n.getValue().substring(start, end);
							}
							start = end;
						}
					}
				}
			}
			else {
					text += "\n" + n.getValue();
					this.spaceBeforeText++;
			}
		}
		else {
			text += n.getValue();
		}
		
		return text;
	}
	
	private void handleStyle(Styles style, Node n){
		String viewText = n.getValue();
		
		Element parent = (Element)n.getParent();
		if(parent.indexOf(n) > 0){
			int priorIndex = parent.indexOf(n) - 1;
			if(parent.getChild(priorIndex) instanceof Element && ((Element)parent.getChild(priorIndex)).getLocalName().equals("br")){
				insertBefore(this.spaceBeforeText + this.total, "\n");
			}
		}
		
		for (StylesType styleType : style.getKeySet()) {
			switch(styleType){
				case linesBefore:
					if(isFirst(n)){
						String textBefore = makeInsertionString(Integer.valueOf((String)style.get(styleType)),'\n');
						insertBefore(this.total - this.spaceBeforeText, textBefore);
					}
					break;
				case linesAfter:
					if(isLast(n)){
						String textAfter = makeInsertionString(Integer.valueOf((String)style.get(styleType)), '\n');
						insertAfter(this.spaceBeforeText + this.total + viewText.length() + this.spaceAfterText, textAfter);
					}
					break;
				case firstLineIndent: 
					if(isFirst(n)){
						insertBefore(this.spaceBeforeText + this.total, "\t");	
					//	int spaces = Integer.valueOf((String)style.get(styleType));
					//	this.view.setLineIndent(this.view.getLineAtOffset(this.spaceBeforeText + this.total + this.spaceAfterText + this.escapeChars) , 1, spaces * getFontWidth());
					}
					break;
				case format:
					this.view.setLineAlignment(this.view.getLineAtOffset(this.spaceBeforeText + this.total + this.spaceAfterText), 1, Integer.valueOf((String)style.get(styleType)));	
					break;	
				case Font:
					 setFontRange(this.total, n.getValue().length() + this.spaceAfterText, SWT.ITALIC);
					 break;
				case leftMargin:
			//		this.view.setLineWrapIndent(this.view.getLineAtOffset(this.spaceBeforeText + this.total), 1, this.view.getLineIndent(this.view.getLineAtOffset(this.spaceBeforeText + this.total))+ (2 * getFontWidth()));
					break;
				default:
					System.out.println(styleType);
			}
		}
		
		if(parent.getAttributeValue("semantics").equals("action,no") || parent.getAttributeValue("semantics").equals("action,italicx")){
			Element grandParent = (Element)parent.getParent();
			while(grandParent.getAttributeValue("semantics").equals("action,no") || grandParent.getAttributeValue("semantics").equals("action,italicx")){
				grandParent = (Element)grandParent.getParent();
			}
			
			if(isLast(n) && grandParent.indexOf(parent) == grandParent.getChildCount() - 1){
				insertAfter(this.spaceBeforeText + this.total + n.getValue().length() + this.spaceAfterText, "\n");
			}
		}
		else if(isLast(n)){
			Elements els = parent.getChildElements();
			if(els.size() > 0 && els.get(els.size() - 1).getLocalName().equals("brl"))
				insertAfter(this.spaceBeforeText + this.total + n.getValue().length() + this.spaceAfterText, "\n");
		}
	}
	
	private void handleTextEdit(DocumentManager dm, ExtendedModifyEvent e){
		int changes = e.length;
		int replacedTextLength = e.replacedText.length();
		int placeholder;
		if(replacedTextLength > 0){
			if(e.start < currentStart){
				placeholder = view.getCaretOffset();
				view.setCaretOffset(e.start);
				setCurrent(dm);
				view.setCaretOffset(placeholder);
			}
			
			if(e.start + replacedTextLength > currentEnd){
				view.setCaretOffset(e.start);
				setCurrent(dm);
				
				if(currentChanges > 0){
					sendUpdate(dm);
				}
				
				if(selectionStart < currentStart){
					sendAdjustRangeMessage(dm, "start", currentStart - selectionStart);
				}
				else if(selectionStart > currentEnd){
					sendAdjustRangeMessage(dm, "end", selectionStart - currentEnd);	
				}
					
				placeholder = currentStart;
				if(e.length < e.replacedText.length()){
					setSelection(selectionStart + e.length, selectionLength - e.length);
					if(selectionStart == currentEnd)
						sendUpdate(dm);
				}
				else {
					selectionLength -= currentEnd - e.start;
					makeTextChange(changes - (currentEnd - e.start));
					sendUpdate(dm);
					selectionStart = currentEnd;
				}
		
				deleteSelection(dm);
				view.setCaretOffset(placeholder);
				setCurrent(dm);
				view.setCaretOffset(e.start + e.length);
			}
			else {
				if(selectionStart < currentStart){
					sendAdjustRangeMessage(dm, "start", currentStart - selectionStart);
					changes -= replacedTextLength;
					makeTextChange(changes);
					sendUpdate(dm);
					setCurrent(dm);
				}
				else if(selectionStart > currentEnd){
					sendAdjustRangeMessage(dm, "end", selectionStart - currentEnd);	
				}
				else {				
					if(selectionLength > e.length)
						changes = e.length - selectionLength;
					
					makeTextChange(changes);
				}
			}
		}
		else {
			if(oldCursorPosition > currentEnd){
				sendAdjustRangeMessage(dm, "end", oldCursorPosition - currentEnd);
			}
			
			if(oldCursorPosition < currentStart){
				sendAdjustRangeMessage(dm, "start", currentStart - oldCursorPosition);
			}
		
			makeTextChange(changes);
		}
		
		checkStyleRange(range);
		setSelection(-1,-1);
	}
	
	private void handleTextDeletion(DocumentManager dm, ExtendedModifyEvent e){
		int offset = view.getCaretOffset() - oldCursorPosition;
		setListenerLock(true);
		if(selectionLength > 0){
			view.setCaretOffset(selectionStart);
			setCurrent(dm);
			deleteSelection(dm);
		}
		else if(currentChar == SWT.BS){
			if(oldCursorPosition == currentStart && view.getCaretOffset() >= previousEnd){
				shiftLeft(offset);
				sendDeleteSpaceMessage(dm, offset, SWT.BS);
			}
			else if(oldCursorPosition == currentStart && view.getCaretOffset() < previousEnd){
				if(textChanged == true)
					sendUpdate(dm);
				setCurrent(dm);
				makeTextChange(offset);
			}
			else if(oldCursorPosition > currentEnd){
				shiftLeft(offset);
				sendDeleteSpaceMessage(dm, offset, SWT.DEL);
			}
			else{
				makeTextChange(offset);
			}
		}
		else if(currentChar == SWT.DEL){
			if(offset == 0){
				offset = -1;
			}
			
			if(oldCursorPosition == currentEnd && oldCursorPosition < nextStart){
				nextStart += offset;
				sendDeleteSpaceMessage(dm, offset, SWT.DEL);
			}
			else if(oldCursorPosition == currentEnd && view.getCaretOffset() == nextStart){
				if(textChanged == true)
					sendUpdate(dm);
				view.setCaretOffset(view.getCaretOffset() + 1);
				setCurrent(dm);
				view.setCaretOffset(view.getCaretOffset() - 1);
				makeTextChange(offset);
			}
			else if(oldCursorPosition < currentStart && previousEnd == -1){
				shiftLeft(offset);
				sendDeleteSpaceMessage(dm, offset, SWT.BS);
			}
			else if(oldCursorPosition < currentStart && oldCursorPosition > previousEnd){
				shiftLeft(offset);
				sendDeleteSpaceMessage(dm, offset, SWT.BS);
			}
			else if(oldCursorPosition > currentEnd && oldCursorPosition < nextStart){
				shiftLeft(offset);
				sendDeleteSpaceMessage(dm, offset, SWT.DEL);
			}
			else {
				makeTextChange(offset);
			}				
		}
		
		if(currentStart == currentEnd && (currentStart == previousEnd || currentStart == nextStart)){
			if(currentStart == currentEnd && textChanged == true){
				sendUpdate(dm);	
				setCurrent(dm);
			}
			else {
				setCurrent(dm);
			}
		}
		setListenerLock(false);
	}

	private void deleteSelection(DocumentManager dm){
		if(selectionStart >= currentStart && selectionStart + selectionLength <= currentEnd){
			makeTextChange(-selectionLength);
		}
		else if(selectionStart + selectionLength > currentEnd && selectionStart + selectionLength >= nextStart || previousEnd == -1){	
			int changes = 0;
			while(selectionLength > 0){
				if(selectionStart > currentStart && selectionStart != currentEnd){
					changes = currentEnd - selectionStart;
					makeTextChange(-changes);
					selectionLength -= changes;
					sendUpdate(dm);
					setCurrent(dm);
				}
				else if(selectionStart == currentEnd && selectionStart != nextStart){
					changes = nextStart - currentEnd;

					if(selectionLength < changes)
						changes = selectionLength;
					
					sendDeleteSpaceMessage(dm, -changes, SWT.DEL);
					selectionLength -= changes;
					view.setCaretOffset(nextStart);
					setCurrent(dm);
					view.setCaretOffset(currentStart);
				}
				else if(selectionStart < currentStart && previousEnd == -1){
					changes = currentStart - selectionStart;
					selectionLength -= changes;
					sendDeleteSpaceMessage(dm, -changes, SWT.BS);
					view.setCaretOffset(currentStart);
					setCurrent(dm);
					view.setCaretOffset(currentStart);
				}
				else if(selectionStart == currentStart){
					if(currentStart == currentEnd){
						view.setCaretOffset(nextStart);
						setCurrent(dm);
						view.setCaretOffset(currentStart);	
					}
					
					if(currentEnd - currentStart < selectionLength){
						changes = currentEnd - currentStart;
						makeTextChange(-changes);
						selectionLength -= changes;
						sendUpdate(dm);
						setCurrent(dm);
					}
					else {
						makeTextChange(-selectionLength);
						sendUpdate(dm);
						selectionLength = 0;
					}
				}
				else {
					sendIncrementCurrent(dm);
				}
			}
		}
		setSelection(-1, -1);
	}
	
	private boolean isFirst(Node n){
		Element parent = (Element)n.getParent();
		if(parent.indexOf(n) == 0){
			if(parent.getLocalName().equals("em") || parent.getLocalName().equals("strong")){
				return isFirstElement(parent);
			}
			else 
				return true;
		}
		else {
			return false;
		}
	}
	
	private boolean isFirstElement(Element child){
		Element parent = (Element)child.getParent();
		while(!this.stylesTable.getKeyFromAttribute(parent).equals("para")){
			if(parent.indexOf(child) != 0)
				return false;
			
			child = parent;
			parent = (Element)parent.getParent(); 
		}
		
		if(parent.indexOf(child) == 0)
			return true;
		else
			return false;
	}
	
	private boolean isLast(Node n){
		boolean isLast = false;
		Element parent = (Element)n.getParent();
		
		for(int i = 0; i < parent.getChildCount(); i++){
			if(parent.getChild(i) instanceof Text){
				if(parent.getChild(i).equals(n)){
					isLast = true;
				}
				else{
					isLast = false;
				}
			}
		}
		
		return isLast;
	}
	
	
	public String getString(int start, int length){
		return view.getTextRange(start, length);
	}

	public void copy(){
		view.copy();
	}
	
	public void selectAll(){
		view.selectAll();
		setSelection(0, view.getCharCount());
	}
	
	public void cut(){
		view.cut();
	}
	
	public void paste(){
		view.paste();
	}
	
	public void updateCursorPosition(Message message){
		setListenerLock(true);
		setViewData(message);
		setCursorPosition(message);
		setPositionFromStart();
		setListenerLock(false);
	}
	
	private void setCursorPosition(Message message){
		int offset = (Integer)message.getValue("offset");
		if(message.contains("element")){
			Element e = getBrlNode((Node)message.getValue("element"));
			int pos;
			if(e != null){
				int [] arr = getIndexArray(e);
				if(arr == null){
					if((Integer)message.getValue("lastPosition") == 0)
						pos = currentStart;
					else
						pos = currentEnd;
				}
				else {
					if((Integer)message.getValue("lastPosition") < 0 && currentStart > 0)
						pos = currentStart + (Integer)message.getValue("lastPosition");	
					else if((Integer)message.getValue("lastPosition") == arr.length)
						pos = currentEnd;
					else if((Integer)message.getValue("lastPosition") > arr.length)
						pos = currentEnd + offset;
					else 
						pos = currentStart + arr[(Integer)message.getValue("lastPosition")] + offset;
				}
				
				view.setCaretOffset(pos);
			}
			else {
				view.setCaretOffset(currentStart);
			}
		}
	}
	
	public void setPositionFromStart(){
		int count = 0;
		positionFromStart = view.getCaretOffset() - currentStart;
		if(positionFromStart > 0 && currentStart + positionFromStart <= currentEnd){
			String text = view.getTextRange(currentStart, positionFromStart);
			count = text.length() - text.replaceAll("\n", "").length();
			positionFromStart -= count;
			cursorOffset = count;
		}
		else if(positionFromStart > 0 && currentStart + positionFromStart > currentEnd){
			String text = view.getTextRange(currentStart, positionFromStart);
			count = text.length() - text.replaceAll("\n", "").length();
			cursorOffset = (currentStart + positionFromStart) - currentEnd;
			positionFromStart = 99999;
		}
		else {
			positionFromStart -= count;
			cursorOffset = count;
		}
	}
	
	private StyleRange getStyleRange(){
		if(currentStart < view.getCharCount()){
			return view.getStyleRangeAtOffset(currentStart);
		}
		return null;
	}
	
	private void checkStyleRange(StyleRange range){
		if(range != null){
			updateRange(range, currentStart, currentEnd - currentStart);
		}
	}
	
	private void setSelection(int start, int length){
		selectionStart = start;
		selectionLength = length;
	}

	@Override
	public void resetView() {
		setListenerLock(true);
		view.setText("");
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
		this.escapeChars = 0;
		oldCursorPosition = -1;
		currentChanges = 0;
		textChanged = false;
		setListenerLock(false);
	}
}