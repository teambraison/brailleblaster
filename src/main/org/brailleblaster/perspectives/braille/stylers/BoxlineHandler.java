package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Text;

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
			ArrayList<Element>sidebarList = findBoxlines(wrapper);
			if(sidebarList.size() > 1)
				createMultipleBoxlines(sidebarList, wrapper, parents, itemList);
			else 
				createFullBoxline(wrapper, parents, m, itemList);
			
			manager.dispatch(Message.createSetCurrentMessage(Sender.TREE, list.get(list.getCurrentIndex() + 1).start, false));
			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
		}
	}
	
	private void createMultipleBoxlines(ArrayList<Element> elList, Element wrapper, ArrayList<Element>parents, ArrayList<TextMapElement> itemList){			
		for(int i = 0; i < elList.size(); i++){
			if(i == 0)
				setStyle(elList.get(i), "topBox");
			else if(i == elList.size() - 1)
				setStyle(elList.get(i), "bottomBox");
			else
				setStyle(elList.get(i), "middleBox");
		}
			
		Document doc = document.translateElements(elList);
		Element parent = (Element)doc.getChild(0);
		
		String style = getStyle(wrapper);
		Message m = new Message(null);
		if(style.equals("topBox"))
			createFullBoxline(wrapper, parents, m, itemList);
		else if(style.equals("bottomBox") || style.equals("middleBox"))
			createHalfBox(wrapper, m, itemList, parents);
		
		int index = elList.indexOf(wrapper);
		elList.remove(index);
		parent.removeChild(index);
		
		resetSidebars(elList, parent);
	}
	
	private void createFullBoxline(Element wrapper, ArrayList<Element>parents,  Message m, ArrayList<TextMapElement> itemList){
		Element boxline = document.translateElement((Element)wrapper.copy());
		int startPos = createTopLine(wrapper, m, itemList, boxline, styles.get(styles.getKeyFromAttribute(parents.get(0))));
		int endPos = createBottomLine(wrapper, m, itemList, boxline, startPos, styles.get(styles.getKeyFromAttribute(parents.get(parents.size() - 1))));
	
		int treeIndex;
		if(!treeView.getTree().getSelection()[0].equals(treeView.getRoot()))
			treeIndex = treeView.getTree().getSelection()[0].getParentItem().indexOf(treeView.getTree().getSelection()[0]);
		else
			treeIndex = 0;
	
		//remove items from tree
		removeTreeItems(itemList);
	
		ArrayList<TextMapElement> treeItemData = new ArrayList<TextMapElement>();
		treeItemData.add(list.get(startPos));
		treeItemData.add(list.get(endPos));
		//add aside or sidebar to tree
		treeView.newTreeItem(treeItemData, treeIndex, 0);
	}
	
	private void createHalfBox(Element wrapper, Message m, ArrayList<TextMapElement>itemList,ArrayList<Element>parents){		
		Element boxline = document.translateElement((Element)wrapper.copy());
		int endPos = createBottomLine(wrapper, m, itemList, boxline, list.indexOf(itemList.get(itemList.size() - 1)), styles.get(styles.getKeyFromAttribute(parents.get(parents.size() - 1))));
		
		int treeIndex;
		if(!treeView.getTree().getSelection()[0].equals(treeView.getRoot()))
			treeIndex = treeView.getTree().getSelection()[0].getParentItem().indexOf(treeView.getTree().getSelection()[0]);
		else
			treeIndex = 0;
		
		//remove items from tree
		removeTreeItems(itemList);
		
		ArrayList<TextMapElement> treeItemData = new ArrayList<TextMapElement>();
		treeItemData.add(list.get(endPos));
		//add aside or sidebar to tree
		treeView.newTreeItem(treeItemData, treeIndex, 0);
	}
	
	/** Private helper method that handles specifics of creating map element and adding the top line in the view
	 * @param wrapper: Aside or Sidebar in the DOM which the top boxline will be enclosed
	 * @param m: The message passed containing offset info
	 * @param itemList: the list containing the map elements to be enclosed in the boxline
	 * @param boxline: The translation passed from createBoxline which contains the top boxline element
	 * @param firstStyle: The style of the first element, used to determine where to place the boxline if a line before is suppposed to occur
	 * @return integer representing the index of the top boxline in the maplist
	 */
	private int createTopLine(Element wrapper, Message m, ArrayList<TextMapElement>itemList, Element boxline, Styles firstStyle){
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
	
	/**Private helper method that handles specifics of creating map element and adding the bottom line in the view
	 * @param wrapper: Aside or Sidebar in the DOM which the top boxline will be enclosed
	 * @param m: The message passed containing offset info
	 * @param itemList: the list containing the map elements to be enclosed in the boxline
	 * @param boxline: The translation passed from createBoxline which contains the bottom boxline element
	 * @param lastStyle: The style of the last element, used to determine where to place the boxline if a line after is supposed to occur following the lst element
	 * @return: int of the index of the boxline
	 */
	private int createBottomLine(Element wrapper, Message m, ArrayList<TextMapElement>itemList, Element boxline, int startPos, Styles lastStyle){
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
	
	private void resetSidebars(ArrayList<Element> elList, Element parent){
		while(elList.size() > 0){
			if(getStyle(parent.getChildElements().get(0)).equals("topBox") || getStyle(parent.getChildElements().get(0)).equals("boxline"))
				changeToFullBox(elList.get(0), parent.getChildElements().get(0));
			else if(getStyle(parent.getChildElements().get(0)).equals("bottomBox") || getStyle(parent.getChildElements().get(0)).equals("middleBox"))
				changeToHalfBox(elList.get(0), parent.getChildElements().get(0));
			
			setStyle(elList.get(0), getStyle(parent.getChildElements().get(0)));
			parent.removeChild(0);
			elList.remove(0);
		}
	}
	
	/** Modifies full or top box styles which have both top and bottom lines
	 * @param box : Element to be modified
	 * @param replacement : New translation element
	 */
	private void changeToFullBox(Element box, Element replacement){
		//set top
		if(box.getChild(0) instanceof Element && ((Element)box.getChild(0)).getLocalName().equals("brl")){
			replaceBoxLine((Element)box.getChild(0), (Element)replacement.getChild(0));
		}
		else if(box.getChild(0) instanceof Element && !((Element)box.getChild(0)).getLocalName().equals("brl")){
			Text t = findText(box.getChild(0));
			if(t != null)
				insertBoxLine(list.findNodeIndex(t, 0), (Element)box, (Element)replacement.removeChild(0));
		}
		
		//set bottom
		if(box.getChild(box.getChildCount() - 1) instanceof Element &&  ((Element)box.getChild(0)).getLocalName().equals("brl"))
			replaceBoxLine((Element)box.getChild(box.getChildCount() - 1), (Element)replacement.getChild(replacement.getChildCount() - 1));
	}
	
	/** Modifies middle or bottom box styles which have only a lower boxline
	 * @param box : Element to be modified
	 * @param replacement : New translation element
	 */
	private void changeToHalfBox(Element box, Element replacement){
		if(box.getChild(0) instanceof Element && ((Element)box.getChild(0)).getLocalName().equals("brl")){
			int index = list.findNodeIndex(box.getChild(0), 0);
			BrlOnlyMapElement b = (BrlOnlyMapElement)list.get(index);
			removeTopBoxline(b);
			b.parentElement().removeChild(b.n);
		}
		
		if(box.getChild(box.getChildCount() - 1) instanceof Element &&  ((Element)box.getChild(box.getChildCount() - 1)).getLocalName().equals("brl"))
			replaceBoxLine((Element)box.getChild(box.getChildCount() - 1), (Element)replacement.getChild(replacement.getChildCount() - 1));
	}
	
	private void insertBoxLine(int index, Element box, Element brl){
		String style = getStyle(list.get(index).parentElement());
		Styles firstStyle = styles.get(style);
		//inserted in DOM
		box.insertChild(brl, 0);
		
		Message m = new Message(null);
		//find start position
		int start, brailleStart;
		if(firstStyle.contains(StylesType.linesBefore)){
			start = (Integer)m.getValue("prev");
			brailleStart = (Integer)m.getValue("braillePrev");
		}
		else {
			start = list.get(index).start;
			brailleStart = list.get(index).brailleList.getFirst().start;
		}
		
		BrlOnlyMapElement b1 =  new BrlOnlyMapElement(box.getChild(0), box);
		b1.setOffsets(start, start + b1.textLength());
		b1.setBrailleOffsets(brailleStart, brailleStart + b1.getText().length());
		vi.addElementToSection(list, b1, index);
		
		//set text
		text.insertText(start, list.get(index).getText() + "\n");
		braille.insertText(brailleStart, list.get(index).brailleList.getFirst().value() + "\n");
		list.shiftOffsetsFromIndex(index + 1, list.get(index).getText().length() + 1, list.get(index).brailleList.getFirst().value().length() + 1, list.get(index + 1).start);
	}
	
	private void replaceBoxLine(Element brl, Element replacement){		
		int index = list.findNodeIndex(brl, 0);
		Text t = document.findBoxlineTextNode(brl);
		Text newText = document.findBoxlineTextNode(replacement);
		int length = t.getValue().length() - newText.getValue().length();
		t.setValue(replacement.getValue());
		braille.replaceTextRange(list.get(index).brailleList.getFirst().start, list.get(index).brailleList.getLast().end - list.get(index).brailleList.getFirst().start, t.getValue());
		
		if(length > 0)
			list.shiftOffsetsFromIndex(index + 1, length, length, 0);
	}
		
	private ArrayList<Element> findBoxlines(Element e){
		ArrayList<Element> elList = new ArrayList<Element>();
		Element parent = (Element)e.getParent();
		int index = parent.indexOf(e);
			
		for(int i = index - 1; i >= 0; i--){
			if(parent.getChild(i) instanceof Element && isBoxLine((Element)parent.getChild(i)))
				elList.add(0, (Element)parent.getChild(i));
			else 
				break;
		}
			
		elList.add(e);
			
		for(int i = index + 1; i < parent.getChildCount(); i++){
			if(parent.getChild(i) instanceof Element && isBoxLine((Element)parent.getChild(i)))
				elList.add((Element)parent.getChild(i));
			else 
				break;
		}
		return elList; 
	}
	
	/** Removes a boxline from the views and the DOM
	 * @param boxline : Element wrapping content and representing a boxline
	 * @param itemList : List containing opening and closing boxline
	 */
	public void removeSingleBoxline(Element boxline, ArrayList<TextMapElement> itemList){		
		ArrayList<Element>sidebarList = findBoxlines(boxline);
		removeBoxLine(boxline, itemList);
		
		sidebarList.remove(boxline);
		if(sidebarList.size() > 0){
			Element parent = (Element) sidebarList.get(0).getParent();
			parent = buildSegment(parent, parent.indexOf(sidebarList.get(0)), parent.indexOf(sidebarList.get(sidebarList.size() - 1)));
			resetSidebars(sidebarList, parent);
		}
	}
	
	/** Handles deleting a boxline when text selection occurs and one or more boxlines may be selected
	 * @param itemList : ItemList containing text map elements in selection collected via manager's getSelected method
	 */
	public void removeMultiBoxline(ArrayList<TextMapElement> itemList){
		clearNonBrlElements(itemList);
		
		int start = itemList.get(0).parentElement().getParent().indexOf(itemList.get(0).parentElement());
		int end = itemList.get(itemList.size() - 1).parentElement().getParent().indexOf(itemList.get(itemList.size() - 1).parentElement());
		Element parent = (Element) itemList.get(0).parentElement().getParent();
		
		for(int i = 0,j = itemList.size(); i < itemList.size(); i++, j--){
			BrlOnlyMapElement b = list.findJoiningBoxline((BrlOnlyMapElement)itemList.get(i));
			if(!itemList.contains(b) && b != null){
				itemList.add(j, b);
				if(b.parentElement().getParent().indexOf(b.parentElement()) > end && b.parentElement().getParent().equals(parent))
					end = b.parentElement().getParent().indexOf(b.parentElement()); 
			}
		}
		
		while(itemList.size() > 0){
			ArrayList<TextMapElement>boxline = new ArrayList<TextMapElement>();
			if(getStyle(itemList.get(0).parentElement()).equals("boxline") || getStyle(itemList.get(0).parentElement()).equals("topBox")){
				int index = getMatchingParent(itemList, 0);
				boxline.add(itemList.get(0));
				boxline.add(itemList.get(index));
				itemList.remove(0);
				itemList.remove(index - 1);
			}
			else if(getStyle(itemList.get(0).parentElement()).equals("middleBox") || getStyle(itemList.get(0).parentElement()).equals("bottomBox")){
				boxline.add(itemList.get(0));
				itemList.remove(0);
			}
			treeView.populateItem(boxline.get(0).parentElement());
			removeBoxLine(boxline.get(0).parentElement(), boxline);
		}
		
		if(start > 0 && parent.getChild(start - 1) instanceof Element && isBoxLine((Element)parent.getChild(start - 1)))
			start--;
		if(end < parent.getChildCount() - 1 && parent.getChild(end + 1) instanceof Element && isBoxLine((Element)parent.getChild(end + 1)))
			end++;
		
		ArrayList<Element>elList = new ArrayList<Element>();
		for(int i = start; i <= end; i++){
			if(parent.getChild(i) instanceof Element && isBoxLine((Element)parent.getChild(i)))
				elList.add((Element)parent.getChild(i));
		}
		
		Element newDoc = buildSegment(parent, start, end);
		resetSidebars(elList, newDoc);
	}
	
	private void removeBoxLine(Element boxline, ArrayList<TextMapElement> itemList){
		String style = getStyle(boxline);
		if(style.equals("boxline") || style.equals("topBox")){
			removeTopBoxline((BrlOnlyMapElement)itemList.get(0));
			removeBottomBoxline((BrlOnlyMapElement)itemList.get(1));
			removeBoxLineElement(boxline);
		}
		else if(style.equals("middleBox") || style.equals("bottomBox")){
			removeBottomBoxline((BrlOnlyMapElement)itemList.get(0));
			removeBoxLineElement(boxline);
		}
	}
	
	private Element buildSegment(Element parent, int start, int end){
		ArrayList<Element>elList = new ArrayList<Element>();
		for(int i = start; i <= end; i++){
			if(parent.getChild(i) instanceof Element)
				elList.add((Element)parent.getChild(i));
		}
		
		for(int i = 0; i < elList.size(); i++){
			if(i == 0 && isBoxLine(elList.get(i))){
				if(i < elList.size() - 1 && isBoxLine(elList.get(i + 1)))
					setStyle(elList.get(i), "topBox");
				else
					setStyle(elList.get(i), "boxline");
			}
			else if(i == elList.size() - 1 && isBoxLine(elList.get(i))){
				if(i > 0 && isBoxLine(elList.get(i - 1)))
					setStyle(elList.get(i), "bottomBox");
				else
					setStyle(elList.get(i), "boxline");
			}
			else {
				if(isBoxLine(elList.get(i))){
					if(isBoxLine(elList.get(i - 1)) && isBoxLine(elList.get(i + 1)))
						setStyle(elList.get(i), "middleBox");
					else if(isBoxLine(elList.get(i - 1)) && !isBoxLine(elList.get(i + 1)))
						setStyle(elList.get(i), "bottomBox");
					else if(!isBoxLine(elList.get(i - 1)) && isBoxLine(elList.get(i + 1)))
						setStyle(elList.get(i), "topBox");
					else
						setStyle(elList.get(i), "boxline");
				}
			}
		}
		
		Document doc = document.translateElements(elList);
		Element root = (Element)doc.getChild(0);
		Elements els = root.getChildElements();
		for(int i = 0; i < els.size(); i++){
			if(!isBoxLine(els.get(i)))
				root.removeChild(els.get(i));
		}
		
		return root;
	}
	
	private void clearNonBrlElements(ArrayList<TextMapElement> itemList){
		for(int i = 0; i < itemList.size(); i++){
			if(!(itemList.get(i) instanceof BrlOnlyMapElement)){
				itemList.remove(i);
				i--;
			}
		}
	}
	
	/** Removes the boxline from the views and maplist
	 * @param b 
	 */
	private void removeTopBoxline(BrlOnlyMapElement b){
		int index = list.indexOf(b);
		manager.getText().replaceTextRange(b.start, (b.end + 1) - b.start, "");
		manager.getBraille().replaceTextRange(b.brailleList.getFirst().start, (b.brailleList.getFirst().end + 1) - b.brailleList.getFirst().start, "");
		list.shiftOffsetsFromIndex(index,  -((b.end + 1) - b.start), -((b.brailleList.getFirst().end + 1) - b.brailleList.getFirst().start), 0);
		list.remove(index);
	}
	
	/** Removes the boxline from the views and maplist
	 * @param b 
	 */
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
		treeView.resetTreeItem(boxline);
		boxline.getParent().removeChild(boxline);
	}
	
	private boolean isBoxLine(Element e){
		if(checkSemanticsAttribute(e, "boxline") || checkSemanticsAttribute(e, "topBox") || checkSemanticsAttribute(e, "middleBox") || checkSemanticsAttribute(e, "bottomBox"))
			return true;
		else
			return false;
	}
	
	private boolean checkSemanticsAttribute(Element e, String value){
		Attribute atr = e.getAttribute("semantics");
		
		if(atr == null || !atr.getValue().contains(value))
			return false;
		
		return true;
	}
	
	private Text findText(Node n){
		if(n.getChild(0) instanceof Text)
			return (Text)n.getChild(0);
		else if(n instanceof Element)
			return findText(n);
		else 
			return null;
	}
	
	private void removeTreeItems(ArrayList<TextMapElement>itemList){
		if(treeView.getClass().equals(XMLTree.class)){
			for(int i = 0; i < itemList.size(); i++){
				Message treeMessage = new Message(null);
				treeMessage.put("removeAll", true);
				treeView.removeItem(itemList.get(i), treeMessage);
			}
		}
	}
	
	private void setStyle(Element e, String style){
		Message m = new Message(null);
		m.put("element", e);
		m.put("type", "style");
		m.put("action", style);
		manager.getDocument().applyAction(m);
	}
	
	private String getStyle(Element box){
		return box.getAttributeValue("semantics").split(",")[1];
	}
	
	private int getMatchingParent(ArrayList<TextMapElement>elList, int index){
		Element parent = elList.get(index).parentElement();
		for(int i = 0; i < elList.size(); i++)
			if(i != index && elList.get(i).parentElement().equals(parent))
				return i;
		
		return -1;
	}
}
