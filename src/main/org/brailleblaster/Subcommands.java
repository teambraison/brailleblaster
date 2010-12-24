package org.brailleblaster;

import org.brailleblaster.louisutdml.*;
import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.ProgramCaller;
import org.eclipse.swt.*;
import org.eclipse.swt.printing.*;
import java.util.*;

public class Subcommands
{

private WPManager wpManager;
private LocaleHandler lh = new LocaleHandler ();
private String subcommand;
private String[] subArgs;

public Subcommands (String[] args)
throws IllegalArgumentException
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
{
ProgramCaller pc = new ProgrfamCaller (subcommand, subArgs, 0);
}

private void doEmboss (String[] args)
{
}

}
