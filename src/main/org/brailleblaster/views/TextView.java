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

import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractContent;
import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.wordprocessor.BBEvent;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Group;


public class TextView extends AbstractView {
	public int total;
	private int oldCursorPosition = -1;
	private int currentChar;
	private int currentStart, currentEnd, previous, next;
	private int currentChanges = 0;
	private boolean textChanged;
	
	public TextView (Group documentWindow) {
		super (documentWindow, 16, 57, 0, 100);
		this.total = 0;
	}

	public void initializeListeners(final DocumentManager dm){		
		view.addVerifyKeyListener(new VerifyKeyListener(){
			@Override
			public void verifyKey(VerifyEvent e) {
				oldCursorPosition = view.getCaretOffset();
				currentChar = e.keyCode;
			}		
		});

		view.addExtendedModifyListener(new ExtendedModifyListener(){
			@Override
			public void modifyText(ExtendedModifyEvent e) {
					if(e.length > 0){
						handleTextInsertion(e.length);
					}
					else {
						int offset = view.getCaretOffset() - oldCursorPosition;
						if(currentChar == SWT.BS){
							if(oldCursorPosition == currentStart && currentStart != currentEnd) { 
								currentStart += offset;
								currentEnd += offset;
								incrementNext(offset);
								sendDeleteSpaceMessage(dm, oldCursorPosition, offset);
							}
							else if(currentStart == currentEnd && currentStart + offset == previous){
								currentStart += offset;
								makeTextViewChange(offset);
							}
							else {
								makeTextViewChange(offset);
							}
						}
						else if(currentChar == SWT.DEL){
							if(offset == 0)
								offset = -1;
							
							if(currentStart == currentEnd){
								incrementNext(offset);
								currentChanges += offset;
								textChanged = true;
							}
							else if(view.getCaretOffset() == currentEnd && view.getCaretOffset() != next){
								sendDeleteSpaceMessage(dm, oldCursorPosition + 1, -1);
								incrementNext(offset);
							}
							else if(view.getCaretOffset() == currentEnd && view.getCaretOffset() == next) {
								if(textChanged == true){
									sendUpdate(dm);	
								}
								view.setCaretOffset(view.getCaretOffset() + 1);
								setCurrent(dm);
								makeTextViewChange(offset);
								view.setCaretOffset(view.getCaretOffset() - 1);
							}
							else {
								makeTextViewChange(offset);
							}
						}
						
						if(currentStart == currentEnd && (currentStart == previous || currentStart == next)){
							if(currentStart == currentEnd && textChanged == true){
								sendUpdate(dm);	
								setCurrent(dm);
							}
						}				
					}		
			}
		});	
		
		view.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				Message message = new Message(BBEvent.GET_CURRENT);
				message.put("sender", "text");
			    message.put("offset",view.getCaretOffset());
				dm.dispatch(message);
				setViewData(message);
				if(oldCursorPosition == -1 || oldCursorPosition < currentStart || oldCursorPosition > currentEnd)
					view.setCaretOffset((Integer)message.getValue("start"));
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(textChanged == true)
					sendUpdate(dm);	
			}
		});
		
		view.addCaretListener(new CaretListener(){
			@Override
			public void caretMoved(CaretEvent e) {
				if(currentChar == SWT.ARROW_DOWN || currentChar == SWT.ARROW_LEFT || currentChar == SWT.ARROW_RIGHT || currentChar == SWT.ARROW_UP){
					if(e.caretOffset > currentEnd || e.caretOffset < currentStart){
						if(textChanged == true){
							sendUpdate(dm);
						}
						setCurrent(dm);
					}
				}
			}
		});
		
		view.addMouseListener(new MouseListener(){
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
	}
	
	private void sendUpdate(DocumentManager dm){
			Message updateMessage = new Message(BBEvent.UPDATE);
			updateMessage.put("offset", oldCursorPosition);
			if(next == -1)
				updateMessage.put("newPosition", currentStart);
			
			updateMessage.put("newText", getString(currentStart, currentEnd - currentStart));
			updateMessage.put("length", currentChanges);
			dm.dispatch(updateMessage);
			currentChanges = 0;
			textChanged = false;
	}
	
	private void setCurrent(DocumentManager dm){
		Message message = new Message(BBEvent.SET_CURRENT);
		message.put("offset", view.getCaretOffset());
		dm.dispatch(message);
		setViewData(message);
	}
	
	private void sendDeleteSpaceMessage(DocumentManager dm, int start, int offset){
		Message message = new Message(BBEvent.TEXT_DELETION);
		message.put("offset", start);
		message.put("length", offset);
		message.put("deletionType", currentChar);
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
	
	private void setViewData(Message message){
		currentStart = (Integer)message.getValue("start");
		currentEnd = (Integer)message.getValue("end");
		previous = (Integer)message.getValue("previous");
		next = (Integer)message.getValue("next");
	}
	
	private void handleTextInsertion(int length){
		currentEnd += length;
		incrementNext(length);
		currentChanges += length;
		textChanged = true;
	}
	
	private void makeTextViewChange(int offset){
		currentEnd += offset;
		incrementNext(offset);
		currentChanges += offset;
		textChanged = true;
	}
	
	private void incrementNext(int offset){
		if(next != -1){
			next += offset;
		}
	}
	
	public void setCursor(int offset){
		view.setFocus();
		view.setCaretOffset(offset);
	}

	public void setText(Node n, LinkedList<TextMapElement>list){
		view.append(n.getValue() + "\n");
		list.add(new TextMapElement(this.total, n));
		this.total += n.getValue().length() + 1;
	}

	public String getString(int start, int length){
		return view.getTextRange(start, length);
	}

	/* This is a derivative work from 
	 * org.eclipse.swt.custom.DefaultContent.java 
	*/
	private class TextContent extends AbstractContent {
	}

}