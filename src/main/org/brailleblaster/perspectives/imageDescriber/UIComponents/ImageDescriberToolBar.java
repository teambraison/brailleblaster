/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2010, 2012
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
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

package org.brailleblaster.perspectives.imageDescriber.UIComponents;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.brailleblaster.util.ImageHelper;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ImageDescriberToolBar {
	
	int MAX_W = 32;
	int MAX_H = 32;
	private ToolBar toolBar;
	WPManager wordProc;
	ImageHelper imgHelper;
	ImageDescriberController currentEditor;
	
	public ImageDescriberToolBar(Shell shell, final WPManager wp, ImageDescriberController controller)
	{
		setEditor(controller);
		String sep = BBIni.getFileSep();
		LocaleHandler lh = new LocaleHandler();
		toolBar = new ToolBar(shell, SWT.NONE);
		FormData location = new FormData();
		location.left = new FormAttachment(0);
		location.right = new FormAttachment(40);
		location.top = new FormAttachment(0);
		toolBar.setLayoutData(location);
		wordProc = wp;
		imgHelper = new ImageHelper();
		
		// Calculate max width and height for toolbar buttons.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		MAX_W = (int)screenSize.getWidth() / 30;
		MAX_H = MAX_W;
		
		// Change font size depending on screen resolution.
		FontData[] oldFontData = toolBar.getFont().getFontData();
		if( (int)screenSize.getWidth() >= 1920)
			oldFontData[0].setHeight(9);
		else if( (int)screenSize.getWidth() >= 1600)
			oldFontData[0].setHeight(8);
		else if( (int)screenSize.getWidth() >= 1280)
			oldFontData[0].setHeight(6);
		else if( (int)screenSize.getWidth() >= 1024)
			oldFontData[0].setHeight(4);
		else if( (int)screenSize.getWidth() >= 800)
			oldFontData[0].setHeight(3);
		toolBar.setFont( new Font(null, oldFontData[0]) );
		
		// Path to dist folder.
		String distPath = BBIni.getProgramDataPath().substring(0, BBIni.getProgramDataPath().lastIndexOf(sep));
		distPath += sep + "programData";
		
		// FO
		String tlabel;
		ToolItem openItem = new ToolItem(toolBar, SWT.PUSH);
		tlabel = lh.localValue("&Open");
		openItem.setText(tlabel.replace("&", ""));
		openItem.setImage( imgHelper.createScaledImage(new Image(null, distPath + sep + "images" + sep + "open.png"), MAX_W, MAX_H) );
//		openItem.setImage( new Image(null, distPath + sep + "images" + sep + "open.png") );
		openItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {
					// dm.setReturn (WP.OpenDocumentGetFile);
				} 
				else {
					int index= wp.getFolder().getSelectionIndex();
					if(index == -1){
						wp.addDocumentManager(null);
						setEditor((ImageDescriberController)wp.getList().getLast());
						currentEditor.fileOpenDialog();
						//wp.getList().getFirst().fileOpenDialog();
					}
					else {
						//wp.getList().get(index).fileOpenDialog();
						currentEditor.fileOpenDialog();
					}
				}
			}
		});

		ToolItem saveItem = new ToolItem(toolBar, SWT.PUSH);
		// FO
		tlabel = lh.localValue("&Save");
		saveItem.setText(tlabel.replace("&", ""));
		saveItem.setImage(new Image(null, distPath  + sep + "images" + sep + "save.png"));
		saveItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {
					// dm.setReturn (WP.OpenDocumentGetFile);
				} 
				else {
					int index= wp.getFolder().getSelectionIndex();
					if(index == -1){
						//wp.addDocumentManager(null);
						//wp.getList().getFirst().fileSave();
					}
					else {
					//	wp.getList().get(index).fileSave();
						currentEditor.save();
					}
				}
			}
		});
		
		ToolItem saveAsItem = new ToolItem(toolBar, SWT.PUSH);
		saveAsItem.setText("Save As");
		saveAsItem.setImage(new Image(null, distPath  + sep + "images" + sep + "saveAs.png"));
		saveAsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {
					// dm.setReturn (WP.OpenDocumentGetFile);
				} 
				else {
					int index= wp.getFolder().getSelectionIndex();
					if(index == -1){
					//	wp.addDocumentManager(null);
					//	wp.getList().getFirst().saveAs();
					}
					else {
						//wp.getList().get(index).saveAs();
						currentEditor.saveAs();
					}
				}
			}
		});
		
		toolBar.pack();
		
	} // ImageDescriberToolBar() constructor.
	
	public void setEditor(ImageDescriberController editor){
		currentEditor = editor;
	}
	
	public void dispose() {
		toolBar.dispose();
	}
	
} // class ImageDescriberToolBar.

