package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public abstract class StylePanel {
	static LocaleHandler lh = new LocaleHandler();
	
	private final static int LEFT_MARGIN = 0;
	private final static int RIGHT_MARGIN = 15;
	private final static int TOP_MARGIN = 50;
	private final static int BOTTOM_MARGIN = 100;
	
	protected Group group;
	protected StyleManager sm;
	
	public StylePanel(StyleManager sm, SashForm sash){
		this.sm = sm;
		this.group = new Group(sash, SWT.FILL | SWT.BORDER);
		this.group.setText(lh.localValue("editStyle"));
		setLayoutData(this.group, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		this.group.setLayout(new FormLayout());   	
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
	
	protected Label makeLabel(String text, int left, int right, int top, int bottom){
		Label l = new Label(group, SWT.BORDER | SWT.CENTER);
		l.setText(text);
		setLayoutData(l, left, right, top, bottom);
		
		return l;
	}
	
	protected Spinner makeSpinner(int left, int right, int top, int bottom){
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
	
	protected Combo makeCombo(String [] values, int left, int right, int top, int bottom){
		Combo cb = new Combo(group, SWT.BORDER);
		cb.setItems(values);
		setLayoutData(cb, left, right, top, bottom);
		
		return cb;
	}
	
	protected void showTable(){
		group.setVisible(true);
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
