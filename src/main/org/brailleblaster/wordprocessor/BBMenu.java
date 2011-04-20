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
fileItem.setText (lh.localValue("&file"));
MenuItem editItem = new MenuItem (menuBar, SWT.CASCADE);
editItem.setText (lh.localValue("&Edit"));
MenuItem viewItem = new MenuItem (menuBar, SWT.CASCADE);
viewItem.setText (lh.localValue("&View"));
MenuItem translationItem = new MenuItem (menuBar, SWT.CASCADE);
translationItem.setText (lh.localValue("&Translation"));
MenuItem insertItem = new MenuItem (menuBar, SWT.CASCADE);
insertItem.setText (lh.localValue("&Insert"));
MenuItem advancedItem = new MenuItem (menuBar, SWT.CASCADE);
advancedItem.setText (lh.localValue("&Advanced"));
MenuItem helpItem = new MenuItem (menuBar, SWT.CASCADE);
helpItem.setText (lh.localValue("&Help"));

// Set up file menu
Menu fileMenu = new Menu (dm.documentWindow, SWT.DROP_DOWN);
MenuItem newItem = new MenuItem (fileMenu, SWT.PUSH);
newItem.setText (lh.localValue("&new"));
newItem.addSelectionListener (new SelectionAdapter() {
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

// Set up view menu

// Set up translation menu

// Set up insert menu

// Set up advanced menu

// Set up help menu

// Activate menus when documentWindow shell is opened
dm.documentWindow.setMenuBar (menuBar);
}

public boolean exitSelected() {
return exited;
}

}

