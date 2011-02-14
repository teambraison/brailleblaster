package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;

public class BrailleView
{

StyledText braille;

public BrailleView (Shell brailleShell)
{
braille = new StyledText 
    (brailleShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
}

}

