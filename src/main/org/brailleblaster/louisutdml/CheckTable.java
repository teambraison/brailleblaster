package org.brailleblaster.louisutdml;

import org.liblouis.Jliblouisutdml;

/**
* Checks a liblouis table for correctness.
*/

public class CheckTable
{

public CheckTable (String tableList, String logFile, int mode)
throws LiblouisutdmlException
{
boolean result;
result = Jliblouisutdml.getInstance().checkTable (tableList, logFile, 
mode);
}

}

