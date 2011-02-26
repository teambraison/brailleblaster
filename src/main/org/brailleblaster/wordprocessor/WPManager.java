package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.brailleblaster.BBIni;

/**
* This is the controller for the whole word processing operation.
*/

public class WPManager
{
Display display;
StartupDialogs startup;
DocumentManager[] documents = new DocumentManager[8];
int currentDocument = 0;

/**
* Normal word processor entry poinnt.
*/

public WPManager ()
{
display = BBIni.getDisplay();
documents[currentDocument] = new DocumentManager (display);
display.dispose();
}

/**
* Handles text editor, etc.
*/

public WPManager (Object o)
{
TextEditor editor = new TextEditor ();
}

}

