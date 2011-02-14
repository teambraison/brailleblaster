package org.brailleblaster.wordprocessor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
* This class manages each document in an MDI environment. It controls the 
* braille View and the daisy View.
*/

public class DocumentManager
{
Shell documentWindow;
BBMenu menu;
DaisyView daisy;
Shell daisyShell;
BrailleView braille;
Shell brailleShell;
BBStatusBar statusBar;

public DocumentManager (Display display)
{
documentWindow = new Shell (display);
documentWindow.setText ("BrailleBlaster");
menu = new BBMenu (documentWindow);
brailleShell = new Shell (documentWindow);
braille = new BrailleView (brailleShell);
daisyShell = new Shell (documentWindow);
daisy = new DaisyView (daisyShell);
statusBar = new BBStatusBar (documentWindow);
}

}

