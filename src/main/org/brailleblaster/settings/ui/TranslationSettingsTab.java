package org.brailleblaster.settings.ui;

import java.util.HashMap;

import org.brailleblaster.settings.SettingsManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class TranslationSettingsTab {
	TabItem item;
	Group group; 
	
	TranslationSettingsTab(TabFolder folder, SettingsManager ppm, HashMap<String, String>tempSettingsMap){
		item = new TabItem(folder, 0);	
		item.setText("Translation Settings");
		
		group = new Group(folder, SWT.BORDER);
		item.setControl(group);
	}
}
