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

package org.brailleblaster.printers;

import org.brailleblaster.BBIni;
import java.util.logging.Logger;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.printing.*;

public class PrintersManager {

	StyledText text;
	Font font;
	Color foregroundColor, backgroundColor;
	
	Printer printer;
	GC gc;
	FontData[] printerFontData;
	RGB printerForeground, printerBackground;
	Logger logger;
	
	int lineHeight = 0;
	int tabWidth = 0;
	int leftMargin, rightMargin, topMargin, bottomMargin;
	int x, y;
	int index, end;
	String textToPrint;
	String tabs;
	StringBuffer wordBuffer;
	 PrintDialog dialog;
	 
    public PrintersManager (Shell shell, StyledText st) {
    	logger = BBIni.getLogger();
    	this.text = st;
    	this.textToPrint = st.getText() + "\n";
    	this.dialog = new PrintDialog(shell, SWT.SHELL_TRIM);
    }

    public void beginPrintJob() {
    	PrinterData data = dialog.open();
		if (data == null) return;
		if (data.printToFile) {
			data.fileName = "print.out"; 
		}

		printerFontData = this.text.getFont().getFontData();
		printerForeground = this.text.getForeground().getRGB();
		printerBackground = this.text.getBackground().getRGB();
		
		printer = new Printer(data);
		
		Runnable thread = text.print(printer);
		thread.run();
    }
}

