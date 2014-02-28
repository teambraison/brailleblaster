/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.txt
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.perspectives.imageDescriber.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.eclipse.swt.graphics.Image;

public class ImageDescriber extends BBDocument {
	// The document with images we want to add descriptions to.
	private ImageDescriberController dm;
	//private BBDocument doc;
	// Current image element.
	private Element curImgElement;
	// Root element.
	private Element rootElement;
	// List of <img> elements.
	ArrayList<Element> imgElmList = null;
	// Copies of the <img> list prodnote text.
	ArrayList<String> prodCopyList = null;
	// List of images associated with the <img> elements.
	ArrayList<Image> imgFileList = null;
	// The current element we're working on in our list of <img>'s.
	int curElementIndex = -1;
	// The number of img elements we have in this document.
	int numImgElms = 0;
	// Namespace for the document.
	String nameSpace;
	// The index of the furthest node in the tree so far.
	int furthestDocIndex = -1;
	// Current node in whole doc. 
	int curDocIndex = -1;
	// Context/namespace for xpath.
	XPathContext context;
	// xPath - current image nodes.
	Nodes imgs = null;
	// For grabbing the first <img> element.
	boolean grabFirstImage = true;
	// Description for undo.
	String undoDesc;
	String undoAlt;
	
	
	// Image describer context helps us handle element manipulation in a generic way.
	ImageDescriberContext imgContext = new ImageDescriberContext();
	
	///////////////////////////////////////////////////////////////////////////
	// Call ImageDescriber with this Constructor to initialize everything.
	public ImageDescriber(ImageDescriberController dm){
		super(dm);
		
		// Init variables.
		this.dm = dm;
	} // ImageDescriber(DocumentManager docManager)
	
	public ImageDescriber(ImageDescriberController dm, String fileName, Document doc){
		super(dm, doc);
		this.dm = dm;
		
		
		initializeVariables();
	}
	
	@Override
	public boolean startDocument (String completePath, String configFile, String configSettings){
		try {
			if(super.startDocument(completePath, configFile, configSettings)){
				initializeVariables();
				return true;
			}
			else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void initializeVariables(){
		rootElement = doc.getRootElement();
		imgElmList = new ArrayList<Element>();
		prodCopyList = new ArrayList<String>();
		imgFileList = new ArrayList<Image>();
		nameSpace = rootElement.getDocument().getRootElement().getNamespaceURI();
		context = new XPathContext("dtb", nameSpace);
		imgs = doc.getRootElement().query("//dtb:img[1]", context);
		
		// Set the image context.
		if(dm.getArchiver() != null) {
			if(dm.getArchiver().getOrigDocPath().toLowerCase().endsWith(".epub"))
					imgContext.setDocType(ImageDescriberContext.ET_EPUB);
		}
		else if(dm.getWorkingPath() != null)
			if(dm.getWorkingPath().toLowerCase().endsWith(".xml"))
				imgContext.setDocType(ImageDescriberContext.ET_NIMAS);
		
		// Point to root.
		curImgElement = rootElement;
			
		// Fill the image list first.
		fillImgList_XPath();
			
		// Get size of <img> list.
		numImgElms = imgElmList.size();
			
		// Go to first image.
		nextImageElement();
	}
	
	///////////////////////////////////////////////////////////////////////////	
	// Returns the root element.
	public Element getRoot()
	{
		// Return root element.
		return rootElement;
		
	} // Element getRoot()
	
	///////////////////////////////////////////////////////////////////////////
	// Returns the current element...
	public Element currentImageElement()
	{
		// Return the element.
		return curImgElement;
		
	} // Element currentImageElement()
	
	///////////////////////////////////////////////////////////////////////////	
	// Gets the element at the given index.
	public Element getElementAtIndex(int index)
	{
		// Make sure index is within bounds of list.
		if(index >= 0 && index < imgElmList.size())
			return imgElmList.get(index);
		
		// If we make it here, element doesn't exist.
		return null;
		
	} // Element getElementAtIndex(int index)
	
	///////////////////////////////////////////////////////////////////////////
	// Returns the number of <img> elements found in the document.
	public int getNumImgElements()
	{
		// Return number of image elements.
		return numImgElms;
		
	} // int getNumImgElements()
	
	///////////////////////////////////////////////////////////////////////////
	// Recursively moves through xml tree and adds <img> nodes to list.
	// Also builds image path list.
	public void fillImgList(Element e)
	{
		// Is this element an <img>?
		if( e.getLocalName().compareTo("img") == 0 ) {
		
			// Add element to list.
			imgElmList.add(e);
			
			// Add image file path to list.
			
			// Remove slash and dot, if it's there.
			String tempStr = e.getAttributeValue("src");
			if( tempStr.startsWith(".") )
				tempStr = tempStr.substring( BBIni.getFileSep().length() + 1, tempStr.length() );
			
			// Build image path.
			tempStr = dm.getWorkingPath().substring(0, dm.getWorkingPath().lastIndexOf(BBIni.getFileSep())) + BBIni.getFileSep() + tempStr;
			if(tempStr.contains("/") && BBIni.getFileSep().compareTo("/") != 0)
				tempStr = tempStr.replace("/", "\\");
			
			// Add.
			imgFileList.add( new Image(null, tempStr) );
		}
		
		// Get children.
		Elements childElms = e.getChildElements();
		
		// Get their children, and so on.
		for(int curChild = 0; curChild < childElms.size(); curChild++)
			fillImgList( childElms.get(curChild) );
	
	} // FillImgList(Element e)

	///////////////////////////////////////////////////////////////////////////
	// Enumerates <img> elements using xpath, then adds them all to our list.
	public void fillImgList_XPath()
	{
		// Get all <img> elements using xpath.
		Nodes imgTags = null;
		imgTags = getRoot().query("//dtb:img", context);
		
		// For every <img>, add it to our list.
		for(int curTag = 0; curTag < imgTags.size(); curTag++)
		{
			// Add element to list.
			imgElmList.add( (Element)(imgTags.get(curTag)) );
			curImgElement = imgElmList.get(curTag);
			
			// Wrap in container element appropriate for the document type.
			if( imgContext.hasContainer(curImgElement) == false ) {
				curImgElement = imgContext.addContainer(curImgElement);
				imgElmList.set(curTag, curImgElement);
			}
			
			// Add image file path to list.
			
			// Now to build a path to the current image.
			String tempStr = imgElmList.get(imgElmList.size() - 1).getAttributeValue("src");
			
			// Remove dots and slashes at beginning.
			if( tempStr.startsWith(".") && dm.getArchiver() == null)
				tempStr = tempStr.substring( BBIni.getFileSep().length() + 1, tempStr.length() );
			
			// Build image path.
			tempStr = dm.getWorkingPath().substring(0, dm.getWorkingPath().lastIndexOf(BBIni.getFileSep())) + BBIni.getFileSep() + tempStr;
			if(tempStr.contains("/") && BBIni.getFileSep().compareTo("/") != 0)
				tempStr = tempStr.replace("/", "\\");
			
			// Add.
			if( imgElmList.get(curTag).getAttribute("src").getValue().toLowerCase().endsWith(".svg") )
				imgFileList.add( null );
			else	
				imgFileList.add( new Image(null, tempStr) );
			
		
		} // for(int...
	
	} // fillImgList_XPath()
	
	//////////////////////////////////////////////////////////////////////
	// Pass the path to an svg file, and we'll convert it to a jpg. 
	// Returns path to jpg.
	public String convertSVG2JPG(String svgPath)
	{
		//////////////////
		// SVG Conversion.
		
			// If this element uses an svg file, convert to jpeg.
			try {
				// Create a JPEG transcoder
		        JPEGTranscoder t = new JPEGTranscoder();
		        
		        // Set the transcoding hints.
		        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
		                   new Float(.8));
	
		        // Create the transcoder input.
		        String svgURI = new File( svgPath ).toURL().toString();
		        TranscoderInput input = new TranscoderInput(svgURI);
	
		        // Create the transcoder output.
		        OutputStream ostream;
				
		        // Get output stream.
		        String imgSrcStr = svgPath;
		        String outStr = imgSrcStr.substring(0, imgSrcStr.lastIndexOf(".")) + ".jpg";
				ostream = new FileOutputStream(outStr);
		        TranscoderOutput output = new TranscoderOutput(ostream);
	
		        // Save the image.
		        t.transcode(input, output);
	
		        // Flush and close the stream.
		        ostream.flush();
		        ostream.close();
		        
		        // Return path to jpg.
		        return outStr;
			}
			catch (IOException ioe) { ioe.printStackTrace(); }
			catch (TranscoderException te) { te.printStackTrace(); }

		// SVG Conversion.
        //////////////////
		
		// Return null if we couldn't do the conversion for some reason.
		return null;
			
	} // convertSVG2JPG()
	
	///////////////////////////////////////////////////////////////////////////
	// Sets the description and alt text to their previous values.
	public void undoCurDescAndAlt()
	{
		// Set description and alt text.
		setDescription(undoDesc, null, null, null);
		setCurElmImgAttributes(null, null, undoAlt);
		
	} // elementUndo
	
	///////////////////////////////////////////////////////////////////////////
	// Returns the next <img> element that was found in the xml doc.
	public Element nextImageElement()
	{
		// Move to next element.
		curElementIndex++;
		
		// Move to first if we hit the end.
		if(curElementIndex >= numImgElms)
			curElementIndex = 0;
		
		// Make sure there are images.
		if(numImgElms == 0)
			return null;
		
		// Set current element.
		curImgElement = imgElmList.get(curElementIndex);
		
		// Wrap image in an appropriate container.
		if( imgContext.hasContainer(curImgElement) == false ) {
			curImgElement = imgContext.addContainer(curImgElement);
			imgElmList.set(curElementIndex, curImgElement);
		}
		
		// Set undo strings.
		undoDesc = getCurDescription();
		undoAlt = getCurElmAttribute("alt");
		
		// Return current <img> element.
		return curImgElement;
		
	} // NextImageElement()
	
	///////////////////////////////////////////////////////////////////////////
	// Returns the previous <img> element that was found in the xml doc.
	public Element prevImageElement()
	{
		// Make sure there are images.
		if(numImgElms == 0)
			return null;
		
		// Move to previous element, then return it.
		curElementIndex--;
		if(curElementIndex < 0)
			curElementIndex = numImgElms - 1;
		
		// Set current element.
		curImgElement = imgElmList.get(curElementIndex);
		
		// Set undo strings.
		undoDesc = getCurDescription();
		undoAlt = getCurElmAttribute("alt");
		
		// Return current <img> element.
		return curImgElement;
	
	} // PrevImageElement()
	
	///////////////////////////////////////////////////////////////////////////
	// Traverses xml tree until it finds the next <img>.
	public Element getNextImageElement(Element e)
	{
		// Get next image element.
		curDocIndex++;
		if( e.getClass().getName().compareTo("nu.xom.Element") == 0) {
			if( e.getLocalName().compareTo("img") == 0 ) {
				if(curDocIndex > furthestDocIndex)
				{
					// Record depth.
					furthestDocIndex = curDocIndex;
					
					// Return new element found.
					return e;
				
				} // if(curDocIndex > furthestDocIndex)
				
			} // local name == img
			
		} // if xom element
		
		// 
		Element newImgElement = null;
		
		// Go through every child and find the next image.
		for(int curC = 0; curC < e.getChildCount(); curC++)
		{
			if( e.getChild(curC).getClass().getName().compareTo("nu.xom.Element") == 0)
			{
				newImgElement = getNextImageElement( ((Element)(e.getChild(curC))) );
				
				if(newImgElement != null)
					break;
			}
		
		}
		
		return newImgElement;
		
	} // getNextImageElement(Element e)
	
	///////////////////////////////////////////////////////////////////////////
	// Encapsulates given element into <imggroup>, and adds 
	// <prodnote> in the group with it.
	public Element wrapInImgGrp(Element e)
	{
		System.out.println("Called wrapInImgGrp()");
		
		// Create all elements.nameSpace
		Element imgGrpElm = new Element("imggroup", nameSpace);
		Element prodElm = new Element("prodnote", nameSpace);
		Element captElm = new Element("caption", nameSpace);
		Element copyElm = (nu.xom.Element)e.copy();
		
		// If there was no id attribute in the <img> element, add one.
		if(copyElm.getAttribute("id") == null)
			copyElm.addAttribute( new Attribute("id", "TODO!") );
		
		// If the original didn't have an ID value, add one.
		String idValue = copyElm.getAttributeValue("id");
		if(idValue == null)
			idValue = "TODO!";
		
		// Add <prodnote> attributes.
		prodElm.addAttribute( new Attribute("id", "TODO!") );
		prodElm.addAttribute( new Attribute("imgref", idValue) );
		prodElm.addAttribute( new Attribute("render", "required") );
		// Add <caption> attributes.
		captElm.addAttribute( new Attribute("id", "TODO!") );
		captElm.addAttribute( new Attribute("imgref", idValue) );
		
		// Arrange child hierarchy.
		imgGrpElm.insertChild(captElm, 0);
		imgGrpElm.insertChild(prodElm, 0);
		imgGrpElm.insertChild(copyElm, 0);
		
		// Replace given element with this updated one.
		e.getParent().replaceChild(e, imgGrpElm);
		
		// Return newly parented copy of element passed.
		return copyElm;
		
	} // wrapInImgGrp(Element e)
	
	///////////////////////////////////////////////////////////////////////////
	// Returns Image object that element at given index represents.
	public Image getImageFromElmIndex(int elmIndex)
	{
		// Return image.
		if(imgFileList.size() > 0)
			return imgFileList.get(elmIndex);
		
		// Return null if the list is empty.
		return null;
		
	} // getElementImage()
	
	///////////////////////////////////////////////////////////////////////////
	// Returns image of current element.
	public Image getCurElementImage()
	{
		// Return the image.
		if(imgFileList.size() > 0)
		{
			// If this is an svg file, we'll have to do a conversion.
			if( imgElmList.get(curElementIndex).getAttribute("src").getValue().toLowerCase().endsWith(".svg") )
			{
				// Now to build a path to the current image.
				String tempStr = imgElmList.get(imgElmList.size() - 1).getAttributeValue("src");
				
				// Remove dots and slashes at beginning.
				if( tempStr.startsWith(".") && dm.getArchiver() == null)
					tempStr = tempStr.substring( BBIni.getFileSep().length() + 1, tempStr.length() );
				
				// Build image path.
				tempStr = dm.getWorkingPath().substring(0, dm.getWorkingPath().lastIndexOf(BBIni.getFileSep())) + BBIni.getFileSep() + tempStr;
				if(tempStr.contains("/") && BBIni.getFileSep().compareTo("/") != 0)
					tempStr = tempStr.replace("/", "\\");
				
				// Return the svg image.
				return new Image( null, convertSVG2JPG(tempStr) );
			}
			else
				return imgFileList.get(curElementIndex);
			
		} // if svg
		
		// Return null if the list is empty.
		return null;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Returns current element's index in list.
	public int getCurrentElementIndex() {
		
		// Return the index.
		return curElementIndex;
		
	} // getCurrentElementIndex()
	
	///////////////////////////////////////////////////////////////////////////
	// Returns attribute for current element.
	public String getCurElmAttribute(String attName)
	{
		// Return attribute value.
		return imgContext.getAttribute(curImgElement, attName);
		
	} // getCurElmAttribute()
	
	///////////////////////////////////////////////////////////////////////////
	// Sets the current element's <img> attributes. Pass null to atts you 
	// don't want modified.
	public void setCurElmImgAttributes(String tagID, String tagSRC, String tagALT)
	{
		// Set attribute values.
		imgContext.setAttribute(curImgElement, "id", tagID);
		imgContext.setAttribute(curImgElement, "src", tagSRC);
		imgContext.setAttribute(curImgElement, "alt", tagALT);

	} // setCurElmImgAttributes()
	
	///////////////////////////////////////////////////////////////////////////
	// Sets an element's attributes using given index to find it.
	public void setElementAttributesAtIndex(int index, String tagID, String tagSRC, String tagALT)
	{
		// Get parent of <img> element.
		nu.xom.Node parNode = imgElmList.get(index).getParent();
		
		///////////////////////////
				
		// Find <img>.
		Element ch = null;
		for(int curC = 0; curC < parNode.getChildCount(); curC++)
		{
			// Make sure this is an element and not a comment or something crazy.
			if( parNode.getChild(curC).getClass().getName().compareTo("nu.xom.Element") == 0) {
				
				// Get element.
				ch = (Element)parNode.getChild(curC);
				
				// Is this an <img> element?
				if( ch.getLocalName().compareTo("img") == 0 )
				{
					// Set attribute values.
					imgContext.setAttribute(ch, "id", tagID);
					imgContext.setAttribute(ch, "src", tagSRC);
					imgContext.setAttribute(ch, "alt", tagALT);
					
				} // if( ch.getLocalName()...
				
			} // if element and not comment.
		
		} // for(int curC = 0...
			
		
	} // setElementAttributesAtIndex()
	
	///////////////////////////////////////////////////////////////////////////
	// Checks for a description and returns true if it exists for this node.
	// False otherwise.
	// 
	// Notes: Element MUST be a child of an image group of sorts.
	public boolean hasDescElm(Element e)
	{
		return imgContext.hasDescElement(e);
		
	} // hasProdNote()
	
	///////////////////////////////////////////////////////////////////////////
	// Returns the text/description in the current <imggroup>'s prodnote.
	// Returns null if it couldn't find the <prodnote> or if it didn't have 
	// text.
	public String getCurDescription()
	{
		// If there are no images, just return null.
		if(numImgElms < 1)
			return null;
		
		return imgContext.getDescription(curImgElement);
		
	} // getCurProdText()
	
	///////////////////////////////////////////////////////////////////////////
	// Uses given index to reference a particular <img>, and returns its 
	// prodnote text. Returns null if prodnote couldn't be found, or it 
	// contained no text.
	public String getDescAtIndex(int index)
	{
		// If there are no images, just return null.
		if(numImgElms < 1)
			return null;
		
		
		return imgContext.getDescription(imgElmList.get(index));
	
	} // getProdTextAtIndex()
	
	///////////////////////////////////////////////////////////////////////////
	// Sets <prodnote> text and attributes. Uses parent of current <img> element 
	// to get to <prodnote>. Pass null to args you don't want modified.
	public void setDescription(String text, String tagID, String tagIMGREF, String tagRENDER)
	{
		imgContext.setDescription(curImgElement, text);
		imgContext.setAttribute(curImgElement, "id", tagID);
		imgContext.setAttribute(curImgElement, "src", tagIMGREF);
		imgContext.setAttribute(curImgElement, "alt", tagRENDER);
	
	} // setCurElmProdAttributes
	
	///////////////////////////////////////////////////////////////////////////
	// Sets <prodnote> for element at index.
	// 
	// Notes: Must already be wrapped in <imggroup>
	public void setDescAtIndex(int index, String text, String tagID, String tagIMGREF, String tagRENDER)
	{
		imgContext.setDescription(imgElmList.get(index), text);
		imgContext.setAttribute(imgElmList.get(index), "id", tagID);
		imgContext.setAttribute(imgElmList.get(index), "src", tagIMGREF);
		imgContext.setAttribute(imgElmList.get(index), "alt", tagRENDER);
		
	} // setProdAtIndex()
	
	///////////////////////////////////////////////////////////////////////////
	// Removes images from memory.
	public void disposeImages()
	{
		// Loop through them all and delete.
		for(int curImg = 0; curImg < imgFileList.size(); curImg++)
			if(imgFileList.get(curImg) != null)
				imgFileList.get(curImg).dispose();
		
	} // disposeImages()
	
	public void resetCurrentIndex() {
		curElementIndex = -1;
	}
	
	public ArrayList<Element> getImageList() {
		return imgElmList;
	}
	
} // public class ImageDescriber {
