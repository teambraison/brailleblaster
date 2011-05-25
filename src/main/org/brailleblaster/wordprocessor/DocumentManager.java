package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.FileDialog;
import org.brailleblaster.BBIni;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.printing.*;

/**
* This class manages each document in an MDI environment. It controls the 
* braille View and the daisy View.
*/

public class DocumentManager {

final Display display;
final Shell documentWindow;
final FormLayout layout;
private String documentName = "untitled";
private BBToolBar toolBar;
private BBMenu menu;
final DaisyView daisy;
final BrailleView braille;
final BBStatusBar statusBar;
boolean exitSelected = false;

public DocumentManager (Display display) {
this.display = display;
documentWindow = new Shell (display, SWT.SHELL_TRIM);
layout = new FormLayout();
documentWindow.setLayout (layout);
documentWindow.setText ("BrailleBlaster " + documentName);
menu = new BBMenu (this);
toolBar = new BBToolBar (this);
daisy = new DaisyView (documentWindow);
braille = new BrailleView (documentWindow);
statusBar = new BBStatusBar (documentWindow);
documentWindow.setSize (900, 600);
documentWindow.open();
//checkLiblouisutdml();
while (!documentWindow.isDisposed() && !exitSelected) {
if (!display.readAndDispatch())
display.sleep();
}
}

void checkLiblouisutdml () {
if (BBIni.haveLiblouisutdml()) {
return;
}
Shell shell = new Shell (display, SWT.DIALOG_TRIM);
MessageBox mb = new MessageBox (shell, SWT.YES | SWT.NO);
mb.setMessage ("The Braille facility is missing."
+ " Do you wish to continue?");
int result = mb.open();
if (result == SWT.NO) {
exitSelected = true;
}
shell.dispose();
}

void newDocument() {
}

void fileNew() {
}

void fileOpen () {
Shell shell = new Shell (display);
FileDialog dialog = new FileDialog (shell, SWT.OPEN);
dialog.open();
shell.dispose();
}

void fileSave() {
Shell shell = new Shell (display);
FileDialog dialog = new FileDialog (shell, SWT.SAVE);
dialog.open();
shell.dispose();
}

}

