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

package newcode;

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
public class Semantics {

 /**
 * This is an entry in the SemanticTable, which is used to control 
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
private SemanticEntry[] semanticTable = new SemanticEntry[100];
int lineCount = 0;

/** 
 * This is the parsed xml document (Containing UTDML).
 */
private Document workingDocument;

/**
 * The semanticLookup hash table has literal markup  in the 
 * semanticTable as keys and the index of entries in the samanticTable 
 * as values.
 */
private Hashtable<String, Integer> semanticLookup = new 
  Hashtable<String, Integer>();

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
  while (true) {
  numbytes = 0;
  prevch = 0;
  isComment = false;
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
  try {
  } catch (ArrayIndexOutOfBoundsException e) {
  semanticTable[lineCount].markup = parts[0];
  return false;
  }
  semanticLookup.put (semanticTable[lineCount].markup, lineCount);
  semanticTable[lineCount].operation = parts[1];
  semanticTable[lineCount].operand = parts[2];
  semanticTable[lineCount].parameters = parts[3];
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
 * Find the root element of workingDocument and look for a file with the 
 * root name concatenated with .sem in the semantics directory. Read 
 * this file into semanticTable, separating the various strings and putting 
 * them into their proper fields. Then construct the semanticLookup
 * Hashtable. If no semantic-action file is found construct a bare-bones 
 * semanticTable, and output a prototype semantic-action file in the 
 * user's semantics directory. Show a dialog box informing the user of 
 * the situation.
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
 * if newEntries = true make a record of any markup that is not listed 
 * in the semanticTable. This information will later be used to make a 
 * file containing this 
 * information.
 */
private void recordNewEntries (String newEntry) {
}

/**
 * If any new entries have been recorded, output them to a file in the 
 user's semantics directory. First sort them so that element names come 
 first, then element,attribute pairs, then 
 element,attribute,attribute,balue triplets.
 */
private void outputNewEntries() {
}

/**
 * Evaluate any Xpath expressions that the semanticTable may contain and 
 add the bbsem attribute to the nodes in the nodeset. The bbsem attribut 
 has the index of the entry in the semanticTable as its valuel
 */
private void doXPathExpressions() {
}

/**
 * Add the bbsem attribute to nodes in the parse tree.  Any XPath 
 * expressions in the semanticTable have already been aplied. The 
 * parse tree is traversed, and the semanticTable is checked for matching
 * markup. If found, a bbsem attribute with a value of the index in the 
 * semanticTable is added to the element node.
 */
private void addBBSemAttr (Element element) {
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
public void makeDocumentModel (String fileName) {
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
  file = null;
  makeSemanticsTable();
  doXPathExpressions();
  Element rootElement = workingDocument.getRootElement();
  addBBSemAttr (rootElement);
  semanticLookup = null;
}

/**
 * This method is used by the readAndEdit method to carry out the 
 * appropriate operations for each element having a bbsem attribute.
 */
private void doActionOrStyle (Element element) {
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
 * Remove the bbsem attribute before saving the document. The method 
 * walks through the parse tree, calling itself recursively for each 
 * element.
 */
private void removeBBSemAttr() {
}

/**
 * Save the file with utd markup so that work can be resumed at a later 
 * time. The bbsem attribute is removed. 
 */
public void saveWorkikngFile () {
  String fileName = documentName + ".utd";
  removeBBSemAttr();
}

/**
 * Enhance the original document by moving any edited Braille into the 
 print portion, with appropriate markup. Remove the bbsem attribute and 
 * the meta,name,utd element.
 */
public void saveEnhancedDocument() {
  removeBBSemAttr();
}

}
