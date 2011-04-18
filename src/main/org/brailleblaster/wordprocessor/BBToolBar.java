package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Shell;

public class BBToolBar {

private ToolBar toolBar;

public BBToolBar (Shell documentWindow) {
toolBar = new ToolBar (documentWindow, 0);
}

}

