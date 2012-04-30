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

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.BBIni;
import org.brailleblaster.settings.SettingsDialog;

class BBMenu {

/**
* This class contains all the menus.
*/

final Menu menuBar;

/* All the menu items are member fields so they can be accessed outside 
* the constructor. This might be done for example with setEnabled(false) 
* to indicate that a menu item is unavailable.
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
MenuItem createStyleItem;
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

BBMenu (final DocumentManager dm) {
LocaleHandler lh = new LocaleHandler();

/* Note that the values in the setText methods are keys for 
* localization. They are not intended to be seen by the user. 
* Capitalization should follow the convention for names in Java, but 
* this is not always consistent. Values, with proper capitalization, are 
* shown in the files in the dist/programData/lang subdirectory.
*/

// Set up menu bar
menuBar = new Menu (dm.documentWindow, SWT.BAR);
MenuItem fileItem = new MenuItem (menuBar, SWT.CASCADE);
fileItem.setText (lh.localValue("&File"));
MenuItem editItem = new MenuItem (menuBar, SWT.CASCADE);
editItem.setText (lh.localValue("&Edit"));
MenuItem viewItem = new MenuItem (menuBar, SWT.CASCADE);
viewItem.setText (lh.localValue("&View"));
MenuItem translateItem = new MenuItem (menuBar, SWT.CASCADE);
translateItem.setText (lh.localValue("&Translate"));
MenuItem insertItem = new MenuItem (menuBar, SWT.CASCADE);
insertItem.setText (lh.localValue("&Insert"));
MenuItem advancedItem = new MenuItem (menuBar, SWT.CASCADE);
advancedItem.setText (lh.localValue("&Advanced"));
MenuItem helpItem = new MenuItem (menuBar, SWT.CASCADE);
helpItem.setText (lh.localValue("&Help"));

// Set up file menu
Menu fileMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
newItem = new MenuItem (fileMenu, SWT.PUSH);
newItem.setText (lh.localValue("&New"));
newItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
if (BBIni.debugging()) {
dm.setReturn (WP.NewDocument);
}
else {
dm.fileNew();
}
}
});
openItem = new MenuItem (fileMenu, SWT.PUSH);
openItem.setText (lh.localValue("&Open"));
openItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
if (BBIni.debugging()) {
dm.setReturn (WP.OpenDocumentGetFile);
} else {
dm.fileOpen();
}
}
});
recentItem = new MenuItem (fileMenu, SWT.PUSH);
recentItem.setText (lh.localValue("&Recent"));
recentItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
importItem = new MenuItem (fileMenu, SWT.PUSH);
importItem.setText (lh.localValue("&Import"));
importItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
saveItem = new MenuItem (fileMenu, SWT.PUSH);
saveItem.setText (lh.localValue("&Save"));
saveItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
saveAsItem = new MenuItem (fileMenu, SWT.PUSH);
saveAsItem.setText (lh.localValue("Save&As"));
saveAsItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileSaveAs();
}
});
embosserSetupItem = new MenuItem (fileMenu, SWT.PUSH);
embosserSetupItem.setText (lh.localValue("&EmbosserSetup"));
embosserSetupItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
embosserPreviewItem = new MenuItem (fileMenu, SWT.PUSH);
embosserPreviewItem.setText (lh.localValue("Embosser&Preview"));
embosserPreviewItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
embossInkPreviewItem = new MenuItem (fileMenu, SWT.PUSH);
embossInkPreviewItem.setText (lh.localValue("Emboss&InkPreview"));
embossInkPreviewItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
embossNowItem = new MenuItem (fileMenu, SWT.PUSH);
embossNowItem.setText (lh.localValue("Emboss&Now!"));
embossNowItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileEmbossNow();
}
});
embossInkNowItem = new MenuItem (fileMenu, SWT.PUSH);
embossInkNowItem.setText (lh.localValue("EmbossInkN&ow"));
embossInkNowItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
printPageSetupItem = new MenuItem (fileMenu, SWT.PUSH);
printPageSetupItem.setText (lh.localValue("PrintPageS&etup"));
printPageSetupItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
printPreviewItem = new MenuItem (fileMenu, SWT.PUSH);
printPreviewItem.setText (lh.localValue("PrintP&review"));
printPreviewItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
printItem = new MenuItem (fileMenu, SWT.PUSH);
printItem.setText (lh.localValue("&Print"));
printItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
languageItem = new MenuItem (fileMenu, SWT.PUSH);
languageItem.setText (lh.localValue("&Language"));
languageItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
closeItem = new MenuItem (fileMenu, SWT.PUSH);
closeItem.setText (lh.localValue("&close"));
closeItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
if (BBIni.debugging()) {
dm.setReturn (WP.DocumentClosed);
}
else {
dm.setReturn (WP.BBClosed);
}
}
});
if (!BBIni.getPlatformName().equals("cocoa")) {
exitItem = new MenuItem (fileMenu, SWT.PUSH);
exitItem.setText (lh.localValue("e&xit"));
exitItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.setReturn (WP.BBClosed);
}
});
}
fileItem.setMenu (fileMenu);

// Set up edit menu
Menu editMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
undoItem = new MenuItem (editMenu, SWT.PUSH);
undoItem.setText (lh.localValue("&Undo"));
undoItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
if (BBIni.debugging()) {
dm.setReturn (WP.SwitchDocuments);
} else {
dm.placeholder();
}
}
});
redoItem = new MenuItem (editMenu, SWT.PUSH);
redoItem.setText (lh.localValue("&Redo"));
redoItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
cutItem = new MenuItem (editMenu, SWT.PUSH);
cutItem.setText (lh.localValue("&Cut"));
cutItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
copyItem = new MenuItem (editMenu, SWT.PUSH);
copyItem.setText (lh.localValue("c&Opy"));
copyItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
pasteItem = new MenuItem (editMenu, SWT.PUSH);
pasteItem.setText (lh.localValue("&Paste"));
pasteItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
searchItem = new MenuItem (editMenu, SWT.PUSH);
searchItem.setText (lh.localValue("&Search"));
searchItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
replaceItem = new MenuItem (editMenu, SWT.PUSH);
replaceItem.setText (lh.localValue("&Replace"));
replaceItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
spellCheckItem = new MenuItem (editMenu, SWT.PUSH);
spellCheckItem.setText (lh.localValue("&SpellCheck"));
spellCheckItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
boldToggleItem = new MenuItem (editMenu, SWT.PUSH);
boldToggleItem.setText (lh.localValue("&BoldToggle"));
boldToggleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
italicToggleItem = new MenuItem (editMenu, SWT.PUSH);
italicToggleItem.setText (lh.localValue("&ItalicToggle"));
italicToggleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
underlineToggleItem = new MenuItem (editMenu, SWT.PUSH);
underlineToggleItem.setText (lh.localValue("&UnderlineToggle"));
underlineToggleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
zoomImageItem = new MenuItem (editMenu, SWT.PUSH);
zoomImageItem.setText (lh.localValue("&ZoomImage"));
zoomImageItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
selectAllItem = new MenuItem (editMenu, SWT.PUSH);
selectAllItem.setText (lh.localValue("&SelectAll"));
selectAllItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
createStyleItem = new MenuItem (editMenu, SWT.PUSH);
createStyleItem.setText (lh.localValue("&CreateStyle"));
createStyleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
nextElementItem = new MenuItem (editMenu, SWT.PUSH);
nextElementItem.setText (lh.localValue("&NexstElement"));
nextElementItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
assocSelectionItem = new MenuItem (editMenu, SWT.PUSH);
assocSelectionItem.setText (lh.localValue("&AssocSelection"));
assocSelectionItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
lockSelectionItem = new MenuItem (editMenu, SWT.PUSH);
lockSelectionItem.setText (lh.localValue("&LockSelection"));
lockSelectionItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
unlockSelectionItem = new MenuItem (editMenu, SWT.PUSH);
unlockSelectionItem.setText (lh.localValue("&UnlockSelection"));
unlockSelectionItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
editLockedItem = new MenuItem (editMenu, SWT.PUSH);
editLockedItem.setText (lh.localValue("&EditLocked"));
editLockedItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
keybdBrlToggleItem = new MenuItem (editMenu, SWT.PUSH);
keybdBrlToggleItem.setText (lh.localValue("&KeybdBrlToggle"));
keybdBrlToggleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
cursorFollowItem = new MenuItem (editMenu, SWT.PUSH);
cursorFollowItem.setText (lh.localValue("&CursorFollow"));
cursorFollowItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
dragCursorItem = new MenuItem (editMenu, SWT.PUSH);
dragCursorItem.setText (lh.localValue("&DragCursor"));
dragCursorItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
editItem.setMenu (editMenu);

// Set up view menu
Menu viewMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
increaseFontSizeItem = new MenuItem (viewMenu, SWT.PUSH);
increaseFontSizeItem.setText (lh.localValue("&IncreaseFontSize"));
increaseFontSizeItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
decreaseFontSizeItem = new MenuItem (viewMenu, SWT.PUSH);
decreaseFontSizeItem.setText (lh.localValue("&DecreaseFintSize"));
decreaseFontSizeItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
increaseContrastItem = new MenuItem (viewMenu, SWT.PUSH);
increaseContrastItem.setText (lh.localValue("&IncreaseContrast"));
increaseContrastItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
decreaseContrastItem = new MenuItem (viewMenu, SWT.PUSH);
decreaseContrastItem.setText (lh.localValue("&DecreaseContrast"));
decreaseContrastItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
showOutlineItem = new MenuItem (viewMenu, SWT.PUSH);
showOutlineItem.setText (lh.localValue("&ShowOutline"));
showOutlineItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
braillePresentationItem = new MenuItem (viewMenu, SWT.PUSH);
braillePresentationItem.setText (lh.localValue("&BraillePresentation"));
braillePresentationItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
formatLikeBrailleItem = new MenuItem (viewMenu, SWT.PUSH);
formatLikeBrailleItem.setText (lh.localValue("&FormatLikeBraille"));
formatLikeBrailleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
showPageBreaksItem = new MenuItem (viewMenu, SWT.PUSH);
showPageBreaksItem.setText (lh.localValue("&ShowPageBreaks"));
showPageBreaksItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
viewItem.setMenu (viewMenu);

// Set up translate menu
Menu translateMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
xtranslateItem = new MenuItem (translateMenu, SWT.PUSH);
xtranslateItem.setText (lh.localValue("&Translate"));
xtranslateItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.translate();
}
});
backTranslateItem = new MenuItem (translateMenu, SWT.PUSH);
backTranslateItem.setText 
(lh.localValue("&BackTranslate"));
backTranslateItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
translationTemplatesItem = new MenuItem (translateMenu, 
SWT.PUSH);
translationTemplatesItem.setText 
(lh.localValue("&TranslationTemplates"));
translationTemplatesItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
translateItem.setMenu (translateMenu);

// Set up insert menu
Menu insertMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
inLineMathItem = new MenuItem (insertMenu, SWT.PUSH);
inLineMathItem.setText (lh.localValue("&InLineMath"));
inLineMathItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
displayedMathItem = new MenuItem (insertMenu, SWT.PUSH);
displayedMathItem.setText (lh.localValue("&DisplayedMath"));
displayedMathItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
inLineGraphicItem = new MenuItem (insertMenu, SWT.PUSH);
inLineGraphicItem.setText (lh.localValue("&InLineGraphic"));
inLineGraphicItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
displayedGraphicItem = new MenuItem (insertMenu, SWT.PUSH);
displayedGraphicItem.setText (lh.localValue("&DisplayedGraphic"));
displayedGraphicItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
tableItem = new MenuItem (insertMenu, SWT.PUSH);
tableItem.setText (lh.localValue("&Table"));
tableItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
insertItem.setMenu (insertMenu);

// Set up advanced menu
Menu advancedMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
brlFormatItem = new MenuItem (advancedMenu, SWT.PUSH);
brlFormatItem.setText (lh.localValue("&BrailleFormat"));
brlFormatItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
brailleASCIIItem = new MenuItem (advancedMenu, SWT.PUSH);
brailleASCIIItem.setText (lh.localValue("&brailleASCIITable"));
brailleASCIIItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
showTranslationTemplatesItem = new MenuItem (advancedMenu, 
SWT.PUSH);
showTranslationTemplatesItem.setText 
(lh.localValue("&ShowTranslationTemplates"));
showTranslationTemplatesItem.addSelectionListener (new 
SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
showFormatTemplatesItem = new MenuItem (advancedMenu, 
SWT.PUSH);
showFormatTemplatesItem.setText (lh.localValue("&ShowFormatTemplates"));
showFormatTemplatesItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
changeSettingsItem = new MenuItem (advancedMenu, 
SWT.PUSH);
changeSettingsItem.setText (lh.localValue("&changeSettings"));
changeSettingsItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
new SettingsDialog().open();
}
});
advancedItem.setMenu (advancedMenu);

// Set up help menu
Menu helpMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
aboutItem = new MenuItem (helpMenu, SWT.PUSH);
aboutItem.setText (lh.localValue("&About"));
aboutItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
new UserHelp(WP.AboutBB);
}
});
helpInfoItem = new MenuItem (helpMenu, SWT.PUSH);
helpInfoItem.setText (lh.localValue("&helpInfo"));
helpInfoItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
new UserHelp(WP.HelpInfo);
}
});
tutorialsItem = new MenuItem (helpMenu, SWT.PUSH);
tutorialsItem.setText (lh.localValue("&Tutorials"));
tutorialsItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
new UserHelp(WP.ReadTutorial);
}
});
readManualItem = new MenuItem (helpMenu, SWT.PUSH);
readManualItem.setText (lh.localValue("&ReadManuals"));
readManualItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
new UserHelp(WP.ReadManuals);
}
});
checkUpdatesItem = new MenuItem (helpMenu, SWT.PUSH);
checkUpdatesItem.setText (lh.localValue("&CheckUpdates"));
checkUpdatesItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
new UserHelp (WP.CheckUpdates);
}
});
helpItem.setMenu (helpMenu);

// Activate menus when documentWindow shell is opened
dm.documentWindow.setMenuBar (menuBar);
}

}

