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
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.messages.BBEvent;
import org.brailleblaster.messages.Message;
import org.brailleblaster.wordprocessor.DocumentManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public class TreeView extends AbstractView {
	private class TreeItemData{
		Element element;
		ArrayList<TextMapElement>textMapList;
		
		public TreeItemData(Element e){
			this.element = e;
			this.textMapList = new ArrayList<TextMapElement>();
		}
	}
	
	private final static int LEFT_MARGIN = 0;
	private final static int RIGHT_MARGIN = 15;
	private final static int TOP_MARGIN = 0;
	private final static int BOTTOM_MARGIN = 100;
	private final static int ADJUSTED_BOTTOM_MARGIN = 69;
	
	public Tree tree;
	private TreeItem root, previousItem;
	private Menu menu;
	
	private FocusListener treeFocusListener;
	private SelectionListener selectionListener;
	private TraverseListener traverseListener;
	private Group group;
	
	public TreeView(final DocumentManager dm, Group documentWindow){
		super(documentWindow, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		this.group = documentWindow;
		this.tree = new Tree(view, SWT.VIRTUAL | SWT.NONE);
		
		this.menu = new Menu(tree);
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText("Edit Element Style");
		
		item.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dm.toggleAttributeEditor();	
			}		
		});
		
		this.tree.setMenu(this.menu);
		
		view.setLayout(new FillLayout());
		view.getVerticalBar().dispose();
		view.getHorizontalBar().dispose();
		
		view.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				tree.setFocus();
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}
		});		
		
		this.tree.pack();
	}
	
	public void initializeListeners(final DocumentManager dm){
		this.tree.addSelectionListener(selectionListener = new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {	
				if(!getLock()){
					Tree t = (Tree)e.getSource();
					TreeItem [] items = t.getSelection();
					
					if(!items[0].equals(root)){
						if(!previousItem.isDisposed() && !previousItem.equals(root) && !isParent(previousItem, items[0]) && previousItem.getItemCount() > 0){
							depopulateItemChildren(previousItem);
							previousItem.setExpanded(false);
						}
						
						if(items[0].getItemCount() == 0){
							populateItemChildren(items[0], ((TreeItemData)items[0].getData()).element, dm);
							items[0].setExpanded(false);
						}
					
						resetTree(items[0]);
						previousItem = items[0];
					}
					else {
						if(!previousItem.equals(root)){
							if(!previousItem.equals(root) && previousItem.getItemCount() > 0){
								depopulateItemChildren(previousItem);
								previousItem.setExpanded(false);
							}
						}
						items[0].setExpanded(false);
					}
				
					if(((TreeItemData)items[0].getData()).textMapList.size() > 0){
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
		
		this.tree.addFocusListener(treeFocusListener = new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void focusLost(FocusEvent e) {
				setListenerLock(true);
				TreeItem current = tree.getSelection()[0];
				if(current.equals(root)){
					current = root.getItem(0);
				}
				if(!hasTreeData(current)){
					TreeItem parent = current.getParentItem();
					int index = parent.indexOf(current);
					TreeItem next = null;
					while(next == null){
						next = findNextItem(dm, current);
						if(next != null){
							TreeItemData data = (TreeItemData)next.getData();
							tree.setSelection(next);
					
							ArrayList<TextMapElement>list = data.textMapList;
							TextMapElement temp = list.get(0);
							Message message = new Message(BBEvent.SET_CURRENT);
							message.put("sender", "tree");
							message.put("offset", temp.start);
							dm.dispatch(message);
						}
						if(index < parent.getItemCount() - 1){
							index++;
							current = parent.getItem(index);
						}
						else {
							if(parent.equals(root))
								break;
							
							current = parent;
							parent = parent.getParentItem();
						}
					}
				}
				setListenerLock(false);

				
				if(tree.getItemCount() > 0){
					Message cursorMessage = new Message(BBEvent.UPDATE_CURSORS);
					cursorMessage.put("sender", "tree");
					dm.dispatch(cursorMessage);
				}
			}
		});
	
		this.tree.addTraverseListener(traverseListener = new TraverseListener(){
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
		
		setListenerLock(false);
	}
	
	public void removeListeners(){
		tree.removeSelectionListener(selectionListener);
		tree.removeFocusListener(treeFocusListener);
		tree.removeTraverseListener(traverseListener);
	}
	
	private void populateItemChildren(TreeItem item, Element e, DocumentManager dm){
		ArrayList<Text>textList = new ArrayList<Text>();
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			if(!els.get(i).getLocalName().equals("pagenum") && !els.get(i).getLocalName().equals("brl") && !els.get(i).getAttributeValue("semantics").contains("skip")){
				TreeItem temp = new TreeItem(item, 0);
				temp.setText(els.get(i).getLocalName());
				TreeItemData data = new TreeItemData(els.get(i));
			
				for(int j = 0; j < els.get(i).getChildCount(); j++){
					if(els.get(i).getChild(j) instanceof Text){
						textList.add((Text)els.get(i).getChild(j));
					}
				}
				if(textList.size() > 0){
					Message message = new Message(BBEvent.GET_TEXT_MAP_ELEMENTS);
					message.put("nodes", textList);
					message.put("itemList", data.textMapList);
					dm.dispatch(message);
				}
				textList.clear();
				temp.setData(data);
			}
		}
	}
	
	private void depopulateItemChildren(TreeItem item){
			TreeItem [] items = item.getItems();
		
			for(int i = 0; i < items.length; i++){
				items[i].dispose();
			}
	}
	
	public void setRoot(Element e, DocumentManager dm){
		this.root = new TreeItem(this.tree, 0);
		this.root.setText(e.getLocalName());
		TreeItemData data = new TreeItemData(e);
		this.root.setData(data);
		populateItemChildren(this.root, e, dm);
		previousItem = root;
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
	
	public void setSelection(TextMapElement t, Message m, DocumentManager dm){
		setListenerLock(true);
		searchTree(this.getRoot(), t, m);
		if(m.getValue("item") != null){		
			if(m.contains("item")){
				this.tree.setSelection(((TreeItem)m.getValue("item")));
				if(((TreeItem)m.getValue("item")).getItemCount() == 0)
					populateItemChildren(((TreeItem)m.getValue("item")), (Element)t.n.getParent(), dm);
				
				resetTree(((TreeItem)m.getValue("item")));
				previousItem = this.tree.getSelection()[0];
				previousItem.setExpanded(false);
			}
		}
		else {
			Element parent = (Element)t.n.getParent();
			buildTreeFromElement(parent, dm);
			searchTree(this.getRoot(), t, m);
			this.tree.setSelection(((TreeItem)m.getValue("item")));
			resetTree(((TreeItem)m.getValue("item")));
			previousItem = this.tree.getSelection()[0];
			previousItem.setExpanded(false);
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
			if(list.size() == 0 && item.getItemCount() == 0){
				previousItem = item.getParentItem();
				item.dispose();
				
				if(m.contains("element")){
					m.remove("item");
					searchTreeForElement(this.root, (Element)m.getValue("element"), m);
					if((TreeItem)m.getValue("item") != null){
						item = (TreeItem)m.getValue("item");
						previousItem = item.getParentItem();
						item.dispose();
					}
				}
			}
		}
		setListenerLock(false);
	}
	
	private void searchTree(TreeItem item, TextMapElement t, Message m){
		boolean found = false;

		for(int i = 0; i < item.getItemCount() && !found; i++){
			if(((TreeItemData)item.getItem(i).getData()).textMapList != null){
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
	
	private void searchTreeForElement(TreeItem item, Element e, Message m){
		boolean found = false;
		
		for(int i = 0; i < item.getItemCount() && !found; i++){
			Element itemElement = ((TreeItemData)item.getItem(i).getData()).element;
		
			if(itemElement.equals(e)){
				m.put("item", item.getItem(i));
				m.put("itemElement",itemElement);
				found = true;
				break;
			}
			
			if(!found)
				searchTreeForElement(item.getItem(i), e, m);
		}
	}
	
	private TreeItem findElementInTree(TreeItem item, Element e){
		Message message = new Message(null);
		searchTreeForElement(item, e, message);
		if(message.contains("item"))
			return (TreeItem)message.getValue("item");
		else
			return null;
	}
	
	private void buildTreeFromElement(Element e, DocumentManager dm){	
		TreeItem item = findElementInTree(this.root, e);
		if(item == null){
			buildTreeFromElement((Element)e.getParent(), dm);
			item = findElementInTree(this.root, e);
		}
		
		if(item.getItemCount() == 0){
			populateItemChildren(item, e, dm);
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
	
	private ArrayList<TextMapElement> getList(TreeItem item){
		TreeItemData temp = (TreeItemData)item.getData();
		return (ArrayList<TextMapElement>)temp.textMapList;
	}
	
	private void resetTree(TreeItem currentItem){
		setListenerLock(true);
		ArrayList<TreeItem>previousParents = new ArrayList<TreeItem>();
		boolean match = false;
		if(!previousItem.isDisposed() && !previousItem.equals(currentItem)){
			previousParents.add(previousItem);
			TreeItem parent = previousItem.getParentItem();
		
			while(parent != null){
				previousParents.add(parent);
				parent = parent.getParentItem();
			}
		
			parent = currentItem;
			int location = -1;
		
			while(parent != null && !match){
				for(int i = 0; i < previousParents.size(); i++){
					if(previousParents.get(i).equals(parent)){
						match = true;
						location = i - 1;
					}
				}
				parent = parent.getParentItem();
			}
			
			if(match){
				if(location >= 0){
					depopulateItemChildren(previousParents.get(location));	
					previousParents.get(location).setExpanded(false);
				}
			}
		}
		setListenerLock(false);
	}
	
	private boolean isParent(TreeItem parent, TreeItem child){
		if(parent.equals(root))
			return true;
		
		TreeItem nextElement = child.getParentItem();
		while(nextElement != null){
			if(nextElement.equals(parent))
				return true;
			
			nextElement = nextElement.getParentItem();
		}
			
		return false;	
	}
	
	public void clearTree(){
		tree.removeAll();
	}

	private boolean hasTreeData(TreeItem item){
	
		if(getList(item).size() == 0)
			return false;
		
		return true;
	}
	
	private TreeItem findNextItem(DocumentManager dm, TreeItem item){
		boolean populated = false;
		if(item.getItemCount() == 0){
			populated = true;
			Element el = ((TreeItemData)item.getData()).element;
			populateItemChildren(item, el, dm);
		}
		TreeItem [] items = item.getItems();
		for(int i = 0; i < items.length; i++){
			if(getList(items[i]).size() > 0){
				return items[i]; 
			}
			else {
				Element e = ((TreeItemData)items[i].getData()).element;
				populateItemChildren(items[i], e, dm);
				for(int j = 0; j < items[i].getItemCount(); j++){
					TreeItem child = findNextItem(dm, items[i]);
					if(child != null)
						return child;
					else
						depopulateItemChildren(items[i]);
				}
			}
		}
		if(populated){
			depopulateItemChildren(item);
		}
		return null;
	}
	
	@Override
	protected void setViewData(Message message) {
		// TODO Auto-generated method stub
	}
	
	public void resetView(Group group) {
		setListenerLock(true);
		this.root.setExpanded(false);
		depopulateItemChildren(this.root);
		this.root.dispose();
		this.root = null;
		this.previousItem = null;
		setListenerLock(false);
	}
	
	public void adjustLayout(boolean fullSize){
		if(fullSize)
			setLayout(LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		else
			setLayout(LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, ADJUSTED_BOTTOM_MARGIN);
		
		group.layout();
	}
}
