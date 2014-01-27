/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2010, 2012
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
 * and
 * American Printing House for the Blind, Inc. www.aph.org
 *
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

package org.brailleblaster.perspectives.braille;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Properties;
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
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.MapList;
import org.brailleblaster.perspectives.braille.mapping.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.BBEvent;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.spellcheck.SpellCheckManager;
import org.brailleblaster.perspectives.braille.stylepanel.StyleManager;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.tree.BookTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.brailleblaster.wordprocessor.BBProgressBar;
import org.brailleblaster.printers.PrintPreview;
import org.brailleblaster.printers.PrintersManager;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.YesNoChoice;
import org.brailleblaster.util.Zipper;
import org.brailleblaster.wordprocessor.BBFileDialog;
import org.brailleblaster.wordprocessor.BBStatusBar;
import org.brailleblaster.wordprocessor.FontManager;
import org.brailleblaster.wordprocessor.WPManager;
import org.daisy.printing.PrinterDevice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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
	Archiver arch = null;
	
	//Constructor that sets things up for a new document.
	public Manager(WPManager wp, String docName) {
		super(wp, docName);
//		
		simBrailleDisplayed = loadSimBrailleProperty();
		fontManager = new FontManager(this);
		this.styles = new BBSemanticsTable(currentConfig);
		this.documentName = docName;
		this.list = new MapList(this);
		this.item = new TabItem(wp.getFolder(), 0);
		this.group = new Group(wp.getFolder(),SWT.NONE);
		this.group.setLayout(new FormLayout());	
		this.sm = new StyleManager(this);
		this.treeView = loadTree();
		this.text = new TextView(this.group, this.styles);
		this.braille = new BrailleView(this.group, this.styles);
		this.item.setControl(this.group);
		initializeDocumentTab();
		this.document = new BrailleDocument(this, this.styles);
		pb = new BBProgressBar(wp.getShell());
		fontManager.setFontWidth(simBrailleDisplayed);
		
		logger = BBIni.getLogger();
		
		if(docName != null)
			openDocument(docName);
		else {
			docCount++;
			initializeAllViews(docName, BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "dtbook.xml", null);
			Nodes n = this.document.query("/*/*[2]/*[2]/*[1]/*[1]");
			((Element)n.get(0)).appendChild(new Text(""));
			this.list.add(new TextMapElement(0, 0, n.get(0).getChild(0)));
			setTabTitle(docName);
		}				
	}
	
	public Manager(WPManager wp, String docName, Document doc, TabItem item){
		super(wp, docName);
		
		simBrailleDisplayed = loadSimBrailleProperty();
		fontManager = new FontManager(this);
		this.styles = new BBSemanticsTable(currentConfig);
		this.documentName = docName;
		this.list = new MapList(this);
		this.item = item;
		this.group = new Group(wp.getFolder(),SWT.NONE);
		this.group.setLayout(new FormLayout());	
		this.sm = new StyleManager(this);
		this.treeView = loadTree();
		this.text = new TextView(this.group, this.styles);
		this.braille = new BrailleView(this.group, this.styles);
		this.item.setControl(this.group);
		initializeDocumentTab();
		this.document = new BrailleDocument(this, this.styles);
		pb = new BBProgressBar(wp.getShell());
		fontManager.setFontWidth(simBrailleDisplayed);
		
		logger = BBIni.getLogger();
		
		this.document = new BrailleDocument(this, doc, this.styles);

		group.setRedraw(false);
		initializeViews(document.getRootElement());
		treeView.setRoot(document.getRootElement());
		document.notifyUser();
		text.initializeListeners(this);
		braille.initializeListeners(this);
		treeView.initializeListeners(this);
		text.hasChanged = false;
		braille.hasChanged = false;
		text.view.setWordWrap(true);
		braille.view.setWordWrap(true);
		group.setRedraw(true);
		if(list.size() == 0){
			Nodes n = this.document.query("/*/*[2]/*[2]/*[1]/*[1]");
			if(n.get(0).getChildCount() > 0)
				this.list.add(new TextMapElement(0, 0, n.get(0).getChild(0)));
			else {
				((Element)n.get(0)).appendChild(new Text(""));
				this.list.add(new TextMapElement(0, 0, n.get(0).getChild(0)));
			}
		}
	}	
	

	private void initializeDocumentTab(){
		fontManager.setShellFonts(wp.getShell(), simBrailleDisplayed);	
		setTabList();
		wp.getShell().layout();
	}
	
	public void setTabList(){
		if(sm.panelIsVisible()){
			tabList = new Control[]{treeView.getView(), sm.getGroup(), text.view, braille.view};
		}
		else {
			tabList = new Control[]{treeView.getView(), text.view, braille.view};
		}
		group.setTabList(tabList);
	}
	
	public void fileSave(){	
		// Borrowed from Save As function. Different document types require 
		// different save methods.
		if(workingFilePath == null){
			saveAs();
		}
		else {
			checkForUpdatedViews();
			
			if(arch != null) { // Save archiver supported file.
				if(arch.getOrigDocPath().endsWith("epub"))
					arch.save(document, null);
				else if(arch.getOrigDocPath().endsWith("xml"))
				{
					if(fu.createXMLFile(document.getNewXML(), workingFilePath)){
						String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem"; 
						copySemanticsFile(tempSemFile, fu.getPath(workingFilePath) + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem");
					}
					else {
						new Notify("An error occured while saving your document.  Please check your original document.");
					}
				}
			}
			else if(workingFilePath.endsWith("xml")){
				if(fu.createXMLFile(document.getNewXML(), workingFilePath)){
					String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem"; 
					copySemanticsFile(tempSemFile, fu.getPath(workingFilePath) + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem");
				}
				else {
					new Notify("An error occured while saving your document.  Please check your original document.");
				}
			}
			else if(workingFilePath.endsWith("utd")) {		
				document.setOriginalDocType(document.getDOM());
				if(fu.createXMLFile(document.getDOM(), workingFilePath)){
					String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem"; 
					copySemanticsFile(tempSemFile, fu.getPath(workingFilePath) + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem");
				}
				else {
					new Notify("An error occured while saving your document.  Please check your original document.");
				}
			}
			else if(workingFilePath.endsWith("brf")){
				if(!document.createBrlFile(this, workingFilePath)){
					new Notify("An error has occurred.  Please check your original document");
				}
			}
			
			// If the document came from a zip file, then rezip it.
			if(zippedPath.length() > 0)
				zipDocument();
		
			text.hasChanged = false;
			braille.hasChanged = false;
		}
	}
	
	public void fileOpenDialog() {
		String tempName;

		String[] filterNames = new String[] { "XML", "XML ZIP", "XHTML", "HTML","HTM", "EPUB", "TEXT", "BRF", "UTDML working document", };
		String[] filterExtensions = new String[] { "*.xml", "*.zip", "*.xhtml","*.html", "*.htm", "*.epub", "*.txt", "*.brf", "*.utd", };
		BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.OPEN, filterNames, filterExtensions);
		
		tempName = dialog.open();
		
		// Don't do any of this if the user failed to choose a file.
		if(tempName != null)
		{
			// 
			
			// Open it.
			if(workingFilePath != null || text.hasChanged || braille.hasChanged || documentName != null){
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
		arch = ArchiverFactory.getArchive(fileName);
		String archFileName = null;
		if(arch != null)
			archFileName = arch.open();
		
		// Potentially massaged archiver file, or just pass to BB.
		if(archFileName != null) {
			workingFilePath = archFileName;
			zippedPath = "";
		}
		else
			workingFilePath = fileName;
		
		////////////////////////
		// Zip and Recent Files.

			// If the file opened was an xml zip file, unzip it.
			if(fileName.endsWith(".zip") && archFileName == null) {
				// Create unzipper.
				Zipper unzipr = new Zipper();
				// Unzip and update "opened" file.
	//			workingFilePath = unzipr.Unzip(fileName, fileName.substring(0, fileName.lastIndexOf(".")) + BBIni.getFileSep());
				String sp = BBIni.getFileSep();
				String tempOutPath = BBIni.getTempFilesPath() + fileName.substring(fileName.lastIndexOf(sp), fileName.lastIndexOf(".")) + sp;
				workingFilePath = unzipr.Unzip(fileName, tempOutPath);
				// Store paths.
				zippedPath = fileName;
			}
			else {
				// There is no zip file to deal with.
				zippedPath = "";
			}
			
			////////////////
			// Recent Files.
			addRecentFileEntry(fileName);

		// Zip and Recent Files.
		////////////////////////	
			
		// Change current config based on file type.
		if(arch != null)
		{
			// Is this an epub document?
			if( arch.getOrigDocPath().endsWith(".epub") == true )
				currentConfig = getAutoCfg("epub");
		}
//		else if( workingFilePath.endsWith(".xml") )
//			currentConfig = getAutoCfg("nimas"); // Nimas document.
//		else if( workingFilePath.endsWith(".xhtml") )
//			currentConfig = getAutoCfg("epub");
			
		initializeAllViews(fileName, workingFilePath, null);
	}	
	
	private void initializeAllViews(String fileName, String filePath, String configSettings){
		try{
			if(document.startDocument(filePath, currentConfig, configSettings)){
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
				text.initializeListeners(this);
				braille.initializeListeners(this);
				treeView.initializeListeners(this);
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
				workingFilePath = null;
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
				if(current.getChild(i) instanceof Element && !((Element)current.getChild(i)).getLocalName().equals("pagenum")){
					Element currentChild = (Element)current.getChild(i);
					document.checkSemantics(currentChild);
					if(!currentChild.getLocalName().equals("meta") & !currentChild.getAttributeValue("semantics").contains("skip"))
						initializeViews(currentChild);
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
		
		if(message.getValue("sender").equals("text")){
			setUpdateCursorMessage(message, text.positionFromStart, text.cursorOffset);
			braille.updateCursorPosition(message);
		}
		else if(message.getValue("sender").equals("braille")) {
			setUpdateCursorMessage(message, braille.positionFromStart, braille.cursorOffset);
			text.updateCursorPosition(message);
		}
		else if(message.getValue("sender").equals("tree")){
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
			handleUpdateScrollbar(Message.createUpdateScollbarMessage("tree", list.getCurrent().start));
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
		list.checkList();
		if((Integer)message.getValue("deletionType") == SWT.BS){
			if(list.hasBraille(list.getCurrentIndex())){
				braille.removeWhitespace(list.getCurrent().brailleList.getFirst().start + (Integer)message.getValue("length"),  (Integer)message.getValue("length"), SWT.BS, this);
			}
			list.shiftOffsetsFromIndex(list.getCurrentIndex(), (Integer)message.getValue("length"), (Integer)message.getValue("length"));
		}
		else if((Integer)message.getValue("deletionType") == SWT.DEL){
			list.shiftOffsetsFromIndex(list.getCurrentIndex() + 1, (Integer)message.getValue("length"), (Integer)message.getValue("length"));
			if(list.hasBraille(list.getCurrentIndex())){
				braille.removeWhitespace(list.get(list.getCurrentIndex() + 1).brailleList.getFirst().start,  (Integer)message.getValue("length"), SWT.DEL, this);
			}
		}
	}
	
	private void handleUpdate(Message message){
		message.put("selection", this.treeView.getSelection(list.getCurrent()));
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
		
		text.clearRange(textStart, textEnd - textStart);
		braille.clearRange(brailleStart, brailleEnd - brailleStart);
		list.shiftOffsetsFromIndex(currentIndex, -(textEnd - textStart), -(brailleEnd - brailleStart));	
		
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

		list.shiftOffsetsFromIndex(currentIndex, list.get(currentIndex - 1).end - textStart, list.get(currentIndex - 1).brailleList.getLast().end - brailleStart);
		
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
		if(list.getCurrentIndex() > 0)
			document.insertEmptyTextNode(list, list.get(list.getCurrentIndex() - 1),  list.get(list.getCurrentIndex() - 1).end + 1, list.get(list.getCurrentIndex() - 1).brailleList.getLast().end + 1,list.getCurrentIndex());
		else
			document.insertEmptyTextNode(list, list.getCurrent(), list.getCurrent().start, list.getCurrent().brailleList.getFirst().start, list.getCurrentIndex());
			
		if(list.size() - 1 != list.getCurrentIndex() - 1){
			list.shiftOffsetsFromIndex(list.getCurrentIndex() + 1, 1, 1);
		}
		int index = treeView.getSelectionIndex();
		
		m.put("length", 1);
		m.put("newBrailleLength", 1);
		m.put("brailleLength", 0);

		if(list.getCurrentIndex()  > 0)
			braille.insertLineBreak(list.get(list.getCurrentIndex() - 1).brailleList.getLast().end);
		else
			braille.insertLineBreak(list.getCurrent().brailleList.getFirst().start - 1);
			
		treeView.newTreeItem(list.get(list.getCurrentIndex()), index, 0);
	}
	
	private void insertElementAtEnd(Message m){
		document.insertEmptyTextNode(list, list.getCurrent(), list.getCurrent().end + 1, list.getCurrent().brailleList.getLast().end + 1, list.getCurrentIndex() + 1);
		if(list.size() - 1 != list.getCurrentIndex() + 1){
			list.shiftOffsetsFromIndex(list.getCurrentIndex() + 2, 1, 1);
		}
		int index = treeView.getSelectionIndex();
		
		m.put("length", 1);
		m.put("newBrailleLength", 1);
		m.put("brailleLength", 0);

		braille.insertLineBreak(list.getCurrent().brailleList.getLast().end);
		treeView.newTreeItem(list.get(list.getCurrentIndex() + 1), index, 1);
	}
	
	public void insertTranscriberNote(){
		text.update(this, false);
			
		ArrayList<Integer>posList = list.findTextMapElementRange(list.getCurrentIndex(), (Element)list.getCurrent().n.getParent(), true);
			
		text.insertNewNode(this, list.get(posList.get(posList.size() - 1)).end);
			
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
		if(message.getValue("sender").equals("braille")){
			text.positionScrollbar(braille.view.getTopIndex());
		}
		else{
			braille.positionScrollbar(text.view.getTopIndex());
		}
	}
	
	private void handleUpdateStyle(Message message){
		if(document.getDOM() != null && text.view.getText().length() > 0){
			group.setRedraw(false);
			Element parent = document.getParent(list.getCurrent().n, true);
			message.put("previousStyle", styles.get(styles.getKeyFromAttribute(parent)));
			ArrayList<TextMapElement> itemList = list.findTextMapElements(list.getCurrentIndex(), parent, true);
		
			int start = list.getNodeIndex(itemList.get(0));
			int end = list.getNodeIndex(itemList.get(itemList.size() - 1));
			int currentIndex = list.getCurrentIndex();
			message.put("firstLine", text.view.getLineAtOffset(itemList.get(0).start));
			
			for(int i = start; i <= end; i++){
				list.setCurrent(i);
				list.getCurrentNodeData(message);
				text.adjustStyle(this, message, list.getCurrent().n);
				braille.adjustStyle(this, message, list.getCurrent());
				if(message.contains("linesBeforeOffset")){
					list.shiftOffsetsFromIndex(list.getCurrentIndex(), (Integer)message.getValue("linesBeforeOffset"), (Integer)message.getValue("linesBeforeOffset"));
					message.remove("linesBeforeOffset");
				}
		
				if(message.contains("linesAfterOffset")){
					list.shiftOffsetsFromIndex(list.getCurrentIndex() + 1, (Integer)message.getValue("linesAfterOffset"),  (Integer)message.getValue("linesAfterOffset"));
					message.remove("linesAfterOffset");
				}
			}
			list.setCurrent(currentIndex);
			document.changeSemanticAction(message, list.getCurrent().parentElement());
			treeView.adjustItemStyle(list.getCurrent());
			group.setRedraw(true);
		}
		else
			new Notify(lh.localValue("nothingToApply"));
	}
	
	public String getFileExt(String fileName) {
		String ext = "";
		String fn = fileName.toLowerCase();
		int dot = fn.lastIndexOf(".");
		if (dot > 0) {
			ext = fn.substring(dot + 1);
		}
		return ext;
	}
	
	public void saveAs(){
		String[] filterNames = new String[] {"XML", "EPUB", "BRF", "UTDML"};
		String[] filterExtensions = new String[] {".xml",".epub","*.brf", "*.utd"};
		BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.SAVE, filterNames, filterExtensions);
		String filePath = dialog.open();
		
		if(filePath != null){
			checkForUpdatedViews();
			String ext = getFileExt(filePath);
			
			if(ext.endsWith("epub")) { // Save archiver supported file.
				if(arch != null) {
					if(arch.getOrigDocPath().endsWith("epub")) {
						arch.save(document, filePath);
						setTabTitle(filePath);
						documentName = filePath;
					}
					else
						System.out.println("Can only save epub files as epub files... for now.");
				}
				else
					System.out.println("Can only save epub files as epub files... for now.");
			}
			else if(ext.equals("brf")){
				if(!this.document.createBrlFile(this, filePath)){
					new Notify("An error has occurred.  Please check your original document");
				}
			}
			else if(ext.equals("xml")){
			    if(fu.createXMLFile(document.getNewXML(), filePath)) {
			    	setTabTitle(filePath);
					documentName = filePath;
			    
			    	String tempSemFile; 			    
				    if(workingFilePath == null)
				    	tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName("outFile.utd") + ".sem";
				    else
				    	tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem";
				    
				    //String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(workingFilePath) + ".sem"; 
			    	String savedSemFile = fu.getPath(filePath) + BBIni.getFileSep() + fu.getFileName(filePath) + ".sem";   
			    
			    	//Save new semantic file to correct location and temp folder for further editing
			    	copySemanticsFile(tempSemFile, savedSemFile);
			    	copySemanticsFile(tempSemFile, BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(filePath) + ".sem");
			    	
					//update working file path to newly saved file
			    	workingFilePath = filePath;				    
			    }
			    else {
			    	new Notify("An error occured while saving your document.  Please check your original document.");
			    }
			}
			else if(ext.equals("utd")) {				
				document.setOriginalDocType(document.getDOM());
				if(fu.createXMLFile(document.getDOM(), filePath)){
					setTabTitle(filePath);
			    	documentName = filePath;
				    
				    String fileName;
			    	if(workingFilePath == null)
				    	fileName = "outFile";
				    else
				    	fileName = fu.getFileName(workingFilePath);
				    
				    String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem"; 
			    	String savedTempFile = fu.getPath(filePath) + BBIni.getFileSep() + fu.getFileName(filePath) + ".sem";
				    
			    	copySemanticsFile(tempSemFile, savedTempFile);
			    	copySemanticsFile(tempSemFile, BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(filePath) + ".sem");

			    	workingFilePath = filePath;
				}
				else {
			    	new Notify("An error occured while saving your document.  Please check your original document.");
			    }
			}
			
		    text.hasChanged = false;
			braille.hasChanged = false;	
			documentEdited = false;
		}
	}
	
	@Override
	public void close() {
		if (text.hasChanged || braille.hasChanged || documentEdited) {
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"));
			if (ync.result == SWT.YES) {
				this.fileSave();
			}
		}
		dispose();
		item.dispose();
		
		if(workingFilePath == null & docCount > 0)
			docCount--;
	}
	
	public void nextElement(){
		if(list.size() != 0){		
			if(text.view.isFocusControl()){
				text.increment(this);
				text.view.setCaretOffset(list.getCurrent().start);
			}
			else if(braille.view.isFocusControl()){
				braille.increment(this);
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
		if(list.size() != 0){
			if(text.view.isFocusControl()){
				text.decrement(this);
				text.view.setCaretOffset(list.getCurrent().start);
			}
			else if(braille.view.isFocusControl()){
				braille.decrement(this);
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
		if(this.document.createBrlFile(this, filePath)){
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
	
	private void setCurrentOnRefresh(String sender, int offset, boolean isBraille){
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
			
				setCurrentOnRefresh("text",currentOffset, false);
				text.setPositionFromStart();
				text.view.setFocus();
			}
			else if(braille.view.isFocusControl()){
				currentOffset = braille.view.getCaretOffset();
				resetViews();
			
				braille.view.setCaretOffset(currentOffset);
				setCurrentOnRefresh("braille",currentOffset, true);	
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
			if(workingFilePath == null)
				fileName = "outFile";
			else
				fileName = fu.getFileName(workingFilePath);
			
			if(fu.exists(BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem"))
				initializeAllViews(documentName, path, "semanticFiles " + document.getSemanticFileHandler().getDefaultSemanticsFiles() +"," + BBIni.getTempFilesPath() + BBIni.getFileSep() + fileName + ".sem\n");
			else
				initializeAllViews(documentName, path, null);
			
			f.delete();
	
			text.hasChanged = textChanged;
			braille.hasChanged = brailleChanged;
			
			if(workingFilePath == null && list.size() == 0){
				Nodes n = document.query("/*/*[2]/*[2]/*[1]/*[1]");
				((Element)n.get(0)).appendChild(new Text(""));
				list.add(new TextMapElement(0, 0, n.get(0).getChild(0)));
			}
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
			if(!sm.getConfigFile().equals(currentConfig)){
				sm.resetStylePanel(currentConfig);
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
		if(!styles.getConfig().equals(currentConfig))
			styles.resetStyleTable(currentConfig);	
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
				text.update(this, true);
				
				list.setCurrent(currentIndex);
				list.getCurrentNodeData(message);
				text.updateCursorPosition(message);
				braille.updateCursorPosition(message);
			}
		}
	}
	
	public void closeUntitledTab(){
		document.deleteDOM();
		if(!currentConfig.equals(BBIni.getDefaultConfigFile())){
			currentConfig = BBIni.getDefaultConfigFile();
			document.resetBBDocument(currentConfig);
			styles.resetStyleTable(currentConfig);
			sm.getStyleTable().resetTable(currentConfig);
		}
		treeView.removeListeners();
		treeView.clearTree();
		text.removeListeners();
		braille.removeListeners();
		list.clearList();	
	}
	
	private void startProgressBar(){
    	if(workingFilePath != null)
    		pb.start();
	}
 
	public BBProgressBar getProgressBar(){
		return pb;
	}
	
	//if tree has focus when opening a document and closing an untitled document, the trees selection must be reset
	public void checkTreeFocus(){
		if(treeView.getTree().isFocusControl() && treeView.getTree().getSelectionCount() == 0){
			treeView.setSelection(list.getFirst());
		}
	}
	
	public void setCurrentConfig(String config){
		if(workingFilePath != null)
			currentConfig = config;
	}
	
	public void checkForUpdatedViews(){
		if(text.hasChanged)
			text.update(this, false);
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
	
	public TextMapElement getClosest(int offset){
		Message m = new Message(null);
		m.put("offset", offset);
		TextMapElement t = list.get(list.findClosest(m, 0, list.size() - 1));
		
		if(offset >= t.start && offset <= t.end)
			return t;
		else
			return null;
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
	
	public void initiateSpellCheck(){
		if(text.view.getText().equals(""))
			new Notify(lh.localValue("noText"));
		else 
			new SpellCheckManager(this);
	}
	
	private BBTree loadTree(){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(BBIni.getUserSettings()));
			if(!properties.containsKey("tree")){
				properties.setProperty("tree", BookTree.class.getCanonicalName().toString());
				properties.store(new FileOutputStream(BBIni.getUserSettings()), null);
				return new BookTree(this, group);
			}
			else {
				Class<?> clss = Class.forName((String)properties.getProperty("tree"));
				Constructor<?> constructor = clss.getConstructor(Manager.class, Group.class);
				return (BBTree)constructor.newInstance(this, group);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
		
		return null;
	}
	
	public void swapTree(Class<?> clss){
		boolean focused = false;
		try {
			if(treeView.getTree().isFocusControl())
				focused = true;
			
			Constructor<?> constructor = clss.getConstructor(new Class[]{Manager.class, Group.class});
			treeView.removeListeners();
			treeView.dispose();
			treeView = (BBTree)constructor.newInstance(this, group);
			setTabList();
			treeView.setRoot(document.getRootElement());
			if(focused)
				treeView.getTree().setFocus();
			
			treeView.setSelection(list.getCurrent());
			treeView.getView().getParent().layout();
			treeView.initializeListeners(this);
			saveTree();
			
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	private void saveTree(){
		Properties prop = new Properties();
    	try {
			prop.load(new FileInputStream(BBIni.getUserSettings()));
			prop.setProperty("tree", treeView.getClass().getCanonicalName().toString());
			prop.store(new FileOutputStream(BBIni.getUserSettings()), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toggleBrailleFont(boolean showSimBraille){
		fontManager.toggleBrailleFont(showSimBraille);
		saveSimBrailleProperty(showSimBraille);
		setSimBrailleDisplayed(showSimBraille);
	}
	
	private void saveSimBrailleProperty(boolean showSimBraille){
		Properties prop = new Properties();
    	try {
			prop.load(new FileInputStream(BBIni.getUserSettings()));
			prop.setProperty("simBraille", String.valueOf(showSimBraille));
			prop.store(new FileOutputStream(BBIni.getUserSettings()), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean loadSimBrailleProperty(){
		Properties properties = new Properties();
		
		try {
			properties.load(new FileInputStream(BBIni.getUserSettings()));
			if(!properties.containsKey("simBraille")){
				properties.setProperty("simBraille", "true");	
				properties.store(new FileOutputStream(BBIni.getUserSettings()), null);
				return true;
			}
			else {
					return Boolean.valueOf((String)properties.get("simBraille"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
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
	
	@Override
	public String getWorkingPath(){
		return this.workingFilePath;
	}
	
	public BrailleDocument getDocument(){
		return document;
	}
	
	@Override
	public String getCurrentConfig(){
		return currentConfig;
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
		text.update(this, false);
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
}
