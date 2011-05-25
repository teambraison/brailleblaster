package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.ScrollBar;

public class DaisyView {

private StyledText daisy;

public DaisyView (Shell documentWindow) {
daisy = new StyledText
    (documentWindow, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
daisy.setText ("DAISY View");
FormData location = new FormData();
location.left = new FormAttachment(0);
location.right = new FormAttachment(50);
location.top = new FormAttachment (14);
location.bottom = new FormAttachment(94);
daisy.setLayoutData (location);
ScrollBar bar = daisy.getHorizontalBar();
}

}

