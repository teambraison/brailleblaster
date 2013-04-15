/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
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

package org.brailleblaster.util;


/** The TextNodeItem represents a text node in an XML DOM that is to be rendered in a text edit control. Generally a TextNodeItem is used in a TextNodeMap to keep track of where in the DOM modifications need to be made when text is edited.
 * 
 *
 */
public class TextNodeItem {
	private static final String TAG = "TextNodeItem";
	

// Style to be used when rendering the text. It might just be "block" or "inline" or an "action from a semantic action file. 
	private int style = -1;

	 //	  Position of this text node, in document order, within the XML DOM tree. 
	private int index = -1;

	 // Offset of the text contained in this text node within the context of the text editor.
	private int startOffset = -1;

	// Offset of the end of text contained in this text node within the context of the text editor.
private int endOffset = -1;
	
	public void setStyle (int val) {
		style = val;
	}
	
	public int getStyle () {
		return style;
	}
	
	public void setIndex (int val) {
		index = val;
	}
	
	public int getIndex () {
		return index;
	}

	public void setRange (int start, int end) {
		startOffset = start;
		endOffset = end;
	}
	
	public int getstart () {
		return startOffset;
	}

	public int getEnd () {
		return endOffset;
	}
}
