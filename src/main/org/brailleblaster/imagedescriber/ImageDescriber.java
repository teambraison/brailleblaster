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

package org.brailleblaster.imagedescriber;

import java.util.ArrayList;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.eclipse.swt.graphics.Image;

public class ImageDescriber {

	// Manages document.
	DocumentManager dm;
	// The document with images we want to add descriptions to.
	private BBDocument doc;
	// Current image element.
	private Element curImgElement;
	// Root element.
	private Element rootElement;
	// List of <img> elements.
	ArrayList<Element> imgElmList = null;
	// List of images associated with the <img> elements.
	ArrayList<Image> imgFileList = null;
	// The current element we're working on.
	int curElementIndex = -1;
	// The number of img elements we have in this document.
	int numImgElms = 0;
	
	///////////////////////////////////////////////////////////////////////////
	// Call ImageDescriber with this Constructor to initialize everything.
	public ImageDescriber(DocumentManager docManager){
		
		// Init variables.
		dm = docManager;
		doc = dm.document;
		rootElement = doc.getRootElement();
		curImgElement = rootElement;
		imgElmList = new ArrayList<Element>();
		imgFileList = new ArrayList<Image>();
		curElementIndex = -1;

		// Fill list of <img>'s.
		fillImgList(rootElement);
		
		// Get size of <img> list.
		numImgElms = imgElmList.size();
		
		// Only init the current element if there are <img>'s.
		if(numImgElms > 0)
			curElementIndex = 0;
		
		for( int asdf = 0; asdf < numImgElms; asdf++ ) {
			curImgElement = imgElmList.get(asdf);
			curElementIndex = asdf;
			if( hasImgGrpParent(imgElmList.get(asdf)) == false) {
				wrapInImgGrp(imgElmList.get(asdf));
//				setCurElmImgAttributes("Rubber Chicken", "Rubber Chicken", "Rubber Chicken");
			}
			
		} // for()
		
	} // ImageDescriber(DocumentManager docManager)
	
	///////////////////////////////////////////////////////////////////////////
	// Returns the next <img> element that was found in the xml doc.
	public Element nextImageElement()
	{
		// Make sure there are images.
		if(numImgElms == 0)
			return null;
		
		// Move to next element, then return it.
		curElementIndex++;
		if(curElementIndex >= numImgElms)
			curElementIndex = 0;
		
		// Set current element.
		curImgElement = imgElmList.get(curElementIndex);
		
		// Return current <img> element.
		return imgElmList.get(curElementIndex);
		
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
		
		// Return current <img> element.
		return imgElmList.get(curElementIndex);
	
	} // PrevImageElement()
	
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
				tempStr = tempStr.substring( 2, tempStr.length() );
			
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
	// Returns true if the current image has an <imggroup> parent.
	// False otherwise.
	public boolean hasImgGrpParent(Element e)
	{
		// If the parent is <imggroup>, return true.
		if( ((nu.xom.Element)(e.getParent())).getLocalName().compareTo("imggroup") == 0 )
			return true;
		else
			return false;
		
	} // HasImgGrpParent(Element e)
	
	///////////////////////////////////////////////////////////////////////////
	// Encapsulates given element into <imggroup>, and adds 
	// <prodnote> in the group with it.
	public void wrapInImgGrp(Element e)
	{
		// Create all elements.
		String ns = e.getDocument().getRootElement().getNamespaceURI();
		Element imgGrpElm = new Element("imggroup", ns);
		Element prodElm = new Element("prodnote", ns);
		Element captElm = new Element("caption", ns);
		Element copyElm = (nu.xom.Element)e.copy();
		
		// Add <prodnote> attributes.
		prodElm.addAttribute( new Attribute("id", "TODO!") );
		prodElm.addAttribute( new Attribute("imgref", copyElm.getAttributeValue("id")) );
		prodElm.addAttribute( new Attribute("render", "required") );
		// Add <caption> attributes.
		captElm.addAttribute( new Attribute("id", "TODO!") );
		captElm.addAttribute( new Attribute("imgref", copyElm.getAttributeValue("id")) );
		
		// Arrange child hierarchy.
		imgGrpElm.appendChild(copyElm);
		imgGrpElm.appendChild(captElm);
		imgGrpElm.appendChild(prodElm);
		
		// Replace given element with this updated one.
		e.getParent().replaceChild(e, imgGrpElm);
		
	} // wrapInImgGrp(Element e)
	
	///////////////////////////////////////////////////////////////////////////
	// Returns Image object that element at given index represents.
	public Image getImageFromElmIndex(int elmIndex)
	{
		// Return image.
		return imgFileList.get(elmIndex);
		
	} // getElementImage()
	
	///////////////////////////////////////////////////////////////////////////
	// Returns current element's index in list.
	public int getCurrentElementIndex() {
		
		// Return the index.
		return curElementIndex;
		
	} // getCurrentElementIndex()
	
	///////////////////////////////////////////////////////////////////////////
	// Sets the current element's <img> attributes.
	public void setCurElmImgAttributes(String tagID, String tagSRC, String tagALT)
	{
		// Find <img> and change its attributes.
		int childCount = curImgElement.getParent().getChildCount();
		for(int curElm = 0; curElm < childCount; curElm++)
		{
			// Get <img> element from <imggroup>.
			Element child = (Element)((curImgElement.getParent())).getChild(0);
			
			// If this is the <img> element, change its attributes.
			if( child.getLocalName().compareTo("img") == 0 )
			{
				// Set attributes.
				child.getAttribute("id").setValue(tagID);
				child.getAttribute("src").setValue(tagSRC);
				child.getAttribute("alt").setValue(tagALT);
				
				// Found it. Stop searching.
				break;
				
			} // if( child.getLocalName()...
			
		} // for(int curElm...
		
	} // setCurElmImgAttributes()
	
	///////////////////////////////////////////////////////////////////////////
	// Sets the current element's <prodnote> attributes.
	public void setCurElmProdAttributes(String tagID, String tagIMGREF, String tagRENDER)
	{
		// Set attributes.
		curImgElement.getAttribute("id").setValue(tagID);
		curImgElement.getAttribute("imgref").setValue(tagIMGREF);
		curImgElement.getAttribute("render").setValue(tagRENDER);
	
	} // setCurElmProdAttributes
		
} // public class ImageDescriber {
