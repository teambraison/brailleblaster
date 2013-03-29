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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Serializer;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.mapping.MapList;
import org.brailleblaster.mapping.TextMapElement;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TreeItem;

//This class manages each document in an MDI environment. It controls the braille View and the daisy View.
public class DocumentManager {
	WPManager wp;
	TabItem item;
	Group group;
	BBStatusBar statusBar;
	TreeView treeView;
	TextView text;
	BrailleView braille;
	FormLayout layout;
	Control [] tabList;
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
	
	//Constructor that sets things up for a new document.
	DocumentManager(WPManager wp, String docName) {
		this.documentName = docName;
		this.list = new MapList(this);
		this.wp = wp;
		this.item = new TabItem(wp.getFolder(), 0);
		this.group = new Group(wp.getFolder(),SWT.NONE);
		this.group.setLayout(new FormLayout());		
		this.treeView = new TreeView(this, this.group);
		this.text = new TextView(this.group);
		this.braille = new BrailleView(this.group);
		this.item.setControl(this.group);
		
		this.document = new BBDocument(this);
		FontManager.setShellFonts(this.wp.getShell(), this);
		
		this.tabList = new Control[]{this.treeView.view, this.text.view, this.braille.view};
		this.group.setTabList(this.tabList);
		
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
		System.out.println("File save occurs");
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
		
		////////////////////////
		// Zip and Recent Files.
		
			// The data file.
			String dataFilePath = fileName;
		
			// If the file opened was an xml zip file, unzip it.
			if(fileName.endsWith(".zip")) {
				// Create unzipper.
				Zipper unzipr = new Zipper();
				// Unzip and update "opened" file.
				dataFilePath = unzipr.Unzip(fileName, fileName.substring(0, fileName.lastIndexOf(".")) + "\\");
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
			if(this.document.startDocument(dataFilePath, "preferences.cfg", null)){
				this.documentName = fileName;
				setTabTitle(fileName);
				this.treeView.setRoot(this.document.getRootElement());
				initializeViews(this.document.getRootElement(), this.treeView.getRoot());				
				list.getLast().brailleList.removeLast();
				this.text.initializeListeners(this);
				this.braille.initializeListeners(this);
				this.text.view.replaceTextRange(this.text.view.getCharCount() - 1, 1, "");
				this.text.hasChanged = false;	
				this.braille.hasChanged = false;
				this.wp.checkToolbarSettings();
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
	
	private void initializeViews(Node current, TreeItem item){
		if(current instanceof Text && !((Element)current.getParent()).getLocalName().equals("brl")){
			this.text.setText(current, list);
			if(item.getData() == null)
				item.setData(list.getLast());
		}
		
		for(int i = 0; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Element &&  ((Element)current.getChild(i)).getLocalName().equals("brl")){
				TreeItem temp = this.treeView.newTreeItem(current.getChild(i), item);
				initializeBraille(current.getChild(i), temp, list.getLast());
			}
			else {
				if(current.getChild(i) instanceof Element){
					TreeItem temp = this.treeView.newTreeItem(current.getChild(i), item);
					initializeViews(current.getChild(i), temp);
				}
				else {
					initializeViews(current.getChild(i), item);
				}
			}
		}
	}
	
	private void initializeBraille(Node current, TreeItem item, TextMapElement t){
		if(current instanceof Text && ((Element)current.getParent()).getLocalName().equals("brl")){
			this.braille.setBraille(current, t);
		}
		
		for(int i = 0; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Element){
				TreeItem temp = this.treeView.newTreeItem(current.getChild(i), item);
				initializeBraille(current.getChild(i), temp, t);
			}
			else {
				initializeBraille(current.getChild(i), item, t);
			}
		}
	}
	
	public void dispatch(Message message){
		int index;
		
		switch(message.type){
			case SET_CURRENT:
				list.checkList();
				message.put("selection", this.treeView.getSelection());
				index = list.findClosest(message);
				if(index == -1){
					message.put("start", list.getCurrent().offset);
					message.put("end", list.getCurrent().offset + list.getCurrent().n.getValue().length());
					this.treeView.setSelection(list.getCurrent(), message);
				}
				else {
					list.setCurrent(index);
					list.getCurrentNodeData(message);
					this.treeView.setSelection(list.getCurrent(), message);
				}
				break;
			case GET_CURRENT:
				list.getCurrentNodeData(message);
				this.treeView.setSelection(list.getCurrent(), message);
				break;
			case TEXT_DELETION:
				index = list.findClosest(message);
				break;
			case UPDATE:
				message.put("selection", this.treeView.getSelection());
				this.document.updateDOM(list, message);
				list.updateOffsets(list.getCurrentIndex(), message);
				this.braille.updateBraille(list.getCurrent(), (Integer)message.getValue("brailleLength"));
				list.checkList();
				break;
			case REMOVE_NODE:
				index = (Integer)message.getValue("index");
				this.document.updateDOM(list, message);
				if(list.get(index).brailleList.getFirst().offset != -1){
					int carriageReturnLineFeed = 2;
					this.braille.removeWhitespace(list.get(index).brailleList.getFirst().offset);
					message.put("brailleLength", carriageReturnLineFeed);
					list.updateOffsets(index, message);
				}
				list.get(index).brailleList.clear();
				this.treeView.removeItem(list.get(index), message);
				list.remove(index);
				System.out.println("Item removed");
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
		String[] filterNames = new String[] {"BRF", "UTDML"};
		String[] filterExtensions = new String[] {"*.brf", "*.utd"};
		BBFileDialog dialog = new BBFileDialog(this.wp.getShell(), SWT.SAVE, filterNames, filterExtensions);
		String filePath = dialog.open();
		if(filePath != null){
			String ext = getFileExt(filePath);
		
			if(ext.equals("brf")){
				try {
					String text = this.braille.view.getTextRange(0, this.braille.view.getCharCount());
					File f = new File(filePath);
					FileWriter fw;
					fw = new FileWriter(f);
					BufferedWriter writer = new BufferedWriter(fw);
					writer.write(text);
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(ext.equals("xml")){
				SaveOptionsDialog saveDialog = new SaveOptionsDialog(this.wp.getShell(), SWT.NONE);
				if(saveDialog.open() == SaveSelection.TEXT_AND_BRAILLE){
					
				}
				else if(saveDialog.open() == SaveSelection.TEXT_ONLY){
					
				}
			}
			else if(ext.equals("utd")) {				
				try {
					FileOutputStream os = new FileOutputStream(filePath);
				    Serializer serializer = new Serializer(os, "UTF-8");
				    serializer.write(this.document.getDOM());
				    os.close();
				    setTabTitle(filePath);
				    this.documentName = this.item.getText();
				}
				catch (IOException e) {
					e.printStackTrace(); 
				}  
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
