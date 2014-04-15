// Chuck M. - A line of text within a config file. It could be a comment, label, or variable with associated values.

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
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
 */

package org.brailleblaster.settings;

public class ConfigEntry {
	// The full line of text.
	String line;
	// Variable name and value.
	String name;
	String value;
	// Index/line number within config file.
	int lineNumber;
	// Index in combo box.
	int comboIndex;
	
	/////////////////////////////////////////////////////////
	// Constructor.
	public ConfigEntry()
	{
		line = new String();
		name = new String();
		value = new String();
		lineNumber = -1;
		comboIndex = -1;
	}
}
