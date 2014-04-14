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

package org.brailleblaster.wordprocessor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class BBProgressBar {
	ProgressBar pb;
	int maximum = 100;
	boolean done;
	
	public BBProgressBar(Shell shell){
		this.pb = new ProgressBar(shell, SWT.SMOOTH);
		FormData location = new FormData();
		location.left = new FormAttachment(0);
		location.right = new FormAttachment(6);
		location.bottom = new FormAttachment(100);
		this.pb.setLayoutData (location);
		this.pb.setSelection(0);
		this.pb.setVisible(false);
		this.pb.setMaximum(this.maximum);
		this.done = false;
	}
	
	public void start(){
		pb.setVisible(true);
		//pb.getDisplay().getCurrent().update();	
		 for (int i=0; i<=pb.getMaximum (); i++) {
			    try {
			    	Thread.sleep (50);
			    } catch (Throwable th) 
			    {
			    	
			    }
			    pb.setSelection (i);
		}
	}
	
	public void stop(){
		this.pb.setVisible(false);
		this.pb.setSelection(0);
	}
}
