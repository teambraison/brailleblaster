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

import nu.xom.Element;

/** 
 * This class contains the data structures and mothods used for handling 
 *styles.
 * <p>Styles are used in the print window only. The Braille window will 
 * already have been 
 * formatted by liblouisutdml. The rationale behind the use of styles is 
 * that if the print window looks good visually and the correct markup 
 * is used in the document, the Braille will also be formatted 
 * correctly.<p>

 * <p>Note that BrailleBlaster styles deal with layout. Italic, bold, 
 * etc. are dealt with by action methods in the Semantics class.</p>
*/

public class Styles {

  /**
    * Reserved styles are defined by the developers. If the user 
    * redefines them the changed styles go in her/his directories.
    */
String[] reservedStyles = {
  "document",
  "para",
  "heading1",
  "heading2",
  "heading3",
  "heading4",
  "heading5",
  "heading6",
  "heading7",
  "heading8",
  "heading9",
  "heading10",
  "contentsHeader",
  "contents1",
  "contents2",
  "contents3",
  "contents4",
  "conents5",
  "contents6",
  "contents7",
  "contents8",
  "contents9",
  "contents10"
  };

/** 
 * The different formats that can be applied to styles.
 */  
enum StyleFormat {
  inherit,
  leftJustified,
  rightJustified,
  centered,
  alignColumnsLeft,
  alignColumnsRight,
  listColumns,
  listLines,
  computerCoded,
  contents
};

/**
 * The status items mark the various stages in the processing of a 
 * style.
 */
enum StyleStatus {
  error,
  beforeBody,
  startBody,
  resumeBody,
  bodyInterrupted,
  afterBody,
};

/**
  * This embedded class defines the various fields that specify a style.
  */
class StyleType {
  int linesBefore;
  int linesAfter;
  int leftMargin;
  int rightMargin;
  boolean keepWithNext;
  boolean dontSplit;
  boolean orphanControl;
  int firstLineIndent;
  StyleFormat format;
  boolean newPageBefore;
  boolean newPageAfter;
}

/**
 * An item on the style stack, which is used to handle nested styles.
 */
private class StyleRecord {
  StyleType style;
  StyleStatus status;
  StyleFormat curStyleFormat;
  Element curElement;
  int curLeftMargin;
  int curRightMargin;
  int curFirstLineIndent;
}

/**
 * The stack array is managed as a fifo stack to handle nested styles. 
 * For example, we might push a table style onto the stack in the startStyle 
 * method. Each row would then be pushed onto the stack by StartStyle 
 * and removed by endStyle. When the last row has been processed, the top  of 
 * the stack will contain the table style, which is then completed by 
 * endStyle.
 */
private StyleRecord[] stack = new StyleRecord[20];

/**
 * Begins the processing of a style and places a strylRecord on the top 
 * of the stack. If the element does not have a style it returns false.
 */
public boolean startStyle (Element element) {
  return true;
}

/**
 * Completes the processing of the styleRecord on the top of the stack 
 and pops it.
 */
public void endStyle () {
}

/**
 * During editing this method is called to add a new element to the 
 * parse tree at the current location. The element may be the root of a 
 * subtree. For example, its children may be emphasized or MathML 
 * expressions or images.
 */
public void applyStyle (String style, Element element) {
}

}

