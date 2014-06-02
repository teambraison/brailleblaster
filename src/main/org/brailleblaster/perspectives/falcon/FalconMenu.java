package org.brailleblaster.perspectives.falcon;

import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.brailleblaster.wordprocessor.BBMenu;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;

public class FalconMenu extends BBMenu{
	private final int MENU_INDEX = 1;
	FalconController currentController;

	MenuItem openItem;
	
	public FalconMenu(final WPManager wp, FalconController idc) {
		super(wp);
		// TODO Auto-generated constructor stub
		
		setPerspectiveMenuItem(MENU_INDEX);
		currentController = idc;
		
		openItem = new MenuItem(fileMenu, SWT.PUSH, 0);
		openItem.setText(lh.localValue("&Open") + "\t" + lh.localValue("Ctrl + O"));
		openItem.setAccelerator(SWT.MOD1 + 'O');
		openItem.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				
			}		
		});
	}

	@Override
	public void setCurrent(Controller controller) {
		currentController = (FalconController)controller;
	}

	@Override
	public Controller getCurrent() {
		return currentController;
	}

}
