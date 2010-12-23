package org.brailleblaster;

import org.brailleblaster.louisutdml.*;
import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.printing.*;

public class Subcommands
{

private WPManager wpManager;
private LocaleHandler lh = new LocaleHandler ();

public Subcommands (String[] args)
{
String command;
command = args[0];
if (command.equals ("file2brl"))
doFile2brl (args);
else if (command.equals ("emboss"))
doEmboss (args);
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
