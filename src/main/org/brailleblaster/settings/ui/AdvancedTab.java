package org.brailleblaster.settings.ui;

import java.util.HashMap;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.settings.SettingsManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class AdvancedTab {
	HashMap<String, String> m_settingsMap;
	SettingsManager sm;
	TabItem item;
	Composite group, subgroup;
	
	Label minLinesLabel, minCellsLabel;
	Text minLinesBox, minCellsBox;
	
	LocaleHandler lh;
	
	public AdvancedTab(TabFolder folder, final SettingsManager sm, HashMap<String, String> settingsMap) {
		lh = new LocaleHandler();
		this.sm = sm;
		m_settingsMap = settingsMap;
		item = new TabItem(folder, 0);	
		item.setText( lh.localValue("advancedProperties") );
		
		group = new Composite(folder, 0);
		group.setLayout(new FormLayout());
		item.setControl(group);
		setFormLayout(group, 0, 100, 0, 100);
		
		subgroup = new Composite(folder, 0);
		subgroup.setLayout(new GridLayout(2, true));
		item.setControl(subgroup);
		setFormLayout(subgroup, 0, 100, 0, 100);
		
		//////////////
		// Min Values
		
			minLinesLabel = new Label(subgroup, 0);
			minLinesLabel.setText(lh.localValue("minLinesPerPage"));
			minLinesBox = new Text(subgroup, SWT.BORDER);
			setGridData(minLinesBox);
			
			minCellsLabel = new Label(subgroup, 0);
			minCellsLabel.setText(lh.localValue("minCellsPerLine"));
			minCellsBox = new Text(subgroup, SWT.BORDER);
			setGridData(minCellsBox);
			
			// If the values don't exist, these lines will add them.
			// They check for the key existence for us.
			initCustomSettings();
			minLinesBox.setText( m_settingsMap.get("minLinesPerPage") );
			minCellsBox.setText( m_settingsMap.get("minCellsPerLine") );

		// Min Values
		//////////////
			
		minLinesBox.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				m_settingsMap.put("minLinesPerPage", getStringValue(minLinesBox));
			}	
		});
		
		minCellsBox.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				m_settingsMap.put("minCellsPerLine", getStringValue(minCellsBox));
			}	
		});
	}
	
	public void initCustomSettings() {
			if( m_settingsMap.containsKey("minCellsPerLine") == false )
				m_settingsMap.put( "minCellsPerLine", "12" );
			if( m_settingsMap.containsKey("minLinesPerPage") == false )
				m_settingsMap.put( "minLinesPerPage", "7" );
	}
	
	private String getStringValue(Text t){
		if(t.getText().length() == 0)
			return "0";
		else
			return t.getText();
	}
	
	public boolean validate() {
		return true;
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
