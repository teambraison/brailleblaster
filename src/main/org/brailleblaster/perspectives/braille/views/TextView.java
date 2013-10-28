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

package org.brailleblaster.perspectives.braille.views;

import java.util.Map.Entry;

import nu.xom.Element;
import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.mathml.ImageCreator;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.mapping.MapList;
import org.brailleblaster.perspectives.braille.mapping.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;


public class TextView extends AbstractView {
	private final static int LEFT_MARGIN = 16;
	private final static int RIGHT_MARGIN = 57;
	private final static int TOP_MARGIN = 0;
	private final static int BOTTOM_MARGIN = 100;
	
	private int oldCursorPosition = -1;
	private int currentChar;
	private int currentStart, currentEnd, previousEnd, nextStart, selectionStart, selectionLength;
	private int currentChanges = 0;
	private boolean textChanged;
	private BBSemanticsTable stylesTable;
	private StyleRange range;
	private int[] selectionArray;
	private SelectionListener selectionListener, scrollbarListener;
	private VerifyKeyListener verifyKeyListener;
	private VerifyListener verifyListener;
	private ExtendedModifyListener modListener;
	private FocusListener focusListener;
	private CaretListener caretListener;
	private MouseListener mouseListener;
	private PaintObjectListener paintObjListener;
	private int originalStart, originalEnd;
	private TextMapElement currentElement;
	
 	public TextView (Group documentWindow, BBSemanticsTable table) {
		super (documentWindow, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		this.stylesTable = table;
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
	}

	public void initializeListeners(final Manager dm){	
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
				}
			}			
		});
		
		view.addVerifyKeyListener(verifyKeyListener = new VerifyKeyListener(){
			@Override
			public void verifyKey(VerifyEvent e) {
				oldCursorPosition = view.getCaretOffset();
				currentChar = e.keyCode;
				
				if(e.stateMask == SWT.CONTROL && e.keyCode == 'a'){
					selectAll(dm);
				}
				else if(e.character == SWT.CR){
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
						sendUpdate(dm);
						setCurrent(dm);
					}
					
					if(atEnd) {
						Message m = Message.createInsertNodeMessage(false, false, true);
						m.put("length", originalEnd - originalStart);
						dm.dispatch(m);
						setListenerLock(true);
						view.setCaretOffset(currentEnd);
						view.insert("\n");
						view.setCaretOffset(view.getCaretOffset() + 1);
						setListenerLock(false);
						e.doit = false;
						setCurrent(dm);
					}
					else if(atStart){
						Message m = Message.createInsertNodeMessage(false, true, false);
						m.put("length", originalEnd - originalStart);
						dm.dispatch(m);
						setListenerLock(true);
						view.insert("\n");
						view.setCaretOffset(view.getCaretOffset() + 1);
						setListenerLock(false);
						e.doit = false;
						setCurrent(dm);
					}
					else {
						Message m;
						int origLength =  getString(currentStart, view.getCaretOffset() - currentStart).length();
						if(view.getCaretOffset() == currentEnd){
							m = Message.createInsertNodeMessage(true, false, true);
						}
						else if(view.getCaretOffset() == currentStart){
							m = Message.createInsertNodeMessage(true, true, false);
						}
						else
							m = Message.createInsertNodeMessage(true, false, false);
							
						m.put("originalLength", origLength);
						m.put("length", originalEnd - originalStart);
						m.put("position", getString(currentStart, view.getCaretOffset() - currentStart).replace("\n", "").length());
						int pos = view.getCaretOffset();
						dm.dispatch(m);
						setListenerLock(true);
						view.setCaretOffset(pos + (Integer)m.getValue("length"));
						e.doit = false;
						setCurrent(dm);
						view.setCaretOffset(currentStart);
						setListenerLock(false);
					}
				}
				
				if(oldCursorPosition == currentStart && oldCursorPosition != previousEnd && e.character == SWT.BS && view.getLineAlignment(view.getLineAtOffset(currentStart)) != SWT.LEFT ){					
					Message message;
					
					if(view.getLineAlignment(view.getLineAtOffset(currentStart)) == SWT.RIGHT){
						view.setLineAlignment(view.getLineAtOffset(currentStart), 1, SWT.CENTER);
						message = Message.createAdjustAlignmentMessage("text",SWT.CENTER);
					}
					else {
						view.setLineAlignment(view.getLineAtOffset(currentStart), 1, SWT.LEFT);
						message = Message.createAdjustAlignmentMessage("text",SWT.LEFT);
					}
					dm.dispatch(message);
					e.doit = false;
					setSelection(-1, -1);
				}
				
				if(oldCursorPosition == currentStart && oldCursorPosition != previousEnd && e.character == SWT.BS && view.getLineIndent(view.getLineAtOffset(currentStart)) != 0 && currentStart != currentEnd){
					Message message = Message.createAdjustIndentMessage("text", 0, view.getLineAtOffset(currentStart));
				
					view.setLineIndent(view.getLineAtOffset(currentStart), 1, 0);
					dm.dispatch(message);
					e.doit = false;
				}
				/*
				else if(oldCursorPosition > 0 && oldCursorPosition < view.getCharCount() && e.character == SWT.BS){
					if(view.getLineAtOffset(oldCursorPosition) != view.getLineAtOffset(oldCursorPosition - 1) && view.getLineIndent(view.getLineAtOffset(oldCursorPosition)) != 0){
						Message message = new Message(BBEvent.ADJUST_INDENT);
						message.put("sender", "text");
						message.put("indent", 0);
						message.put("line", view.getLineAtOffset(oldCursorPosition));
						view.setLineIndent(view.getLineAtOffset(oldCursorPosition), 1, 0);
						dm.dispatch(message);
						e.doit = false;
					}
				}
				*/
				if(selectionLength > 0){
					saveStyleState(selectionStart);
				}
				else
					saveStyleState(currentStart);
				
				if(currentElement.isMathML() && (e.keyCode != SWT.BS && e.keyCode != SWT.DEL && e.keyCode != SWT.ARROW_DOWN && e.keyCode != SWT.ARROW_LEFT && e.keyCode != SWT.ARROW_RIGHT && e.keyCode != SWT.ARROW_UP))
					e.doit = false;
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
				Message message = Message.createGetCurrentMessage("text", view.getCaretOffset());
				dm.dispatch(message);
				setViewData(message);
				if(oldCursorPosition == -1 && positionFromStart == 0){
					view.setCaretOffset((Integer)message.getValue("start"));
				}
				
				sendStatusBarUpdate(dm, view.getLineAtOffset(view.getCaretOffset()));
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(textChanged == true)
					sendUpdate(dm);	
				
				setPositionFromStart();
				Message message = Message.createUpdateCursorsMessage("text");
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
					
							setCurrent(dm);
						}
					}
				}
				
				if(view.getLineAtOffset(view.getCaretOffset()) != currentLine){
					sendStatusBarUpdate(dm,  view.getLineAtOffset(view.getCaretOffset()));
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

		view.getVerticalBar().addSelectionListener(scrollbarListener = new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void widgetSelected(SelectionEvent e) {		
				checkStatusBar("text", dm);
			}
		});
		
		// use a verify listener to dispose the images	
		view.addVerifyListener(verifyListener = new VerifyListener()  {
			public void verifyText(VerifyEvent event) {
				if(event.doit != false && event.start != event.end && !getLock()){
					StyleRange style = view.getStyleRangeAtOffset(event.start);
					if (style != null) {
						Image image = (Image)style.data;
						if (image != null && !image.isDisposed()) {
							saveStyleState(currentStart);
							image.dispose();
							currentChanges--;
							event.doit = false;
							sendRemoveMathML(dm, event.start);
							if(view.getCaretOffset() != 0 && event.keyCode == SWT.BS)
								view.setCaretOffset(view.getCaretOffset() - 1);
							
							if(!(nextStart == -1 && previousEnd == -1))
								setCurrent(dm);
						}
					}
				}
			}
		});	
		
		// draw images on paint event
		view.addPaintObjectListener(paintObjListener = new PaintObjectListener() {
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
				checkStatusBar("text", dm);
			}
		});
		
		setListenerLock(false);
	}
	
	public void removeListeners(){
		if(selectionListener != null) {
			view.removeSelectionListener(selectionListener);
			view.removeExtendedModifyListener(modListener);
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
	public void update(Manager dm){
		if(textChanged){
			sendUpdate(dm);
		}
	}
	
	private void sendUpdate(Manager dm){
			Message updateMessage = Message.createUpdateMessage(view.getCaretOffset(), getString(currentStart, currentEnd - currentStart), originalEnd - originalStart);
			dm.dispatch(updateMessage);
			words += (Integer)updateMessage.getValue("diff");
			sendStatusBarUpdate(dm, view.getLineAtOffset(view.getCaretOffset()));
			currentChanges = 0;
			textChanged = false;
			restoreStyleState(currentStart, currentEnd);
	}
	
	private void setCurrent(Manager dm){
		Message message = Message.createSetCurrentMessage("text", view.getCaretOffset(), false);
		dm.dispatch(message);
		setViewData(message);
	}
	
	private void sendDeleteSpaceMessage(Manager dm, int offset, int key){
		Message message = Message.createTextDeletionMessage(offset, key, false);
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
	
	private void sendAdjustRangeMessage(Manager dm, String type, int position){
		Message adjustmentMessage = Message.createAdjustRange(type, position);
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
	
	private void sendRemoveMathML(Manager manager, int offset){
		Message removeMessage = Message.createRemoveMathMLMessage(offset, -(currentEnd - currentStart));
		removeMessage.put("offset", currentChanges);
		manager.dispatch(removeMessage);
		currentChanges = 0;
		textChanged = false;
		if(currentEnd <= view.getCharCount())
			restoreStyleState(currentStart, currentEnd);
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
		
		currentElement = (TextMapElement)message.getValue("currentElement");
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
		nextStart += offset;
	}
	
	public void setCursor(int offset){
		view.setFocus();
		view.setCaretOffset(offset);
	}

	public void setText(Node n, MapList list){
		Styles style = stylesTable.makeStylesElement((Element)n.getParent(), n);
		Styles prevStyle;
		if(list.size() > 0)
			prevStyle = stylesTable.makeStylesElement(list.getLast().parentElement(),list.getLast().n);
		else
			prevStyle = null;
		
		String newText = appendToView(n, true);
		int textLength = newText.length();

		view.append(newText);
		handleStyle(prevStyle, style, n, newText);
		
		list.add(new TextMapElement(spaceBeforeText + total, spaceBeforeText + total + textLength,n));
		total += spaceBeforeText + textLength + spaceAfterText;
		
		spaceAfterText = 0;
		spaceBeforeText = 0;
		view.setCaretOffset(0);
		words += getWordCount(n.getValue());
	}
	
	public void setMathML(MapList list, Element math){
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
		setImageRange(image, total, length);
		handleStyle(prevStyle, style, math, " ");
		/*
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
		*/
		StyleRange s = view.getStyleRangeAtOffset(total);
		s.length = length;
		view.setStyleRange(s);
		
		list.add(new TextMapElement(spaceBeforeText + total, spaceBeforeText + (total + length) + spaceAfterText, math));
		total += spaceBeforeText + length + spaceAfterText;
		spaceAfterText = 0;
		spaceBeforeText = 0;	
	}	
	
	private void setImageRange(Image image, int offset, int length) {
		StyleRange style = new StyleRange ();
		style.start = offset;
		style.length = length;
		style.data = image;
		Rectangle rect = image.getBounds();
		style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
		view.setStyleRange(style);		
	}
	
	public void reformatText(Node n, Message message, Manager dm){
		String reformattedText;
		Styles style = stylesTable.makeStylesElement((Element)n.getParent(), n);
		int margin = 0;
		int pos = view.getCaretOffset();
		setListenerLock(true);
		StyleRange range = getStyleRange();
		
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
		
		if(isFirst(n) && style.contains(StylesType.firstLineIndent))
			setFirstLineIndent(currentStart, style);
		
		if(style.contains(StylesType.Font))
			setFontRange(currentStart, reformattedText.length(), Integer.valueOf((String)style.get(StylesType.Font)));
		
		checkStyleRange(range);
	
		view.setCaretOffset(pos);		
		spaceAfterText = 0;
		spaceBeforeText = 0;
		setListenerLock(false);
	}
	
	public void removeMathML(Message m){
		setListenerLock(true);
		int pos = view.getCaretOffset();
		
		if(view.getCharCount() > 0)
			view.replaceTextRange((Integer)m.getValue("start"), Math.abs((Integer)m.getValue("length")), "");
		
		view.setCaretOffset(pos);		
		setListenerLock(false);
	}
	
	public void insertText(MapList list, int listIndex, int start, Node n){
		Styles style = stylesTable.makeStylesElement((Element)n.getParent(), n);
		String reformattedText =  appendToView(n, false);
		setListenerLock(true);
		int originalPosition = view.getCaretOffset();
		view.setCaretOffset(start);
		view.insert(reformattedText);
		list.add(listIndex, new TextMapElement(start, start + reformattedText.length(), n));
		
		int margin = 0;
		
		//reset margin in case it is not applied
		if(start == view.getOffsetAtLine(view.getLineAtOffset(start)))
			handleLineWrap(start, reformattedText, 0, false);
				
		if(style.contains(StylesType.leftMargin)) {
			margin = Integer.valueOf((String)style.get(StylesType.leftMargin));
			handleLineWrap(start, reformattedText, margin, style.contains(StylesType.firstLineIndent));
		}
					
		if(isFirst(n) && style.contains(StylesType.firstLineIndent))
			setFirstLineIndent(start, style);
		
		if(style.contains(StylesType.format))
			setAlignment(start, start + n.getValue().length(), style);
		
		if(style.contains(StylesType.Font))
			setFontRange(start, reformattedText.length(), Integer.valueOf((String)style.get(StylesType.Font)));

		view.setCaretOffset(originalPosition);
		setListenerLock(false);
	}
		
	private String appendToView(Node n, boolean append){
		String text = "";
		Element brl = getBrlNode(n);
		int start = 0;
		int end = 0;
		int totalLength = 0;
		
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
									text += n.getValue().substring(start);
									spaceBeforeText++;
								}
								else if(start == 0){
									text += n.getValue().substring(start);
								}
								else {
									text += "\n" + n.getValue().substring(start);
								}
							}
							else{
								    end += indexes[totalLength] - end;
									if(start == 0 && append){
										view.append("\n");
										text += n.getValue().substring(start, end);
										spaceBeforeText++;
									}
									else if(start == 0){
										text += n.getValue().substring(start, end);
									}
									else{
										text += "\n" + n.getValue().substring(start, end);
									}
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
			else if(append){
					view.append("\n");
					text += n.getValue();
					spaceBeforeText++;
			}
			else {
				text += n.getValue();
			}
		}
		else {
			text += n.getValue();
		}
		
		return text;
	}
	
	private void handleStyle(Styles prevStyle, Styles style, Node n, String viewText){
		boolean isFirst = isFirst(n);
		
		for (Entry<StylesType, String> entry : style.getEntrySet()) {
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
					if(isFirst && (Integer.valueOf(entry.getValue()) > 0 || style.contains(StylesType.leftMargin)))
						setFirstLineIndent(spaceBeforeText + total, style);
					break;
				case format:
					setAlignment(spaceBeforeText + total, spaceBeforeText + total + viewText.length(), style);
					break;	
				case Font:
					setFontRange(total, spaceBeforeText + viewText.length(), Integer.valueOf(entry.getValue()));
					break;
				case leftMargin:
					if(isFirst)
						handleLineWrap(spaceBeforeText + total, viewText, Integer.valueOf(entry.getValue()), style.contains(StylesType.firstLineIndent));
					else if(!isFirst && view.getLineAtOffset(spaceBeforeText + total) == view.getLineAtOffset(total))
						handleLineWrap(spaceBeforeText + total, viewText, Integer.valueOf(entry.getValue()), style.contains(StylesType.firstLineIndent));
					else
						handleLineWrap(spaceBeforeText + total, viewText, Integer.valueOf(entry.getValue()), false);
					break;
				default:
					System.out.println(entry.getKey());
			}
		}
	}
	
	private void handleTextEdit(Manager dm, ExtendedModifyEvent e){
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
					if(range != null)
						updateRange(range, currentStart, e.length);
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
					setCurrent(dm);
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
				else if(e.start + replacedTextLength == currentEnd){
					changes -= replacedTextLength;
					makeTextChange(changes);
					sendUpdate(dm);
					setCurrent(dm);
					//Test more
				}
				else {				
					if(selectionLength != e.length)
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
	
	private void handleTextDeletion(Manager dm, ExtendedModifyEvent e){
		int offset = view.getCaretOffset() - oldCursorPosition;
		setListenerLock(true);
		if(e.replacedText.length() > 1){
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

	private void deleteSelection(Manager dm){
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
					selectionStart = currentEnd;
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
			if(parent.getAttributeValue("semantics").contains("action")){
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
	
	public void selectAll(Manager dm){
		if(textChanged == true){
			sendUpdate(dm);
		}
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
	
	public void adjustStyle(Manager dm, Message m, Node n){		
		int startLine = (Integer)m.getValue("firstLine");
		int length = 0;
		int spaces = 0;
		int offset = 0;
		int indent = 0;
		int prev = 0;
		int next = 0;
		boolean isFirst = isFirst(n);
		boolean isLast = isLast(n);
		String textBefore = "";
		setViewData(m);
		Styles style = (Styles)m.getValue("Style");
		Styles previousStyle = (Styles)m.getValue("previousStyle");
		
		setListenerLock(true);	
		
		if(isFirst || (!isFirst && view.getLineAtOffset(currentStart) != startLine))
			view.setLineIndent(view.getLineAtOffset(currentStart), getLineNumber(currentStart, view.getTextRange(currentStart, (currentEnd - currentStart))), 0);
		view.setLineAlignment(view.getLineAtOffset(currentStart), getLineNumber(currentStart, view.getTextRange(currentStart, (currentEnd - currentStart))), SWT.LEFT);
			
		for (Entry<StylesType, String> entry : style.getEntrySet()) {
			switch(entry.getKey()){
				case linesBefore:
					if(isFirst){
						saveStyleState(currentStart);
						if(-1 != previousEnd){
							prev = previousEnd;
						}
						indent  = view.getLineIndent(view.getLineAtOffset(currentStart));
						if(currentStart != prev){
							view.replaceTextRange(prev, (currentStart - prev), "");
							length = currentStart - prev;	
						}
						spaces = Integer.valueOf(entry.getValue());
						if(previousEnd == -1){
							textBefore = makeInsertionString(spaces,'\n');
							offset = spaces - length;
						}
						else {	
							textBefore = makeInsertionString(spaces + 1,'\n');
							offset = (spaces + 1) - length;
						}
						insertBefore(currentStart - (currentStart - prev), textBefore);
						m.put("linesBeforeOffset", offset);
						currentStart += offset;
						currentEnd += offset;
						if(nextStart != -1)
							nextStart += offset;
						view.setLineIndent(view.getLineAtOffset(currentStart), 1, indent);
						restoreStyleState(currentStart, currentEnd);
					}
					break;
				case linesAfter:
					if(isLast){
						if(-1 != nextStart){
							next = nextStart;
							saveStyleState(currentStart);
							indent  = view.getLineIndent(view.getLineAtOffset(next));
						}
						else {
							next = currentEnd;
						}
					
						if(currentEnd != next && next != 0){
							view.replaceTextRange(currentEnd, (next - currentEnd), "");
							length = next - currentEnd;	
						}
						spaces = Integer.valueOf(entry.getValue());
						textBefore = makeInsertionString(spaces + 1,'\n');
						insertBefore(currentEnd, textBefore);
						offset = (spaces + 1) - length;
						m.put("linesAfterOffset", offset);
						if(nextStart != -1)
							nextStart += offset;
						if(nextStart != -1){
							view.setLineIndent(view.getLineAtOffset(nextStart), 1, indent);
							restoreStyleState(currentStart, currentEnd);
						}
					}
					break;
				case format:
					setAlignment(currentStart, currentEnd, style);	
					break;
				case firstLineIndent:
					if(isFirst && (Integer.valueOf(entry.getValue()) > 0 || style.contains(StylesType.leftMargin)))
						setFirstLineIndent(currentStart, style);
					break;
				case leftMargin:
					if(isFirst && style.contains(StylesType.firstLineIndent))
						handleLineWrap(currentStart, view.getTextRange(currentStart, (currentEnd - currentStart)), Integer.valueOf(entry.getValue()), true);
					else if(startLine == view.getLineAtOffset(currentStart))
						handleLineWrap(currentStart, view.getTextRange(currentStart, (currentEnd - currentStart)), Integer.valueOf(entry.getValue()), true);
					else
						handleLineWrap(currentStart, view.getTextRange(currentStart, (currentEnd - currentStart)), Integer.valueOf(entry.getValue()), false);
					break;
				default:
					break;
			}
		}

		if(!style.contains(StylesType.linesBefore) && previousStyle.contains(StylesType.linesBefore) && isFirst){
			if(-1 != previousEnd){
				prev = previousEnd;
			}
			
			if(currentStart != prev){
				saveStyleState(currentStart);
				indent  = view.getLineIndent(view.getLineAtOffset(currentStart));
				view.replaceTextRange(prev, (currentStart - prev), "");
				length = currentStart - prev;	
			}
			
			if(isFirst){
				spaces = 1;
				textBefore = makeInsertionString(spaces,'\n');
				offset = spaces - length;
		
				insertBefore(currentStart - (currentStart - prev), textBefore);
				m.put("linesBeforeOffset", offset);
				m.put("firstLine", startLine + offset);
				currentStart += offset;
				currentEnd += offset;
				if(nextStart != -1)
					nextStart += offset;
				if(previousEnd != -1){
					restoreStyleState(currentStart, currentEnd);
					view.setLineIndent(view.getLineAtOffset(currentStart), 1, indent);
				}
			}
		}
		
		if(!style.contains(StylesType.linesAfter) && previousStyle.contains(StylesType.linesAfter) && isLast){
			if(currentEnd != nextStart){
				int removedSpaces;
				if(nextStart != -1)
					removedSpaces = nextStart - currentEnd;
				else
					removedSpaces = view.getCharCount() - currentEnd;
				
				saveStyleState(currentStart);
				if(nextStart != -1)
					indent  = view.getLineIndent(view.getLineAtOffset(nextStart)); 
					
				view.replaceTextRange(currentEnd, removedSpaces, "");
				length = removedSpaces;
			}
			
			if(isLast && nextStart != -1){
				spaces = 1;
				textBefore = makeInsertionString(1,'\n');
				insertBefore(currentEnd, textBefore);
				offset = spaces - length;
				m.put("linesAfterOffset", offset);
				if(nextStart != -1)
					nextStart += offset;
				if(nextStart != -1) {
					restoreStyleState(currentStart, currentEnd);
					view.setLineIndent(view.getLineAtOffset(nextStart), 1, indent);
				}
			}
		}
		setListenerLock(false);
	}
	
	public void resetView(Group group) {
		setListenerLock(true);
		recreateView(group, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		total = 0;
		spaceBeforeText = 0;
		spaceAfterText = 0;
		oldCursorPosition = -1;
		currentChanges = 0;
		textChanged = false;
		setListenerLock(false);
	}
}