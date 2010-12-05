package org.brailleblaster;
import org.brailleblaster.louisutdml.LouisFree;
import org.brailleblaster.wordprocessor.WPManager;
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
