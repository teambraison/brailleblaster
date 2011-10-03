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

import nu.xom.*;
import org.brailleblaster.util.Notify;
import java.io.IOException;

class NewDocument {
Document doc;
Element headAdd;
Element frontAdd;
Element bodyAdd;
Element rearAdd;

private String framework =
"<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"
+ "<dtbook>"
+ "<head/>"
+ "<book><frontmatter/>"
+ "<bodymatter><level><h1/>"
+ "</level></bodymatter>"
+ "<rearmatter/></book></dobook>";

NewDocument() {
Builder builder = new Builder();
try {
doc = builder.build (framework, null);
} catch (ParsingException e) {
new Notify ("Framework is malformed");
return;
} catch (IOException e) {
}
Element rootElement = doc.getRootElement();
findAddChildPoints (rootElement);
}

private void findAddChildPoints (Node node) {
Node newNode;
Element elementNode = null;
String name;
for (int i = 0; i < node.getChildCount(); i++) {
newNode = node.getChild(i);
if (newNode instanceof Element) {
elementNode = (Element)newNode;
name = elementNode.getLocalName();
if (name.equals ("head")) {
headAdd = elementNode;
} else if (name.equals ("frontmatter")) {
frontAdd = elementNode;
} else if (name.equals ("level")) {
bodyAdd = elementNode;
} else if (name.equals ("rearmatter")) {
rearAdd = elementNode;
}
}
findAddChildPoints (newNode);
}
}

}

