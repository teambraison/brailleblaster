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

package org.brailleblaster;

import org.brailleblaster.util.CheckLiblouisutdmlLog;
import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.ProgramCaller;
import org.brailleblaster.embossers.EmbossersManager;
import org.liblouis.liblouisutdml;
import java.io.IOException;
import org.daisy.printing.PrinterDevice;
import java.io.File;
import javax.print.PrintException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Arrays;
import org.eclipse.swt.widgets.Display;

/**
 * Process subcommands.
 * If the first argument is not a recognized subcommand, 
 * assume it is the name of a file to be opened in the wordprocessor.
*/

class Subcommands {
private Logger logger = BBIni.getLogger();
private LocaleHandler lh = new LocaleHandler ();
private liblouisutdml louisutdml;
private CheckLiblouisutdmlLog lbuLog = new CheckLiblouisutdmlLog();
private String subcommand;
private String[] subArgs;

Subcommands (String[] args) {
LocaleHandler lh = new LocaleHandler();
logger = BBIni.getLogger();
if (!BBIni.haveLiblouisutdml()) {
logger.log  (Level.SEVERE, "The Braille translation facility is absent.");
}
// ParseCommandLine.getInstance().parseCommand (args);
louisutdml = liblouisutdml.getInstance();
int i = 0;
while ( i < args.length) {
if (args[i].charAt(0) != '-') {
break;
}
i++;
}
if (i == args.length) {
return;
}
subcommand = args[i];
subArgs = Arrays.copyOfRange (args, i + 1, args.length);
if (subcommand.equals ("translate")) {
doTranslate();
}
else if (subcommand.equals ("emboss")) {
doEmboss();
}
else if (subcommand.equals ("checktable")) {
doChecktable();
}
else if (subcommand.equals ("help")) {
doHelp();
}
else {
Display display = BBIni.getDisplay ();
if (display == null) {
  System.out.println ("Incorrect options or subcommand");
  return;
  }
new WPManager (subcommand);
}
}

/**
 * Translate the input file to the output file according to the options, 
 * if any.
 */
private void doTranslate() {
	//FO 
louisutdml.setLogFile("liblouisutdml.log");
louisutdml.file2brl (subArgs);
lbuLog.showLog();
}

/**
 * This method takes the same arguments as translate, except that the 
 * output file must be specified and must be the name of a printer. Only 
 * the generic embosser is supported at the moment, but most embossers 
 * can run in this mode.
 */

private void doEmboss() {
int outIndex = subArgs.length - 1;
String transOut = "transout";
String embosserName = subArgs[outIndex];
subArgs[outIndex] = transOut;
// FO
if (embosserName.isEmpty() || (subArgs.length < 2)) {
//	logger.log (Level.SEVERE, "Embosser name not supplied. Exiting.");
	System.out.println( "Embosser name not supplied. Exiting.");
	return;
}
louisutdml.setLogFile("liblouisutdml.log");
louisutdml.file2brl (subArgs);
File translatedFile = new File (transOut);
try {
PrinterDevice embosser = new PrinterDevice (embosserName, true);
embosser.transmit (translatedFile);
} catch (PrintException e) {
logger.log (Level.SEVERE, "Embosser is  not working", e);
}
lbuLog.showLog();
}

private void doChecktable() {
String logFile = null;
if (subArgs.length == 0) {
System.out.println ("Usage: checktable tablename");
return;
}
if (subArgs.length > 1) {
logFile = subArgs[1];
}
louisutdml.checkTable (subArgs[0], logFile, 0);
lbuLog.showLog();
}

private void doHelp() {
final String[] help = new String[] {
"Usage: java -jar brailleblaster.jar {options} subcommand arguments",
"options: -nogui, the system does not have a GUI",
};
for (int i = 0; i < help.length; i++) {
System.out.println (help[i]);
}
}

}
