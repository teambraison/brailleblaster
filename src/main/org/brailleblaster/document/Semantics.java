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

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import nu.xom.Builder;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Text;
import nu.xom.Attribute;
import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.YesNoChoice;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * <p>This class provides the means of displaying and editing any
 * flavor of xml.
 * This is accomplished by using a semantic-action file for each type of 
 * xml document, such as NIMAS, epub or docbook. Displaying and editing 
 * in both the print and Braille windows are covered. No transformations 
 * are necessary.</p>
 * <p>Instances of this class may not be reused. A new instance must be 
 * created for each document.</p>
 */
class Semantics {

/** 
 * This is the parsed xml document (Containing UTDML). It is available 
 * to other classes in this package.
 */
Document workingDocument;

/**
 * Root element of workingDocument
 */
private Element rootElement;

Styles st;
Actions act;
Semantics() {
workingDocument = null;
semanticsList = null;
st = new Styles(this);
act = new Actions (this);
}

/**
 * Make the BrailleBlaster document model. The document is parsed and 
 * then the makeSemanticsList method is called to build the 
 * semanticsList. fileName is the complete path of an xml file 
 * containing UTDML. This file is produced by calling the translateFile 
 * method in liblouisutdml with "formatFor utd". It will probably be a 
 * temporary file.
 * @param fileName: The complete path to a file in which UTDML has been 
 * added to the original xml document.
 */
boolean makeSemantics (String fileName) throws Exception {
  if (workingDocument != null) {
  throw new Exception ("Attempt to reuse instance");
  }
  File file = new File (fileName);
Builder parser = new Builder();
try {
workingDocument = parser.build (file);
} catch (ParsingException e) {
new Notify("Problem processing " + fileName + " See stack trace.");
e.printStackTrace();
return false;
} catch (IOException e) {
new Notify ("Problem processing " + fileName + " See stack trace.");
e.printStackTrace();
return false;
}
  rootElement = workingDocument.getRootElement();
  makeSemanticsTable();
  if (errorCount > 0) {
  new Notify (errorMessages + errorCount + " errors found. stop.");
  errorMessages = null;
  return false;
  }
  if (!haveSemanticFile) {
  newEntries = true;
  }
  makeSemanticsList();
  outputNewEntries();
  return true;
}

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

SemanticEntry (String markup, String operation) {
this.markup = markup;
this.operation = operation;
}

void setOperand (String operand) {
this.operand = operand;
}

void setParameters (String parameters) {
this.parameters = parameters;
}

void setMacro (String macro) {
this.macro = macro;
}

void setStyle (Styles.StyleType style) {
this.style = style;
}

void setAction (String action) {
this.action = Actions.Action.valueOf(action);
}

}

/**
 * The semantic table. The semantic-action file for the type of document 
 * being processed is read and each line is used to create an entry in 
 * this table.
 */
ArrayList<SemanticEntry> semanticsTable = new 
     ArrayList<SemanticEntry>();
int semanticsCount = 0; // Number of entries in semanticsTable
SemanticEntry getSemanticEntry (int index) {
return semanticsTable.get (index);
}

/**
 * The semanticsLookup hash table has literal markup  in the 
 * semanticsTable as keys and the index of entries in the samanticsTable 
 * as values.
 */
HashMap<String, Integer> semanticsLookup = new 
  HashMap<String, Integer>();

private LocaleHandler lh = new LocaleHandler();
private boolean internetAccessRequired;
private boolean haveSemanticFile = true;
private boolean newEntries;

/**
 * Handle the namespaces entries in a semantic-action file. These are 
 * needed for evaluating XPath expressions.
 */
private void handleNamespaces (String nm) {
}

private int errorCount = 0;
private String errorMessages = "";

/**
 * Handle error messages.
 * @Param: fileName, name of semantic file.
 *  @lineNumber, line on which the error occurred.
 *   @message, error message.
 */
private void recordError (String fileName, int lineNumber, String 
message) {
errorCount++;
errorMessages = errorMessages + "error: " + fileName + ":" + lineNumber 
+ ": " + 
message + "\n";
}
 
private FileUtils fu = new FileUtils();
private String fileSep = BBIni.getFileSep();

/**
 * Compile a semantic-action file. If an include statement is 
 * encountered this method calls itself recursively.
 * @Param: filename.
 */
private boolean compileFile (String fileName) {
  String completePath = fu.findInProgramData ("semantics" + fileSep + 
  fileName);
if (completePath == null) {
  haveSemanticFile = false;
  recordError (fileName, 0, "not found");
  return false;
  }
  FileInputStream semFile  = null;
  BufferedReader br;
  try {
  semFile = new FileInputStream (completePath);
  } catch (FileNotFoundException e) {
  /* This should not happen, because findInProgramData has already
  * checked*/
  }
  String line;
  int lineNumber = 0;
  br = new BufferedReader(new InputStreamReader(semFile));
  try {
  while ((line = br.readLine()) != null) {
   lineNumber++;
   String[] parts = line.trim().split ("\\s+", 6);
  if (parts[0].charAt(0) == '#') {
  continue; //comment
  }
  if (parts.length < 2) {
  recordError (fileName, lineNumber, 
  "at least markup and an operation are required");
  continue;
  }
  if (parts[0].equalsIgnoreCase ("include")) {
  compileFile (parts[1]);
  continue;
  }
  if (parts[0].equalsIgnoreCase ("newEntries")) {
  if (parts[1].equalsIgnoreCase ("yes")) {
  newEntries = true;
  } else if (parts[1].equalsIgnoreCase ("no")) {
  newEntries = false;
  } else {
  recordError (fileName, lineNumber, "'yes' or 'no' is required.");
  }
  continue;
  }
  if (parts[0].equalsIgnoreCase ("internetAccessRequired")) {
  if (parts[1].equalsIgnoreCase ("yes")) {
  internetAccessRequired = true;
  } else if (parts[1].equalsIgnoreCase ("no")) {
  internetAccessRequired = false;
  } else {
  recordError (fileName, lineNumber, "'yes' or 'no' is required.");
  }
  continue;
  }
  SemanticEntry se = new SemanticEntry (parts[0], parts[1]);
  if (parts.length > 2) {
  se.setOperand (parts[2]);
  }
  if (parts.length > 3) {
  se.setParameters (parts[3]);
  }
  if (!(se.operation.equalsIgnoreCase ("style") || 
  se.operation.equalsIgnoreCase ("action") || se.operation.equalsIgnoreCase 
  ("macro"))) {
  recordError (fileName, lineNumber, 
  "There is no semantic operation called "
 + se.operation);
  continue;
  }
  if (se.operation.equalsIgnoreCase ("style")
  && fu.findInProgramData ("styles" + fileSep + 
  se.operand 
  + ".properties") == null) {
  recordError (fileName, lineNumber,
  "There is no style called " + se.operand);
  continue;
  }
  if (se.operation.equalsIgnoreCase ("action") && 
  !act.exists (se.operand)) {
  recordError (fileName, lineNumber,
  "There is no action called " + se.operand);
  continue;
  }
  semanticsTable.add (se);
  semanticsLookup.put (se.markup, semanticsCount);
  semanticsCount++;
  }
  } catch (IOException e) {
  new Notify ("Problem reading " + completePath);
  return false;
  }
  try {
  semFile.close();
  } catch (IOException e) {
  return false;
  }
  return true;
}
 
/**
 * This method takes the root element of workingDocument and 
 * concatenates with the path to the semantics directory, and add the 
 * ".sem" suffix. The resulting string is then passed to compileFile, 
 * which constructs the semanticsTable and the lookupSemantics hasTable.
 */
private void makeSemanticsTable() {
  internetAccessRequired = false;
  newEntries = false;
  String rootName = rootElement.getLocalName();
  String fileName = rootName + ".bbs";
  compileFile (fileName);
}

/**
 * Given the name of a style, find its markup in the semanticsTable. The 
 * search is linear. Since there may be more than one set of markup for 
 * a style, the semantic-action file must be arranged so that the preferred 
 * markup is nearest the beginning.
 */
String findStyleMarkup (String styleName) {
  for (int i = 0; i < semanticsCount; i++) {
  SemanticEntry se = semanticsTable.get(i);
  if (se.operation.equalsIgnoreCase ("style") && 
  se.operand.equalsIgnoreCase (styleName))
  return se.markup;
  }
  return null;
}

/**
 * Given the name of an actgion, find its markup in the semanticsTable. 
 * The search is linear. Since there may be more than one set of markup for 
 * an action, the semantic-action file must be arranged so that the 
 * preferred markup is nearest the beginning.
 */
String findActionMarkup (String actionName) {
  for (int i = 0; i < semanticsCount; i++) {
  SemanticEntry se = semanticsTable.get(i);
  if (se.operation.equalsIgnoreCase ("action") && 
  se.operand.equalsIgnoreCase (actionName))
  return se.markup;
  }
  return null;
}

/**
 * ArrayList for keeping a record of markup not in the semantic-actions 
 * file
 */
private ArrayList<String> newMarkup = new ArrayList<String>();

/**
 * if newEntries = true make a record of any markup that is not listed 
 * in the semanticsTable. This information will later be used to make a 
 * file containing this 
 * information.
 */
private void recordNewEntries (String markup) {
if (newEntries) {
newMarkup.add (markup);
}
}

/**
 * If any new entries have been recorded, output them to a file in the 
 * user's semantics directory. First sort them so that element names 
 * come first, then element,attribute pairs, then 
 * element,attribute,attribute,balue triplets.
 */
private void outputNewEntries() {
  if (newMarkup.size() == 0) return;
}

/**
 * This embedded class contains the semantic information for elements 
 * whose markup is in the SemanticsTable.
 */
class ElementSemantics {
Element element;
int semanticsIndex;
int start; // start of text in StyledText.
int end;
int depth;

ElementSemantics (Element element, int semanticsIndex) {
this.element = element;
this.semanticsIndex = semanticsIndex;
}

void setStart (int start) {
this.start = start;
}

void setEnc (int end) {
this.end = end;
}

void setDepth (int depth) {
this.depth = depth;
}

}

/**
 * Storage for semantic information.
 */
ArrayList<ElementSemantics> semanticsList;

/**
 * Make the starting SemanticsList immediately after the document has 
 * been parsed and the semanticsTable built.
 */
private void makeSemanticsList() {
semanticsList = new ArrayList<ElementSemantics>(1000);
doXPathExpressions();
findSemantics (rootElement, -1);
}

/**
 * Evaluate any Xpath expressions that the semanticsTable may contain 
 * and record their nodesets together with the index on the semanticsTable 
 * where the XPath expression occurred.
 */
private void doXPathExpressions() {
}

 /**
 * Walk through the parse tree, depth-first, and add elements with 
 * semannticsTable markup to semanticsList.
 */
private void findSemantics (Node node, int depth) {
Node newNode;
Element element;
String elementName;
for (int i = 0; i < node.getChildCount(); i++) {
newNode = node.getChild(i);
if (newNode instanceof Element) {
element = (Element)newNode;
int semanticsIndex;
if ((semanticsIndex = hasSemantics(element)) != -1) {
ElementSemantics elementEntry = new ElementSemantics(element, 
semanticsIndex);
elementEntry.setDepth (depth);
semanticsList.add (elementEntry);
}
elementName = element.getLocalName();
if (!(elementName.equalsIgnoreCase("brl") || elementName.equalsIgnoreCase("math"))) {
findSemantics (element, depth++);
}
}
}
}

/**
 * See if the semanticsTable contains markup for this element,its 
 * attributes, and or its attribute values. If so return the index of 
 * the entry in the semanticsTable
 * @param element, the element to check
 * @return int the index of the markup in the semanticsTable.
 */
private int hasSemantics (Element element) {
  String elementName = element.getLocalName();
  Attribute attr;
  String attrName;
  String attrValue;
  String key;
  Integer semanticsTableIndex;
  int numAttr = element.getAttributeCount();
  for (int i = 0; i < numAttr; i++) {
  attr = element.getAttribute(i);
  attrName = attr.getLocalName();
  attrValue = attr.getValue();
  key = elementName + "," + attrName + "," + attrValue;
  semanticsTableIndex = semanticsLookup.get (key);
  if (semanticsTableIndex != null) {
  return semanticsTableIndex.intValue();
  }
  else if (newEntries) {
  recordNewEntries (key);
  }
  key = elementName + "," + attrName;
  semanticsTableIndex = semanticsLookup.get (key);
  if (semanticsTableIndex != null) {
  return semanticsTableIndex.intValue();
  }
  else if (newEntries) {
  recordNewEntries (key);
  }
  }
  semanticsTableIndex = semanticsLookup.get (elementName);
  if (semanticsTableIndex != null) {
  return semanticsTableIndex.intValue();
  }
  else if (newEntries) {
  recordNewEntries (elementName);
  }
  return -1;
}
 
/**
 * Revise the semanticsList after an edit. 
 * @param startPos: the position at whichh to begin the revision.
 */
void wReviseSemanticList (int startPos) {
}
 
/**
 * Return a nodeset.
 * @param node beginning node
 * @param xpathExpr an XPath expression
 */
Nodes getNodes (Node node, String xpathExpr) {
if (node == null) {
return rootElement.query (xpathExpr);
} else {
return node.query (xpathExpr);
}
}

/**
 * Save the file with utd markup so that work can be resumed at a later 
 * time.
 * @Param fileName: The complete path of the file to which the document 
 * is to be saved.
 */
void saveWorkingFile (String fileName) {
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
new Notify(lh.localValue("cannotWriteFile"));
return;
}
}

/**
 * Enhance the original document by moving any edited Braille into the 
 * original document Remove the <brl> subtrees and the meta,name,utd 
 * element.
 * @param fileName: The complete path of the file to which the document 
 * is to be saved.
 */
void saveEnhancedDocument(String fileName) {
Node node;
Nodes nodes;
nodes = rootElement.query ("//meta[@name='brl']");
node = nodes.get(0);
node.detach();
nodes = rootElement.query ("//brl");
for (int i = 0; i < nodes.size(); i++) {
node = nodes.get(i);
node.detach();
}
nodes = null;
saveWorkingFile (fileName);
}

}

