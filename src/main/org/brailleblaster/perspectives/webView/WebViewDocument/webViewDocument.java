package org.brailleblaster.perspectives.webView.WebViewDocument;

import nu.xom.Document;

import org.brailleblaster.document.BBDocument;
import org.brailleblaster.perspectives.Controller;
/**
 * child class of parent BBDocument class  which manages Dom elements
 * @author smastoureshgh
 * @version:0.0.0
 */

public class webViewDocument extends BBDocument{
	
	/**
	 * constructor
	 * @param dm superclass Controller
	 * @param doc xom Document object 
	 */

	public webViewDocument(Controller dm, Document doc) {
		super(dm, doc);
		// TODO Auto-generated constructor stub
	}
	/**
	 * second constructor
	 * @param dm superclass Controller
	 */
	public webViewDocument(Controller dm)
	{
		super(dm);
	}
	
	public boolean startDocument(String path, String configFile, String configSettings){
		try {
			return super.startDocument(path, configFile, configSettings);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	

}
