package org.brailleblaster.wordprocessor;
import org.brailleblaster.wordprocessor.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.brailleblaster.localization.LocaleHandler;

/*
This class manages each document in an MDI environment. It controls the 
* braille window and the daisy window.
*/

public class DocumentManager
{
DaisyWindow daisy = new DaisyWindow ();
BrailleWindow braille = new BrailleWindow ();
public DocumentManager ()
{
}

}

