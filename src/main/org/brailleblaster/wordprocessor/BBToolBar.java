package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.brailleblaster.localization.LocaleHandler;

public class BBToolBar {

private ToolBar toolBar;

public BBToolBar (DocumentManager dm) {
LocaleHandler lh = new LocaleHandler();
toolBar = new ToolBar (dm.documentWindow, SWT.BORDER);
FormData location = new FormData();
location.left = new FormAttachment(0);
location.right = new FormAttachment(100);
location.top = new FormAttachment (8);
location.bottom = new FormAttachment(13);
toolBar.setLayoutData (location);
ToolItem trranslateItem = new ToolItem (toolBar, SWT.PUSH);
translateItem.setText (lh.localValue ("translate");
toolBar.pack();
}

}

