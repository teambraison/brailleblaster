/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknowledged.
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
  * this program; see the file LICENSE.txt
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/
package org.brailleblaster.perspectives.braille.messages;

//Enumeration used by Message class used by the DocumentManager to redirect messages between views
public enum BBEvent {
	INCREMENT,
	DECREMENT,
	WHITESPACE_DELETION,
	EDIT,
	UPDATE,
	MERGE,
	INSERT_NODE,
	REMOVE_NODE,
	SET_CURRENT,
	GET_CURRENT,
	GET_TEXT_MAP_ELEMENTS,
	ADJUST_ALIGNMENT,
	ADJUST_INDENT,
	ADJUST_LINES,
	UPDATE_STATUSBAR,
	UPDATE_CURSORS,
	UPDATE_SCROLLBAR,
	UPDATE_STYLE,
	ADJUST_RANGE,
	SPLIT_TREE;
}
