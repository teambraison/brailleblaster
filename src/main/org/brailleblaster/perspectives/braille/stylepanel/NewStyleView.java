package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.util.Notify;
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
				sm.closeEditStyle();
			}		
		});
		
		saveButton = new Button(group, SWT.NONE);
		saveButton.setText(lh.localValue("apply"));
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
					System.out.println("Save style here");
					sm.saveNewItem(style);
				}
			}
		});
		
		resetLayout();
		styleName.setFocus();
	}
	
	private Styles getNewStyle(){
		Styles newStyle;
		if(styleName.getText().length() == 0){
			new Notify("Style name cannot be blank");
			return null;
		}
		else
			newStyle = sm.getSemanticsTable().getNewStyle(styleName.getText());
		
		
		int selectionIndex = emphasisCombo.getSelectionIndex(); 
		if(selectionIndex != -1){
			if(selectionIndex == BOLD)
				newStyle.put(StylesType.emphasis, "boldx");
			else if(selectionIndex == ITALIC)
				newStyle.put(StylesType.emphasis, "italicx");
			else if(selectionIndex == UNDERLINE)
				newStyle.put(StylesType.emphasis, "underlinex");		
		}
		
		
		selectionIndex = alignmentCombo.getSelectionIndex(); 
		if(selectionIndex != -1){
			if(selectionIndex == LEFT)
				newStyle.put(StylesType.format, "leftjustified");
			else if(selectionIndex == CENTER)
				newStyle.put(StylesType.format, "center");
			else if(selectionIndex == RIGHT)
				newStyle.put(StylesType.format, "rightJustified");
						
		}
		
		int value = linesBeforeSpinner.getSelection();
		if(value > 0)
				newStyle.put(StylesType.linesBefore, String.valueOf(value));
		
		
		value = linesAfterSpinner.getSelection();
		if(value > 0)
			newStyle.put(StylesType.linesAfter, String.valueOf(value));
		
		value = marginSpinner.getSelection();
		if(value > 0)
			newStyle.put(StylesType.leftMargin, String.valueOf(value));
		
		value = indentSpinner.getSelection();
		if(value > 0 || value < 0)
			newStyle.put(StylesType.firstLineIndent, String.valueOf(value));
		
		if(newStyle.getKeySet().size() > 0)
			return newStyle;
		else{
			new Notify("No values entered to create a new style.  Style cannot be blank");
			return null;
		}
	}
	
}
