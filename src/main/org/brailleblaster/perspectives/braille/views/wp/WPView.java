package org.brailleblaster.perspectives.braille.views.wp;

import nu.xom.Element;
import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.abstractClasses.BBView;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Group;

public abstract class WPView extends AbstractView implements BBView {
	protected BBSemanticsTable stylesTable;
	
	public WPView(Group group, int left, int right, int top, int bottom, BBSemanticsTable table){
		super(group, left, right, top, bottom);
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
		this.stylesTable = table;
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

	//sets range and applies given form of emphasis
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
	
	//lengthens or shortens style range
	protected void updateRange(StyleRange style, int start, int length){
		style.start = start;
		style.length = length;
		view.setStyleRange(style);
	}
	
	//reverts range to plain text
	protected void resetStyleRange(StyleRange range){
		if(range.fontStyle != SWT.NORMAL || (range.fontStyle == SWT.NORMAL && range.underline == true)){
			range.fontStyle = SWT.NORMAL;
			range.underline = false;
		}
		view.setStyleRange(range);
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
	
	public void resetCursor(int pos){
		setListenerLock(true);
		view.setFocus();
		view.setCaretOffset(pos);
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
}
