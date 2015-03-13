/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brailleblaster.settings.ui;

import java.util.function.Consumer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Useful utilities for creating the SWT UI
 * @author lblakey
 */
public final class SettingsUIUtils {
	private SettingsUIUtils() {
	}
	
	public static void setGridData(Control c){
		GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        c.setLayoutData(gridData);
	}
	
	public static void setGridDataGroup(Group group) {
		setGridData(group);
		((GridData)group.getLayoutData()).grabExcessVerticalSpace = true;
	}
	
	/**
	 * Generate a standard label
	 *
	 * @param parent
	 * @param text
	 * @return
	 */
	public static Label addLabel(Composite parent, String text) {
		Label label = new Label(parent, 0);
		label.setText(text);
		setGridData(label);
		return label;
	}
	
	
	public static SelectionListener makeSelectedListener(Consumer<SelectionEvent> function) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				function.accept(se);
			}
		};
	}
}
