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
 * The various actions that can be carried out on an xml document, in 
 * addition to processing styles.
 */
enum Action {
  blankSpace,
  skip,
  generic,
  cdata,
  htmlLink,
  htmlTarget,
  noTranslate,
  attrToText,
  runningHead,
  footer,
  boxLine,
  italic,
  bold,
  underline,
  compbrl,
  lineSpacing,
  blankLine,
  softReturn,
  newPage,
  brl,
  music,
  math,
  chemistry,
  graphic
};

 /**
 * This is an entry in the SemanticTable, which is used to control 
 * displaying and editing.
 */
class SemanticEntry {
  String markup;
  String operation;
  String operand;
  String parameters;
  Action action;
  Styles.StyleType style;
  String macro;
}

/**
 * The semantic table. The semantic-action file for the type of document 
 * being processed is read and each line is used to create an entry in 
 * this table.
 */
private SemanticEntry[] semanticTable = new SemanticEntry[100];

/** 
 * This is the parsed xml document (Containing UTDML).
 */
private Document parsedDocument;

/**
 * The semanticLookup hash table has literal markup  in the 
 semanticTable as keys and the index of entries in the samanticTable as 
 values.
 */
Hashtable<String, Integer> semanticLookup = new Hashtable<String, 
   Integer>();
private LocaleHandler lh = new LocaleHandler();
private boolean internetAccessRequired;
private boolean newEntries;

/**
 * Handle the namespaces entries in a semantic-action file.
 */
private void handleNamespaces (String nm) {
}
 
/**
 * Find the root element of parsedDocument and look for a file with the 
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
 * Make the BrailleBlaster document model, calling addBBSemAttr 
 * recursively. fileName is the complete path of an xml file containing 
 * UTDML.
 */
public void makeDocumentModel (String fileName) {
  File file = new File (fileName);
Builder parser = new Builder();
try {
parsedDocument = parser.build (file);
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
  Element rootElement = parsedDocument.getRootElement();
  addBBSemAttr (rootElement);
  semanticLookup = null;
}

private void doActionOrStyle (Element element) {
}

public void readAndEdit() {
}

}
