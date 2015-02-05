/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
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

package org.brailleblaster.perspectives.braille.ui;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.brailleblaster.util.ImageHelper;
import org.brailleblaster.wordprocessor.BBFileDialog;
import org.brailleblaster.wordprocessor.BBToolBar;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.graphics.Font;
//import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class BrailleToolBar extends BBToolBar {
	String curView = null;
	
	// FO
	public BrailleToolBar(Shell shell, final WPManager wp, Manager manager) {
		super(shell, wp, manager);
		
		setEditor(manager);



		ToolItem openItem = new ToolItem(toolBar, SWT.PUSH);
		tlabel = lh.localValue("&Open");
		openItem.setText(tlabel.replace("&", ""));
		openItem.setImage( imgHelper.createScaledImage(new Image(null, distPath + sep + "images" + sep + "open.png"), MAX_W, MAX_H) );
//		openItem.setImage( new Image(null, distPath + sep + "images" + sep + "open.png") );
		openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filePath = fileOpenDialog();
				int index= wp.getFolder().getSelectionIndex();
				if(index == -1 && filePath != null){
					wp.addDocumentManager(filePath);
				}
				else if(filePath != null){
					if( ((Manager)currentEditor).canReuseTab() ){
						((Manager)currentEditor).closeUntitledTab();
						((Manager)currentEditor).openDocument(filePath);
						((Manager)currentEditor).checkTreeFocus();
					}
					else
						wp.addDocumentManager(filePath);
				}
			}
		});

		ToolItem saveItem = new ToolItem(toolBar, SWT.PUSH);
		// FO
		tlabel = lh.localValue("&Save");
		saveItem.setText(tlabel.replace("&", ""));
		saveItem.setImage(new Image(null, distPath  + sep + "images" + sep + "save.png"));
		saveItem.addSelectionListener(new SelectionAdapter() {
			@Override
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
						((Manager)currentEditor).fileSave();
					}
				}
			}
		});
		
		ToolItem saveAsItem = new ToolItem(toolBar, SWT.PUSH);
		saveAsItem.setText("Save As");
		saveAsItem.setImage(new Image(null, distPath  + sep + "images" + sep + "saveAs.png"));
		saveAsItem.addSelectionListener(new SelectionAdapter() {
			@Override
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
						((Manager)currentEditor).saveAs();
					}
				}
			}
		});
	
		
		ToolItem translateItem = new ToolItem(toolBar, SWT.PUSH);
		// FO
		tlabel = lh.localValue("&Translate");
		translateItem.setText(tlabel.replace("&", ""));
		translateItem.setImage(new Image(null, distPath  + sep + "images" + sep + "translate.png"));
		// FO
		translateItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// dm.translateView(true);
			}
		});

		ToolItem embossNow = new ToolItem(toolBar, SWT.PUSH);
		// FO
		tlabel = lh.localValue("Emboss&Now!");
		embossNow.setText(tlabel.replace("&", ""));
		embossNow.setImage(new Image(null, distPath  + sep + "images" + sep + "emboss.png"));
		embossNow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index= wp.getFolder().getSelectionIndex();
				if(index != -1){
					//wp.getList().get(index).fileEmbossNow();
					((Manager)currentEditor).fileEmbossNow();
				}
			}
		});

		/**
		 * ToolItem embossWithInk = new ToolItem (toolBar, SWT.PUSH); tlabel =
		 * lh.localValue ("EmbossInkN&ow"); embossWithInk.setText
		 * (tlabel.replace ("&", "")); embossWithInk.setEnabled(false);
		 * embossWithInk.addSelectionListener (new SelectionAdapter() { public
		 * void widgetSelected (SelectionEvent e) { dm.placeholder(); } });
		 **/

		ToolItem daisyPrint = new ToolItem(toolBar, SWT.PUSH);
		tlabel = lh.localValue("&Print");
		daisyPrint.setText(tlabel.replace("&", ""));
		daisyPrint.setImage(new Image(null, distPath  + sep + "images" + sep + "print.png"));
		daisyPrint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// dm.daisyPrint();
			}
		});
		
		final ToolItem viewSwitch = new ToolItem(toolBar, SWT.PUSH);
		tlabel = lh.localValue("&Toggle View");
		viewSwitch.setText(tlabel.replace("&", ""));
		setViewButton(viewSwitch);
		viewSwitch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setViewButton(viewSwitch);
			}
		});

		FormData bloc = new FormData();
		bloc.left = new FormAttachment(40);
		bloc.right = new FormAttachment(50);
		bloc.top = new FormAttachment(5);
		
		toolBar.pack();
	}
	
	public void setViewButton(final ToolItem ti) {
		final String dpStr = distPath;
		final String sepStr = sep;
		String view = ((Manager)currentEditor).getCurrentEditor();
		if(view == null || view.equals("")){
			((Manager)currentEditor).setEditingView(null);
			ti.setImage(new Image(null, dpStr  + sepStr + "images" + sepStr + "view_PB.png"));
		}
		else if(view.equals(TextView.class.getCanonicalName())){
			((Manager)currentEditor).setEditingView(TextView.class.getCanonicalName());
			ti.setImage(new Image(null, dpStr  + sepStr + "images" + sepStr + "view_P.png"));
		}
		else if(view.equals(BrailleView.class.getCanonicalName())) {
			((Manager)currentEditor).setEditingView(BrailleView.class.getCanonicalName());
			ti.setImage(new Image(null, dpStr  + sepStr + "images" + sepStr + "view_B.png"));
		}
	}
}