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
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.liblouis.liblouisutdml;
import org.brailleblaster.util.Notify;
import java.io.File;
import org.daisy.printing.*;
import javax.print.PrintException;
import org.eclipse.swt.widgets.Listener;
import org.brailleblaster.settings.Welcome;

class DocumentManager {

/**
 * This class manages each document in an MDI environment. It controls 
 * the braille View and the daisy View.
*/

final Display display;
final Shell documentWindow;
final int documentNumber;
final String docID;
int action;
int returnReason = 0;
FormLayout layout;
String documentName = null;
boolean documentChanged = false;
BBToolBar toolBar;
BBMenu menu;
AbstractView activeView;
DaisyView daisy;
BrailleView braille;
BBStatusBar statusBar;
boolean exitSelected = false;
Document doc = null;
NewDocument newDoc;
String configFileList = null;
String tempPath;
boolean haveOpenedFile = false;
String translatedFileName = null;
liblouisutdml louisutdml;
String logFile = "Translate.log";
String configSettings = null;
int mode = 0;
/* Start of fields used for UTDML processing*/
UTD utd;
int braillePageNumber; //number of braille pages
String firstTableName;
int dpi; // resolution
int paperWidth;
int paperHeight;
int leftMargin;
int rightMargin;
int topMargin;
int bottomMargin;
String utdIndexAttr;
int utdIndexPos;
/*End of utd fields*/


/**
 * Constructor that sets things up for a new document.
*/
DocumentManager (Display display, int documentNumber, int action, 
String documentName) {
this.display = display;
this.documentNumber = documentNumber;
docID = new Integer (documentNumber).toString();
this.action = action;
this.documentName = documentName;
tempPath = BBIni.getTempFilesPath() + BBIni.getFileSep();
louisutdml = liblouisutdml.getInstance();
documentWindow = new Shell (display, SWT.SHELL_TRIM);
layout = new FormLayout();
documentWindow.setLayout (layout);
menu = new BBMenu (this);
toolBar = new BBToolBar (this);
daisy = new DaisyView (documentWindow);
braille = new BrailleView (documentWindow);
// activeView = (ProtoView)daisy;
statusBar = new BBStatusBar (documentWindow);
documentWindow.setSize (1000, 700);
documentWindow.layout(true, true);
documentWindow.addListener (SWT.Close, new Listener () {
public void handleEvent (Event event) {
handleShutdown(event);
}
});
documentWindow.open();
setWindowTitle ("untitled");
if (documentNumber == 0) {
new Welcome(); // This then calls the settings dialogs.
}
if (action == WP.OpenDocumentGetFile) {
fileOpen();
} else if (action == WP.DocumentFromCommandLine) {
openFirstDocument();
}
utd = new UTD();
while (!documentWindow.isDisposed() && returnReason == 0) {
if (!display.readAndDispatch())
display.sleep();
}
if (!BBIni.debugging()) {
documentWindow.dispose();
return;
}
switch (returnReason) {
case WP.DocumentClosed:
case WP.BBClosed:
finish();
break;
default:
//documentWindow.setVisible (false);
break;
}
}

/**
 * This nested class encapsulates hnadling of the Universal 
 * TactileDocument Markup Language (UTDML);
 */
class UTD {
Node beforeBrlNode = null;
boolean firstPage;
boolean firstLineOnPage;
int maxlines;
int numlines;

UTD () {
}

void displayTranslatedFile() {
beforeBrlNode = null;
utdIndexAttr = null;
utdIndexPos = 0;
firstPage = true;
firstLineOnPage = true;
maxlines = 20;
numlines = 0;
Builder parser = new Builder();
try {
doc = parser.build (translatedFileName);
} catch (ParsingException e) {
new Notify ("Malformed document");
return;
}
catch (IOException e) {
new Notify ("Could not open " + translatedFileName);
return;
}
Element rootElement = doc.getRootElement();
walkTree (rootElement);
}

private void walkTree (Element node) {
Node newNode;
Element element;
String elementName;
for (int i = 0; i < node.getChildCount(); i++) {
newNode = node.getChild(i);
if (newNode instanceof Element) {
element = (Element)newNode;
elementName = element.getLocalName();
if (elementName.equals ("meta")) {
doUtdMeta (element);
} else if (elementName.equals ("brl")) {
if (i > 0) {
beforeBrlNode = newNode.getChild(i - 1);
} else {
beforeBrlNode = null;
}
doBrlNode (element);
} else {
walkTree (element);
}
}
}
}

private void doUtdMeta (Element node) {
String metaContent;
metaContent = node.getAttributeValue ("name");
if (!(metaContent.equals ("utd"))) {
return;
}
metaContent = node.getAttributeValue ("content");
return;
}

private void doBrlNode (Element node) {
utdIndexAttr = node.getAttributeValue ("index");
utdIndexPos = 0;
Node newNode;
Element element;
String elementName;
for (int i = 0; i < node.getChildCount(); i++) {
newNode = node.getChild(i);
if (newNode instanceof Element) {
element = (Element)newNode;
elementName = element.getLocalName();
if (elementName.equals ("newpage")) {
doNewpage (element);
} else if (elementName.equals ("newline")) {
doNewline (element);
} else if (elementName.equals ("span")) {
doSpanNode (element);
} else if (elementName.equals ("graphic")) {
doGraphic (element);
}
}
else if (newNode instanceof Text) {
doTextNode (newNode);
}
}
utdIndexAttr = null;
utdIndexPos = 0;
}

private void doSpanNode (Element node) {
String whichSpan = node.getAttributeValue ("class");
if (whichSpan.equals ("brlonly")) {
doBrlonlyNode (node);
}
else if (whichSpan.equals ("locked")) {
doLockedNode (node);
}
}

private void doBrlonlyNode (Element node) {
Node newNode;
Element element;
String elementName;
for (int i = 0; i < node.getChildCount(); i++) {
newNode = node.getChild(i);
if (newNode instanceof Element) {
element = (Element)newNode;
elementName = element.getLocalName();
if (elementName.equals ("newpage")) {
doNewpage (element);
} else if (elementName.equals ("newline")) {
doNewline (element);
} else if (elementName.equals ("graphic")) {
doGraphic (element);
}
}
else if (newNode instanceof Text) {
doTextNode (newNode);
}
}
}

private void doLockedNode (Element node) {
}

private void doNewpage (Element node) {
firstLineOnPage = true;
if (firstPage) {
firstPage = false;
return;
}
}

private void doNewline (Element node) {
}

private void doTextNode (Node node) {
Text text = (Text)node;
}

private void doGraphic (Element node) {
}

private void doGraphics (Element node) {
}

private void finishBrlNode () {
}

}

/**
 * Handle application shutdown signal from OS;
 */
void handleShutdown (Event event) {
event.doit = true;
}

 /**
* Clean up before closing the document.
*/
void finish() {
documentWindow.dispose();
}

/**
* Checks if a return request  is valid and does any necessary 
* processing.
*/
boolean setReturn (int reason) {
switch (reason) {
case WP.SwitchDocuments:
if (WPManager.haveOtherDocuments()) {
returnReason = reason;
return true;
}
return false;
case WP.NewDocument:
returnReason = reason;
break;
case WP.OpenDocumentGetFile:
returnReason = reason;
break;
case WP.DocumentClosed:
returnReason = reason;
break;
case WP.BBClosed:
returnReason = reason;
break;
default:
break;
}
return true;
}

/**
 * This method is called to resume processing on this document after 
 * working on another.
 */
void resume() {
if (documentWindow.isDisposed())
return;
documentWindow.forceActive();
returnReason = 0;
}
 
void openFirstDocument() {
Builder parser = new Builder();
try {
doc = parser.build (documentName);
} catch (ParsingException e) {
new Notify ("Malformed document");
return;
}
catch (IOException e) {
new Notify ("Could not open " + documentName);
return;
}
setWindowTitle (documentName);
haveOpenedFile = true;
Element rootElement = doc.getRootElement();
walkTree (rootElement);
}

private void setWindowTitle (String pathName) {
int index = pathName.lastIndexOf (File.separatorChar);
if (index == -1) {
documentWindow.setText ("BrailleBlaster " + pathName);
} else {
documentWindow.setText ("BrailleBlaster " + pathName.substring (index + 
1));
}
}

void createDocument () {
newDoc = new NewDocument();
newDoc.fillOutBody (daisy.view);
doc = newDoc.getDocument();
}

void fileNew() {
placeholder();
}

void fileOpen () {
if (BBIni.debugging() && doc != null) {
returnReason = WP.OpenDocumentGetFile;
return;
}
Shell shell = new Shell (display, SWT.DIALOG_TRIM);
FileDialog dialog = new FileDialog (shell, SWT.OPEN);
dialog.setFilterExtensions (new String[] {"xml", "utd"});
dialog.setFilterNames (new String[] {"DAISY xml file", "DAISY file with UTDML"});
documentName = dialog.open();
shell.dispose();
if (documentName == null) {
new Notify ("File not found");
return;
}
String fileName = documentName;
Builder parser = new Builder();
try {
doc = parser.build (fileName);
} catch (ParsingException e) {
new Notify ("Malformed document");
return;
}
catch (IOException e) {
new Notify ("Could not open " + documentName);
return;
}
setWindowTitle (documentName);
haveOpenedFile = true;
Element rootElement = doc.getRootElement();
walkTree (rootElement);
}

private void walkTree (Node node) {
int maxlines = 20;
int numlines = 0;
Node newNode;
for (int i = 0; i < node.getChildCount(); i++) {
newNode = node.getChild(i);
if (newNode instanceof Element) {
walkTree (newNode);
}
else if (newNode instanceof Text) {
numlines++;
if (numlines == maxlines) {
return;
}
String value = newNode.getValue();
daisy.view.append (value);
}
}
}

void fileSave() {
placeholder();
}

void fileSaveAs () {
Shell shell = new Shell (display, SWT.DIALOG_TRIM);
FileDialog dialog = new FileDialog (shell, SWT.SAVE);
String saveTo = dialog.open();
shell.dispose();
if (saveTo == null) {
new Notify ("could not write to " + saveTo);
return;
}
if (translatedFileName == null) {
new Notify ("There is no translated file to be saved.");
return;
}
FileInputStream inFile = null;
FileOutputStream outFile = null;
try {
inFile = new FileInputStream (translatedFileName);
} catch (FileNotFoundException e) {
new Notify ("Could not open " + translatedFileName);
return;
}
try {
outFile = new FileOutputStream (saveTo);
} catch (FileNotFoundException e) {
new Notify ("Could not open " + saveTo);
return;
}
byte[] buffer = new byte[1024];
int length = 0;
while (length != -1) {
try {
length = inFile.read (buffer, 0, buffer.length);
} catch (IOException e) {
new Notify ("Problem reading " + translatedFileName);
break;
}
if (length == -1) {
break;
}
try {
outFile.write (buffer, 0, length);
} catch (IOException e) {
new Notify ("Problem writing to " + saveTo);
break;
}
}
try {
outFile.close();
} catch (IOException e) {
new Notify (saveTo + " could not be completed");
}
}

void showBraille() {
String line;
BufferedReader translation = null;
try {
translation = new BufferedReader (new FileReader 
(translatedFileName));
} catch (FileNotFoundException e) {
new Notify ("Could not fine " + translatedFileName);
}
for (int i = 0; i < 20; i++) {
try {
line = translation.readLine();
} catch (IOException e) {
new Notify ("Problem reading " + translatedFileName);
return;
}
if (line == null) {
break;
}
braille.view.append (line + "\n");
}
try {
translation.close();
} catch (IOException e) {
}
}

void translate() {
if (!haveOpenedFile) {
/* We have a new file. */
createDocument();
}
if (doc == null) {
new Notify ("There is no open file.");
return;
}
configFileList = "preferences.cfg";
if (BBIni.useUtd()) {
configSettings = "formatFor utd\n";
}
String docFile = tempPath + docID + "-tempdoc.xml";
translatedFileName = tempPath + docID + "-doc.brl";
FileOutputStream writer = null;
try {
writer = new FileOutputStream (docFile);
} catch (FileNotFoundException e) {
new Notify ("could not open file for writing");
return;
}
Serializer outputDoc = new Serializer (writer);
try {
outputDoc.write (doc);
} catch (IOException e) {
new Notify ("Could not write to file");
return;
}
boolean result = louisutdml.translateFile (configFileList, docFile, 
translatedFileName, logFile, configSettings, mode);
if (!result) {
new Notify ("Translation failed.");
return;
}
showBraille();
}

void fileEmbossNow () {
if (translatedFileName == null) {
translate();
}
if (translatedFileName == null) {
return;
}
Shell shell = new Shell (display, SWT.DIALOG_TRIM);
PrintDialog embosser = new PrintDialog (shell);
PrinterData data = embosser.open();
shell.dispose();
if (data == null || data.equals("")) {
return;
}
File translatedFile = new File (translatedFileName);
PrinterDevice embosserDevice;
try {
embosserDevice = new PrinterDevice (data.name, true);
embosserDevice.transmit (translatedFile);
} catch (PrintException e) {
new Notify ("Could not emboss on "  + data.name);
}
}

void placeholder() {
new Notify ("This menu item is not yet implemented. Sorry.");
}

}

