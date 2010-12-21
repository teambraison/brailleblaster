package org.brailleblaster;
import org.brailleblaster.louisutdml.*;
import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;
import org.apache.commons.exec.*;

public class Subcommands
{

private WPManager wpManager;
private LocaleHandler lh = new LocaleHandler ();

public Subcommands (String[] args)
{
if (args[0].equals ("file2brl"))
doFile2brl (args);
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

}
