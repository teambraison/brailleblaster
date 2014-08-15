package org.brailleblaster.perspectives.falcon;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.perspectives.Controller;
import org.eclipse.swt.graphics.Image;

public class FalconDocument extends BBDocument {

	// The current image object.
	// We hold onto it so it can easily be retrieved and deleted.
	Image curImage = null;
	// Index of current image.
	int curImageIdx = -1;
	// The path to the current image.
	String curImagePath = null;
	// Namespace.
	String nameSpace;
	// Context/namespace for xpath.
	XPathContext context;
	// xPath - current image nodes.
	Nodes imgs = null;
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// Constructor - Controller.
	public FalconDocument(Controller dm) {
		super(dm);
		
	} // FalconDocument(Controller dm) - Constructor.

	////////////////////////////////////////////////////////////////////////////////////
	// Constructor - Controller, Document.
	public FalconDocument(Controller dm, Document doc) {
		
		// Call super!
		super(dm, doc);
		
		// Namespace and context for document.
		nameSpace = doc.getRootElement().getDocument().getRootElement().getNamespaceURI();
		context = new XPathContext("dtb", nameSpace);
		
		// Use xPath to grab every <img>.
		imgs = doc.getRootElement().query("//dtb:img[1]", context);
		
		// Set the current image.
		curImageIdx = 0;
		
	} // FalconDocument(Controller dm, Document doc) - Constructor.
	
	////////////////////////////////////////////////////////////////////////////////////
	// Once a document is opened, we can call this to grab all images for later 
	// traversal.
	public void initImages()
	{
		// If we currently don't have images, search the document for some.
		if(imgs.size() == 0)
			imgs = doc.getRootElement().query("//dtb:img[1]", context);
			
	} // initImages()
	
	////////////////////////////////////////////////////////////////////////////////////
	// Get the current image this document is looking at.
	public Image getCurImg()
	{
		// Make sure there are images at all.
		if(imgs.size() == 0)
			return null;
		
		// Path to image.
		String imgPath = ((Element)(imgs.get(curImageIdx))).getAttributeValue("src");
		
		// Build image path.
		imgPath = dm.getWorkingPath().substring(0, dm.getWorkingPath().lastIndexOf(BBIni.getFileSep())) + BBIni.getFileSep() + imgPath;
		if(imgPath.contains("/") && BBIni.getFileSep().compareTo("/") != 0)
			imgPath = imgPath.replace("/", "\\");
		
		// Remove %20(space).
		imgPath = imgPath.replace("%20", " ");
		
		// If there's an image in here already, get rid of it, and get this new one.
		if(curImage != null)
			curImage.dispose();
		
		// Store path to our image.
		curImagePath = imgPath;
		
		// Get new image.
		curImage = new Image(null, imgPath);
		
		// Finally, return it.
		return curImage;
		
	} // getCurImg()
	
	////////////////////////////////////////////////////////////////////////////////////
	// Returns the path to the current image.
	public String getCurImgPath() {
		return curImagePath;
	} // getCurImgPath()
	
	////////////////////////////////////////////////////////////////////////////////////
	// Cleans up memory we may, or may not, be taking up.
	public void cleanup() {
		if(curImage != null)
			curImage.dispose();
	} // cleanup().

} // class FalconDocument()
