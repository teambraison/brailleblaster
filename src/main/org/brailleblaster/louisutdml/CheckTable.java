package org.brailleblaster.louisutdml;

import org.liblouis.LibLouisUTDML;

/**
* Checks a liblouis table for correctness.
*/

public class CheckTable
{

public CheckTable (String tableList, String logFile, int mode)
throws LiblouisutdmlException
{
boolean result;
result = LibLouisUTDML.getInstance().checkTable (tableList, logFile, 
mode);
}

}

