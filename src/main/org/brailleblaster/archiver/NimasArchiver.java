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
// Prepares Nimas Archive for opening.
public class NimasArchiver extends Archiver {

	NimasArchiver(String docToPrepare) {
		super(docToPrepare);
	}


	@Override
	public String open() {
		
		// We are able to handle this document without modifying it. Just 
		// give back the path to the working document.
		return originalDocPath;
		
	} // open()

	
	@Override
	public void save(Manager dm, String path) {
		
		// Save the document. Pretty easy, since it's just one file.
//		dm.saveAs();
		
	} // save()

} // class NimasArchiver
