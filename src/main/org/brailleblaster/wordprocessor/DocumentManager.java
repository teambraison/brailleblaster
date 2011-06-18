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
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.custom.StyledText;
import nu.xom.*;
import java.io.IOException;
import org.eclipse.swt.widgets.Event;

class DocumentManager {

/**
* This class manages each document in an MDI environment. It controls the 
* braille View and the daisy View.
*/

final Display display;
final Shell documentWindow;
final int action;
FormLayout layout;
String documentName = "untitled";
BBToolBar toolBar;
BBMenu menu;
DaisyView daisy;
BrailleView braille;
BBStatusBar statusBar;
boolean exitSelected = false;
Document doc = null;

DocumentManager (Display display, int action) {
this.display = display;
this.action = action;
documentWindow = new Shell (display, SWT.SHELL_TRIM);
layout = new FormLayout();
documentWindow.setLayout (layout);
documentWindow.setText ("BrailleBlaster " + documentName);
menu = new BBMenu (this);
toolBar = new BBToolBar (this);
daisy = new DaisyView (documentWindow);
braille = new BrailleView (documentWindow);
statusBar = new BBStatusBar (documentWindow);
documentWindow.setSize (1000, 700);
documentWindow.layout(true, true);
documentWindow.open();
if (action == WP.OpenDocumentGetFile) {
fileOpen();
}
while (!documentWindow.isDisposed() && !exitSelected) {
if (!display.readAndDispatch())
display.sleep();
}
documentWindow.dispose();
}

void fileOpen () {
Shell shell = new Shell (display, SWT.DIALOG_TRIM);
FileDialog dialog = new FileDialog (shell, SWT.OPEN);
dialog.setFilterExtensions (new String[] {"xml", "utd"});
dialog.setFilterNames (new String[] {"DAISY xml file", "DAISY file with UTDML"});
String name = dialog.open();
shell.dispose();
if (name == null) return;
String fileName = "file://" + name;
Builder parser = new Builder();
try {
doc = parser.build (fileName);
}
catch (ParsingException e) {
}
catch (IOException e) {
}
}

void sendOpenEvent (int WPEventType) {
Event event = new Event();
event.detail = WPEventType;
documentWindow.notifyListeners (SWT.OpenDocument, event);
}

void fileSave() {
Shell shell = new Shell (display, SWT.DIALOG_TRIM);
FileDialog dialog = new FileDialog (shell, SWT.SAVE);
dialog.open();
shell.dispose();
}

void fileSaveAs () {
}

void placeholder() {
}

}

