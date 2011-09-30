/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.wordprocessor;

import java.awt.Desktop;
import org.brailleblaster.util.Notify;
import org.brailleblaster.BBIni;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

/**
* This class handles the items on the help menu.
*/
class UserHelp {

private String helpPath;
Desktop desktop;

UserHelp (String helpName) {
helpPath = BBIni.getHelpDocsPath() + BBIni.getFileSep();
desktop = Desktop.getDesktop();
if (helpName.equals ("about")) {
new Notify (BBIni.getVersion() +  ", released on " + 
BBIni.getReleaseDate() + 
". For questions and bug reports contact john.boyer@abilitiessoft.com");
}
else if (helpName.equals ("manuals")) {
showHelp ("manuals.html");
}
else if (helpName.equals ("helpinfo")) {
showHelp ("helpinfo.html");
}
else {
new Notify (helpName + " is being written.");
}
}

/**
* Display help documents in the local browser.
*/
void showHelp (String fileName) {
String completePath = helpPath + fileName;
URI uri = null;
try {
uri = new URI ("file://" + completePath);
} catch (URISyntaxException e) {
new Notify ("Bad URI");
return;
}
try {
desktop.browse (uri);
} catch (IOException e) {
new Notify ("Could not open " + completePath);
}
}

}

