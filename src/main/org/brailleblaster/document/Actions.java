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
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.document;

import nu.xom.Element;

/**
 * This class contains the actions which can be performed on elements. 
 * They are distinct from styles.
 */
class Actions {

Semantics sm;
Actions (final Semantics sm) {
this.sm = sm;
}

/**
 * The various actions that can be carried out on an xml document, in 
 * addition to processing styles.
 */
enum Action {
  blankspace,
  skip,
  generic,
  cdata,
  htmllink,
  htmltarget,
  notranslate,
  pagenum,
  attrtotext,
  runninghead,
  footer,
  boxline,
  italic,
  bold,
  underline,
  compbrl,
  linespacing,
  blankline,
  softreturn,
  brl,
  music,
  math,
  chemistry,
  graphic
};

/**
 * Check if an action exists.
 */
boolean exists (String actionName) {
  try {
  Action.valueOf (actionName);
  } catch (IllegalArgumentException e) {
  return false;
  }
  return true;
}
 
/**
 * This method is called to carry out any actions that may be needed 
 * before an elemennt is processed. If the action itself processes the 
 * element, such as skipping it and its subtree, the method returns 
 * false.
 */
boolean actionBeforeElement (Element element) {
Action act = Action.valueOf ("blankLine");
  return true;
}

/**
 * This method is called after an element and its subtree have been 
 * processed to take any action that might be necessary at this point.
 */
void actionAfterElement() {
}

}
