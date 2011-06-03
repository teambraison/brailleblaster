package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

class DaisyView {

StyledText view;

DaisyView (Shell documentWindow) {
view = new StyledText (documentWindow, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
FormData location = new FormData();
location.left = new FormAttachment(0);
location.right = new FormAttachment(55);
location.top = new FormAttachment (12);
location.bottom = new FormAttachment(92);
view.setLayoutData (location);
view.setText ("DAISY view");
}

}
