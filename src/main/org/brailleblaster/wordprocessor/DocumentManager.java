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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParsingException;
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
	TextView text;
	BrailleView braille;
	FormLayout layout;
	public DocumentBase db;
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
		this.text = new TextView(this.group);
		this.braille = new BrailleView(this.group);
		this.item.setControl(this.group);
		
		tempPath = BBIni.getTempFilesPath() + BBIni.getFileSep();
		louisutdml = liblouisutdml.getInstance();
		
		FontManager.setShellFonts(this.wp.getShell(), this);
		
		this.tabList = new Control[]{this.treeView.view, this.text.view, this.braille.view};
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
			if(this.db.getDocumentTree() != null || this.text.hasChanged || this.braille.hasChanged || this.documentName != null){
				wp.addDocumentManager(tempName);
			}
			else {
				openDocument(tempName);
			}
			
			////////////////
			// Recent Files.
					
				// Update the recent files submenu.
				wp.getMainMenu().addRecentEntry(tempName);
				
			// Recent Files.
			////////////////
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
				this.text.hasChanged = false;	
				this.braille.hasChanged = false;		
				list.getLast().list.removeLast();
				this.text.addListeners(this);	
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
			this.text.view.append(current.getValue() + "\n");
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
		if (this.text.hasChanged || this.braille.hasChanged) {
			YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged"));
			if (ync.result == SWT.YES) {
				this.fileSave();
			}
		}

		haveOpenedFile = false;
		brailleFileName = null;
		documentName = null;
	
		this.text.hasChanged = false;
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
	
	public int findClosest(int location){
		for(int i = 0; i < list.size() - 1; i++){
			if(location >= list.get(i).offset && location < list.get(i + 1).offset){
				return i;
			}
		}
		if(location > this.list.get(this.list.size() - 1).offset){
					return this.list.size() - 1;
		}
		else {
			return -1;
		}
	}
	
	public void changeNodeText(int nodeNum, String text){
		Text temp = (Text)this.list.get(nodeNum).n;
		logger.log(Level.INFO, "Original Text Node Value:\t" + temp.getValue());
		logger.log(Level.INFO, "New Text Node Value:\t" + text);
		temp.setValue(text);
		System.out.println(this.list.get(nodeNum).n.getValue());
	}
	
	public void updateOffsets(int nodeNum, int offset){
		for(int i = nodeNum + 1; i < list.size(); i++)
			list.get(i).offset +=offset;
	}
	
	public void changeFocus(){
		TreeItem[] temp = this.treeView.tree.getSelection();
		TextMapElement me = (TextMapElement)temp[0].getData();
		if(me != null){
			this.text.view.setFocus();
			this.text.view.setCaretOffset(me.offset);
		}
	}
	
	public void updateFields(int nodeNum, int offset){		
		changeNodeText(nodeNum, this.text.view.getTextRange(this.list.get(nodeNum).offset, this.list.get(nodeNum).n.getValue().length() + offset));
		updateOffsets(nodeNum, offset);
		changeBrailleNodes(nodeNum, list.get(nodeNum).n.getValue());
	}
	
	public void changeBrailleNodes(int index, String text){
		String xml = getXMLString(text);
		Document d = getXML(xml);
		
		Element e = d.getRootElement().getChildElements("brl").get(0);
		d.getRootElement().removeChild(e);
		
		int startOffset = list.get(index).list.getFirst().offset;
		int total = 0;
		String logString = "";
		for(int i = 0; i < list.get(index).list.size(); i++){
			total += list.get(index).list.get(i).n.getValue().length() + 1;
			logString += list.get(index).list.get(i).n.getValue() + "\n";
		}
		logger.log(Level.INFO, "Original Braille Node Value:\n" + logString);
		
		Element brl = (Element)list.get(index).list.getFirst().n.getParent();
		list.get(index).n.getParent().removeChild(brl);
		list.get(index).n.getParent().appendChild(e);
		list.get(index).list.clear();
		
		String insertionString = "";
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				list.get(index).list.add(new BrailleMapElement(startOffset, e.getChild(i)));
				startOffset += e.getChild(i).getValue().length() + 1;
				insertionString += list.get(index).list.getLast().n.getValue() + "\n";
			}
		}		
		logger.log(Level.INFO, "New Braille Node Value:\n" + insertionString);
		this.braille.view.replaceTextRange(list.get(index).list.getFirst().offset, total, insertionString);
		updateBrailleOffsets(index, total);
	}
	
	public void updateBrailleOffsets(int index, int originalLength){
		int total = 0;
		for(int i = 0; i < list.get(index).list.size(); i++){
			total += list.get(index).list.get(i).n.getValue().length() + 1;
		}
		
		total -= originalLength;
		
		for(int i = index + 1; i < list.size(); i++){
			for(int j = 0; j < list.get(i).list.size(); j++){
				list.get(i).list.get(j).offset += total;
			}
		}
	}
	
	public Document getXML(String xml){
		byte [] outbuf = new byte[xml.length() * 10];
		
		int total = translateString(xml, outbuf);   
		if( total != -1){
			xml = "";
			for(int i = 0; i < total; i++)
				xml += (char)outbuf[i];
			
			StringReader sr = new StringReader(xml);
			Builder builder = new Builder();
			try {
				return builder.build(sr);
				
			} catch (ParsingException | IOException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		return null;
	}
	
	public String getXMLString(String text){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><string>" + text + "</string>";
	}
	
	public void setBrailleFocus(){
		int index = findClosest(this.text.view.getCaretOffset());
		this.braille.view.setFocus();
		this.braille.view.setCaretOffset(list.get(index).list.getFirst().offset);
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
	
	public int translateString(String text, byte[] outbuffer) {
		String logFile = BBIni.getLogFilesPath() + BBIni.getFileSep() + BBIni.getInstanceID() + BBIni.getFileSep() + "liblouisutdml.log";	
		String preferenceFile = BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + 
				BBIni.getFileSep() + "preferences.cfg";
		
		byte[] inbuffer;
		try {
			inbuffer = text.getBytes("UTF-8");
			int [] outlength = new int[1];
			outlength[0] = text.length() * 5;
			
			if(liblouisutdml.getInstance().translateString(preferenceFile, inbuffer, outbuffer, outlength, logFile, "formatFor utd\n mode notUC\n", 0)){
				return outlength[0];
			}
			else {
				System.out.println("An error occurred while translating");
				return -1;
			}
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return -1;
		}	
	}
}
