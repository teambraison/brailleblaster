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

package org.brailleblaster.abstractClasses;

import java.util.LinkedList;

import nu.xom.Text;

import org.brailleblaster.views.mapElement;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.events.*;

public abstract class AbstractView {
	public StyledText view;
	public boolean hasFocus = false;
	public boolean hasChanged = false;
	
	public AbstractView() {
	}

	public AbstractView(Group group, int left, int right, int top, int bottom) {
		view = new StyledText(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);

		FormData location = new FormData();
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		view.setLayoutData(location);

		// view.addVerifyKeyListener (new VerifyKeyListener() {
		// public void verifyKey (VerifyEvent event) {
		// handleKeystrokes (event);
		// }
		// });

		view.addModifyListener(viewMod);
	}

	// Better use a ModifyListener to set the change flag.
	ModifyListener viewMod = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			hasChanged = true;
		}
	};
	
	void handleKeystrokes(VerifyEvent event) {
		hasChanged = true;
		event.doit = true;
	}
	
	public abstract void initializeView();
}
