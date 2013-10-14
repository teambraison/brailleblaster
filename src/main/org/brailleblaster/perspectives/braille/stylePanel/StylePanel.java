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

package org.brailleblaster.perspectives.braille.stylepanel;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.YesNoChoice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/*	TODO
 * 	1. Can user modify build-in styles? If yes, then do we keep the original build-in properties file?
 *  2. Delete function. Can users delete build-in styles?
 *  3. Add localized strings
 */


public class StylePanel extends Dialog{
	static Display display;
	static Shell stylePanel;

	Button delete;
	Button create; 
	Button modify;
	Button apply;
	
	static int propertyLabelWidth = 125;
	static int propertyTextWidth = 100;

	private String fileSep;
	private StyleManager sm;
	private String localStylesPath;
	private String buildInStylesPath;
	private final String PROPEXTENSION = ".properties";


	private static HashMap<String, Style> styleMap 
	= new HashMap<String, Style>();

	FileUtils fu = new FileUtils();
	private Combo combo;
	private LocaleHandler lh = new LocaleHandler();
	ArrayList<Style> styleList;//styles
	ArrayList<String> displayArr;//Styles' names for display

	public StylePanel (Shell parent, int style) {
		super (parent, style);
	}

//	StylePanel(StyleManager styleManager) {
//		this(styleManager.dm.documentWindow, SWT.NONE);
//		this.sm = styleManager;
//		display = BBIni.getDisplay();
//		fileSep = BBIni.getFileSep();
//		localStylesPath = BBIni.getStylePath();
//		buildInStylesPath = BBIni.getProgramDataPath()+fileSep+"styles" ;
//		readStyleFiles("default");
//	}
	StylePanel(StyleManager styleManager) {
		this(styleManager.dm.getWPManager().getShell(), SWT.NONE);
		this.sm = styleManager;
		display = styleManager.dm.getDisplay();
		fileSep = BBIni.getFileSep();
		localStylesPath = BBIni.getStylePath();
		buildInStylesPath = BBIni.getProgramDataPath()+fileSep+"styles" ;
		readStyleFiles("default");
	}

	/**
	 *  Read style properties files and update the combo if it has been created;
	 *  Then set the default item index of the combo to the index of styleName
	 * 	@param styleName
	 */
	public void readStyleFiles(String styleName){
		styleList = new ArrayList<Style>();
		readStyleFromBuildIn();
		readStyleFromLocal();
		displayArr = new ArrayList<String>();
		for(Style style: styleList){
			String displayName = style.toString();
			if(style.getIsBuildIn()) displayName += " [" + lh.localValue("styleBuildIn")+"]";
			displayArr.add(displayName);
		}
		if(combo != null){
			combo.setItems((String[])displayArr.toArray(new String[displayArr.size()]));
			int index = (styleName!=null)?displayArr.indexOf(styleName):0;
			combo.select(index);
		}
	}

	private void readStyleFromBuildIn(){
		File dir = new File(buildInStylesPath);
		if(!dir.exists()) {
			//no build-in styles
			return;
		}
		String[] children = dir.list();
		for(int curStr = 0; curStr < children.length; curStr++){
			int index = children[curStr].indexOf(PROPEXTENSION);
			
			// This isn't a style file. Skip it.
			if(index < 0)
				continue;
			
			String styleName = children[curStr].substring(0,index);
			Style style = new Style(styleName);
			style.setIsBuildIn(true);
			styleList.add(style);
		}
	}

	private void readStyleFromLocal(){
		File dir = new File(localStylesPath);
		ClassLoader loader = null;
		try {
			URL[] urls={				
					dir.toURI().toURL()
			};
			loader = new URLClassLoader(urls);
		} catch (MalformedURLException e){
			e.printStackTrace();
		}
		String[] children = dir.list();
		for(String s:children){
			int index = s.indexOf(PROPEXTENSION);
			String styleName = s.substring(0,index);
			Style style = new Style(styleName, loader);
			styleList.add(style);
		}
	}
	
	void open(){
		Shell parent = getParent();
		Display display = parent.getDisplay();
		stylePanel = new Shell(parent, SWT.DIALOG_TRIM);
		stylePanel.setText(lh.localValue("Style Panel"));
		FormLayout layout = new FormLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		stylePanel.setLayout(layout);

		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = stylePanel.getBounds();
		int x = bounds.x + ((bounds.width - rect.width) / 2) + 10;
		int y = bounds.y + ((bounds.height - rect.height) / 2) + 10;
		stylePanel.setLocation (x, y);

		GridLayout gridLayout = new GridLayout ();
		stylePanel.setLayout (gridLayout);
		gridLayout.marginTop = 15;
		gridLayout.marginBottom = 15;
		gridLayout.marginLeft = 10; 
		gridLayout.marginRight = 10; 
		gridLayout.numColumns = 7;
		//gridLayout.horizontalSpacing = 15;
		gridLayout.makeColumnsEqualWidth =true;
		
		
		Label name = new Label(stylePanel, SWT.HORIZONTAL);
		name.setText(lh.localValue("styleName"));

		combo = new Combo (stylePanel, SWT.DROP_DOWN);
		combo.setItems((String[])displayArr.toArray(new String[displayArr.size()]));
		combo.select(0);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		combo.setLayoutData(data);

		Accessible accName = name.getAccessible();
		Accessible accNameCombo = combo.getAccessible();
		accName.addRelation(ACC.RELATION_LABEL_FOR, accNameCombo);
		accNameCombo.addRelation(ACC.RELATION_LABELLED_BY, accName);

		create = new Button(stylePanel, SWT.PUSH);
		modify = new Button(stylePanel, SWT.PUSH);
		delete = new Button(stylePanel, SWT.PUSH);
		apply = new Button(stylePanel, SWT.PUSH);
		
		delete.setText(lh.localValue("styleDelete"));
		data = new GridData(GridData.FILL_HORIZONTAL);
		delete.setLayoutData(data);

		create.setText(lh.localValue("styleCreate"));
		create.setLayoutData(data);
				
		modify.setText(lh.localValue("styleModify"));
		modify.setLayoutData(data);

		apply.setText(lh.localValue("styleApply"));//need to add to i18n files
		apply.setLayoutData(data);
		updateButtons(isExisted() >= 0);
		

		combo.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				updateButtons(isExisted() >= 0);
			}
		});


		apply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int key = combo.getSelectionIndex();
				if(key != -1){
					Style style = styleList.get(key);
					//printStyle(style);
					apply(style);
				}
				stylePanel.close();
			}
		});

		modify.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int key = combo.getSelectionIndex();
				if(key != -1){
					Style style = styleList.get(key);
					modify(style);
				}else{
					key = isExisted();
					if(key != -1){
						Style style = styleList.get(key);
						modify(style);
					}else{					
						System.out.println("trying to modify a style that doesn't exsist");
					}
				}
			}
		});

		create.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String styleName = combo.getText();
				create(styleName);
			}
		});
		
		delete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				YesNoChoice prompt =  new YesNoChoice(lh.localValue("styleDeleteMsg")+"\""+combo.getText()+"\"?");
				if(prompt.result == SWT.YES){
					int key = isExisted();
					Style style = styleList.get(key);
					delete(style);
				}
			}
		});

		// show the SWT window
		stylePanel.pack();
		stylePanel.open();
		while (!stylePanel.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		// tear down the SWT window
		stylePanel.dispose();     
	}

	/**
	 * 	Check whether the current text in the combo field is already a style.
	 *	return the index if exist; -1 otherwise.
	 */
	int isExisted(){
		String list[] = combo.getItems();
		String styleName = combo.getText();
		for(int i =0; i< list.length; i++){
			if(list[i].equals(styleName)){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 	update button's enabled status based on whether the style is existed
	 * 
	 * @param style
	 */
	
	void updateButtons(boolean isExisted){
		modify.setEnabled(isExisted);
		delete.setEnabled(isExisted);
		apply.setEnabled(isExisted);
		create.setEnabled(!isExisted);
		if(isExisted){
			stylePanel.setDefaultButton(apply);
		}else{
			stylePanel.setDefaultButton(create);
		}
	}
	

	void apply(Style style){
//		if(view == null){
//			this.view = dm.daisy.view;
//		}
//		view.setLineIndent(1, 1, 100);
		//style.print();
	}

	void create(String styleName){
		sm.createStyle(styleName);
	}

	void modify(Style style){
		sm.modifyStyle(style);
	}
	
	void delete(Style style){
		if(style.getIsBuildIn()){
			//if it is build-in style, should it be deleted?
		}else{
			String fileName = localStylesPath + fileSep +style.getName() + PROPEXTENSION;
			//System.out.println("deleting" + fileName);
			if(fu.deleteFile(fileName)){
				readStyleFiles(null);
			}else{
				System.out.println("fail to delete: " + fileName);
			}
		}
	}

	void printStyle(Style style){
		style.print();
	}
}
