package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;

public class DaisyView
{

StyledText daisy;

public DaisyView (Shell daisyShell)
{
daisy = new StyledText
    (daisyShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
}

}

