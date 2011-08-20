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

import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.ProgramCaller;
import org.brailleblaster.embossers.EmbosserManager;
import org.liblouis.liblouisutdml;
import java.util.*;
import java.io.IOException;
import org.daisy.printing.PrinterDevice;
import java.io.File;
import javax.print.PrintException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
* Process subcommands.
*/

public class Subcommands {

private Logger logger = BBIni.getLogger();
private WPManager wpManager;
private LocaleHandler lh = new LocaleHandler ();
private liblouisutdml louisutdml;
private String subcommand;
private String[] subArgs;

public Subcommands (String[] args) {
logger = BBIni.getLogger();
if (!BBIni.haveLiblouisutdml()) {
logger.log  (Level.SEVERE, "The Braille translation facility is absent.");
System.out.println 
("You can use the word processor on a demonstration basis.");
return;
}
ParseCommandLine.getInstance().parseCommand (args);
louisutdml = liblouisutdml.getInstance();
subcommand = args[0];
subArgs = Arrays.copyOfRange (args, 1, args.length);
if (subcommand.equals ("translate"))
doTranslate (subArgs);
else if (subcommand.equals ("emboss"))
doEmboss (subArgs);
else {
logger.log (Level.WARNING,
lh.localValue ("subcommand") + args[0] + 
lh.localValue ("notRecognized"));
}
}

private void doTranslate (String[] args)
{
louisutdml.file2brl (subArgs);
}

/**
* This method takes the same arguments as translate, except that the 
* output file must be specified and must be the name of a printer. Only 
* the generic embosser is supported at the moment, but most embossers 
* can run in this mode.
*/

private void doEmboss (String[] args) {
int outIndex = args.length - 1;
String transOut = "transout";
String embosserName = args[outIndex];
args[outIndex] = transOut;
louisutdml.file2brl (args);
File translatedFile = new File (transOut);
try {
PrinterDevice embosser = new PrinterDevice (embosserName, true);
embosser.transmit (translatedFile);
} catch (PrintException e) {
logger.log (Level.SEVERE, "Embosser is  not working", e);
}
}

}
