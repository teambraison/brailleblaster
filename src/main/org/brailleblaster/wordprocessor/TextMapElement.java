package org.brailleblaster.wordprocessor;

import java.util.LinkedList;

import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractMapElement;
import org.eclipse.swt.widgets.TreeItem;

public class TextMapElement extends AbstractMapElement<BrailleMapElement>{
	TreeItem item;
	
	public TextMapElement(int offset, Node n, TreeItem item) {
		super(offset, n);
		this.item = item;
		this.list = new LinkedList<BrailleMapElement>();		
	}
}
