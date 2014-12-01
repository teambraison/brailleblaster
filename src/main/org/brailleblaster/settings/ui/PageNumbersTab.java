package org.brailleblaster.settings.ui;

import java.util.HashMap;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.settings.SettingsManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
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
	Label ppnLabel, bpnLabel, cpLabel, intpLabel, ppnlocLabel, bpnlocLabel, ppnRngLabel;
	Combo ppnCombo, bpnCombo, cpCombo, intpCombo, ppnlocCombo, bpnlocCombo, ppnRngCombo;
	
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
		
		bpnLabel = new Label(bpnGroup, 0);
		bpnLabel.setText("Braille Page Numbers");
		setGridData(bpnLabel);
		bpnCombo = new Combo(bpnGroup, SWT.READ_ONLY);
		bpnCombo.add("No");
		bpnCombo.add("Yes");
		if( settingsMap.get("numberBraillePages").equals("yes") )
			bpnCombo.setText("Yes");
		else
			bpnCombo.setText("No");
		setGridData(bpnCombo);
		
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
		
		bpnlocLabel = new Label(bpnGroup, 0);
		bpnlocLabel.setText("BPN Location");
		setGridData(bpnlocLabel);
		bpnlocCombo = new Combo(bpnGroup, SWT.READ_ONLY);
		bpnlocCombo.add("Bottom");
		bpnlocCombo.add("Top");
		if( settingsMap.get("braillePageNumberAt").equals("top") )
			bpnlocCombo.setText("Top");
		else
			bpnlocCombo.setText("Bottom");
		setGridData(bpnlocCombo);
		
		/////////////////////////////////////////////////////////////
		
		ppnGroup = new Group(group, 0);
		ppnGroup.setLayout(new GridLayout(2, true));
		ppnGroup.setText(lh.localValue("print"));
		
		
		ppnLabel = new Label(ppnGroup, 0);
		ppnLabel.setText("Print Page Numbers");
		setGridData(ppnLabel);
		ppnCombo = new Combo(ppnGroup, SWT.READ_ONLY);
		ppnCombo.add("No");
		ppnCombo.add("Yes");
		if( settingsMap.get("printPages").equals("yes") )
			ppnCombo.setText("Yes");
		else
			ppnCombo.setText("No");
		setGridData(ppnCombo);
		
		/////////////
		
		ppnlocLabel = new Label(ppnGroup, 0);
		ppnlocLabel.setText("PPN Location");
		setGridData(ppnlocLabel);
		ppnlocCombo = new Combo(ppnGroup, SWT.READ_ONLY);
		ppnlocCombo.add("Bottom");
		ppnlocCombo.add("Top");
		if( settingsMap.get("printPageNumberAt").equals("top") )
			ppnlocCombo.setText("Top");
		else
			ppnlocCombo.setText("Bottom");
		setGridData(ppnlocCombo);
		
		/////////////
		
		ppnRngLabel = new Label(ppnGroup, 0);
		ppnRngLabel.setText("Page Number Range");
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
		setGridData(ppnLabel);
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
		
		ppnCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = ppnCombo.getSelectionIndex();
				settingsMap.put("printPages", ppnCombo.getText().toLowerCase());
			}
		});
		
		bpnCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = bpnCombo.getSelectionIndex();
				settingsMap.put("numberBraillePages", bpnCombo.getText().toLowerCase());
			}
		});
		
		cpCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = cpCombo.getSelectionIndex();
				settingsMap.put("continuePages", cpCombo.getText().toLowerCase());
			}
		});
		
		intpCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = intpCombo.getSelectionIndex();
				settingsMap.put("interpoint", intpCombo.getText().toLowerCase());
			}
		});
		
		ppnlocCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = ppnlocCombo.getSelectionIndex();
				settingsMap.put("printPageNumberAt", ppnlocCombo.getText().toLowerCase());
			}
		});
		
		bpnlocCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = bpnlocCombo.getSelectionIndex();
				settingsMap.put("braillePageNumberAt", bpnlocCombo.getText().toLowerCase());
			}
		});
		
		ppnRngCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = ppnRngCombo.getSelectionIndex();
				settingsMap.put("printPageNumberRange", ppnRngCombo.getText().toLowerCase());
			}
		});
	}
	
	private void setFormLayout(Control c, int left, int right, int top, int bottom){
		FormData location = new FormData();
		
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		
		c.setLayoutData(location);
	}
	
	private void setGridData(Control c){
		GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        c.setLayoutData(gridData);
	}
}
