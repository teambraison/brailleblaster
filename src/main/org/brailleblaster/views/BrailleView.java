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
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by John J. Boyer john.boyer@abilitiessoft.com
 */

package org.brailleblaster.views;

import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractContent;
import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.mapping.BrailleMapElement;
import org.brailleblaster.mapping.TextMapElement;
import org.eclipse.swt.widgets.Group;

public class BrailleView extends AbstractView {
	public int total;
	
	public BrailleView(Group documentWindow) {
		// super (documentWindow, 56, 100, 12, 92);
		super(documentWindow, 58, 100, 0, 100);
		this.total = 0;
	}

	/*
	 * This is a derivative work from org.eclipse.swt.custom.DefaultContent.java
	 */
	public void initializeView() {

	}
	
	public void setBraille(Node n, TextMapElement t){
		view.append(n.getValue() + "\n");
		t.brailleList.add(new BrailleMapElement(this.total, n));
		this.total += n.getValue().length() + 1;
	}
	
	public void updateBraille(TextMapElement t, int total){
		String insertionString = "";
		for(int i = 0; i < t.brailleList.size(); i++){
			insertionString += t.brailleList.get(i).n.getValue() + "\n";
		}	
		view.replaceTextRange(t.brailleList.getFirst().offset, total, insertionString);
	}
	
	public void setCursor(int offset){
		view.setFocus();
		view.setCaretOffset(offset);
	}
	
	public void removeWhitespace(int offset){
		view.replaceTextRange(offset, 1, "");
	}
	
	class BrailleContent extends AbstractContent {
	}
}
