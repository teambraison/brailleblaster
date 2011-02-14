package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class BBStatusBar
{

Label statusBar;

public BBStatusBar (Shell documentWindow)
{
statusBar = new Label
    (documentWindow, SWT.NONE);
}

}

