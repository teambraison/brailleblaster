package org.brailleblaster.perspectives.braille.views.wp;

import java.util.ArrayList;

import nu.xom.Element;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.abstractClasses.BBView;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public abstract class WPView extends AbstractView implements BBView {
	protected BBSemanticsTable stylesTable;
	protected static int currentAlignment;
	public static int currentLine;
	protected static int topIndex, scrollBarPos;
	public StyledText view;
	protected int charWidth;
	protected ModifyListener viewMod;
	
	public WPView(Manager manager, SashForm sash, BBSemanticsTable table){
		super(manager, sash);
		this.total = 0;
		this.spaceBeforeText = 0;
		this.spaceAfterText = 0;
		this.stylesTable = table;
		view = new StyledText(sash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
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
	public abstract void getBounds(Message m, ArrayList<TextMapElement>list);
	
	@Override
	protected abstract void setViewData(Message message);
	
	
	/** finds Char width used to determine indent value
	 * @return  width of braille cell
	 */
	protected int getFontWidth(){
		GC gc = new GC(view);
		FontMetrics fm =gc.getFontMetrics();
		gc.dispose();
		return fm.getAverageCharWidth();
	}
	
	/**Determines line height, used when setting MathML images in views
	 * @return value of font height, equivalent to line height
	 */
	protected int getFontHeight(){
		GC gc = new GC(view);
		FontMetrics fm =gc.getFontMetrics();
		gc.dispose();
		return fm.getHeight();
	}
	
	/** Public method for calculating char width, used when font changes, or font size changes
	 */
	public void setcharWidth(){
		charWidth = getFontWidth();
	}
	
	/** Positions the scrollbar based off the top-most visible line
	 * @param topIndex : top-most visible line in view
	 */
	public void positionScrollbar(int topIndex){
		setListenerLock(true);
		sash.setRedraw(false);
		view.setTopIndex(topIndex);
		sash.setRedraw(true);
		sash.getDisplay().update();
		setListenerLock(false);
	}
	
	/** Inserts text after a specified offset, used in setting lines after
	 * @param position : offset in view at which point to begin insertion
	 * @param text : text to insert
	 */
	protected void insertAfter(int position, String text){
		int previousPosition = view.getCaretOffset();
		view.setCaretOffset(position);
		view.insert(text);
		this.spaceAfterText += text.length();
		view.setCaretOffset(previousPosition);
	}
	
	/** Inserts text before a specified offset, used in setting lines before
	 * @param position : offset in view at which point to begin insertion
	 * @param text : text to insert
	 */
	protected void insertBefore(int position, String text){
		int previousPosition = view.getCaretOffset();
		view.setCaretOffset(position);
		view.insert(text);
		this.spaceBeforeText += text.length();
		view.setCaretOffset(previousPosition);
	}
	
	/** Used to create a string of repeating characters, for example, multiple blank lines
	 * @param length : length of string
	 * @param c : repeating char
	 * @return : String of repeated char
	 */
	protected String makeInsertionString(int length, char c){
		String insertionString = "";
		for(int i = 0; i < length; i++){
			insertionString += c;
		}
		
		return insertionString;
	}
	
	/** Disables event listeners, inserts text into view at the oint specified
	 * @param start : start offset in view
	 * @param text : text to insert
	 */
	public void insertText(int start, String text){
		setListenerLock(true);
		int originalPosition = view.getCaretOffset();
		view.setCaretOffset(start);
		view.insert(text);
		view.setCaretOffset(originalPosition);
		setListenerLock(false);
	}
	
	public void replaceTextRange(int start, int length, String text){
		setListenerLock(true);
		int originalPosition = view.getCaretOffset();
		view.setCaretOffset(start);
		view.replaceTextRange(start, length, text);
		view.setCaretOffset(originalPosition);
		setListenerLock(false);
	}
	
	protected void setImageStyleRange(Image image, int offset, int length) {
		StyleRange style = new StyleRange ();
		style.start = offset;
		style.length = length;
		style.data =image;
		Rectangle rect = image.getBounds();
		style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
		view.setStyleRange(style);		
	}

	/** sets range and applies given form of emphasis
	 * @param start : position at which to insert style range
	 * @param length : length style range covers
	 * @param styleRange : SWT StyleRange object
	 */
	protected void setFontStyleRange(int start, int length, StyleRange styleRange){
		styleRange.start = start;
		styleRange.length = length;	
		view.setStyleRange(styleRange);
	}

	/**	reverts range to plain text
	 * @param range : Style Range to reset
	 */
	protected void resetStyleRange(StyleRange range){
		range.fontStyle = SWT.NORMAL;
		range.underline = false;
		view.setStyleRange(range);
	}	
	
	/** Gets index attribute from brl element and converts to integer array
	 * @param e : Brl element to retrieve indexes
	 * @return : Integer Array containing brl indexes
	 */
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
	
	/** saves alignment, used when updating text since alignment info is removed with previous text is deleted and new translation is inserted
	 * @param start : offset used to find line number
	 */
	protected void saveAlignment(int start){
		int line = this.view.getLineAtOffset(start);
		currentAlignment = view.getLineAlignment(line);
	}
	
	/**Restores previous alignment when updating text 
	 * @param start : offset used to find beginning line
	 * @param end : offset used to determine last line to apply alignment
	 */
	protected void restoreAlignment(int start, int end){
		int line = view.getLineAtOffset(start);
		if(end > view.getCharCount())
			end = view.getCharCount();
		int lineCount = view.getLineAtOffset(end) - view.getLineAtOffset(start);
		view.setLineAlignment(line, lineCount + 1, currentAlignment);
	}
	
	/** Used to set LibLouisUTDML LeftMargin style attribute, in SWT API uses a line wrap function to achieve equivalent
	 * @param pos : starting offset
	 * @param text : text inserted in view
	 * @param indent : amount of left margin specified in number of braille cells
	 * @param skipFirstLine : true if first line should not have style applied
	 */
	protected void handleLineWrap(int pos, String text, int indent, boolean skipFirstLine){
		int newPos;
		int i = 0;
		
		while(i < text.length() && text.charAt(i) == '\n')
			i++;
		
		if(!skipFirstLine)
			view.setLineIndent(view.getLineAtOffset(pos + i), 1, indent * charWidth);
	
		for(; i < text.length(); i++){
			if(text.charAt(i) == '\n' && i != text.length() - 1){
				i++;
				newPos = pos + i;
				view.setLineIndent(view.getLineAtOffset(newPos), 1, indent * charWidth);
			}
		}
	}

	/** Counts total lines a string covers within a view
	 * @param startOffset : starting position of text
	 * @param text : text placed in view
	 * @return lines count covered by text
	 */
	protected int getLineNumber(int startOffset, String text){
		int startLine = view.getLineAtOffset(startOffset);
		int endLine = view.getLineAtOffset(startOffset + text.length());	
		return (endLine - startLine) + 1;
	}
	
	/** Pauses event listeners, moves cursor to a new position
	 * @param pos : offset at which to place cursor
	 */
	public void resetCursor(int pos){
		setListenerLock(true);
		view.setFocus();
		view.setCaretOffset(pos);
		setListenerLock(false);
	}
	
	/** Set lines before a block element specified by liblouisutdml config file
	 * @param start : start position
	 * @param style : Java object representing liblouisutdml style
	 */
	protected void setLinesBefore(int start, Styles style){
		String textBefore = makeInsertionString(Integer.valueOf((String)style.get(StylesType.linesBefore)),'\n');
		insertBefore(start, textBefore);
	}
	
	/** Set lines after a block element specified by liblouisutdml style
	 * @param start : start position
	 * @param style : Java object representing liblouisutdml style
	 */
	protected void setLinesAfter(int start, Styles style){
		String textAfter = makeInsertionString(Integer.valueOf((String)style.get(StylesType.linesAfter)), '\n');
		insertAfter(start, textAfter);
	}
	
	/** Sets the first line indent specified by liblouisutdml style
	* @param start : start position
	 * @param style : Java object representing liblouisutdml style
	 */
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
	
	/** Sets alignment using a Styles object
	 * @param start : start offset
	 * @param end : end offset
	 * @param style : style object
	 */
	protected void setAlignment(int start, int end, Styles style){
		int startLine = view.getLineAtOffset(start);
		view.setLineAlignment(startLine, getLineNumber(start, view.getTextRange(start, (end - start))),  Integer.valueOf((String)style.get(StylesType.format)));	
	}
	
	/** Sets alignment using the swt constant value for alignment
	 * @param start : start offset
	 * @param end : end offset
	 * @param style : swt constant for left, center, right
	 */
	protected void setAlignment(int start, int end, int style){
		int startLine = view.getLineAtOffset(start);
		view.setLineAlignment(startLine, getLineNumber(start, view.getTextRange(start, (end - start))),  style);	
	}
	
	/** Pauses event listeners and clears a range of text
	 * @param start : starting offset
	 * @param length : length of text
	 */
	public void clearTextRange(int start, int length){
		setListenerLock(true);
		view.replaceTextRange(start, length, "");
		setListenerLock(false);
	}
	
	/** Creates message and text to display status in the status bar.
	 *  The message typically covers current style, line number and word count
	 * @param line
	 */
	protected void sendStatusBarUpdate(int line){
		String statusBarText = "";
		String page = manager.getCurrentPrintPage();
		if(page != null)
			statusBarText += "Page: " + page + " | ";
		
		//Every 25th line start counting line again
		statusBarText += "Line: " + String.valueOf(line % manager.getDocument().getLinesPerPage() + 1) + " | ";
		//Added this line for cursor position
		statusBarText += "Cell Number: " + String.valueOf(view.getCaretOffset()-view.getOffsetAtLine(line) + ((view.getLineIndent(line) / charWidth) + 1)) + " | ";
		
		if(view.getLineIndent(line) > 0)
			statusBarText += " Indent: Cell " + ((view.getLineIndent(line) / charWidth) + 1) + " | ";
		
		if(view.getLineAlignment(line) != SWT.LEFT){
			if(view.getLineAlignment(line) == SWT.CENTER)
				statusBarText += " Alignment: Center" + " | ";
			else if(view.getLineAlignment(line) == SWT.RIGHT)
				statusBarText += " Alignment: Right" + " | ";
		}
		
		if(manager.getCurrent() != null){
			String styleName;
			if(manager.getCurrent() instanceof PageMapElement){
				styleName = "Print Page";
			}
			else {
				Element e = manager.getCurrent().parentElement();
				while(stylesTable.getSemanticTypeFromAttribute(e).equals("action"))
					e = (Element)e.getParent();
			
				styleName = stylesTable.getKeyFromAttribute(e);
				Styles style = stylesTable.get(styleName);
			
				if(styleName.contains("local_")){
					String [] tokens = styleName.split("_");
					styleName = tokens[1];
					style = stylesTable.get(styleName);
					styleName = (String)style.get(StylesType.name);
				}
				else if(style.contains(StylesType.name))
					styleName = (String)style.get(StylesType.name);
				else
					styleName = style.getName();
			}
			statusBarText += "Style: " + styleName + " | ";
		}
		
		Message statusMessage = Message.createUpdateStatusbarMessage(statusBarText + " Words: " + words);
		manager.dispatch(statusMessage);
		currentLine = view.getLineAtOffset(view.getCaretOffset());
	}
	
	/** Public method for moving the scrollbar by reseting the top index.  Event listeners
	 * are paused and index is changed.
	 * @param line
	 */
	public void setTopIndex(int line){
		setListenerLock(true);
		view.setTopIndex(line);
		topIndex = line;
		setListenerLock(false);
	}
	
	/** Checks if status bar should be updated
	 * @param sender : Enumeration signifiy which view, text, braille or tree, is sending the message
	 */
	public void checkStatusBar(Sender sender){
		if(!getLock()){
			if(topIndex != view.getTopIndex()){
				topIndex = view.getTopIndex();
				Message scrollMessage = Message.createUpdateScollbarMessage(sender, view.getOffsetAtLine(topIndex));
				manager.dispatch(scrollMessage);
			}
		}
	}
	
	/** Redraws the views when a refresh occurs
	 * @param group : group in which to place styledtext widget
	 * @param left : left position for layout
	 * @param right : right position for layout
	 * @param top : top position for layout
	 * @param bottom : bottom position for layout
	 */
	protected void recreateView(SashForm sashform){
		view.dispose();
		view = new StyledText(sashform, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		view.addModifyListener(viewMod);
		view.getParent().layout();
	}
	
	/** Returns total chars in view during initialization
	 * Used when initializing views
	 * @return
	 */
	public int getTotal(){
		return total;
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
