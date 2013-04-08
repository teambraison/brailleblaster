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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Group;

public class BrailleView extends AbstractView {
	private int currentStart, currentEnd;
	private BBSemanticsTable stylesTable;
	
	public BrailleView(Group documentWindow, BBSemanticsTable table) {
		super(documentWindow, 58, 100, 0, 100);
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
		this.stylesTable = table;
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
		String key = this.stylesTable.getKeyFromAttribute(t.n); 
		
		view.append(n.getValue());
		
		if(this.stylesTable.containsKey(key)){
			Styles temp = this.stylesTable.get(key);
			handleStyle(temp, n, (Element)t.n.getParent());
		}
		
		t.brailleList.add(new BrailleMapElement(this.spaceBeforeText + this.total, this.spaceBeforeText + this.total + n.getValue().length(), n));
		this.total += this.spaceBeforeText + n.getValue().length() + this.spaceAfterText;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
	}
	
	private void handleStyle(Styles style, Node n, Element parent){
		String viewText = n.getValue();
		for (StylesType styleType : style.getKeySet()) {
			switch(styleType){
				case linesBefore:
					String textBefore = makeInsertionString(Integer.valueOf((String)style.get(styleType)),'\n');
					insertBefore(this.spaceBeforeText + this.total, textBefore);
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
					 setFontRange(n.getValue().length() + this.spaceAfterText, SWT.ITALIC);
					 break;
				default:
					System.out.println(styleType);
			}
		}
		
		if(parent.getLocalName().equals("em") || parent.getLocalName().equals("strong")){
			Element grandParent = (Element)parent.getParent();
			while(grandParent.getLocalName().equals("em") || grandParent.getLocalName().equals("strong")){
				grandParent = (Element)grandParent.getParent();
			}
			int textNodes = 0;
			for(int i = 0; i < grandParent.getChildCount(); i++){
				if(grandParent.getChild(i) instanceof Element && !((Element)grandParent.getChild(i)).getLocalName().equals("brl")){
					textNodes++;
				}
			}
			
			if(textNodes - 1 == grandParent.indexOf(parent) && isLast(n)){
				insertAfter(this.spaceBeforeText + this.total + n.getValue().length() + this.spaceAfterText, "\n");
			}
		}
		else if(isLast(n)){
			insertAfter(this.spaceBeforeText + this.total + n.getValue().length() + this.spaceAfterText, "\n");
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
			return true;
		}
		else {
			return false;
		}
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
		
		if(isLast){
			Element grandParent = (Element)parent.getParent();
			for(int i = 0; i < grandParent.getChildCount(); i++){
				if(grandParent.getChild(i) instanceof Element){
					if(grandParent.getChild(i).equals(parent)){
						isLast = true;
					}
					else{
						isLast = false;
					}
				}
			}
		}
		
		return isLast;
	}
	
	public void updateBraille(TextMapElement t, int total){
		String insertionString = "";
		for(int i = 0; i < t.brailleList.size(); i++){
			insertionString += t.brailleList.get(i).n.getValue();
		}	
		if(t.brailleList.getFirst().start != -1){
			view.replaceTextRange(t.brailleList.getFirst().start, total, insertionString);
		}
	}
	
	public void removeWhitespace(int start, int length){
		System.out.println("Start pos\t" + (start + length) + " length\t" + length);
		view.replaceTextRange(start + length, Math.abs(length), "");
	}
	
	public void changeAlignment(int startPosition, int alignment){
		view.setLineAlignment(view.getLineAtOffset(startPosition), 1, alignment);
	}
	
	/*
	 * This is a derivative work from org.eclipse.swt.custom.DefaultContent.java
	 */
	class BrailleContent extends AbstractContent {
	}
}
