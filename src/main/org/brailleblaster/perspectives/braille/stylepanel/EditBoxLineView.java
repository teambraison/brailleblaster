package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class EditBoxLineView extends StylePanel {

	private Styles originalStyle;
	
	private Label styleLabel, topBoxLineLabel, bottomBoxLineLabel;
	private Text styleName, topBoxLine, bottomBoxLine;
	private Button saveButton, cancelButton;
	
	public EditBoxLineView(StyleManager sm, Group documentWindow, Styles style) {
		super(sm, documentWindow);
		originalStyle = style;
		
		styleLabel = makeLabel(lh.localValue("styleName"), 0, 50, 0, 10);
		styleName = new Text(group, SWT.BORDER);
		setLayoutData(styleName, 50, 100, 0, 10);
		styleName.setText(style.getName());
		styleName.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}
		});
		
		topBoxLineLabel = makeLabel("Top Line", 0, 50, 10, 20);
		topBoxLine = new Text(group, SWT.BORDER);
		setLayoutData(topBoxLine, 50, 100, 10, 20);
		topBoxLine.setTextLimit(1);
		
		bottomBoxLineLabel = makeLabel("Bottom Line", 0, 50, 20, 30);
		bottomBoxLine = new Text(group, SWT.BORDER);
		setLayoutData(bottomBoxLine, 50, 100, 20, 30);
		bottomBoxLine.setTextLimit(1);
		
		cancelButton = new Button(group, SWT.NONE);
		cancelButton.setText(lh.localValue("styleCancel"));
		setLayoutData(cancelButton, 0, 50, 90, 100);
		
		saveButton = new Button(group, SWT.NONE);
		saveButton.setText(lh.localValue("save"));
		setLayoutData(saveButton, 50, 100, 90, 100);
		
		Control [] tablist = {styleName, topBoxLine, bottomBoxLine, cancelButton, saveButton};
		group.setTabList(tablist);
		resetLayout();
		
		initializeListeners();
		initializeFormData();
		styleName.setFocus();
	}
	
	private void initializeListeners(){
		cancelButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				sm.closeEditStyle(null);
			}
		});
		
		saveButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveStyle();
			}
		});		
		
		group.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.stateMask == SWT.MOD3 && e.character == 'c')
					sm.closeEditStyle(null);
				else if(e.stateMask == SWT.MOD3 && e.character == 's')
					saveStyle();				
			}		
		});
	}
	
	private void initializeFormData(){
		if(originalStyle.contains(StylesType.topBoxline))
			topBoxLine.setText((String)originalStyle.get(StylesType.topBoxline));
		
		if(originalStyle.contains(StylesType.bottomBoxline))
			bottomBoxLine.setText((String)originalStyle.get(StylesType.bottomBoxline));
	}
	
	private void saveStyle(){
		if(validate()){
			LocaleHandler lh = new LocaleHandler();
			Styles newStyle = saveEditedStyle();
			if(newStyle != null)
				sm.saveEditedStyle(originalStyle, newStyle);
			else
				new Notify(lh.localValue("noChange"));
		}
	}
	
	private boolean validate(){
		if(topBoxLine.getText().length() > 0 && bottomBoxLine.getText().length() > 0)
			return true;
		else {
			new Notify("Both top and bottom boxlines must be specified");
			return false;
		}
	}
	
	protected Styles saveEditedStyle(){
		boolean changed = false;
		Styles newStyle = sm.getSemanticsTable().getNewStyle(originalStyle.getName());
		
		if(!originalStyle.contains(StylesType.topBoxline) || !((String)originalStyle.get(StylesType.topBoxline)).equals(topBoxLine.getText())){
			newStyle.put(StylesType.topBoxline, topBoxLine.getText());
			changed = true;
		}
		else
			newStyle.put(StylesType.topBoxline, (String)originalStyle.get(StylesType.topBoxline));
		
		if(!originalStyle.contains(StylesType.bottomBoxline) || !((String)originalStyle.get(StylesType.bottomBoxline)).equals(bottomBoxLine.getText())){
			newStyle.put(StylesType.bottomBoxline, bottomBoxLine.getText());
			changed = true;
		}
		else
			newStyle.put(StylesType.bottomBoxline, (String)originalStyle.get(StylesType.bottomBoxline));
		
		if(changed)
			return newStyle;
		else
			return null;
	}

}
