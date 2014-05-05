package org.brailleblaster.perspectives.webView;

import java.io.File;
import java.io.IOException;

import nu.xom.Document;

import org.apache.commons.io.FileUtils;
import org.brailleblaster.BBIni;
import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.ArchiverFactory;
import org.brailleblaster.archiver.EPub3Archiver;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.imageDescriber.document.ImageDescriber;
import org.brailleblaster.perspectives.imageDescriber.views.ImageDescriberView;
import org.brailleblaster.perspectives.webView.WebViewDocument.webViewDocument;
import org.brailleblaster.util.ImageHelper;
import org.brailleblaster.util.Notify;
import org.brailleblaster.wordprocessor.BBFileDialog;
import org.brailleblaster.wordprocessor.BBStatusBar;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabItem;

/**
 * child of parent class Controller which the controller 
 * class manages events within the tab folder 
 * (control model) which controls webViewBrowser (view) model
 * @author smastoureshgh
 * @version 0.0.0
 */
public class WebViewController extends Controller {


	//Archiver arch ;
	webViewBrowser vb=null;
    webViewDocument webDoc;
	int index=0;
	String currentPath;
	String currentConfig;
	/**
	 * constructor create new reference to Browser view 
	 * Come to this constructor when the perspective is Web View for the first time
	 * @param wp :reference to WPManager class
	 * @param fileName: reference to file name
	 */
	public WebViewController(WPManager wp, String fileName) {

		super(wp);
		currentPath=fileName;
		vb=new webViewBrowser(this,wp.getFolder(),null);
		// if no file opened then create an empty template
		if(currentPath == null)
			arch = ArchiverFactory.getArchive(templateFile);

	}
	/**
	 * constructor : come to this constructor when changing between perspectives
	 * @param wp
	 * @param doc
	 * @param tabItem
	 * @param arch : get archiver from previous perspective 
	 */

	public WebViewController(WPManager wp, Document doc, TabItem tabItem,Archiver arch) {
		super(wp);
		this.arch=arch;

		currentPath =arch.getWorkingFilePath();
		vb=new webViewBrowser(this,wp.getFolder(),tabItem);
		if(arch instanceof EPub3Archiver){
			if(currentPath!=null){
				// write css file in directory of the book
				writeCss(currentPath);
				//vb.item.setText("Book");
				vb.showContents(0);
				vb.navigate();	
			}
		}
		//else
			//new Notify("Please open an epub book");
		
			
		webDoc = new webViewDocument(this, doc);

	

		
	}

	/**
	 * Open Epub book and save it to archFileName
	 * @param filepath
	 * @return
	 */
	private void openBook(String fileName){
		String archFileName=null;
		arch = ArchiverFactory.getArchive(fileName);
		
		if(arch != null){
			archFileName = arch.getWorkingFilePath();
			currentPath = archFileName;
			currentConfig = "epub.cfg";
			// write css file in directory of the book
			writeCss(archFileName);
		}


	}
	/**
	 * Get Current Chapter of Epub book
	 * @param index
	 * @return
	 */
	public String getChapter(int index){
		String str = null;
		if(arch instanceof EPub3Archiver)
			str=((EPub3Archiver)arch).getSpineFilePath(index);
		return str;

	}
	/**
	 * @return size of epub book
	 */
	public int getSize()
	{
		int bookSize=0;
		if(arch instanceof EPub3Archiver)
			bookSize=((EPub3Archiver)arch).getSpine().size();
		return bookSize;

	}
	public Archiver getArchiver() {
		return arch;
	}
	
	/**
	 *  Open a file dialog just with Epub option
	 */

	public void fileOpenDialog() {
		String tempName;	
		String[] filterNames = new String[] {  "EPUB"};
		String[] filterExtensions = new String[] {  "*.epub"};
		BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.OPEN, filterNames, filterExtensions);

		tempName = dialog.open();

		// Don't do any of this if the user failed to choose a file.
		if(tempName != null) {
			// Open it.
			if(!canReuseTab())
				wp.addDocumentManager(tempName);
			else 
				reuseTab(tempName);

			addRecentFileEntry(tempName);
		} 



	}
    /**
     * Write a css file in temp folder
     * @param path : file path for temp folder in java
     */
	public void writeCss(String path)
	{
		String sourcePath=BBIni.getProgramDataPath() + BBIni.getFileSep()+"styles"+BBIni.getFileSep() +"brailstylesheet.css";
		File source = new File(sourcePath);
		//remove temp.xml from the path and go to its directory
		path=path.replace("temp.xml", "");
		File desc = new File(path);
		try {
		    FileUtils.copyFileToDirectory(source, desc);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
	}

	@Override
	public void restore(WPManager wp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
	
		vb.group.dispose();

	}

	@Override
	public void close() {
		dispose();

	}

	@Override
	public Document getDoc() {
		// TODO Auto-generated method stub
		if(webDoc == null){
			 this.webDoc = new webViewDocument(this);
		     webDoc.startDocument(arch.getWorkingFilePath(), arch.getCurrentConfig(), null);
		}
		return webDoc.getDOM();
		//return null;
	}

	@Override
	public void setStatusBarText(BBStatusBar statusBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canReuseTab() {
		if (currentPath==null) 
		{
			return true;
		}
		else if (currentPath.substring(currentPath.length()-5, currentPath.length() ).equals(".html"))
			return true;
		
		else
			return false;

	}

	@Override
	/**
	 * open the book and show the content of the book and navigate through it
	 */
	public void reuseTab(String file) {
		if (file!=null)
		{	
			openBook(file);
			vb.item.setText(file.substring(file.lastIndexOf(File.separatorChar) + 1));
			vb.showContents(0);
			vb.navigate();
		}   


	}


}
