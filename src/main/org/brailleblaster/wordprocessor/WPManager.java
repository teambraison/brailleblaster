package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.MessageBox;
import org.brailleblaster.BBIni;

/**
* This is the controller for the whole word processing operation.
*/

public class WPManager {
Display display;
SettingsDialogs settings;
DocumentManager[] documents = new DocumentManager[8];
int currentDocument = 0;

/**
* Normal word processor entry poinnt.
*/

public WPManager () {
display = BBIni.getDisplay();
if (display == null) {
System.exit (1);
}
if (!BBIni.haveLiblouisutdml()) {
Shell shell = new Shell (display);
MessageBox mb = new MessageBox (shell, SWT.YES | SWT.NO);
mb.setMessage ("The Braille facility is missing."
+ " Do you wish to continue?");
int result = mb.open();
shell.dispose();
if (result == SWT.NO)
System.exit (1);
}
documents[currentDocument] = new DocumentManager (display);
}

/**
* Handles text editor, etc.
*/

public WPManager (Object o)
{
TextEditor editor = new TextEditor ();
}

}

