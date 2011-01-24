package org.brailleblaster.louisutdml;

import org.liblouis.Jliblouisutdml;

public class TranslateFile
{

  public TranslateFile (String configFileList, String
   inputFileName,
   String outputFileName,
					   String logFileName,
   String settingsString, int mode)
throws LiblouisutdmlException
{
boolean result;
result = Jliblouisutdml.getInstance().translateFile
(configFileList, inputFileName,
 outputFileName, logFileName,
 settingsString, mode);
}

}
