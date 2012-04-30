/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
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
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.wordprocessor;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Combo;
import org.brailleblaster.BBIni;


/**
 * Pick a document from those recently opened and return its absolute 
 * path.
*/ 
class RecentDocuments {
Shell shell;
Combo combo;

RecentDocuments() {
shell = new Shell (BBIni.getDisplay(), SWT.DIALOG_TRIM);
combo = new Combo (shell, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);

shell.dispose();
}

void addDocument (String document) {
}

String pickDocument() {
shell.open();
String document = combo.getText();
return document;
}

}


