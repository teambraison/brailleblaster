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
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

public class BBStatusBar {

	private Label statusBar;

	public BBStatusBar (Shell documentWindow) {
		statusBar = new Label (documentWindow, SWT.BORDER);
		FormData location = new FormData();
		location.left = new FormAttachment(0);
		location.right = new FormAttachment(100);
		location.bottom = new FormAttachment(100);
		statusBar.setLayoutData (location);
	}

	public void setText (String text) {
		statusBar.setText (text);
	}
	
	public void resetLocation(int left, int right, int bottom){
		FormData data = (FormData)this.statusBar.getLayoutData();
		data.left = new FormAttachment(left);
		data.right = new FormAttachment(right);
		data.bottom = new FormAttachment(bottom);
		statusBar.setLayoutData (data);
		this.statusBar.getParent().layout();
	}
}

