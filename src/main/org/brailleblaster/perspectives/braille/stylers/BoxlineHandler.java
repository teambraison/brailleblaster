package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Elements;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.tree.XMLTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;

public class BoxlineHandler {
	Manager manager;
	BrailleDocument document;
	BBSemanticsTable styles;
	BBTree treeView;
	TextView text;
	BrailleView braille;
	MapList list;
	ViewInitializer vi;
	
	public BoxlineHandler(Manager manager, MapList list, ViewInitializer vi){
		this.manager = manager;
		this.document = manager.getDocument();
		this.styles = manager.getStyleTable();
		this.text = manager.getText();
		this.braille = manager.getBraille();
		this.treeView = manager.getTreeView();
		this.list = list;
		this.vi = vi;
	}
	
	/** Wraps a block level element in the appropriate tag then translates and adds boxline brl top and bottom nodes
	 * @param p: parent of text nodes, the block element to be wrapped in a boxline
	 * @param m: message passed to views containing offset positions
	 * @param itemList: arraylist containing text nodes of the block element
	 */
	public void createBoxline(ArrayList<Element>parents, Message m, ArrayList<TextMapElement> itemList){		
		Element wrapper = document.wrapElement(parents, "boxline");
		if(wrapper != null){
			Element boxline = document.translateElement((Element)wrapper.copy());
			int startPos = createTopBoxline(wrapper, m, itemList, boxline, styles.get(styles.getKeyFromAttribute(parents.get(0))));
			createBottomBoxline(wrapper, m, itemList, boxline, startPos, styles.get(styles.getKeyFromAttribute(parents.get(parents.size() - 1))));
			
			int treeIndex;
			if(!treeView.getTree().getSelection()[0].equals(treeView.getRoot()))
				treeIndex = treeView.getTree().getSelection()[0].getParentItem().indexOf(treeView.getTree().getSelection()[0]);
			else
				treeIndex = 0;
			
			//remove items from tree
			if(treeView.getClass().equals(XMLTree.class)){
				for(int i = 0; i < itemList.size(); i++)
					treeView.removeItem(itemList.get(i), new Message(null));
			}
			
			//add aside or sidebar to tree
			treeView.newTreeItem(list.get(startPos), treeIndex, 0);
			
			manager.dispatch(Message.createSetCurrentMessage(Sender.TREE, list.get(list.getCurrentIndex() + 1).start, false));
			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
		}
	}
	
	/** Private helper method for createBoxLine, it handles specifics of the top boxline
	 * @param wrapper: Aside or Sidebar in the DOM which the top boxline will be enclosed
	 * @param m: The message passed containing offset info
	 * @param itemList: the list containing the map elements to be enclosed in the boxline
	 * @param boxline: The translation passed from createBoxline which contains the top boxline element
	 * @param firstStyle: The style of the first element, used to determine where to place the boxline if a line before is suppposed to occur
	 * @return integer representing the index of the top boxline in the maplist
	 */
	private int createTopBoxline(Element wrapper, Message m, ArrayList<TextMapElement>itemList, Element boxline, Styles firstStyle){
		int startPos = list.indexOf(itemList.get(0));
		
		//find start position
		int start, brailleStart;
		if(firstStyle.contains(StylesType.linesBefore)){
			start = (Integer)m.getValue("prev");
			brailleStart = (Integer)m.getValue("braillePrev");
		}
		else {
			start = itemList.get(0).start;
			brailleStart = itemList.get(0).brailleList.getFirst().start;
		}
		
		//insert top boxline
		wrapper.insertChild(boxline.removeChild(0), 0);
		BrlOnlyMapElement b1 =  new BrlOnlyMapElement(wrapper.getChild(0), wrapper);
		b1.setOffsets(start, start + b1.textLength());
		b1.setBrailleOffsets(brailleStart, brailleStart + b1.getText().length());
		vi.addElementToSection(list, b1, startPos);
		
		//set text
		text.insertText(start, list.get(startPos).getText() + "\n");
		braille.insertText(brailleStart, list.get(startPos).brailleList.getFirst().value() + "\n");
		list.shiftOffsetsFromIndex(startPos + 1, list.get(startPos).getText().length() + 1, list.get(startPos).brailleList.getFirst().value().length() + 1, list.get(startPos + 1).start);
		
		return startPos;
	}
	
	/**Private helper method for createBoxLine, it handles specifics of the top boxline
	 * @param wrapper: Aside or Sidebar in the DOM which the top boxline will be enclosed
	 * @param m: The message passed containing offset info
	 * @param itemList: the list containing the map elements to be enclosed in the boxline
	 * @param boxline: The translation passed from createBoxline which contains the bottom boxline element
	 * @param lastStyle: The style of the last element, used to determine where to place the boxline if a line after is suppposed to occur following the lst element
	 * @return: int of the index of the boxline
	 */
	private int createBottomBoxline(Element wrapper, Message m, ArrayList<TextMapElement>itemList, Element boxline, int startPos, Styles lastStyle){
		//find end position
		int endPos = list.indexOf(itemList.get(itemList.size() - 1)) + 1;
		int end, brailleEnd;
		if(lastStyle.contains(StylesType.linesAfter)){
			end = (Integer)m.getValue("next") + list.get(startPos).getText().length() + 1;
			brailleEnd = (Integer)m.getValue("brailleNext") + list.get(startPos).getText().length() + 1;
		}
		else {
			end = list.get(endPos - 1).end;
			brailleEnd = itemList.get(itemList.size() - 1).brailleList.getLast().end;
		}
		
		//insert bottom boxline
		wrapper.appendChild(boxline.removeChild(boxline.getChildCount() - 1));
		BrlOnlyMapElement b2 =  new BrlOnlyMapElement(wrapper.getChild(wrapper.getChildCount() - 1), wrapper);
		b2.setOffsets(end + 1, end + 1 + b2.textLength());
		b2.setBrailleOffsets(brailleEnd + 1, brailleEnd + 1 + b2.getText().length());
		vi.addElementToSection(list, b2, endPos);

		//set text
		text.insertText(end, "\n" + list.get(endPos).getText());
		braille.insertText(brailleEnd, "\n" + list.get(endPos).brailleList.getFirst().value());
		list.shiftOffsetsFromIndex(endPos + 1, list.get(endPos).getText().length() + 1, list.get(endPos).brailleList.getFirst().value().length() + 1, list.get(endPos).start);
			
		return endPos;
	}
	
	/** Removes a boxline from the views and the DOM
	 * @param boxline : Element wrapping content and representing a boxline
	 * @param itemList : List containing opening and closing boxline
	 */
	public void removeBoxline(Element boxline, ArrayList<TextMapElement> itemList){			
		removeTopBoxline((BrlOnlyMapElement)itemList.get(0));
		removeBottomBoxline((BrlOnlyMapElement)itemList.get(1));
		removeBoxLineElement(boxline);
	}
	
	private void removeTopBoxline(BrlOnlyMapElement b){
		int index = list.indexOf(b);
		manager.getText().replaceTextRange(b.start, (b.end + 1) - b.start, "");
		manager.getBraille().replaceTextRange(b.brailleList.getFirst().start, (b.brailleList.getFirst().end + 1) - b.brailleList.getFirst().start, "");
		list.shiftOffsetsFromIndex(index,  -((b.end + 1) - b.start), -((b.brailleList.getFirst().end + 1) - b.brailleList.getFirst().start), 0);
		list.remove(index);
	}
	
	private void removeBottomBoxline(BrlOnlyMapElement b){
		int index = list.indexOf(b);
		manager.getText().replaceTextRange(b.start - 1, b.end - (b.start - 1), "");
		manager.getBraille().replaceTextRange(b.brailleList.getFirst().start - 1, b.brailleList.getFirst().end - (b.brailleList.getFirst().start - 1), "");
		list.shiftOffsetsFromIndex(index,  -(b.end - (b.start - 1)), -(b.brailleList.getFirst().end - (b.brailleList.getFirst().start - 1)), 0);
		list.remove(index);
	}
	
	/** Removes boxline from DOM and re-inserts contents into the DOM
	 * @param boxline : Element wrapping content and representing a boxline
	 */
	private void removeBoxLineElement(Element boxline){
		int index = boxline.getParent().indexOf(boxline);
		Elements els = boxline.getChildElements();
		
		for(int i = 0; i < els.size(); i++){
			if((i == 0 || i == els.size() - 1) && els.get(i).getLocalName().equals("brl"))
				els.get(i).getParent().removeChild(els.get(i));
			else {
				boxline.getParent().insertChild(boxline.removeChild(els.get(i)), index);
				index++;
			}
		}
		
		boxline.getParent().removeChild(boxline);
		if(treeView.getClass().equals(XMLTree.class))
			resetTree();
	}
	
	/** XMLTree needs to construct new structure after the portion of the DOM is re-built
	 * TreeItems are re-positioned in the correct location.  Not necessry for the BookTree
	 * since the boxline is not a headline element
	 */
	private void resetTree(){
		TreeItem item = treeView.getTree().getSelection()[0];
		int index = item.getParentItem().indexOf(item);
		TreeItem [] children = item.getItems();
		
		for(int i = 0; i < children.length; i++){
			TreeItem newItem = new TreeItem(item.getParentItem(), SWT.NONE, index);
			newItem.setData(children[i].getData());
			newItem.setText(children[i].getText());
			index++;
		}
		
		item.dispose();
	}
}
