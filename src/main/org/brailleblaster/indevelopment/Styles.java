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

package indevelopment;

import nu.xom.Element;

public class Styles {

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

enum StyleStatus {
  error,
  beforeBody,
  startBody,
  resumeBody,
  bodyInterrupted,
  afterBody,
};

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

class StyleRecord {
  StyleType style;
  StyleStatus status;
  StyleFormat curStyleFormat;
  Element curElement;
  int curLeftMargin;
  int curRightMargin;
  int curFirstLineIndent;
}

StyleRecord[] stack = new StyleRecord[20];

public void applyStyle (String style, String text) {
}

}

