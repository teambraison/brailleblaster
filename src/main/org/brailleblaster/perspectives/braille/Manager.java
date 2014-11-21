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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.ArchiverFactory;
import org.brailleblaster.embossers.EmbossersManager;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventQueue;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.Range;
import org.brailleblaster.perspectives.braille.mapping.elements.SectionElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.spellcheck.SpellCheckManager;
import org.brailleblaster.perspectives.braille.stylepanel.StyleManager;
import org.brailleblaster.perspectives.braille.stylers.BoxlineHandler;
import org.brailleblaster.perspectives.braille.stylers.HideActionHandler;
import org.brailleblaster.search.*;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewFactory;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.tree.TreeView;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.brailleblaster.perspectives.braille.views.wp.WPView;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabItem;

//This class manages each document in an MDI environment. It controls the braille View and the daisy View.
public class Manager extends Controller {
	SashForm containerSash, miscSash, editorSash;
	BBTree treeView;
	private TextView text;
	private BrailleView braille;
	private BBProgressBar pb;
	private ViewInitializer vi;
	StyleManager sm;
	FormLayout layout;

	BBSemanticsTable styles;
	String documentName = null;
	String logFile = "Translate.log";
	String configSettings = null;
	static String recentFileName = null;
	LocaleHandler lh = new LocaleHandler();
	final static Logger logger = LoggerFactory.getLogger(Manager.class);
	public BrailleDocument document;
	private FontManager fontManager;
	private boolean simBrailleDisplayed;
	private MapList list;
	private EventQueue eventQueue;
	SearchDialog srch = null;
	
	//Constructor that sets things up for a new document.
	public Manager(WPManager wp, String docName) {
		super(wp);	
		eventQueue = new EventQueue();
		simBrailleDisplayed = loadSimBrailleProperty();
		fontManager = new FontManager(this);
		styles = new BBSemanticsTable(BBIni.getDefaultConfigFile());
		documentName = docName;
		item = new TabItem(wp.getFolder(), 0);
		containerSash = new SashForm(wp.getFolder(),SWT.HORIZONTAL);
		
		miscSash = new SashForm(containerSash, SWT.VERTICAL);
		treeView = TreeView.loadTree(this, miscSash);
		sm = new StyleManager(this, miscSash);
		
		editorSash = new SashForm(containerSash, SWT.HORIZONTAL);
		text = new TextView(this, editorSash, styles);
		braille = new BrailleView(this, editorSash, styles);
		
		setSash();
		setEditingView();
		item.setControl(this.containerSash);
		initializeDocumentTab();
		document = new BrailleDocument(this, styles);
		pb = new BBProgressBar(wp.getShell());
		fontManager.setFontWidth(simBrailleDisplayed);
		srch = new SearchDialog(wp.getShell(), SWT.NONE, this);
		if(docName != null)
			openDocument(docName);
		else {
			
			docCount++;
			
			// Restore?
			String restorePath = docRestore();
			if(restorePath != null)
				openDocument(restorePath);
			else {
				arch = ArchiverFactory.getArchive( templateFile, false);
				vi = ViewFactory.createUpdater(arch, document, text, braille, treeView);
				//list = vi.getList(this);
				resetConfiguations();
				initializeAllViews(docName, templateFile, null);
				//formatTemplateDocument();
				setTabTitle(docName);
			}
		}				
		
		if(BBIni.getPlatformName().equals("cocoa"))
			treeView.getTree().select(treeView.getRoot());
	}
	
	public Manager(WPManager wp, Document doc, TabItem item, Archiver arch){
		super(wp);	
		eventQueue = new EventQueue();
		this.arch = arch;
		simBrailleDisplayed = loadSimBrailleProperty();
		fontManager = new FontManager(this);
		styles = new BBSemanticsTable(arch.getCurrentConfig());
		documentName = arch.getOrigDocPath();
		this.item = item;
		containerSash = new SashForm(wp.getFolder(),SWT.NONE);
		containerSash.setLayout(new FormLayout());	
		
		miscSash = new SashForm(containerSash, SWT.VERTICAL);
		treeView = TreeView.loadTree(this, miscSash);
		sm = new StyleManager(this, miscSash);
		
		editorSash = new SashForm(containerSash, SWT.HORIZONTAL);
		text = new TextView(this, editorSash, styles);
		braille = new BrailleView(this, editorSash, styles);
		
		editorSash.setWeights(new int [] {50, 50});
		miscSash.setWeights(new int[] {100, 0});
		containerSash.setWeights(new int[] {10, 90});
		
		this.item.setControl(containerSash);
		initializeDocumentTab();
		document = new BrailleDocument(this, styles);
		pb = new BBProgressBar(wp.getShell());
		fontManager.setFontWidth(simBrailleDisplayed);
		srch = new SearchDialog(wp.getShell(), SWT.NONE, this);
		document = new BrailleDocument(this, doc, this.styles);
		vi = ViewFactory.createUpdater(arch, document, text, braille, treeView);
		
		containerSash.setRedraw(false);
		vi.initializeViews(this);
		list = vi.getList(this);
		
		treeView.setRoot(document.getRootElement());
		document.notifyUser();
		text.initializeListeners();
		braille.initializeListeners();
		treeView.initializeListeners();
		text.hasChanged = false;
		braille.hasChanged = false;
		text.view.setWordWrap(true);
		braille.view.setWordWrap(true);
		containerSash.setRedraw(true);
		
		if(BBIni.getPlatformName().equals("cocoa"))
			treeView.getTree().select(treeView.getRoot());
	}	
	

	private void initializeDocumentTab(){
		fontManager.setShellFonts(wp.getShell(), simBrailleDisplayed);	
		setTabList();
		wp.getShell().layout();
	}
	
	private void setSash(){
		PropertyFileManager pfm = BBIni.getPropertyFileManager();
		String containerWeight = pfm.getProperty("containerWeight");
		String editorWeight = pfm.getProperty("textWeight");
		
		if(containerWeight != null){
			Integer val = Integer.valueOf(containerWeight);
			containerSash.setWeights(new int [] {1000 - val, val});
		}
		else
			containerSash.setWeights(new int [] {10, 90});
		
		if(editorWeight != null) {
			Integer val = Integer.valueOf(editorWeight);
			editorSash.setWeights(new int [] {val, 1000 - val});
		}
		else
			editorSash.setWeights(new int []{50, 50});
		
		miscSash.setWeights(new int[] {100, 0});
	}
	
	private void setEditingView(){
		PropertyFileManager pfm = BBIni.getPropertyFileManager();
		String view = pfm.getProperty("editorView");
		if(view != null){
			if(view.equals(text.getClass().getCanonicalName())){
				editorSash.setMaximizedControl(text.view);
			}
			else if(view.equals(braille.getClass().getCanonicalName())){
				editorSash.setMaximizedControl(braille.view);
			}
		}
	}
	
	public void setEditingView(String key){
		text.update(false);
		PropertyFileManager pfm =BBIni.getPropertyFileManager();
		if(key == null){
			editorSash.setMaximizedControl(null);
			//reset weights using absolute value to handle obscure swt bug that results in negative weights
			int val = Integer.valueOf(pfm.getProperty("textWeight"));
			editorSash.setWeights(new int [] {val, 1000 - val});
		}
		else if(key.equals(text.getClass().getCanonicalName())){
			if(editorSash.getMaximizedControl() == null){
				int [] weight = editorSash.getWeights();
				pfm.save("textWeight", String.valueOf(Math.abs(weight[0])));
			}
			editorSash.setMaximizedControl(text.view);
		}
		else if(key.equals(braille.getClass().getCanonicalName())){
			if(editorSash.getMaximizedControl() == null){
				int [] weight = editorSash.getWeights();
				pfm.save("textWeight", String.valueOf(Math.abs(weight[0])));
			}
			editorSash.setMaximizedControl(braille.view);
		}
	}
	
	public void setTabList(){
		Control [] tabList, tabList2, tabList3;
		if(sm.panelIsVisible()){
			tabList = new Control[]{miscSash, editorSash};
			tabList2 = new Control[]{treeView.getTree(), sm.getGroup()};
			tabList3 = new Control[] {text.view, braille.view};
		}
		else { 
			tabList = new Control[]{miscSash, editorSash};
			tabList2 = new Control[]{treeView.getTree()};
			tabList3 = new Control[] {text.view, braille.view};
		}
		
		containerSash.setTabList(tabList);
		miscSash.setTabList(tabList2);
		editorSash.setTabList(tabList3);
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
			
			arch.save(document, null);
			
			text.hasChanged = false;
			braille.hasChanged = false;
			arch.setDocumentEdited(false);
		}
	}
	
	public void openDocument(String fileName){	
		// If this is the first document, load a previous session.
		boolean restoreArchive = false;
		String restorePath = docRestore();
		if(restorePath != null) {
			restoreArchive = true;
			fileName = restorePath;
		}
		else if( restorePath == null && fileName != null )
			restoreArchive = false;
		else
			restoreArchive = false;
		
		// Create archiver and massage document if necessary.
		String config = ""; 
		if(arch != null)
			config = arch.getCurrentConfig();
		
		arch = ArchiverFactory.getArchive(fileName, restoreArchive);
		
		if(!config.equals(arch.getCurrentConfig()))
			resetConfiguations();
		
		// Recent Files.
		addRecentFileEntry(fileName);
		initializeAllViews(fileName, arch.getWorkingFilePath(), null);
		
		// Start the auto-saver
		if(!BBIni.debugging())
			arch.resumeAutoSave( document, arch.getWorkingFilePath() );
	}	
	
	private void initializeAllViews(String fileName, String filePath, String configSettings){
		try{
			if(document.startDocument(filePath, arch.getCurrentConfig(), configSettings)){
				checkSemanticsTable();
				containerSash.setRedraw(false);
				text.view.setWordWrap(false);
				braille.view.setWordWrap(false);
				wp.getStatusBar().resetLocation(6,100,100);
				wp.getStatusBar().setText("Loading...");
				startProgressBar();
				documentName = fileName;
				setTabTitle(fileName);
				vi = ViewFactory.createUpdater(arch, document, text, braille, treeView);
				vi.initializeViews(this);
				list = vi.getList(this);
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
				containerSash.setRedraw(true);
				checkAtributeEditor();
			}
			else {
				System.out.println("The Document Base document tree is empty");
				logger.error("The Document Base document tree is null, the file failed to parse properly");
				//workingFilePath = null;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			logger.error("Unforeseen Exception", e);
		}
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
				findTextMapElements(message);
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
	
	public void checkView(TextMapElement t){
		if(!list.contains(t))
			list = vi.bufferViews(getSection(t));
	}
	
	public void decrementView(){
		TextMapElement t = list.getFirst();
		int section = getSection(t);
		if(section != 0){
			list = vi.bufferViews(section);
			list.setCurrent(list.indexOf(t) - 1);
			text.resetOffsets();
		}
	}
	
	public void incrementView(){
		TextMapElement t = list.getLast();
		int section = getSection(t);
		if(section != vi.getSectionList().size() - 1){
			list = vi.bufferViews(section + 1);
			list.setCurrent(list.indexOf(t) + 1);
			text.resetOffsets();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void findTextMapElements(Message message){
		ArrayList<Text>textList = (ArrayList<Text>)message.getValue("nodes");
		
		ArrayList<SectionElement>secList = vi.getSectionList();
		Element e = (Element)textList.get(0).getParent();
		while(e != null && !e.getLocalName().equals("level1") && !e.getLocalName().equals("body")){
			e = (Element)e.getParent();
		}
		
		if(secList.size() > 1){
			for(int i = 0; i < secList.size(); i++){
				if(secList.get(i).getParent().equals(e)){
					if(secList.get(i).isVisible())
						list.findTextMapElements(message);
					else {
						secList.get(i).getList().findTextMapElements(message);
						//list = vi.resetViews(i);
						//list.findTextMapElements(message);
					}
				}
			}
		}
		else
			list.findTextMapElements(message);
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
		boolean viewReset = false;
		list.checkList();
		
		if(message.getValue("isBraille").equals(true)){
			message.put("selection", treeView.getSelection(list.getCurrent()));
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
		
		if(list.getCurrentIndex() == list.size() - 1 && !vi.getSectionList().get(vi.getSectionList().size() -  1).isVisible()){
			list = vi.bufferForward();
			list.getCurrentNodeData(message);
			viewReset = true;
		}
		else if(list.getCurrentIndex() == 0 && !vi.getSectionList().get(0).isVisible() && getSection(list.getCurrent()) != vi.getSectionList().size() - 1){
			list = vi.bufferBackward();
			list.getCurrentNodeData(message);
			viewReset = true;
		}
		
		if((treeView.getTree().isFocusControl() && !currentElementOnScreen()) || viewReset){
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
		
		if(currentLine >= topIndex && currentLine <= (topIndex + totalLines - 1))
			return true;
		else 
			return false;
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
		
		ArrayList<Integer> originalElements = list.findTextMapElementRange(list.getCurrentIndex(), (Element)list.getCurrent().parentElement(), true);
		ArrayList<Element> els = document.splitElement(list, list.getCurrent(), m);
		
		int textStart = list.get(originalElements.get(0)).start;
		int textEnd = list.get(originalElements.get(originalElements.size() - 1)).end;
		int brailleStart = list.get(originalElements.get(0)).brailleList.getFirst().start;	
			
		int brailleEnd = list.get(originalElements.get(originalElements.size() - 1)).brailleList.getLast().end;
				
		int currentIndex = list.getCurrentIndex();
		
		for(int i = originalElements.size() - 1; i >= 0; i--){
			int pos = originalElements.get(i);
			
			if(pos < currentIndex){
				vi.remove(list, pos);
				currentIndex--;
			}
			else if(pos >= currentIndex){
				vi.remove(list, pos);
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
				text.insertText(vi,list, currentIndex, currentStart, e.getChild(i));
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
			document.insertEmptyTextNode(vi, list, list.getCurrent(),  list.getCurrent().start - 1, list.getCurrent().brailleList.getFirst().start - 1,list.getCurrentIndex(),(String) m.getValue("elementName"));
		else
			document.insertEmptyTextNode(vi, list, list.getCurrent(), list.getCurrent().start, list.getCurrent().brailleList.getFirst().start, list.getCurrentIndex(),(String) m.getValue("elementName"));
			
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

		braille.insertLineBreak(list.getCurrent().brailleList.getFirst().start - 1);
			
		treeView.newTreeItem(list.get(list.getCurrentIndex()), index, 0);
	}
	
	private void insertElementAtEnd(Message m){
		int origPos = list.getCurrent().start;
		document.insertEmptyTextNode(vi, list, list.getCurrent(), list.getCurrent().end + 1, list.getCurrent().brailleList.getLast().end + 1, list.getCurrentIndex() + 1,(String) m.getValue("elementName"));
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
	    if (arch.getCurrentConfig().equals("epub.cfg")){
	    	text.insertNewNode(list.get(posList.get(posList.size() - 1)).end,"aside");
	    }
	    else{		
		    text.insertNewNode(list.get(posList.get(posList.size() - 1)).end,"prodnote");
	    }
			
		Styles style = styles.get("trnote");
		Message styleMessage =  Message.createUpdateStyleMessage(style, false, false);
		dispatch(styleMessage);
	}
	
	private void handleRemoveNode(Message message){
		int index = (Integer)message.getValue("index");
		treeView.removeItem(list.get(index), message);
		document.updateDOM(list, message);
		list.get(index).brailleList.clear();
		vi.remove(list, index);
		//list.remove(index);
					
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
		int index = list.indexOf(t);
		list.updateOffsets(index, m);
		vi.remove(list, index);
		//list.remove(t);
		
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
	
	/***
     * Get parent style of the current TextMapElement 
     * @param current
     * @param message
     * @return
     */
	private Element parentStyle(TextMapElement current, Message message) {
		Element parent;
		if(current instanceof PageMapElement || current instanceof BrlOnlyMapElement)
			parent = current.parentElement();
		else
			parent = document.getParent(current.n, true);
		
		message.put("previousStyle", styles.get(styles.getKeyFromAttribute(parent)));
		return parent;
	}
	
	/***
	 * Handle style for all cases
	 * @param message
	 */
	private void handleUpdateStyle(Message message) {
		if (document.getDOM() != null && text.view.getText().length() > 0) {
			containerSash.setRedraw(false);
			if(message.getValue("isBoxline").equals(true)){
				if(message.getValue("multiSelect").equals(false)) 
					handleSingleBoxLine(message);
				else 
					handleMultiBoxLine(message);
			}
			else{
				if(message.getValue("multiSelect").equals(false)) 
					handleStyleSingleSelected(message);
				else 
					handleStyleMultiSelected(message);
			}
			containerSash.setRedraw(true);
		}
		else
			notify(lh.localValue("nothingToApply"));
	}
	
	/***
	 * Handle style if user just move cursor
	 * @param message: Message object passed containing information from style table manager
	 */
	private void handleStyleSingleSelected(Message message) {
		Element parent = parentStyle(list.getCurrent(), message);
		document.changeSemanticAction(message, parent);
		ArrayList<TextMapElement> itemList = list.findTextMapElements(list.getCurrentIndex(), parent, true);
		adjustStyle(itemList, message);
	}
	
	/***
	 * Apply styles to selected text for multiple elements
	 * @param start: starting offset of highlighted text used to find first block element
	 * @param end: end position of highlighted text used to find last block element
	 * @param message: Message object passed containing information from style table manager
	 */
	private void handleStyleMultiSelected(Message message){
		
		int start=text.getSelectedText()[0];
		int end=text.getSelectedText()[1];
		
		Set<TextMapElement> itemSet = getElementSelected(start, end);		
		Iterator<TextMapElement> itr = itemSet.iterator();
		ArrayList<Element>parents = new ArrayList<Element>();
		
		while (itr.hasNext()) {
			TextMapElement tempElement= itr.next();
			if( (!((tempElement instanceof BrlOnlyMapElement) || (tempElement instanceof PageMapElement)))){
				Message styleMessage = Message.createUpdateStyleMessage((Styles)message.getValue("Style"), (Boolean)message.getValue("multiSelect"), (Boolean)message.getValue("isBoxline"));
				Element parent = parentStyle(tempElement, styleMessage);
				parents.add(parent);
				document.changeSemanticAction(message, parent);
				ArrayList<TextMapElement> itemList = list.findTextMapElements(list.getNodeIndex(tempElement), parent, true);
				adjustStyle( itemList,styleMessage);
			}
		}
	}
	
	
	/** Prepares object needed by boxline handler to create boxline around a single element
	 * @param message: Message object passed containing information from style table manager
	 */
	private void handleSingleBoxLine(Message message){
		Element parent = parentStyle(list.getCurrent(), message);
		ArrayList<Element>parents = new ArrayList<Element>();
		parents.add(parent);
		ArrayList<TextMapElement> itemList = list.findTextMapElements(list.getCurrentIndex(), parent, true);
		
		if(((Styles)message.getValue("Style")).getName().equals("boxline")){
			boolean invalid = false;
			for(int i = 0; i < itemList.size() && !invalid; i++){
				if(itemList.get(i) instanceof PageMapElement)
					invalid = true;
			}
			if(!invalid){
				adjustStyle(itemList, message);
				BoxlineHandler bxh = new BoxlineHandler(this, list, vi);
				bxh.createBoxline(parents, message, itemList);
			}
		else
			notify(lh.localValue("invalidBoxline.containsPage"));
		}
		else {
			adjustStyle(itemList, message);
			TextMapElement box = list.findJoiningBoxline((BrlOnlyMapElement)itemList.get(0));
			if(box != null){
				if(list.indexOf(box) < list.indexOf(itemList.get(0)))
					itemList.add(0, box);
				else
					itemList.add(box);
			}
			
			BoxlineHandler bxh = new BoxlineHandler(this, list, vi);
			bxh.removeSingleBoxline(parent, itemList);
			
			if(list.getCurrentIndex() > list.size())
				dispatch(Message.createSetCurrentMessage(Sender.TEXT, list.get(list.size() - 1).start, false));
			else if(list.size() > 0)
				dispatch(Message.createSetCurrentMessage(Sender.TEXT, list.get(list.getCurrentIndex()).start, false));
		}
	}
	
	/** Prepares object needed by boxline handler to create boxline around a multiple elements
	 * @param message: Message object passed containing information from style table manager
	 */
	private void handleMultiBoxLine(Message message){
		int start=text.getSelectedText()[0];
		int end=text.getSelectedText()[1];
		
		Set<TextMapElement> itemSet = getElementSelected(start, end);		
		Iterator<TextMapElement> itr = itemSet.iterator();
		ArrayList<Element>parents = new ArrayList<Element>();
		ArrayList<TextMapElement>itemList = new ArrayList<TextMapElement>();
		
		boolean invalid = false;
		
		while(itr.hasNext() && !invalid){
			TextMapElement tempElement= itr.next();
			if(tempElement instanceof BrlOnlyMapElement){
				BrlOnlyMapElement b = list.findJoiningBoxline((BrlOnlyMapElement)tempElement);
				if((b == null && !tempElement.parentElement().getAttributeValue("semantics").contains("middleBox") && !tempElement.parentElement().getAttributeValue("semantics").contains("bottomBox") )
						|| (b != null && (b.start > end || b.end < start))){
					invalid = true;
					notify(lh.localValue("invalidBoxline.incorrectSelection"));
					break;
				}
			}
			Element parent = parentStyle(tempElement, message);
			itemList.addAll(list.findTextMapElements(list.getNodeIndex(tempElement), parent, true));
			parents.add(parent);
		}
		
		for(int i = 0; i < itemList.size() && !invalid; i++){
			if(itemList.get(i) instanceof PageMapElement){
				invalid = true;
				notify(lh.localValue("invalidBoxline.containsPage"));
			}
		}
		
		if(!invalid){
			if(((Styles)message.getValue("Style")).getName().equals("boxline")){
				adjustStyle(itemList, message);
				BoxlineHandler bxh = new BoxlineHandler(this, list, vi);
				bxh.createBoxline(parents, message, itemList);	
			}
			else {
				BoxlineHandler bxh = new BoxlineHandler(this, list, vi);
				bxh.removeMultiBoxline(itemList);
				if(list.getCurrentIndex() > list.size())
					dispatch(Message.createSetCurrentMessage(Sender.TEXT, list.get(list.size() - 1).start, false));
				else if(list.size() > 0)
					dispatch(Message.createSetCurrentMessage(Sender.TEXT, list.get(list.getCurrentIndex()).start, false));
				
				dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
			}
		}
	}
	
	/***
	 * Adjust style of elements in the list base on previous and next element 
	 * @param itemList : all selected items which we want style to be applied
	 * @param message : passing information regarding styles
	 */
	private void adjustStyle(ArrayList<TextMapElement> itemList, Message message) {
		int start = list.indexOf(itemList.get(0));
		int end = list.indexOf(itemList.get(itemList.size() - 1));
		int origPos = list.get(list.getNodeIndex(itemList.get(0))).start;
		if (start > 0) {
			message.put("prev", list.get(start - 1).end);
			message.put("braillePrev",
					list.get(start - 1).brailleList.getLast().end);
		} else {
			message.put("prev", -1);
			message.put("braillePrev", -1);
		}

		if (end < list.size() - 1) {
			message.put("next", list.get(end + 1).start);
			message.put("brailleNext",
					list.get(end + 1).brailleList.getFirst().start);
		} else {
			message.put("next", -1);
			message.put("brailleNext", -1);
		}

		text.adjustStyle(message, itemList);
		braille.adjustStyle(message, itemList);

		if (message.contains("linesBeforeOffset"))
			list.shiftOffsetsFromIndex(start,
					(Integer) message.getValue("linesBeforeOffset"),
					(Integer) message.getValue("linesBeforeOffset"), origPos);
		if (message.contains("linesAfterOffset") && list.size() > 1
				&& end < list.size() - 1)
			list.shiftOffsetsFromIndex(end + 1,
					(Integer) message.getValue("linesAfterOffset"),
					(Integer) message.getValue("linesAfterOffset"), origPos);

		treeView.adjustItemStyle(list.getCurrent());
	}	
	
	public void saveAs(){
		String filePath;
		
		if(!BBIni.debugging()){
			BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.SAVE, arch.getFileTypes(), arch.getFileExtensions());
			filePath = dialog.open();
		}
		else
			filePath = BBIni.getDebugSavePath();
		
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
		boolean cancel = false;
		if (!BBIni.debugging() && documentHasBeenEdited()) {
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"), true);
			if (ync.result == SWT.YES) {
				fileSave();
			}
			else if(ync.result == SWT.CANCEL)
				cancel = true;
		}
		
		if(!cancel){
			// Shut down auto-save feature.
			arch.destroyAutoSave();
			// Clear previous session information. Otherwise, we'll be looking 
			// for files in the user's temp folder that aren't there.
			arch.clearPrevSession();
			saveScreenProperties();
			dispose();
			item.dispose();
			fontManager.disposeFonts();
			if(arch.getOrigDocPath() == null & docCount > 0)
				docCount--;
			arch.destroyAutoSave();
			wp.removeController(this);
			
			if(wp.getList().size() == 0)
				wp.getStatusBar().setText("");
		}
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
		EmbossersManager embosser = new EmbossersManager();
		embosser.emboss(document);
		embosser.close();
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
		Control c = editorSash.getMaximizedControl();
		int [] weights = editorSash.getWeights();
		WPView view = null;
		if(c != null){
			if(c.equals(text.view))
				view = text;
			else if(c.equals(braille.view))
				view = braille;
		}
		editorSash.setRedraw(false);
		if(document.getDOM() != null){
			if(text.view.isFocusControl()){
				currentOffset = text.view.getCaretOffset();
				resetViews();
				
				if(currentOffset < text.view.getCharCount())
					text.view.setCaretOffset(currentOffset);
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
		
		if(c != null){
			editorSash.setMaximizedControl(view.view);
		}
		else
			editorSash.setWeights(weights);
		editorSash.setRedraw(true);
	}
	
	private void resetViews(){
		try {
			boolean textChanged = text.hasChanged;
			boolean brailleChanged = braille.hasChanged;
			
			int index = getSection(list.getCurrent());
			String path = BBIni.getTempFilesPath() + BBIni.getFileSep() + "temp.xml";
			File f = new File(path);
			f.createNewFile();
			fu.createXMLFile(document.getNewXML(), path);
			list.clearList();
			text.removeListeners();
			text.resetView(editorSash);
			braille.removeListeners();
			braille.resetView(editorSash);
			treeView.removeListeners();
			treeView.resetView(miscSash);
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
			
			if(index != -1)
				vi.bufferViews(index);
			//if(arch.getOrigDocPath() == null && list.size() == 0)
			//	formatTemplateDocument();
		} 
		catch (IOException e) {
			new Notify("An error occurred while refreshing the document. Please save your work and try again.");
			e.printStackTrace();
			logger.error("IO Exception", e);
		}
	}
	
	private void updateTempFile(){
		String tempFile = document.getOutfile();
		if(!fu.createXMLFile(document.getDOM(), tempFile))
		    new Notify("An error occured while saving a temporary file.  Please restart brailleblaster");
	}
	
	public void toggleAttributeEditor(){
		if(!sm.panelIsVisible()){
			if(list.size() == 0){
				sm.displayTable(null);
			}
			else {
				sm.displayTable(list.getCurrent());
			}
			setTabList();
			PropertyFileManager pfm = BBIni.getPropertyFileManager();
			String weight = pfm.getProperty("stylePanelWeight");
			if(weight != null){
				Integer val = Integer.valueOf(weight);
				miscSash.setWeights(new int [] {1000 - val, val});
			}
			else
				miscSash.setWeights(new int[]{50, 50});
		}
		else {
			int [] panelWeights = miscSash.getWeights();
			PropertyFileManager pfm = BBIni.getPropertyFileManager();
			pfm.save("stylePanelWeight", String.valueOf(panelWeights[1]));
			sm.hideTable();
			setTabList();
			miscSash.setWeights(new int[]{100, 0});
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
	
	public void hide(){
		HideActionHandler h = new HideActionHandler(this, list, vi);
		h.hideText();
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
    	if(arch.getOrigDocPath() != null && !BBIni.debugging())
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
		m.put("selection", treeView.getSelection(list.getCurrent()));
		
		int index = list.findClosest(m, 0, list.size() - 1);
		TextMapElement t = null;
		if(index != -1)
			t = list.get(index);
		
		if(t != null && offset >= t.start && offset <= t.end)
			return t;
		else
			return null;
	}
	/***
	 * Return all elements that selected in text
	 * @param start :start location of where text selected
	 * @param end:  where selection ended
	 * @return: Set of all element where in selection
	 */
	public Set<TextMapElement> getElementSelected(int start, int end) {
		
		Set<TextMapElement> elementSelectedSet = new LinkedHashSet<TextMapElement>();
		Set<Element> parentElement = new LinkedHashSet<Element>();
		int j=start;
		while( j < end) {
			TextMapElement t = getElementInRange(j);
			
			if (t != null) {
				Element currentParent = document.getParent(t.n, true);
				if(!(parentElement.contains(currentParent))){
					parentElement.add(currentParent);
					elementSelectedSet.add(t);
				}
				
				j=t.end+1;
				
			}
			else
			{
			    j=j+1;	
			}
		}
		return elementSelectedSet;
	}
	
	public TextMapElement getElementInBrailleRange(int offset){
		Message m = new Message(null);
		m.put("offset", offset);
		m.put("selection", treeView.getSelection(list.getCurrent()));
		
		int index = list.findClosestBraille(m);
		TextMapElement t = null;
		if(index != -1)
			t = list.get(index);
		
		if(t != null && offset >= t.brailleList.getFirst().start && offset <= t.brailleList.getLast().end)
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
	
	public int indexOf(int section, TextMapElement t){
		if(vi.getSectionList().size() > 1)
			return vi.getSectionList().get(section).getList().indexOf(t);
		else
			return list.indexOf(t);
	}
	
	public int getSectionSize(int index){
		return vi.getSectionList().get(index).getList().size();
	}
	
	public int getSection(TextMapElement t){
		for(int i = 0; i < vi.getSectionList().size(); i++){
			if(vi.getSectionList().get(i).getList().contains(t))
				return i;
		}
		
		return -1;
	}
	
	public int getSection(Node n){
		for(int i = 0; i < vi.getSectionList().size(); i++){
			if(vi.getSectionList().get(i).getList().contains(n))
				return i;
		}
		
		return -1;
	}
	
	public int getSection(int section, Node n){
		for(int i = section; i < vi.getSectionList().size(); i++){
			if(vi.getSectionList().get(i).getList().contains(n))
				return i;
		}
		
		return -1;
	}
	
	public Range getRange(int section, int listIndex, Node n){
		for(int i = section; i < vi.getSectionList().size(); i++){
			int index = vi.getSectionList().get(i).getList().findNodeIndex(n, listIndex);
			if(index != -1)
				return new Range(i, index);
			
			listIndex = 0;
		}
		
		return null;
	}
	
	public int findNodeIndex(Node n, int startIndex){
		return list.findNodeIndex(n, startIndex);
	}
	
	public int findNodeIndex(Node n, int section, int startIndex){
		return vi.getSectionList().get(section).getList().findNodeIndex(n, startIndex);
	}
	
	public TextMapElement getTextMapElement(int index){
		return list.get(index);
	}
	
	public TextMapElement getTextMapElement(int section, int index){
		return vi.getSectionList().get(section).getList().get(index);
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
	
	public void swapTree(Class<?> clss){
		boolean focused = false;

		int [] weights = miscSash.getWeights();
		if(treeView.getTree().isFocusControl())
			focused = true;
			
		treeView.removeListeners();
		treeView.dispose();
		treeView = TreeView.createTree(clss, this, miscSash);
		setTabList();
		treeView.setRoot(document.getRootElement());
		if(focused)
			treeView.getTree().setFocus();
			
		treeView.setSelection(list.getCurrent());
		treeView.getTree().getParent().layout();
		treeView.initializeListeners();
		vi.resetTree(treeView);
		
		sm.getGroup().moveBelow(treeView.getTree());
		miscSash.layout();
		miscSash.setWeights(weights);
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
			if(text.view.isFocusControl()){
				stView = text.view;
				return list.findCurrentPrintPageValue(stView.getCaretOffset());
			}
			else {
				stView = braille.view;
				return list.findCurrentBraillePageValue(stView.getCaretOffset());
			}
		}
		else 
			return null;
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
	
	public SashForm getGroup(){
		return containerSash;
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

	@Override
	public void restore(WPManager wp) {
		checkTreeFocus();
	}

	@Override
	public void dispose() {
		text.update(false);
		list.clearList();
		containerSash.dispose();
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
		if(sm.getStyleTable().isVisible()){
			e.doit = false;
			sm.getStyleTable().getTable().setFocus();
		}
	}
	
	private void saveScreenProperties(){
		PropertyFileManager pfm = BBIni.getPropertyFileManager();
		int [] containerWeights = containerSash.getWeights();
		int [] editorWeights = editorSash.getWeights();
		
		pfm.save("containerWeight", String.valueOf(Math.abs(containerWeights[1])));
		
		if(editorSash.getMaximizedControl() == null)
			pfm.save("textWeight", String.valueOf(Math.abs(editorWeights[0])));
		
		if(sm.panelIsVisible()){
			int [] panelWeights = miscSash.getWeights();
			pfm.save("stylePanelWeight", String.valueOf(Math.abs(panelWeights[1])));
		}
		
		if(editorSash.getMaximizedControl() != null){
			if(editorSash.getMaximizedControl().equals(text.view))
				pfm.save("editorView", text.getClass().getCanonicalName());
			else if(editorSash.getMaximizedControl().equals(braille.view))
				pfm.save("editorView", braille.getClass().getCanonicalName());
		}
		else
			pfm.save("editorView", "");
	}
	
	@Override
	public boolean documentHasBeenEdited(){
		return text.hasChanged || braille.hasChanged || arch.getDocumentEdited();
	}
	
	public String getCurrentEditor(){
		Control c = editorSash.getMaximizedControl();
		if(c != null){
			if(c.equals(text.view))
				return text.getClass().getCanonicalName();
			else if(c.equals(braille.view))
				return braille.getClass().getCanonicalName();
			else
				return null;
		}
		else
			return null;
	}
	
	public void addEvent(EventFrame f){
		eventQueue.add(f);
	}
	
	public void undo(){
		eventQueue.popEvent(vi, document, list, this);
	}

	/** Creates a Notify class alert box if debugging is not active
	 * @param notify : String to be used in an alert box, should already be localized
	 */
	private void notify(String notify){
		if(!BBIni.debugging())
			new Notify(notify);
	}
	
}
