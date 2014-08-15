package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;

public class NewStyleView extends EditPanel{
	
	private Button saveButton, cancelButton;
	
	public NewStyleView(final StyleManager sm, Group documentWindow) {
	    super(sm, documentWindow, null);
		cancelButton = new Button(group, SWT.NONE);
		cancelButton.setText(lh.localValue("styleCancel"));
		setLayoutData(cancelButton, 0, 50, 90, 100);
		cancelButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				sm.closeEditStyle(null);
			}		
		});
		
		saveButton = new Button(group, SWT.NONE);
		saveButton.setText(lh.localValue("save"));
		setLayoutData(saveButton, 50, 100, 90, 100);
		
		saveButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveNewStyle();
			}
		});
		
		
		group.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(hasFocus()){
					if(e.stateMask == SWT.MOD3 && e.character == 's')
						saveNewStyle();
					else if(e.stateMask == SWT.MOD3 && e.character == 'c')
						sm.closeEditStyle(null);
				}
			}	
		});
		
		resetLayout();
		styleName.setFocus();
	}
	
	private void saveNewStyle(){
		Styles style = getNewStyle();
		if(style != null){
			sm.saveNewItem(style);
		}
	}
	
	private boolean hasFocus(){
		if(styleName.isFocusControl() || alignmentCombo.isFocusControl() || emphasisCombo.isFocusControl() || linesBeforeSpinner.isFocusControl() || 
				linesAfterSpinner.isFocusControl() || marginSpinner.isFocusControl() || indentSpinner.isFocusControl() || cancelButton.isFocusControl() || saveButton.isFocusControl())
			return true;
		else
			return false;
	}
}
