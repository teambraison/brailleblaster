/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org www.aph.org
  *
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.userHelp.CallOutsideWP;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.brailleblaster.wordprocessor.WPManager;

/**
 * This class displays the welcome screen The first time BrailleBlaster 
 * is started and on subsequent startups unless the user has chosen not 
 * to see it. The user can also chose to read a tutorial while on the 
 * weocome screen.
  */

public class Welcome {
	private Properties prop;
	private String userSettings;
	LocaleHandler lh = new LocaleHandler();
public Welcome() {
loadProperties();
if (!showWelcome()) {
return;
}

Display display = WPManager.getDisplay();
final Shell shell = new Shell(display, SWT.CLOSE|SWT.BORDER);

FormLayout formLayout = new FormLayout();
formLayout.marginWidth = 10;
formLayout.marginHeight = 10;
formLayout.spacing = 10;
shell.setLayout(formLayout);
shell.setText ("WELCOME");
Label label = new Label(shell, SWT.WRAP);
FormData data = new FormData();
data.left = new FormAttachment(0, 0);
data.top = new FormAttachment(0, 0);
label.setText(new LocaleHandler ().localValue ("welcomeMessage"));
label.setLayoutData(data);

//Check button: Welcome setting: whether to show it when BB opens
final Button checkbox= new Button(shell, SWT.CHECK);
checkbox.setText(lh.localValue("settingsWelcome"));
FormData data1 = new FormData();
data1.left = new FormAttachment(0, 0);
data1.top = new FormAttachment(label, 5);
checkbox.setLayoutData(data1);

//Button: click to open tutorial in help doc
final Button tutBtn = new Button(shell, SWT.PUSH);
tutBtn.setText("Open tutorial");
FormData data2 = new FormData();
data2.left = new FormAttachment(0, 0);
data2.top = new FormAttachment(checkbox, 5);
tutBtn.setLayoutData(data2);

tutBtn.addSelectionListener(new SelectionListener() {
  @Override
public void widgetSelected(SelectionEvent e) {
    new CallOutsideWP().showTutorial();
  }
  @Override
public void widgetDefaultSelected(SelectionEvent e) {
  }
});

//Button: click to close the dialog
final Button okBtn = new Button(shell, SWT.PUSH);
okBtn.setText("OK");
FormData data3 = new FormData();
data3.width = 100;
data3.right = new FormAttachment(100, 0);
data3.top = new FormAttachment(checkbox, 5);
okBtn.setLayoutData(data3);
okBtn.addSelectionListener(new SelectionListener() {
  @Override
public void widgetSelected(SelectionEvent e) {
    String showWelcome; 
    if(checkbox.getSelection()) {
      showWelcome = "yes";
    }
    else {
      showWelcome = "no";
    }
    setProperty("showWelcome", showWelcome);
    storeProperty();
    shell.dispose();
  }
  @Override
public void widgetDefaultSelected(SelectionEvent e) {
  }
});



shell.pack();
shell.open();
shell.setFocus();
		
while (!shell.isDisposed()) {
	if (!display.readAndDispatch()) display.sleep();
}
shell.dispose();

//sd.open();


}
private void loadProperties() {
	userSettings= BBIni.getUserSettings();
	prop = new Properties();
	try {
		prop.load(new FileInputStream(userSettings));
	} catch (FileNotFoundException e) {
		new Notify(e.getMessage());
	} catch (IOException e) {
		new Notify(e.getMessage());
	}
}

public void setProperty(String key, String value) {
	prop.setProperty(key, value);
}

public void storeProperty() {
	//no change will be made to the properties file until this method is called
	try {
		prop.store(new FileOutputStream(userSettings), null);
	} catch (FileNotFoundException e) {
		new Notify(e.getMessage());
	} catch (IOException e) {
		new Notify(e.getMessage());
	}
}

public boolean showWelcome() {
	return prop.getProperty("showWelcome").equals("yes"); 
}
}
