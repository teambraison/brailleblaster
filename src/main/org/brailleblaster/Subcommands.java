package org.brailleblaster;

import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.ProgramCaller;
import org.brailleblaster.embossers.EmbosserManager;
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
private String subcommand;
private String[] subArgs;

public Subcommands (String[] args)
throws IllegalArgumentException, IOException, InterruptedException
{
subcommand = args[0];
subArgs = Arrays.copyOfRange (args, 1, args.length);
if (subcommand.equals ("file2brl"))
doFile2brl (subArgs);
else if (subcommand.equals ("emboss"))
doEmboss (subArgs);
else
{
throw new IllegalArgumentException
(lh.localValue ("subcommand") + args[0] + 
lh.localValue ("not recognized"));
}
}

private void doFile2brl (String[] args)
throws InterruptedException, IOException
{
ProgramCaller pc = new ProgramCaller ("native/bin/" + subcommand, 
args, 0);
pc.waitTillDone ();
}

private void doEmboss (String[] args)
throws InterruptedException, IOException
{
}

}
