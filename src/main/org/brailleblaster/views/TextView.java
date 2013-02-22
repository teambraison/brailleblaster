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

package org.brailleblaster.views;

import nu.xom.Comment;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;

import org.brailleblaster.abstractClasses.AbstractContent;
import org.brailleblaster.abstractClasses.AbstractView;
import org.eclipse.swt.widgets.Group;



public class TextView extends AbstractView {
int total;

public TextView (Group documentWindow) {
// super (documentWindow, 0, 55, 12, 92);
super (documentWindow, 16, 57, 0, 100);
this.total = 0;
}

/* This is a derivative work from 
 * org.eclipse.swt.custom.DefaultContent.java 
*/

public void initializeView(){
	
}

public void setText(Document doc){
	Element e = doc.getRootElement();
	
	for(int i = 0; i < e.getChildCount(); i++){
			this.setTextHelper(e.getChild(i));
	}
}

private void setTextHelper(Node e){
	for(int i = 0; i < e.getChildCount(); i++){
		if(!(e.getChild(i) instanceof Text) && !(e.getChild(i) instanceof Comment)){
			if(!((Element)e.getChild(i)).getLocalName().equals("brl"))
				setTextHelper(e.getChild(i));
		}
		else if(e.getChild(i) instanceof Text){
			view.append(e.getChild(i).getValue() + "\n");
		}
	}
}

private class TextContent extends AbstractContent {
}

}
