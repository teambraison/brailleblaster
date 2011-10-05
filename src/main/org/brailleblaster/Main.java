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

package org.brailleblaster;

import org.liblouis.liblouisutdml;
import org.brailleblaster.wordprocessor.WPManager;
import org.brailleblaster.localization.LocaleHandler;
import java.io.IOException;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;

/**
* This class contains the main method. If there are no arguments it 
* passes control directly to the word processor. If there are arguments 
* it passes them to the constructor of the class Subcommands. It will do more 
* processing as the project develops.
*/

public class Main {
public static void main (String[] args) {
BBIni.initialize(args);
BBIni.setVersion ("brailleblaster-1.3.0");
BBIni.setReleaseDate ("October 5, 2011");
if (args.length == 0)
new WPManager (null);
else
{
new Subcommands (args);
}
Display display = BBIni.getDisplay();
if (display != null)
display.dispose();
if (BBIni.haveLiblouisutdml())
{
liblouisutdml louisutdml = liblouisutdml.getInstance ();
louisutdml.free();
}
}

}
