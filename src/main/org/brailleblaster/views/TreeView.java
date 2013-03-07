/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org
  *
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


import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public class TreeView extends AbstractView {
	
	public Tree tree;
	private Control[] tabList;
	
	public TreeView(final DocumentManager dm, Group documentWindow){
		super(documentWindow, 0, 15, 0, 100);
		this.tree = new Tree(view, SWT.NONE);
	
		view.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				tree.setFocus();
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		this.tree.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == SWT.CR){
					dm.changeFocus();
				}
			}
		});	
		
		view.setLayout(new FillLayout());
		
		this.tree.pack();
	}
	
	public void initializeView(){
		
	}
	
	public void populateTree(Document doc){
		TreeItem root = new TreeItem(this.tree, 0);
		root.setText(doc.getRootElement().getLocalName());
			
		Elements rootNode = doc.getRootElement().getChildElements();
		
        for(int i = 0; i < rootNode.size(); i++){
        	Element e = rootNode.get(i);
        	TreeItem temp = new TreeItem(root, 0);
        	temp.setText(e.getLocalName());
        	populateHelper(e, temp);
        }
	}
	
	private void populateHelper(Element e, TreeItem item){
		Elements n = e.getChildElements();

		for(int i = 0; i < n.size(); i++){
			Element e2 = n.get(i);
			TreeItem temp = new TreeItem(item, 0);
        	temp.setText(e2.getLocalName());
        	populateHelper(e2, temp);
		}
	}
	
	public void setRoot(Element e){
		TreeItem root = new TreeItem(this.tree, 0);
		root.setText(e.getLocalName());
	}
	
	public void setTreeItem(Node e, TreeItem item){
		Element e2 = (Element)e;
		TreeItem temp = new TreeItem(item, 0);
    	temp.setText(e2.getLocalName());
	}
	
	public TreeItem getRoot(){
		return this.tree.getItem(0);
	}
	
	public void clearTree(){
		tree.removeAll();
	}
}
