package org.brailleblaster.perspectives.braille.views.wp;

import org.brailleblaster.perspectives.braille.Manager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class EditMenu {
	Menu menu;
	MenuItem copyItem;
	MenuItem cutItem;
	MenuItem pasteItem;
	MenuItem hideItem;
	WPView view;
	
	public EditMenu(WPView view, final Manager manager){
		this.view = view;
		menu = new Menu(view.view);
		
		copyItem = new MenuItem(menu, SWT.PUSH);
		copyItem.setText("Copy");
		copyItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				manager.getText().copy();
			}
		});
		
		cutItem = new MenuItem(menu, SWT.PUSH);
		cutItem.setText("Cut");
		cutItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				manager.getText().cut();
			}
		});
		
		pasteItem = new MenuItem(menu, SWT.PUSH);
		pasteItem.setText("Paste");
		pasteItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				manager.getText().paste();
			}
		});
		
		hideItem = new MenuItem(menu, SWT.PUSH);
		hideItem.setText("Hide");
		hideItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				manager.hide();
			}
		});
		
		view.view.setMenu(menu);
	}
	
	protected void dispose(){
		copyItem.dispose();
		cutItem.dispose();
		pasteItem.dispose();
		hideItem.dispose();
		menu.dispose();
	}
}
