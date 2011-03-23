package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
* This class manages each document in an MDI environment. It controls the 
* braille View and the daisy View.
*/

public class DocumentManager {

Shell documentWindow;
String documentName = "untitled";
BBToolBar toolBar;
BBMenu menu;
DaisyView daisy;
BrailleView braille;
BBStatusBar statusBar;

public DocumentManager (Display display) {
documentWindow = new Shell (display);
documentWindow.open();
documentWindow.setText ("BrailleBlaster " + documentName);
toolBar = new BBToolBar (documentWindow);
menu = new BBMenu (documentWindow);
braille = new BrailleView (documentWindow);
daisy = new DaisyView (documentWindow);
statusBar = new BBStatusBar (documentWindow);
while (!documentWindow.isDisposed()) {
if (!display.readAndDispatch())
display.sleep();
}
}

}

