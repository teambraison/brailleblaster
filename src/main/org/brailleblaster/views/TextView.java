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
  * this program; see the file LICENSE.txt
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.views;

import org.brailleblaster.abstractClasses.AbstractContent;
import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Group;

public class TextView extends AbstractView {
public int total;

public TextView (Group documentWindow) {
// super (documentWindow, 0, 55, 12, 92);
super (documentWindow, 16, 57, 0, 100);
this.total = 0;
}

/* This is a derivative work from 
 * org.eclipse.swt.custom.DefaultContent.java 
*/

public void initializeView(){
	
}

public void addListeners(final DocumentManager dm){
	view.addTraverseListener(new TraverseListener(){
		@Override
		public void keyTraversed(TraverseEvent e) {
			if(e.keyCode == SWT.TAB && e.stateMask == SWT.CTRL){
				dm.setBrailleFocus();
			}
		}
	});

	view.addExtendedModifyListener(new ExtendedModifyListener(){
		@Override
		public void modifyText(ExtendedModifyEvent e) {
			if(dm.db.getDocumentTree() != null){
				if(e.length == 0){
					int index = dm.findClosest(view.getCaretOffset() + 1);
					dm.updateFields(index, -1);
				}
				else {
					int index = dm.findClosest(view.getCaretOffset() - 1);
					dm.updateFields(index, 1);
				}
			}
		}
	});	
}

private class TextContent extends AbstractContent {
}

}
