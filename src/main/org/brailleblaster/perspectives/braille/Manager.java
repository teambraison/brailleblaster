/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
 * and
 * American Printing House for the Blind, Inc. www.aph.org www.aph.org
 *
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
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
 */

package org.brailleblaster.perspectives.braille;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintException;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.ArchiverFactory;
import org.brailleblaster.document.SemanticFileHandler;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.MapList;
import org.brailleblaster.perspectives.braille.mapping.PageMapElement;

import org.brailleblaster.perspectives.braille.mapping.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.BBEvent;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.spellcheck.SpellCheckManager;
import org.brailleblaster.perspectives.braille.stylepanel.StyleManager;
import org.brailleblaster.search.*;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.tree.BookTree;
import org.brailleblaster.perspectives.braille.views.tree.TreeView;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.brailleblaster.wordprocessor.BBProgressBar;
import org.brailleblaster.printers.PrintPreview;
import org.brailleblaster.printers.PrintersManager;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.PropertyFileManager;
import org.brailleblaster.util.YesNoChoice;
import org.brailleblaster.wordprocessor.BBFileDialog;
import org.brailleblaster.wordprocessor.BBStatusBar;
import org.brailleblaster.wordprocessor.FontManager;
import org.brailleblaster.wordprocessor.WPManager;
import org.daisy.printing.PrinterDevice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;

//This class manages each document in an MDI environment. It controls the braille View and the daisy View.
public class Manager extends Controller {
	Group group;
	BBTree treeView;
	private TextView text;
	private BrailleView braille;
	private BBProgressBar pb;
	StyleManager sm;
	FormLayout layout;
	Control [] tabList;
	BBSemanticsTable styles;
	String documentName = null;
	private boolean metaContent = false;
	String logFile = "Translate.log";
	String configSettings = null;
	static String recentFileName = null;
	LocaleHandler lh = new LocaleHandler();
	static Logger logger;
	public BrailleDocument document;
	private FontManager fontManager;
	private boolean simBrailleDisplayed;
	private MapList list;
	SearchDialog srch = null;
	
	//Constructor that sets things up for a new document.
	public Manager(WPManager wp, String docName) {
		super(wp);	
		simBrailleDisplayed = loadSimBrailleProperty();
		fontManager = new FontManager(this);
		styles = new BBSemanticsTable(BBIni.getDefaultConfigFile());
		documentName = docName;
		list = new MapList(this);
		item = new TabItem(wp.getFolder(), 0);
		group = new Group(wp.getFolder(),SWT.NONE);
		group.setLayout(new FormLayout());	
		sm = new StyleManager(this);
		treeView = loadTree();
		text = new TextView(this, group, styles);
		braille = new BrailleView(this, group, styles);
		item.setControl(this.group);
		initializeDocumentTab();
		document = new BrailleDocument(this, styles);
		pb = new BBProgressBar(wp.getShell());
		fontManager.setFontWidth(simBrailleDisplayed);
		srch = new SearchDialog(wp.getShell(), SWT.NONE, this);
		
		logger = BBIni.getLogger();
		
		if(docName != null)
			openDocument(docName);
		else {
			docCount++;
			arch = ArchiverFactory.getArchive( templateFile);
			initializeAllViews(docName, templateFile, null);
			formatTemplateDocument();
			setTabTitle(docName);
		}				
	}
	
	public Manager(WPManager wp, Document doc, TabItem item, Archiver arch){
		super(wp);	
		this.arch = arch;
		simBrailleDisplayed = loadSimBrailleProperty();
		fontManager = new FontManager(this);
		styles = new BBSemanticsTable(arch.getCurrentConfig());
		documentName = arch.getOrigDocPath();
		list = new MapList(this);
		this.item = item;
		group = new Group(wp.getFolder(),SWT.NONE);
		group.setLayout(new FormLayout());	
		sm = new StyleManager(this);
		treeView = loadTree();
		text = new TextView(this, group, styles);
		braille = new BrailleView(this, group, styles);
		this.item.setControl(group);
		initializeDocumentTab();
		document = new BrailleDocument(this, styles);
		pb = new BBProgressBar(wp.getShell());
		fontManager.setFontWidth(simBrailleDisplayed);
		srch = new SearchDialog(wp.getShell(), SWT.NONE, this);
		
		logger = BBIni.getLogger();
		
		document = new BrailleDocument(this, doc, this.styles);

		group.setRedraw(false);
		initializeViews(document.getRootElement());
		treeView.setRoot(document.getRootElement());
		document.notifyUser();
		text.initializeListeners();
		braille.initializeListeners();
		treeView.initializeListeners();
		text.hasChanged = false;
		braille.hasChanged = false;
		text.view.setWordWrap(true);
		braille.view.setWordWrap(true);
		group.setRedraw(true);
		if(list.size() == 0)
			formatTemplateDocument();
	}	
	

	private void initializeDocumentTab(){
		fontManager.setShellFonts(wp.getShell(), simBrailleDisplayed);	
		setTabList();
		wp.getShell().layout();
	}
	
	public void setTabList(){
		if(sm.panelIsVisible()){
			tabList = new Control[]{treeView.getTree(), sm.getGroup(), text.view, braille.view};
		}
		else {
			tabList = new Control[]{treeView.getTree(), text.view, braille.view};
		}
		group.setTabList(tabList);
	}
	
	///////////////////////////////////////////////////////////////
	// Opens the search/replace dialog.
	public void search() {
		srch.open();
	}
	
	public void fileSave(){	
		// Borrowed from Save As function. Different document types require 
		// different save methods.
		if(arch.getOrigDocPath() == null){
			saveAs();
		}
		else {
			checkForUpdatedViews();
			
			if(arch.getOrigDocPath().endsWith(".txt"))
				arch.save(document, arch.getOrigDocPath());
			else
				arch.save(document, null);
			
			text.hasChanged = false;
			braille.hasChanged = false;
			arch.setDocumentEdited(false);
		}
	}
	
	public void fileOpenDialog() {
		String tempName;

		String[] filterNames = new String[] { "XML", "XML ZIP", "XHTML", "HTML","HTM", "EPUB", "TEXT", "UTDML working document"};
		String[] filterExtensions = new String[] { "*.xml", "*.zip", "*.xhtml","*.html", "*.htm", "*.epub", "*.txt", "*.utd"};
		BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.OPEN, filterNames, filterExtensions);

		tempName = dialog.open();
		
		// Don't do any of this if the user failed to choose a file.
		if(tempName != null)
		{
			// Open it.
			if(arch.getOrigDocPath() != null || text.hasChanged || braille.hasChanged || documentName != null){
				wp.addDocumentManager(tempName);
			}
			else {
				closeUntitledTab();
				openDocument(tempName);
				checkTreeFocus();
			}	
		} // if(tempName != null)
	}
	
	public void openDocument(String fileName){	
		// Create archiver and massage document if necessary.
		String config = ""; 
		if(arch != null)
			config = arch.getCurrentConfig();
		
		arch = ArchiverFactory.getArchive(fileName);
		
		if(!config.equals(arch.getCurrentConfig()))
			resetConfiguations();
		
		// Recent Files.
		addRecentFileEntry(fileName);
			
		initializeAllViews(fileName, arch.getWorkingFilePath(), null);
	}	
	
	private void initializeAllViews(String fileName, String filePath, String configSettings){
		try{
			if(document.startDocument(filePath, arch.getCurrentConfig(), configSettings)){
				checkSemanticsTable();
				group.setRedraw(false);
				text.view.setWordWrap(false);
				braille.view.setWordWrap(false);
				wp.getStatusBar().resetLocation(6,100,100);
				wp.getStatusBar().setText("Loading...");
				startProgressBar();
				documentName = fileName;
				setTabTitle(fileName);
				initializeViews(document.getRootElement());
				treeView.setRoot(document.getRootElement());
				document.notifyUser();
				text.initializeListeners();
				braille.initializeListeners();
				treeView.initializeListeners();
				text.hasChanged = false;
				braille.hasChanged = false;
				wp.getStatusBar().resetLocation(0,100,100);
				pb.stop();
				wp.getStatusBar().setText("Words: " + text.words);
				braille.setWords(text.words);
				text.view.setWordWrap(true);
				braille.view.setWordWrap(true);
				group.setRedraw(true);
				checkAtributeEditor();
			}
			else {
				System.out.println("The Document Base document tree is empty");
				logger.log(Level.SEVERE, "The Document Base document tree is null, the file failed to parse properly");
				//workingFilePath = null;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			logger.log(Level.SEVERE, "Unforeseen Exception", e);
		}
	}
	
	private void initializeViews(Node current){
		if(current instanceof Text && !((Element)current.getParent()).getLocalName().equals("brl") && vaildTextElement(current, current.getValue())){
			text.setText(current, list);
		}
		
		for(int i = 0; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Element &&  ((Element)current.getChild(i)).getLocalName().equals("brl")){
				initializeBraille(current.getChild(i), list.getLast());
			}
			else if(current.getChild(i) instanceof Element &&  ((Element)current.getChild(i)).getLocalName().equals("math")){
				//if math is empty skip next brl element
				if(validateMath((Element)current.getChild(i))){
					initializeMathML((Element)current.getChild(i), (Element)current.getChild(i + 1));
				}
				else
					i++;
			}
			else {
				if(current.getChild(i) instanceof Element){
					if(((Element)current.getChild(i)).getLocalName().equals("pagenum")){
						initializePrintPage((Element)current.getChild(i));
					}
					else {
						Element currentChild = (Element)current.getChild(i);
						document.checkSemantics(currentChild);
						if(!currentChild.getLocalName().equals("meta") & !currentChild.getAttributeValue("semantics").contains("skip"))
							initializeViews(currentChild);
					}
				}
				else if(!(current.getChild(i) instanceof Element)) {
					initializeViews(current.getChild(i));
				}
			}
		}
	}
	
	private void initializeBraille(Node current, TextMapElement t){
		if(current instanceof Text && ((Element)current.getParent()).getLocalName().equals("brl")){
			Element grandParent = (Element)current.getParent().getParent();
			if(!(grandParent.getLocalName().equals("span") && document.checkAttributeValue(grandParent, "class", "brlonly")))
				braille.setBraille(list, current, t);
		}
		
		for(int i = 0; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Element){
				initializeBraille(current.getChild(i), t);
			}
			else {
				initializeBraille(current.getChild(i), t);
			}
		}
	}
	
	private void initializePrintPage(Element page){
		Node textNode = document.findPrintPageNode(page);
		if(textNode != null){
			text.addPageNumber(list, textNode);
		
			Node brailleText = document.findBraillePageNode(page);
			braille.addPageNumber(list, brailleText);
		}
	}
	
	private void initializeMathML(Element math, Element brl){
		text.setMathML(list, math);
	}
	
	private boolean validateMath(Element math){
		int count = math.getChildCount();
		for(int i = 0; i < count; i++){
			if(math.getChild(i) instanceof Text)
				return true;
			else if(math.getChild(i) instanceof Element){
				if(validateMath((Element)math.getChild(i)))
					return true;
			}
		}
		
		return false;
	}
	
	public void dispatch(Message message){
		switch(message.type){
			case INCREMENT:
				handleIncrement(message);
				break;
			case DECREMENT:
				handleDecrement(message);
				break;
			case UPDATE_CURSORS:
				handleUpdateCursors(message);
				break;
			case SET_CURRENT:
				handleSetCurrent(message);
				break;
			case GET_CURRENT:
				handleGetCurrent(message);
				break;
			case TEXT_DELETION:
				handleTextDeletion(message);
				break;
			case UPDATE:
				handleUpdate(message);
				break;
			case INSERT_NODE:
				handleInsertNode(message);
				break;
			case REMOVE_NODE:
				handleRemoveNode(message);
				break;
			case REMOVE_MATHML:
				handleRemoveMathML(message);
				break;
			case UPDATE_STATUSBAR:
				handleUpdateStatusBar(message);
				break;
			case ADJUST_ALIGNMENT:
				handleAdjustAlignment(message);
				break;
			case ADJUST_INDENT:
				handleAdjustIndent(message);
				break;
			case ADJUST_RANGE:
				list.adjustOffsets(list.getCurrentIndex(), message);
				break;
			case GET_TEXT_MAP_ELEMENTS:
				list.findTextMapElements(message);
				break;
			case UPDATE_SCROLLBAR:
				handleUpdateScrollbar(message);
				break;
			case UPDATE_STYLE:
				handleUpdateStyle(message);
				break;
			default:
				break;
		}
	}
	
	private void handleIncrement(Message message){
		list.incrementCurrent(message);
		treeView.setSelection(list.getCurrent());
		resetCursorData();
	}
	
	private void handleDecrement(Message message){
		list.decrementCurrent(message);
		treeView.setSelection(list.getCurrent());
		resetCursorData();
	}
	
	private void handleUpdateCursors(Message message){
		message.put("element", list.getCurrent().n);
		
		if(message.getValue("sender").equals(Sender.TEXT)){
			setUpdateCursorMessage(message, text.positionFromStart, text.cursorOffset);
			braille.updateCursorPosition(message);
		}
		else if(message.getValue("sender").equals(Sender.BRAILLE)) {
			setUpdateCursorMessage(message, braille.positionFromStart, braille.cursorOffset);
			text.updateCursorPosition(message);
		}
		else if(message.getValue("sender").equals(Sender.TREE)){
			setUpdateCursorMessage(message, text.positionFromStart, text.cursorOffset);
			braille.updateCursorPosition(message);
			setUpdateCursorMessage(message, braille.positionFromStart, braille.cursorOffset);
			text.updateCursorPosition(message);
		}
	}
	
	private void setUpdateCursorMessage(Message m, int lastPosition, int offset){
		m.put("lastPosition", lastPosition);
		m.put("offset", offset);
		list.getCurrentNodeData(m);
	}
	
	private void handleSetCurrent(Message message){
		int index;
		list.checkList();
		
		if(message.getValue("isBraille").equals(true)){
			index = list.findClosestBraille(message);
			list.setCurrent(index);
			list.getCurrentNodeData(message);
			treeView.setSelection(list.getCurrent());
		}
		else {
			message.put("selection", treeView.getSelection(list.getCurrent()));
			index = list.findClosest(message, 0, list.size() - 1);
			if(index == -1){
				list.getCurrentNodeData(message);
				treeView.setSelection(list.getCurrent());
			}
			else {
				list.setCurrent(index);
				list.getCurrentNodeData(message);
				treeView.setSelection(list.getCurrent());
			}
			sm.setStyleTableItem(list.getCurrent());
			resetCursorData();
		}
		
		if(treeView.getTree().isFocusControl() && !currentElementOnScreen()){
			text.view.setTopIndex(text.view.getLineAtOffset(list.getCurrent().start));
			handleUpdateScrollbar(Message.createUpdateScollbarMessage(Sender.TREE, list.getCurrent().start));
		}
	}
	
	private boolean currentElementOnScreen(){
		int viewHeight = text.view.getClientArea().height;
		int lineHeight = text.view.getLineHeight();
		int totalLines = viewHeight / lineHeight;
		
		int currentLine = text.view.getLineAtOffset(list.getCurrent().start);
		int topIndex = text.view.getTopIndex();
		
		if(currentLine >= topIndex && currentLine <= (topIndex + totalLines - 1)){
			return true;
		}
		else {
			return false;
		}
	}
	
	private void handleGetCurrent(Message message){
		message.put("selection", treeView.getSelection(list.getCurrent()));
		list.getCurrentNodeData(message);
		if(list.size() > 0)
			treeView.setSelection(list.getCurrent());
	}
	
	private void handleTextDeletion(Message message){
		int brailleStart = 0;
		list.checkList();
		if(list.size() > 0){		
			int start = (Integer)message.getValue("offset");
			int index = list.findClosest(message, 0, list.size() - 1);
			TextMapElement t = list.get(index);
			if(start < t.start){
				if(index > 0){
					if(t.brailleList.size() > 0)
						brailleStart = t.brailleList.getFirst().start + (Integer)message.getValue("length");
				}
				else{
					brailleStart = 0;
				}
			}
			else if(t.brailleList.size() > 0)
				brailleStart = t.brailleList.getLast().end;
		
			braille.removeWhitespace(brailleStart, (Integer)message.getValue("length"));
		
			if(start >= t.end && index != list.size() - 1 && list.size() > 1)
				list.shiftOffsetsFromIndex(index + 1, (Integer)message.getValue("length"), (Integer)message.getValue("length"), (Integer)message.getValue("offset"));
			else if(index != list.size() -1 || (index == list.size() - 1 && start < t.start))
				list.shiftOffsetsFromIndex(index, (Integer)message.getValue("length"), (Integer)message.getValue("length"), (Integer)message.getValue("offset"));
		}
		else
			braille.removeWhitespace(0,  (Integer)message.getValue("length"));
	}
	
	private void handleUpdate(Message message){
		message.put("selection", treeView.getSelection(list.getCurrent()));
		if(list.getCurrent().isMathML()){
			handleRemoveMathML(Message.createRemoveMathMLMessage((Integer)message.getValue("offset"), list.getCurrent().end - list.getCurrent().start, list.getCurrent()));
			message.put("diff", 0);
		}
		else {
			document.updateDOM(list, message);
			braille.updateBraille(list.getCurrent(), message);
			text.reformatText(list.getCurrent().n, message, this);
			list.updateOffsets(list.getCurrentIndex(), message);
			list.checkList();
		}
		arch.setDocumentEdited(true);
	}
	
	private void handleInsertNode(Message m){
		if(m.getValue("split").equals(true)){
			splitElement(m);
		}
		else {
			if(m.getValue("atStart").equals(true))
				insertElementAtBeginning(m);
			else
				insertElementAtEnd(m);
		}
	}
	
	private void splitElement(Message m){
		int origPos =list.getCurrent().start;
		int treeIndex = treeView.getBlockElementIndex();
		
		ArrayList<Integer> originalElements = list.findTextMapElementRange(list.getCurrentIndex(), (Element)list.getCurrent().n.getParent(), true);
		ArrayList<Element> els = document.splitElement(list, list.getCurrent(), m);
		
		int textStart = list.get(originalElements.get(0)).start;
		int textEnd = list.get(originalElements.get(originalElements.size() - 1)).end;
		int brailleStart = list.get(originalElements.get(0)).brailleList.getFirst().start;	
			
		int brailleEnd = list.get(originalElements.get(originalElements.size() - 1)).brailleList.getLast().end;
				
		int currentIndex = list.getCurrentIndex();
		
		for(int i = originalElements.size() - 1; i >= 0; i--){
			int pos = originalElements.get(i);
			
			if(pos < currentIndex){
				list.remove(pos);
				currentIndex--;
			}
			else if(pos >= currentIndex){
				list.remove(pos);
			}
		}
		
		text.clearTextRange(textStart, textEnd - textStart);
		braille.clearTextRange(brailleStart, brailleEnd - brailleStart);
		list.shiftOffsetsFromIndex(currentIndex, -(textEnd - textStart), -(brailleEnd - brailleStart), origPos);	
		
		int firstElementIndex = currentIndex;
		currentIndex = insertElement(els.get(0), currentIndex, textStart, brailleStart) - 1;
		
		String insertionString = "";
		Styles style = styles.get(styles.getKeyFromAttribute(document.getParent(list.get(currentIndex).n, true)));

		if(style.contains(StylesType.linesBefore)){
			for(int i = 0; i < Integer.valueOf((String)style.get(StylesType.linesBefore)) + 1; i++){
				insertionString += "\n";
			}
		}
		else if(style.contains(StylesType.linesAfter)){
			for(int i = 0; i < Integer.valueOf((String)style.get(StylesType.linesAfter)) + 1; i++){
				insertionString += "\n";
			}
		}
		else {
			insertionString = "\n";
		}

		text.insertText(list.get(currentIndex).end, insertionString);
		braille.insertText(list.get(currentIndex).brailleList.getLast().end, insertionString);
		//braille.insertLineBreak(list.get(currentIndex).brailleList.getLast().end);
		m.put("length", insertionString.length());
		
		int secondElementIndex = currentIndex + 1;
		currentIndex = insertElement(els.get(1), currentIndex + 1, list.get(currentIndex).end + insertionString.length(), list.get(currentIndex).brailleList.getLast().end + insertionString.length());

		list.shiftOffsetsFromIndex(currentIndex, list.get(currentIndex - 1).end - textStart, list.get(currentIndex - 1).brailleList.getLast().end - brailleStart, origPos);
		
		treeView.split(Message.createSplitTreeMessage(firstElementIndex, secondElementIndex, currentIndex, treeIndex));
	}
	
	private int insertElement(Element e, int index, int start, int brailleStart){
		int count = e.getChildCount();
		int currentIndex = index;
		int currentStart = start;
		int currentBrailleStart = brailleStart;
		
		for(int i = 0; i < count; i++){
			if(e.getChild(i) instanceof Text){
				text.insertText(list, currentIndex, currentStart, e.getChild(i));
				currentStart = list.get(currentIndex).end;
				i++;
				insertBraille((Element)e.getChild(i), currentIndex, currentBrailleStart);
				currentBrailleStart = list.get(currentIndex).brailleList.getLast().end;
				currentIndex++;
			}
			else if(e.getChild(i) instanceof Element && !((Element)e.getChild(i)).getLocalName().equals("brl")){
				currentIndex = insertElement((Element)e.getChild(i), currentIndex, currentStart, currentBrailleStart);
				currentStart = list.get(currentIndex - 1).end;
				currentBrailleStart = list.get(currentIndex - 1).brailleList.getLast().end;
			}
		}
		
		return currentIndex;
	}
	
	public void insertBraille(Element e, int index, int brailleStart){
		int count = e.getChildCount();
		
		for(int i = 0; i < count; i++){
			if(e.getChild(i) instanceof Text){
				braille.insert(list.get(index), e.getChild(i), brailleStart);
				brailleStart = list.get(index).brailleList.getLast().end;
			}
		}
	}
	
	private void insertElementAtBeginning(Message m){
		int origPos = list.getCurrent().start;
		if(list.getCurrentIndex() > 0 && list.getCurrent().start != 0)
			document.insertEmptyTextNode(list, list.getCurrent(),  list.getCurrent().start - 1, list.getCurrent().brailleList.getFirst().start - 1,list.getCurrentIndex());
		else
			document.insertEmptyTextNode(list, list.getCurrent(), list.getCurrent().start, list.getCurrent().brailleList.getFirst().start, list.getCurrentIndex());
			
		if(list.size() - 1 != list.getCurrentIndex() - 1){
			if(list.getCurrentIndex() == 0)
				list.shiftOffsetsFromIndex(list.getCurrentIndex() + 1, 1, 1, origPos);
			else
				list.shiftOffsetsFromIndex(list.getCurrentIndex(), 1, 1, origPos);
		}
		int index = treeView.getSelectionIndex();
		
		m.put("length", 1);
		m.put("newBrailleLength", 1);
		m.put("brailleLength", 0);

		//if(list.getCurrentIndex()  > 0)
		//	braille.insertLineBreak(list.get(list.getCurrentIndex() - 1).brailleList.getLast().end);
		//else
			braille.insertLineBreak(list.getCurrent().brailleList.getFirst().start - 1);
			
		treeView.newTreeItem(list.get(list.getCurrentIndex()), index, 0);
	}
	
	private void insertElementAtEnd(Message m){
		int origPos = list.getCurrent().start;
		document.insertEmptyTextNode(list, list.getCurrent(), list.getCurrent().end + 1, list.getCurrent().brailleList.getLast().end + 1, list.getCurrentIndex() + 1);
		if(list.size() - 1 != list.getCurrentIndex() + 1)
			list.shiftOffsetsFromIndex(list.getCurrentIndex() + 2, 1, 1, origPos);
		
		int index = treeView.getSelectionIndex();
		
		m.put("length", 1);
		m.put("newBrailleLength", 1);
		m.put("brailleLength", 0);

		braille.insertLineBreak(list.getCurrent().brailleList.getLast().end);
		treeView.newTreeItem(list.get(list.getCurrentIndex() + 1), index, 1);
	}
	
	public void insertTranscriberNote(){
		text.update(false);
			
		ArrayList<Integer>posList = list.findTextMapElementRange(list.getCurrentIndex(), (Element)list.getCurrent().n.getParent(), true);
			
		text.insertNewNode(list.get(posList.get(posList.size() - 1)).end);
			
		Element e = list.getCurrent().parentElement();
		e.addAttribute(new Attribute("class", "trNote"));
			
		Message styleMessage =  new Message(BBEvent.UPDATE_STYLE);
		Styles style = styles.get("trnote");
		styleMessage.put("Style", style);
		dispatch(styleMessage);
	}
	
	private void handleRemoveNode(Message message){
		int index = (Integer)message.getValue("index");
		treeView.removeItem(list.get(index), message);
		document.updateDOM(list, message);
		list.get(index).brailleList.clear();
		list.remove(index);
					
		if(list.size() == 0){
			text.removeListeners();
			braille.removeListeners();
			treeView.removeListeners();
			list.clearList();
			text.view.setEditable(false);
		}
	}
	
	private void handleRemoveMathML(Message m){
		TextMapElement t = (TextMapElement)m.getValue("TextMapElement");
		document.updateDOM(list, m);
		braille.removeMathML(t);
		text.removeMathML(m);
		treeView.removeMathML(t);
		list.updateOffsets(list.indexOf(t), m);
		list.remove(t);
		
		if(list.size() == 0){
			text.removeListeners();
			braille.removeListeners();
			treeView.removeListeners();
			list.clearList();
			text.view.setEditable(false);
		}
	}
	
	private void handleUpdateStatusBar(Message message){
		braille.setWords(text.words);
		wp.getStatusBar().setText((String)message.getValue("line"));
	}
	
	private void handleAdjustAlignment(Message message){
		braille.changeAlignment(list.getCurrent().brailleList.getFirst().start, (Integer)message.getValue("alignment"));
	}
	
	private void handleAdjustIndent(Message message){
		braille.changeIndent(list.getCurrent().brailleList.getFirst().start, message);	
	}
	
	private void handleUpdateScrollbar(Message message){
		if(message.getValue("sender").equals(Sender.BRAILLE))
			text.positionScrollbar(braille.view.getTopIndex());
		else
			braille.positionScrollbar(text.view.getTopIndex());
	}
	
	private void handleUpdateStyle(Message message){
		if(document.getDOM() != null && text.view.getText().length() > 0){
			group.setRedraw(false);
			Element parent = document.getParent(list.getCurrent().n, true);
			message.put("previousStyle", styles.get(styles.getKeyFromAttribute(parent)));
			document.changeSemanticAction(message, list.getCurrent().parentElement());
			message.put("style", styles.get(styles.getKeyFromAttribute(parent)));
			ArrayList<TextMapElement> itemList = list.findTextMapElements(list.getCurrentIndex(), parent, true);
		
			int start = list.indexOf(itemList.get(0));
			int end = list.indexOf(itemList.get(itemList.size() - 1));
	//		int currentIndex = list.getCurrentIndex();
			int origPos = list.get(list.getNodeIndex(itemList.get(0))).start;
			if(start > 0){
				message.put("prev", list.get(start - 1).end);
				message.put("braillePrev", list.get(start - 1).brailleList.getLast().end);
			}
			else {
				message.put("prev", -1);
				message.put("braillePrev", -1);
			}
			
			if(end < list.size() - 1){
				message.put("next", list.get(end + 1).start);
				message.put("brailleNext",  list.get(end + 1).brailleList.getFirst().start);
			}
			else {
				message.put("next", -1);
				message.put("brailleNext", -1);
			}
			
			text.adjustStyle(message, itemList);
			braille.adjustStyle(message, itemList);
			
			if(message.contains("linesBeforeOffset"))
				list.shiftOffsetsFromIndex(start, (Integer)message.getValue("linesBeforeOffset"), (Integer)message.getValue("linesBeforeOffset"), origPos);	
			if(message.contains("linesAfterOffset") && list.size() > 1 && end < list.size() - 1)
				list.shiftOffsetsFromIndex(end + 1, (Integer)message.getValue("linesAfterOffset"),  (Integer)message.getValue("linesAfterOffset"), origPos);

			//list.setCurrent(currentIndex);
			treeView.adjustItemStyle(list.getCurrent());
			group.setRedraw(true);
		}
		else
			new Notify(lh.localValue("nothingToApply"));
	}
	
	public void saveAs(){
		BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.SAVE, arch.getFileTypes(), arch.getFileExtensions());
		String filePath = dialog.open();
		
		if(filePath != null){
			checkForUpdatedViews();
			String ext = getFileExt(filePath);
			
			String origExt = "";
			if(arch.getOrigDocPath() != null)
				origExt = getFileExt(arch.getOrigDocPath());
			
			arch = arch.saveAs(document, filePath, ext);
			if((origExt.equals("txt") && ext.equals("txt")) || (!ext.equals("txt") && !ext.equals("brf"))){
				setTabTitle(filePath);
		    	documentName = filePath;
			}
			
		    text.hasChanged = false;
			braille.hasChanged = false;	
			arch.setDocumentEdited(false);
		}
	}
	
	@Override
	public void close() {
		if (text.hasChanged || braille.hasChanged || arch.getDocumentEdited()) {
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"));
			if (ync.result == SWT.YES) {
				fileSave();
			}
		}
		dispose();
		item.dispose();
		fontManager.disposeFonts();
		if(arch.getOrigDocPath() == null & docCount > 0)
			docCount--;
	}
	
	public void nextElement(){
		if(list.size() != 0 && list.getCurrentIndex() < list.size() - 1){		
			if(text.view.isFocusControl()){
				text.incrementCurrent();
				text.view.setCaretOffset(list.getCurrent().start);
			}
			else if(braille.view.isFocusControl()){
				braille.incrementCurrent();
				braille.view.setCaretOffset(list.getCurrent().brailleList.getFirst().start);
			}
			else {
				Message message = Message.createIncrementMessage();
				dispatch(message);
				text.view.setCaretOffset(list.getCurrent().start);
				braille.view.setCaretOffset(list.getCurrent().brailleList.getFirst().start);
			}
		}
	}
	
	public void prevElement(){
		if(list.size() != 0 && list.getCurrentIndex() > 0){
			if(text.view.isFocusControl()){
				text.decrementCurrent();
				text.view.setCaretOffset(list.getCurrent().start);
			}
			else if(braille.view.isFocusControl()){
				braille.decrementCurrent();
				braille.view.setCaretOffset(list.getCurrent().brailleList.getFirst().start);
			}
			else {
				Message message = Message.createDecrementMessage();
				dispatch(message);
				text.view.setCaretOffset(list.getCurrent().start);
				braille.view.setCaretOffset(list.getCurrent().brailleList.getFirst().start);
			}
		}
	}
	
	private void resetCursorData(){
		text.positionFromStart = 0;
		text.cursorOffset = 0;
		braille.positionFromStart = 0;
		braille.cursorOffset = 0;
	}
	
	public void textPrint(){
		PrintersManager pn = new PrintersManager(wp.getShell(), text.view);
		pn.beginPrintJob();	
	}
	
	public void fileEmbossNow() {		
		Shell shell = new Shell(wp.getShell(), SWT.DIALOG_TRIM);
		PrintDialog embosser = new PrintDialog(shell);
		PrinterData data = embosser.open();
		
		if (data == null || data.equals("")) {
			return;
		}
		
		String filePath = BBIni.getTempFilesPath() + BBIni.getFileSep() + "tempBRF.brf";
		if(this.document.createBrlFile(filePath)){
			File translatedFile = new File(filePath);
			PrinterDevice embosserDevice;
			try {
				embosserDevice = new PrinterDevice(data.name, true);
				embosserDevice.transmit(translatedFile);
				translatedFile.delete();
			} catch (PrintException e) {
				new Notify(lh.localValue("cannotEmboss") + ": " + data.name + "\n" + e.getMessage());
				logger.log(Level.SEVERE, "Print Exception", e);
			}
		}
	}
	
	public void printPreview(){
		if(braille.view.getCharCount() > 0){
			new PrintPreview(this.getDisplay(), document, this);
		}
	}
	
	private void setCurrentOnRefresh(Sender sender, int offset, boolean isBraille){
		Message m = Message.createSetCurrentMessage(sender, offset, isBraille);
		dispatch(m);
	}
	
	public void refresh(){	
		int currentOffset;
		if(document.getDOM() != null){
			if(text.view.isFocusControl()){
				currentOffset = text.view.getCaretOffset();
				resetViews();
				
				if(currentOffset < text.view.getCharCount()){
					text.view.setCaretOffset(currentOffset);
				}
				else
					text.view.setCaretOffset(0);
			
				setCurrentOnRefresh(Sender.TEXT,currentOffset, false);
				text.setPositionFromStart();
				text.view.setFocus();
			}
			else if(braille.view.isFocusControl()){
				currentOffset = braille.view.getCaretOffset();
				resetViews();
			
				braille.view.setCaretOffset(currentOffset);
				setCurrentOnRefresh(Sender.BRAILLE,currentOffset, true);	
				braille.setPositionFromStart();
				braille.view.setFocus();
			}
			else if(treeView.getTree().isFocusControl()){	
				if(text.view.getCaretOffset() > 0)
					currentOffset = text.view.getCaretOffset();
				else
					currentOffset = list.getCurrent().start;
			
				resetViews();

				setCurrentOnRefresh(null, currentOffset, false);
				text.view.setCaretOffset(currentOffset);
				text.setPositionFromStart();
			}
			else {
				currentOffset = text.view.getCaretOffset();		
				resetViews();		
				setCurrentOnRefresh(null,currentOffset, false);
				text.view.setCaretOffset(currentOffset);
				text.setPositionFromStart();
			}
		}
	}
	
	private void resetViews(){
		try {
			boolean textChanged = text.hasChanged;
			boolean brailleChanged = braille.hasChanged;
			
			String path = BBIni.getTempFilesPath() + BBIni.getFileSep() + "temp.xml";
			File f = new File(path);
			f.createNewFile();
			fu.createXMLFile(document.getNewXML(), path);
			list.clearList();
			text.removeListeners();
			text.resetView(group);
			braille.removeListeners();
			braille.resetView(group);
			treeView.removeListeners();
			treeView.resetView(group);
			initializeDocumentTab();
			text.words = 0;
			updateTempFile();
			document.deleteDOM();
			
			String fileName;
			if(arch.getOrigDocPath() == null)
				fileName = "outFile";
			else
				fileName = fu.getFileName(arch.getWorkingFilePath());
			
			if(fu.exists(BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem"))
				initializeAllViews(documentName, path, "semanticFiles " + document.getSemanticFileHandler().getDefaultSemanticsFiles() +"," + BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem\n");
			else
				initializeAllViews(documentName, path, null);
			
			f.delete();
	
			text.hasChanged = textChanged;
			braille.hasChanged = brailleChanged;
			
			if(arch.getOrigDocPath() == null && list.size() == 0)
				formatTemplateDocument();
		} 
		catch (IOException e) {
			new Notify("An error occurred while refreshing the document. Please save your work and try again.");
			e.printStackTrace();
			logger.log(Level.SEVERE, "IO Exception", e);
		}
	}
	
	private void updateTempFile(){
		String tempFile = document.getOutfile();
		if(!fu.createXMLFile(document.getDOM(), tempFile))
		    new Notify("An error occured while saving a temporary file.  Please restart brailleblaster");
	}
	
	private boolean vaildTextElement(Node n , String text){
		Element e = (Element)n.getParent();
		int index = e.indexOf(n);
		int length = text.length();
		
		if(index == e.getChildCount() - 1 || !(e.getChild(index + 1) instanceof Element && ((Element)e.getChild(index + 1)).getLocalName().equals("brl")))
			return false;
		
		for(int i = 0; i < length; i++){
			if(text.charAt(i) != '\n' && text.charAt(i) != '\t')
				return true;
		}
		
		return false;
	}
	
	public void toggleAttributeEditor(){
		if(!sm.panelIsVisible()){
			treeView.adjustLayout(false);
			if(list.size() == 0){
				sm.displayTable(null);
			}
			else {
				sm.displayTable(list.getCurrent());
			}
			setTabList();
		}
		else {
			treeView.adjustLayout(true);
			sm.hideTable();
			setTabList();
		}
	}
	
	public void checkAtributeEditor(){
		if(sm != null){
			if(!sm.getConfigFile().equals(arch.getCurrentConfig())){
				sm.resetStylePanel(arch.getCurrentConfig());
			}
		}
	}
	
	public boolean isAttributeEditorOpen(){
		if(sm != null && sm.panelIsVisible())
			return true;
		else
			return false;
	}
	
	public void checkSemanticsTable(){
		if(!styles.getConfig().equals(arch.getCurrentConfig()))
			styles.resetStyleTable(arch.getCurrentConfig());	
	}
	
	public void toggleFont(int fontType){
		if(list.size() > 0){
			Message message = new Message(null);
			message.put("offset", 0);
			int currentIndex = list.getCurrentIndex();
			
			if(document.hasEmphasisElement(list.getCurrent(), fontType)){
				document.changeTextStyle(fontType, list.getCurrent());
				list.setCurrent(list.getCurrentIndex());
				list.getCurrentNodeData(message);
				text.updateCursorPosition(message);
				braille.updateCursorPosition(message);
				text.update(true);
				
				list.setCurrent(currentIndex);
				list.getCurrentNodeData(message);
				text.updateCursorPosition(message);
				braille.updateCursorPosition(message);
			}
		}
	}
	
	public void closeUntitledTab(){
		document.deleteDOM();
		treeView.removeListeners();
		treeView.clearTree();
		text.removeListeners();
		braille.removeListeners();
		list.clearList();	
	}
	
	private void resetConfiguations(){
		document.resetBBDocument(arch.getCurrentConfig());
		styles.resetStyleTable(arch.getCurrentConfig());
		sm.getStyleTable().resetTable(arch.getCurrentConfig());
	}
	
	private void startProgressBar(){
    	if(arch.getOrigDocPath() != null)
    		pb.start();
	}
 
	public BBProgressBar getProgressBar(){
		return pb;
	}
	
	//if tree has focus when opening a document and closing an untitled document, the trees selection must be reset
	public void checkTreeFocus(){
		if(treeView.getTree().isFocusControl() && treeView.getTree().getSelectionCount() == 0)
			treeView.setSelection(list.getFirst());
	}
	
	public void checkForUpdatedViews(){
		if(text.hasChanged)
			text.update(false);
	}

	public TextMapElement getPrevious(){
		if(list.getCurrentIndex() > 0)
			return list.get(list.getCurrentIndex() - 1);
		else
			return null;
	}
	
	public TextMapElement getCurrent(){
		return list.getCurrent();
	}
	
	public TextMapElement getNext(){
		if(list.size() > 0 && list.getCurrentIndex() <= list.size() - 1)
			return list.get(list.getCurrentIndex() + 1);
		else
			return null;
	}
	
	public TextMapElement getElementInRange(int offset){
		Message m = new Message(null);
		m.put("offset", offset);
		
		int index = list.findClosest(m, 0, list.size() - 1);
		TextMapElement t = null;
		if(index != -1)
			t = list.get(index);
		
		if(t != null && offset >= t.start && offset <= t.end)
			return t;
		else
			return null;
	}
	
	public TextMapElement getClosest(int offset){
		Message m = new Message(null);
		m.put("offset", offset);
		TextMapElement t = list.get(list.findClosest(m, 0, list.size() - 1));
		return t;
	}
	
	public int indexOf(TextMapElement t){
		return list.indexOf(t);
	}
	
	public int findNodeIndex(Node n, int startIndex){
		return list.findNodeIndex(n, startIndex);
	}
	
	public TextMapElement getTextMapElement(int index){
		return list.get(index);
	}
	
	public int getListSize() {
		return list.size();
	}
	
	public void initiateSpellCheck(){
		if(text.view.getText().equals(""))
			new Notify(lh.localValue("noText"));
		else 
			new SpellCheckManager(this);
	}
	
	private BBTree loadTree(){
		PropertyFileManager prop = BBIni.getPropertyFileManager();
		String tree = prop.getProperty("tree");
		if(tree == null){
			prop.save("tree", BookTree.class.getCanonicalName().toString());
			return new BookTree(this, group);
		}
		else {			
			try {
				Class<?> clss = Class.forName(tree);
				return TreeView.createTree(clss, this, group);	
			} catch (ClassNotFoundException e) {		
				logger.log(Level.SEVERE, "Class Not Found Exception", e);
			} 
		}
		
		return null;
	}
	
	public void swapTree(Class<?> clss){
		boolean focused = false;

		if(treeView.getTree().isFocusControl())
			focused = true;
			
		treeView.removeListeners();
		treeView.dispose();
		treeView = TreeView.createTree(clss, this, group);
		setTabList();
		treeView.setRoot(document.getRootElement());
		if(focused)
			treeView.getTree().setFocus();
			
		treeView.setSelection(list.getCurrent());
		treeView.getTree().getParent().layout();
		treeView.initializeListeners();
		//save latest setting to user settings file
		BBIni.getPropertyFileManager().save("tree",  treeView.getClass().getCanonicalName().toString());
	}
	
	public void toggleBrailleFont(boolean showSimBraille){
		fontManager.toggleBrailleFont(showSimBraille);
		//save setting to user settings property file
		BBIni.getPropertyFileManager().save("simBraille", String.valueOf(showSimBraille));
		setSimBrailleDisplayed(showSimBraille);
	}
	
	public boolean loadSimBrailleProperty(){
		PropertyFileManager prop = BBIni.getPropertyFileManager();
		String simBraille = prop.getProperty("simBraille");
		if(simBraille == null){
			prop.save("simBraille", "true");
			return true;
		}
		else
			return Boolean.valueOf(simBraille);
	}
	
	public boolean inPrintPageRange(int offset){
		return list.inPrintPageRange(offset);
	}
	
	public boolean inBraillePageRange(int offset){
		return list.inBraillePageRange(offset);
	}
	
	//Values returned are in relation to position in arrayList, i.e. zero based.  Returns list size if offset is greater than last page start
	public String getCurrentPrintPage(){
		if(list.getPageCount() > 0){
			StyledText stView;
			if(text.view.isFocusControl())
				stView = text.view;
			else
				stView = braille.view;
			
			if(stView.getCaretOffset() >list.getLastPage().start)
				return String.valueOf(list.getPageCount());
			else{
				if(stView.equals(text.view))
					return list.findCurrentPrintPageValue(stView.getCaretOffset());
				else
					return list.findCurrentBraillePageValue(stView.getCaretOffset());
			}
		}
		else 
			return null;
	}
	
	public int getPrintPageStart(int offset){
		PageMapElement p = list.findPage(offset);
		
		if(p != null)
			return p.start;
		else
			return -1;  
	}
	
	public int getPrintPageEnd(int offset){
		PageMapElement p = list.findPage(offset);
		
		if(p != null)
			return p.end;
		else
			return -1;  
	}
	
	public int getBraillePageStart(int offset){
		PageMapElement p = list.findBraillePage(offset);
		
		if(p != null)
			return p.brailleStart;
		else
			return -1;  
	}
	
	public int getBraillePageEnd(int offset){
		PageMapElement p = list.findBraillePage(offset);
		
		if(p != null)
			return p.brailleEnd;
		else
			return -1;  
	}
	
	public PageMapElement getPageElement(int offset){
		return list.findPage(offset);
	}
	
	public FontManager getFontManager(){
		return fontManager;
	}
		
	public StyledText getTextView(){
		return text.view;
	}
	
	public StyledText getBrailleView(){
		return braille.view;
	}
	
	public Display getDisplay(){
		return wp.getShell().getDisplay();
	}
	
	public WPManager getWPManager(){
		return wp;
	}
	
	public Group getGroup(){
		return group;
	}
	
	public String getDocumentName(){
		return documentName;
	}
	
	public BrailleDocument getDocument(){
		return document;
	}
	
	public BBSemanticsTable getStyleTable(){
		return styles;
	}

	public TextView getText() {
		return text;
	}

	public BrailleView getBraille() {
		return braille;
	}
	
	public BBTree getTreeView(){
		return treeView;
	}

	public boolean isSimBrailleDisplayed() {
		return simBrailleDisplayed;
	}

	private void setSimBrailleDisplayed(boolean simBrailleDisplayed) {
		this.simBrailleDisplayed = simBrailleDisplayed;
	}

	public boolean isMetaContent() {
		return metaContent;
	}

	public void setMetaContent(boolean metaContent) {
		this.metaContent = metaContent;
	}

	@Override
	public void restore(WPManager wp) {
		checkTreeFocus();
	}

	@Override
	public void dispose() {
		text.update(false);
		list.clearList();
		group.dispose();
	}

	@Override
	public Document getDoc() {
		return document.getDOM();
	}

	@Override
	public void setStatusBarText(BBStatusBar statusBar) {
		if(text.view.getCharCount() > 0) 
			statusBar.setText("Words: " + text.words);
		else
			statusBar.setText("Words: " + 0);
	}

	@Override
	public boolean canReuseTab() {
		if(text.hasChanged || braille.hasChanged || documentName != null)
			return false;
		else
			return true;
	}

	@Override
	public void reuseTab(String file) {
		closeUntitledTab();
		openDocument(file);
		checkTreeFocus();
		if(docCount > 0)
			docCount--;	
	}
	
	public void setStyleTableFocus(TraverseEvent e){
		if(this.sm.getStyleTable().isVisible()){
			e.doit = false;
			sm.getStyleTable().getTable().setFocus();
		}
	}
	
	//adds or tracks a text node for a blank document when user starts 
	private void formatTemplateDocument(){
		Nodes n = document.query("/*[1]/*[2]");
			
		if(n.get(0).getChildCount() > 0){
			if(n.get(0).getChild(0).getChildCount() == 0)
				((Element)n.get(0).getChild(0)).appendChild(new Text(""));
				
			list.add(new TextMapElement(0, 0, n.get(0).getChild(0).getChild(0)));
		}
		else {
			Element p = new Element("p", document.getRootElement().getNamespaceURI());
			SemanticFileHandler sfh = new SemanticFileHandler(arch.getCurrentConfig());
			p.addAttribute(new Attribute("semantics","styles," + sfh.getDefault("p")));	
			p.appendChild(new Text(""));
			((Element)n.get(0)).appendChild(p);
			list.add(new TextMapElement(0, 0, n.get(0).getChild(0).getChild(0)));
		}
	}
}
