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

import java.util.ArrayList;
import java.util.Map.Entry;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.document.BBSemanticsTable;
import org.brailleblaster.document.BBSemanticsTable.Styles;
import org.brailleblaster.document.BBSemanticsTable.StylesType;
import org.brailleblaster.mapping.BrailleMapElement;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.messages.Message;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
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
import org.eclipse.swt.widgets.Group;


public class BrailleView extends AbstractView {
	private final static int LEFT_MARGIN = 58;
	private final static int RIGHT_MARGIN = 100;
	private final static int TOP_MARGIN = 0;
	private final static int BOTTOM_MARGIN = 100;
	
	private int currentStart, currentEnd, nextStart, previousEnd;
	private BBSemanticsTable stylesTable;
	private int oldCursorPosition = -1;
	private ArrayList<BrailleMapElement> pageRanges = new ArrayList<BrailleMapElement>();
	private String charAtOffset;
	
	private VerifyKeyListener verifyListener;
	private FocusListener focusListener;
	private MouseListener mouseListener;
	private CaretListener caretListener;
	private SelectionListener selectionListener;
	
	public BrailleView(Group documentWindow, BBSemanticsTable table) {
		super(documentWindow, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
		this.stylesTable = table;
	}
	
	public void initializeListeners(final Manager dm){
		view.addVerifyKeyListener(verifyListener = new VerifyKeyListener(){
			@Override
			public void verifyKey(VerifyEvent e) {
				oldCursorPosition = view.getCaretOffset();
			}
			
		});
		
		view.addFocusListener(focusListener = new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				Message message = Message.createGetCurrentMessage("braille", view.getCaretOffset());
				dm.dispatch(message);
				setViewData(message);
				if(oldCursorPosition == -1 && positionFromStart  == 0){
					view.setCaretOffset((Integer)message.getValue("brailleStart"));
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				setPositionFromStart();
				Message message = Message.createUpdateCursorsMessage("braille");
				dm.dispatch(message);
			}
		});
		
		view.addMouseListener(mouseListener = new MouseListener(){
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if(view.getCaretOffset() > currentEnd || view.getCaretOffset() < currentStart){
					setCurrent(dm);
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub			
			}		
		});
		
		view.addCaretListener(caretListener = new CaretListener(){
			@Override
			public void caretMoved(CaretEvent e) {
				if(!getLock()){
					if(view.getCaretOffset() > currentEnd || view.getCaretOffset() < currentStart){
						if(view.getCaretOffset() > currentEnd && view.getCaretOffset() < nextStart)
							charAtOffset = view.getText(view.getCaretOffset(), view.getCaretOffset());
						setCurrent(dm);
					}
				
				
					if(view.getLineAtOffset(view.getCaretOffset()) != currentLine){
						 sendStatusBarUpdate(dm, view.getLineAtOffset(view.getCaretOffset()));
					}
				}
			}
		});
		
		view.getVerticalBar().addSelectionListener(selectionListener = new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub		
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkStatusBar("braille", dm);
			}
		});
		
		view.addPaintListener(new PaintListener(){
			@Override
			public void paintControl(PaintEvent e) {
				checkStatusBar("braille", dm);
			}			
		});
	
		setListenerLock(false);
	}
	
	public void removeListeners(){
		view.removeVerifyKeyListener(verifyListener);
		view.removeFocusListener(focusListener);
		view.removeMouseListener(mouseListener);
		view.removeCaretListener(caretListener);
		view.getVerticalBar().removeSelectionListener(selectionListener);
	}
	
	private void setCurrent(Manager dm){
		Message message = Message.createSetCurrentMessage("braille", view.getCaretOffset(), true);
		
		if(charAtOffset != null)
			message.put("char", charAtOffset);
		
		dm.dispatch(message);
		setViewData(message);
		charAtOffset = null;
	}
	
	@SuppressWarnings("unchecked")
	protected void setViewData(Message message){
		currentStart = (Integer)message.getValue("brailleStart");
		currentEnd = (Integer)message.getValue("brailleEnd");
		nextStart = (Integer)message.getValue("nextBrailleStart");
		previousEnd = (Integer)message.getValue("previousBrailleEnd");
		this.pageRanges.clear();
		setPageRange((ArrayList<BrailleMapElement>)message.getValue("pageRanges"));
	}
	
	private void setPageRange(ArrayList<BrailleMapElement> list){
		this.pageRanges.clear();
		for(int i = 0; i < list.size(); i++){
			this.pageRanges.add(list.get(i));
		}
	}
	
	public void setBraille(Node n, TextMapElement t){
		setListenerLock(true);
		Styles style = stylesTable.makeStylesElement(t.parentElement(), n);
		String textBefore = "";
		String text = n.getValue();
		int textLength = text.length();
	
		if(insertNewLine(n)){
			textBefore = "\n";
			spaceBeforeText++;
		}
		
		view.append(textBefore + text);
		handleStyle(style, n, t.parentElement());
		
		t.brailleList.add(new BrailleMapElement(spaceBeforeText + total, spaceBeforeText + total + textLength, n));
		total += spaceBeforeText + textLength + spaceAfterText;
		spaceBeforeText = 0;
		spaceAfterText = 0;
		setListenerLock(false);
	}
	
	private boolean insertNewLine(Node n){
		Element parent = (Element)n.getParent();
		int index = parent.indexOf(n);
		if(index > 0){
			if(((Element)parent.getChild(index - 1)).getLocalName().equals("newline")){
				return true;
			}
		}
		
		return false;
	}
	
	/*
	private void checkFinalNewline(Node n){
		Element parent = (Element)n.getParent();
		int childCount = parent.getChildCount();
		
		if(parent.indexOf(n) == childCount - 2){
			if(parent.getChild(childCount - 1) instanceof Element && ((Element)parent.getChild(childCount - 1)).getLocalName().equals("newline")){
				view.append("\n");
				this.spaceAfterText++;
			}
		}
	}
	*/
	
	private void handleStyle(Styles style, Node n, Element parent){
		boolean isFirst = isFirst(n);
		String viewText = n.getValue();

		for (Entry<StylesType, String> entry : style.getEntrySet()) {
			switch(entry.getKey()){
				case linesBefore:
					if(isFirst)
						setLinesBefore(total + spaceBeforeText, style);
					break;
				case linesAfter:
					if(isLast(n))
						setLinesAfter(spaceBeforeText + total + viewText.length() + spaceAfterText, style);
					break;
				case firstLineIndent: 
					if(isFirst && (Integer.valueOf(entry.getValue()) > 0 || style.contains(StylesType.leftMargin)))
						setFirstLineIndent(spaceBeforeText + total, style);
					break;
				case format:
					setAlignment(spaceBeforeText + total, spaceBeforeText + total + n.getValue().length(), style);
					break;	
				case Font:
			//		 setFontRange(this.total, this.spaceBeforeText + n.getValue().length(), Integer.valueOf(entry.getValue()));
					 break;
				case leftMargin:
					if(followsNewLine(n)){
						if(isFirst && !style.contains(StylesType.firstLineIndent))
							view.setLineIndent(view.getLineAtOffset(spaceBeforeText + total), 1, (Integer.valueOf(entry.getValue()) * charWidth));
						else if(!isFirst)
							view.setLineIndent(view.getLineAtOffset(spaceBeforeText + total), 1, (Integer.valueOf(entry.getValue()) * charWidth));
					}
					break;
				default:
					System.out.println(entry.getKey());
			}
		}
	}
	
	public void adjustStyle(Manager dm, Message m, TextMapElement t){
		int startLine = (Integer)m.getValue("firstLine");
		int length = 0;
		int spaces = 0;
		int offset = 0;
		int indent = 0;
		int prev = 0;
		int next = 0;
		String textBefore = "";
		setViewData(m);
		Styles style = (Styles)m.getValue("Style");
		Styles previousStyle = (Styles)m.getValue("previousStyle");
		
		setListenerLock(true);
		if(isFirst(t.brailleList.getFirst().n) || (!isFirst(t.brailleList.getFirst().n) && view.getLineAtOffset(currentStart) != startLine))
			view.setLineIndent(view.getLineAtOffset(currentStart), getLineNumber(currentStart, view.getTextRange(currentStart, (currentEnd - currentStart))), 0);
		view.setLineAlignment(view.getLineAtOffset(currentStart), getLineNumber(currentStart, view.getTextRange(currentStart, (currentEnd - currentStart))), SWT.LEFT);
		
		for (Entry<StylesType, String> entry : style.getEntrySet()) {
			switch(entry.getKey()){
				case linesBefore:
					if(isFirst(t.brailleList.getFirst().n)){
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
						currentStart += offset;
						currentEnd += offset;
						if(nextStart != -1)
							nextStart += offset;
						
						view.setLineIndent(view.getLineAtOffset(currentStart), 1, indent);
						restoreStyleState(currentStart, currentEnd);
					}
					break;
				case linesAfter:
					if(isLast(t.brailleList.getLast().n)){
						if(-1 != nextStart){
							next = nextStart;
							indent  = view.getLineIndent(view.getLineAtOffset(next));
							saveStyleState(currentStart);
						}
						else {
							nextStart = currentEnd;
						}
				
						if(currentEnd != next && next != 0){
							view.replaceTextRange(currentEnd, (next - currentEnd), "");
							length = next - currentEnd;	
						}
						spaces = Integer.valueOf(entry.getValue());
						textBefore = makeInsertionString(spaces + 1,'\n');
						insertBefore(currentEnd, textBefore);
						offset = (spaces + 1) - length;
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
					if(isFirst(t.brailleList.getFirst().n) && (Integer.valueOf(entry.getValue()) > 0 || style.contains(StylesType.leftMargin)))
						setFirstLineIndent(currentStart, style);
					break;
				case leftMargin:
					if(isFirst(t.brailleList.getFirst().n) && style.contains(StylesType.firstLineIndent))
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
		
		if(!style.contains(StylesType.linesBefore)  && previousStyle.contains(StylesType.linesBefore)){
			if(-1 != previousEnd){
				prev = previousEnd;
			}
			
			if(currentStart != prev){
				saveStyleState(currentStart);
				indent  = view.getLineIndent(view.getLineAtOffset(currentStart));
				view.replaceTextRange(prev, (currentStart - prev), "");
				length = currentStart - prev;	
			}
			
			if(isFirst(t.brailleList.getFirst().n)){
				spaces = 1;
				textBefore = makeInsertionString(spaces,'\n');
				offset = spaces - length;
		
				insertBefore(currentStart - (currentStart - prev), textBefore);
				m.put("linesBeforeOffset", offset);
				currentStart += offset;
				currentEnd += offset;
				if(nextStart != -1)
					nextStart += offset;
				if(previousEnd != -1){
					view.setLineIndent(view.getLineAtOffset(currentStart), 1, indent);
					restoreStyleState(currentStart, currentEnd);
				}
			}
		}
		
		if(!style.contains(StylesType.linesAfter) &&  previousStyle.contains(StylesType.linesAfter)){
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
			
			if(isLast(t.brailleList.getLast().n) && nextStart != -1){
				spaces = 1;
				textBefore = makeInsertionString(1,'\n');
				insertBefore(currentEnd, textBefore);
				offset = spaces - length;
				m.put("linesAfterOffset", offset);
				if(nextStart != -1)
					nextStart += offset;
				if(nextStart != -1){
					restoreStyleState(currentStart, currentEnd);
					view.setLineIndent(view.getLineAtOffset(nextStart), 1, indent);
				}
			}
		}
		setListenerLock(false);
	}
	
	
	private boolean followsNewLine(Node n){
		Element parent = (Element)n.getParent();
		int index = parent.indexOf(n);
		
		if(index > 0 && isElement(parent.getChild(index - 1))){
			if(((Element)parent.getChild(index - 1)).getLocalName().equals("newline"))
				return true;
		}
		return false;
	}
	
	private boolean isFirst(Node n){	
		int i = 0;
		Element parent = (Element)n.getParent();
		
		while(!(isText(parent.getChild(i)))){
			i++;
		}
		
		if(parent.indexOf(n) == i){
			Element grandParent = (Element)parent.getParent();
			Elements els = grandParent.getChildElements();
			
		if(!els.get(0).getLocalName().equals("brl") || !els.get(0).equals(parent))
			return false;
			
			if(grandParent.getAttributeValue("semantics").contains("action") && !grandParent.getLocalName().equals("lic")){
				return isFirstElement(grandParent);
			}
			else {
				i = 0;
				while((isText(grandParent.getChild(i)))){
					i++;
				}
				if(grandParent.indexOf(parent) == i)
					return true;
				else 
					return false;
			}
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
				else{
					isLast = false;
				}
			}
			else if(isElement(n)){
				if(parent.getChild(i).equals(n)){
					isLast = true;
				}
				else{
					isLast = false;
				}
			}
		}
		
		if(isLast){
			Element grandParent = (Element)parent.getParent();
			for(int i = 0; i < grandParent.getChildCount(); i++){
				if(isElement(grandParent.getChild(i))){
					if(grandParent.getChild(i).equals(parent)){
						isLast = true;
					}
					else if(grandParent.getLocalName().equals("li") && isElement(grandParent.getChild(i))){
						if(!((Element)grandParent.getChild(i)).getLocalName().equals("list") && !((Element)grandParent.getChild(i)).getLocalName().equals("p"))
							isLast = false;
					}
					else if(!((i == grandParent.getChildCount() - 1) && ((Element)grandParent.getChild(i)).getLocalName().equals("br"))) {
						isLast = false;
					}
				}
			}
			
			if(isLast && grandParent.getAttributeValue("semantics").contains("action"))
				isLast = isLast(parent);
		}
		
		return isLast;
	}
	
	public void updateBraille(TextMapElement t, Message message){
		Styles style = stylesTable.makeStylesElement(t.parentElement(), t.n);
		int total = (Integer)message.getValue("brailleLength");
		int margin = 0;
		
		System.out.println("Value: " + t.value());
		String insertionString = (String)message.getValue("newBrailleText");
		
		if(t.brailleList.getFirst().start != -1){
			setListenerLock(true);			
			view.replaceTextRange(t.brailleList.getFirst().start, total, insertionString);
			restoreStyleState(t.brailleList.getFirst().start, t.brailleList.getLast().end);
			
			//reset margin in case it is not applied
			if(t.brailleList.getFirst().start == view.getOffsetAtLine(view.getLineAtOffset(t.brailleList.getFirst().start)))
				handleLineWrap(t.brailleList.getFirst().start, insertionString, 0, false);
			
			if(style.contains(StylesType.leftMargin)) {
				margin = Integer.valueOf((String)style.get(StylesType.leftMargin));
				handleLineWrap(t.brailleList.getFirst().start, insertionString, margin, style.contains(StylesType.firstLineIndent));
			}
				
			if(isFirst(t.brailleList.getFirst().n) && style.contains(StylesType.firstLineIndent))
				setFirstLineIndent(t.brailleList.getFirst().start, style);
			
			setListenerLock(false);	
		}
	}
	
	public void removeWhitespace(int start, int length, char c, Manager dm){
		setListenerLock(true);
		Message message = Message.createGetCurrentMessage("braille", view.getCaretOffset());
		dm.dispatch(message);
		setViewData(message);
	
		if(c == SWT.DEL && (start != currentEnd && start != previousEnd)){
			start--;
		}
	
		view.replaceTextRange(start, Math.abs(length), "");
		setListenerLock(false);
	}
	
	public void changeAlignment(int startPosition, int alignment){
		view.setLineAlignment(view.getLineAtOffset(startPosition), 1, alignment);
	}
	
	public void changeIndent(int start, Message message){
		view.setLineIndent(view.getLineAtOffset(start), 1, (Integer)message.getValue("indent"));
	}
	
	public void updateCursorPosition(Message message){
		setListenerLock(true);
		setViewData(message);
		setCursorPosition(message);
		setPositionFromStart();
		setListenerLock(false);
	}
	
	public void setPositionFromStart(){
		int count = 0;
		positionFromStart = view.getCaretOffset() - currentStart;
		if(positionFromStart > 0 && currentStart + positionFromStart <= currentEnd){
			String text = view.getTextRange(currentStart, positionFromStart);
			count = text.length() - text.replaceAll("\n", "").length();
			positionFromStart -= count;
			positionFromStart -= checkPageRange(currentStart + positionFromStart);
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
					else if((Integer)message.getValue("lastPosition") == 99999)
						pos = currentEnd + offset;
					else {
						pos = currentStart + findCurrentPosition(arr, (Integer)message.getValue("lastPosition")) + offset;
						pos += checkPageRange(pos);
					}
				}
				view.setCaretOffset(pos);
			}
			else {
				view.setCaretOffset(currentStart);
			}
		}
	}
	
	private int checkPageRange(int position){
		int offset = 0;
		for(int i = 0; i < this.pageRanges.size(); i++){
			if(position + offset > this.pageRanges.get(i).start){
				offset += this.pageRanges.get(i).end - this.pageRanges.get(i).start;
			}
		}
			
		return offset;	
	}
	
	private int findCurrentPosition(int [] indexes, int textPos){
		for(int i = 0; i < indexes.length; i++){
			if(textPos == indexes[i]){
				return i;
			}
			else if(textPos < indexes[i]){
				return i - 1;
			}
		}
		
		return indexes.length;
	}
	
	public void setWords(int words){
		this.words = words;
	}

	public void resetView(Group group) {
		setListenerLock(true);
		recreateView(group, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		total = 0;
		spaceBeforeText = 0;
		spaceAfterText = 0;
		oldCursorPosition = -1;
		setListenerLock(false);
	}
	
	public void insert(TextMapElement t, Node n, int pos){
		Styles style = stylesTable.makeStylesElement(t.parentElement(), t.n);
		int margin = 0;
		int originalPosition = view.getCaretOffset();
		Element parent = (Element)n.getParent();
		int start = pos;
		int index = parent.indexOf(n);
		
		setListenerLock(true);
		view.setCaretOffset(pos);
		if(index > 0 && isElement(parent.getChild(index - 1)) && ((Element)parent.getChild(index - 1)).getLocalName().equals("newline") && t.brailleList.size() > 0){
			view.insert("\n");
			start++;
			view.setCaretOffset(pos + 1);
		}
		view.insert(n.getValue());
		t.brailleList.add(new BrailleMapElement(start, start + n.getValue().length(), n));
		
		//reset margin in case it is not applied
		if(t.brailleList.getLast().start == view.getOffsetAtLine(view.getLineAtOffset(t.brailleList.getLast().start)))
			handleLineWrap(t.brailleList.getLast().start, n.getValue(), 0, false);
				
		if(style.contains(StylesType.leftMargin)) {
			margin = Integer.valueOf((String)style.get(StylesType.leftMargin));
			handleLineWrap(t.brailleList.getLast().start, n.getValue(), margin, false);
		}
					
		if(isFirst(n) && style.contains(StylesType.firstLineIndent))
			setFirstLineIndent(t.brailleList.getFirst().start, style);
		
		if(style.contains(StylesType.format))
			setAlignment(start,start + n.getValue().length(),style);
		
		view.setCaretOffset(originalPosition);
		setListenerLock(false);
	}
	
	public void insertLineBreak(int insertPosition){
		setListenerLock(true);
		int pos = view.getCaretOffset();
		view.setCaretOffset(insertPosition);
		view.insert("\n");
		view.setCaretOffset(pos);
		setListenerLock(false);
	}
}
