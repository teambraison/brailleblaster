package org.brailleblaster.perspectives.falcon;

import java.io.File;

import nu.xom.Document;

import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.ArchiverFactory;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.imageDescriber.Falconator;
import org.brailleblaster.perspectives.imageDescriber.document.ImageDescriber;
import org.brailleblaster.perspectives.imageDescriber.views.ImageDescriberView;
import org.brailleblaster.util.ImageHelper;
import org.brailleblaster.wordprocessor.BBFileDialog;
import org.brailleblaster.wordprocessor.BBStatusBar;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabItem;

public class FalconController extends Controller{

	FalconDocument falcDoc;
	Falconator falcon = new Falconator("C:\\APPS\\cygwin\\home\\cmyers\\brailleblaster\\src\\main\\org\\brailleblaster\\perspectives\\imageDescriber\\Falconator.dll");
	Group group;
	
	public FalconController(WPManager wordProcesserManager, String fileName) {
		super(wordProcesserManager);
		
		this.item = new TabItem(wp.getFolder(), 0);
		this.group = new Group(wp.getFolder(), SWT.NONE);
		this.group.setLayout(new FormLayout());
		
		// Start the image describer and build the DOM
		if(fileName != null){
			if(openDocument(fileName))
				item.setText(fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1));
		}
		else {
			if(openDocument(null)){
				docCount++;
				if(docCount > 1)
					item.setText("Untitled #" + docCount);
				else
					item.setText("Untitled");
			}
		}
		
		//Set the views in the tab area
//		idv = new ImageDescriberView(this.group, imgDesc, this);
		this.item.setControl(this.group);
		
		// Image helper class. Image helper functions, and such.
//		imgHelper = new ImageHelper();
	}
	
	public FalconController(WPManager wp, Document doc, TabItem tabItem, Archiver arch) {
		super(wp);

		this.arch = arch;

		falcDoc = new FalconDocument(this, doc);
		this.item = tabItem;
		
		if(falcon != null)
			falcon.init();
		
//		this.group = new Group(wp.getFolder(), SWT.NONE);
//		this.group.setLayout(new FormLayout());
//		idv = new ImageDescriberView(this.group, imgDesc, this);
//		this.item.setControl(this.group);
//		idv.setTextBox(imgDesc.getCurDescription());
	}

	public boolean openDocument(String fileName){
		
		// Make sure we have a valid filename.
		if(fileName != null) 
			arch = ArchiverFactory.getArchive(fileName);
		else
			arch = ArchiverFactory.getArchive(templateFile);
		
		// Recent Files.
		addRecentFileEntry(fileName);
		
		// Start the document.
		try { return falcDoc.startDocument(arch.getWorkingFilePath(), arch.getCurrentConfig(), null); }
		catch (Exception e) { e.printStackTrace(); }
		return false;
	}

	public void fileOpenDialog() {
		falcon.setForce(0.0f, 7.0f, 0.0f);
		String tempName;
		String[] filterNames = new String[] { "XML", "XML ZIP", "EPUB", "XHTML", "HTML","HTM","UTDML working document"};
		String[] filterExtensions = new String[] { "*.xml", "*.zip", "*.epub", "*.xhtml","*.html", "*.htm", "*.utd"};
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
			
		} // if(tempName != null)

	} // fileOpenDialog()
	
	@Override
	public void restore(WPManager wp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		falcon.shutdown();
		
	}

	@Override
	public void close() {
		dispose();
		item.dispose();
	}

	@Override
	public Document getDoc() {
		return falcDoc.getDOM();
	}

	@Override
	public void setStatusBarText(BBStatusBar statusBar) {
		statusBar.setText( "Novint Falcon Perspective" );
		
	}

	@Override
	public boolean canReuseTab() {
		if(arch.getOrigDocPath() != null || arch.getDocumentEdited())
			return false;
		else
			return true;
	}

	@Override
	public void reuseTab(String file) {
		closeUntitledDocument();
		openDocument(file);
		item.setText(file.substring(file.lastIndexOf(File.separatorChar) + 1));
//		idv.setMainImage();
//		idv.setBrowser();
//		idv.setTextBox(imgDesc.getCurDescription());
//		idv.setAltBox(imgDesc.getCurElmAttribute("alt"));
	}

	private void closeUntitledDocument(){
		falcDoc.deleteDOM();
	}
}
