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

package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.brailleblaster.localization.LocaleHandler;

public class BBToolBar {

private ToolBar toolBar;

public BBToolBar (DocumentManager dm) {
LocaleHandler lh = new LocaleHandler();
toolBar = new ToolBar (dm.documentWindow, SWT.HORIZONTAL);
FormData location = new FormData();
location.left = new FormAttachment(0);
location.right = new FormAttachment(100);
location.top = new FormAttachment (4);
toolBar.setLayoutData (location);
ToolItem translateItem = new ToolItem (toolBar, SWT.PUSH);
translateItem.setText (lh.localValue ("translate"));
ToolItem embossNow = new ToolItem (toolBar, SWT.PUSH);
embossNow.setText (lh.localValue ("embossNow"));
ToolItem embossWithInk = new ToolItem (toolBar, SWT.PUSH);
embossWithInk.setText (lh.localValue ("EmbossWithInk"));
ToolItem openItem = new ToolItem (toolBar, SWT.PUSH);
openItem.setText (lh.localValue ("Open"));
ToolItem saveItem = new ToolItem (toolBar, SWT.PUSH);
saveItem.setText (lh.localValue ("Save"));

toolBar.pack();
}

}

