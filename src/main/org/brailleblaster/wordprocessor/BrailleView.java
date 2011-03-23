package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;

public class BrailleView {

StyledText braille;

public BrailleView (Shell documentWindow) {
braille = new StyledText 
    (documentWindow, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
}

}

