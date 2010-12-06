package org.brailleblaster;
import org.brailleblaster.louisutdml.LouisFree;
import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;

/* This class contains the main method. It processes any subcommands and 
* then passes cortrol to WPManager with the appropriate parameters. When 
* WPManager returns it frees up any memory used by liblouisutdml.
*/

public class Main
{
WPManager wpManager = new WPManager ();

public static void main (String[] args)
{
Main m = new Main ();
m.processArgs (args);
LouisFree.louisFree ();
}

private void processArgs (String[] args)
{
if (args.length == 0)
wpManager.normal ();
else
wpManager.special ();
}

}
