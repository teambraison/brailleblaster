package org.brailleblaster.settings.ui;

import java.util.HashMap;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.settings.SettingsManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

public class ConfigPanel {
	Shell shell;
	TabFolder folder;
	PagePropertiesTab pageProperties;
	TranslationSettingsTab translationSettings;
	Button okButton, cancelButton;
	
	public ConfigPanel(final SettingsManager sm, final Manager m){
		shell = new Shell(Display.getDefault(), SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		shell.setText("Settings");
		shell.setLayout(new FormLayout());
		setPanelSize();
		
		folder = new TabFolder(shell, SWT.NONE);
		setFormLayout(folder, 0, 100, 0, 94);
		
		final HashMap<String, String> settingsCopy = sm.getMapClone();
		pageProperties = new PagePropertiesTab(folder, sm, settingsCopy);
		//translationSettings = new TranslationSettingsTab(folder, ppm, settingsCopy);
		
		okButton = new Button(shell, SWT.PUSH);
		okButton.setText("OK");
		setFormLayout(okButton, 50, 75, 94, 100);
		okButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				sm.saveConfiguration(settingsCopy);
				sm.close();
				m.refresh();
			}	
		});
		
		cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setText("Cancel");
		setFormLayout(cancelButton, 75, 100, 94, 100);
		cancelButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				sm.close();
			}	
		});
		
		shell.addListener(SWT.Close, new Listener(){
			@Override
			public void handleEvent(Event e) {
				sm.close();
			}		
		});
		
		shell.open();
	}
	
	private void setPanelSize(){
		Monitor primary = shell.getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		int x = (bounds.width / 2) - ((bounds.width / 6) / 2);
		int y = (bounds.height / 2) - ((bounds.height / 3) / 2);
		shell.setSize(bounds.width / 6, bounds.height / 3);
		shell.setLocation(x, y);
	}
	
	private void setFormLayout(Control c, int left, int right, int top, int bottom){
		FormData location = new FormData();
		
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		
		c.setLayoutData(location);
	}
	
	public Shell getShell(){
		return shell;
	}
	
	public void close(){
		shell.dispose();
	}
}
