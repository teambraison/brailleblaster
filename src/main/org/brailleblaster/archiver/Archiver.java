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

package org.brailleblaster.archiver;

import org.brailleblaster.perspectives.braille.Manager;

//////////////////////////////////////////////////////////////////////////////////
// Archiver gives methods for opening/handling particular document types. 
// Some of these types have special needs, and therefore, specific 
// implementations. This class is ABSTRACT, and is to be used as a 
// base for other archivers.
abstract public class Archiver {
	
	String originalDocPath;
	String unzippedDocPath;
	String workingDocPath; 
	
	// Get-er for original document path.
	public String getOrigDocPath() { return originalDocPath; }
	
	//////////////////////////////////////////////////////////////////////////////////
	// Constructor. Stores path to document to prepare.
	Archiver(String docToPrepare)
	{
		// Store paths.
		originalDocPath = docToPrepare;
		workingDocPath = originalDocPath;
		
	}
	//////////////////////////////////////////////////////////////////////////////////
	// 
	public abstract String open();
	//////////////////////////////////////////////////////////////////////////////////
	// 
	public abstract void save(Manager dm, String path);
	
} // class Archiver