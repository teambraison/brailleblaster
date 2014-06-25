/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org www.aph.org
  *
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknowledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.embossers;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintException;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;
import org.daisy.printing.PrinterDevice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class EmbossersManager
{
	Shell shell;
	PrintDialog embosser;
	Logger logger;
	
	public EmbossersManager(){
		logger = BBIni.getLogger();
		this.shell = new Shell(Display.getDefault(), SWT.DIALOG_TRIM);
		embosser = new PrintDialog(this.shell);
	}
	
	public void emboss(BBDocument document){
		PrinterData data = embosser.open();
		
		if (data == null || data.equals("")) {
			return;
		}
		
		String filePath = BBIni.getTempFilesPath() + BBIni.getFileSep() + "tempBRF.brf";
		if(document.createBrlFile(filePath)){
			File translatedFile = new File(filePath);
			PrinterDevice embosserDevice;
			try {
				embosserDevice = new PrinterDevice(data.name, true);
				embosserDevice.transmit(translatedFile);
				translatedFile.delete();
			} catch (PrintException e) {
				LocaleHandler lh = new LocaleHandler();
				new Notify(lh.localValue("cannotEmboss") + ": " + data.name + "\n" + e.getMessage());
				logger.log(Level.SEVERE, "Print Exception", e);
			}
		}
	}
	
	public void close(){
		if(!shell.isDisposed())
			shell.dispose();
	}
}

