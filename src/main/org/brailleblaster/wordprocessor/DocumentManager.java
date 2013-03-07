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
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;


import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.DocumentBase;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.YesNoChoice;
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
import org.liblouis.liblouisutdml;

//This class manages each document in an MDI environment. It controls the braille View and the daisy View.
public class DocumentManager {
	WPManager wp;
	TabItem item;
	Group group;
	BBStatusBar statusBar;
	TreeView treeView;
	TextView daisy;
	BrailleView braille;
	FormLayout layout;
	DocumentBase db;
	Control [] tabList;
	static int docCount = 0;
	String documentName = null;
	boolean documentChanged = false;
	RecentDocuments rd;
	UTD utd;
	NewDocument newDoc;
	String configFileList = null;
	static String tempPath = null;
	boolean haveOpenedFile = false;
	String translatedFileName = null;
	String brailleFileName = null; // FO
	String daisyWorkFile = null; // FO
	String tikaWorkFile = null; // FO
	String newDaisyFile = "utdml-doc"; // FO
	boolean textAndBraille = false;
	boolean saveUtdml = false;
	boolean metaContent = false;
	liblouisutdml louisutdml;
	String logFile = "Translate.log";
	String configSettings = null;
	static String recentFileName = null;
	static int recentFileNameIndex = -1;
	LocaleHandler lh = new LocaleHandler();
	static Logger logger;
	LinkedList<TextMapElement> list = new LinkedList<TextMapElement>();
	int brailleTotal = 0;
	int textTotal = 0;
	
	//Constructor that sets things up for a new document.
	DocumentManager(WPManager wp, String docName) {
		this.documentName = docName;
		this.wp = wp;
		this.db = new DocumentBase();
		this.item = new TabItem(wp.getFolder(), 0);
		this.group = new Group(wp.getFolder(),SWT.NONE);
		this.group.setLayout(new FormLayout());
		
		this.treeView = new TreeView(this, this.group);
		this.daisy = new TextView(this.group);
		this.braille = new BrailleView(this.group);
		this.item.setControl(this.group);
		
		tempPath = BBIni.getTempFilesPath() + BBIni.getFileSep();
		louisutdml = liblouisutdml.getInstance();
		
		FontManager.setShellFonts(this.wp.getShell(), this);
		
		this.tabList = new Control[]{this.treeView.view, this.daisy.view, this.braille.view};
		this.group.setTabList(this.tabList);
		
		rd = new RecentDocuments(this);
		utd = new UTD(this);
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
	
	public void fileOpenDialog(WPManager wp) {
		String tempName;

		FileDialog dialog = new FileDialog(this.wp.getShell(), SWT.OPEN);
		String filterPath = "/";
		String[] filterNames = new String[] { "XML", "TEXT", "BRF", "UTDML working document", };
		String[] filterExtensions = new String[] { "*.xml", "*.txt", "*.brf", "*.utd", };

		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterPath = System.getProperty("user.home");
			if (filterPath == null){
				filterPath = "c:\\";
			}
		}
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFilterPath(filterPath);

		tempName = dialog.open();
		
		if(tempName != null){
			if(this.db.getDocumentTree() != null || this.daisy.hasChanged || this.braille.hasChanged || this.documentName != null){
				wp.addDocumentManager(tempName);
			}
			else {
				openDocument(tempName);
			}
		}
	}
	
	public void openDocument(String fileName){
		System.out.println(fileName + " is opened here");
		try{
			if(this.db.startDocument(fileName, "preferences.cfg", null)){
				setTabTitle(fileName);
				this.treeView.setRoot(this.db.getDocumentTree().getRootElement());
				//setAttributes(this.db.getDocumentTree().getRootElement());
				setViews(this.db.getDocumentTree().getRootElement(), this.treeView.getRoot());			
				this.daisy.hasChanged = false;	
				this.braille.hasChanged = false;		
				//list.getLast().list.removeLast();
				//this.daisy.addListeners(this);	
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
	
	private void setViews(Node current, TreeItem item){
		if(current instanceof Text && !((Element)current.getParent()).getLocalName().equals("brl")){
			this.daisy.view.append(current.getValue() + "\n");
			list.add(new TextMapElement(textTotal, current, item));
			item.setData(list.getLast());
			textTotal += current.getValue().length() + 1;
		}
		
		for(int i = 0; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Element &&  ((Element)current.getChild(i)).getLocalName().equals("brl")){
				TreeItem temp = new TreeItem(item, 0);
				temp.setText(((Element)current.getChild(i)).getLocalName());
				setBraille(current.getChild(i), temp, list.getLast());
			}
			else {
				if(current.getChild(i) instanceof Element){
					TreeItem temp = new TreeItem(item, 0);
					temp.setText(((Element)current.getChild(i)).getLocalName());
					setViews(current.getChild(i), temp);
				}
				else {
					setViews(current.getChild(i), item);
				}
			}
		}
	}
	
	private void setBraille(Node current, TreeItem item, TextMapElement t){
		if(current instanceof Text && ((Element)current.getParent()).getLocalName().equals("brl")){
			this.braille.view.append(current.getValue() + "\n");
			t.list.add(new BrailleMapElement(brailleTotal, current));
			brailleTotal += current.getValue().length() + 1;
		}
		
		for(int i = 0; i < current.getChildCount(); i++){
			if(current.getChild(i) instanceof Element){
				TreeItem temp = new TreeItem(item, 0);
				temp.setText(((Element)current.getChild(i)).getLocalName());
				setBraille(current.getChild(i), temp, t);
			}
			else {
				setBraille(current.getChild(i), item, t);
			}
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
	
	public void fileClose() {
		if (this.daisy.hasChanged || this.braille.hasChanged) {
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"));
			if (ync.result == SWT.YES) {
				this.fileSave();
			}
		}

		haveOpenedFile = false;
		brailleFileName = null;
		documentName = null;
	
		this.daisy.hasChanged = false;
		this.braille.hasChanged = false;
		
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
	
	public void changeFocus(){
		TreeItem[] temp = this.treeView.tree.getSelection();
		TextMapElement me = (TextMapElement)temp[0].getData();
		if(me != null){
			this.daisy.view.setFocus();
			this.daisy.view.setCaretOffset(me.offset);
		}
	}
	
	public StyledText getDaisyView(){
		return this.daisy.view;
	}
	
	public StyledText getBrailleView(){
		return this.braille.view;
	}
	
	public Display getDisplay(){
		return this.wp.getShell().getDisplay();
	}
}
