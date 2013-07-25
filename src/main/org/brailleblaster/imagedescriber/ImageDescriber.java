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

import nu.xom.Element;

import org.brailleblaster.document.BBDocument;

public class ImageDescriber {

	// The document with images we want to add descriptions to.
	private BBDocument doc;
	// Current image element.
	private Element curImgElement;
	// Root element.
	private Element rootElement;
	// List of <img> elements.
	ArrayList<Element> imgList = null;
	// The current element we're working on.
	int curElement = -1;
	
	///////////////////////////////////////////////////////////////////////////
	// Call ImageDescriber with this Constructor to initialize everything.
	public ImageDescriber(BBDocument document){
		
		// Init variables.
		doc = document;
		rootElement = doc.getRootElement();
		curImgElement = rootElement;
		imgList = new ArrayList<Element>();
		curElement = -1;

		// Fill list of <img>'s.
		FillImgList(rootElement);
		
	} // ImageDescriber(BBDocument document)
	
	///////////////////////////////////////////////////////////////////////////
	// Searches forward in the xml tree for an element named <img>
	public Element NextImageElement()
	{
		return null;
		
	} // NextImageElement()
	
	///////////////////////////////////////////////////////////////////////////
	// Searches backward in the xml tree for an element named <img>
	public Element PrevImageElement()
	{
		return null;
	
	} // PrevImageElement()
	
	///////////////////////////////////////////////////////////////////////////
	// Recursively moves through xml tree and adds <img> nodes to list.
	public void FillImgList(Element e)
	{
		// Is this element an <img>?
		
		String elementName = e.getLocalName();
		
	} // FillImgList(Element e)
		
} // public class ImageDescriber {
