package org.brailleblaster.views;

import java.util.Map.Entry;

import nu.xom.Element;
import nu.xom.Node;

import org.brailleblaster.document.BBSemanticsTable;
import org.brailleblaster.document.BBSemanticsTable.Styles;
import org.brailleblaster.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.messages.BBEvent;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class PropertyView {
	private final static int LEFT_MARGIN = 0;
	private final static int RIGHT_MARGIN = 15;
	private final static int TOP_MARGIN = 70;
	private final static int BOTTOM_MARGIN = 100;
	
	private BBSemanticsTable stylesTable;
	
	private Group group;
	private Label nameLabel;
	private Spinner linesBeforeSpinner, linesAfterSpinner, indentSpinner, lineWrapSpinner;
	private Combo combo;
	
	public PropertyView(Manager dm, Group documentWindow) {
		this.group = new Group(documentWindow, SWT.BORDER | SWT.V_SCROLL);
		this.stylesTable = dm.getStyleTable();
		group.setText("Element Attributes");
		setLayoutData(group, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		group.setLayout(new FormLayout());
		
		Combo styleList = new Combo(group, SWT.NONE);
		String [] list = {"Placeholder","for", "formatting", "options"};
		styleList.setItems(list);
		setLayoutData(styleList, 0, 70, 0, 10);
		
		Button newStyleButton = new Button(group, SWT.NONE);
		newStyleButton.setText("New");
		setLayoutData(newStyleButton, 70, 100, 0, 10);
		
		nameLabel = new Label(group, SWT.FILL | SWT.CENTER);
		nameLabel.setText("None Selected");
		FontData[] fD = nameLabel.getFont().getFontData();
		fD[0].setStyle(SWT.BOLD);
		nameLabel.setFont( new Font(group.getDisplay(),fD[0]));
		setLayoutData(nameLabel, 0, 100, 15, 25);
		
		Label linesBefore = new Label(group, SWT.CENTER);
		linesBefore.setText("Lines Before");
		setLayoutData(linesBefore, LEFT_MARGIN, 75, 25, 40);
		
		linesBeforeSpinner = new Spinner(group, SWT.BORDER);
		linesBeforeSpinner.setSelection(0);
		setLayoutData(linesBeforeSpinner, 75, 100, 25, 40);
		
		Label linesAfter = new Label(group, SWT.NONE);
		linesAfter.setText("Lines After");
		linesAfter.setAlignment(SWT.CENTER);
		setLayoutData(linesAfter, LEFT_MARGIN, 75, 40, 55);
		
		linesAfterSpinner = new Spinner(group, SWT.BORDER);
		linesAfterSpinner.setSelection(0);
		setLayoutData(linesAfterSpinner, 75, 100, 40, 55);
		
		Label indent = new Label(group, SWT.NONE);
		indent.setText("Indent");
		indent.setAlignment(SWT.CENTER);
		setLayoutData(indent, LEFT_MARGIN, 75, 55, 70);
		
		indentSpinner = new Spinner(group, SWT.BORDER);
		indentSpinner.setSelection(0);
		setLayoutData(indentSpinner, 75, 100, 55, 70);
		
		Label lineWrap = new Label(group, SWT.NONE);
		lineWrap.setText("Line Wrap");
		lineWrap.setAlignment(SWT.CENTER);
		setLayoutData(lineWrap, LEFT_MARGIN, 75, 70, 85);
		
		lineWrapSpinner = new Spinner(group, SWT.BORDER);
		lineWrapSpinner.setSelection(0);
		setLayoutData(lineWrapSpinner, 75, 100, 70, 85);
		
		Label alignment = new Label(group, SWT.NONE);
		alignment.setText("Alignment");
		alignment.setAlignment(SWT.CENTER);
		setLayoutData(alignment, LEFT_MARGIN, 75, 85, 100);
		
		combo = new Combo(group, SWT.DROP_DOWN | SWT.FILL);
		String [] arr = {"Left", "Center","Right"};
		combo.setItems(arr);
		setLayoutData(combo, 75, 100, 85, 100);
		
		initializeListeners(dm);
	}
	
	private void setLayoutData(Control c, int left, int right, int top, int bottom){
		FormData location = new FormData();
		
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		c.setLayoutData(location);
	}
	
	private void initializeListeners(final Manager dm){
		linesBeforeSpinner.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Message m = new Message(BBEvent.UPDATE_STYLE);
				m.put("key", StylesType.linesBefore);
				m.put("value", linesBeforeSpinner.getSelection());
				dm.dispatch(m);
			}	
		});
		
		linesAfterSpinner.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Message m = new Message(BBEvent.UPDATE_STYLE);
				m.put("key", StylesType.linesAfter);
				m.put("value", linesAfterSpinner.getSelection());
				dm.dispatch(m);
			}	
		});
		
		indentSpinner.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Message m = new Message(BBEvent.UPDATE_STYLE);
				m.put("key", StylesType.firstLineIndent);
				m.put("value", indentSpinner.getSelection());
				dm.dispatch(m);
			}	
		});
		
		lineWrapSpinner.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Message m = new Message(BBEvent.UPDATE_STYLE);
				m.put("key", StylesType.leftMargin);
				m.put("value", lineWrapSpinner.getSelection());
				dm.dispatch(m);
			}	
		});
		
		combo.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub			
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				Message m = new Message(BBEvent.UPDATE_STYLE);
				m.put("key", StylesType.format);
				if(combo.getSelectionIndex() == 2)
					m.put("value", SWT.RIGHT);
				else if(combo.getSelectionIndex() == 1){
					m.put("value", SWT.CENTER);
				}
				else {
					m.put("value", SWT.LEFT);
				}
				dm.dispatch(m);			
			}
		});
	}
	
	public void populateView(Node n){
		resetView();
		String key = this.stylesTable.getKeyFromAttribute((Element)n.getParent());
		Styles style = this.stylesTable.makeStylesElement(key, n);
		
		String name = ((Element)n.getParent()).getLocalName();
		nameLabel.setText("Element: " + name);
		
		for (Entry<StylesType, String> entry : style.getEntrySet()) {
			switch(entry.getKey()){
				case linesBefore:
					linesBeforeSpinner.setSelection(Integer.valueOf(entry.getValue()));
					linesBeforeSpinner.setEnabled(true);
					break;
				case linesAfter:
					linesAfterSpinner.setSelection(Integer.valueOf(entry.getValue()));
					linesAfterSpinner.setEnabled(true);
					break;
				case firstLineIndent: 
					indentSpinner.setSelection(Integer.valueOf(entry.getValue()));
					indentSpinner.setEnabled(true);
					break;
				case format:
					int val = Integer.valueOf(entry.getValue());
					if(val == SWT.RIGHT)
						combo.select(2);
					else if(val == SWT.CENTER)
						combo.select(1);
					else 
						combo.select(0);
					
					combo.setEnabled(true);
					break;	
				case Font:			
					 break;
				case leftMargin:
					lineWrapSpinner.setSelection(Integer.valueOf(entry.getValue()));
					lineWrapSpinner.setEnabled(true);
					break;
				default:
					System.out.println(entry.getKey());
			}
		}
		group.layout();
	}
	
	private void resetView(){
		linesBeforeSpinner.setSelection(0);
		linesAfterSpinner.setSelection(0);
		indentSpinner.setSelection(0);
		lineWrapSpinner.setSelection(0);
		combo.deselectAll();
	}

	public void setFocus(){
		linesBeforeSpinner.setFocus();
	}
}
