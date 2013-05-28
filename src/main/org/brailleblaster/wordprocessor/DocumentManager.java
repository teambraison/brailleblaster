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
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Serializer;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.mapping.MapList;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.printers.PrintPreview;
import org.brailleblaster.printers.PrintersManager;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.YesNoChoice;
import org.brailleblaster.util.Zipper;
import org.brailleblaster.views.BrailleView;
import org.brailleblaster.views.TextView;
import org.brailleblaster.views.TreeView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
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
		
		this.document = new BBDocument(this);
		FontManager.setShellFonts(this.wp.getShell(), this);
		
		this.tabList = new Control[]{this.treeView.view, this.text.view, this.braille.view};
		this.group.setTabList(this.tabList);
	//	this.wp.getStatusBar().setText("Words: " + 0);
		
		logger = BBIni.getLogger();
		
		docCount++;
		
		if(docName != null){
			openDocument(docName);
		}
		else{
			setTabTitle(docName);
		}				
	}

	public void fileSave(){
		
		// Borrowed from Save As function. Different document types require 
		// different save methods.
		try {
			if(workingFilePath.endsWith("xml")){
				Document newDoc = this.document.getNewXML();
				FileOutputStream os = new FileOutputStream(workingFilePath);
			    Serializer serializer = new Serializer(os, "UTF-8");
			    serializer.write(newDoc);
			    os.close();
			}
			else if(workingFilePath.endsWith("utd")) {				
				FileOutputStream os = new FileOutputStream(workingFilePath);
			    Serializer serializer = new Serializer(os, "UTF-8");
			    serializer.write(this.document.getDOM());
			    os.close();
			}
			else if(workingFilePath.endsWith("brf")){
				this.document.createBrlFile(workingFilePath);
			}
			
			// If the document came from a zip file, then rezip it.
			if(zippedPath.length() > 0)
			{
				// Create zipper.
				Zipper zpr = new Zipper();
				// Input string.
				String inPath = zippedPath.substring(0, zippedPath.lastIndexOf(".")) + "\\";
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
		System.out.println(fileName + " is opened here");
		
		// Update file we're about to work on.
		workingFilePath = fileName;
		
		////////////////////////
		// Zip and Recent Files.
		
			// If the file opened was an xml zip file, unzip it.
			if(fileName.endsWith(".zip")) {
				// Create unzipper.
				Zipper unzipr = new Zipper();
				// Unzip and update "opened" file.
				workingFilePath = unzipr.Unzip(fileName, fileName.substring(0, fileName.lastIndexOf(".")) + "\\");
				
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
						addNewDoc = false;
						break;
					}
				}
				
				// Update the recent files submenu.
				if(addNewDoc == true)
					this.wp.getMainMenu().addRecentEntry(fileName);
				
			// Recent Files.
			////////////////

		// Zip and Recent Files.
		////////////////////////
		
		try{
			if(this.document.startDocument(workingFilePath, "preferences.cfg", null)){
				this.wp.getStatusBar().resetLocation(6,100,100);
				this.wp.getStatusBar().setText("Loading...");
				this.wp.getProgressBar().start();
				this.documentName = fileName;
				setTabTitle(fileName);
				this.treeView.setRoot(this.document.getRootElement(), this);
				initializeViews(this.document.getRootElement());
				this.document.notifyUser();
				//list.getLast().brailleList.removeLast();
				this.text.view.replaceTextRange(this.text.view.getCharCount() - 1, 1, "");
				this.braille.view.replaceTextRange(this.braille.view.getCharCount() - 1, 1, "");
				this.text.initializeListeners(this);
				this.braille.initializeListeners(this);
				this.text.hasChanged = false;	
				this.braille.hasChanged = false;
				this.wp.checkToolbarSettings();
				this.wp.getStatusBar().resetLocation(0,100,100);
				this.wp.getProgressBar().stop();
				this.wp.getStatusBar().setText("Words: " + this.text.words);
				this.braille.setWords(this.text.words);
			}
			else {
				System.out.println("The Document Base document tree is empty");
				logger.log(Level.SEVERE, "The Document Base document tree is null, the file failed to parse properly");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	private void initializeViews(Node current){
		if(current instanceof Text && !((Element)current.getParent()).getLocalName().equals("brl")){
			this.text.setText(current, list);
		}
		
		for(int i = 0; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Element &&  ((Element)current.getChild(i)).getLocalName().equals("brl")){
				initializeBraille(current.getChild(i), list.getLast());
			}
			else {
				if(current.getChild(i) instanceof Element && !((Element)current.getChild(i)).getLocalName().equals("pagenum")){
					this.document.checkSemantics((Element)current.getChild(i));
					initializeViews(current.getChild(i));
				}
				else if(!(current.getChild(i) instanceof Element)) {
					initializeViews(current.getChild(i));
				}
			}
		}
	}
	
	private void initializeBraille(Node current, TextMapElement t){
		if(current instanceof Text && ((Element)current.getParent()).getLocalName().equals("brl")){
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
				if(list.size() > 0)
					this.treeView.setSelection(list.getCurrent(), message, this);
				resetCursorData();
				break;
			case DECREMENT:
				list.decrementCurrent(message);
				if(list.size() > 0)
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
					list.shiftOffsetsAfterIndex(list.getCurrentIndex(), (Integer)message.getValue("length"));
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
					this.document.createBrlFile(filePath);
				}
				else if(ext.equals("xml")){
					Document newDoc = this.document.getNewXML();
					FileOutputStream os = new FileOutputStream(filePath);
				    Serializer serializer = new Serializer(os, "UTF-8");
				    serializer.write(newDoc);
				    os.close();
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
		if(list.size() != 0 ){
			Message message = new Message(BBEvent.INCREMENT);
			dispatch(message);	
		}
		
		if(text.view.isFocusControl()){
			text.view.setCaretOffset(list.getCurrent().start);
		}
		else if(braille.view.isFocusControl()){
			braille.view.setCaretOffset(list.getCurrent().brailleList.getFirst().start);
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
	
	public void printPreview(){
		if(this.braille.view.getCharCount() > 0){
			PrintPreview pv = new PrintPreview(this.getDisplay(), this.document);
		}
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
	
	private void checkSemantics(Element e){
		if(!e.getLocalName().equals("meta") && e.getAttributeValue("semantics") == null){
			Notify errorMessage = new Notify("No semantic attribute exists for element \"" + e.getLocalName() + "\". Please consider editing the configuration files.");
			Attribute attr = new Attribute("semantics", "style,para");
			e.addAttribute(attr);
		}
	}
}
