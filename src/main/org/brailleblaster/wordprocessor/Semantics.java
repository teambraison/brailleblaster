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

package org.brailleblaster.wordprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import nu.xom.Builder;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Text;
import nu.xom.Attribute;
import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.YesNoChoice;
import java.util.Hashtable;

/**
 * This class provides the means of displaying and editing any
 * flavor of xml.
 * This is accomplished by using a semantic-action file for each type of 
 * xml document, such as NIMAS, epub or docbook. Displaying and editing 
 * in both the print and Braille windows are covered. No transformations 
 * are necessary.
 * See also Styles.java
 */
class Semantics {

 /**
 * This is an entry in the SemanticsTable, which is used to control 
 * displaying and editing.
 */
class SemanticEntry {
  String markup;
  String operation;
  String operand;
  String parameters;
  Actions.Action action;
  Styles.StyleType style;
  String macro;
}

/**
 * The semantic table. The semantic-action file for the type of document 
 * being processed is read and each line is used to create an entry in 
 * this table.
 */
private SemanticEntry[] semanticsTable = new SemanticEntry[100];
int lineCount = 0;

/** 
 * This is the parsed xml document (Containing UTDML).
 */
private Document workingDocument;

/**
 * The semanticsLookup hash table has literal markup  in the 
 * semanticsTable as keys and the index of entries in the samanticsTable 
 * as values.
 */
private Hashtable<String, Integer> semanticsLookup = new 
  Hashtable<String, Integer>();

private StyleType st = new StyleType();
private Actions act = new Actions();
private LocaleHandler lh = new LocaleHandler();
private boolean internetAccessRequired;
private boolean haveSemanticFile = true;
private boolean newEntries;

/**
 * Handle the namespaces entries in a semantic-action file.
 */
private void handleNamespaces (String nm) {
}

/**
 * Handle error messages.
 * Param: fileName, name of semantic file.
 *        lineNumber, line on which the eror occurred.
 *        message, error message.
 */
private void showErrors (String fileName, int lineNumber, String 
message) {
System.out.println (fileName + ":" + lineNumber + ": " + message);
}
 
/**
 * Compile a semantic-action file. If an include statement is 
 * encountered this method calls itself recursively.
 * Param: filename.
 */
private boolean compileFile (String fileName) {
  FileInputStream semFile;
  try {
  semFile = new FileInputStream (fileName);
  } catch (FileNotFoundException e) {
  haveSemanticFile = false;
  return false;
  }
  byte[] bytebuf = new byte[1024];
  int numbytes = 0;
  String line;
  boolean isCrNext = false;
  boolean isComment = false;
  int ch = 0;
  int prevch = 0;
  int lineNumber = 0;
  while (true) {
  numbytes = 0;
  prevch = 0;
  isComment = false;
  lineNumber++;
  while (true) {
  try {
  ch = semFile.read();
  } catch (IOException e) {
  return false;
  }
  if (ch == -1) {
  break;
  }
  ch &= 0xff;
  if (numbytes == 0 && ch <= 32) {
  continue;
  }
  if (ch == 13 && isCrNext) {
  isCrNext = false;
  continue;
  }
  if (ch == '#') {
  isComment = true;
  }
  if (ch == 10 || ch == 13) {
  numbytes--;
  if (prevch == '\\') {
  isCrNext = true;
  continue;
  }
  break;
  }
  prevch = ch;
  bytebuf[numbytes++] = (byte)ch;
  }
  if (ch == -1) {
  break;
  }
  if (isComment) {
  continue;
  }
  line = new String (bytebuf, numbytes);
  String[] parts = line.split (line, 6);
  if (parts[0].equals ("include")) {
  compileFile (parts[1]);
  continue;
  }
  if (parts[0].equals ("newEntries") && parts[1].equals ("yes")) {
  newEntries = true;
  continue;
  }
  if (parts[0].equals ("internetAccessRequired") && parts[1].equals 
  ("yes")) {
  internetAccessRequired = true;
  continue;
  }
  try {
  semanticsTable[lineCount].markup = parts[0];
  } catch (ArrayIndexOutOfBoundsException e) {
  showErrors (fileName, lineNumber, "Too many semantic entries.");
  return false;
  }
  semanticsLookup.put (semanticsTable[lineCount].markup, lineCount);
  semanticsTable[lineCount].operation = parts[1];
  semanticsTable[lineCount].operand = parts[2];
  semanticsTable[lineCount].parameters = parts[3];
  lineCount++;
  }
  try {
  semFile.close();
  } catch (IOException e) {
  return false;
  }
  return true;
}
 
/**
 * This method finds the root element of workingDocument and 
 * concatenates with the path to the semantics directory, and add the 
 * ".sem" suffix. The resulting string is then passed to compileFile, 
 * which constructs the semanticsTable and the sookupSemantics hasTable.
 */
private void makeSemanticsTable() {
  internetAccessRequired = false;
  newEntries = false;
  Element rootElement = workingDocument.getRootElement();
  String rootName = rootElement.getLocalName();
  String fileName = rootName + ".sem";
  compileFile (fileName);
}

/**
 * Given the name of a style, find its markup in the semanticsTable. The 
 * search is linear. Since there may be more than one set of markup for 
 * a style, the semantic-action file must be arranged so that the preferred 
 * markup is nearest the beginning.
 */
public String findStyleMarkup (String styleName) {
  for (int i = 0; i < lineCount; i++) {
  if (semanticsTable[i].operation.equals ("style") && 
  semanticsTable[i].operand.equals (styleName))
  return semanticsTable[i].markup;
  }
  return null;
}

/**
 * Given the name of an actgion, find its markup in the semanticsTable. 
 The 
 * search is linear. Since there may be more than one set of markup for 
 * an action, the semantic-action file must be arranged so that the 
 * preferred markup is nearest the beginning.
 */
public String findActionMarkup (String actionName) {
  for (int i = 0; i < lineCount; i++) {
  if (semanticsTable[i].operation.equals ("action") && 
  semanticsTable[i].operand.equals (actionName))
  return semanticsTable[i].markup;
  }
  return null;
}

/**
 * if newEntries = true make a record of any markup that is not listed 
 * in the semanticsTable. This information will later be used to make a 
 * file containing this 
 * information.
 */
private void recordNewEntries (String markup) {
}

/**
 * If any new entries have been recorded, output them to a file in the 
 * user's semantics directory. First sort them so that element names 
 * come first, then element,attribute pairs, then 
 * element,attribute,attribute,balue triplets.
 */
private void outputNewEntries() {
}

/**
 * Evaluate any Xpath expressions that the semanticsTable may contain 
 and 
 * add the bbsem attribute to the nodes in the nodeset. The bbsem 
 * attribute has the index of the entry in the semanticsTable as its 
 * valuel
 */
private void doXPathExpressions() {
}

/**
 * Add the bbsem attribute to nodes in the parse tree.  Any XPath 
 * expressions in the semanticsTable have already been aplied. The 
 * parse tree is traversed, and the semanticsTable is checked for 
 matching
 * markup. If found, a bbsem attribute with a value of the index in the 
 * semanticsTable is added to the element node. The attribute is not 
 * added if it is already present because it was set by an XPath 
 * expression.
 */
private void addBBSemAttr (Node node) {
Node newNode;
for (int i = 0; i < node.getChildCount(); i++) {
newNode = node.getChild(i);
if (newNode instanceof Element) {
Element element = (Element)newNode;
/* Check if in SemanticsTable. If so, add bbsem attribute, unless 
 * already there.*/
if (element.getAttribute ("bbsem") == null) {
helpAddAttr (element);
}
/* Process this element recursively */
addBBSemAttr (element);
}
}
}

/**
 * This is a helper method for addBBSemAttr. It scans through 
 * attributges to see if elementName,attrName,attrValue or 
 * elementName,attrrName match some markup0 in semanticsTable. If so, it 
 * adds the bbsem attribute with the index of the semanticEntry to the 
 * element and returns. If not 
 * it checks to see if the elementName alone 
 * matches and adds the bbsem attribute if it does. It then returnsj.
 * param element: the element to be checked.
 */
private void helpAddAttr (Element element) {
  String elementName = element.getLocalName();
  Attribute attr;
  String attrName;
  String attrValue;
  String key;
  Attribute bbsemAttr = new Attribute("bbsem", "99");
  Integer semanticsTableIndex;
  int numAttr = element.getAttributeCount();
  for (int i = 0; i < numAttr; i++) {
  attr = element.getAttribute(i);
  attrName = attr.getLocalName();
  attrValue = attr.getValue();
  key = elementName + "," + attrName + "," + attrValue;
  semanticsTableIndex = semanticsLookup.get (key);
  if (semanticsTableIndex != null) {
  bbsemAttr.setValue (semanticsTableIndex.toString());
  element.addAttribute (bbsemAttr);
  return;
  }
  else if (newEntries) {
  recordNewEntries (key);
  }
  key = elementName + "," + attrName;
  semanticsTableIndex = semanticsLookup.get (key);
  if (semanticsTableIndex != null) {
  bbsemAttr.setValue (semanticsTableIndex.toString());
  element.addAttribute (bbsemAttr);
  return;
  }
  else if (newEntries) {
  recordNewEntries (key);
  }
  }
  semanticsTableIndex = semanticsLookup.get (elementName);
  if (semanticsTableIndex != null) {
  bbsemAttr.setValue (semanticsTableIndex.toString());
  element.addAttribute (bbsemAttr);
  }
  else if (newEntries) {
  recordNewEntries (elementName);
  }
}

/**
 * The complete path of the document file.
 */
private String documentName;

/**
 * Make the BrailleBlaster document model, calling addBBSemAttr 
 * recursively. fileName is the complete path of an xml file containing 
 * UTDML.
 */
void makeDocumentModel (String fileName) {
  documentName = fileName;
  File file = new File (fileName);
Builder parser = new Builder();
try {
workingDocument = parser.build (file);
} catch (ParsingException e) {
new Notify(lh.localValue("malformedDocument"));
return;
} catch (IOException e) {
new Notify(lh.localValue("couldNotOpen") + " " + fileName);
return;
}
  makeSemanticsTable();
  if (!haveSemanticFile) {
  newEntries = true;
  }
  doXPathExpressions();
  Element rootElement = workingDocument.getRootElement();
  addBBSemAttr (rootElement);
  semanticsLookup = null;
}

/**
 * This method is used by the readAndEdit method to carry out the 
 * appropriate operations for each element having a bbsem attribute.
 */
private void doSemantics (Element element) {
  String semanticsTableIndex = element.getAttributeValue ("bbsem");
  if (semanticsTableIndex == null) {
  return;
  }
}

/**
 * This method enables the user to read and edit the contents of the 
 * document. It moves around in the parse tree, following the user's 
 * scrolling and cursor movements. The doStyleOrAction method is called 
 * for each element having a bbsem attribute.
 */
public void readAndEdit() {
}

/**
 * This method takes an alment, usually the root element, and removes 
 * the bbsem attribute from its subtree.
 *      @param element
 */
private void removeBBSemAttr (Node node) {
Node newNode;
for (int i = 0; i < node.getChildCount(); i++) {
newNode = node.getChild(i);
if (newNode instanceof Element) {
Element element = (Element)newNode;
Attribute attr = element.getAttribute ("bbsem");
if (attr != null) {
element.removeAttribute (attr);
}
removeBBSemAttr (element);
}
}
}

/**
 * Save the file with utd markup so that work can be resumed at a later 
 * time. The bbsem attribute is removed. 
 */
public void saveWorkikngFile () {
  String fileName = documentName + ".utd";
  removeBBSemAttr (workingDocument.getRootElement());
  FileOutputStream writer = null;
  try {
writer = new FileOutputStream(fileName);
} catch (FileNotFoundException e) {
new Notify(lh.localValue("cannotOpenFileW") + " " + fileName);
return;
}
Serializer outputDoc = new Serializer(writer);
try {
outputDoc.write(workingDocument);
outputDoc.flush();
} catch (IOException e) {
//logger.log(Level.SEVERE, lh.localValue("cannotWriteFile") + ": " + 
new Notify(lh.localValue("cannotWriteFile"));
return;
}
}

/**
 * Enhance the original document by moving any edited Braille into the 
 * print portion, with appropriate markup. Remove the bbsem attribute 
 * and the meta,name,utd element.
 */
public void saveEnhancedDocument() {
}

}
