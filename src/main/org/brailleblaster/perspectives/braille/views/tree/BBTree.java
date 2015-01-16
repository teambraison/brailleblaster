package org.brailleblaster.perspectives.braille.views.tree;

import java.util.ArrayList;

import nu.xom.Element;

import org.brailleblaster.abstractClasses.BBView;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public interface BBTree extends BBView {
	public void setRoot(Element e);
	public TreeItem getRoot();
	public void newTreeItem(TextMapElement t, int index, int offset);
	public void newTreeItem(ArrayList<TextMapElement>list, int index, int offset);
	public void removeCurrent();
	public void removeItem(TextMapElement t, Message m);
	public void removeMathML(TextMapElement t);
	public int getBlockElementIndex();
	public void setSelection(TextMapElement t);
	public TextMapElement getSelection(TextMapElement t);
	public int getSelectionIndex();
	public void clearTree();
	public Tree getTree();
	public void split(Message m);
	public void merge(Message m);
	public void adjustItemStyle(TextMapElement t);
	public void populateItem(Element e);
	public void resetTreeItem(Element e);
	public ArrayList<Integer> getItemPath();
	public void rebuildTree(ArrayList<Integer> indexes);
	public void dispose();
}
