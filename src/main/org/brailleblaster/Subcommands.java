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

/**
* Process subcommands.
*/

public class Subcommands
{

private WPManager wpManager;
private LocaleHandler lh = new LocaleHandler ();
private liblouisutdml louisutdml;
private String subcommand;
private String[] subArgs;

public Subcommands (String[] args)
throws IllegalArgumentException, IOException, PrintException
{
louisutdml = liblouisutdml.getInstance();
subcommand = args[0];
subArgs = Arrays.copyOfRange (args, 1, args.length);
if (subcommand.equals ("translate"))
doTranslate (subArgs);
else if (subcommand.equals ("emboss"))
doEmboss (subArgs);
else
{
throw new IllegalArgumentException
(lh.localValue ("subcommand") + args[0] + 
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

private void doEmboss (String[] args)
throws PrintException
{
int outIndex = args.length - 1;
String transOut = "transout";
String embosserName = args[outIndex];
args[outIndex] = transOut;
louisutdml.file2brl (args);
File translatedFile = new File (transOut);
PrinterDevice embosser = new PrinterDevice (embosserName, true);
embosser.transmit (translatedFile);
}

}
