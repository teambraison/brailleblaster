package org.brailleblaster.perspectives.braille.ui;

import org.brailleblaster.BBIni;
import org.brailleblaster.imagedescriber.ImageDescriber;
import org.brailleblaster.imagedescriber.ImageDescriberDialog;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.settings.ConfigFileDialog;
import org.brailleblaster.settings.SettingsDialog;
import org.brailleblaster.wordprocessor.BBMenu;
import org.brailleblaster.wordprocessor.FontManager;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class BrailleMenu extends BBMenu{

	Manager currentEditor;
	
	MenuItem newItem;
	MenuItem openItem;
	MenuItem importItem;
	MenuItem saveItem;
	MenuItem saveAsItem;
	MenuItem embosserSetupItem;
	MenuItem embossNowItem;
	MenuItem printItem;
	MenuItem printPreviewItem;
	MenuItem closeItem;
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
	MenuItem assocSelectionItem;
	MenuItem lockSelectionItem;
	MenuItem unlockSelectionItem;
	MenuItem editLockedItem;
	MenuItem keybdBrlToggleItem;
	// MenuItem imgDescItem;
	MenuItem prevElementItem;
	MenuItem nextElementItem;
	MenuItem refreshItem;
	MenuItem increaseFontSizeItem;
	MenuItem decreaseFontSizeItem;
	MenuItem increaseContrastItem;
	MenuItem decreaseContrastItem;
	MenuItem showOutlineItem;
	MenuItem xtranslateItem;
	MenuItem backTranslateItem;
	MenuItem translationTemplatesItem;
	MenuItem inLineMathItem;
	MenuItem displayedMathItem;
	MenuItem inLineGraphicItem;
	MenuItem displayedGraphicItem;
	MenuItem tableItem;
	MenuItem brlFormatItem;
	MenuItem showTranslationTemplatesItem;
	MenuItem showFormatTemplatesItem;
	MenuItem changeSettingsItem;
	
	public BrailleMenu(final WPManager wp, Manager editor) {
		super(wp);

		setCurrent(editor);
		MenuItem editItem = new MenuItem(menuBar, SWT.CASCADE, 1);
		editItem.setText(lh.localValue("&Edit"));
		MenuItem navigateItem = new MenuItem(menuBar, SWT.CASCADE, 2);
		navigateItem.setText(lh.localValue("&Navigate"));
	
		MenuItem translateItem = new MenuItem(menuBar, SWT.CASCADE, 4);
		translateItem.setText(lh.localValue("&Braille"));
		MenuItem insertItem = new MenuItem(menuBar, SWT.CASCADE, 5);
		insertItem.setText(lh.localValue("&Insert"));
		insertItem.setEnabled(false); /* FO */

		MenuItem advancedItem = new MenuItem(menuBar, SWT.CASCADE, 6);
		advancedItem.setText(lh.localValue("&Advanced"));
		advancedItem.setEnabled(false);
		
		newItem = new MenuItem(fileMenu, SWT.PUSH, 0);
		newItem.setText(lh.localValue("&New") + "\tCtrl + N");
		newItem.setAccelerator(SWT.MOD1 + 'N');
		newItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {

				} else {
					System.out.println("New Document Tab created");
					wp.addDocumentManager(null);
				}
			}
		});
		
		openItem = new MenuItem(fileMenu, SWT.PUSH, 1);
		openItem.setText(lh.localValue("&Open") + "\tCtrl + O");
		openItem.setAccelerator(SWT.MOD1 + 'O');
		openItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {

				} 
				else {
					int index= wp.getFolder().getSelectionIndex();
					if(index == -1){
						wp.addDocumentManager(null);
						((Manager)wp.getList().getFirst()).fileOpenDialog();
						if(((Manager)wp.getList().getFirst()).getWorkingPath() == null)
							((Manager)wp.getList().getFirst()).close();					
					}
					else {
						//wp.getList().get(index).fileOpenDialog();
						currentEditor.fileOpenDialog();
					}
				}
			}
		});
		/*
		importItem = new MenuItem(fileMenu, SWT.PUSH, 3);
		importItem.setText(lh.localValue("&Import"));
		importItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//dm.importDocument();
			}
		});
		*/
		saveItem = new MenuItem(fileMenu, SWT.PUSH, 3);
		saveItem.setText(lh.localValue("&Save") + "\tCtrl + S");
		saveItem.setAccelerator(SWT.MOD1 + 'S');
		saveItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count= wp.getFolder().getItemCount();
				//if(index != -1){
					//wp.getList().get(index).fileSave();
				//}
				if(count > 0)
					currentEditor.fileSave();
			}
		});
		saveAsItem = new MenuItem(fileMenu, SWT.PUSH, 4);
		saveAsItem.setText(lh.localValue("Save&As") + "\tF12");
		saveAsItem.setAccelerator(SWT.F12);
		saveAsItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
				//if(index != -1){
					//wp.getList().get(index).saveAs();
				//}
				if(count > 0)
					currentEditor.saveAs();
			}
		});
		
		
		embosserSetupItem = new MenuItem(fileMenu, SWT.PUSH, 5);
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
		
		embossNowItem = new MenuItem(fileMenu, SWT.PUSH, 6);
		embossNowItem.setText(lh.localValue("E&mboss&Now!"));
		embossNowItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
				//if(index != -1){
				//	wp.getList().get(index).fileEmbossNow();
				//}
				if(count > 0)
					currentEditor.fileEmbossNow();
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
	
*/	
		printPreviewItem = new MenuItem(fileMenu, SWT.PUSH, 7);
		printPreviewItem.setText(lh.localValue("&BraillePreview") + "\tAlt + HOME");
		printPreviewItem.setAccelerator(SWT.MOD3 + SWT.HOME);
		printPreviewItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
			//	if(index != -1){
			//		wp.getList().get(index).printPreview();
			//	}
				if(count > 0)
					currentEditor.printPreview();
			}
		});
		
		printItem = new MenuItem(fileMenu, SWT.PUSH, 8);
		printItem.setText(lh.localValue("&Print") + "\tCtrl + P");
		printItem.setAccelerator(SWT.MOD1 + 'p');
		printItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
				//if(index != -1){
					//wp.getList().get(index).textPrint();
				//}
				if(count > 0)
					currentEditor.textPrint();
			}
		});
		
		closeItem = new MenuItem(fileMenu, SWT.PUSH, 10);
		closeItem.setText(lh.localValue("&Close") + "\tCtrl + W");
		closeItem.setAccelerator(SWT.MOD1 + 'W');
		closeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
				//wp.getList().get(index).close();
				//wp.getList().remove(index);
				
				Controller temp = currentEditor;
				wp.removeController(temp);
				
				if(count > 0)
					temp.close();

				
	
				if(wp.getList().size() == 0)
					setCurrent(null);
			}
		});
		
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
		cutItem.setText(lh.localValue("&Cut") + "\tCtrl + X");
		cutItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getSelectionIndex();
				//if(index != -1)
					//wp.getList().get(index).getText().copy();
				if(count > 0)
					currentEditor.getText().cut();
			}
		});
		copyItem = new MenuItem(editMenu, SWT.PUSH);
		copyItem.setText(lh.localValue("&Copy") + "\tCtrl + C");
		copyItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
				//if(index != -1)
					//wp.getList().get(index).getText().copy();
				if(count > 0)
					currentEditor.getText().copy();
			}
		});
		pasteItem = new MenuItem(editMenu, SWT.PUSH);
		pasteItem.setText(lh.localValue("&Paste") + "\tCtrl + V");
		pasteItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
				//if(index != -1)
					//wp.getList().get(index).getText().paste();
				if(count > 0)
					currentEditor.getText().paste();
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
		selectAllItem.setText(lh.localValue("&SelectAll") + "\tCtrl + A");
		//selectAllItem.setEnabled(false);
		selectAllItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
				//if(index != -1){
				//	wp.getList().get(index).getText().selectAll(wp.getList().get(index));
				//}
				if(count > 0)
					currentEditor.getText().selectAll(currentEditor);
			}
		});
		stylePanelItem = new MenuItem(editMenu, SWT.PUSH);
		stylePanelItem.setText(lh.localValue("&StylePanel"));
		stylePanelItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {

				} 
				else {
					int count = wp.getFolder().getItemCount();
			//		if(index != -1){
			//			wp.getList().get(index).toggleAttributeEditor();
			//		}
					if(count > 0)
						currentEditor.toggleAttributeEditor();
				}
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
		//editItem.setMenu(editMenu);
		//editItem.setEnabled(true);
		
		/*
		// Image Description Menu Item.
		imgDescItem = new MenuItem(editMenu, SWT.PUSH);
		imgDescItem.setText(lh.localValue("&Image Describer"));
		imgDescItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
						
				// Current Doc Manager.
				Manager curDm = null;
						
				if (BBIni.debugging()) {
							
				} 
				else {
					int index= wp.getFolder().getSelectionIndex();
					if(index == -1){
						wp.addDocumentManager(null);
						curDm = ((Manager)wp.getList().getFirst());
					}
					else {
						curDm = currentEditor;
					}
				}
						
				// Run Image Describer on current document.
				if(curDm.document.getDOM() != null) {
//					ImageDescriber imgDesc = new ImageDescriber(curDm);
					new ImageDescriberDialog(wordProc.getShell(), SWT.NONE, wordProc);
				}
			}
		});
		*/
		editItem.setMenu(editMenu);
		editItem.setEnabled(true);
				
		// Set up navigation menu
		Menu navigateMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		prevElementItem = new MenuItem(navigateMenu, SWT.PUSH);
		prevElementItem.setText(lh.localValue("&PreviousElement") + "\tCtrl + Up");
		prevElementItem.setAccelerator(SWT.MOD1 + SWT.ARROW_UP);
		prevElementItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
			//if(index != -1)
				//wp.getList().get(index).prevElement();
				if(count > 0)
					currentEditor.prevElement();
			}
		});

		nextElementItem = new MenuItem(navigateMenu, SWT.PUSH);
		nextElementItem.setText(lh.localValue("&NexstElement") + "\tCtrl + Down");
		nextElementItem.setAccelerator(SWT.MOD1 + SWT.ARROW_DOWN);
		nextElementItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
			//if(index != -1)
				//wp.getList().get(index).nextElement();
				if(count > 0)
					currentEditor.nextElement();
			}
		});
				
		navigateItem.setMenu(navigateMenu);
		
		
				
		refreshItem = new MenuItem(viewMenu, SWT.PUSH);
		refreshItem.setText("Refresh\tF5");
		refreshItem.setAccelerator(SWT.F5);
		refreshItem.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
			//if(index  > -1)
				//wp.getList().get(index).refresh();
				if(count > 0)
					currentEditor.refresh();
			}
		});
		increaseFontSizeItem = new MenuItem(viewMenu, SWT.PUSH);
		increaseFontSizeItem.setText(lh.localValue("&IncreaseFontSize") + "\tCtrl + '+'");
		increaseFontSizeItem.setAccelerator(SWT.MOD1 + '+');
		increaseFontSizeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
				if(count > 0)
					FontManager.increaseFont(wp, currentEditor);
			}
		});
		decreaseFontSizeItem = new MenuItem(viewMenu, SWT.PUSH);
		decreaseFontSizeItem.setText(lh.localValue("&DecreaseFontSize") + "\tCtrl + '-'");
		decreaseFontSizeItem.setAccelerator(SWT.MOD1 + '-');
		decreaseFontSizeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = wp.getFolder().getItemCount();
				if(count > 0)
					FontManager.decreaseFont(wp, currentEditor);
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
				
		// Set up translate menu
		Menu brailleMenu = new Menu(wp.getShell(), SWT.DROP_DOWN);
		xtranslateItem = new MenuItem(brailleMenu, SWT.PUSH);
		xtranslateItem.setText(lh.localValue("&Translate"));
		xtranslateItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
					int count = wp.getFolder().getItemCount();
				//if(index != -1)
					//wp.getList().get(index).refresh();
					if(count > 0)
						currentEditor.refresh();
				}
			});
			backTranslateItem = new MenuItem(brailleMenu, SWT.PUSH);
			backTranslateItem.setText(lh.localValue("&BackTranslate"));
			backTranslateItem.setEnabled(false);
			backTranslateItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					//dm.placeholder();
				}
			});
			translationTemplatesItem = new MenuItem(brailleMenu, SWT.PUSH);
			translationTemplatesItem.setText(lh.localValue("&TranslationTemplates"));
			translationTemplatesItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					new ConfigFileDialog(wp.getShell(), SWT.NONE, wordProc);
				}
			});
			translateItem.setMenu(brailleMenu);
				
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
		/*		KC: Removing this for now since no one knows what it is for.
		 * brailleASCIIItem = new MenuItem(advancedMenu, SWT.PUSH);
				brailleASCIIItem.setText(lh.localValue("&brailleASCIITable"));
				brailleASCIIItem.setEnabled(false);
				brailleASCIIItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						//dm.placeholder();
					}
				});
		*/
			showTranslationTemplatesItem = new MenuItem(advancedMenu, SWT.PUSH);
			showTranslationTemplatesItem.setText(lh.localValue("&ShowTranslationTemplates"));
			showTranslationTemplatesItem.setEnabled(false);
			showTranslationTemplatesItem.addSelectionListener(new SelectionAdapter() {
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
			advancedItem.setEnabled(true);
	}

	public void setCurrent(Controller controller){
		currentEditor = (Manager)controller;
	}

	
	public Controller getCurrent() {
		return currentEditor;
	}
}
