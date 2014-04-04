package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class EditPanel {
	static LocaleHandler lh = new LocaleHandler();
	
	private final static int LEFT_MARGIN = 0;
	private final static int RIGHT_MARGIN = 15;
	private final static int TOP_MARGIN = 50;
	private final static int BOTTOM_MARGIN = 100;
	
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
	protected Group group;
	protected StyleManager sm;
	protected Text styleName;

	private Label styleLabel, linesBeforeLabel, linesAfterLabel, marginLabel, indentLabel, alignmentLabel, emphasisLabel;
	protected Combo alignmentCombo, emphasisCombo;
	protected Spinner linesBeforeSpinner, linesAfterSpinner, marginSpinner, indentSpinner;
	
	public EditPanel(StyleManager sm, Group documentWindow, Styles style){
		this.sm = sm;
		originalStyle = style;
		this.group = new Group(documentWindow, SWT.FILL | SWT.BORDER);
		this.group.setText(lh.localValue("editStyle"));
		setLayoutData(this.group, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		this.group.setLayout(new FormLayout());   	
		
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
	
	protected void setLayoutData(Control c, int left, int right, int top, int bottom){
		FormData location = new FormData();
		
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		
		c.setLayoutData(location);
	}
	
	protected void resetLayout(){
		group.pack();
		group.getParent().layout();
	}
	
	private Label makeLabel(String text, int left, int right, int top, int bottom){
		Label l = new Label(group, SWT.BORDER | SWT.CENTER);
		l.setText(text);
		setLayoutData(l, left, right, top, bottom);
		
		return l;
	}
	
	private Spinner makeSpinner(int left, int right, int top, int bottom){
		Spinner sp = new Spinner(group, SWT.BORDER);
		setLayoutData(sp, left, right, top, bottom);
		return sp;
	}
	
	protected void setSpinnerData(Spinner sp, Styles style, StylesType type){
		if(style.contains(type))
			sp.setSelection(Integer.valueOf((String)style.get(type)));
		else
			sp.setSelection(0);
	}
	
	private Combo makeCombo(String [] values, int left, int right, int top, int bottom){
		Combo cb = new Combo(group, SWT.BORDER);
		cb.setItems(values);
		setLayoutData(cb, left, right, top, bottom);
		
		return cb;
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
	
	protected void showTable(){
		group.setVisible(true);
		styleName.setFocus();
	}
	
	protected void hideTable(){
		group.setVisible(false);
	}
	
	protected Group getGroup(){
        return group;
    }
	
	protected void dispose(){
		group.dispose();
	}
	
	protected boolean isVisible(){
        if(!group.isDisposed() && group.isVisible())
            return true;
        else
            return false;
    }
}
