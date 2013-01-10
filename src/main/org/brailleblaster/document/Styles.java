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

import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import nu.xom.Element;
import org.brailleblaster.BBIni;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import java.io.IOException;
import java.io.FileNotFoundException;


/** 
 * This class contains the data structures and mothods used for handling 
 * styles.
 * <p>Styles are used in the print window only. The Braille window will 
 * already have been 
 * formatted by liblouisutdml. The rationale behind the use of styles is 
 * that if the print window looks good visually and the correct markup 
 * is used in the document, the Braille will also be formatted 
 * correctly.</p>
 * <p>Note that BrailleBlaster styles deal with layout. Italic, bold, 
 * etc. are dealt with by action methods in the Actions class.</p>
 */
class Styles {

FileUtils fu = new FileUtils();
String fileSep = BBIni.getFileSep();

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
  "contentsHeader",
  "contents1",
  "contents2",
  "contents3",
  "contents4",
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
  String name = "";
  int linesBefore = 0;
  int linesAfter = 0;
  int leftMargin = 0;
  int rightMargin= 0;
  int firstLineIndent = 0;
  StyleFormat format = StyleFormat.leftJustified;
}

/**
 * This methods writes the style as a properties file on 
 * userProgramDataPath/styles.
 */
void writeStyle (StyleType st) {
  String fileName = BBIni.getUserProgramDataPath() + 
  fileSep + "styles" + fileSep + st.name + ".properties";
  fu.create (fileName);
Properties prop = new Properties();
try {
prop.load(new FileInputStream(fileName));
} catch (FileNotFoundException e) {
new Notify(e.getMessage());
return;
} catch (IOException e) {
new Notify(e.getMessage());
return;
}
  prop.setProperty ("name", st.name);
  prop.setProperty ("linesBeforee", Integer.toString (st.linesBefore));
  prop.setProperty ("linesAfter", Integer.toString (st.linesAfter));
  prop.setProperty ("leftMargin", Integer.toString (st.leftMargin));
  prop.setProperty ("rightMargin", Integer.toString (st.rightMargin));
  prop.setProperty ("firstLineIndent", 
  Integer.toString (st.firstLineIndent));
  prop.setProperty ("format", st.format.toString());
try {
prop.store(new FileOutputStream (fileName), null);
} catch (FileNotFoundException e) {
new Notify(e.getMessage());
} catch (IOException e) {
new Notify(e.getMessage());
}
}

/**
 * Read a style from a properties file in programData and create a new 
 * instance of StyleType with all fields initialized from the file.
 */
StyleType readStyle (String styleName) {
  String fileName = fu.findInProgramData ("styles" + fileSep + 
  styleName 
  + ".properties");
Properties prop = new Properties();
try {
prop.load(new FileInputStream(fileName));
} catch (FileNotFoundException e) {
new Notify ("There is no style named " + styleName);
return null;
} catch (IOException e) {
new Notify(e.getMessage());
return null;
}
  StyleType st = new StyleType();
  st.name = prop.getProperty ("name");
   st.linesBefore = Integer.parseInt (prop.getProperty ("linesBefore"));
  st.linesAfter = Integer.parseInt (prop.getProperty ("linesAfter"));
  st.leftMargin = Integer.parseInt (prop.getProperty ("leftMargin"));
  st.rightMargin = Integer.parseInt (prop.getProperty ("rightMargin"));
  st.firstLineIndent = Integer.parseInt (prop.getProperty 
  ("firstLineIndent"));
  st.format = st.format.valueOf (prop.getProperty ("format"));
  return st;  
}

/**
 * Edit a StyleType. This is done in a dialog box.
 */
void editStyle (StyleType st) {
}

/**
 * An item on the style stack, which is used to handle nested styles. It 
 * is an fifo stack.
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
 * Index of the top item on the style stack.
 */
private int styleTop;

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
boolean startStyle (Element element) {
  return true;
}

/**
 * Completes the processing of the styleRecord on the top of the stack 
 * and pops it.
 */
void endStyle () {
}

/**
 * During editing this method is called to add a new style element to 
 * the parse tree at the current location. The element may be the root of a 
 * subtree. For example, its children may be emphasized or MathML 
 * expressions or images.
 */
void applyStyle (Element element) {
}

}

