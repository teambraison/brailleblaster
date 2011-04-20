package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.brailleblaster.localization.LocaleHandler;

public class BBMenu {

private Menu menuBar;
private boolean exited = false;

public BBMenu (final DocumentManager dm) {
LocaleHandler lh = new LocaleHandler();

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
MenuItem newItem = new MenuItem (fileMenu, SWT.PUSH);
newItem.setText (lh.localValue("&New"));
newItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem openItem = new MenuItem (fileMenu, SWT.PUSH);
openItem.setText (lh.localValue("&Open"));
openItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem recentItem = new MenuItem (fileMenu, SWT.PUSH);
recentItem.setText (lh.localValue("&Recent"));
recentItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem importItem = new MenuItem (fileMenu, SWT.PUSH);
importItem.setText (lh.localValue("&Import"));
importItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem saveItem = new MenuItem (fileMenu, SWT.PUSH);
saveItem.setText (lh.localValue("&Save"));
saveItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem saveAsItem = new MenuItem (fileMenu, SWT.PUSH);
saveAsItem.setText (lh.localValue("Savf&As"));
saveAsItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem embosserSetupItem = new MenuItem (fileMenu, SWT.PUSH);
embosserSetupItem.setText (lh.localValue("&EmbosserSetup"));
embosserSetupItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem embosserPreviewItem = new MenuItem (fileMenu, SWT.PUSH);
embosserPreviewItem.setText (lh.localValue("Embosser&Preview"));
embosserPreviewItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem embossInkPreviewItem = new MenuItem (fileMenu, SWT.PUSH);
embossInkPreviewItem.setText (lh.localValue("Emboss&IncPreview"));
embossInkPreviewItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem embossNowItem = new MenuItem (fileMenu, SWT.PUSH);
embossNowItem.setText (lh.localValue("Emboss&Now!"));
embossNowItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem embossInkNowItem = new MenuItem (fileMenu, SWT.PUSH);
embossInkNowItem.setText (lh.localValue("EmbossInkN&ow"));
embossInkNowItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem printPageSetupItem = new MenuItem (fileMenu, SWT.PUSH);
printPageSetupItem.setText (lh.localValue("PrintPageS&etup"));
printPageSetupItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem printPreviewItem = new MenuItem (fileMenu, SWT.PUSH);
printPreviewItem.setText (lh.localValue("PrintP&review"));
printPreviewItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem printItem = new MenuItem (fileMenu, SWT.PUSH);
printItem.setText (lh.localValue("&Print"));
printItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem languageItem = new MenuItem (fileMenu, SWT.PUSH);
languageItem.setText (lh.localValue("&Language"));
languageItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
dm.fileNew();
}
});
MenuItem exitItem = new MenuItem (fileMenu, SWT.PUSH);
exitItem.setText (lh.localValue("e&xit"));
exitItem.addSelectionListener (new SelectionAdapter() {
public void widgetSelected (SelectionEvent e) {
exited = true;
}
});
fileItem.setMenu (fileMenu);

// Set up edit menu
Menu editMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
editItem.setMenu (editMenu);

// Set up view menu
Menu viewMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
viewItem.setMenu (viewMenu);

// Set up translate menu
Menu translateMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
translateItem.setMenu (translateMenu);

// Set up insert menu
Menu insertMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
insertItem.setMenu (insertMenu);

// Set up advanced menu
Menu advancedMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
advancedItem.setMenu (advancedMenu);

// Set up help menu
Menu helpMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
helpItem.setMenu (helpMenu);

// Activate menus when documentWindow shell is opened
dm.documentWindow.setMenuBar (menuBar);
}

public boolean exitSelected() {
return exited;
}

}

