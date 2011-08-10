package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.brailleblaster.localization.LocaleHandler;

class BBMenu {

private Menu menuBar;

public BBMenu (final DocumentManager dm) {
LocaleHandler lh = new LocaleHandler();

// Set up menu bar
menuBar = new Menu (dm.documentWindow, SWT.BAR);
MenuItem fileItem = new MenuItem (menuBar, SWT.CASCADE);
fileItem.setText (lh.localValue("&File"));
MenuItem editItem = new MenuItem (menuBar, SWT.CASCADE);
editItem.setText (lh.localValue("&Edit"));
editItem.setEnabled(false);
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
MenuItem newItem = new MenuItem (fileMenu, SWT.PUSH);
newItem.setText (lh.localValue("&New"));
newItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.sendOpenEvent (WP.NewDocument);
}
});
MenuItem openItem = new MenuItem (fileMenu, SWT.PUSH);
openItem.setText (lh.localValue("&Open"));
openItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
if (dm.action == WP.NewDocument)
dm.fileOpen();
else
dm.sendOpenEvent (WP.OpenDocumentGetFile);
}
});
MenuItem recentItem = new MenuItem (fileMenu, SWT.PUSH);
recentItem.setText (lh.localValue("&Recent"));
recentItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
recentItem.setEnabled(false);
MenuItem importItem = new MenuItem (fileMenu, SWT.PUSH);
importItem.setText (lh.localValue("&Import"));
importItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.sendOpenEvent (WP.ImportDocument);
}
});
MenuItem saveItem = new MenuItem (fileMenu, SWT.PUSH);
saveItem.setText (lh.localValue("&Save"));
saveItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileSave();
}
});
MenuItem saveAsItem = new MenuItem (fileMenu, SWT.PUSH);
saveAsItem.setText (lh.localValue("Save&As"));
saveAsItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileSaveAs();
}
});
MenuItem embosserSetupItem = new MenuItem (fileMenu, SWT.PUSH);
embosserSetupItem.setText (lh.localValue("&EmbosserSetup"));
embosserSetupItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem embosserPreviewItem = new MenuItem (fileMenu, SWT.PUSH);
embosserPreviewItem.setText (lh.localValue("Embosser&Preview"));
embosserPreviewItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem embossInkPreviewItem = new MenuItem (fileMenu, SWT.PUSH);
embossInkPreviewItem.setText (lh.localValue("Emboss&IncPreview"));
embossInkPreviewItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem embossNowItem = new MenuItem (fileMenu, SWT.PUSH);
embossNowItem.setText (lh.localValue("Emboss&Now!"));
embossNowItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileEmbossNow();
}
});
MenuItem embossInkNowItem = new MenuItem (fileMenu, SWT.PUSH);
embossInkNowItem.setText (lh.localValue("EmbossInkN&ow"));
embossInkNowItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem printPageSetupItem = new MenuItem (fileMenu, SWT.PUSH);
printPageSetupItem.setText (lh.localValue("PrintPageS&etup"));
printPageSetupItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem printPreviewItem = new MenuItem (fileMenu, SWT.PUSH);
printPreviewItem.setText (lh.localValue("PrintP&review"));
printPreviewItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem printItem = new MenuItem (fileMenu, SWT.PUSH);
printItem.setText (lh.localValue("&Print"));
printItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem languageItem = new MenuItem (fileMenu, SWT.PUSH);
languageItem.setText (lh.localValue("&Language"));
languageItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem exitItem = new MenuItem (fileMenu, SWT.PUSH);
exitItem.setText (lh.localValue("e&xit"));
exitItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.exitSelected = true;
}
});
fileItem.setMenu (fileMenu);

// Set up edit menu
Menu editMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
MenuItem undoItem = new MenuItem (editMenu, SWT.PUSH);
undoItem.setText (lh.localValue("&Undo"));
undoItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem redoItem = new MenuItem (editMenu, SWT.PUSH);
redoItem.setText (lh.localValue("&Redo"));
redoItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem cutItem = new MenuItem (editMenu, SWT.PUSH);
cutItem.setText (lh.localValue("&Cut"));
cutItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem copyItem = new MenuItem (editMenu, SWT.PUSH);
copyItem.setText (lh.localValue("c&Opy"));
copyItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem pasteItem = new MenuItem (editMenu, SWT.PUSH);
pasteItem.setText (lh.localValue("&Paste"));
pasteItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem searchItem = new MenuItem (editMenu, SWT.PUSH);
searchItem.setText (lh.localValue("&Search"));
searchItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem replaceItem = new MenuItem (editMenu, SWT.PUSH);
replaceItem.setText (lh.localValue("&Replace"));
replaceItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem spellCheckItem = new MenuItem (editMenu, SWT.PUSH);
spellCheckItem.setText (lh.localValue("&SpellCheck"));
spellCheckItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem boldToggleItem = new MenuItem (editMenu, SWT.PUSH);
boldToggleItem.setText (lh.localValue("&BoldToggle"));
boldToggleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem italicToggleItem = new MenuItem (editMenu, SWT.PUSH);
italicToggleItem.setText (lh.localValue("&ItalicToggle"));
italicToggleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem underlineToggleItem = new MenuItem (editMenu, SWT.PUSH);
underlineToggleItem.setText (lh.localValue("&UnderlineToggle"));
underlineToggleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem zoomImageItem = new MenuItem (editMenu, SWT.PUSH);
zoomImageItem.setText (lh.localValue("&ZoomImage"));
zoomImageItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem selectAllItem = new MenuItem (editMenu, SWT.PUSH);
selectAllItem.setText (lh.localValue("&SelectAll"));
selectAllItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem createStyleItem = new MenuItem (editMenu, SWT.PUSH);
createStyleItem.setText (lh.localValue("&CreateStyle"));
createStyleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem nextElementItem = new MenuItem (editMenu, SWT.PUSH);
nextElementItem.setText (lh.localValue("&NexstElement"));
nextElementItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem assocSelectionItem = new MenuItem (editMenu, SWT.PUSH);
assocSelectionItem.setText (lh.localValue("&AssocSelection"));
assocSelectionItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem lockSelectionItem = new MenuItem (editMenu, SWT.PUSH);
lockSelectionItem.setText (lh.localValue("&LockSelection"));
lockSelectionItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem unlockSelectionItem = new MenuItem (editMenu, SWT.PUSH);
unlockSelectionItem.setText (lh.localValue("&UnlockSelection"));
unlockSelectionItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem editLockedItem = new MenuItem (editMenu, SWT.PUSH);
editLockedItem.setText (lh.localValue("&EditLocked"));
editLockedItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem keybdBrlToggleItem = new MenuItem (editMenu, SWT.PUSH);
keybdBrlToggleItem.setText (lh.localValue("&KeybdBrlToggle"));
keybdBrlToggleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem cursorFollowItem = new MenuItem (editMenu, SWT.PUSH);
cursorFollowItem.setText (lh.localValue("&CursorFollow"));
cursorFollowItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem dragCursorItem = new MenuItem (editMenu, SWT.PUSH);
dragCursorItem.setText (lh.localValue("&DragCursor"));
dragCursorItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
editItem.setMenu (editMenu);

// Set up view menu
Menu viewMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
MenuItem increaseFontSizeItem = new MenuItem (viewMenu, SWT.PUSH);
increaseFontSizeItem.setText (lh.localValue("&IncreaseFontSize"));
increaseFontSizeItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem decreaseFontSizeItem = new MenuItem (viewMenu, SWT.PUSH);
decreaseFontSizeItem.setText (lh.localValue("&DecreaseFintSize"));
decreaseFontSizeItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem increaseContrastItem = new MenuItem (viewMenu, SWT.PUSH);
increaseContrastItem.setText (lh.localValue("&IncreaseContrast"));
increaseContrastItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem decreaseContrastItem = new MenuItem (viewMenu, SWT.PUSH);
decreaseContrastItem.setText (lh.localValue("&DecreaseContrast"));
decreaseContrastItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem showOutlineItem = new MenuItem (viewMenu, SWT.PUSH);
showOutlineItem.setText (lh.localValue("&ShowOutline"));
showOutlineItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem braillePresentationItem = new MenuItem (viewMenu, SWT.PUSH);
braillePresentationItem.setText (lh.localValue("&BraillePresentation"));
braillePresentationItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem formatLikeBrailleItem = new MenuItem (viewMenu, SWT.PUSH);
formatLikeBrailleItem.setText (lh.localValue("&FormatLikeBraille"));
formatLikeBrailleItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem showPageBreaksItem = new MenuItem (viewMenu, SWT.PUSH);
showPageBreaksItem.setText (lh.localValue("&ShowPageBreaks"));
showPageBreaksItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
viewItem.setMenu (viewMenu);

// Set up translate menu
Menu translateMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
MenuItem xtranslateItem = new MenuItem (translateMenu, SWT.PUSH);
xtranslateItem.setText (lh.localValue("&Translate"));
xtranslateItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.translate();
}
});
MenuItem translationTemplatesItem = new MenuItem (translateMenu, 
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
MenuItem inLineMathItem = new MenuItem (insertMenu, SWT.PUSH);
inLineMathItem.setText (lh.localValue("&InLineMath"));
inLineMathItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem displayedMathItem = new MenuItem (insertMenu, SWT.PUSH);
displayedMathItem.setText (lh.localValue("&DisplayedMath"));
displayedMathItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem inLineGraphicItem = new MenuItem (insertMenu, SWT.PUSH);
inLineGraphicItem.setText (lh.localValue("&InLineGraphic"));
inLineGraphicItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem displayedGraphicItem = new MenuItem (insertMenu, SWT.PUSH);
displayedGraphicItem.setText (lh.localValue("&DisplayedGraphic"));
displayedGraphicItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem tableItem = new MenuItem (insertMenu, SWT.PUSH);
tableItem.setText (lh.localValue("&Table"));
tableItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
insertItem.setMenu (insertMenu);

// Set up advanced menu
Menu advancedMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
MenuItem brlFormatItem = new MenuItem (advancedMenu, SWT.PUSH);
brlFormatItem.setText (lh.localValue("&BrailleFormat"));
brlFormatItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem brailleASCIIItem = new MenuItem (advancedMenu, SWT.PUSH);
brailleASCIIItem.setText (lh.localValue("&brailleASCIITable"));
brailleASCIIItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem showTranslationTemplatesItem = new MenuItem (advancedMenu, 
SWT.PUSH);
showTranslationTemplatesItem.setText 
(lh.localValue("&ShowTranslationTemplates"));
showTranslationTemplatesItem.addSelectionListener (new 
SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem showFormatTemplatesItem = new MenuItem (advancedMenu, 
SWT.PUSH);
showFormatTemplatesItem.setText (lh.localValue("&ShowFormatTemplates"));
showFormatTemplatesItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
advancedItem.setMenu (advancedMenu);

// Set up help menu
Menu helpMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
MenuItem readManualItem = new MenuItem (helpMenu, SWT.PUSH);
readManualItem.setText (lh.localValue("&ReadManual"));
readManualItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem helpInfoItem = new MenuItem (helpMenu, SWT.PUSH);
helpInfoItem.setText (lh.localValue("&helpInfo"));
helpInfoItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem tutorialsItem = new MenuItem (helpMenu, SWT.PUSH);
tutorialsItem.setText (lh.localValue("&Tutorials"));
tutorialsItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem checkUpdatesItem = new MenuItem (helpMenu, SWT.PUSH);
checkUpdatesItem.setText (lh.localValue("&CheckUpdates"));
checkUpdatesItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
MenuItem aboutItem = new MenuItem (helpMenu, SWT.PUSH);
aboutItem.setText (lh.localValue("&About"));
aboutItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.placeholder();
}
});
helpItem.setMenu (helpMenu);

// Activate menus when documentWindow shell is opened
dm.documentWindow.setMenuBar (menuBar);
}

}

