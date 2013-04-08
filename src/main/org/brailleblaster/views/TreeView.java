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


import nu.xom.Element;
import nu.xom.Node;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.wordprocessor.BBEvent;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.brailleblaster.wordprocessor.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public class TreeView extends AbstractView {
	
	public Tree tree;
	
	public TreeView(final DocumentManager dm, Group documentWindow){
		super(documentWindow, 0, 15, 0, 100);
		this.tree = new Tree(view, SWT.NONE);
	
		view.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				tree.setFocus();
			}
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub	
			}
		});	
		
		this.tree.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void widgetSelected(SelectionEvent e) {				
				Tree t = (Tree)e.getSource();
				TreeItem [] items = t.getSelection();

				if(items[0].getData() != null){
					TextMapElement temp = (TextMapElement)items[0].getData();
					Message message = new Message(BBEvent.SET_CURRENT);
					message.put("offset", temp.start);
					dm.dispatch(message);
				}
			}
		});
		
		view.setLayout(new FillLayout());
		
		this.tree.pack();
	}
	
	public void setRoot(Element e){
		TreeItem root = new TreeItem(this.tree, 0);
		root.setText(e.getLocalName());
	}
	
	public TreeItem getRoot(){
		return this.tree.getItem(0);
	}
	
	public TreeItem newTreeItem(Node e, TreeItem item){
		Element e2 = (Element)e;
		TreeItem temp = new TreeItem(item, 0);
    	temp.setText(e2.getLocalName());
    	return temp;
	}
	
	public void setItemData(TreeItem item, Node n){
		item.setData(n);
	}
	
	public void setSelection(TextMapElement t, Message m){
		searchTree(this.getRoot(), t, m);
		if(m.getValue("item") != null){		
			this.tree.setSelection(((TreeItem)m.getValue("item")));
		}
	}
	
	public void removeItem(TextMapElement t, Message m){
		searchTree(this.getRoot(), t, m);
		if((TreeItem)m.getValue("item") != null)
			((TreeItem)m.getValue("item")).dispose();
	}
	
	private void searchTree(TreeItem item, TextMapElement t, Message m){
		for(int i = 0; i < item.getItemCount(); i++){
			if(item.getItem(i).getData() != null && item.getItem(i).getData().equals(t)){
				m.put("item",item.getItem(i));
				break;
			}
			else{
				searchTree(item.getItem(i), t, m);
			}
		}
	}
	
	public TextMapElement getSelection(){
		TreeItem [] arr = this.tree.getSelection();
		if(arr.length > 0 && arr[0].getData() != null)
			return (TextMapElement)arr[0].getData();
		else
			return null;
	}
	
	public void clearTree(){
		tree.removeAll();
	}
}
