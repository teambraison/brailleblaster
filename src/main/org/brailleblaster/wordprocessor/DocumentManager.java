package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.layout.FormLayout;
import org.brailleblaster.BBIni;
import org.eclipse.swt.printing.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.custom.StyledText;
import nu.xom.*;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.IOException;
import org.liblouis.liblouisutdml;
import org.brailleblaster.util.Notify;
import java.io.OutputStream;

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
String configFileList = null;
String openedFile = null;
String tempPath;
liblouisutdml louisutdml;
String logFile;
String settings;
int mode = 0;

/**
* Constructor that sets things up for a new document.
*/
DocumentManager (Display display, int action) {
this.display = display;
this.action = action;
tempPath = BBIni.getTempFilesPath() + BBIni.getFileSep();
louisutdml = liblouisutdml.getInstance();
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
openedFile = dialog.open();
shell.dispose();
if (openedFile == null) {
new Notify ("File not found");
return;
}
String fileName = openedFile;
Builder parser = new Builder();
try {
doc = parser.build (fileName);
}
catch (ParsingException e) {
new Notify ("Malformed document");
return;
}
catch (IOException e) {
new Notify ("Could not open file");
return;
}
Element rootElement = doc.getRootElement();
walkTree (rootElement);
}

private void walkTree (Node node) {
Node newNode;
for (int i = 0; i < node.getChildCount(); i++) {
newNode = node.getChild(i);
if (newNode instanceof Element) {
walkTree (newNode);
}
else if (newNode instanceof Text) {
String value = newNode.getValue();
daisy.view.append (value);
}
}
}

void sendOpenEvent (int WPEventType) {
Event event = new Event();
event.detail = WPEventType;
documentWindow.notifyListeners (SWT.OpenDocument, event);
}

void fileSave() {
}

void fileSaveAs () {
Shell shell = new Shell (display, SWT.DIALOG_TRIM);
FileDialog dialog = new FileDialog (shell, SWT.SAVE);
dialog.open();
shell.dispose();
}

void translate() {
configFileList = "preferences.cfg";
String docFile = tempPath + "tempdoc.xml";
String outTemp = tempPath + "doc.brl";
FileOutputStream writer = null;
try {
writer = new FileOutputStream (docFile);
}
catch (FileNotFoundException e) {
new Notify ("could not open file for writing");
return;
}
Serializer outputDoc = new Serializer (writer);
try {
outputDoc.write (doc);
}
catch (IOException e) {
new Notify ("Could not write to file");
return;
}
logFile = tempPath + "translate.log";
boolean result = louisutdml.translateFile (configFileList, docFile, 
outTemp, logFile, settings, mode);
}

void placeholder() {
}

}

