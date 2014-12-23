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

package org.brailleblaster.perspectives.braille.views.wp;

import java.util.ArrayList;
import java.util.Map.Entry;

import nu.xom.Element;
import nu.xom.Node;

import org.brailleblaster.mathml.ImageCreator;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.views.wp.formatters.WhiteSpaceManager;
import org.brailleblaster.util.Notify;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class TextView extends WPView {
	private int oldCursorPosition = -1;
	private int currentChar;
	private int currentStart, currentEnd, previousEnd, nextStart, selectionStart, selectionLength;
	private int currentChanges = 0;
	private boolean textChanged, readOnly;
	private StyleRange range;
	private int[] selectionArray;
	
	private SelectionAdapter selectionListener;
	private TraverseListener traverseListener;
	private SelectionAdapter scrollbarListener;
	private VerifyKeyListener verifyKeyListener;
	private VerifyListener verifyListener;
	private ExtendedModifyListener modListener;
	private FocusListener focusListener;
	private CaretListener caretListener;
	private MouseAdapter mouseListener;
	private PaintObjectListener paintObjListener;
	
	private int originalStart, originalEnd;
	private TextMapElement currentElement;
	private EditMenu menu;

	private boolean multiSelected;

 	public TextView (Manager manager, SashForm sash, BBSemanticsTable table) {
		super (manager, sash, table);
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
		this.manager = manager;
		menu = new EditMenu(this, manager);
		multiSelected=false;
		readOnly = false;
	}

	@Override
	public void initializeListeners(){	
		view.addSelectionListener(selectionListener = new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {		
				setSelection();
			}			
		});
		
		view.addTraverseListener(traverseListener = new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.stateMask == SWT.MOD1 + SWT.MOD2 && e.keyCode == SWT.TAB)
					manager.setStyleTableFocus(e);
			}
			
		});
		
		view.addVerifyKeyListener(verifyKeyListener = new VerifyKeyListener(){
			@Override
			public void verifyKey(VerifyEvent e) {
				oldCursorPosition = view.getCaretOffset();
				currentChar = e.keyCode;

				if(readOnly){
					if((Character.isDigit(e.character) && !validEdit())|| (Character.isLetter(e.character) && !validEdit()) || e.keyCode == SWT.CR)
						e.doit = false;
				}
				
				if(e.stateMask == SWT.CONTROL && e.keyCode == 'a'){
					selectAll();
				}
				else if(e.stateMask == SWT.MOD1 + SWT.MOD2 && e.keyCode == SWT.TAB){
					//when shift + tab traverse is overridden to set to style table, then key event fires so disregard
					e.doit = false;
				}
				else if(e.stateMask == SWT.MOD1 && e.keyCode == 'x' && (currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement)){
					if(!validCut())
						e.doit = false;
				}
				else if(e.stateMask == SWT.MOD1 && e.keyCode == 'v' && (currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement)){
					if(!validPaste())
						e.doit = false;
				}
				else if(!readOnly && e.character == SWT.CR){
					boolean atEnd = false;
					boolean atStart = false;
					
					if(view.getCaretOffset() == currentEnd){
						if(stylesTable.isBlockElement(currentElement.parentElement()) || isLast(currentElement.n))
							atEnd = true;
					}
					else if(view.getCaretOffset() == currentStart){
						if(stylesTable.isBlockElement(currentElement.parentElement()) || isFirst(currentElement.n))
							atStart = true;
					}
					
					if(textChanged == true){
						sendUpdate();
						setCurrent(view.getCaretOffset());
					}
					
					if(atEnd) {
						Message m = Message.createInsertNodeMessage(false, false, true,"p");
						insertNewNode(m, currentEnd);			
						e.doit = false;	
					}
					else if(atStart){
						Message m = Message.createInsertNodeMessage(false, true, false,"p");
						insertNewNode(m, null);						
						e.doit = false;
					}
					else {
						Message m;
						int origLength =  getString(currentStart, view.getCaretOffset() - currentStart).length();
						if(view.getCaretOffset() == currentEnd)
							m = Message.createInsertNodeMessage(true, false, true, "p");
						else if(view.getCaretOffset() == currentStart)
							m = Message.createInsertNodeMessage(true, true, false, "p");
						else
							m = Message.createInsertNodeMessage(true, false, false, "p");
							
						m.put("originalLength", origLength);
						m.put("length", originalEnd - originalStart);
						m.put("position", getString(currentStart, view.getCaretOffset() - currentStart).replace("\n", "").length());
						int pos = view.getCaretOffset();
						manager.dispatch(m);
						setListenerLock(true);
						e.doit = false;
						setCurrent(pos + (Integer)m.getValue("length"));
						view.setCaretOffset(currentStart);
						setListenerLock(false);
					}
				}
				
				if(selectionLength <= 0 && oldCursorPosition == currentStart && oldCursorPosition != previousEnd && e.character == SWT.BS && view.getLineAlignment(view.getLineAtOffset(currentStart)) != SWT.LEFT ){					
					Message message;
					
					if(view.getLineAlignment(view.getLineAtOffset(currentStart)) == SWT.RIGHT){
						view.setLineAlignment(view.getLineAtOffset(currentStart), 1, SWT.CENTER);
						message = Message.createAdjustAlignmentMessage(Sender.TEXT,SWT.CENTER);
					}
					else {
						view.setLineAlignment(view.getLineAtOffset(currentStart), 1, SWT.LEFT);
						message = Message.createAdjustAlignmentMessage(Sender.TEXT,SWT.LEFT);
					}
					manager.dispatch(message);
					e.doit = false;
					setSelection(-1, -1);
				}
				
				if(selectionLength <= 0 && oldCursorPosition == currentStart && oldCursorPosition != previousEnd && e.character == SWT.BS && view.getLineIndent(view.getLineAtOffset(currentStart)) != 0 && currentStart != currentEnd){
					Message message = Message.createAdjustIndentMessage(Sender.TEXT, 0, view.getLineAtOffset(currentStart));
				
					view.setLineIndent(view.getLineAtOffset(currentStart), 1, 0);
					manager.dispatch(message);
					e.doit = false;
				}
				
				//Blocks text from crossing page boundaries in original markup
				if(e.keyCode == SWT.BS && !validBackspace())
					e.doit = false;
				else if(e.keyCode == SWT.DEL && !validDelete())
					e.doit = false;
			
				if(selectionLength > 0)
					saveAlignment(selectionStart);
				else
					saveAlignment(currentStart);
				
				if(currentElement.isMathML() && (e.keyCode != SWT.BS && e.keyCode != SWT.DEL && e.keyCode != SWT.ARROW_DOWN && e.keyCode != SWT.ARROW_LEFT && e.keyCode != SWT.ARROW_RIGHT && e.keyCode != SWT.ARROW_UP))
					e.doit = false;
			}		
		});
		
		view.addExtendedModifyListener(modListener = new ExtendedModifyListener(){
			@Override
			public void modifyText(ExtendedModifyEvent e) {
				if(!getLock()){
					if(e.length > 0)
						handleTextEdit(e);
					else 
						handleTextDeletion(e);
				}
			}
		});	
		
		view.addFocusListener(focusListener = new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				Message message = Message.createGetCurrentMessage(Sender.TEXT, view.getCaretOffset());
				manager.dispatch(message);
				setViewData(message);
				if(oldCursorPosition == -1 && positionFromStart == 0)
					view.setCaretOffset((Integer)message.getValue("start"));
				
				sendStatusBarUpdate(view.getLineAtOffset(view.getCaretOffset()));
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(textChanged == true)
					sendUpdate();	
				
				setPositionFromStart();
				Message message = Message.createUpdateCursorsMessage(Sender.TEXT);
				manager.dispatch(message);
			}
		});
		
		view.addCaretListener(caretListener = new CaretListener(){
			@Override
			public void caretMoved(CaretEvent e) {
				if(view.getSelectionCount() == 0)
					multiSelected = false;
				
				if(!getLock()){
					if(currentChar == SWT.ARROW_DOWN || currentChar == SWT.ARROW_LEFT || currentChar == SWT.ARROW_RIGHT || currentChar == SWT.ARROW_UP || currentChar == SWT.PAGE_DOWN || currentChar == SWT.PAGE_UP){
						if(e.caretOffset >= currentEnd || e.caretOffset < currentStart){
							if(textChanged == true){
								sendUpdate();
							}
							
							setCurrent(view.getCaretOffset());

							currentChar = ' ';
							
						}
						sendStatusBarUpdate(view.getLineAtOffset(view.getCaretOffset()));
						
					}
				}

				if(view.getLineAtOffset(view.getCaretOffset()) != currentLine)
					sendStatusBarUpdate(view.getLineAtOffset(view.getCaretOffset()));
			}
		});
		
		view.addMouseListener(mouseListener = new MouseAdapter(){
			@Override
			public void mouseDown(MouseEvent e) {
				if( (view.getCaretOffset() > currentEnd || view.getCaretOffset() < currentStart)){
					if(textChanged == true)
						sendUpdate();
				
					setCurrent(view.getCaretOffset());
				}
			}	
		});

		view.getVerticalBar().addSelectionListener(scrollbarListener = new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {		
				checkStatusBar(Sender.TEXT);
				
				if(!getLock() & scrollBarPos != view.getVerticalBar().getSelection()){
					scrollBarPos = view.getVerticalBar().getSelection();
					if(view.getVerticalBar().getSelection() == (view.getVerticalBar().getMaximum() - view.getVerticalBar().getThumb()))
						manager.incrementView();
					else if(view.getVerticalBar().getSelection() == 0)
						manager.decrementView();
				}
			}
		});
		
		// use a verify listener to dispose the images	
		view.addVerifyListener(verifyListener = new VerifyListener()  {
			@Override
			public void verifyText(VerifyEvent event) {
				if(event.doit != false && event.start != event.end && !getLock() && selectionLength != view.getCharCount()){
					TextMapElement t = manager.getElementInRange(event.start);
					StyleRange style = null;
					if(t != null)
						style = view.getStyleRangeAtOffset(t.start);
					if (style != null) {
						Image image = (Image)style.data;
						if (image != null && !image.isDisposed()) {
							saveAlignment(currentStart);
							image.dispose();
							currentChanges--;
							event.doit = false;
							sendRemoveMathML(t);
						
							if(view.getCaretOffset() != 0 && event.keyCode == SWT.BS)
								view.setCaretOffset(view.getCaretOffset() - 1);
							
							if(!(nextStart == -1 && previousEnd == -1))
								setCurrent(view.getCaretOffset());
						}
					}
				}
			}
		});	
		
		// draw images on paint event
		view.addPaintObjectListener(paintObjListener = new PaintObjectListener() {
			@Override
			public void paintObject(PaintObjectEvent event) {
				StyleRange style = event.style;
				Image image = (Image)style.data;
				if (!image.isDisposed()) {
					int x = event.x;
					int y = event.y + event.ascent - style.metrics.ascent;						
					event.gc.drawImage(image, x, y);
				}
			}
		});
		
		//handles image on dispose
		view.addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				StyleRange[] styles = view.getStyleRanges();
				for (int i = 0; i < styles.length; i++) {
					StyleRange style = styles[i];
					if (style.data != null) {
						Image image = (Image)style.data;
						if (image != null) image.dispose();
					}
				}
			}
		});
		
		view.addPaintListener(new PaintListener(){
			@Override
			public void paintControl(PaintEvent e) {
				checkStatusBar(Sender.TEXT);
			}
		});
		view.addModifyListener(viewMod);
		setListenerLock(false);
	}
	
	@Override
	public void removeListeners(){
		if(selectionListener != null) {
			view.removeModifyListener(viewMod);
			view.removeSelectionListener(selectionListener);
			view.removeExtendedModifyListener(modListener);
			view.removeTraverseListener(traverseListener);
			view.removeFocusListener(focusListener);
			view.removeVerifyKeyListener(verifyKeyListener);
			view.removeMouseListener(mouseListener);
			view.removeCaretListener(caretListener);
			view.getVerticalBar().removeSelectionListener(scrollbarListener);
			view.removePaintObjectListener(paintObjListener);
			view.removeVerifyListener(verifyListener);
		}
	}
	
	//public method to check if an update should be made before exiting or saving
	public void update(boolean forceUpdate){
		if(textChanged || forceUpdate)
			sendUpdate();
	}
	
	private void sendUpdate(){
		Message updateMessage = Message.createUpdateMessage(view.getCaretOffset(), getString(currentStart, currentEnd - currentStart), originalEnd - originalStart);
		manager.dispatch(updateMessage);
		words += (Integer)updateMessage.getValue("diff");
		currentChanges = 0;
		textChanged = false;
	}
	
	private void setCurrent(int pos){
		Message message = Message.createSetCurrentMessage(Sender.TEXT, pos, false);
		manager.dispatch(message);
		setViewData(message);
	}
	
	private void sendDeleteSpaceMessage(int start, int offset, ExtendedModifyEvent e){
		int length = selectionLength > -1 ? selectionLength : 0;
		int textPos = length == 0 ? 0 : e.replacedText.length() - length;
		
		String replacedText = e.replacedText.substring(textPos, textPos + Math.abs(offset));
		Message message = Message.createTextDeletionMessage(start, offset, replacedText, false);
		manager.dispatch(message);
		
		if(message.getValue("update").equals(true)){								
			currentChanges += offset;
			if(textChanged ){
				sendUpdate();
			}
			setCurrent(view.getCaretOffset());
			currentEnd += offset;
			incrementNext(offset);
			textChanged = true;
		}
	}
	
	private void sendAdjustRangeMessage(String type, int position){
		Message adjustmentMessage = Message.createAdjustRange(type, position);
		manager.dispatch(adjustmentMessage);
		
		if(type.equals("start")){
			currentStart -= position;
			originalStart -= position;
		}
		else {
			currentEnd += position;
			nextStart += position;
		}
	}
	
	private void sendRemoveMathML(TextMapElement t){
		Message removeMessage = Message.createRemoveNodeMessage(manager.indexOf(t), -(t.end - t.start));
		removeMessage.put("offset", currentChanges);
		manager.dispatch(removeMessage);
		currentChanges = 0;
		textChanged = false;
		if(currentEnd <= view.getCharCount())
			restoreAlignment(currentStart, currentEnd);
	}
	
	@Override
	protected void setViewData(Message message){
		currentStart = (Integer)message.getValue("start");
		currentEnd = (Integer)message.getValue("end");
		previousEnd = (Integer)message.getValue("previous");
		nextStart = (Integer)message.getValue("next");
		
		originalStart = currentStart;
		originalEnd = currentEnd;
		
		if(currentStart < view.getCharCount())
			range = getStyleRange();
		
		currentElement = (TextMapElement)message.getValue("currentElement");
		sendStatusBarUpdate(view.getLineAtOffset(view.getCaretOffset()));
		if(currentElement instanceof BrlOnlyMapElement || currentElement instanceof PageMapElement)
			readOnly = true;
		else
			readOnly = false;
	}
	
	private void makeTextChange(int offset){
		currentEnd += offset;
		incrementNext(offset);
		currentChanges += offset;
		textChanged = true;
	}
	
	private void incrementNext(int offset){
		if(nextStart != -1)
			nextStart += offset;
	}
	
	private void shiftLeft(int offset){
		currentStart += offset;
		currentEnd += offset;
		nextStart += offset;
	}
	
	public void setCursor(int offset, Manager cont){
		view.setFocus();
		view.setCaretOffset(offset);
		
		if(offset < currentStart || offset > currentEnd)
			setCurrent(view.getCaretOffset());
	}
	
	public void setText(TextMapElement t, MapList list, int index){
		Styles style = stylesTable.makeStylesElement(t.parentElement(), t.n);
		Styles prevStyle;
		if(list.size() > 0 && index != 0 && list.get(index - 1).n != null)
			prevStyle = stylesTable.makeStylesElement(list.get(index - 1).parentElement(), list.get(index - 1).n);
		else
			prevStyle = null;
		
		String newText = appendToView(t.n, true);
		int textLength = newText.length();

		view.append(newText);
		if(!(t instanceof PageMapElement))
			handleStyle(prevStyle, style, t.n, newText);
		
		t.setOffsets(spaceBeforeText + total, spaceBeforeText + total + textLength);
		total += spaceBeforeText + textLength + spaceAfterText;
		
		spaceAfterText = 0;
		spaceBeforeText = 0;
		view.setCaretOffset(0);
		words += getWordCount(t.getText());
	}
	
	public void prependText(TextMapElement t, MapList list, int index){
		Styles style = stylesTable.makeStylesElement(t.parentElement(), t.n);
		Styles prevStyle;
		if(list.size() > 0 && index != 0)
			prevStyle = stylesTable.makeStylesElement(list.get(index - 1).parentElement(), list.get(index - 1).n);
		else
			prevStyle = null;
		
		String newText = insertToView(t.n, true);
		int textLength = newText.length();

		view.insert(newText);
		handleStyle(prevStyle, style, t.n, newText);
		
		t.setOffsets(spaceBeforeText + total, spaceBeforeText + total + textLength);
		total += spaceBeforeText + textLength + spaceAfterText;
		
		spaceAfterText = 0;
		spaceBeforeText = 0;
		view.setCaretOffset(total);
		words += getWordCount(t.getText());
	}
	
	public void setMathML(MapList list, TextMapElement t){
		Element math = (Element)t.n;
		Styles style = stylesTable.makeStylesElement((Element)math.getParent(), math);
		Styles prevStyle;
		int length = 1;
		
		if(list.size() > 0)
			prevStyle = stylesTable.makeStylesElement(list.getLast().parentElement(),list.getLast().n);
		else
			prevStyle = null;
		
		int index = math.getParent().indexOf(math);
		Element brl = (Element)math.getParent().getChild(index + 1);
		
		if(brl.getChild(0) instanceof Element){
			if(((Element)brl.getChild(0)).getLocalName().equals("newpage")){
				if(brl.getChild(1) instanceof Element && ((Element)brl.getChild(1)).getLocalName().equals("newline")){
					view.append("\n");
					total++;
				}
			}
			else {
				if(((Element)brl.getChild(0)).getLocalName().equals("newline")){
					view.append("\n");
					total++;
				}
			}
		}
		
		Image image = ImageCreator.createImage(view.getDisplay(), math, getFontHeight());
		view.append(" ");

		setImageStyleRange(image, total, length);
		handleStyle(prevStyle, style, math, " ");
		
		for(int i = 1; i < brl.getChildCount(); i++){
			if(i == 1 && brl.getChild(i) instanceof Element && ((Element)brl.getChild(i)).getLocalName().equals("newline") && !(brl.getChild(0) instanceof Element)){
				view.append("\n");
				length++;
			}
			else if(i > 1 && brl.getChild(i) instanceof Element && ((Element)brl.getChild(i)).getLocalName().equals("newline")){
				view.append("\n");
				length++;
			}	
		}

		StyleRange s = view.getStyleRangeAtOffset(spaceBeforeText + total);
		setFontStyleRange(s.start, length, s);
		
		t.setOffsets(spaceBeforeText + total, spaceBeforeText + (total + length) + spaceAfterText);
		total += spaceBeforeText + length + spaceAfterText;
		spaceAfterText = 0;
		spaceBeforeText = 0;	
	}	
	
	public void prependMathML(MapList list, TextMapElement t){
		Element math = (Element)t.n;
		Styles style = stylesTable.makeStylesElement((Element)math.getParent(), math);
		Styles prevStyle;
		int length = 1;
		
		if(list.size() > 0)
			prevStyle = stylesTable.makeStylesElement(list.getLast().parentElement(),list.getLast().n);
		else
			prevStyle = null;
		
		int index = math.getParent().indexOf(math);
		Element brl = (Element)math.getParent().getChild(index + 1);
		
		if(brl.getChild(0) instanceof Element){
			if(((Element)brl.getChild(0)).getLocalName().equals("newpage")){
				if(brl.getChild(1) instanceof Element && ((Element)brl.getChild(1)).getLocalName().equals("newline")){
					view.insert("\n");
					total++;
					view.setCaretOffset(total);
				}
			}
			else {
				if(((Element)brl.getChild(0)).getLocalName().equals("newline")){
					view.insert("\n");
					total++;
					view.setCaretOffset(total);
				}
			}
		}
		
		Image image = ImageCreator.createImage(view.getDisplay(), math, getFontHeight());
		view.insert(" ");
		view.setCaretOffset(total + 1);
		
		setImageStyleRange(image, total, length);
		handleStyle(prevStyle, style, math, " ");
		
		if(spaceBeforeText > 0)
			view.setCaretOffset(view.getCaretOffset() + spaceBeforeText);
		
		for(int i = 1; i < brl.getChildCount(); i++){
			if(i == 1 && brl.getChild(i) instanceof Element && ((Element)brl.getChild(i)).getLocalName().equals("newline") && !(brl.getChild(0) instanceof Element)){
				view.insert("\n");
				length++;
				view.setCaretOffset(view.getCaretOffset() + 1);
			}
			else if(i > 1 && brl.getChild(i) instanceof Element && ((Element)brl.getChild(i)).getLocalName().equals("newline")){
				view.insert("\n");
				length++;
				view.setCaretOffset(view.getCaretOffset() + 1);
			}	
		}
		
		StyleRange s = view.getStyleRangeAtOffset(spaceBeforeText + total);
		setFontStyleRange(s.start, length, s);
		
		t.setOffsets(spaceBeforeText + total, spaceBeforeText + (total + length) + spaceAfterText);
		total += spaceBeforeText + length + spaceAfterText;
		spaceAfterText = 0;
		spaceBeforeText = 0;	
	}	
	
	public void reformatText(Node n, Message message, Manager dm){
		String reformattedText;
		Styles style = stylesTable.makeStylesElement((Element)n.getParent(), n);
		int margin = 0;
		int pos = view.getCaretOffset();
		setListenerLock(true);		
		
		if(!n.getValue().equals(""))
			reformattedText =  appendToView(n, false);
		else
			reformattedText = n.getValue();
		
		view.replaceTextRange(currentStart, currentEnd - currentStart, reformattedText);
		if(currentStart == view.getOffsetAtLine(view.getLineAtOffset(currentStart)))
			handleLineWrap(currentStart, reformattedText, 0, false);
		
		message.put("length", (reformattedText.length() + spaceAfterText) - (Integer)message.getValue("length"));
		if(style.contains(StylesType.leftMargin)){
			margin = Integer.valueOf((String)style.get(StylesType.leftMargin));
			handleLineWrap(currentStart, reformattedText, margin, style.contains(StylesType.firstLineIndent));
		}
		
		if(isFirst(n) && style.contains(StylesType.firstLineIndent) && reformattedText.length() > 0)
			setFirstLineIndent(currentStart, style);
		
		if(style.contains(StylesType.emphasis))
			setFontStyleRange(currentStart, reformattedText.length(), (StyleRange)style.get(StylesType.emphasis));
		else {
			StyleRange range = getStyleRange();
			if(range != null)
				 resetStyleRange(range);		
		}
		
		if(style.contains(StylesType.format))
			setAlignment(currentStart, currentEnd, style);
		else
			setAlignment(currentStart, currentEnd, SWT.LEFT);
	
		view.setCaretOffset(pos);		
		spaceAfterText = 0;
		spaceBeforeText = 0;
		setListenerLock(false);
	}
	
	public void refreshStyle(TextMapElement t){
		Styles style = stylesTable.makeStylesElement((Element)t.parentElement(), t.n);
		String text = view.getTextRange(t.start, t.end - t.start);
		int margin = 0;
		if(style.contains(StylesType.leftMargin)){
			margin = Integer.valueOf((String)style.get(StylesType.leftMargin));
			handleLineWrap(currentStart, text, margin, style.contains(StylesType.firstLineIndent));
		}
		
		if(isFirst(t.n) && style.contains(StylesType.firstLineIndent) && text.length() > 0)
			setFirstLineIndent(t.start, style);
		
		if(style.contains(StylesType.emphasis))
			setFontStyleRange(t.start, text.length(), (StyleRange)style.get(StylesType.emphasis));
		else {
			StyleRange range = getStyleRange();
			if(range != null)
				 resetStyleRange(range);		
		}
		
		if(style.contains(StylesType.format))
			setAlignment(currentStart, currentEnd, style);
		else
			setAlignment(currentStart, currentEnd, SWT.LEFT);
	}
	
	public void removeMathML(Message m){
		setListenerLock(true);
		
		if(view.getCharCount() > 0) 
			view.replaceTextRange((Integer)m.getValue("start"), Math.abs((Integer)m.getValue("length")), "");
			
		setListenerLock(false);
	}
	
	public void insertText(ViewInitializer vi, MapList list, int listIndex, int start, Node n){
		Styles style = stylesTable.makeStylesElement((Element)n.getParent(), n);
		String reformattedText =  appendToView(n, false);
		setListenerLock(true);
		int originalPosition = view.getCaretOffset();
		view.setCaretOffset(start);
		view.insert(reformattedText);
		vi.addElementToSection(list, new TextMapElement(start, start + reformattedText.length(), n), listIndex);
		
		int margin = 0;
		
		//reset margin in case it is not applied
		if(start == view.getOffsetAtLine(view.getLineAtOffset(start)))
			handleLineWrap(start, reformattedText, 0, false);
				
		if(style.contains(StylesType.leftMargin)) {
			margin = Integer.valueOf((String)style.get(StylesType.leftMargin));
			handleLineWrap(start, reformattedText, margin, style.contains(StylesType.firstLineIndent));
		}
					
		if(!(list.get(listIndex) instanceof BrlOnlyMapElement) && isFirst(n) && style.contains(StylesType.firstLineIndent))
			setFirstLineIndent(start, style);
		
		if(style.contains(StylesType.format))
			setAlignment(start, start + n.getValue().length(), style);
		
		if(style.contains(StylesType.emphasis))
			setFontStyleRange(start, reformattedText.length(), (StyleRange)style.get(StylesType.emphasis));

		view.setCaretOffset(originalPosition);
		setListenerLock(false);
	}
	
	public void resetElement(Message m, ViewInitializer vi, MapList list, int listIndex, int start, TextMapElement t){
		int linesBefore = 0;
		int linesAfter = 0;
		Styles style = stylesTable.makeStylesElement((Element)t.n.getParent(), t.n);
		String reformattedText;
		if(t instanceof BrlOnlyMapElement)
			reformattedText = t.getText();
		else
			reformattedText =  appendToView(t.n, false);
		
		boolean isFirst = t instanceof PageMapElement ||  t instanceof BrlOnlyMapElement || isFirst(t.n);
		boolean isLast  = t instanceof PageMapElement ||  t instanceof BrlOnlyMapElement || isLast(t.n);
		
		setListenerLock(true);
		int originalPosition = view.getCaretOffset();
		
		view.setCaretOffset(start);
		view.insert(reformattedText);
		
		vi.addElementToSection(list, t, listIndex);
		
		int margin = 0;
		
		WhiteSpaceManager wsp = new WhiteSpaceManager(manager, this, list);
		
		if(isFirst)
			linesBefore = wsp.setLinesBefore(t, start, style);	
		
		if(isLast)
			linesAfter = wsp.setLinesAfter(t, start + reformattedText.length() + linesBefore, style);
		
		t.setOffsets(start + linesBefore, linesBefore + start + reformattedText.length());
		m.put("textLength", reformattedText.length() + linesBefore + linesAfter);
		m.put("textOffset", reformattedText.length() + linesBefore + linesAfter + start);
		
		start += linesBefore;
		//reset margin in case it is not applied
		if(start == view.getOffsetAtLine(view.getLineAtOffset(start)))
			handleLineWrap(start, reformattedText, 0, false);
				
		if(style.contains(StylesType.leftMargin)) {
			margin = Integer.valueOf((String)style.get(StylesType.leftMargin));
			handleLineWrap(start, reformattedText, margin, style.contains(StylesType.firstLineIndent));
		}
					
		if(!(list.get(listIndex) instanceof BrlOnlyMapElement) && isFirst && style.contains(StylesType.firstLineIndent))
			setFirstLineIndent(start, style);
		
		if(style.contains(StylesType.format))
			setAlignment(start, start + t.n.getValue().length(), style);
		
		if(style.contains(StylesType.emphasis))
			setFontStyleRange(start, reformattedText.length(), (StyleRange)style.get(StylesType.emphasis));

		view.setCaretOffset(originalPosition);
		setListenerLock(false);
	}
		
	private String appendToView(Node n, boolean append){
		StringBuilder text = new StringBuilder();
		Element brl = getBrlNode(n);
		int start = 0;
		int end = 0;
		int totalLength = 0;
		
		try {
			if(brl != null){
				int[] indexes = getIndexArray(brl);
				if(indexes != null){
					for(int i = 0; i < brl.getChildCount(); i++){
						if(isText(brl.getChild(i))){
							if(i > 0 && ((Element)brl.getChild(i - 1)).getLocalName().equals("newline")){
								String brltext = brl.getChild(i).getValue();						
								totalLength += brltext.length();
								end = indexes[totalLength - 1];
								if(totalLength == indexes.length){
									if(start == 0 && append){
										view.append("\n");
										text.append(n.getValue().substring(start));
										spaceBeforeText++;
									}
									else if(start == 0){
										text.append(n.getValue().substring(start));
									}
									else {
										text.append("\n" + n.getValue().substring(start));
									}
								}
								else{
								    end += indexes[totalLength] - end;
									if(start == 0 && append){
										view.append("\n");
										text.append(n.getValue().substring(start, end));
										spaceBeforeText++;
									}
									else if(start == 0){
										text.append(n.getValue().substring(start, end));
									}
									else{
										text.append("\n" + n.getValue().substring(start, end));
									}
								}
								start = end;
							}
							else {
								String brltext = brl.getChild(i).getValue();
								totalLength += brltext.length();
								end = indexes[totalLength - 1];
								if(totalLength == indexes.length){
									text.append(n.getValue().substring(start));
								}
								else{
									end += indexes[totalLength] - end;
									text.append(n.getValue().substring(start, end));
								}
								start = end;
							}
						}
					}
				}
				else if(append){
					view.append("\n");
					text.append(n.getValue());
					spaceBeforeText++;
				}
				else {
					text.append(n.getValue());
				}
			}
			else {
				text.append(n.getValue());
			}
		}
		catch(StringIndexOutOfBoundsException e){
			new Notify("An error occured while translating.  Be aware the this may affect cursor accurarcy and other translations in the document.  The document is most likely not be suitable for editing. Check the logs for details");
			logger.error("Index Error:\t" + n.getParent().toXML().toString(), e);
			text.append(n.getValue().substring(start));
		}
		
		return text.toString();
	}
	
	private String insertToView(Node n, boolean append){
		StringBuilder text = new StringBuilder();
		Element brl = getBrlNode(n);
		int start = 0;
		int end = 0;
		int totalLength = 0;
		
		try {
			if(brl != null){
				int[] indexes = getIndexArray(brl);
				if(indexes != null){
					for(int i = 0; i < brl.getChildCount(); i++){
						if(isText(brl.getChild(i))){
							if(i > 0 && ((Element)brl.getChild(i - 1)).getLocalName().equals("newline")){
								String brltext = brl.getChild(i).getValue();						
								totalLength += brltext.length();
								end = indexes[totalLength - 1];
								if(totalLength == indexes.length){
									if(start == 0 && append){
										view.insert("\n");
										text.append(n.getValue().substring(start));
										spaceBeforeText++;
										view.setCaretOffset(view.getCaretOffset() + 1);
									}
									else if(start == 0){
										text.append(n.getValue().substring(start));
									}
									else {
										text.append("\n" + n.getValue().substring(start));
									}
								}
								else{
								    end += indexes[totalLength] - end;
									if(start == 0 && append){
										view.insert("\n");
										text.append(n.getValue().substring(start, end));
										spaceBeforeText++;
										view.setCaretOffset(view.getCaretOffset() + 1);
									}
									else if(start == 0){
										text.append(n.getValue().substring(start, end));
									}
									else{
										text.append("\n" + n.getValue().substring(start, end));
									}
								}
								start = end;
							}
							else {
								String brltext = brl.getChild(i).getValue();
								totalLength += brltext.length();
								end = indexes[totalLength - 1];
								if(totalLength == indexes.length){
									text.append(n.getValue().substring(start));
								}
								else{
									end += indexes[totalLength] - end;
									text.append(n.getValue().substring(start, end));
								}
								start = end;
							}
						}
					}
				}
				else if(append){
					view.insert("\n");
					text.append(n.getValue());
					spaceBeforeText++;
					view.setCaretOffset(view.getCaretOffset() + 1);
				}
				else {
					text.append(n.getValue());
				}
			}
			else {
				text.append(n.getValue());
			}
		}
		catch(StringIndexOutOfBoundsException e){
			new Notify("An error occured while translating.  Be aware the this may affect cursor accurarcy and other translations in the document.  The document is most likely not be suitable for editing. Check the logs for details");
			logger.error("Index Error:\t" + n.getParent().toXML().toString(), e);
			text.append(n.getValue().substring(start));
		}
		
		return text.toString();
	}
	
	private void handleStyle(Styles prevStyle, Styles style, Node n, String viewText){
		boolean isFirst = isFirst(n);
		
		for (Entry<StylesType, Object> entry : style.getEntrySet()) {
			switch(entry.getKey()){
				case linesBefore:
					if(isFirst && (prevStyle == null || !prevStyle.contains(StylesType.linesAfter)))
						setLinesBefore(total + spaceBeforeText, style);
					break;
				case linesAfter:
					if(isLast(n))
						setLinesAfter(spaceBeforeText + total + viewText.length(), style);
					break;
				case firstLineIndent: 
					if(isFirst && (Integer.valueOf((String)entry.getValue()) > 0 || style.contains(StylesType.leftMargin)))
						setFirstLineIndent(spaceBeforeText + total, style);
					break;
				case format:
					setAlignment(spaceBeforeText + total, spaceBeforeText + total + viewText.length(), style);
					break;	
				case emphasis:
					setFontStyleRange(total, spaceBeforeText + viewText.length(), (StyleRange)entry.getValue());
					break;
				case leftMargin:
					if(isFirst || (!isFirst && view.getLineAtOffset(spaceBeforeText + total) == view.getLineAtOffset(total)))
						handleLineWrap(spaceBeforeText + total, viewText, Integer.valueOf((String)entry.getValue()), style.contains(StylesType.firstLineIndent));
					else
						handleLineWrap(spaceBeforeText + total, viewText, Integer.valueOf((String)entry.getValue()), false);
					break;
				case name:
					break;
				case topBoxline:
				case bottomBoxline:
					break;
				default:
					System.out.println(entry.getKey());
			}
		}
	}
	
	private void handleTextEdit(ExtendedModifyEvent e){
		int changes = e.length;
		int placeholder;
		if(e.replacedText.length() > 0){
			if(e.start < currentStart){
				setCurrent(e.start);
			}
			
			if(e.start + e.replacedText.length() > currentEnd){
				view.setCaretOffset(e.start);
				setCurrent(view.getCaretOffset());
				
				if(selectionStart < currentStart)
					sendAdjustRangeMessage("start", currentStart - selectionStart);
				else if(selectionStart > currentEnd)
					sendAdjustRangeMessage("end", selectionStart - currentEnd);	
					
				placeholder = currentStart;
				if(e.length < e.replacedText.length()){
					setSelection(selectionStart + e.length, selectionLength - e.length);
					changes = currentEnd - selectionStart;
					makeTextChange(-changes);
					selectionLength -= changes;
					sendUpdate();
					setCurrent(view.getCaretOffset());
					selectionStart = currentEnd;
				}
				else {
					selectionLength -= currentEnd - e.start;
					makeTextChange(changes - (currentEnd - e.start));
					sendUpdate();
					setCurrent(view.getCaretOffset());
					selectionStart = currentEnd;
				}
		
				deleteSelection(e);
				setCurrent(placeholder);
				view.setCaretOffset(e.start + e.length);
			}
			else {
				if(selectionStart < currentStart){
					sendAdjustRangeMessage("start", currentStart - selectionStart);
					changes -= e.replacedText.length();
					makeTextChange(changes);
					sendUpdate();
					setCurrent(view.getCaretOffset());
				}
				else if(e.start + e.replacedText.length() == currentEnd){
					changes -= e.replacedText.length();
					makeTextChange(changes);
				}
				else {					
					changes = e.length - selectionLength;							
					makeTextChange(changes);
				}
			}
		}
		else {
			if(oldCursorPosition > currentEnd)
				sendAdjustRangeMessage("end", oldCursorPosition - currentEnd);
			
			if(oldCursorPosition < currentStart)
				sendAdjustRangeMessage("start", currentStart - oldCursorPosition);
		
			makeTextChange(changes);
		}
		
		checkStyleRange(range);
		setSelection(-1,-1);
	}
	
	private void handleTextDeletion(ExtendedModifyEvent e){
		int offset = view.getCaretOffset() - oldCursorPosition;
		setListenerLock(true);
		if(e.replacedText.length() > 1){
			setCurrent(view.getCaretOffset());
			deleteSelection(e);
		}
		else if(currentChar == SWT.BS){
			if(oldCursorPosition == currentStart && view.getCaretOffset() >= previousEnd)
				deleteSpaceAndShift(view.getCaretOffset(), offset, e);
			else if(oldCursorPosition == currentStart && view.getCaretOffset() < previousEnd){
				if(textChanged)
					sendUpdate();

				setCurrent(view.getCaretOffset());
				makeTextChange(offset);
			}
			else if(oldCursorPosition > currentEnd)
				deleteSpaceAndShift(view.getCaretOffset(), offset, e);
			else
				makeTextChange(offset);
		}
		else if(currentChar == SWT.DEL){
			offset = -1;
			
			if(selectionStart + selectionLength != currentEnd && oldCursorPosition == currentEnd && oldCursorPosition < nextStart){
				if(textChanged)
					sendUpdate();
				
				nextStart += offset;
				sendDeleteSpaceMessage(view.getCaretOffset(), offset, e);
			}
			else if(oldCursorPosition == currentEnd && view.getCaretOffset() == nextStart){
				if(textChanged) 	
					sendUpdate();		
				
				setCurrent(view.getCaretOffset() + 1);
				makeTextChange(offset);
			}
			else if ((previousEnd == -1 && selectionLength > 0 && selectionStart < currentStart && selectionStart + selectionLength <= currentStart)|| (oldCursorPosition < currentStart && (previousEnd == -1 || oldCursorPosition > previousEnd)))
				deleteSpaceAndShift(view.getCaretOffset(), offset, e);
			else if( (oldCursorPosition == currentEnd && nextStart == -1)|| (oldCursorPosition > currentEnd && (oldCursorPosition < nextStart || nextStart == -1)))
				deleteSpaceAndShift(view.getCaretOffset(), offset, e);
			else 
				makeTextChange(offset);
		}
		else {
			offset = -1;
			if((previousEnd == -1 || selectionStart >= previousEnd) && selectionStart + selectionLength <= currentStart)
				deleteSpaceAndShift(selectionStart, offset, e);
			else if(selectionStart == currentEnd && (nextStart == -1 || selectionStart + selectionLength <= nextStart))
				deleteSpaceAndShift(selectionStart, offset, e);
			else 
				makeTextChange(offset);
		}
		
		if(currentStart == currentEnd && (currentStart == previousEnd || currentStart == nextStart)){
			if(textChanged)
				sendUpdate();	
		
			setCurrent(view.getCaretOffset());
		}
		setListenerLock(false);
	}

	private void deleteSelection(ExtendedModifyEvent e){
		if(selectionStart >= currentStart && selectionStart + selectionLength <= currentEnd)
			makeTextChange(-selectionLength);
		else if(selectionStart + selectionLength > currentEnd && selectionStart + selectionLength >= nextStart || previousEnd == -1){	
			int changes = 0;
			while(selectionLength > 0){
				if(manager.getElementInRange(selectionStart) instanceof BrlOnlyMapElement || manager.getElementInRange(selectionStart) instanceof PageMapElement){
					TextMapElement p = manager.getElementInRange(selectionStart);
					handleReadOnlySelection(p, true);
			//		if(selectionLength <= 0)
			//			break;
				}
				else if(manager.inPrintPageRange(selectionStart + 1) ||  manager.getElementInRange(selectionStart + 1) instanceof  BrlOnlyMapElement){
					TextMapElement p = manager.getElementInRange(selectionStart + 1);
					handleReadOnlySelection(p, false);
			//		if(selectionLength <= 0)
			//			break;
				}
				else if(selectionStart  == currentEnd && nextStart == -1){
					changes= (currentEnd + selectionLength) - currentEnd;
					sendDeleteSpaceMessage(selectionStart, -changes, e);
					selectionLength -= changes;
				}
				else if(selectionStart > currentStart && selectionStart != currentEnd){
					changes = currentEnd - selectionStart;
					makeTextChange(-changes);
					selectionLength -= changes;
					sendUpdate();
					setCurrent(view.getCaretOffset());
					selectionStart = currentEnd;
				}
				else if(selectionStart == currentEnd && selectionStart != nextStart){
					if(nextStart != -1 && manager.inPrintPageRange((selectionStart + nextStart) / 2))
						changes = manager.getElementInRange((selectionStart + nextStart) / 2).start - selectionStart - 1;
					else if(nextStart != -1)
						changes = nextStart - currentEnd;
					else
						changes= (currentEnd + selectionLength) - currentEnd;

					if(selectionLength < changes)
						changes = selectionLength;
					
					sendDeleteSpaceMessage(selectionStart, -changes, e);
					if(changes != 0)
						selectionLength -= changes;
					else
						selectionLength = 0;
					
					setCurrent(nextStart);
					view.setCaretOffset(currentStart);
				}
				else if(selectionStart < currentStart && previousEnd <= -1){
					changes = currentStart - selectionStart;
					selectionLength -= changes;
					sendDeleteSpaceMessage(selectionStart, -changes, e);
					
					setCurrent(currentStart);
					view.setCaretOffset(currentStart);
				}
				else if(selectionStart < currentStart){
					changes = currentStart - selectionStart;
					selectionLength -= changes;
					sendDeleteSpaceMessage(selectionStart, -changes, e);
					
					setCurrent(currentStart);
					view.setCaretOffset(currentStart);
				}
				else if(selectionStart == currentStart){
					if(currentStart == currentEnd){
						setCurrent(nextStart);
						view.setCaretOffset(currentStart);	
					}
					
					if(currentEnd - currentStart < selectionLength){
						changes = currentEnd - currentStart;
						makeTextChange(-changes);
						selectionLength -= changes;
						if(currentElement.isMathML())
							sendRemoveMathML(currentElement);
						else
							sendUpdate();
					
						if(nextStart != -1)
							setCurrent(view.getCaretOffset());
					}
					else {
						makeTextChange(-selectionLength);
						sendUpdate();
						selectionLength = 0;
					}
				}
				else {
					incrementCurrent();
				}
			}
		}
		setSelection(-1, -1);
		
		if(currentElement instanceof PageMapElement)
			view.setCaretOffset(previousEnd);
	}
	
	private void handleReadOnlySelection(TextMapElement p, boolean partial){
		int pos = p.end;
		setListenerLock(true);
		String pageText;
		if(partial){
			pageText = p.getText().substring(selectionStart - p.start);
		}
		else {
			if(selectionStart + selectionLength > p.end)
				pageText = "\n" + p.getText();
			else
				pageText = "\n" + p.getText().substring(0, (selectionStart + selectionLength) - (selectionStart + 1));
		}
		
		while(manager.inPrintPageRange(pos + 1) ||  manager.getElementInRange(pos + 1) instanceof  BrlOnlyMapElement){
			p =  manager.getElementInRange(pos + 1);
			pos = p.end;
			if(selectionStart + selectionLength > p.end)
				pageText += "\n"+ p.getText();
			else
				pageText += "\n" + p.getText().substring(0, (selectionStart + selectionLength) - p.start);
		}
		
		if(selectionStart + selectionLength > p.end)
			pageText += "\n";
		
		view.setCaretOffset(selectionStart);
		view.insert(pageText);
		setSelection(selectionStart + 1, selectionLength - 1);
		if(selectionLength - pageText.length() - 1 <= 0 && selectionStart + pageText.length() >= view.getCharCount())
			view.replaceTextRange(view.getCharCount() -1, 1, "");		
		
		setListenerLock(false);		
		if(selectionStart + selectionLength > p.end)
			selectionLength -= pageText.length() - 1;
		else
			selectionLength -= pageText.length();
		
		selectionStart = pos + 1;
		view.setCaretOffset(selectionStart);
		
		if(manager.inPrintPageRange(view.getCaretOffset()) || manager.getElementInRange(pos + 1) instanceof  BrlOnlyMapElement){
			int start = manager.getElementInRange(view.getCaretOffset()).start;
			view.setCaretOffset(start - 1);
		}
		
		setCurrent(view.getCaretOffset());
		if(currentElement.equals(p))
			incrementCurrent();
	}
	
	//a helper method for common series of methods that typically, but not always, follow one another
	private void deleteSpaceAndShift(int start, int offset, ExtendedModifyEvent e){
		shiftLeft(offset);
		sendDeleteSpaceMessage(start, offset, e);
	}
	
	private boolean isFirst(Node n){
		Element parent = (Element)n.getParent();
		
		if(parent.indexOf(n) == 0){
			if(parent.getAttributeValue("semantics").contains("action"))
				return isFirstElement(parent);
			else 
				return true;
		}
		else {
			return false;
		}
	}
	
	private boolean isFirstElement(Element child){
		Element parent = (Element)child.getParent();
		
		while(parent.getAttributeValue("semantics").contains("action")){
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
			if(isText(parent.getChild(i))){
				if(parent.getChild(i).equals(n)){
					isLast = true;
				}
				else {
					isLast = false;
				}
			}
			else if((isElement(parent.getChild(i)) && !((Element)parent.getChild(i)).getLocalName().equals("brl"))){
				if(parent.getChild(i).equals(n)){
					isLast = true;
				}
				else if(parent.getLocalName().equals("li")){
					if(!((Element)parent.getChild(i)).getLocalName().equals("list") && !((Element)parent.getChild(i)).getLocalName().equals("p"))
						isLast = false;
				}
				else if(!((i == parent.getChildCount() - 1) && ((Element)parent.getChild(i)).getLocalName().equals("br"))) {
					isLast = false;
				}
			}
		}
		
		if(isLast == true && (parent.getAttributeValue("semantics").contains("action"))){
			return isLast(parent);
		}
		else
			return isLast;
	}
	
	public String getString(int start, int length){
		return view.getTextRange(start, length);
	}

	public void copy(){
		view.copy();
	}
	
	public void selectAll(){
		if(textChanged){
			sendUpdate();
		}
		view.selectAll();
		setSelection(0, view.getCharCount());
	}
	
	public void cut(){
		if(validCut())
			view.cut();
	}
	
	public void paste(){
		if(validPaste())
			view.paste();
	}
	
	public void copyAndPaste(String text, int start, int end){
		view.setCaretOffset(start);
		setCurrent(view.getCaretOffset());
		view.setSelection(start, end);
		setSelection(start, end - start);
	
		Clipboard cb = new Clipboard(view.getDisplay());
        TextTransfer textTransfer = TextTransfer.getInstance();
		cb.setContents(new Object[]{text}, new Transfer[]{textTransfer});
		view.paste();
		cb.dispose();

		sendUpdate();
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
		if(range != null)
			setFontStyleRange(currentStart, currentEnd - currentStart, range);
	}
	
	private void setSelection(int start, int length){
		selectionStart = start;
		selectionLength = length;
	}
	
	public void highlight(int start, int end){
		view.setSelection(start, end);
	}
	
	//adjusts style when changed from the style panel, style adjustments occur at block element level
	public void adjustStyle(Message m, ArrayList<TextMapElement>list){		
		int start = list.get(0).start;
		int end = list.get(list.size() - 1).end;
		int length = 0;
		int spaces = 0;
		getBounds(m, list);
		String textBefore = "";
		
		//Get previous style for comparison on adding or removing lines before or after
		Styles style = (Styles)m.getValue("Style");
		Styles previousStyle = (Styles)m.getValue("previousStyle");
				
		setListenerLock(true);
		//Reset indent, alignment, and emphasis
		view.setLineIndent(view.getLineAtOffset(start), getLineNumber(start, view.getTextRange(start, (end - start))), 0);
		view.setLineAlignment(view.getLineAtOffset(start), getLineNumber(start, view.getTextRange(start, (end - start))), SWT.LEFT);
		setFontStyleRange(start, end - start, new StyleRange());
		
		if(!style.contains(StylesType.linesBefore) && previousStyle.contains(StylesType.linesBefore))
			removeLinesBefore(m);
			
		if(!style.contains(StylesType.linesAfter) && previousStyle.contains(StylesType.linesAfter))
			removeLinesAfter(m);
		
		start = (Integer)m.getValue("start");
		end = (Integer)m.getValue("end");
		int prev = (Integer)m.getValue("prev");
		int next = (Integer)m.getValue("next");
		view.setSelection(selectionStart);
		
		for (Entry<StylesType, Object> entry : style.getEntrySet()) {
			switch(entry.getKey()){
				case linesBefore:
					int linesBeforeOffset;
					if(start != prev){
						view.replaceTextRange(prev, (start - prev), "");
						length = start - prev;	
					}
					spaces = Integer.valueOf((String)entry.getValue());
						
					textBefore = makeInsertionString(spaces,'\n');
					linesBeforeOffset = spaces - length;
				
					insertBefore(start - (start - prev), textBefore);
					m.put("linesBeforeOffset", linesBeforeOffset);
					start += linesBeforeOffset;
					end += linesBeforeOffset;
					if(next != -1)
						next += linesBeforeOffset;
					break;
				case linesAfter:
					length = 0;
					int linesAfterOffset;
					if(end != next && next != 0){
						view.replaceTextRange(end, (next - end), "");
						length = next - end;	
					}
						
					spaces = Integer.valueOf((String)entry.getValue());
					textBefore = makeInsertionString(spaces,'\n');
					insertBefore(end, textBefore);
					linesAfterOffset = spaces - length;
					m.put("linesAfterOffset", linesAfterOffset);
					break;
				case format:
					setAlignment(start, end, style);	
					break;
				case firstLineIndent:
					if(Integer.valueOf((String)entry.getValue()) > 0 || style.contains(StylesType.leftMargin))
						setFirstLineIndent(start, style);
					break;
				case leftMargin:
					if(style.contains(StylesType.firstLineIndent))
						handleLineWrap(start, view.getTextRange(start, (end - start)), Integer.valueOf((String)entry.getValue()), true);
					else
						handleLineWrap(start, view.getTextRange(start, (end - start)), Integer.valueOf((String)entry.getValue()), false);
					break;
				default:
					break;
			}
				
			int offset = (Integer)m.getValue("offset");
			
			//inline elements may have different emphasis, so all must be check seperately 
			for(int i = 0; i < list.size(); i++){
				Styles nodeStyle = stylesTable.makeStylesElement(list.get(i).parentElement(), list.get(i).n);
				if(nodeStyle.contains(StylesType.emphasis))
					setFontStyleRange(list.get(i).start + offset, (list.get(i).end + offset) - (list.get(i).start + offset), (StyleRange)nodeStyle.get(StylesType.emphasis));
			}
		}
		setListenerLock(false);
	}
	
	public void getBounds(Message m, ArrayList<TextMapElement>list){
		m.put("start", list.get(0).start);
		m.put("end", list.get(list.size() - 1).end);
		m.put("prev", getPrev(m));
		m.put("next", getNext(m));
		m.put("offset", 0);
	}
	
	//private helper method used by adjust style
	//finds the point from which to remove any excess spacing before applying lines before
	private int getPrev(Message m){
		int prev = (Integer)m.getValue("prev");
		if(-1 != prev)
			prev++;				
		else 
			prev = 0;
		
		return prev;
	}
	
	//private helper method used by adjust style
	//finds the point from which to remove any excess spacing before applying lines after
	private int getNext(Message m){
		int next = (Integer)m.getValue("next");
		if(next != -1)
			next--;
		else 
			next = view.getCharCount();
		
		return next;
	}
	
	//helper method used by adjustStyles to remove lines before
	private void removeLinesBefore(Message m){
		int start = (Integer)m.getValue("start");
		int end = (Integer)m.getValue("end");
		int prev = (Integer)m.getValue("prev");
		int next = (Integer)m.getValue("next");
		int offset = (Integer)m.getValue("offset");
		int length = 0;
		
		if(start != prev){
			view.replaceTextRange(prev, (start - prev), "");
			length = start - prev;	
			offset -= length;
		}

		start += offset;
		end += offset;
		if(next != -1)
			next += offset;
		
		m.put("linesBeforeOffset", offset);
		m.put("offset", offset);
		m.put("start", start);
		m.put("end", end);
		m.put("prev", prev);
		if(next != -1)
			m.put("next", next);
	}
	
	//helper method used by adjustStyles to remove lines after
	private void removeLinesAfter(Message m){
		int end = (Integer)m.getValue("end");
		int next = (Integer)m.getValue("next");
		int offset = (Integer)m.getValue("offset");
		
		if(end != next){
			int removedSpaces;
			removedSpaces = next - end;
			offset = -removedSpaces;
			view.replaceTextRange(end, removedSpaces, "");
		}
		
		if(next != -1 && end != next){
			m.put("linesAfterOffset", offset);
			next += offset;
			m.put("next", next + offset);
		}
	}
	
	public void insertNewNode(int pos,String elementName){
		Message m = Message.createInsertNodeMessage(false, false, true,elementName);
		
		if(pos > currentEnd){
			view.setCaretOffset(pos);
			setCurrent(view.getCaretOffset());
		}
				
		insertNewNode(m, pos);
	}
			
	//Calls manager to insert a node or element in the DOM and updates the view
	//Used when inserting new paragraphs or transcriber notes
	private void insertNewNode(Message m, Integer pos){
		m.put("length", originalEnd - originalStart);
		manager.dispatch(m);
		setListenerLock(true);
				
		if(pos != null)
			view.setCaretOffset(pos);
		
		view.insert("\n");
		view.setCaretOffset(view.getCaretOffset() + 1);	
				
		if(view.getCharCount() != view.getCaretOffset()){
			StyleRange range = view.getStyleRangeAtOffset(view.getCaretOffset());
			if(range != null)
				resetStyleRange(range);
		}
				
		setListenerLock(false);
		setCurrent(view.getCaretOffset());
	}	
	
	@Override
	public void addPageNumber(PageMapElement p, boolean insert){
		String text = p.n.getValue();
		
		spaceBeforeText++;
		
		if(insert){
			view.insert("\n" + text);
			view.setCaretOffset(spaceBeforeText + text.length() + total);
		}
		else
			view.append("\n" + text);
		
		p.setOffsets(spaceBeforeText + total, spaceBeforeText + total + text.length());
		total += spaceBeforeText + text.length();
		spaceBeforeText = 0;
	}
	
	@Override
	public void resetView(SashForm sashform) {
		setListenerLock(true);
		menu.dispose();
		recreateView(sashform);
		total = 0;
		spaceBeforeText = 0;
		spaceAfterText = 0;
		oldCursorPosition = -1;
		currentChanges = 0;
		textChanged = false;
		setListenerLock(false);
	}
	
	public void resetOffsets(){
		Message m = Message.createGetCurrentMessage(Sender.TEXT, view.getCaretOffset());
		manager.dispatch(m);
		setViewData(m);
	}

	public void setBRLOnlyText(BrlOnlyMapElement b,boolean insert) {
	
		String textSidebar=b.getText();
		spaceBeforeText++;
		if(insert){
		    view.insert("\n"+textSidebar);
		    view.setCaretOffset(spaceBeforeText+textSidebar.length()+total);
		}
		else{
			view.append("\n"+textSidebar);
		}
		b.setOffsets(spaceBeforeText+total, spaceBeforeText+total+textSidebar.length());
		total += spaceBeforeText+textSidebar.length();
		spaceBeforeText = 0;
        spaceBeforeText = 0;
	}
	
	public int[] getSelectedText()
	{
		int [] temp=new int[2];
		if(selectionArray!=null){
		   temp[0]=selectionArray[0];
		   temp[1]=selectionArray[0]+selectionArray[1];
		}
		return temp;
	}

	public boolean isMultiSelected() {
		return multiSelected;
	}
	
	private boolean validCut(){
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if(selectionStart == currentStart && selectionLength == (currentEnd - currentStart))
				return false;
			else if(selectionStart >= currentStart && selectionStart < currentEnd && selectionLength <= (currentEnd - selectionStart))
				return false;
			else if(selectionStart  == currentEnd && selectionLength == 1 && selectionStart + selectionLength == nextStart)
				return false;
		}
		return true;
	}
	
	private boolean validPaste(){
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if(selectionStart == currentStart && selectionLength == (currentEnd - currentStart))
				return false;
			else if(selectionStart >= currentStart && selectionStart < currentEnd && selectionLength <= (currentEnd - selectionStart))
				return false;
			else if(view.getSelectionRanges()[1] == 0 || (selectionStart >= currentStart && selectionStart <= currentEnd ))
				return false;
		}
		return true;
	}
	
	private boolean validDelete(){
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if((selectionStart >= currentStart && selectionStart + selectionLength <= currentEnd) || selectionLength == 0)
				return false;
			else if(selectionLength <= 0 && view.getCaretOffset() == currentEnd)
				return false;
			else if(selectionStart == currentEnd && (selectionStart + selectionLength) == nextStart && selectionLength == 1)
				return false;
		}
		else if(selectionLength <= 0 && manager.inPrintPageRange(view.getCaretOffset() + 1) || (selectionLength <= 0 && (manager.getElementInRange(view.getCaretOffset() + 1) instanceof BrlOnlyMapElement)))
			return false;
		
		return true;
	}
	
	private boolean validBackspace(){
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if(selectionStart >= currentStart && selectionStart + selectionLength <= currentEnd)
				return false;
			else if(selectionLength <= 0)
				return false;
			else if(selectionStart == currentEnd && (selectionStart + selectionLength) == nextStart && selectionLength == 1)
				return false;
		}
		else if(selectionLength <= 0 && manager.inPrintPageRange(view.getCaretOffset() - 1) || (selectionLength <= 0 && (manager.getElementInRange(view.getCaretOffset() - 1) instanceof BrlOnlyMapElement)))
			return false;
		
		return true;
	}
	
	private boolean validEdit(){
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if(selectionLength <= 0)
				return false;
			else if(selectionStart == currentStart && selectionLength == (currentEnd - currentStart))
				return false;
			else if(selectionStart >= currentStart && selectionStart < currentEnd && selectionLength <= (currentEnd - selectionStart))
				return false;
			else if(selectionStart >= currentStart && selectionStart <= currentEnd)
				return false;
		}
		
		return true;
	}
	
	public void setCurrentSelection(int start, int end){
		view.setSelection(start, end);
		setSelection();
	}
	
	private void setSelection(){
		selectionArray = view.getSelectionRanges();
		
		if(selectionArray[1] > 0){
			setSelection(selectionArray[0], selectionArray[1]);
			multiSelected=true;
			currentChar = ' ';
			if(currentChanges > 0)
				sendUpdate();
		}
		else{
			multiSelected = false;
		}
	}
}