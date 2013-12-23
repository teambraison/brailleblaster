package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;


public class EditStyleView extends EditPanel {

	private Button applyButton, cancelButton;
	private SelectionListener applyListener, saveAsListener;
	
	private boolean apply;
	
	public EditStyleView(final StyleManager sm, Group documentWindow, Styles style){
		super(sm, documentWindow, style);
		apply = true;
		
		cancelButton = new Button(group, SWT.NONE);
		cancelButton.setText(lh.localValue("styleCancel"));
		setLayoutData(cancelButton, 0, 50, 90, 100);
		
		applyButton = new Button(group, SWT.NONE);
		applyButton.setText(lh.localValue("apply"));
		setLayoutData(applyButton, 50, 100, 90, 100);
		
		applyListener = new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				LocaleHandler lh = new LocaleHandler();
				Styles newStyle = saveEditedStyle();
				if(newStyle != null){
					sm.saveEditedStyle(originalStyle, newStyle);
				}
				else
					new Notify(lh.localValue("noChange"));
			}
		};
		
		saveAsListener = new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub			
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				new Notify("NOT YET IMPLEMENTED");
			}
		};
		
		Control [] tablist = {styleName, linesBeforeSpinner, linesAfterSpinner, marginSpinner, indentSpinner, alignmentCombo, emphasisCombo, cancelButton, applyButton};
		group.setTabList(tablist);
		resetLayout();
		
		initializeListeners();
		initializeFormData(style);
		styleName.setFocus();
	}
	
	private void initializeListeners(){
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
		
		applyButton.addSelectionListener(applyListener);
		
		
		styleName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				if(!styleName.getText().equals(originalStyle.getName()) && apply)
					toggleApplyButton();
				else if(styleName.getText().equals(originalStyle.getName()) && !apply)
					toggleApplyButton();
			}		
		});
	}
	
	private void initializeFormData(Styles style){
		styleName.setText(style.getName());
		setSpinnerData(linesBeforeSpinner, style, StylesType.linesBefore);
		setSpinnerData(linesAfterSpinner, style, StylesType.linesAfter);
		setSpinnerData(marginSpinner, style, StylesType.leftMargin);
		setSpinnerData(indentSpinner, style, StylesType.firstLineIndent);
		
		if(originalStyle.contains(StylesType.emphasis)){	
			int emphasisValue = Integer.valueOf((String)originalStyle.get(StylesType.emphasis));
			
			if(emphasisValue == SWT.BOLD)
				emphasisCombo.select(BOLD);
			else if(emphasisValue == SWT.ITALIC)
				emphasisCombo.select(ITALIC);
			else
				emphasisCombo.select(UNDERLINE);
		}
		
		if(originalStyle.contains(StylesType.format)){			
			int value;
			try {
				value = Integer.valueOf((String)originalStyle.get(StylesType.format));
			}
			catch(NumberFormatException e){
				value = -1;
			}
			
			if(value == SWT.LEFT)
				alignmentCombo.select(LEFT);
			else if(value == SWT.CENTER)
				alignmentCombo.select(CENTER);
			else if(value == SWT.RIGHT)
				alignmentCombo.select(RIGHT);
		}
	}
	
	protected void toggleApplyButton(){
		LocaleHandler lh = new LocaleHandler();
		if(apply){
			applyButton.setText(lh.localValue("saveAs"));
			apply = false;
			applyButton.removeSelectionListener(applyListener);
			applyButton.addSelectionListener(saveAsListener);
		}
		else{
			applyButton.setText(lh.localValue("apply"));
			apply = true;
			applyButton.removeSelectionListener(saveAsListener);
			applyButton.addSelectionListener(applyListener);
		}
	}
	
	protected Styles saveEditedStyle(){
		Styles newStyle = sm.getSemanticsTable().getNewStyle(originalStyle.getName());
		boolean changed = false;
		
		if(originalStyle.contains(StylesType.emphasis)){
			if(emphasisCombo.getSelectionIndex() != -1){
				String value = emphasisCombo.getItem(emphasisCombo.getSelectionIndex());
				int emphasis;
				
				if(value.equals(lh.localValue("bold"))){
					value = "boldx";
					emphasis = SWT.BOLD;
				}
				else if(value.equals(lh.localValue("italic"))){
					value = "italicx";
					emphasis = SWT.ITALIC;  
				}
				else {
					value = "underlinex";
					emphasis = SWT.UNDERLINE_SINGLE;
				}
				
				if(emphasis != Integer.valueOf((String)originalStyle.get(StylesType.emphasis))){
					newStyle.put(StylesType.emphasis, value);
					changed = true;
				}
				else{
					newStyle.put(StylesType.emphasis, value);
				}
			}
			else
				changed = true;
		}
		else if(emphasisCombo.getSelectionIndex() != -1){
			String value = emphasisCombo.getItem(emphasisCombo.getSelectionIndex()) + 'x';
			newStyle.put(StylesType.emphasis, value);
			changed = true;
		}
		
		if(originalStyle.contains(StylesType.format)){
			if(alignmentCombo.getSelectionIndex() != -1){
				String value = alignmentCombo.getItem(alignmentCombo.getSelectionIndex());
				int alignment = 0;
				if(value.equals(lh.localValue("left"))){
					value = "leftJustified";
					alignment = SWT.LEFT;
				}
				else if(value.equals(lh.localValue("center"))){
					value = "centered";
					alignment = SWT.CENTER;
				}
				else {
					value = "rightJustified";
					alignment = SWT.RIGHT;
				}
				
				if(alignment != Integer.valueOf((String)originalStyle.get(StylesType.format))){
					newStyle.put(StylesType.format, value);
					changed = true;
				}
				else
					newStyle.put(StylesType.format, value);
					
			}
			else
				changed = true;
		}
		else if(alignmentCombo.getSelectionIndex() != -1){
			String value = alignmentCombo.getItem(alignmentCombo.getSelectionIndex());
			
			if(value.equals(lh.localValue("left")))
				value = "leftJustified";
			else if(value.equals(lh.localValue("center")))
				value = "centered";
			else
				value = "rightJustified";
			
			newStyle.put(StylesType.format, value);
			changed = true;
		}
		
		if(originalStyle.contains(StylesType.linesBefore)){
			if(linesBeforeSpinner.getSelection() != Integer.valueOf((String)originalStyle.get(StylesType.linesBefore))){
				if(linesBeforeSpinner.getSelection() > 0)
					newStyle.put(StylesType.linesBefore, String.valueOf(linesBeforeSpinner.getSelection()));
				
				changed = true;
			}
			else
				newStyle.put(StylesType.linesBefore, (String)originalStyle.get(StylesType.linesBefore));
		}
		else if(linesBeforeSpinner.getSelection() != 0){
			newStyle.put(StylesType.linesBefore, String.valueOf(linesBeforeSpinner.getSelection()));
			changed = true;
		}
		
		if(originalStyle.contains(StylesType.linesAfter)){
			if(linesAfterSpinner.getSelection() != Integer.valueOf((String)originalStyle.get(StylesType.linesAfter))){
				if(linesAfterSpinner.getSelection() > 0)
					newStyle.put(StylesType.linesAfter, String.valueOf(linesAfterSpinner.getSelection()));
				
				changed = true;
			}
			else
				newStyle.put(StylesType.linesAfter, (String)originalStyle.get(StylesType.linesAfter));
		}
		else if(linesAfterSpinner.getSelection() != 0){
			newStyle.put(StylesType.linesAfter, String.valueOf(linesAfterSpinner.getSelection()));
			changed = true;
		}
		
		if(originalStyle.contains(StylesType.leftMargin)){
			if(marginSpinner.getSelection() != Integer.valueOf((String)originalStyle.get(StylesType.leftMargin))){
				if(marginSpinner.getSelection() > 0)
					newStyle.put(StylesType.leftMargin, String.valueOf(marginSpinner.getSelection()));
				
				changed = true;
			}
			else
				newStyle.put(StylesType.leftMargin, (String)originalStyle.get(StylesType.leftMargin));
		}
		else if(marginSpinner.getSelection() != 0){
			newStyle.put(StylesType.leftMargin, String.valueOf(marginSpinner.getSelection()));
			changed = true;
		}
		
		if(originalStyle.contains(StylesType.firstLineIndent)){
			if(indentSpinner.getSelection() != Integer.valueOf((String)originalStyle.get(StylesType.firstLineIndent))){
				if(indentSpinner.getSelection() > 0)
					newStyle.put(StylesType.firstLineIndent, String.valueOf(indentSpinner.getSelection()));
				
				changed = true;
			}
			else
				newStyle.put(StylesType.firstLineIndent, (String)originalStyle.get(StylesType.firstLineIndent));
		}
		else if(indentSpinner.getSelection() != 0){
			newStyle.put(StylesType.firstLineIndent, String.valueOf(indentSpinner.getSelection()));
			changed = true;
		}
		
		
		if(changed)
			return newStyle;
		else
			return null;
	}
}
