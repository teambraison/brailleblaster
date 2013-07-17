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

package org.brailleblaster.wordprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Serializer;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.document.BBSemanticsTable;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.mapping.MapList;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.messages.BBEvent;
import org.brailleblaster.messages.Message;
import org.brailleblaster.printers.PrintPreview;
import org.brailleblaster.printers.PrintersManager;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.YesNoChoice;
import org.brailleblaster.util.Zipper;
import org.brailleblaster.views.BrailleView;
import org.brailleblaster.views.TextView;
import org.brailleblaster.views.TreeView;
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
public class DocumentManager {
	WPManager wp;
	TabItem item;
	Group group;
	TreeView treeView;
	TextView text;
	BrailleView braille;
	FormLayout layout;
	Control [] tabList;
	BBSemanticsTable styles;
	static int docCount = 0;
	String documentName = null;
	boolean metaContent = false;
	String logFile = "Translate.log";
	String configSettings = null;
	static String recentFileName = null;
	LocaleHandler lh = new LocaleHandler();
	static Logger logger;
	public BBDocument document;
	boolean simBrailleDisplayed = false;
	MapList list;
	String zippedPath;
	String workingFilePath;
	
	//Constructor that sets things up for a new document.
	DocumentManager(WPManager wp, String docName) {
		this.styles = new BBSemanticsTable();
		this.documentName = docName;
		this.list = new MapList(this);
		this.wp = wp;
		this.item = new TabItem(wp.getFolder(), 0);
		this.group = new Group(wp.getFolder(),SWT.NONE);
		this.group.setLayout(new FormLayout());		
		this.treeView = new TreeView(this, this.group);
		this.text = new TextView(this.group, this.styles);
		this.braille = new BrailleView(this.group, this.styles);
		this.item.setControl(this.group);
		initializeDocumentTab();
		this.document = new BBDocument(this);
		
		logger = BBIni.getLogger();
		
		docCount++;
		
		if(docName != null){
			openDocument(docName);
		}
		else{
			setTabTitle(docName);
		}				
	}

	private void initializeDocumentTab(){
		FontManager.setShellFonts(this.wp.getShell(), this);	
		this.tabList = new Control[]{this.treeView.view, this.text.view, this.braille.view};
		this.group.setTabList(this.tabList);
		wp.getShell().layout();
	}
	
	public void fileSave(){	
		// Borrowed from Save As function. Different document types require 
		// different save methods.
		try {
			if(workingFilePath.endsWith("xml")){
			    createXMLFile(workingFilePath);
			}
			else if(workingFilePath.endsWith("utd")) {				
				FileOutputStream os = new FileOutputStream(workingFilePath);
			    Serializer serializer = new Serializer(os, "UTF-8");
			    serializer.write(this.document.getDOM());
			    os.close();
			}
			else if(workingFilePath.endsWith("brf")){
				if(!this.document.createBrlFile(workingFilePath)){
					new Notify("An error has occurred.  Please check your original document");
				}
			}
			
			// If the document came from a zip file, then rezip it.
			if(zippedPath.length() > 0)
			{
				// Create zipper.
				Zipper zpr = new Zipper();
				// Input string.
				String sp = BBIni.getFileSep();
				String inPath = BBIni.getTempFilesPath() + zippedPath.substring(zippedPath.lastIndexOf(sp), zippedPath.lastIndexOf(".")) + sp;
//				String inPath = zippedPath.substring(0, zippedPath.lastIndexOf(".")) + BBIni.getFileSep();
				// Zip it!
				zpr.Zip(inPath, zippedPath);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void fileOpenDialog() {
		String tempName;

		String[] filterNames = new String[] { "XML", "XML ZIP", "TEXT", "BRF", "UTDML working document", };
		String[] filterExtensions = new String[] { "*.xml", "*.zip", "*.txt", "*.brf", "*.utd", };
		BBFileDialog dialog = new BBFileDialog(this.wp.getShell(), SWT.OPEN, filterNames, filterExtensions);
		
		tempName = dialog.open();
		
		// Don't do any of this if the user failed to choose a file.
		if(tempName != null)
		{
			// Open it.
			if(this.document.getDOM() != null || this.text.hasChanged || this.braille.hasChanged || this.documentName != null){
				this.wp.addDocumentManager(tempName);
			}
			else {
				openDocument(tempName);
			}
			
		} // if(tempName != null)
	}
	
	public void openDocument(String fileName){	
		// Update file we're about to work on.
		workingFilePath = fileName;
		////////////////////////
		// Zip and Recent Files.
		
			// If the file opened was an xml zip file, unzip it.
			if(fileName.endsWith(".zip")) {
				// Create unzipper.
				Zipper unzipr = new Zipper();
				// Unzip and update "opened" file.
//				workingFilePath = unzipr.Unzip(fileName, fileName.substring(0, fileName.lastIndexOf(".")) + BBIni.getFileSep());
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
				
				// Get recent file list.
				ArrayList<String> strs = this.wp.getMainMenu().getRecentDocumentsList();
				
				// Search list for duplicate. If one exists, don't add this new one.
				boolean addNewDoc = true;
				for(int curStr = 0; curStr < strs.size(); curStr++) {
					if(strs.get(curStr).compareTo(fileName) == 0) {
						
						// This isn't a new document. First, remove from doc list and recent item submenu.
						wp.getMainMenu().getRecentDocumentsList().remove(curStr);
						wp.getMainMenu().getRecentItemSubMenu().getItem(curStr).dispose();
						
						// We found a duplicate, so there is no point in going further.
						break;
						
					} // if(strs.get(curStr)...
					
				} // for(int curStr = 0...
				
				// Add to top of recent items submenu.
				wp.getMainMenu().addRecentEntry(fileName);
				
			// Recent Files.
			////////////////

		// Zip and Recent Files.
		////////////////////////
		
		initializeAllViews(fileName, workingFilePath);
	}	
	
	private void initializeAllViews(String fileName, String filePath){
		try{
			if(this.document.startDocument(filePath, BBIni.getDefaultConfigFile(), null)){
				this.group.setRedraw(false);
				this.text.view.setWordWrap(false);
				this.braille.view.setWordWrap(false);
				this.wp.getStatusBar().resetLocation(6,100,100);
				this.wp.getStatusBar().setText("Loading...");
				this.wp.getProgressBar().start();
				this.documentName = fileName;
				setTabTitle(fileName);
				this.treeView.setRoot(this.document.getRootElement(), this);
				initializeViews(this.document.getRootElement());
				this.document.notifyUser();
				this.text.initializeListeners(this);
				this.braille.initializeListeners(this);
				this.treeView.initializeListeners(this);
				this.text.hasChanged = false;
				this.braille.hasChanged = false;
				this.wp.checkToolbarSettings();
				this.wp.getStatusBar().resetLocation(0,100,100);
				this.wp.getProgressBar().stop();
				this.wp.getStatusBar().setText("Words: " + this.text.words);
				this.braille.setWords(this.text.words);
				this.text.view.setWordWrap(true);
				this.braille.view.setWordWrap(true);
				this.group.setRedraw(true);
			}
			else {
				System.out.println("The Document Base document tree is empty");
				logger.log(Level.SEVERE, "The Document Base document tree is null, the file failed to parse properly");
			}
		}
		catch(Exception e){
			e.printStackTrace();
			logger.log(Level.SEVERE, "Unforeseen Exception", e);
		}
	}
	
	private void initializeViews(Node current){
		if(current instanceof Text && !((Element)current.getParent()).getLocalName().equals("brl") && vaildTextElement(current.getValue())){
			this.text.setText(current, list);
		}
		
		for(int i = 0; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Element &&  ((Element)current.getChild(i)).getLocalName().equals("brl")){
				initializeBraille(current.getChild(i), list.getLast());
			}
			else {
				if(current.getChild(i) instanceof Element && !((Element)current.getChild(i)).getLocalName().equals("pagenum")){
					Element currentChild = (Element)current.getChild(i);
					this.document.checkSemantics(currentChild);
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
			if(!(grandParent.getLocalName().equals("span") && this.document.checkAttributeValue(grandParent, "class", "brlonly")))
				this.braille.setBraille(current, t);
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
	
	public void dispatch(Message message){
		int index;
		
		switch(message.type){
			case INCREMENT:
				list.incrementCurrent(message);
				this.treeView.setSelection(list.getCurrent(), message, this);
				resetCursorData();
				break;
			case DECREMENT:
				list.decrementCurrent(message);
				this.treeView.setSelection(list.getCurrent(), message, this);
				resetCursorData();
				break;
			case UPDATE_CURSORS:
				message.put("element", list.getCurrent().n);
				if(message.getValue("sender").equals("text")){
					message.put("lastPosition", this.text.positionFromStart);
					message.put("offset", this.text.cursorOffset);
					list.getCurrentNodeData(message);
					this.braille.updateCursorPosition(message);
				}
				else if(message.getValue("sender").equals("braille")) {
					message.put("lastPosition", this.braille.positionFromStart);
					message.put("offset", this.braille.cursorOffset);
					list.getCurrentNodeData(message);
					this.text.updateCursorPosition(message);
				}
				else if(message.getValue("sender").equals("tree")){
					message.put("lastPosition", this.text.positionFromStart);
					message.put("offset", this.text.cursorOffset);
					list.getCurrentNodeData(message);
					this.braille.updateCursorPosition(message);
					message.put("lastPosition", this.braille.positionFromStart);
					message.put("offset", this.braille.cursorOffset);
					this.text.updateCursorPosition(message);
				}
				break;
			case SET_CURRENT:
				list.checkList();
				if(message.contains("isBraille")){
					index = list.findClosestBraille(message);
					list.setCurrent(index);
					list.getCurrentNodeData(message);
					this.treeView.setSelection(list.getCurrent(), message, this);
				}
				else {
					message.put("selection", this.treeView.getSelection(list.getCurrent()));
					index = list.findClosest(message, 0, list.size() - 1);
					if(index == -1){
						list.getCurrentNodeData(message);
						this.treeView.setSelection(list.getCurrent(), message, this);
					}
					else {
						list.setCurrent(index);
						list.getCurrentNodeData(message);
						this.treeView.setSelection(list.getCurrent(), message, this);
					}
					resetCursorData();
				}
				break;
			case GET_CURRENT:
				message.put("selection", this.treeView.getSelection(list.getCurrent()));
				list.getCurrentNodeData(message);
				if(list.size() > 0)
					this.treeView.setSelection(list.getCurrent(), message, this);
				break;
			case TEXT_DELETION:
				list.checkList();
				if((Integer)message.getValue("deletionType") == SWT.BS){
					if(list.hasBraille(list.getCurrentIndex())){
						this.braille.removeWhitespace(list.getCurrent().brailleList.getFirst().start + (Integer)message.getValue("length"),  (Integer)message.getValue("length"), SWT.BS, this);
					}
					list.shiftOffsetsFromIndex(list.getCurrentIndex(), (Integer)message.getValue("length"));
				}
				else if((Integer)message.getValue("deletionType") == SWT.DEL){
					list.shiftOffsetsFromIndex(list.getCurrentIndex() + 1, (Integer)message.getValue("length"));
					if(list.hasBraille(list.getCurrentIndex())){
						this.braille.removeWhitespace(list.get(list.getCurrentIndex() + 1).brailleList.getFirst().start,  (Integer)message.getValue("length"), SWT.DEL, this);
					}
				}
				break;
			case UPDATE:
				message.put("selection", this.treeView.getSelection(list.getCurrent()));
				this.document.updateDOM(list, message);
				this.braille.updateBraille(list.getCurrent(), message);
				this.text.reformatText(list.getCurrent().n, message, this);
				list.updateOffsets(list.getCurrentIndex(), message);
				list.checkList();
				break;
			case REMOVE_NODE:
				index = (Integer)message.getValue("index");
				this.document.updateDOM(list, message);
				list.get(index).brailleList.clear();
				this.treeView.removeItem(list.get(index), message);
				list.remove(index);
				System.out.println("Item removed");				
				if(list.size() == 0)
					this.text.removeListeners();
				break;
			case UPDATE_STATUSBAR:
				this.braille.setWords(this.text.words);
				this.wp.getStatusBar().setText((String)message.getValue("line"));
				break;
			case ADJUST_ALIGNMENT:
				this.braille.changeAlignment(list.getCurrent().brailleList.getFirst().start, (Integer)message.getValue("alignment"));
				break;
			case ADJUST_INDENT:
				this.braille.changeIndent(list.getCurrent().brailleList.getFirst().start, message);
				break;
			case ADJUST_RANGE:
				list.adjustOffsets(list.getCurrentIndex(), message);
				break;
			case GET_TEXT_MAP_ELEMENTS:
				list.findTextMapElements(message);
				break;
			case UPDATE_SCROLLBAR:
				if(message.contains("sender")){
					index = list.findClosestBraille(message);
					this.text.positionScrollbar(this.braille.view.getTopIndex());
				}
				else{
					index = list.findClosest(message, 0, list.size() - 1);
					this.braille.positionScrollbar(this.text.view.getTopIndex());
				}
				break;
			default:
				break;
		}
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
		String[] filterNames = new String[] {"XML", "BRF", "UTDML"};
		String[] filterExtensions = new String[] {".xml","*.brf", "*.utd"};
		BBFileDialog dialog = new BBFileDialog(this.wp.getShell(), SWT.SAVE, filterNames, filterExtensions);
		String filePath = dialog.open();
		if(filePath != null){
			String ext = getFileExt(filePath);
			try {
				if(ext.equals("brf")){
					if(!this.document.createBrlFile(filePath)){
						new Notify("An error has occurred.  Please check your original document");
					}
				}
				else if(ext.equals("xml")){
				    createXMLFile(filePath);
				    setTabTitle(filePath);
				    this.documentName = filePath;
				}
				else if(ext.equals("utd")) {				
					FileOutputStream os = new FileOutputStream(filePath);
				    Serializer serializer = new Serializer(os, "UTF-8");
				    serializer.write(this.document.getDOM());
				    os.close();
				    setTabTitle(filePath);
				    this.documentName = filePath;
				}
			}
			catch (IOException e) {
				e.printStackTrace(); 
				logger.log(Level.SEVERE, "IO Exception", e);
			} 
		}
	}
	
	public void fileClose() {
		if (this.text.hasChanged || this.braille.hasChanged) {
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"));
			if (ync.result == SWT.YES) {
				this.fileSave();
			}
		}
		this.item.dispose();
	}
	
	private void setTabTitle(String pathName) {
		if(pathName != null){
			int index = pathName.lastIndexOf(File.separatorChar);
			if (index == -1) {
				this.item.setText(pathName);
			} 
			else {
				this.item.setText(pathName.substring(index + 1));
			}
		}
		else {
			if(docCount == 1){
				this.item.setText("Untitled");			
			}
			else {
				this.item.setText("Untitled #" + docCount);
			}
		}
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
				Message message = new Message(BBEvent.INCREMENT);
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
				Message message = new Message(BBEvent.DECREMENT);
				dispatch(message);
				text.view.setCaretOffset(list.getCurrent().start);
				braille.view.setCaretOffset(list.getCurrent().brailleList.getFirst().start);
			}
		}
	}
	
	private void resetCursorData(){
		this.text.positionFromStart = 0;
		this.text.cursorOffset = 0;
		this.braille.positionFromStart = 0;
		this.braille.cursorOffset = 0;
	}
	
	public void textPrint(){
		PrintersManager pn = new PrintersManager(this.wp.getShell(), this.text.view);
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
		if(this.braille.view.getCharCount() > 0){
			new PrintPreview(this.getDisplay(), this.document);
		}
	}
	
	private void setCurrentOnRefresh(String sender, int offset){
		Message m = new Message(BBEvent.SET_CURRENT);
		if(sender != null){
			m.put("sender", sender);
		}
		m.put("offset", offset);
		dispatch(m);
	}
	
	public void refresh(){	
		int currentOffset;
		if(this.document.getDOM() != null){
			if(this.text.view.isFocusControl()){
				currentOffset = this.text.view.getCaretOffset();
				resetViews();
				initializeDocumentTab();
				
				if(currentOffset < this.text.view.getCharCount()){
					this.text.view.setCaretOffset(currentOffset);
				}
				else
					this.text.view.setCaretOffset(0);
			
				setCurrentOnRefresh("text",currentOffset);
				this.text.setPositionFromStart();
				this.text.view.setFocus();
			}
			else if(this.braille.view.isFocusControl()){
				currentOffset = this.braille.view.getCaretOffset();
				resetViews();
				initializeDocumentTab();
			
				this.braille.view.setCaretOffset(currentOffset);
				setCurrentOnRefresh("braille",currentOffset);	
				this.braille.setPositionFromStart();
				this.braille.view.setFocus();
			}
			else if(this.treeView.tree.isFocusControl()){	
				if(this.text.view.getCaretOffset() > 0)
					currentOffset = this.text.view.getCaretOffset();
				else
					currentOffset = list.getCurrent().start;
			
				resetViews();
				initializeDocumentTab();

				setCurrentOnRefresh(null, currentOffset);
				this.text.view.setCaretOffset(currentOffset);
				this.text.setPositionFromStart();
			}
			else {
				currentOffset = this.text.view.getCaretOffset();
			
				resetViews();		
				initializeDocumentTab();
			
				setCurrentOnRefresh(null,currentOffset);
				this.text.view.setCaretOffset(currentOffset);
				this.text.setPositionFromStart();
			}
		}
	}
	
	private void resetViews(){
		try {
			String path = BBIni.getTempFilesPath() + BBIni.getFileSep() + "temp.xml";
			File f = new File(path);
			f.createNewFile();
			createXMLFile(path);
			list.clear();
			this.text.removeListeners();
			this.text.resetView(this.group);
			this.braille.removeListeners();
			this.braille.resetView(this.group);
			this.treeView.removeListeners();
			this.treeView.resetView(this.group);
			this.text.words = 0;
			updateTempFile();
			this.document.deleteDOM();
			initializeAllViews(this.documentName, path);
			f.delete();
			
		} catch (IOException e) {
			new Notify("An error occurred while refreshing the document. Please save your work and try again.");
			e.printStackTrace();
			logger.log(Level.SEVERE, "IO Exception", e);
		}
	}
	
	private boolean createXMLFile(String path){
		try {
			Document newDoc = this.document.getNewXML();
			FileOutputStream os;
			os = new FileOutputStream(path);
			Serializer serializer;	
			serializer = new Serializer(os, "UTF-8");
			serializer.write(newDoc);
			os.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "File Not Found Exception", e);
			return false;
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Unsupported Encoding Exception", e);
			return false;
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "IO Exception", e);
			return false;
		}
		return true;
	}
	
	private void updateTempFile(){
		try {
			String tempFile = this.document.getOutfile();
			FileOutputStream os = new FileOutputStream(tempFile);
	    	Serializer serializer = new Serializer(os, "UTF-8");
	    	serializer.write(this.document.getDOM());
	    	os.close();
		}
		catch(Exception e){
			e.printStackTrace();
			logger.log(Level.SEVERE, "Exception", e);
		}
	}
	
	private boolean vaildTextElement(String text){
		int length = text.length();
		
		for(int i = 0; i < length; i++){
			if(text.charAt(i) != '\n' && text.charAt(i) != '\t')
				return true;
		}
		
		return false;
	}
	
	public void toggleBrailleFont(){
		FontManager.toggleBrailleFont(this.wp, this);
	}
		
	public StyledText getDaisyView(){
		return this.text.view;
	}
	
	public StyledText getBrailleView(){
		return this.braille.view;
	}
	
	public Display getDisplay(){
		return this.wp.getShell().getDisplay();
	}
}
