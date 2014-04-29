package org.brailleblaster.perspectives.webView.WebViewMenu;

import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.brailleblaster.perspectives.webView.WebViewController;
import org.brailleblaster.settings.ConfigFileDialog;
import org.brailleblaster.wordprocessor.BBMenu;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
/**
 * child class of parent class BBMenu which The BBMenu class provides a minimal menu
 * at the top of the SWT shell,it contains several items common across all perspectives 
 * @author smastoureshgh
 * @version 0.0.0
 */
public class WebViewMenu extends BBMenu{
	
	private final int MENU_INDEX = 2;
	private WebViewController currentController;
	MenuItem openItem;

	/**
	 * constructor
	 * @param wp : reference to WPManager class
	 * @param controller :reference to my controller class 
	 */
	public WebViewMenu(final WPManager wp, WebViewController idc) {
		super(wp);
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
				int index = wp.getFolder().getSelectionIndex();
				if(index == -1){
					wp.addDocumentManager(null);
					currentController = (WebViewController) wp.getList().getFirst(); 
					currentController.fileOpenDialog();
					if(currentController.getWorkingPath() == null){
						currentController.close();
						wp.removeController(currentController);
						currentController = null;
					}
				}
				else {
					currentController.fileOpenDialog();
				}
			}		
		});
		

	}


	//public WebViewMenu(WPManager wp, WebViewController controller) {
		//super(wp);
		//setPerspectiveMenuItem(MENU_INDEX);

	//}

	@Override
	public void setCurrent(Controller controller) {
		currentController = (WebViewController)controller;
		
	}

	@Override
	public Controller getCurrent() {
		return currentController;

	}

}
