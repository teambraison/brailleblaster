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

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Text;
import nu.xom.Attribute;

/**
 * This class provides the means of displaying and editing different 
 * flavors of xml.
 * This is accomplished by using a semantic-action file for each type of 
 * xml document, such as NIMAS, epub or docbook. Displaying and editing 
 * both the print and Braille windows are covered. No transformations 
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
SemanticEntry[] semanticTable = new SemanticEntry[100];

public boolean makeSemanticsTable (Document doc) {
return true;
}

private void addBBSemAttr (Element element) {
}

public void makeDocumentModel (Document doc) {
}

private void doActionOrStyle (Element element) {
}

public void readAndEdit (Document doc) {
}

}
