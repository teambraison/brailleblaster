package org.brailleblaster.perspectives.falcon;

import java.io.File;

import nu.xom.Document;

import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.ArchiverFactory;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.wordprocessor.BBFileDialog;
import org.brailleblaster.wordprocessor.BBStatusBar;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabItem;

public class FalconController extends Controller {

	FalconDocument falcDoc = null;
	Falconator falcon = new Falconator("C:\\APPS\\cygwin\\home\\cmyers\\brailleblaster\\src\\main\\org\\brailleblaster\\perspectives\\falcon\\Falconator.dll");
	Group group;
	FalconView fv;

	public FalconController(WPManager wordProcesserManager, String fileName) {
		super(wordProcesserManager);

		this.item = new TabItem(wp.getFolder(), 0);
		this.group = new Group(wp.getFolder(), SWT.NONE);
		this.group.setLayout(new FormLayout());

		// Start the image describer and build the DOM
		if (fileName != null) {
			if (openDocument(fileName))
				item.setText(fileName.substring(fileName
						.lastIndexOf(File.separatorChar) + 1));
		} else {
			if (openDocument(null)) {
				docCount++;
				if (docCount > 1)
					item.setText("Untitled #" + docCount);
				else
					item.setText("Untitled");
			}
		}

		// Set the views in the tab area
		fv = new FalconView(this.group, falcDoc, this);
		
		this.item.setControl(this.group);
	}

	public FalconController(WPManager wp, Document doc, TabItem tabItem, Archiver arch) {
		super(wp);

		this.arch = arch;

		falcDoc = new FalconDocument(this, doc);
		this.item = tabItem;

		if (falcon != null)
			falcon.init();

		this.group = new Group(wp.getFolder(), SWT.NONE);
		this.group.setLayout(new FormLayout());
		fv = new FalconView(this.group, falcDoc, this);
		this.item.setControl(this.group);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Sets the forces on the Falcon Device.
	public void setForce(float x, float y, float z) {
		falcon.setForce(x, y, z);
	} // setForce()

	//////////////////////////////////////////////////////////////////////////////////////////
	// Sets the image that our Falcon will use for boundaries.
	public void setImage(String path) {
		falcon.setImage(path);
	} // setImage()
	
	public boolean openDocument(String fileName) {

		// Make sure we have a valid filename.
		if (fileName == null)
			return false;
		// fileName = templateFile;
		arch = ArchiverFactory.getArchive(fileName, false);

		// Recent Files.
		addRecentFileEntry(fileName);

		// Start the document.
		boolean result = false;
		try {
			result = falcDoc.startDocument(arch.getWorkingFilePath(), arch.getCurrentConfig(), null);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		return result;
	}

	public void fileOpenDialog() {
		String tempName;
		String[] filterNames = new String[] { "XML", "XML ZIP", "EPUB",
				"XHTML", "HTML", "HTM", "UTDML working document" };
		String[] filterExtensions = new String[] { "*.xml", "*.zip", "*.epub",
				"*.xhtml", "*.html", "*.htm", "*.utd" };
		BBFileDialog dialog = new BBFileDialog(wp.getShell(), SWT.OPEN,
				filterNames, filterExtensions);

		tempName = dialog.open();

		// Don't do any of this if the user failed to choose a file.
		if (tempName != null) {
			// Open it.
			if (!canReuseTab()) {
				wp.addDocumentManager(tempName);
			}
			else
				reuseTab(tempName);

			addRecentFileEntry(tempName);

		} // if(tempName != null)

	} // fileOpenDialog()

	@Override
	public void restore(WPManager wp) {
		
	}

	@Override
	public void dispose() {
		falcon.shutdown();
		if (falcDoc != null)
			falcDoc.cleanup();
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
		statusBar.setText("Novint Falcon Perspective");

	}

	@Override
	public boolean canReuseTab() {
		if (arch.getOrigDocPath() != null || arch.getDocumentEdited())
			return false;
		else
			return true;
	}

	@Override
	public void reuseTab(String file) {
		closeUntitledDocument();
		openDocument(file);
		item.setText(file.substring(file.lastIndexOf(File.separatorChar) + 1));
		
		// Only get an image if there is a document.
		if(falcDoc != null)
		{
			// Get the first image. If there isn't one, grab them all from 
			// the document.
			if(falcDoc.getCurImg() == null)
				falcDoc.initImages();
			
			// Get the image.
			if(falcDoc.getCurImg() != null)
				fv.setImage(falcDoc.getCurImg());
		}
		
		// idv.setTextBox(imgDesc.getCurDescription());
		// idv.setAltBox(imgDesc.getCurElmAttribute("alt"));
	}

	private void closeUntitledDocument() {
		falcDoc.deleteDOM();
	}
}
