package org.brailleblaster;

import org.liblouis.Jliblouisutdml;
//import org.brailleblaster.louisutdml.LouisFree;
import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;

/**
* This class contains the main method. If there are no arguments it 
* passes control directly to the word processor. If there are arguments 
* it passes them to the constructor of the class Subcommands. It will do more 
* processing as the project develops.
*/

public class Main
{

public static void main (String[] args)
{
try {
BBIni initialConditions = BBIni.getInstance();
if (args.length > 0)
new Subcommands (args);
else
new WPManager ();
Jliblouisutdml louisutdml = Jliblouisutdml.getInstance ();
louisutdml.free ();
} catch (Exception e) {
e.printStackTrace();
}
}

}
