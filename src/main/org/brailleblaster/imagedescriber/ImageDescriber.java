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
	// Namespace for the document.
	String nameSpace;
	
	///////////////////////////////////////////////////////////////////////////
	// Call ImageDescriber with this Constructor to initialize everything.
	public ImageDescriber(DocumentManager docManager){
		
		// Init variables.
		dm = docManager;
		doc = dm.document;
		rootElement = doc.getRootElement();
		imgElmList = new ArrayList<Element>();
		imgFileList = new ArrayList<Image>();
		nameSpace = rootElement.getDocument().getRootElement().getNamespaceURI();

		// Fill list of <img>'s.
		fillImgList(rootElement);
		
		// Get size of <img> list.
		numImgElms = imgElmList.size();
		
		// Only init the current element if there are <img>'s.
		if(numImgElms > 0) {
			curImgElement = imgElmList.get(0);
			curElementIndex = 0;
		}
		
		
		for( int asdf = 0; asdf < numImgElms; asdf++ ) {
			if( hasImgGrpParent(imgElmList.get(asdf)) == false) {
				curImgElement = wrapInImgGrp( imgElmList.get(asdf) );
				imgElmList.set(asdf, curImgElement);
				curElementIndex = asdf;
//				setCurElmProdAttributes("Rubber Chicken", "Rubber Chicken", "Rubber Chicken");
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
	public Element wrapInImgGrp(Element e)
	{
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
		// Set attribute values.
		curImgElement.getAttribute("id").setValue(tagID);
		curImgElement.getAttribute("src").setValue(tagSRC);
		curImgElement.getAttribute("alt").setValue(tagALT);
		
		
	} // setCurElmImgAttributes()
	
	///////////////////////////////////////////////////////////////////////////
	// Sets <prodnote> attributes for current <img> element's parent.
	public void setCurElmProdAttributes(String tagID, String tagIMGREF, String tagRENDER)
	{
		// Get parent of <img> element.
		nu.xom.Node parNode = curImgElement.getParent();
		
		// Find <prodnote>.
		Element ch = null;
		for(int curC = 0; curC < parNode.getChildCount(); curC++) {
			ch = (Element)parNode.getChild(curC);
			if( ch.getLocalName().compareTo("prodnote") == 0 ) {

				// Found it. Break.
				break;
				
			} // if( ch.getLocalName()...
			
		} // for(int curC = 0...
		
		// Set attributes.
		ch.getAttribute("id").setValue(tagID);
		ch.getAttribute("imgref").setValue(tagIMGREF);
		ch.getAttribute("render").setValue(tagRENDER);
	
	} // setCurElmProdAttributes
		
} // public class ImageDescriber {
