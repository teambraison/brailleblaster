package org.brailleblaster.perspectives.webView;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
/**
 * Show an Epub book in the Browser using swt widget (view model)
 * @author smastoureshgh
 * @version 0.0.0
 */

public class webViewBrowser  {

	WebViewController webc;
	TabItem item;
	Group group;
	Browser browser;
	Button previous=null;
	Button next=null;
	int index=0;

	/**
	 * constructor
	 * @param webc : a reference to WebViewController
	 * @param folder: a reference to TabFolder widget of swt
	 */
	public webViewBrowser(WebViewController webc, TabFolder folder)
	{
		this.webc=webc;
		createContents(folder);

	}


	/**
	 * Creates the main window's contents to show a book in the browser
	 * @param shell the main window
	 */
	private void createContents(TabFolder folder) {
		item = new TabItem(folder, SWT.NONE);
		group = new Group(folder, SWT.BORDER);
		group.setLayout(new FillLayout());
		item.setControl(group);
		// Create a web browser
		browser = new Browser(group, SWT.WEBKIT);
		browser.setJavascriptEnabled(true);


	}



	/**
	 * Show the content of each chapter in the browser view
	 * @param index
	 */
	public void showContents(int index){
		if (index> webc.getSize()-1)
			index=0;
		if (index<0)
			index=webc.getSize()-1;	
		// Script to replace current style sheet and change picture with text
		final String script = 
				"var allsuspects=document.getElementsByTagName('link'); " + 
				"for (var i=allsuspects.length-1; i>=0; i--) { " + 
		        "if (allsuspects[i].getAttribute('href')!=null) { " + 
		        "var newelement=document.createElement('link'); " +
		        "newelement.setAttribute('rel', 'stylesheet'); " +
		        "newelement.setAttribute('type', 'text/css'); " + 
		        "newelement.setAttribute('href', 'brailstylesheet.css'); " + 
		        "allsuspects[i].parentNode.replaceChild(newelement, allsuspects[i]); " +
		        "}" + 
		        "}";
	
		// listen to when the page load in browser
		browser.addProgressListener(new ProgressListener() {
			public void changed(ProgressEvent event) {
			}

			public void completed(ProgressEvent event) {
				browser.execute(script);


			}

		});
		browser.setUrl(webc.getChapter(index));

	}
	/**
	 * Navigate through content of the book by using right or left arrow 
	 */

	public void navigate()
	{
		//navigate through the book by keyboard left and right arrow
		browser.addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event) {
				if(event.keyCode == SWT.ARROW_LEFT)
				{
					showContents(--index);

				}
				if(event.keyCode == SWT.ARROW_RIGHT)
				{
					showContents(++index);

				}
			}
		});

	}



}
