package org.brailleblaster.perspectives;

import nu.xom.Document;

import org.brailleblaster.wordprocessor.BBStatusBar;
import org.brailleblaster.wordprocessor.WPManager;

public interface DocumentManager {
	//reset values or focus of a SWT component after switching perspectives
	public void restore(WPManager wp);
	
	//used to properly dispose of SWT components within the tab area when switching perspectives
	public void dispose();
	
	//Used when closing a tab and when closing a tab when the program exits.  The controller is responsible for checking and saving a document and performing necessary clean-up
	public void close();
	
	//returns a XOM document, which is passed to another controller when switching perspectives
	public Document getDoc();
	
	//Resets the status bar for a perspective dependent message
	public void setStatusBarText(BBStatusBar statusBar);
	
	//check before opening a new document whether it should reuse the current tab or open in a new tab
	public boolean canReuseTab();
	
	//performs any necessary clean-up before opening a document within the current tab
	public void reuseTab(String file);
}