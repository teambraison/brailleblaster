package org.brailleblaster.perspectives.braille.views.wp;

import nu.xom.Element;
import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.abstractClasses.BBView;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Group;

public abstract class WPView extends AbstractView implements BBView {
	protected BBSemanticsTable stylesTable;
	protected static int currentAlignment;
	public static int currentLine;
	protected static int topIndex, scrollBarPos;
	public StyledText view;
	protected int charWidth;
	protected ModifyListener viewMod;
	
	public WPView(Manager manager, Group group, int left, int right, int top, int bottom, BBSemanticsTable table){
		super(manager, group);
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
		this.stylesTable = table;
		view = new StyledText(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		setLayout(view, left, right, top, bottom);
		// Better use a ModifyListener to set the change flag.
		viewMod = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				hasChanged = true;
			}
		};
		scrollBarPos = 0;
	}
	
	public abstract void addPageNumber(PageMapElement p, boolean insert);
	@Override
	protected abstract void setViewData(Message message);
	
	protected int getFontWidth(){
		GC gc = new GC(view);
		FontMetrics fm =gc.getFontMetrics();
		gc.dispose();
		return fm.getAverageCharWidth();
	}
	
	protected int getFontHeight(){
		GC gc = new GC(view);
		FontMetrics fm =gc.getFontMetrics();
		gc.dispose();
		return fm.getHeight();
	}
	
	public void setcharWidth(){
		charWidth = getFontWidth();
	}
	
	public void positionScrollbar(int topIndex){
		setListenerLock(true);
		group.setRedraw(false);
		view.setTopIndex(topIndex);
		group.setRedraw(true);
		group.getDisplay().update();
		setListenerLock(false);
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
	
	public void insertText(int start, String text){
		setListenerLock(true);
		int originalPosition = view.getCaretOffset();
		view.setCaretOffset(start);
		view.insert(text);
		view.setCaretOffset(originalPosition);
		setListenerLock(false);
	}
	
	protected void setImageStyleRange(Image image, int offset, int length) {
		StyleRange style = new StyleRange ();
		style.start = offset;
		style.length = length;
		style.data = image;
		Rectangle rect = image.getBounds();
		style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
		view.setStyleRange(style);		
	}

	//sets range and applies given form of emphasis
	protected void setFontStyleRange(int start, int length, StyleRange styleRange){
		styleRange.start = start;
		styleRange.length = length;	
		view.setStyleRange(styleRange);
	}
	
	//reverts range to plain text
	protected void resetStyleRange(StyleRange range){
		range.fontStyle = SWT.NORMAL;
		range.underline = false;
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
	
	//saves alignment, used when updating text since alignment info is removed with previous text is deleted and new translation is inserted
	protected void saveStyleState(int start){
		int line = this.view.getLineAtOffset(start);
		currentAlignment = view.getLineAlignment(line);
	}
	
	//restores previous alignment when updating text
	protected void restoreStyleState(int start, int end){
		int line = view.getLineAtOffset(start);
		if(end > view.getCharCount())
			end = view.getCharCount();
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
	
	protected void setAlignment(int start, int end, int style){
		int startLine = view.getLineAtOffset(start);
		view.setLineAlignment(startLine, getLineNumber(start, view.getTextRange(start, (end - start))),  style);	
	}
	
	public void clearTextRange(int start, int length){
		setListenerLock(true);
		view.replaceTextRange(start, length, "");
		setListenerLock(false);
	}
	
	protected void sendStatusBarUpdate(int line){
		String statusBarText = "";
		String page = manager.getCurrentPrintPage();
		if(page != null){
		
			statusBarText += "Page: " + page + " | ";
		}
		statusBarText += "Line: " + String.valueOf(line + 1) + " | ";
		
		if(view.getLineIndent(line) > 0){
			statusBarText += " Indent: " + ((view.getLineIndent(line) / charWidth)) + " | "; 
		}
		
		if(view.getLineAlignment(line) != SWT.LEFT){
			if(view.getLineAlignment(line) == SWT.CENTER)
				statusBarText += " Alignment: Center" + " | ";
			else if(view.getLineAlignment(line) == SWT.RIGHT)
				statusBarText += " Alignment: Right" + " | ";
		}
		
		if(manager.getCurrent() != null){
			Element e = manager.getCurrent().parentElement();
			while(stylesTable.getSemanticTypeFromAttribute(e).equals("action"))
				e = (Element)e.getParent();
			
			String style = this.stylesTable.getKeyFromAttribute(e);
			statusBarText += "Style: " + style + " | ";
		}
		
		Message statusMessage = Message.createUPdateStatusbarMessage(statusBarText + " Words: " + words);
		manager.dispatch(statusMessage);
		currentLine = view.getLineAtOffset(view.getCaretOffset());
	}
	
	public void setTopIndex(int line){
		setListenerLock(true);
		view.setTopIndex(line);
		topIndex = line;
		setListenerLock(false);
	}
	
	public void checkStatusBar(Sender sender){
		if(!getLock()){
			if(topIndex != view.getTopIndex()){
				topIndex = view.getTopIndex();
				Message scrollMessage = Message.createUpdateScollbarMessage(sender, view.getOffsetAtLine(topIndex));
				manager.dispatch(scrollMessage);
			}
		}
	}
	
	protected void recreateView(Group group, int left, int right, int top, int bottom){
		view.dispose();
		view = new StyledText(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		view.addModifyListener(viewMod);
		setLayout(view, left, right, top, bottom);
		view.getParent().layout();
	}
	
	public int getTotal(){
		return total;
	}
}
