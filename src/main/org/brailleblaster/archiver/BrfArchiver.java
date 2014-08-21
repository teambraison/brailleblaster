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
package org.brailleblaster.archiver;

import org.brailleblaster.document.BBDocument;

//not yet fully implemented
public class BrfArchiver extends Archiver{

	BrfArchiver(String docToPrepare) {
		super(docToPrepare);
		currentConfig = "preferences.cfg";
		//if implemented, set currentConfig
	}

	@Override
	public void save(BBDocument doc, String path) {
		if(path == null)
			path = workingDocPath;
		
		saveBrf(doc, path);
	}

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// Each Archiver type opens and saves their content in a 
	// different way. They will implement this method to 
	// save their content to the temp folder.
	@Override
	public void backup(BBDocument __doc, String __path) {}
}
