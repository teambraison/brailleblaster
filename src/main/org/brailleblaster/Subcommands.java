package org.brailleblaster;

import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.ProgramCaller;
import org.brailleblaster.embossers.EmbosserManager;
import org.liblouis.liblouisutdml;
import java.util.*;
import java.io.IOException;
import org.daisy.printing.PrinterDevice;

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
throws IllegalArgumentException, IOException, InterruptedException
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

private void doEmboss (String[] args)
throws InterruptedException, IOException
{
louisutdml.file2brl (subArgs);
}

}
