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

package org.brailleblaster.util;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.MessageBox;
import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 * Check the file liblouisutdml.log in brlblst/temp. If it is not empty 
 * show its contents. There are separate methods for the GUI and the 
 * command line.
 */
public class CheckLiblouisutdmlLog {
String logFileName;

public CheckLiblouisutdmlLog () {
logFileName = BBIni.getTempFilesPath() + BBIni.getFileSep() + 
"liblouisutdml.log";
}

public void displayLog () {
StringBuilder logMessages = new StringBuilder (4096);
Display display = BBIni.getDisplay();
Shell shell = new Shell(display, SWT.DIALOG_TRIM);
MessageBox mb = new MessageBox(shell, SWT.OK);
String line;
BufferedReader logStream = null;
try {
logStream = new BufferedReader (new FileReader 
(logFileName));
} catch (FileNotFoundException e) {

new Notify ("Could not find " + logFileName);
return;
}
while(true) {
try {
line = logStream.readLine();
} catch (IOException e) {
new Notify ("Problem reading " + logFileName);
return;
}
if (line == null) {
break;
}
logMessages.append (line + "\n");
}
try {
logStream.close();
} catch (IOException e) {
}
if (logMessages.length() > 0) {
mb.setMessage (logMessages.toString());
mb.open();
}
shell.dispose();
}

public void showLog () {
String line;
BufferedReader logStream = null;
try {
logStream = new BufferedReader (new FileReader 
(logFileName));
} catch (FileNotFoundException e) {
new Notify ("Could not find " + logFileName);
return;
}
while(true) {
try {
line = logStream.readLine();
} catch (IOException e) {
new Notify ("Problem reading " + logFileName);
return;
}
if (line == null) {
break;
}
System.out.println (line);
}
try {
logStream.close();
} catch (IOException e) {
}
}

}
