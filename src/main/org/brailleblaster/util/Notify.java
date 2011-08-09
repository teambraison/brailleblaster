package org.brailleblaster.util;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.MessageBox;
import org.brailleblaster.BBIni;

public class Notify {

/**
* Show the user a message.
*/
public Notify (String message) {
Display display = BBIni.getDisplay();
Shell shell = new Shell(display, SWT.DIALOG_TRIM);
MessageBox mb = new MessageBox(shell, SWT.OK);
mb.setMessage (message);
mb.open();
shell.dispose();
}

}
