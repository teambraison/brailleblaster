package org.brailleblaster.settings.ui;

import java.util.HashMap;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.settings.SettingsManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class PageNumbersTab {
	
	HashMap<String, String> settingsMap;
	SettingsManager sm;
	TabItem item;
	LocaleHandler lh;
	Group ppnGroup, bpnGroup, cpGroup;
	Composite group;
	Label cpLabel, intpLabel, ppnRngLabel, bpnLocLabel, ppnLocLabel;
	//Label ppnLabel, bpnLabel, ppnlocLabel, bpnlocLabel;
	Combo cpCombo, intpCombo, ppnRngCombo, bpnLocCombo, ppnLocCombo;
	//Combo ppnCombo, bpnCombo, ppnlocCombo, bpnlocCombo;
	
	public PageNumbersTab(TabFolder folder, final SettingsManager sm, HashMap<String, String> settingsMap) {
		super();
		lh = new LocaleHandler();
		this.sm = sm;
		this.settingsMap = settingsMap;
		item = new TabItem(folder, 0);	
		item.setText(lh.localValue("pageNumbers"));
		
//		group = new Composite(folder, 0);
//		group.setLayout(new FormLayout());
//		item.setControl(group);
//		setFormLayout(group, 0, 100, 0, 60);
		 
		group = new Composite(folder, SWT.BORDER);
		group.setLayout(new FillLayout(SWT.VERTICAL));
		item.setControl(group);
		
		bpnGroup = new Group(group, 0);
		bpnGroup.setLayout(new GridLayout(2, true));
		bpnGroup.setText(lh.localValue("braille"));
		
		////////////
		
		intpLabel = new Label(bpnGroup, 0);
		intpLabel.setText("Interpoint");
		setGridData(intpLabel);
		intpCombo = new Combo(bpnGroup, SWT.READ_ONLY);
		intpCombo.add("No");
		intpCombo.add("Yes");
		if( settingsMap.get("interpoint").equals("yes") )
			intpCombo.setText("Yes");
		else
			intpCombo.setText("No");
		setGridData(intpCombo);
		
		///////////
			
		/**
		 * Currently the settings have one drop down menu to select "Yes" or "No" 
		 * and a separate settings box to select the location. 
		 * Rather than the user having to enter two settings, this could be consolidated into 
		 * one drop down menu for Braille Page Numbers: Top, Bottom, None.
		 */
		
		bpnLocLabel = new Label (bpnGroup, 0);
		bpnLocLabel.setText("Braille Page Number Location");
		setGridData(bpnLocLabel);
		bpnLocCombo = new Combo(bpnGroup, SWT.READ_ONLY);
		bpnLocCombo.add("Top Left");
		bpnLocCombo.add("Bottom Left");
		bpnLocCombo.add("Top Right");
		bpnLocCombo.add("Bottom Right");
		bpnLocCombo.add("None");
		
		if (settingsMap.get("braillePageNumberAt").equals("Top Left")) {
			bpnLocCombo.setText("Top Left");
		}
		else if (settingsMap.get("braillePageNumberAt").equals("Bottom Left")) {
			bpnLocCombo.setText("Bottom Left");
		}
		else if (settingsMap.get("braillePageNumberAt").equals("Top Right")) {
			bpnLocCombo.setText("Top Right");
		}
		else {
			bpnLocCombo.setText("Bottom Right");
		}
		
		if (settingsMap.get("numberBraillePages").equals("no")) {
			bpnLocCombo.setText("None");
		}
		
		setGridData(bpnLocCombo);
		
		/////////////////////////////////////////////////////////////
		
		ppnGroup = new Group(group, 0);
		ppnGroup.setLayout(new GridLayout(2, true));
		ppnGroup.setText(lh.localValue("print"));
		
		ppnLocLabel = new Label(ppnGroup, 0);
		ppnLocLabel.setText("Print Page Number Location");
		setGridData(ppnLocLabel);
		ppnLocCombo = new Combo(ppnGroup, SWT.READ_ONLY);
		ppnLocCombo.add("Top Left");
		ppnLocCombo.add("Bottom Left");
		ppnLocCombo.add("Top Right");
		ppnLocCombo.add("Bottom Right");
		ppnLocCombo.add("None");
		
		if (settingsMap.get("printPageNumberAt").equals("Top Left") ) {
			ppnLocCombo.setText("Top Left");
		}
		else if (settingsMap.get("printPageNumberAt").equals("Bottom Left")) {
			ppnLocCombo.setText("Bottom Left");
		}
		else if (settingsMap.get("printPageNumberAt").equals("Top Right")) {
			ppnLocCombo.setText("Top Right");
		}
		else {
			ppnLocCombo.setText("Bottom Right");
		}
		
		if (settingsMap.get("printPages").equals("no")) {
			ppnLocCombo.setText("None");
		}
		
		setGridData(ppnLocCombo);
		
		/////////////
		
		ppnRngLabel = new Label(ppnGroup, 0);
		ppnRngLabel.setText("Continuation Symbols For Print Pages");
		setGridData(ppnRngLabel);
		ppnRngCombo = new Combo(ppnGroup, SWT.READ_ONLY);
		ppnRngCombo.add("No");
		ppnRngCombo.add("Yes");
		if( settingsMap.get("printPageNumberRange").equals("yes") )
			ppnRngCombo.setText("Yes");
		else
			ppnRngCombo.setText("No");
		setGridData(ppnRngCombo);
		
		//////////////////////////////////////////////////////////
		
		cpGroup = new Group(group, 0);
		cpGroup.setLayout(new GridLayout(2, true));
		cpGroup.setText(lh.localValue("continue"));
		cpLabel = new Label(cpGroup, 0);
		cpLabel.setText("Continue Pages");
		setGridData(ppnLocLabel);
		cpCombo = new Combo(cpGroup, SWT.READ_ONLY);
		cpCombo.add("No");
		cpCombo.add("Yes");
		if( settingsMap.get("continuePages").equals("yes") )
			cpCombo.setText("Yes");
		else
			cpCombo.setText("No");
		setGridData(cpCombo);
		
		
		addListeners();
	}
	
	private void addListeners(){
		

		ppnLocCombo.addSelectionListener(new SelectionAdapter(){
		@Override
		public void widgetSelected(SelectionEvent e) {

			if (ppnLocCombo.getText().equals("None")) {
				settingsMap.put("printPages", "no");
			}
			else {
				settingsMap.put("printPageNumberAt", ppnLocCombo.getText());
				settingsMap.put("printPages", "yes");
			}
			}
		});
		
		bpnLocCombo.addSelectionListener(new SelectionAdapter(){
		@Override
		public void widgetSelected(SelectionEvent e) {
			int index = bpnLocCombo.getSelectionIndex();
			if (bpnLocCombo.getText().equals("None")) {
				settingsMap.put("numberBraillePages", "no");
			}
			else {
				settingsMap.put("braillePageNumberAt", bpnLocCombo.getText());
				settingsMap.put("numberBraillePages", "yes");
			}
			}
		});
		
		ppnRngCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingsMap.put("printPageNumberRange", ppnRngCombo.getText().toLowerCase());
			}
		});
	}
	
	private void setGridData(Control c){
		GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        c.setLayoutData(gridData);
	}
}
