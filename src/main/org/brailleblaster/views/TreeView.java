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


import java.util.ArrayList;

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
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
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
			
			}
		});	
		
		this.tree.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void widgetSelected(SelectionEvent e) {	
				if(!getLock()){
					Tree t = (Tree)e.getSource();
					TreeItem [] items = t.getSelection();

					if(items[0].getData() != null){
						ArrayList<TextMapElement>list = getList(items[0]);
						TextMapElement temp = list.get(0);
						Message message = new Message(BBEvent.SET_CURRENT);
						message.put("sender", "tree");
						message.put("offset", temp.start);
						if(items[0].getText().equals("brl")){
							message.put("isBraille", true);
							message.put("offset", temp.brailleList.getFirst().start);
						}
						cursorOffset = 0;
						dm.dispatch(message);						
					}
				}
			}
		});
		
		this.tree.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(tree.getItemCount() > 0){
					Message cursorMessage = new Message(BBEvent.UPDATE_CURSORS);
					cursorMessage.put("sender", "tree");
					dm.dispatch(cursorMessage);
				}
			}
		});
		
		this.tree.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.stateMask == SWT.MOD1 && e.keyCode == SWT.ARROW_DOWN){
					sendIncrementCurrent(dm);
					Message cursorMessage = new Message(BBEvent.UPDATE_CURSORS);
					cursorMessage.put("sender", "tree");
					dm.dispatch(cursorMessage);
					e.doit = false;
				}
				else if(e.stateMask == SWT.MOD1 && e.keyCode == SWT.ARROW_UP){
					sendDecrementCurrent(dm);
					Message cursorMessage = new Message(BBEvent.UPDATE_CURSORS);
					cursorMessage.put("sender", "tree");
					dm.dispatch(cursorMessage);
					e.doit = false;
				}
			}
		});
		
		view.setLayout(new FillLayout());
		
		this.tree.pack();
		setListenerLock(false);
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
	
	public void setItemData(TreeItem item, TextMapElement t){
		if(item.getData() == null){
			item.setData(new ArrayList<TextMapElement>());
		}
		
		getList(item).add(t);
	}
	
	public void setSelection(TextMapElement t, Message m){
		setListenerLock(true);
		searchTree(this.getRoot(), t, m);
		if(m.getValue("item") != null){		
			this.tree.setSelection(((TreeItem)m.getValue("item")));
		}
		setListenerLock(false);
	}
	
	public void removeItem(TextMapElement t, Message m){
		setListenerLock(true);
		searchTree(this.getRoot(), t, m);
		if((TreeItem)m.getValue("item") != null){
			TreeItem item = (TreeItem)m.getValue("item");
			int index = (Integer)m.getValue("index");
			ArrayList<TextMapElement> list = getList(item);
			list.remove(index);
			if(list.size() == 0){
				item.dispose();
			}
		}
		setListenerLock(false);
	}
	
	private void searchTree(TreeItem item, TextMapElement t, Message m){
		boolean found = false;
		
		for(int i = 0; i < item.getItemCount() && !found; i++){
			if(item.getItem(i).getData() != null){
				ArrayList<TextMapElement>list = getList(item.getItem(i));
				for(int j = 0; j < list.size(); j++){
					if(list.get(j).equals(t)){
						m.put("item",item.getItem(i));
						m.put("index", j);
						found = true;
						break;
					}
				}
			}
			
			if(!found)
				searchTree(item.getItem(i), t, m);
		}
	}
	
	public TextMapElement getSelection(TextMapElement t){
		setListenerLock(true);
		TreeItem [] arr = this.tree.getSelection();
		if(arr.length > 0 && arr[0].getData() != null){
			ArrayList<TextMapElement>list = getList(arr[0]);
			for(int i = 0; i < list.size(); i++)
				if(list.get(i).equals(t)){
					setListenerLock(false);
					return (TextMapElement)list.get(i);
				}
		}
		setListenerLock(false);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<TextMapElement> getList(TreeItem item){
		return (ArrayList<TextMapElement>)item.getData();
	}
	
	public void clearTree(){
		tree.removeAll();
	}

	@Override
	protected void setViewData(Message message) {
		// TODO Auto-generated method stub
	}
}
