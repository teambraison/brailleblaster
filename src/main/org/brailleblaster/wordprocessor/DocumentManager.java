package org.brailleblaster.wordprocessor;
import org.brailleblaster.wordprocessor.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.brailleblaster.localization.LocaleHandler;

/**
This class manages each document in an MDI environment. It controls the 
* braille View and the daisy View.
*/

public class DocumentManager
{
DaisyView daisy = new DaisyView ();
BrailleView braille = new BrailleView ();
public DocumentManager ()
{
}

}

