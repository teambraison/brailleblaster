package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import nu.xom.Element;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class StyleHandler {

	Manager manager;
	BrailleDocument document;
	MapList list;
	TextView text;
	BrailleView braille;
	BBTree treeView;
	
	public StyleHandler(Manager manager, MapList list){
		this.manager = manager;
		this.document = manager.getDocument();
		this.list = list;
		this.text = manager.getText();
		this.braille = manager.getBraille();
		this.treeView = manager.getTreeView();
	}
	
	public void updateStyle(Message message){
		if(message.getValue("multiSelect").equals(false)) 
			handleStyleSingleSelected(message);
		else 
			handleStyleMultiSelected(message);
	}
	
	/***
	 * Handle style if user just move cursor
	 * @param message: Message object passed containing information from style table manager
	 */
	private void handleStyleSingleSelected(Message message) {
		Element parent = parentStyle(list.getCurrent(), message);
		document.changeSemanticAction(message, parent);
		ArrayList<TextMapElement> itemList = list.findTextMapElements(list.getCurrentIndex(), parent, true);
		adjustStyle(itemList, message);
	}

	/***
	 * Apply styles to selected text for multiple elements
	 * @param start: starting offset of highlighted text used to find first block element
	 * @param end: end position of highlighted text used to find last block element
	 * @param message: Message object passed containing information from style table manager
	 */
	private void handleStyleMultiSelected(Message message){
		int start=text.getSelectedText()[0];
		int end=text.getSelectedText()[1];
		
		Set<TextMapElement> itemSet = manager.getElementInSelectedRange(start, end);		
		Iterator<TextMapElement> itr = itemSet.iterator();
		ArrayList<Element>parents = new ArrayList<Element>();
		
		while (itr.hasNext()) {
			TextMapElement tempElement= itr.next();
			if( (!((tempElement instanceof BrlOnlyMapElement) || (tempElement instanceof PageMapElement)))){
				Message styleMessage = Message.createUpdateStyleMessage((Styles)message.getValue("Style"), (Boolean)message.getValue("multiSelect"), (Boolean)message.getValue("isBoxline"));
				Element parent = parentStyle(tempElement, styleMessage);
				parents.add(parent);
				document.changeSemanticAction(message, parent);
				ArrayList<TextMapElement> itemList = list.findTextMapElements(list.getNodeIndex(tempElement), parent, true);
				adjustStyle( itemList,styleMessage);
			}
		}
	}
	
	private Element parentStyle(TextMapElement current, Message message) {
		Element parent;
		if(current instanceof PageMapElement || current instanceof BrlOnlyMapElement)
			parent = current.parentElement();
		else
			parent = document.getParent(current.n, true);
		
		BBSemanticsTable styles = manager.getStyleTable();
		message.put("previousStyle", styles.get(styles.getKeyFromAttribute(parent)));
		return parent;
	}
	
	/***
	 * Adjust style of elements in the list base on previous and next element 
	 * @param itemList : all selected items which we want style to be applied
	 * @param message : passing information regarding styles
	 */
	private void adjustStyle(ArrayList<TextMapElement> itemList, Message message) {
		int start = list.indexOf(itemList.get(0));
		int end = list.indexOf(itemList.get(itemList.size() - 1));
	
		if (start > 0) {
			message.put("prev", list.get(start - 1).end);
			message.put("braillePrev",
					list.get(start - 1).brailleList.getLast().end);
		} else {
			message.put("prev", -1);
			message.put("braillePrev", -1);
		}

		if (end < list.size() - 1) {
			message.put("next", list.get(end + 1).start);
			message.put("brailleNext",
					list.get(end + 1).brailleList.getFirst().start);
		} else {
			message.put("next", -1);
			message.put("brailleNext", -1);
		}

		text.adjustStyle(message, itemList);
		braille.adjustStyle(message, itemList);

		if (message.contains("linesBeforeOffset"))
			list.shiftOffsetsFromIndex(start,
					(Integer) message.getValue("linesBeforeOffset"),
					(Integer) message.getValue("linesBeforeOffset"));
		if (message.contains("linesAfterOffset") && list.size() > 1
				&& end < list.size() - 1)
			list.shiftOffsetsFromIndex(end + 1,
					(Integer) message.getValue("linesAfterOffset"),
					(Integer) message.getValue("linesAfterOffset"));

		treeView.adjustItemStyle(list.getCurrent());
	}	
}
