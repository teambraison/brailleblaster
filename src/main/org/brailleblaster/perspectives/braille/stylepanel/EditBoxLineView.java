package org.brailleblaster.perspectives.braille.stylepanel;

import java.util.ArrayList;

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

	private Styles boxline, topBox, middleBox, bottomBox;
	
	private Label styleLabel, topBoxLineLabel, middleBoxLineLabel, bottomBoxLineLabel;
	private Text styleName, topBoxLine, middleBoxLine, bottomBoxLine;
	private Button saveButton, cancelButton;
	
	public EditBoxLineView(StyleManager sm, Group documentWindow) {
		super(sm, documentWindow);
		boxline = sm.getSemanticsTable().get("boxline");
		topBox = sm.getSemanticsTable().get("topBox");
		middleBox = sm.getSemanticsTable().get("middleBox");
		bottomBox = sm.getSemanticsTable().get("bottomBox");
		
		styleLabel = makeLabel(lh.localValue("styleName"), 0, 50, 0, 10);
		styleName = new Text(group, SWT.BORDER);
		setLayoutData(styleName, 50, 100, 0, 10);
		styleName.setText(boxline.getName());
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
		
		middleBoxLineLabel = makeLabel("Middle Line", 0, 50, 20, 30);
		middleBoxLine = new Text(group, SWT.BORDER);
		setLayoutData(middleBoxLine, 50, 100, 20, 30);
		middleBoxLine.setTextLimit(1);
		
		bottomBoxLineLabel = makeLabel("Bottom Line", 0, 50, 30, 40);
		bottomBoxLine = new Text(group, SWT.BORDER);
		setLayoutData(bottomBoxLine, 50, 100, 30, 40);
		bottomBoxLine.setTextLimit(1);
		
		cancelButton = new Button(group, SWT.NONE);
		cancelButton.setText(lh.localValue("styleCancel"));
		setLayoutData(cancelButton, 0, 50, 90, 100);
		
		saveButton = new Button(group, SWT.NONE);
		saveButton.setText(lh.localValue("save"));
		setLayoutData(saveButton, 50, 100, 90, 100);
		
		Control [] tablist = {styleName, topBoxLine, middleBoxLine, bottomBoxLine, cancelButton, saveButton};
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
				saveStyles();
			}
		});		
		
		group.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.stateMask == SWT.MOD3 && e.character == 'c')
					sm.closeEditStyle(null);
				else if(e.stateMask == SWT.MOD3 && e.character == 's')
					saveStyles();				
			}		
		});
	}
	
	private void initializeFormData(){
		if(boxline.contains(StylesType.topBoxline))
			topBoxLine.setText((String)boxline.get(StylesType.topBoxline));
		
		if(boxline.contains(StylesType.bottomBoxline))
			bottomBoxLine.setText((String)boxline.get(StylesType.bottomBoxline));
		else if(!boxline.contains(StylesType.bottomBoxline) && bottomBox.contains(StylesType.bottomBoxline))
			bottomBoxLine.setText((String)bottomBox.get(StylesType.bottomBoxline));
		
		if(middleBox != null && middleBox.contains(StylesType.bottomBoxline))
			middleBoxLine.setText((String)middleBox.get(StylesType.bottomBoxline));
		else if(middleBox == null && (topBox != null && topBox.contains(StylesType.bottomBoxline)))
			middleBoxLine.setText((String)topBox.get(StylesType.bottomBoxline));
	}
	
	private void saveStyles(){
		if(validate()){
			LocaleHandler lh = new LocaleHandler();
			Styles newStyle = saveBasicBoxLine();
			Styles newTopBox = saveTopBox();
			Styles newMiddleBox = saveMiddleBox();
			Styles newBottomBox = saveBottomBox();
			ArrayList<Styles>list = new ArrayList<Styles>();
			
			if(newStyle != null)
				list.add(newStyle);
			if(newTopBox != null) 
				list.add(newTopBox);
			if(newMiddleBox != null)
				list.add(newMiddleBox);
			if(newBottomBox != null) 
				list.add(newBottomBox);
			
			if(list.size() == 0)
				new Notify(lh.localValue("noChange"));
			else
				sm.saveBoxline(list);
		}
	}
	
	private boolean validate(){
		if(topBoxLine.getText().length() > 0 && bottomBoxLine.getText().length() > 0 && middleBoxLine.getText().length() > 0)
			return true;
		else {
			new Notify("Top, middle and bottom boxlines must be specified");
			return false;
		}
	}
	
	protected Styles saveBasicBoxLine(){
		boolean changed = false;
		Styles newStyle = sm.getSemanticsTable().getNewStyle(boxline.getName());
		
		if(!boxline.contains(StylesType.topBoxline) || !((String)boxline.get(StylesType.topBoxline)).equals(topBoxLine.getText())){
			newStyle.put(StylesType.topBoxline, topBoxLine.getText());
			changed = true;
		}
		else
			newStyle.put(StylesType.topBoxline, (String)boxline.get(StylesType.topBoxline));
		
		if(!boxline.contains(StylesType.bottomBoxline) || !((String)boxline.get(StylesType.bottomBoxline)).equals(bottomBoxLine.getText())){
			newStyle.put(StylesType.bottomBoxline, bottomBoxLine.getText());
			changed = true;
		}
		else
			newStyle.put(StylesType.bottomBoxline, (String)boxline.get(StylesType.bottomBoxline));
		
		if(changed)
			return newStyle;
		else
			return null;
	}
	
	protected Styles saveTopBox(){
		boolean changed = false;
		Styles newStyle = sm.getSemanticsTable().getNewStyle(topBox.getName());
		
		if(!topBox.contains(StylesType.topBoxline) || !((String)topBox.get(StylesType.topBoxline)).equals(topBoxLine.getText())){
			newStyle.put(StylesType.topBoxline, topBoxLine.getText());
			changed = true;
		}
		else
			newStyle.put(StylesType.topBoxline, (String)topBox.get(StylesType.topBoxline));
		
		if(!topBox.contains(StylesType.bottomBoxline) || !((String)topBox.get(StylesType.bottomBoxline)).equals(middleBoxLine.getText())){
			newStyle.put(StylesType.bottomBoxline, middleBoxLine.getText());
			changed = true;
		}
		else
			newStyle.put(StylesType.bottomBoxline, (String)topBox.get(StylesType.bottomBoxline));
		
		if(changed)
			return newStyle;
		else
			return null;
	}

	protected Styles saveMiddleBox(){
		boolean changed = false;
		Styles newStyle = sm.getSemanticsTable().getNewStyle(middleBox.getName());
		
		if(!middleBox.contains(StylesType.bottomBoxline) || !((String)middleBox.get(StylesType.bottomBoxline)).equals(middleBoxLine.getText())){
			newStyle.put(StylesType.bottomBoxline, middleBoxLine.getText());
			changed = true;
		}
		else
			newStyle.put(StylesType.bottomBoxline, (String)middleBox.get(StylesType.bottomBoxline));
		
		if(changed)
			return newStyle;
		else
			return null;
	}
	
	protected Styles saveBottomBox(){
		boolean changed = false;
		Styles newStyle = sm.getSemanticsTable().getNewStyle(bottomBox.getName());
		
		if(!bottomBox.contains(StylesType.bottomBoxline) || !((String)bottomBox.get(StylesType.bottomBoxline)).equals(bottomBoxLine.getText())){
			newStyle.put(StylesType.bottomBoxline, bottomBoxLine.getText());
			changed = true;
		}
		else
			newStyle.put(StylesType.bottomBoxline, (String)bottomBox.get(StylesType.bottomBoxline));
		
		if(changed)
			return newStyle;
		else
			return null;
	}
}
