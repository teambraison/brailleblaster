/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.liblouis.liblouisutdml;
import org.brailleblaster.util.Notify;

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
String UTDMLTranslation;
String BRFTranslation;
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
String saveTo = dialog.open();
shell.dispose();
if (saveTo == null) {
new Notify ("could not write to file.");
return;
}

}

void translate() {
configFileList = "preferences.cfg";
String docFile = tempPath + "tempdoc.xml";
BRFTranslation = tempPath + "doc.brl";
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
BRFTranslation, logFile, settings, mode);
if (!result) {
new Notify ("Translation failed.");
}
}

void fileEmbossNow () {
Shell shell = new Shell (display, SWT.DIALOG_TRIM);
PrintDialog embosser = new PrintDialog (shell);
PrinterData data = embosser.open();
shell.dispose();
new Notify (data.toString());
}

void placeholder() {
new Notify ("This menu item is not yet implemented. Sorry.");
}

}

