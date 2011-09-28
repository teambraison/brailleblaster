package org.brailleblaster.wordprocessor;

import java.awt.Desktop;
import org.brailleblaster.util.Notify;

class UserHelp {
UserHelp (String helpName) {
Desktop desktop = Desktop.getDesktop();
new Notify (helpName + " is being written.");
}
}

