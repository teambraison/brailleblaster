package org.brailleblaster;

import org.brailleblaster.louisutdml.*;
import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.printing.*;
import java.util.*;

public class Subcommands
{

private WPManager wpManager;
private LocaleHandler lh = new LocaleHandler ();

public Subcommands (String[] args)
throws IllegalArgumentException
{
String subcommand;
subcommand = args[0];
String[] subArgs = Arrays.copyOfRange (args, 1, args.length);
if (subcommand.equals ("file2brl"))
doFile2brl (subArgs);
else if (subcommand.equals ("emboss"))
doEmboss (subArgs);
else
{
System.out.println (lh.localValue ("subcommand") + args[0] + 
lh.localValue ("not recognized"));
return;
}
}

private void doFile2brl (String[] args)
{
}

private void doEmboss (String[] args)
{
}

}
