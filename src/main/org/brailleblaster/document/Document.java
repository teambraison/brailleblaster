/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org
  *
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

package org.brailleblaster.document;

import java.io.InputStream;

public class Document {

private Semantics sm = new Semantics();
private Styles st = new Styles();
private Actions act = new Actions();

public void startDocument (InputStream inputStream) throws Exception {
String fileName = "xxx";
sm.makeDocumentModel (fileName);
}

public void saveWorkingFile (String completePath) {
sm.saveWorkingFile (completePath);
}

public void saveEnhancedDocument (String completePath) {
sm.saveEnhancedDocument (completePath);
}

public void editCreateStyle (String styleName) {
Styles.StyleType styleType = st.readStyle (styleName);
st.editStyle (styleType);
st.writeStyle (styleType);
}

}
