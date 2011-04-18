package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

/**
* This class manages each document in an MDI environment. It controls the 
* braille View and the daisy View.
*/

public class DocumentManager {

private Shell documentWindow;
private String documentName = "untitled";
private BBToolBar toolBar;
private BBMenu menu;
private DaisyView daisy;
private BrailleView braille;
private BBStatusBar statusBar;

public DocumentManager (Display display) {
documentWindow = new Shell (display);
documentWindow.setText ("BrailleBlaster " + documentName);
toolBar = new BBToolBar (documentWindow);
menu = new BBMenu (documentWindow);
braille = new BrailleView (documentWindow);
daisy = new DaisyView (documentWindow);
statusBar = new BBStatusBar (documentWindow);
documentWindow.open();
while (!documentWindow.isDisposed() && !menu.exitSelected()) {
if (!display.readAndDispatch())
display.sleep();
}
}

}

