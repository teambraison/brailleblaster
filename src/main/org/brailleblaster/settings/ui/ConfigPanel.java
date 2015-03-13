package org.brailleblaster.settings.ui;

import java.util.HashMap;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.settings.SettingsManager;
import org.brailleblaster.utd.PageSettings;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

public class ConfigPanel {
	Shell shell;
	TabFolder folder;
	PagePropertiesTab pageProperties;
	TranslationSettingsTab translationSettings;
	PageNumbersTab pageNumTab;
	StyleDefinitionsTab styleDefsTab;
	Button okButton, cancelButton;
	
	public ConfigPanel(final SettingsManager sm, final Manager m){
		LocaleHandler lh = new LocaleHandler();
		shell = new Shell(Display.getDefault(), SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		shell.setText( lh.localValue("settings") );
		shell.setLayout(new FormLayout());
		
		folder = new TabFolder(shell, SWT.NONE);
		setFormLayout(folder, 0, 100, 0, 94);
		
		final HashMap<String, String> settingsCopy = sm.getMapClone();
		PageSettings currentPageSettings = m.getDocument().getEngine().getPageSettings();
		pageProperties = new PagePropertiesTab(folder, currentPageSettings);
		translationSettings = new TranslationSettingsTab(folder, sm, settingsCopy);
		pageNumTab = new PageNumbersTab(folder, currentPageSettings);
		styleDefsTab = new StyleDefinitionsTab(folder, m);
		
		okButton = new Button(shell, SWT.PUSH);
		okButton.setText(lh.localValue(lh.localValue("buttonOk")));
		setFormLayout(okButton, 50, 75, 94, 100);
		okButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				String errorStr = null;
					if( translationSettings.validate() && (errorStr = pageProperties.validate()).compareTo("SUCCESS") == 0){
						sm.saveConfiguration(settingsCopy);
						sm.close();
						m.refresh();
					}
					else {
						LocaleHandler lh = new LocaleHandler();
						new Notify(lh.localValue(errorStr));
					}
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
		
		shell.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.keyCode == SWT.ESC)
					shell.close();
			}
			
		});
		
		shell.addListener(SWT.Close, new Listener(){
			@Override
			public void handleEvent(Event e) {
				sm.close();
			}		
		});
		
		//Autosize shell based on what the internal elements require
		Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		shell.setSize(size);
		
		//Show the window
		shell.open();
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
