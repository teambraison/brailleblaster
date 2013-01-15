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

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class BBToolBar {

private ToolBar toolBar;

// FO
public BBToolBar (final DocumentManager dm) {
LocaleHandler lh = new LocaleHandler();
toolBar = new ToolBar (dm.documentWindow, SWT.HORIZONTAL);
FormData location = new FormData();
location.left = new FormAttachment(0);
location.right = new FormAttachment(50);
location.top = new FormAttachment (4);
toolBar.setLayoutData (location);
// FO
String tlabel;

ToolItem openItem = new ToolItem (toolBar, SWT.PUSH);
tlabel = lh.localValue ("&Open");
openItem.setText (tlabel.replace("&", ""));
openItem.addSelectionListener (new SelectionAdapter() {
  public void widgetSelected (SelectionEvent e) {
      if (BBIni.debugging()) {
          dm.setReturn (WP.OpenDocumentGetFile);
      } else {
          dm.fileOpen();
      }
  }
});         

ToolItem saveItem = new ToolItem (toolBar, SWT.PUSH);
//FO
tlabel = lh.localValue ("&Save");
saveItem.setText (tlabel.replace("&",""));
saveItem.addSelectionListener (new SelectionAdapter() {
  public void widgetSelected (SelectionEvent e) {
	  dm.fileSave();
  }
});

ToolItem translateItem = new ToolItem (toolBar, SWT.PUSH);
// FO
tlabel = lh.localValue ("&Translate");
translateItem.setText (tlabel.replace("&", ""));
// FO
translateItem.addSelectionListener (new SelectionAdapter() {
    public void widgetSelected (SelectionEvent e) {
        dm.translateView(true);
    }
});

ToolItem embossNow = new ToolItem (toolBar, SWT.PUSH);
// FO
tlabel = lh.localValue ("Emboss&Now!");
embossNow.setText (tlabel.replace ("&", ""));
embossNow.addSelectionListener (new SelectionAdapter() {
    public void widgetSelected (SelectionEvent e) {
        dm.fileEmbossNow();
    }
});

/**
ToolItem embossWithInk = new ToolItem (toolBar, SWT.PUSH);
tlabel = lh.localValue ("EmbossInkN&ow");
embossWithInk.setText (tlabel.replace ("&", ""));
embossWithInk.setEnabled(false); 
embossWithInk.addSelectionListener (new SelectionAdapter() {
    public void widgetSelected (SelectionEvent e) {
        dm.placeholder();
    }
});
**/

ToolItem daisyPrint = new ToolItem (toolBar, SWT.PUSH);
tlabel = lh.localValue ("&Print");
daisyPrint.setText (tlabel.replace ("&", ""));
daisyPrint.addSelectionListener (new SelectionAdapter() {
    public void widgetSelected (SelectionEvent e) {
        dm.daisyPrint();
    }
});

toolBar.pack();

FormData bloc = new FormData();
bloc.left = new FormAttachment(68);
bloc.right = new FormAttachment(78);
bloc.top = new FormAttachment (5);
Button checkBrailleItem = new Button (dm.documentWindow, SWT.CHECK);
checkBrailleItem.setLayoutData (bloc);
checkBrailleItem.setText (lh.localValue("viewBraille"));
checkBrailleItem.pack();
checkBrailleItem.addSelectionListener (new SelectionAdapter() {
	public void widgetSelected (SelectionEvent e) {
		dm.toggleBrailleFont();
	}
});
}
}