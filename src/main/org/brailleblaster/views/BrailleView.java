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

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.abstractClasses.AbstractContent;
import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.mapping.BrailleMapElement;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.wordprocessor.BBEvent;
import org.brailleblaster.wordprocessor.BBSemanticsTable;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.Message;
import org.brailleblaster.wordprocessor.BBSemanticsTable.Styles;
import org.brailleblaster.wordprocessor.BBSemanticsTable.StylesType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Group;

public class BrailleView extends AbstractView {
	private int currentStart, currentEnd, nextStart, previousEnd;
	private BBSemanticsTable stylesTable;
	private int oldCursorPosition = -1;
	
	public BrailleView(Group documentWindow, BBSemanticsTable table) {
		super(documentWindow, 58, 100, 0, 100);
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
		this.stylesTable = table;
	}
	
	public void initializeListeners(final DocumentManager dm){
		view.addVerifyKeyListener(new VerifyKeyListener(){
			@Override
			public void verifyKey(VerifyEvent e) {
				oldCursorPosition = view.getCaretOffset();
			}
			
		});
		
		view.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				Message message = new Message(BBEvent.GET_CURRENT);
				message.put("sender", "braille");
				dm.dispatch(message);
				//currentStart = (Integer)message.getValue("brailleStart");
				//currentEnd = (Integer)message.getValue("brailleEnd");
				setViewData(message);
				if(oldCursorPosition == -1 || oldCursorPosition < currentStart || oldCursorPosition > currentEnd){
					view.setCaretOffset((Integer)message.getValue("brailleStart"));
					oldCursorPosition = view.getCaretOffset();
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub	
			}
		});
		
		view.addMouseListener(new MouseListener(){
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
		
		view.addCaretListener(new CaretListener(){
			@Override
			public void caretMoved(CaretEvent e) {
				if(view.getCaretOffset() > currentEnd || view.getCaretOffset() < currentStart){
					setCurrent(dm);
				}
			}
		});
		
		view.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.stateMask == SWT.CONTROL && e.keyCode == SWT.ARROW_DOWN && nextStart != -1){
					sendIncrementCurrent(dm);
					view.setCaretOffset(currentStart);
					e.doit = false;
				}
				else if(e.stateMask == SWT.CONTROL && e.keyCode == SWT.ARROW_UP && previousEnd != -1){
					sendDecrementCurrent(dm);
					view.setCaretOffset(currentStart);
					e.doit = false;
				}
			}
		});
	}
	
	private void setCurrent(DocumentManager dm){
		Message message = new Message(BBEvent.SET_CURRENT);
		message.put("sender", "braille");
		message.put("isBraille", true);
		message.put("offset", view.getCaretOffset());
		dm.dispatch(message);
		setViewData(message);
	}
	
	private void setViewData(Message message){
		currentStart = (Integer)message.getValue("brailleStart");
		currentEnd = (Integer)message.getValue("brailleEnd");
		nextStart = (Integer)message.getValue("nextBrailleStart");
		previousEnd = (Integer)message.getValue("previousBrailleEnd");
	}
	
	private void sendIncrementCurrent(DocumentManager dm){
		Message message = new Message(BBEvent.INCREMENT);
		dm.dispatch(message);
		setViewData(message);
	}
	
	private void sendDecrementCurrent(DocumentManager dm){
		Message message = new Message(BBEvent.DECREMENT);
		dm.dispatch(message);
		setViewData(message);
	}
	
	public void setBraille(Node n, TextMapElement t){
		String key = this.stylesTable.getKeyFromAttribute((Element)t.n.getParent()); 
		Styles style = this.stylesTable.makeStylesElement(key, n);
		
		if(insertNewLine(n)){
			view.append("\n");
			this.total++;
		}
		
		view.append(n.getValue());
		handleStyle(style, n, (Element)t.n.getParent());
		
		t.brailleList.add(new BrailleMapElement(this.spaceBeforeText + this.total, this.spaceBeforeText + this.total + n.getValue().length(), n));
		this.total += this.spaceBeforeText + n.getValue().length() + this.spaceAfterText;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
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
	
	private void handleStyle(Styles style, Node n, Element parent){
		String viewText = n.getValue();
		for (StylesType styleType : style.getKeySet()) {
			switch(styleType){
				case linesBefore:
	//				String textBefore = makeInsertionString(Integer.valueOf((String)style.get(styleType)),'\n');
	//				insertBefore(this.spaceBeforeText + this.total, textBefore);
					break;
				case linesAfter:
					String textAfter = makeInsertionString(Integer.valueOf((String)style.get(styleType)),'\n');
					insertAfter(this.spaceBeforeText + this.total + viewText.length() + this.spaceAfterText, textAfter);
					break;
				case firstLineIndent: 
					if(isFirst(n)){
						int spaces = Integer.valueOf((String)style.get(styleType));
						this.view.setLineIndent(this.view.getLineAtOffset(this.spaceBeforeText + this.total + this.spaceAfterText) , 1, spaces * getFontWidth());
					}
					break;
				case format:
					this.view.setLineAlignment(this.view.getLineAtOffset( this.spaceBeforeText + this.total + this.spaceAfterText), 1, Integer.valueOf((String)style.get(styleType)));	
					break;	
				case Font:
					 setFontRange(this.total, n.getValue().length() + this.spaceAfterText, SWT.ITALIC);
					 break;
				default:
					//System.out.println(styleType);
			}
		}
	}
	
	private boolean isFirst(Node n){
		if(((Element)n.getParent().getParent()).getLocalName().equals("span")){
			return false;
		}
		
		int i = 0;
		Element parent = (Element)n.getParent();
		
		while(!(parent.getChild(i) instanceof Text)){
			i++;
		}
		
		if(parent.indexOf(n) == i){
			Element grandParent = (Element)parent.getParent();
			if(grandParent.getLocalName().equals("em") || grandParent.getLocalName().equals("strong")){
				return isFirstElement(grandParent);
			}
			else {
				i = 0;
				while((grandParent.getChild(i) instanceof Text)){
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
	
	public void updateBraille(TextMapElement t, Message message){
		int total = (Integer)message.getValue("brailleLength");
		int startLine = this.view.getLineAtOffset(t.brailleList.getFirst().start);
		int lineIndent = this.view.getLineIndent(startLine);
		StyleRange range = view.getStyleRangeAtOffset(t.brailleList.getFirst().start);

		String insertionString = (String)message.getValue("newBrailleText");
		
		if(t.brailleList.getFirst().start != -1){
			view.replaceTextRange(t.brailleList.getFirst().start, total, insertionString);
			view.setLineIndent(startLine, 1, lineIndent);
			if(range != null)
				updateRange(range, t.brailleList.getFirst().start, insertionString.length());
		}
	}
	
	public void removeWhitespace(int start, int length){
		view.replaceTextRange(start, Math.abs(length), "");
	}
	
	public void changeAlignment(int startPosition, int alignment){
		view.setLineAlignment(view.getLineAtOffset(startPosition), 1, alignment);
	}
	
	public void changeIndent(int start, Message message){
		view.setLineIndent(view.getLineAtOffset(start), 1, (Integer)message.getValue("indent"));
	}
	/*
	 * This is a derivative work from org.eclipse.swt.custom.DefaultContent.java
	 */
	class BrailleContent extends AbstractContent {
	}
}
