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

import java.util.ArrayList;
import java.lang.StringBuilder;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.XPathContext;

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
	// Copies prodnote text to undo list.
	public void copyMain2UndoList()
	{
		// Clear prodnote text copy list.
		prodCopyList.clear();
		
		// Helps with string copies.
		StringBuilder sb = null;
		
		// Copy current prodnotes into our undo list.
		for(int curItem = 0; curItem < imgElmList.size(); curItem++)
		{
			if(getProdTextAtIndex(curItem) != null)
				sb = new StringBuilder( getProdTextAtIndex(curItem) );
			else
				sb = new StringBuilder( "Error" );
			
			// Add this to string.
			prodCopyList.add( sb.toString() );
		}
		
	} // copyMain2UndoList()
	
	///////////////////////////////////////////////////////////////////////////
	// Copies all old prodnotes to 
	public void copyUndo2MainList()
	{
		// Replace every item changed with the original that was in the DOM.
		for(int curItem = 0; curItem < imgElmList.size(); curItem++) {
			
			// Set prod note.
			setProdAtIndex(curItem, prodCopyList.get(curItem), null, null, null);
			
		} // for(int curItem...
	
	} // copyUndo2MainList()
	
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
			
			// Wrap in <imggroup>
			if( hasImgGrpParent(curImgElement) == false) {
				curImgElement = wrapInImgGrp( curImgElement );
				imgElmList.set(curTag, curImgElement);
				
			} // if( hasImgGrpParent(...
			
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
			imgFileList.add( new Image(null, tempStr) );
		
		} // for(int...
		
		// Copy into our temp list for a potential future undo...
		copyMain2UndoList();
	
	} // fillImgList_XPath()
	
	
	
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
		
		// Wrap in <imggroup>
		if( hasImgGrpParent(curImgElement) == false) {
			curImgElement = wrapInImgGrp( curImgElement );
			imgElmList.set(curElementIndex, curImgElement);
			
		} // if( hasImgGrpParent(...
		
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
		
		// Return current <img> element.
		return curImgElement;
	
	} // PrevImageElement()
	
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
	// Traverses xml tree until it finds the next <img>.
	public Element getNextImageElement(Element e)
	{
	
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
			}
		}
		
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
			return imgFileList.get(curElementIndex);
		
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
	// Sets the current element's <img> attributes. Pass null to atts you 
	// don't want modified.
	public void setCurElmImgAttributes(String tagID, String tagSRC, String tagALT)
	{
		// Set attribute values.
		if(tagID != null)
			curImgElement.getAttribute("id").setValue(tagID);
		if(tagSRC != null)
			curImgElement.getAttribute("src").setValue(tagSRC);
		if(tagALT != null)
			curImgElement.getAttribute("alt").setValue(tagALT);
		
		
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
			// Get current child.
			ch = (Element)parNode.getChild(curC);
			
			// Is this an <img> element?
			if( ch.getLocalName().compareTo("img") == 0 )
			{
				// Set attribute values.
				if(tagID != null)
					ch.getAttribute("id").setValue(tagID);
				if(tagSRC != null)
					ch.getAttribute("src").setValue(tagSRC);
				if(tagALT != null)
					ch.getAttribute("alt").setValue(tagALT);
				
			} // if( ch.getLocalName()...
		
		} // for(int curC = 0...
			
		
	} // setElementAttributesAtIndex()
	
	///////////////////////////////////////////////////////////////////////////
	// Checks for a <prodnote> and returns true if it exists for this node.
	// False otherwise.
	// 
	// Notes: Element MUST be a child of a <imggroup>
	public boolean hasProdNote(Element e)
	{
		// Get parent of <img> element.
		nu.xom.Node parNode = e.getParent();
		
		///////////////////////////
				
		// Find <prodnote>.
		Element ch = null;
		for(int curC = 0; curC < parNode.getChildCount(); curC++) {
			ch = (Element)parNode.getChild(curC);
			if( ch.getLocalName().compareTo("prodnote") == 0 ) {
			
				// Found it. Return true.
				return true;
			
			} // if( ch.getLocalName()...
		
		} // for(int curC = 0...
		
		// Return false if we made it here... no prodnote.
		return false;
		
	} // hasProdNote()
	
	///////////////////////////////////////////////////////////////////////////
	// Returns the text/description in the current <imggroup>'s prodnote.
	// Returns null if it couldn't find the <prodnote> or if it didn't have 
	// text.
	public String getCurProdText()
	{
		// String for <prodnote> text.
		String prodText = "NO PRODNOTE/NO DESCRIPTION.";
		
		// Get parent of <img> element.
		nu.xom.Node parNode = curImgElement.getParent();
		
		///////////////////////////
		
		// Find <prodnote>.
		Element ch = null;
		for(int curC = 0; curC < parNode.getChildCount(); curC++) {
			ch = (Element)parNode.getChild(curC);
			if( ch.getLocalName().compareTo("prodnote") == 0 ) {

				// If no children, add one.
				if(ch.getChildCount() == 0)
					ch.appendChild( new nu.xom.Text("ADD DESCRIPTION!") );
				
				// Get text.
				prodText = ch.getChild(0).getValue();
				
				// Found it. Break.
				break;
				
			} // if( ch.getLocalName()...
			
		} // for(int curC = 0...
		
		// Return <prodnote> text.
		return prodText;
		
	} // getCurProdText()
	
	///////////////////////////////////////////////////////////////////////////
	// Uses given index to reference a particular <img>, and returns its 
	// prodnote text. Returns null if prodnote couldn't be found, or it 
	// contained no text.
	public String getProdTextAtIndex(int index)
	{
		// Get parent of <img> element.
		nu.xom.Node parNode = imgElmList.get(index).getParent();
		
		// String for <prodnote> text.
		String prodText = "NO PRODNOTE/NO DESCRIPTION.";
		
		///////////////////////////
		
		// Find <prodnote>.
		Element ch = null;
		for(int curC = 0; curC < parNode.getChildCount(); curC++) {
			ch = (Element)parNode.getChild(curC);
			if( ch.getLocalName().compareTo("prodnote") == 0 ) {
			
				// If no children, add one.
				if(ch.getChildCount() == 0)
					ch.appendChild( new nu.xom.Text("ADD DESCRIPTION!") );
				
				// Get text.
				prodText = ch.getChild(0).getValue();
				
				// Found it. Break.
				break;
			
			} // if( ch.getLocalName()...
		
		} // for(int curC = 0...
		
		// Return <prodnote> text.
		return prodText;
	
	} // getProdTextAtIndex()
	
	///////////////////////////////////////////////////////////////////////////
	// Sets <prodnote> text and attributes. Uses parent of current <img> element 
	// to get to <prodnote>. Pass null to args you don't want modified.
	public void setCurElmProd(String text, String tagID, String tagIMGREF, String tagRENDER)
	{
		// Get parent of <img> element.
		nu.xom.Node parNode = curImgElement.getParent();
		
		///////////////////////////
		
		// Find <prodnote>.
		Element ch = null;
		int curC = 0;
		for( ; curC < parNode.getChildCount(); curC++) {
			ch = (Element)parNode.getChild(curC);
			if( ch.getLocalName().compareTo("prodnote") == 0 ) {

				// Found it. Break.
				break;
				
			} // if( ch.getLocalName()...
			
		} // for(int curC = 0...
		
		/////////////////////////
		
		// If <prodnote> didn't exist, create it.
		if(curC == parNode.getChildCount())
		{
			// TODO: Create prodnote.
		}
		
		/////////////////////////
		
		// Set text value.
		if(text != null)
		{
			// If no children, add one. Else, replace the one already there.
			if(ch.getChildCount() == 0)
				ch.appendChild( new nu.xom.Text(text) );
			else {
				nu.xom.Node oldNode = ch.getChild(0);
				nu.xom.Node newNode = new nu.xom.Text(text);
				ch.replaceChild(oldNode, newNode);
			}
			
		} // if(text != null)
		
		///////////////////////////
		
		// Set attributes.
		if(tagID != null)
			ch.getAttribute("id").setValue(tagID);
		if(tagIMGREF != null)
			ch.getAttribute("imgref").setValue(tagIMGREF);
		if(tagRENDER != null)
			ch.getAttribute("render").setValue(tagRENDER);
	
	} // setCurElmProdAttributes
	
	///////////////////////////////////////////////////////////////////////////
	// Sets <prodnote> for element at index.
	// 
	// Notes: Must already be wrapped in <imggroup>
	public void setProdAtIndex(int index, String text, String tagID, String tagIMGREF, String tagRENDER)
	{
		// Get parent of element at index.
		// It should be an <imggroup> element.
		Node parNode = imgElmList.get(index).getParent();
		
		// Find <prodnote>.
				Element ch = null;
				int curC = 0;
				for( ; curC < parNode.getChildCount(); curC++) {
					ch = (Element)parNode.getChild(curC);
					if( ch.getLocalName().compareTo("prodnote") == 0 ) {

						// Found it. Break.
						break;
						
					} // if( ch.getLocalName()...
					
				} // for(int curC = 0...
				
				/////////////////////////
				
				// If <prodnote> didn't exist, create it.
				if(curC == parNode.getChildCount())
				{
//					// Create prodnote element.
//					Element prodElm = new Element("prodnote", nameSpace);
//					
//					// Add <prodnote> attributes.
//					prodElm.addAttribute( new Attribute("id", "TODO!") );
//					prodElm.addAttribute( new Attribute("imgref", idValue) );
//					prodElm.addAttribute( new Attribute("render", "required") );
//					
//					// If no children, add one.
//					if(ch.getChildCount() == 0)
//						ch.appendChild( new nu.xom.Text("ADD DESCRIPTION!") );
				}
				
				/////////////////////////
				
				// Set text value.
				if(text != null)
				{
					// If no children, add one. Else, replace the one already there.
					if(ch.getChildCount() == 0)
						ch.appendChild( new nu.xom.Text(text) );
					else {
						nu.xom.Node oldNode = ch.getChild(0);
						nu.xom.Node newNode = new nu.xom.Text(text);
						ch.replaceChild(oldNode, newNode);
					}
					
				} // if(text != null)
				
				///////////////////////////
				
				// Set attributes.
				if(tagID != null)
					ch.getAttribute("id").setValue(tagID);
				if(tagIMGREF != null)
					ch.getAttribute("imgref").setValue(tagIMGREF);
				if(tagRENDER != null)
					ch.getAttribute("render").setValue(tagRENDER);
		
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
	
	public void resetCurrentIndex(){
		curElementIndex = -1;
	}
	
	public ArrayList<Element> getImageList(){
		return imgElmList;
	}
} // public class ImageDescriber {
