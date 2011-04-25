package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.FileDialog;

/**
* This class manages each document in an MDI environment. It controls the 
* braille View and the daisy View.
*/

public class DocumentManager {

final Shell documentWindow;
final FormLayout layout;
private String documentName = "untitled";
private BBToolBar toolBar;
private BBMenu menu;
final DaisyView daisy;
final BrailleView braille;
final BBStatusBar statusBar;

public DocumentManager (Display display) {
documentWindow = new Shell (display);
layout = new FormLayout();
documentWindow.setLayout (layout);
documentWindow.setText ("BrailleBlaster " + documentName);
toolBar = new BBToolBar (documentWindow);
menu = new BBMenu (this);
braille = new BrailleView (documentWindow);
daisy = new DaisyView (documentWindow);
statusBar = new BBStatusBar (documentWindow);
documentWindow.setSize (800, 600);
documentWindow.open();
while (!documentWindow.isDisposed() && !menu.exitSelected()) {
if (!display.readAndDispatch())
display.sleep();
}
}

void fileNew() {
FileDialog dialog = new FileDialog (documentWindow, SWT.OPEN);
dialog.open();
}

  }

