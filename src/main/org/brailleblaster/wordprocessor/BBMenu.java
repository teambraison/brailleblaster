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

package org.brailleblaster.wordprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.settings.SettingsDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

class BBMenu {
	//This class contains all the menus.
	final Menu menuBar;

	/* All the menu items are member fields so they can be accessed outside the
	 * constructor. This might be done for example with setEnabled(false) to
	 * indicate that a menu item is unavailable.
	 */
	MenuItem newItem;
	MenuItem openItem;
	MenuItem recentItem;
	MenuItem importItem;
	MenuItem saveItem;
	MenuItem saveAsItem;
	MenuItem embosserSetupItem;
	MenuItem embosserPreviewItem;
	MenuItem embossInkPreviewItem;
	MenuItem embossNowItem;
	MenuItem embossInkNowItem;
	MenuItem printPageSetupItem;
	MenuItem printPreviewItem;
	MenuItem printItem;
	MenuItem languageItem;
	MenuItem closeItem;
	MenuItem exitItem;
	MenuItem undoItem;
	MenuItem redoItem;
	MenuItem cutItem;
	MenuItem copyItem;
	MenuItem pasteItem;
	MenuItem searchItem;
	MenuItem replaceItem;
	MenuItem spellCheckItem;
	MenuItem boldToggleItem;
	MenuItem italicToggleItem;
	MenuItem underlineToggleItem;
	MenuItem zoomImageItem;
	MenuItem selectAllItem;
	MenuItem stylePanelItem;
	MenuItem nextElementItem;
	MenuItem assocSelectionItem;
	MenuItem lockSelectionItem;
	MenuItem unlockSelectionItem;
	MenuItem editLockedItem;
	MenuItem keybdBrlToggleItem;
	MenuItem cursorFollowItem;
	MenuItem dragCursorItem;
	MenuItem increaseFontSizeItem;
	MenuItem decreaseFontSizeItem;
	MenuItem increaseContrastItem;
	MenuItem decreaseContrastItem;
	MenuItem showOutlineItem;
	MenuItem braillePresentationItem;
	MenuItem formatLikeBrailleItem;
	MenuItem showPageBreaksItem;
	MenuItem xtranslateItem;
	MenuItem backTranslateItem;
	MenuItem translationTemplatesItem;
	MenuItem inLineMathItem;
	MenuItem displayedMathItem;
	MenuItem inLineGraphicItem;
	MenuItem displayedGraphicItem;
	MenuItem tableItem;
	MenuItem brlFormatItem;
	MenuItem brailleASCIIItem;
	MenuItem showTranslationTemplatesItem;
	MenuItem showFormatTemplatesItem;
	MenuItem changeSettingsItem;
	MenuItem readManualItem;
	MenuItem helpInfoItem;
	MenuItem tutorialsItem;
	MenuItem checkUpdatesItem;
	MenuItem aboutItem;
	WPManager wordProc;
	ArrayList<String> recentDocsList = null;
	Menu subMen;
	final int maxRecentFiles = 5;

	BBMenu(final WPManager wp) {
		LocaleHandler lh = new LocaleHandler();

		/*
		 * Note that the values in the setText methods are keys for
		 * localization. They are not intended to be seen by the user.
		 * Capitalization should follow the convention for names in Java, but
		 * this is not always consistent. Values, with proper capitalization,
		 * are shown in the files in the dist/programData/lang subdirectory.
		 */

		// Hold onto word processor so other functions have access.
		wordProc = wp;

    	// Init recent doc list.
    	recentDocsList = new ArrayList<String>();
		
		// Set up menu bar
		menuBar = new Menu(wp.getShell(), SWT.BAR);
		MenuItem fileItem = new MenuItem(menuBar, SWT.CASCADE);
		fileItem.setText(lh.localValue("&File"));
		MenuItem editItem = new MenuItem(menuBar, SWT.CASCADE);
		editItem.setText(lh.localValue("&Edit"));
		MenuItem viewItem = new MenuItem(menuBar, SWT.CASCADE);
		viewItem.setText(lh.localValue("&View"));
		MenuItem translateItem = new MenuItem(menuBar, SWT.CASCADE);
		translateItem.setText(lh.localValue("&Translate"));
		MenuItem insertItem = new MenuItem(menuBar, SWT.CASCADE);
		insertItem.setText(lh.localValue("&Insert"));
		insertItem.setEnabled(false); /* FO */

		MenuItem advancedItem = new MenuItem(menuBar, SWT.CASCADE);
		advancedItem.setText(lh.localValue("&Advanced"));
//		advancedItem.setEnabled(false); 
		MenuItem helpItem = new MenuItem(menuBar, SWT.CASCADE);
		helpItem.setText(lh.localValue("&Help"));

		// Set up file menu
		Menu fileMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		newItem = new MenuItem(fileMenu, SWT.PUSH);
		newItem.setText(lh.localValue("&New") + "\tCtrl + N");
		newItem.setAccelerator(SWT.MOD1 + 'N');
		newItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {
					//dm.setReturn(WP.NewDocument);
				} else {
					System.out.println("New Document Tab created");
					wp.addDocumentManager(null);
				}
			}
		});
		openItem = new MenuItem(fileMenu, SWT.PUSH);
		openItem.setText(lh.localValue("&Open") + "\tCtrl + O");
		openItem.setAccelerator(SWT.MOD1 + 'O');
		openItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {
					//dm.setReturn(WP.OpenDocumentGetFile);
				} 
				else {
					int index= wp.getFolder().getSelectionIndex();
					if(index == -1){
						wp.addDocumentManager(null);
						wp.getList().getFirst().fileOpenDialog();
					}
					else {
						wp.getList().get(index).fileOpenDialog();
					}
				}
			}
		});
		
		recentItem = new MenuItem (fileMenu, SWT.CASCADE);
		recentItem.setText(lh.localValue("&Recent"));
		
		// Setup recent files submenu.
		subMen = new Menu(wordProc.getShell(), SWT.DROP_DOWN);
		recentItem.setMenu(subMen);
		
		// Reads recent file list... from file.
		readRecentFiles();
		
		importItem = new MenuItem(fileMenu, SWT.PUSH);
		importItem.setText(lh.localValue("&Import"));
		importItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.importDocument();
			}
		});
		saveItem = new MenuItem(fileMenu, SWT.PUSH);
		saveItem.setText(lh.localValue("&Save"));
		saveItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.fileSave();
			}
		});
		saveAsItem = new MenuItem(fileMenu, SWT.PUSH);
		saveAsItem.setText(lh.localValue("Save&As"));
		saveAsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.fileSaveAs();
				int index= wp.getFolder().getSelectionIndex();
				if(index != -1 && wp.getList().get(index).documentName != null){
					wp.getList().get(index).saveAs();
				}
			}
		});
		
		saveAsItem = new MenuItem(fileMenu, SWT.PUSH);
		saveAsItem.setText(lh.localValue("Save&Braille"));
		saveAsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.brailleSave();
			}
		});
		
		embosserSetupItem = new MenuItem(fileMenu, SWT.PUSH);
		embosserSetupItem.setText(lh.localValue("&EmbosserSetup"));
		embosserSetupItem.setEnabled(false); /* FO */
		embosserSetupItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
/**		
		embosserPreviewItem = new MenuItem(fileMenu, SWT.PUSH);
		embosserPreviewItem.setText(lh.localValue("Embosser&Preview"));
		embosserPreviewItem.setEnabled(false); 
		embosserPreviewItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dm.placeholder();
			}
		});
		embossInkPreviewItem = new MenuItem(fileMenu, SWT.PUSH);
		embossInkPreviewItem.setText(lh.localValue("Emboss&InkPreview"));
		embossInkPreviewItem.setEnabled(false); 
		embossInkPreviewItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dm.placeholder();
			}
		});
**/		
		embossNowItem = new MenuItem(fileMenu, SWT.PUSH);
		embossNowItem.setText(lh.localValue("Emboss&Now!"));
		embossNowItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.fileEmbossNow();
			}
		});
/**		
		embossInkNowItem = new MenuItem(fileMenu, SWT.PUSH);
		embossInkNowItem.setText(lh.localValue("EmbossInkN&ow"));
		embossInkNowItem.setEnabled(false); 
		embossInkNowItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dm.placeholder();
			}
		});
**/		
/**		
		printPageSetupItem = new MenuItem(fileMenu, SWT.PUSH);
		printPageSetupItem.setText(lh.localValue("PrintPageS&etup"));
		printPageSetupItem.setEnabled(false); 
		printPageSetupItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dm.placeholder();
			}
		});
		printPreviewItem = new MenuItem(fileMenu, SWT.PUSH);
		printPreviewItem.setText(lh.localValue("PrintP&review"));
		printPreviewItem.setEnabled(false); 
		printPreviewItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dm.placeholder();
			}
		});
**/
		printItem = new MenuItem(fileMenu, SWT.PUSH);
		printItem.setText(lh.localValue("&Print"));
//		printItem.setEnabled(false); 
		printItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
//				dm.placeholder();
				//dm.daisyPrint();
			}
		});
		languageItem = new MenuItem(fileMenu, SWT.PUSH);
		languageItem.setText(lh.localValue("&Language"));
		languageItem.setEnabled(false); /* FO */
		languageItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		
		closeItem = new MenuItem(fileMenu, SWT.PUSH);
		closeItem.setText(lh.localValue("&Close") + "\tCtrl + W");
		closeItem.setAccelerator(SWT.MOD1 + 'W');
		closeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = wp.getFolder().getSelectionIndex();
				wp.getList().get(index).fileClose();
				wp.getList().remove(index);
				wp.checkToolbarSettings();
			}
		});
		if (!BBIni.getPlatformName().equals("cocoa")) {
			exitItem = new MenuItem(fileMenu, SWT.PUSH);
			exitItem.setText(lh.localValue("e&xit") + "\tCtrl + Q");
			exitItem.setAccelerator(SWT.MOD1 + 'Q');
			exitItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					for(int i = 0; i < wp.getList().size(); i++){
						DocumentManager temp = wp.getList().get(i);
						if(temp.text.hasChanged || temp.braille.hasChanged){
							temp.fileClose();
						}
					}
					wp.getList().clear();
					wp.getShell().getDisplay().dispose();
				}
			});
		}
		fileItem.setMenu(fileMenu);

		// Set up edit menu
		Menu editMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		undoItem = new MenuItem(editMenu, SWT.PUSH);
		undoItem.setText(lh.localValue("&Undo"));
		undoItem.setEnabled(false);
		undoItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {
					//dm.setReturn(WP.SwitchDocuments);
				} else {
					//dm.placeholder();
				}
			}
		});
		redoItem = new MenuItem(editMenu, SWT.PUSH);
		redoItem.setText(lh.localValue("&Redo"));
		redoItem.setEnabled(false);
		redoItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		cutItem = new MenuItem(editMenu, SWT.PUSH);
		cutItem.setText(lh.localValue("&Cut"));
		cutItem.setEnabled(false);
		cutItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		copyItem = new MenuItem(editMenu, SWT.PUSH);
		copyItem.setText(lh.localValue("&Copy"));
		copyItem.setEnabled(false);
		copyItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		pasteItem = new MenuItem(editMenu, SWT.PUSH);
		pasteItem.setText(lh.localValue("&Paste"));
		pasteItem.setEnabled(false);
		pasteItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		searchItem = new MenuItem(editMenu, SWT.PUSH);
		searchItem.setText(lh.localValue("&Search"));
		searchItem.setEnabled(false);
		searchItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		replaceItem = new MenuItem(editMenu, SWT.PUSH);
		replaceItem.setText(lh.localValue("&Replace"));
		replaceItem.setEnabled(false);
		replaceItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		spellCheckItem = new MenuItem(editMenu, SWT.PUSH);
		spellCheckItem.setText(lh.localValue("&SpellCheck"));
		spellCheckItem.setEnabled(false);
		spellCheckItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		boldToggleItem = new MenuItem(editMenu, SWT.PUSH);
		boldToggleItem.setText(lh.localValue("&BoldToggle"));
		boldToggleItem.setEnabled(false);
		boldToggleItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		italicToggleItem = new MenuItem(editMenu, SWT.PUSH);
		italicToggleItem.setText(lh.localValue("&ItalicToggle"));
		italicToggleItem.setEnabled(false);
		italicToggleItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		underlineToggleItem = new MenuItem(editMenu, SWT.PUSH);
		underlineToggleItem.setText(lh.localValue("&UnderlineToggle"));
		underlineToggleItem.setEnabled(false);
		underlineToggleItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		zoomImageItem = new MenuItem(editMenu, SWT.PUSH);
		zoomImageItem.setText(lh.localValue("&ZoomImage"));
		zoomImageItem.setEnabled(false);
		zoomImageItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		selectAllItem = new MenuItem(editMenu, SWT.PUSH);
		selectAllItem.setText(lh.localValue("&SelectAll"));
		selectAllItem.setEnabled(false);
		selectAllItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		stylePanelItem = new MenuItem(editMenu, SWT.PUSH);
		stylePanelItem.setText(lh.localValue("&StylePanel"));
		stylePanelItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.getStyleManager().stylePanel();
				System.out.println("Empty method");
			}
		});

		nextElementItem = new MenuItem(editMenu, SWT.PUSH);
		nextElementItem.setText(lh.localValue("&NexstElement"));
		nextElementItem.setEnabled(false);
		nextElementItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		assocSelectionItem = new MenuItem(editMenu, SWT.PUSH);
		assocSelectionItem.setText(lh.localValue("&AssocSelection"));
		assocSelectionItem.setEnabled(false);
		assocSelectionItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		lockSelectionItem = new MenuItem(editMenu, SWT.PUSH);
		lockSelectionItem.setText(lh.localValue("&LockSelection"));
		lockSelectionItem.setEnabled(false);
		lockSelectionItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		unlockSelectionItem = new MenuItem(editMenu, SWT.PUSH);
		unlockSelectionItem.setText(lh.localValue("&UnlockSelection"));
		unlockSelectionItem.setEnabled(false);
		unlockSelectionItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		editLockedItem = new MenuItem(editMenu, SWT.PUSH);
		editLockedItem.setText(lh.localValue("&EditLocked"));
		editLockedItem.setEnabled(false);
		editLockedItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		keybdBrlToggleItem = new MenuItem(editMenu, SWT.PUSH);
		keybdBrlToggleItem.setText(lh.localValue("&KeybdBrlToggle"));
		keybdBrlToggleItem.setEnabled(false);
		keybdBrlToggleItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
/**		
		cursorFollowItem = new MenuItem(editMenu, SWT.PUSH);
		cursorFollowItem.setText(lh.localValue("&CursorFollow"));
		cursorFollowItem.setEnabled(false);
		cursorFollowItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dm.placeholder();
			}
		});
		dragCursorItem = new MenuItem(editMenu, SWT.PUSH);
		dragCursorItem.setText(lh.localValue("&DragCursor"));
		dragCursorItem.setEnabled(false);
		dragCursorItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dm.placeholder();
			}
		});
**/		
		editItem.setMenu(editMenu);
		editItem.setEnabled(true);

		// Set up view menu
		Menu viewMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		increaseFontSizeItem = new MenuItem(viewMenu, SWT.PUSH);
		increaseFontSizeItem.setText(lh.localValue("&IncreaseFontSize") + "\tCtrl + '+'");
		increaseFontSizeItem.setAccelerator(SWT.MOD1 + '+');
		increaseFontSizeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = wp.getFolder().getSelectionIndex();
				FontManager.increaseFont(wp, wp.getList().get(index));
			}
		});
		decreaseFontSizeItem = new MenuItem(viewMenu, SWT.PUSH);
		decreaseFontSizeItem.setText(lh.localValue("&DecreaseFontSize") + "\tCtrl + '-'");
		decreaseFontSizeItem.setAccelerator(SWT.MOD1 + '-');
		decreaseFontSizeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = wp.getFolder().getSelectionIndex();
				FontManager.decreaseFont(wp, wp.getList().get(index));
			}
		});
		increaseContrastItem = new MenuItem(viewMenu, SWT.PUSH);
		increaseContrastItem.setText(lh.localValue("&IncreaseContrast"));
		increaseContrastItem.setEnabled(false);
		increaseContrastItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		decreaseContrastItem = new MenuItem(viewMenu, SWT.PUSH);
		decreaseContrastItem.setText(lh.localValue("&DecreaseContrast"));
		decreaseContrastItem.setEnabled(false);
		decreaseContrastItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		showOutlineItem = new MenuItem(viewMenu, SWT.PUSH);
		showOutlineItem.setText(lh.localValue("&ShowOutline"));
		showOutlineItem.setEnabled(false);
		showOutlineItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
/**		
		braillePresentationItem = new MenuItem(viewMenu, SWT.PUSH);
		braillePresentationItem.setText(lh.localValue("&BraillePresentation"));
		braillePresentationItem.setEnabled(false); 
		braillePresentationItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				 dm.placeholder();
	
			}
		});

		formatLikeBrailleItem = new MenuItem(viewMenu, SWT.PUSH);
		formatLikeBrailleItem.setText(lh.localValue("&FormatLikeBraille"));
		formatLikeBrailleItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dm.placeholder();
			}
		});
		showPageBreaksItem = new MenuItem(viewMenu, SWT.PUSH);
		showPageBreaksItem.setText(lh.localValue("&ShowPageBreaks"));
		showPageBreaksItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dm.placeholder();
			}
		});
**/
		viewItem.setMenu(viewMenu);

		// Set up translate menu
		Menu translateMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		xtranslateItem = new MenuItem(translateMenu, SWT.PUSH);
		xtranslateItem.setText(lh.localValue("&Translate"));
		xtranslateItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.translateView(true);
			}
		});
		backTranslateItem = new MenuItem(translateMenu, SWT.PUSH);
		backTranslateItem.setText(lh.localValue("&BackTranslate"));
		backTranslateItem.setEnabled(false);
		backTranslateItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		translationTemplatesItem = new MenuItem(translateMenu, SWT.PUSH);
		translationTemplatesItem
				.setText(lh.localValue("&TranslationTemplates"));
		translationTemplatesItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		translateItem.setMenu(translateMenu);

		// Set up insert menu
		Menu insertMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		inLineMathItem = new MenuItem(insertMenu, SWT.PUSH);
		inLineMathItem.setText(lh.localValue("&InLineMath"));
		inLineMathItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		displayedMathItem = new MenuItem(insertMenu, SWT.PUSH);
		displayedMathItem.setText(lh.localValue("&DisplayedMath"));
		displayedMathItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		inLineGraphicItem = new MenuItem(insertMenu, SWT.PUSH);
		inLineGraphicItem.setText(lh.localValue("&InLineGraphic"));
		inLineGraphicItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		displayedGraphicItem = new MenuItem(insertMenu, SWT.PUSH);
		displayedGraphicItem.setText(lh.localValue("&DisplayedGraphic"));
		displayedGraphicItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		tableItem = new MenuItem(insertMenu, SWT.PUSH);
		tableItem.setText(lh.localValue("&Table"));
		tableItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		insertItem.setMenu(insertMenu);

		// Set up advanced menu
		Menu advancedMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		brlFormatItem = new MenuItem(advancedMenu, SWT.PUSH);
		brlFormatItem.setText(lh.localValue("&BrailleFormat"));
		brlFormatItem.setEnabled(false);
		brlFormatItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		brailleASCIIItem = new MenuItem(advancedMenu, SWT.PUSH);
		brailleASCIIItem.setText(lh.localValue("&brailleASCIITable"));
		brailleASCIIItem.setEnabled(false);
		brailleASCIIItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		showTranslationTemplatesItem = new MenuItem(advancedMenu, SWT.PUSH);
		showTranslationTemplatesItem.setText(lh.localValue("&ShowTranslationTemplates"));
		showTranslationTemplatesItem.setEnabled(false);
		showTranslationTemplatesItem
				.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						//dm.placeholder();
					}
				});
		showFormatTemplatesItem = new MenuItem(advancedMenu, SWT.PUSH);
		showFormatTemplatesItem.setText(lh.localValue("&ShowFormatTemplates"));
		showFormatTemplatesItem.setEnabled(false);
		showFormatTemplatesItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.placeholder();
			}
		});
		changeSettingsItem = new MenuItem(advancedMenu, SWT.PUSH);
		changeSettingsItem.setText(lh.localValue("&changeSettings"));
		changeSettingsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new SettingsDialog().open();
			}
		});
		advancedItem.setMenu(advancedMenu);

		// Set up help menu
		Menu helpMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		aboutItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutItem.setText(lh.localValue("&About"));
		aboutItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(WP.AboutBB);
			}
		});
		helpInfoItem = new MenuItem(helpMenu, SWT.PUSH);
		helpInfoItem.setText(lh.localValue("&helpInfo"));
		helpInfoItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(WP.HelpInfo);
			}
		});
		tutorialsItem = new MenuItem(helpMenu, SWT.PUSH);
		tutorialsItem.setText(lh.localValue("&Tutorials"));
		tutorialsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(WP.ReadTutorial);
			}
		});
		readManualItem = new MenuItem(helpMenu, SWT.PUSH);
		readManualItem.setText(lh.localValue("&ReadManuals"));
		readManualItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(WP.ReadManuals);
			}
		});
		checkUpdatesItem = new MenuItem(helpMenu, SWT.PUSH);
		checkUpdatesItem.setText(lh.localValue("&CheckUpdates"));
		checkUpdatesItem.setEnabled(false); /* FO */
		checkUpdatesItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new UserHelp(WP.CheckUpdates);
			}
		});
		helpItem.setMenu(helpMenu);
		
		// Activate menus when documentWindow shell is opened
		wp.getShell().setMenuBar(menuBar);
	}
	
	//////////////////////////////////////////////////////////////////////////	
	// Returns ArrayList<String> of recent documents list.
	public ArrayList<String> getRecentDocumentsList() {
		return recentDocsList;
	}
	
	//////////////////////////////////////////////////////////////////////////
	// Looks for recent files document, then updates 
	// the main menu's recent files list.
	public void readRecentFiles()
	{
		////////////////////////////
		// Populate Recent Documents
		
			// Temp buffer to hold entries.
	    	ArrayList<String> tempStrList = new ArrayList<String>();
		
			// Open file and populate!
	        try
	        {
	        	// Open File.
	        	BufferedReader reader = new BufferedReader(new FileReader( BBIni.getRecentDocs() ));
	        	
	        	// Record every line into our list.
	            String line = null;
	            while ((line = reader.readLine()) != null)
	            	tempStrList.add(line);
	            
	            // Close the document.
	            reader.close();
	        }
	        catch (FileNotFoundException e) { /*new Notify(e.getMessage());*/ }
	        catch (IOException ioe) { /*new Notify(ioe.getMessage());*/ }
			
			// For every document that has been previously opened, create a menu entry.
	        for(int curLine = 0; curLine < tempStrList.size() && curLine < maxRecentFiles; curLine++)
	        {
	        	// Add new entry.
	        	addRecentEntry(tempStrList.get(curLine));
				
	        } // for(int curLine = 0; curLine...
	        
		// Populate Recent Documents
		////////////////////////////
	}
	
	//////////////////////////////////////////////////////////////////////////	
	// Adds an entry to recent files menu.
	public void addRecentEntry(String path)
	{
		// Construct strings from path. These are added to the recent docs list and menu items.
		// Current line.
		String[] result = path.split( BBIni.getFileSep() + BBIni.getFileSep() );
		String fileName = path.substring( path.lastIndexOf(BBIni.getFileSep()) + 1, path.length() );
		final String curStr = fileName + "  [" + path + "]";
		final String curStr2 = path;
		
		// Add path to recent document list.
		recentDocsList.add(0, path);
		
		// Create new item under sub menu.
		MenuItem newItem = new MenuItem(subMen, SWT.PUSH, 0);
		// Set its text.
		newItem.setText( curStr );
		
		// Add action!
		newItem.addSelectionListener(new SelectionAdapter()
		{
			// Action to perform when widget selected!
			public void widgetSelected(SelectionEvent e)
			{
				int index= wordProc.getFolder().getSelectionIndex();
				if(index == -1){
					wordProc.addDocumentManager(null);
					wordProc.getList().getFirst().openDocument( curStr2 );
				}
				else {
					
					if(wordProc.getList().get(index).document.getDOM() != null || wordProc.getList().get(index).text.hasChanged || wordProc.getList().get(index).braille.hasChanged || wordProc.getList().get(index).documentName != null)
					{
						wordProc.addDocumentManager( curStr2 );
					}
					else
					{
						wordProc.getList().get(index).openDocument(curStr2);
					}
				}
				
			} // public void widgetSelected(SelectionEvent e) {
			
		}); // newItem.addSelectionListener(new SelectionAdapter() {
	}
	
	//////////////////////////////////////////////////////////////////////////
	// Writes contents of recent file list to disk.
	public void writeRecentsToFile()
	{
		try
		{
			// Open file for writing.
			BufferedWriter bw = new BufferedWriter( new FileWriter( BBIni.getRecentDocs() ) );
			
			// List of recent documents.
			ArrayList<String> recdocs = wordProc.getMainMenu().getRecentDocumentsList();
			
			// Starting index.
			int startIndex = recentDocsList.size() - 1;
			if(startIndex >= maxRecentFiles) startIndex = maxRecentFiles - 1;
			
			// Add the file path.
			for(int curLine = startIndex; curLine >= 0; curLine--) {
				bw.write( recdocs.get(curLine) );
				bw.newLine();
	        }
			
			// Close the file.
			bw.close();
		}
		catch(IOException ioe) { ioe.printStackTrace(); }
		
	} // public void writeRecentToFile()
}
