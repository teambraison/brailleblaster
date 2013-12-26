package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;

public class NewStyleView extends EditPanel{
	
	private Button saveButton, cancelButton;
	
	public NewStyleView(final StyleManager sm, Group documentWindow) {
	    super(sm, documentWindow, null);
		cancelButton = new Button(group, SWT.NONE);
		cancelButton.setText(lh.localValue("styleCancel"));
		setLayoutData(cancelButton, 0, 50, 90, 100);
		cancelButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				sm.closeEditStyle(null);
			}		
		});
		
		saveButton = new Button(group, SWT.NONE);
		saveButton.setText(lh.localValue("save"));
		setLayoutData(saveButton, 50, 100, 90, 100);
		
		saveButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Styles style = getNewStyle();
				if(style != null){
					sm.saveNewItem(style);
				}
			}
		});
		
		resetLayout();
		styleName.setFocus();
	}
}
