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
import org.brailleblaster.BBIni;
import org.brailleblaster.util.YesNoChoice;
import org.brailleblaster.util.Notify;

public class WPManager {

/**
 * This is the controller for the whole word processing operation. It is the
 * entry point for the word processor, and therefore the only public class.
 */

String fileName = null;
int action;
private Display display;
private SettingsDialogs settings;
private DocumentManager[] documents = new DocumentManager[8];
private int documentIndex;

/**
 * This constructor is the entry point to the word prodessor. It gets
 * things set up, handles multiple documents, etc.
 */

public WPManager(String fileName) {
this.fileName = fileName;
if (fileName != null) {
action = WP.DocumentFromCommandLine;
} else {
action = WP.NewDocument;
}
display = BBIni.getDisplay();
if (display == null) {
System.out.println ("Could not find graphical interface environment");
System.exit(1);
}
checkLiblouisutdml();
documentIndex = 0;
DocumentManager curDoc;
curDoc = documents[documentIndex] = new DocumentManager(display, 
documentIndex, action, fileName);
do {
switch (curDoc.returnReason) {
case WP.DocumentClosed:
documents[documentIndex] = null;
int moveIndex = documentIndex;
while (documents[moveIndex + 1] != null) {
documents[documentIndex] = documents[moveIndex++];
}
if (documents[documentIndex] == null) {
documentIndex = 0;
}
if (documents[documentIndex] == null) {
return;
}
curDoc = documents[documentIndex];
curDoc.resume();
break;
case WP.SwitchDocuments:
documentIndex++;
if (documents[documentIndex] != null) {
curDoc = documents[documentIndex];
curDoc.resume();
} else {
documentIndex = 0;
curDoc = documents[documentIndex];
curDoc.resume();
}
break;
case WP.NewDocument:
documentIndex++;
if (documentIndex >= documents.length) {
new Notify ("Too many documents");
curDoc.resume();
break;
}
curDoc = documents[documentIndex] = new DocumentManager(display, 
documentIndex, WP.NewDocument, fileName);
break;
case WP.OpenDocumentGetFile:
documentIndex++;
if (documentIndex >= documents.length) {
new Notify ("Too many documents");
curDoc.resume();
break;
}
curDoc = documents[documentIndex] = new DocumentManager(display, 
documentIndex, WP.OpenDocumentGetFile, fileName);
break;
case WP.BBClosed:
for (documentIndex = 0; documentIndex < documents.length; 
documentIndex++) {
if (documents[documentIndex] != null) {
documents[documentIndex].finish();
}
documents[documentIndex] = null;
}
return;
default:
break;
}
} while (curDoc.returnReason != WP.BBClosed);
}

void checkLiblouisutdml() {
if (BBIni.haveLiblouisutdml()) {
return;
}
if (new YesNoChoice
("The Braille facility is not usable." + " See the log."
+ " Do you wish to continue?")
.result == SWT.NO) {
System.exit(1);
}
}

}
