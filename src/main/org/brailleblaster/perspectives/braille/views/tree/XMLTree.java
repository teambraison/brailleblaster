package org.brailleblaster.perspectives.braille.views.tree;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class XMLTree extends TreeView {
	private class TreeItemData {
		Element element;
		ArrayList<TextMapElement>textMapList;
		
		public TreeItemData(Element e){
			this.element = e;
			this.textMapList = new ArrayList<TextMapElement>();
		}
	}
	
	private TreeItem root, previousItem;
	private Menu menu;
	private FocusListener treeFocusListener;
	private SelectionAdapter selectionListener;
	private TraverseListener traverseListener;
	
	public XMLTree(final Manager dm, Group documentWindow){
		super(dm, documentWindow);
		this.menu = new Menu(tree);
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText("Edit Element Style");
		
		item.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				dm.toggleAttributeEditor();	
			}		
		});
		
		this.tree.setMenu(this.menu);	
		this.tree.pack();
	}

	@Override
	public void initializeListeners(){
		tree.addSelectionListener(selectionListener = new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {	
				if(!getLock()){
					TreeItem [] items = tree.getSelection();
					
					if(!items[0].equals(root)){
						if(!previousItem.isDisposed() && !previousItem.equals(root) && !isParent(previousItem, items[0]) && previousItem.getItemCount() > 0){
							depopulateItemChildren(previousItem);
							previousItem.setExpanded(false);
						}
						
						if(items[0].getItemCount() == 0){
							populateItemChildren(items[0], getTreeItemData(items[0]).element);
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
				
					if(getTreeItemData(items[0]).textMapList.size() > 0){
						ArrayList<TextMapElement>list = getList(items[0]);
						TextMapElement temp = list.get(0);
						manager.checkView(temp);
						Message message;
						if(items[0].getText().equals("brl")){
							message = Message.createSetCurrentMessage(Sender.TREE, temp.brailleList.getFirst().start, true);
						}
						else {
							message = Message.createSetCurrentMessage(Sender.TREE, temp.start, false);
						}
						
						cursorOffset = 0;
						manager.dispatch(message);	
					}
				}
			}
		});
		
		this.tree.addFocusListener(treeFocusListener = new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
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
						next = findNextItem(current);
						if(next != null){
							TreeItemData data = getTreeItemData(next);
							tree.setSelection(next);
					
							ArrayList<TextMapElement>list = data.textMapList;
							TextMapElement temp = list.get(0);
							manager.checkView(temp);
							Message message = Message.createSetCurrentMessage(Sender.TREE, temp.start, false);
							manager.dispatch(message);
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
					Message cursorMessage = Message.createUpdateCursorsMessage(Sender.TREE);
					manager.dispatch(cursorMessage);
				}
			}
		});
	
		this.tree.addTraverseListener(traverseListener = new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.stateMask == SWT.MOD1 && e.keyCode == SWT.ARROW_DOWN){
					incrementCurrent();
					Message cursorMessage = Message.createUpdateCursorsMessage(Sender.TREE);
					manager.dispatch(cursorMessage);
					e.doit = false;
				}
				else if(e.stateMask == SWT.MOD1 && e.keyCode == SWT.ARROW_UP){
					decrementCurrent();
					Message cursorMessage = Message.createUpdateCursorsMessage(Sender.TREE);
					manager.dispatch(cursorMessage);
					e.doit = false;
				}
			}
		});
		
		setListenerLock(false);
	}
	
	@Override
	public void removeListeners(){
		tree.removeSelectionListener(selectionListener);
		tree.removeFocusListener(treeFocusListener);
		tree.removeTraverseListener(traverseListener);
	}
	
	private TreeItem newTreeItem(Node e, TreeItem item, int index){
		Element e2 = (Element)e;
		TreeItem temp = new TreeItem(item, 0, index);
		temp.setText(e2.getLocalName());
		return temp;
	}
	
	@Override
	public void newTreeItem(TextMapElement t, int index, int offset){
		Element parentElement = t.parentElement();
		while(parentElement.getAttributeValue("semantics").contains("action")){
			parentElement = (Element)parentElement.getParent();
		}
		
		TreeItem parent = findElementInTree(root, (Element)parentElement.getParent());
		TreeItem newItem = newTreeItem(parentElement, parent, index + offset);
		TreeItemData data = new TreeItemData(parentElement);
		
		if(parentElement.equals(t.parentElement()))
			data.textMapList.add(t);
		
		newItem.setData(data);
	}
	
	public void newTreeItem(ArrayList<TextMapElement>list, int index, int offset){
		Element parentElement = (Element)list.get(0).n.getParent();
		while(parentElement.getAttributeValue("semantics").contains("action")){
			parentElement = (Element)parentElement.getParent();
		}
		
		TreeItem parent = findElementInTree(root, (Element)parentElement.getParent());
		TreeItem newItem = newTreeItem(parentElement, parent, index + offset);
		TreeItemData data = new TreeItemData(parentElement);
		
		for(int i = 0; i < list.size(); i++){
			data.textMapList.add(list.get(i));
		}
		
		newItem.setData(data);
	}
	
	private void populateItemChildren(TreeItem item, Element e){
		ArrayList<Text>textList = new ArrayList<Text>();
	
		Elements els = e.getChildElements();
	
		for(int i = 0; i < els.size(); i++){
			if(!els.get(i).getLocalName().equals("brl") && manager.getDocument().checkAttribute(els.get(i), "semantics") && !els.get(i).getAttributeValue("semantics").contains("skip")){
				TreeItem temp = new TreeItem(item, 0);
				temp.setText(els.get(i).getLocalName());
				TreeItemData data = new TreeItemData(els.get(i));
			
				for(int j = 0; j < els.get(i).getChildCount(); j++){
					if(els.get(i).getChild(j) instanceof Text){
						textList.add((Text)els.get(i).getChild(j));
					}
				}
				if(textList.size() > 0){
					Message message = Message.createGetTextMapElementsMessage(textList, data.textMapList);
					manager.dispatch(message);
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
	
	private boolean hasTreeData(TreeItem item){
		if(getList(item).size() == 0)
			return false;
		
		return true;
	}
	
	@Override
	public void resetView(Group group) {
		setListenerLock(true);
		this.root.setExpanded(false);
		depopulateItemChildren(this.root);
		this.root.dispose();
		this.root = null;
		this.previousItem = null;
		setListenerLock(false);
	}
	
	private ArrayList<TextMapElement> getList(TreeItem item){
		TreeItemData temp = (TreeItemData)item.getData();
		return temp.textMapList;
	}
	
	private TreeItemData getTreeItemData(TreeItem item){
		return (TreeItemData)item.getData();
	}
	
	@Override
	public void setRoot(Element e){
		this.root = new TreeItem(this.tree, 0);
		this.root.setText(e.getLocalName());
		TreeItemData data = new TreeItemData(e);
		this.root.setData(data);
		populateItemChildren(this.root, e);
		previousItem = root;
	}
	
	@Override
	public TreeItem getRoot(){
		return this.tree.getItem(0);
	}
	
	private TreeItem findNextItem(TreeItem item){
		boolean populated = false;
		if(item.getItemCount() == 0){
			populated = true;
			Element el = ((TreeItemData)item.getData()).element;
			populateItemChildren(item, el);
		}
		TreeItem [] items = item.getItems();
		for(int i = 0; i < items.length; i++){
			if(getList(items[i]).size() > 0){
				return items[i]; 
			}
			else {
				Element e = getTreeItemData(items[i]).element;
				populateItemChildren(items[i], e);
				for(int j = 0; j < items[i].getItemCount(); j++){
					TreeItem child = findNextItem(items[i]);
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
	
	private void searchTree(TreeItem item, TextMapElement t, Message m){
		boolean found = false;

		if(t instanceof PageMapElement || t instanceof BrlOnlyMapElement){
			searchTreeForElement(item, t.parentElement(), m);
			if(m.contains("item")){
				found = true;
				item = ((TreeItem)m.getValue("item"));
				if(getTreeItemData(item).textMapList != null){
					ArrayList<TextMapElement>list = getList(item);
					for(int j = 0; j < list.size(); j++){
						if(list.get(j).equals(t)){
							m.put("item",item);
							m.put("treeIndex", j);
							break;
						}
					}
				}
			}
		}
		else if(t.isMathML()) {
			searchTreeForElement(item, (Element)t.n, m);
			if(m.contains("item"))
				found = true;
		}
		else {	
			for(int i = 0; i < item.getItemCount() && !found; i++){
				if(getTreeItemData(item.getItem(i)).textMapList != null){
					ArrayList<TextMapElement>list = getList(item.getItem(i));
					for(int j = 0; j < list.size(); j++){
						if(list.get(j).equals(t)){
							m.put("item",item.getItem(i));
							m.put("treeIndex", j);
							found = true;
							break;
						}
					}
				}
			
				if(!found)
					searchTree(item.getItem(i), t, m);
			}
		}
	}
	
	private void searchTreeForElement(TreeItem item, Element e, Message m){
		boolean found = false;
		
		for(int i = 0; i < item.getItemCount() && !found; i++){
			Element itemElement = getTreeItemData(item.getItem(i)).element;
			
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
	
	private void buildTreeFromElement(Element e){	
		TreeItem item = findElementInTree(this.root, e);
		if(item == null){
			buildTreeFromElement((Element)e.getParent());
			item = findElementInTree(this.root, e);
		}
		
		if(item.getItemCount() == 0){
			populateItemChildren(item, e);
		}
	}
	
	@Override
	public void removeCurrent(){
		TreeItem item = tree.getSelection()[0];
		TreeItemData data = (TreeItemData)item.getData();
		
		if(data.element.getAttributeValue("semantics").contains("action")){
			while(data.element.getAttributeValue("semantics").contains("action")){
				TreeItem temp = item;
				item = item.getParentItem();
				data = getTreeItemData(item);
				//if(temp.getItemCount() == 0)
					temp.dispose();
			}
		}
		//if(item.getItemCount() == 0)
			item.dispose();
	}
	
	@Override
	public void removeMathML(TextMapElement t){
		Message m = new Message(null);
		setListenerLock(true);
		searchTree(this.getRoot(), t, m);
		if((TreeItem)m.getValue("item") != null){
			TreeItem item = (TreeItem)m.getValue("item");
			TreeItem parent = item.getParentItem();
			
			item.dispose();
			if(parent.getItemCount() == 0){
				parent.dispose();
			}
		}
		setListenerLock(false);
	}
	
	@Override
	public int getBlockElementIndex(){
		TreeItem parent = tree.getSelection()[0];
		
		while(!getTreeItemData(parent).element.getAttributeValue("semantics").contains("style")){
			parent = parent.getParentItem();
		}
		
		return parent.getParentItem().indexOf(parent);
	}
	
	@Override
	public void setSelection(TextMapElement t){
		Message message = new Message(null);
		setListenerLock(true);
		searchTree(this.getRoot(), t, message);
		if(message.getValue("item") != null){		
			if(message.contains("item")){
				this.tree.setSelection(((TreeItem)message.getValue("item")));
				if(((TreeItem)message.getValue("item")).getItemCount() == 0)
					populateItemChildren(((TreeItem)message.getValue("item")), (Element)t.n.getParent());
				
				resetTree(((TreeItem)message.getValue("item")));
				previousItem = this.tree.getSelection()[0];
				previousItem.setExpanded(false);
			}
		}
		else {
			Element parent = t.parentElement();
			buildTreeFromElement(parent);
			searchTree(this.getRoot(), t, message);
			this.tree.setSelection(((TreeItem)message.getValue("item")));
			resetTree(((TreeItem)message.getValue("item")));
			previousItem = this.tree.getSelection()[0];
			previousItem.setExpanded(false);
		}
	
		setListenerLock(false);
	}
	
	@Override
	public void removeItem(TextMapElement t, Message m){
		setListenerLock(true);
		searchTree(this.getRoot(), t, m);
		if((TreeItem)m.getValue("item") != null){
			TreeItem item = (TreeItem)m.getValue("item");
			int index = (Integer)m.getValue("treeIndex");
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
	
	@Override
	public TextMapElement getSelection(TextMapElement t){
		setListenerLock(true);
		TreeItem [] arr = this.tree.getSelection();
		if(arr.length > 0 && arr[0].getData() != null){
			ArrayList<TextMapElement>list = getList(arr[0]);
			for(int i = 0; i < list.size(); i++)
				if(list.get(i).equals(t)){
					setListenerLock(false);
					return list.get(i);
				}
		}
		setListenerLock(false);
		return null;
	}
	
	@Override
	public int getSelectionIndex(){
		TreeItem parent = tree.getSelection()[0].getParentItem();
		TreeItem item = tree.getSelection()[0];
			
		TreeItemData data = (TreeItemData)item.getData();
		while(manager.getStyleTable().getSemanticTypeFromAttribute(data.element).equals("action")){
			item = parent;
			parent = parent.getParentItem();
			data = (TreeItemData)item.getData();
		}
				
		return parent.indexOf(item);
	}
	
	@Override
	public void clearTree(){
		tree.removeAll();
	}

	@Override
	public Tree getTree() {
		return tree;
	}

	@Override
	public void split(Message m) {
		int currentIndex = (Integer)m.getValue("currentIndex");
		int treeIndex = (Integer)m.getValue("treeIndex");
		int firstElementIndex = (Integer)m.getValue("firstElementIndex");
		int secondElementIndex = (Integer)m.getValue("secondElementIndex");
		
		removeCurrent();
		addTreeItems(firstElementIndex, currentIndex - 1, treeIndex, 0);
		addTreeItems(secondElementIndex, currentIndex,treeIndex, 1);
	}
	
	private void addTreeItems(int start, int end, int treeIndex, int offset){
		Element parent = manager.getDocument().getParent(manager.getTextMapElement(start).n, true);
		ArrayList<TextMapElement> elementList = new ArrayList<TextMapElement>();
		
		for(int i = start; i < end; i++){
			if(parent.equals(manager.getTextMapElement(i).parentElement())){
				elementList.add(manager.getTextMapElement(i));
			}
		}
		
		if(elementList.size() > 0){
			newTreeItem(elementList, treeIndex, offset);
		}
		else {
			newTreeItem(manager.getTextMapElement(start), treeIndex, offset);
		}
	}
	
	@Override
	public void adjustItemStyle(TextMapElement t) {
		// TODO Auto-generated method stub		
	}
}
