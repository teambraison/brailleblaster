package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class EditPanel extends StylePanel {
	//For use in making localized UI
	final private String [] emphasisList = {lh.localValue("bold"), lh.localValue("italic"), lh.localValue("underline")};
	//For determining combo box position in relation to liblouis value
	protected static final int BOLD = 0;
	protected static final int ITALIC = 1;
	protected static final int UNDERLINE = 2;
			
	final private String [] alignmentList = {lh.localValue("left"), lh.localValue("center"), lh.localValue("right")};
	protected static final int LEFT = 0;
	protected static final int CENTER = 1;
	protected static final int RIGHT = 2;
		
	protected Styles originalStyle;
	protected Text styleName;

	private Label styleLabel, linesBeforeLabel, linesAfterLabel, marginLabel, indentLabel, alignmentLabel, emphasisLabel;
	protected Combo alignmentCombo, emphasisCombo;
	protected Spinner linesBeforeSpinner, linesAfterSpinner, marginSpinner, indentSpinner;
	
	public EditPanel(StyleManager sm, Group documentWindow, Styles style){
		super(sm, documentWindow);
		originalStyle = style;
		
		styleLabel = makeLabel(lh.localValue("styleName"), 0, 50, 0, 10);
		styleName = new Text(group, SWT.BORDER);
		setLayoutData(styleName, 50, 100, 0, 10);
		styleName.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				String key = String.valueOf(e.character);
				if(key.matches("\\s+"))
					e.doit = false;
			}
		});
		styleName.setToolTipText(lh.localValue("styleNameTooltip"));
		
		linesBeforeLabel = makeLabel(lh.localValue("linesBefore"), 0, 50, 10, 20);
		linesBeforeSpinner = makeSpinner(50, 100, 10, 20);
		linesBeforeSpinner.setMinimum(0);
		linesBeforeSpinner.setToolTipText(lh.localValue("linesBeforeTooltip"));
		
		linesAfterLabel = makeLabel(lh.localValue("linesAfter"), 0, 50, 20, 30);
		linesAfterSpinner = makeSpinner(50, 100, 20, 30);
		linesAfterSpinner.setMinimum(0);
		linesAfterSpinner.setToolTipText(lh.localValue("linesAfterTooltip"));
		
		marginLabel = makeLabel(lh.localValue("margin"), 0, 50, 30, 40);
		marginSpinner = makeSpinner(50, 100, 30, 40);
		marginSpinner.setMinimum(0);
		marginSpinner.setToolTipText(lh.localValue("marginToolTip"));
		
		indentLabel = makeLabel(lh.localValue("indent"), 0, 50, 40, 50);
		indentSpinner = makeSpinner(50, 100, 40, 50);
		indentSpinner.setMinimum(-100);
		indentSpinner.setToolTipText(lh.localValue("indentTooltip"));
		
		alignmentLabel = makeLabel(lh.localValue("alignment"), 0, 50, 50, 60);
		alignmentCombo = makeCombo(alignmentList, 50, 100, 50, 60);
		alignmentCombo.setToolTipText(lh.localValue("alignmentTooltip"));
		
		emphasisLabel = makeLabel(lh.localValue("emphasis"), 0, 50, 60, 70);
		emphasisCombo = makeCombo(emphasisList, 50, 100, 60, 70);
		emphasisCombo.setToolTipText(lh.localValue("emphasisTooltip"));
	}
	
	
	
	protected Styles getNewStyle(){
		Styles newStyle;
		if(styleName.getText().length() == 0){
			new Notify(lh.localValue("blankName"));
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
				newStyle.put(StylesType.format, "leftJustified");
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
			new Notify(lh.localValue("blankStyle"));
			return null;
		}
	}
	
	@Override
	protected void showTable(){
		group.setVisible(true);
		styleName.setFocus();
	}
}
