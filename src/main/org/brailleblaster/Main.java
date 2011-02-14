package org.brailleblaster;

import org.liblouis.liblouisutdml;
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
if (args.length == 0)
new WPManager ();
else
{
//ParseCommandLine.getInstance().parseCommand (args);
new Subcommands (args);
}
liblouisutdml louisutdml = liblouisutdml.getInstance ();
louisutdml.free();
} catch (Exception e) {
e.printStackTrace();
}
}

}
