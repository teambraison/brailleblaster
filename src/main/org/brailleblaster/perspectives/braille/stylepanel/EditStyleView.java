package org.brailleblaster.perspectives.braille.stylepanel;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class EditStyleView {
	private final static int LEFT_MARGIN = 0;
	private final static int RIGHT_MARGIN = 15;
	private final static int TOP_MARGIN = 50;
	private final static int BOTTOM_MARGIN = 100;
	
	private Styles originalStyle;
	private boolean apply;
	private EditTable table;
	private Text nameItem;
	
	private Group group;
	private StyleManager sm;
	private Button applyButton, cancelButton;
	private Table t;
	private TableColumn tc1, tc2;
	private SelectionListener applyListener, saveAsListener;
	
	public EditStyleView(final StyleManager sm, Group documentWindow, Styles style){
		LocaleHandler lh = new LocaleHandler();
		this.sm = sm;
		originalStyle = style;
		this.group = new Group(documentWindow, SWT.FILL | SWT.BORDER);
		this.group.setText(lh.localValue("editStyle"));
		setLayoutData(this.group, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		this.group.setLayout(new FormLayout());   
		
		t = new Table(group, SWT.BORDER);
		setLayoutData(t, 0, 100, 0, 90);
		t.setLinesVisible(true);
		t.setHeaderVisible(true);	
		
		tc1 = new TableColumn(t, SWT.CENTER);
		tc1.setText(lh.localValue("styleAttributes"));
		tc1.setResizable(false);
		
		tc2 = new TableColumn(t, SWT.CENTER);   
	    tc2.setText(lh.localValue("styles"));
		tc2.setResizable(false);
	    
		cancelButton = new Button(group, SWT.NONE);
		cancelButton.setText(lh.localValue("styleCancel"));
		setLayoutData(cancelButton, 0, 50, 90, 100);
		
		applyButton = new Button(group, SWT.NONE);
		applyButton.setText(lh.localValue("apply"));
		setLayoutData(applyButton, 50, 100, 90, 100);
		apply = true;
		
		group.pack();
		group.getParent().layout();
		
		tc1.setWidth(group.getClientArea().width / 2);
		tc2.setWidth(group.getClientArea().width / 2);
		
		t.getHorizontalBar().dispose();
		
		applyListener = new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				LocaleHandler lh = new LocaleHandler();
				Styles newStyle = table.saveEditedStyle();
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
		
		table = new EditTable(t, style, this.sm);
		initializeListeners();
		t.setFocus();
	}
	
	private void initializeListeners(){
		t.addListener(SWT.Resize, new Listener(){
			@Override
			public void handleEvent(Event e) {
				tc1.setWidth(group.getClientArea().width / 2);
				tc2.setWidth(group.getClientArea().width / 2);
			}
		});
		
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
		nameItem = table.getNameItem();
		nameItem.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				if(!nameItem.getText().equals(originalStyle.getName()) && apply)
					toggleApplyButton();
				else if(nameItem.getText().equals(originalStyle.getName()) && !apply)
					toggleApplyButton();
			}		
		});
	}
	
	private void setLayoutData(Control c, int left, int right, int top, int bottom){
		FormData location = new FormData();
		
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		
		c.setLayoutData(location);
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
	
	protected boolean isVisible(){
        if(!t.isDisposed() && t.isVisible())
            return true;
        else
            return false;
    }
	
	protected Group getGroup(){
        return group;
    }
	
	protected void dispose(){
		group.dispose();
	}
}
