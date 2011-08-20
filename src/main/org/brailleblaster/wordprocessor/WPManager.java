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
import org.eclipse.swt.widgets.MessageBox;
import org.brailleblaster.BBIni;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.brailleblaster.util.YesNoChoice;

public class WPManager {

/**
 * This is the controller for the whole word processing operation. It is the
 * entry point for the word processor, and therefore the only public class.
 */

private Display display;
private SettingsDialogs settings;
private DocumentManager[] documents = new DocumentManager[8];
private int documentCount = -1;

/**
 * This constructor method is the entry point to the word prodessor. It gets
 * things set up, handles multiple documents, etc.
 */

public WPManager() {

display = BBIni.getDisplay();
if (display == null) {
System.out.println ("Could not find graphical interface environment");
System.exit(1);
}
checkLiblouisutdml();
setListeners();
addDocument(WP.NewDocument);
}

Listener openListener = new Listener() {
public void handleEvent(Event event) {
addDocument(event.detail);
}
};

void setListeners() {
display.addListener(SWT.OpenDocument, openListener);
}

void addDocument(int action) {
documentCount++;
documents[documentCount] = new DocumentManager(display, action);
documents[documentCount] = null;
documentCount--;
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
