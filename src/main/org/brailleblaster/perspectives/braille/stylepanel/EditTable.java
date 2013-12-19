package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class EditTable {
	static LocaleHandler lh = new LocaleHandler();
	
	//For use in making localized UI
	final private String [] emphasisList = {lh.localValue("bold"), lh.localValue("italic"), lh.localValue("underline")};
	//For determining combo box position in relation to liblouis value
	private static final int BOLD = 0;
	private static final int ITALIC = 1;
	private static final int UNDERLINE = 2;
	
	final private String [] alignmentList = {lh.localValue("left"), lh.localValue("center"), lh.localValue("right")};
	private static final int LEFT = 0;
	private static final int CENTER = 1;
	private static final int RIGHT = 2;
	
	private Table t;
	private Styles originalStyle;
	private Text nameItem;
	private StyleManager sm;
	
	public EditTable(Table t, Styles style, StyleManager sm){
		this.t = t;
		this.originalStyle = style;
		this.sm = sm;
		initializeTable();
	}
	
	private void initializeTable(){
		makeTextTableEntry(lh.localValue("element"), originalStyle.getName());
		
		String [] attributeList = {lh.localValue("linesBefore"), lh.localValue("linesAfter"), lh.localValue("indent"), lh.localValue("margin")};
		StylesType [] liblouisAttribute = {StylesType.linesBefore, StylesType.linesAfter, StylesType.firstLineIndent, StylesType.leftMargin};
		for(int i = 0; i < attributeList.length; i++){
			if(originalStyle.contains(liblouisAttribute[i]))
				makeSpinnerTableEntry(attributeList[i], Integer.valueOf((String)originalStyle.get(liblouisAttribute[i])));
			else
				makeSpinnerTableEntry(attributeList[i], 0);
		}
		
		makeEmphasisEntry();
		makeAlignmentEntry();
	}
	
	private void makeEmphasisEntry(){
		int defaultValue = -1;
		if(originalStyle.contains(StylesType.emphasis)){
			
			int emphasisValue = Integer.valueOf((String)originalStyle.get(StylesType.emphasis));
			
			if(emphasisValue == SWT.BOLD)
				defaultValue = BOLD;
			else if(emphasisValue == SWT.ITALIC)
				defaultValue = ITALIC;
			else
				defaultValue = UNDERLINE;
			
		}
		makeComboTableEntry(lh.localValue("emphasis"),  emphasisList, defaultValue);
	}
	
	private void makeAlignmentEntry(){
		int defaultValue = -1;
		if(originalStyle.contains(StylesType.format)){			
			int value;
			try {
				value = Integer.valueOf((String)originalStyle.get(StylesType.format));
			}
			catch(NumberFormatException e){
				value = -1;
			}
			
			if(value == SWT.LEFT)
				defaultValue = LEFT;
			else if(value == SWT.CENTER)
				defaultValue = CENTER;
			else if(value == SWT.RIGHT)
				defaultValue = RIGHT;
		}
		makeComboTableEntry(lh.localValue("alignment"), alignmentList, defaultValue);
	}
	
	private void makeTextTableEntry(String attribute, String value){
		TableItem item = new TableItem(t, SWT.CENTER);
		Text text = makeTableItemTextBox(item, attribute);	
		setTableEditor(item, text, 0);

		final Text valueItem = new Text(t, SWT.NONE);
	
		if(value != null)
			valueItem.setText(value);
		
		if(attribute.equals(lh.localValue("element"))){
			nameItem = valueItem;
		}
		
		setTableEditor(item, valueItem, 1);
	}
	
	private void makeSpinnerTableEntry(String entry, int value){
		TableItem item = new TableItem(t, SWT.CENTER);
		Text text = makeTableItemTextBox(item, entry);
		setTableEditor(item, text, 0);
		
		Spinner sp = new Spinner(t, SWT.NONE);
		if(entry.equals(lh.localValue("indent")))
			sp.setMinimum(-100);
		
		sp.setSelection(value);
		sp.pack();
		
		setTableEditor(item, sp, 1);
	}
	
	private void makeComboTableEntry(String attribute, String [] comboEntries, int defaultValue){	
		TableItem comboItem = new TableItem(t, SWT.CENTER);
		Text text = makeTableItemTextBox(comboItem, attribute);
		setTableEditor(comboItem, text, 0);
		
		Combo cb = new Combo(t, SWT.DROP_DOWN);
		for(int i = 0; i < comboEntries.length; i++){
			cb.add(comboEntries[i]);
		}
		if(defaultValue >= 0)
			cb.select(defaultValue);
		
		setTableEditor(comboItem, cb, 1);
	}
	
	private Text makeTableItemTextBox(TableItem item, String text){
		Text textBox = new Text (t, SWT.NONE);
		textBox.setText(text);
		textBox.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = false;
			}			
		});
		
		return textBox;
	}
	
	private void setTableEditor(TableItem item, Control c, int row){
		TableEditor editor = new TableEditor(t);
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.CENTER;
		editor.setEditor(c, item, row);
		String className = c.getClass().getSimpleName();
		item.setData(className, c);
	}
	
	protected Styles saveEditedStyle(){
		Styles newStyle = sm.getSemanticsTable().getNewStyle(originalStyle.getName());
		boolean changed = false;
		int count = t.getItemCount();
		
		for(int i = 1; i < count; i++){
			TableItem item = t.getItem(i);
			Text text = (Text)item.getData("Text");
		
			if(text.getText().equals(lh.localValue("emphasis"))){
				if(originalStyle.contains(StylesType.emphasis)){
					if(((Combo)item.getData("Combo")).getSelectionIndex() != -1){
						String value = ((Combo)item.getData("Combo")).getItem(((Combo)item.getData("Combo")).getSelectionIndex());
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
				else if(((Combo)item.getData("Combo")).getSelectionIndex() != -1){
					String value =((Combo)item.getData("Combo")).getItem(((Combo)item.getData("Combo")).getSelectionIndex()) + 'x';
					newStyle.put(StylesType.emphasis, value);
					changed = true;
				}
			}
			else if(text.getText().equals(lh.localValue("alignment"))){
				if(originalStyle.contains(StylesType.format)){
					if(((Combo)item.getData("Combo")).getSelectionIndex() != -1){
						String value =((Combo)item.getData("Combo")).getItem(((Combo)item.getData("Combo")).getSelectionIndex());
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
				else if(((Combo)item.getData("Combo")).getSelectionIndex() != -1){
					String value =((Combo)item.getData("Combo")).getItem(((Combo)item.getData("Combo")).getSelectionIndex());
					
					if(value.equals(lh.localValue("left")))
						value = "leftJustified";
					else if(value.equals(lh.localValue("center")))
						value = "centered";
					else
						value = "rightJustified";
					
					newStyle.put(StylesType.format, value);
					changed = true;
				}
			}
			else if(text.getText().equals(lh.localValue("linesBefore"))){
				if(originalStyle.contains(StylesType.linesBefore)){
					if(((Spinner)item.getData("Spinner")).getSelection() != Integer.valueOf((String)originalStyle.get(StylesType.linesBefore))){
						if(((Spinner)item.getData("Spinner")).getSelection() > 0)
							newStyle.put(StylesType.linesBefore, String.valueOf(((Spinner)item.getData("Spinner")).getSelection()));
						
						changed = true;
					}
					else
						newStyle.put(StylesType.linesBefore, (String)originalStyle.get(StylesType.linesBefore));
				}
				else if(((Spinner)item.getData("Spinner")).getSelection() != 0){
					newStyle.put(StylesType.linesBefore, String.valueOf(((Spinner)item.getData("Spinner")).getSelection()));
					changed = true;
				}
			}
			else if(text.getText().equals(lh.localValue("linesAfter"))){
				if(originalStyle.contains(StylesType.linesAfter)){
					if(((Spinner)item.getData("Spinner")).getSelection() != Integer.valueOf((String)originalStyle.get(StylesType.linesAfter))){
						if(((Spinner)item.getData("Spinner")).getSelection() > 0)
							newStyle.put(StylesType.linesAfter, String.valueOf(((Spinner)item.getData("Spinner")).getSelection()));
						changed = true;
					}
					else
						newStyle.put(StylesType.linesAfter, (String)originalStyle.get(StylesType.linesAfter));
				}
				else if(((Spinner)item.getData("Spinner")).getSelection() != 0){
					newStyle.put(StylesType.linesAfter, String.valueOf(((Spinner)item.getData("Spinner")).getSelection()));
					changed = true;
				}
			}
			else if(text.getText().equals(lh.localValue("margin"))){
				if(originalStyle.contains(StylesType.leftMargin)){
					if(((Spinner)item.getData("Spinner")).getSelection() != Integer.valueOf((String)originalStyle.get(StylesType.leftMargin))){
						if(((Spinner)item.getData("Spinner")).getSelection() > 0)
							newStyle.put(StylesType.leftMargin, String.valueOf(((Spinner)item.getData("Spinner")).getSelection()));
						changed = true;
					}
					else
						newStyle.put(StylesType.leftMargin, (String)originalStyle.get(StylesType.leftMargin));
				}
				else if(((Spinner)item.getData("Spinner")).getSelection() != 0){
					newStyle.put(StylesType.leftMargin, String.valueOf(((Spinner)item.getData("Spinner")).getSelection()));
					changed = true;
				}
			}
			else if(text.getText().equals(lh.localValue("indent"))){
				if(originalStyle.contains(StylesType.firstLineIndent)){
					if(((Spinner)item.getData("Spinner")).getSelection() != Integer.valueOf((String)originalStyle.get(StylesType.firstLineIndent))){
						if(((Spinner)item.getData("Spinner")).getSelection() > 0 || ((Spinner)item.getData("Spinner")).getSelection() < 0)
							newStyle.put(StylesType.firstLineIndent, String.valueOf(((Spinner)item.getData("Spinner")).getSelection()));
						changed = true;
					}
					else
						newStyle.put(StylesType.firstLineIndent, (String)originalStyle.get(StylesType.firstLineIndent));
				}
				else if(((Spinner)item.getData("Spinner")).getSelection() != 0){
					newStyle.put(StylesType.firstLineIndent, String.valueOf(((Spinner)item.getData("Spinner")).getSelection()));
					changed = true;
				}
			}
		}
		
		if(changed)
			return newStyle;
		else
			return null;
	}
	
	protected Text getNameItem(){
		return nameItem;
	}
}
