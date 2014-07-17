package org.brailleblaster.perspectives.braille.views.tree;

import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.mapping.elements.Range;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class BookTree extends TreeView {
	private class TreeItemData {
		public LinkedList<TextMapElement>list;
		public int sectionIndex, startRange, endRange;
		public String heading;
		
		public TreeItemData(Element e, int sectionIndex, int startRange){
			list = new LinkedList<TextMapElement>();
			this.sectionIndex = sectionIndex;
			this.startRange = startRange;
			this.endRange = -1;
			setDataList(this, e, sectionIndex, startRange);
			heading = getHeading(e);
		}		
		
		private void setDataList(TreeItemData data, Element e, int section, int start){
			LinkedList<TextMapElement>list = data.list;
			int count = e.getChildCount();
			for(int i = 0; i < count; i++){
				if(e.getChild(i) instanceof Text){
					int index = manager.findNodeIndex(e.getChild(i), section, start);
					if(index != -1)
						list.add(manager.getTextMapElement(section, index));
				}
				else if(e.getChild(i) instanceof Element && !((Element)e.getChild(i)).getLocalName().equals("brl") && table.getSemanticTypeFromAttribute((Element)e.getChild(i)).equals("action"))
					setDataList(data, (Element)e.getChild(i), section, start);
			}
		}
		
		private String getHeading(Element e){
			if(table.getSemanticTypeFromAttribute(e).equals("action"))
				return getHeading((Element)e.getParent());
			
			return table.getKeyFromAttribute(e);
		}
	}
	
	private TreeItem root, previousItem, lastParent;
	private BBSemanticsTable table;
	private SelectionAdapter selectionListener;
	private FocusListener focusListener;
	
	public BookTree(final Manager dm, Group documentWindow){
		super(dm, documentWindow);
		table = dm.getStyleTable();
		tree.pack();
	}
	
	@Override
	public void resetView(Group group) {
		setListenerLock(true);
		root.setExpanded(false);
		root.dispose();
		root = null;
		setListenerLock(false);
	}

	@Override
	public void initializeListeners() {
		tree.addSelectionListener(selectionListener = new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(!getLock() && tree.getSelection()[0].equals(e.item)){
					if(!e.item.equals(root)){
						TreeItemData data = getItemData((TreeItem)e.item);
						manager.checkView(data.list.getFirst());
						Message m = Message.createSetCurrentMessage(Sender.TREE, data.list.getFirst().start, false);
						setCursorOffset(0);
						manager.dispatch(m);
					}
				}
			}
		});
		
		tree.addFocusListener(focusListener = new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {

			}

			@Override
			public void focusLost(FocusEvent e) {
				if(tree.getItemCount() > 0){
					Message cursorMessage = Message.createUpdateCursorsMessage(Sender.TREE);
					manager.dispatch(cursorMessage);
				}
			}		
		});
	}

	@Override
	public void removeListeners() {
		tree.removeSelectionListener(selectionListener);	
		tree.removeFocusListener(focusListener);
	}

	@Override
	public void setRoot(Element e) {
		root = new TreeItem(tree, SWT.LEFT | SWT.BORDER);
		
		if(manager.getDocumentName() != null)
			root.setText(manager.getDocumentName().substring(manager.getDocumentName().lastIndexOf(BBIni.getFileSep()) + 1, manager.getDocumentName().lastIndexOf(".")));
		else
			root.setText("Untitled");
		
		setTree(e, root);
		previousItem = null;
	}
	
	public void setTree(Element e, TreeItem item){	
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			if(table.getKeyFromAttribute(els.get(i)).contains("heading") || table.getKeyFromAttribute(els.get(i)).contains("header")){			
				TreeItem childItem;
				if(previousItem == null){
					childItem = new TreeItem(findCorrectLevel(root, els.get(i)), SWT.LEFT | SWT.BORDER);
				}
				else
					childItem =  new TreeItem(findCorrectLevel(previousItem, els.get(i)), SWT.LEFT | SWT.BORDER);
					
				setNewItemData(childItem, els.get(i));
				setTree(els.get(i), childItem);
			}
			else if(!els.get(i).getLocalName().equals("brl"))
				if(previousItem ==  null)
					setTree(els.get(i), root);
				else
					setTree(els.get(i), previousItem);
		}
	}
	
	private TreeItem findCorrectLevel(TreeItem item, Element e){
		if(!item.equals(root)){
			String level = table.getKeyFromAttribute(e);
			Integer levelValue = Integer.valueOf(level.substring(level.length() - 1));
	
			Integer itemValue = Integer.valueOf(item.getText().substring(item.getText().length() - 2, item.getText().length() - 1));
		
			while(itemValue >= levelValue){
				item = item.getParentItem();
				if(!item.equals(root))
					itemValue = Integer.valueOf(item.getText().substring(item.getText().length() - 2, item.getText().length() - 1));
				else
					break;
			}
		}
		return item;
	}
	
	//Finds text for tree item and sets corresponding data
	private void setNewItemData(TreeItem item, Element e){
		Range range = findIndex(item, e);
		
		if(range != null && range.start != -1){
			item.setData(new TreeItemData(e, range.section, range.start));
			if(previousItem != null){
				if(getItemData(previousItem).startRange > range.start)
					((TreeItemData)previousItem.getData()).endRange = manager.getSectionSize(getItemData(previousItem).sectionIndex) - 1;
				else
					((TreeItemData)previousItem.getData()).endRange = range.start - 1;
			}
			
			item.setText(formatItemText(getItemData(item),e));
		
			previousItem = item;
		}
		else if(item.getItemCount() == 0){
			item.dispose();
		}
	}
	
	private String formatItemText(TreeItemData data, Element e){
		LinkedList<TextMapElement>list = data.list;
		int count = list.size();
		String text = "";
		
		for(int i = 0; i < count; i++){
			text += list.get(i).getText();
		}
		
		text += "(" + data.heading + ")";
		
		return text.trim();
	}
	
	private Range findIndex(TreeItem item, Element e){
		int count = e.getChildCount();
		for(int i = 0; i < count; i++){
			if(e.getChild(i) instanceof Text){
				int sectionIndex, startIndex;
				
				if(previousItem == null){
					sectionIndex = 0;
					startIndex = 0;
				}
				else {
					sectionIndex = getItemData(previousItem).sectionIndex;
					startIndex = getItemData(previousItem).startRange + 1;
				}
				
				//int section = manager.getSection(searchIndex, e.getChild(i));
				return manager.getRange(sectionIndex, startIndex, e.getChild(i));
				//return new Range(section, manager.findNodeIndex(e.getChild(i), section, 0));
			}
			else if(e.getChild(i) instanceof Element && !((Element)e.getChild(i)).getLocalName().equals("brl") &&  !((Element)e.getChild(i)).getLocalName().equals("img") && table.getSemanticTypeFromAttribute((Element)e.getChild(i)).equals("action") && e.getChild(i).getChildCount() != 0)
				return findIndex(item, (Element)e.getChild(i));
		}
		
		return null;
	}
	
	@Override
	public TreeItem getRoot() {
		return root;
	}

	@Override
	public void newTreeItem(TextMapElement t, int index, int offset) {
		if(isHeading(t)){
			TreeItem temp;
			if(lastParent != null)
				temp = new TreeItem(lastParent, SWT.None, index + offset);
			else
				temp = new TreeItem(root, SWT.None, index + offset);
			
			int sectionIndex = getItemData(lastParent).sectionIndex;
			temp.setText(t.getText());
			temp.setData(new TreeItemData(t.parentElement(), sectionIndex, manager.indexOf(t)));
			resetIndexes();
		}
	}

	@Override
	public void removeCurrent() {
		setListenerLock(true);
		TreeItemData data  = getItemData(tree.getSelection()[0]);
		if(data != null && isHeading(data.list.getFirst())){
			lastParent = tree.getSelection()[0].getParentItem();
			tree.getSelection()[0].dispose();
			
			resetIndexes();
		}
		setListenerLock(false);
	}

	@Override
	public void removeItem(TextMapElement t, Message m) {
		setListenerLock(true);
		int index = manager.indexOf(t);
		TreeItem item = findRange(manager.getSection(t), manager.indexOf(t));
		
		if(isHeading(t)){
			LinkedList<TextMapElement> list = getItemData(item).list;
			if(item.getItemCount() > 0 && list.size() == 1)
				copyItemChildren(item);
			
			if(list.size() == 1)
				item.dispose();
			else
				list.remove(t);
		}
		
		updateIndexes(root, index, false);
		if(root.getItemCount() > 0)
			getItemData(getLastItem()).endRange = -1;
		
		setListenerLock(false);
	}
	
	
	private boolean isHeading(TextMapElement t){
		Element parent = t.parentElement();
		
		if(table.getSemanticTypeFromAttribute(parent).equals("action")){
			parent = (Element)t.parentElement().getParent();
			while(!table.getSemanticTypeFromAttribute(parent).equals("style")){
				parent = (Element)parent.getParent();
			}
		}
		
		if(table.getKeyFromAttribute(parent).contains("heading"))
			return true;
		else
			return false;
	}
	
	private void copyItemChildren(TreeItem item){
		TreeItem parent = item.getParentItem();
		int itemIndex = parent.indexOf(item);
		if(itemIndex > 0)
			copyChildrenHelper(item, parent.getItem(itemIndex - 1));
		else
			copyChildrenHelper(item, parent);
	}
	
	private void copyChildrenHelper(TreeItem original, TreeItem newParent){
		int count = original.getItemCount();
		for(int i = 0; i < count; i++){
			TreeItem temp = new TreeItem(newParent, SWT.NONE);
			temp.setText(original.getItem(i).getText());
			temp.setData(original.getItem(i).getData());
			if(original.getItem(i).getItemCount() > 0)
				copyChildrenHelper(original.getItem(i), temp);
		}
	}
	
	private void updateIndexes(TreeItem item, int index, boolean found){
		for(int i = 0; i < item.getItemCount(); i++){
			TreeItemData data = getItemData(item.getItem(i));
			
			if(!found){
				if(data.endRange >= index ||(data.endRange == -1))
					found = true;
			}
			
			if(found){
				if(data.startRange != 0 && data.startRange >= index)	
					data.startRange--;
				
				if(data.endRange != -1 && data.endRange >= index)
					data.endRange--;
				else if(data.endRange == -1 && !item.getItem(i).equals(getLastItem()))
					data.endRange = data.startRange;
			}
			
			if(item.getItem(i).getItemCount() > 0)
				updateIndexes(item.getItem(i), index, found);
		}
	}
	
	private TreeItem getLastItem(){
		TreeItem item = root;
		while(item.getItemCount() > 0){
			item = item.getItem(item.getItemCount() - 1);
		}
		
		if(!item.equals(root))
			return item;
		else
			return null;
	}

	@Override
	public void removeMathML(TextMapElement t) {	
	
	}

	@Override
	public int getBlockElementIndex() {
		return getSelectionIndex();
	}

	@Override
	public void setSelection(TextMapElement t) {		
		setListenerLock(true);
		int section = manager.getSection(t);
		TreeItem item = findRange(section, manager.indexOf(section, t));
		if(item != null){
			tree.setSelection(item);
		}
		else
			tree.setSelection(root);
		
		setListenerLock(false);
	}

	@Override
	public TextMapElement getSelection(TextMapElement t) {
		return t;
	}

	@Override
	public int getSelectionIndex() {
		if(tree.getSelection().length != 0 && !tree.getSelection()[0].equals(root)){
			TreeItem parent = tree.getSelection()[0].getParentItem();
			return parent.indexOf(tree.getSelection()[0]);
		}
		else
			return 0;
	}

	@Override
	public void clearTree() {
		tree.removeAll();	
	}

	@Override
	public Tree getTree() {
		return tree;
	}
	
	private TreeItemData getItemData(TreeItem item){
		return (TreeItemData)item.getData();
	}
	
	private TreeItem findRange(int section, int index){
		return searchTree(root, section, index);
	}
	
	private TreeItem searchTree(TreeItem item, int section, int index){
		TreeItem [] items = item.getItems();
		
		for(int i = 0; i < items.length; i++){
			if(checkItem(items[i], section, index))
				return items[i];
			else if(i < items.length - 1){
				if(section == getSection(items[i]) && index > getEndRange(items[i]) && items[i].getItemCount() > 0){
					TreeItem newItem =  searchTree(items[i], section, index);
					if(newItem != null)
						return newItem;
				}
			}
			else if(i == items.length - 1 && index > getStartRange(items[i]) && items[i].getItemCount() > 0) {
				return searchTree(items[i], section, index);
			}
		}
		
		return null;
	}
	
	private void resetIndexes(){
		resetIndexesHelper(root);
		previousItem = null;
	}
	private void resetIndexesHelper(TreeItem item){
		int count = item.getItemCount();
		for(int i = 0; i < count; i++){
			TreeItemData data = getItemData(item.getItem(i));
			data.startRange = manager.indexOf(data.list.getFirst());
			
			if(previousItem != null){
				if(previousItem != null){
					((TreeItemData)previousItem.getData()).endRange = data.startRange - 1;
				}
			}
			previousItem = item.getItem(i);
			
			if(item.getItem(i).getItemCount() > 0)
				resetIndexesHelper(item.getItem(i));
		}
	}
	
	private int getSection(TreeItem item){
		return ((TreeItemData)item.getData()).sectionIndex;
	}
	
	private int getStartRange(TreeItem item){
		return ((TreeItemData)item.getData()).startRange;
	}
	
	private int getEndRange(TreeItem item){
		return ((TreeItemData)item.getData()).endRange;
	}
	
	private boolean checkItem(TreeItem item, int section, int index){
		TreeItemData data = getItemData(item);
		if((data.sectionIndex == section && index >= data.startRange && index <= data.endRange) || (data.sectionIndex == section && index >= data.startRange && data.endRange == -1))
			return true;
		else
			return false;
	}

	@Override
	public void split(Message m) {
		lastParent = tree.getSelection()[0].getParentItem();
		int section;
		if(lastParent != null)
			section = getItemData(lastParent).sectionIndex;
		else
			section = 0;
		
		TreeItem item = tree.getSelection()[0];
		int firstElementIndex = (Integer)m.getValue("firstElementIndex");
		int secondElementIndex = (Integer)m.getValue("secondElementIndex");
		int treeIndex = (Integer)m.getValue("treeIndex");
		
		if(isHeading(manager.getTextMapElement(firstElementIndex)))
			newTreeItem(manager.getTextMapElement(firstElementIndex), treeIndex, 0);
		
		if(isHeading(manager.getTextMapElement(secondElementIndex))){
			if(!item.equals(root)){
				item.setText(manager.getTextMapElement(secondElementIndex).getText());
				item.setData(new TreeItemData(manager.getTextMapElement(secondElementIndex).parentElement(), section, manager.indexOf(manager.getTextMapElement(secondElementIndex))));
			}
			else {
				newTreeItem(manager.getTextMapElement(secondElementIndex), treeIndex, 1);
			}
		}
		
		resetIndexes();		
	}

	@Override
	public void adjustItemStyle(TextMapElement t) {
		if(isHeading(t)){
			tree.removeAll();
			setRoot(manager.getDocument().getRootElement());
			TreeItem item = findRange(manager.getSection(t), manager.indexOf(t));
			
			tree.setSelection(item);
			
			item = item.getParentItem();
			while(item != null){
				item.setExpanded(true);
				item = item.getParentItem();
			}
		}
	}
}
