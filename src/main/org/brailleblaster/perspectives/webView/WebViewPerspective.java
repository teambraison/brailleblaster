package org.brailleblaster.perspectives.webView;


import org.brailleblaster.perspectives.Perspective;
import org.brailleblaster.perspectives.webView.WebViewMenu.WebViewMenu;
import org.brailleblaster.wordprocessor.WPManager;
/**
 * Making a perspective class for View view of epub books
 * following structure of project to extends perspective class
 * @version:0.0.0
 * @author smastoureshgh
 */

public class WebViewPerspective extends Perspective{
	
	/**
	 * Constructor
	 * @param wp : reference to WPManager class
	 * @param controller :reference to my controller class 
	 */
	public WebViewPerspective(WPManager wp, WebViewController controller){
		perspectiveType = WebViewController.class;
		this.controller = controller;
		this.menu = new WebViewMenu(wp, controller);
	}
	

	@Override
	public void dispose() {
		menu.dispose();
	
		
	}

}
