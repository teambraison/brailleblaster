package org.brailleblaster.louisutdml;

import org.liblouis.LibLouisUTDML;

public class TranslateFile {

	public TranslateFile(String configFileList, String inputFileName, String outputFileName, 
			String logFileName, String settingsString, int mode) throws LiblouisutdmlException {
					boolean result;
					result = LibLouisUTDML.getInstance().translateFile(configFileList,
								inputFileName, outputFileName, logFileName, settingsString, mode);
	}

}
