package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class BBMenu {

Menu menuBar;
MenuItem fileItem;
Menu fileMenu;
MenuItem fileNew;

public BBMenu (Shell documentWindow)
{
menuBar = new Menu (documentWindow, SWT.BAR);
fileItem = new MenuItem (menuBar, SWT.CASCADE);
fileMenu = new Menu (fileItem);
fileNew = new MenuItem (fileMenu, SWT.CASCADE | SWT.PUSH);
fileNew.setText ("new");

}

}

