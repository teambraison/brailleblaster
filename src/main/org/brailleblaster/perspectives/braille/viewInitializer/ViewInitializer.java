package org.brailleblaster.perspectives.braille.viewInitializer;

import java.util.ArrayList;

import nu.xom.Element;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.SectionElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public abstract class ViewInitializer {
	protected final int CHAR_COUNT = 5000;
	
	BrailleDocument document;
	TextView text;
	BrailleView braille;
	BBTree tree;
	MapList viewList;

	protected ArrayList<SectionElement>sectionList;
	
	public ViewInitializer(BrailleDocument doc, TextView text, BrailleView braille, BBTree tree){
		this.document = doc;
		this.text = text;
		this.braille = braille;
		this.tree = tree;
	}
	
	protected MapList makeList(Manager m){
		viewList = new MapList(m);
		for(int i = 0; i < sectionList.size(); i++){
			if(sectionList.get(i).isVisible()){
				viewList.addAll(sectionList.get(i).getList());
				viewList.setCurrent(viewList.indexOf(viewList.getCurrent()));
			}
		}
		
		return viewList;
	}
	
	protected void appendToViews(MapList list, int index){
		int count = list.size();
		
		for(int i = index; i < count; i++){
			if(list.get(i).isMathML()){
				text.setMathML(list, list.get(i));
				braille.setBraille(list.get(i), list, i);
			}
			else if(list.get(i) instanceof BrlOnlyMapElement ){				
				text.setBRLOnlyText((BrlOnlyMapElement) list.get(i),false);
				braille.setBRLOnlyBraille((BrlOnlyMapElement) list.get(i),false);
				
			}
			else if(list.get(i) instanceof PageMapElement){
				text.addPageNumber((PageMapElement)list.get(i), false);
				braille.addPageNumber((PageMapElement)list.get(i), false);
			}
			else {
				text.setText(list.get(i), list, i);
				braille.setBraille(list.get(i), list, i);
			}
		}
		if(count>0 ){
	    	braille.addIndicator();
		}
		
	}
	
	protected void prependToViews(MapList list, int index){
		int count = list.size();
		
		text.view.setCaretOffset(0);
		braille.view.setCaretOffset(0);
		
		for(int i = index; i < count; i++){
			if(list.get(i).isMathML()){
				text.prependMathML(list, list.get(i));
				braille.prependBraille(list.get(i), list, i);
			}
			else if(list.get(i) instanceof BrlOnlyMapElement ){
				text.setBRLOnlyText((BrlOnlyMapElement) list.get(i),true);
				braille.setBRLOnlyBraille((BrlOnlyMapElement) list.get(i),true);
				
			}
			else if(list.get(i) instanceof BrlOnlyMapElement ){
				text.setBRLOnlyText((BrlOnlyMapElement) list.get(i), true);
				braille.setBRLOnlyBraille((BrlOnlyMapElement) list.get(i), true);
			}
			else if(list.get(i) instanceof PageMapElement){
				text.addPageNumber((PageMapElement)list.get(i), true);
				braille.addPageNumber((PageMapElement)list.get(i), true);
			}
			else {
				text.prependText(list.get(i), list, i);
				braille.prependBraille(list.get(i), list, i);
			}
			
			text.view.setCaretOffset(text.getTotal());
			braille.view.setCaretOffset(braille.getTotal());
		}
	}
	
	public MapList bufferBackward(){
		if(sectionList.size() > 1){
			removeListeners();
			int caretOffset = getCursorOffset();
			
			int startPos = findFirst();
			int endPos = findLast();
			if(startPos != endPos && startPos != 0){
				TextMapElement t = viewList.getCurrent();
				//Remove elements
				viewList.removeAll(sectionList.get(endPos).getList());
				//remove text from views
				
				replaceTextRange(sectionList.get(startPos).getList().getLast().end, text.view.getCharCount() - sectionList.get(startPos).getList().getLast().end, sectionList.get(startPos).getList().getLast().brailleList.getLast().end, braille.view.getCharCount() - sectionList.get(startPos).getList().getLast().brailleList.getLast().end);		
				sectionList.get(endPos).resetList();
				
				//reset page indexes for elements not removed
				int size = sectionList.get(startPos - 1).getList().size();
				
				int index = startPos - 1;
				do{
					//set start pos to insert
					text.setTotal(0);
					braille.setTotal(0);
				
					//add elements to viewList
					viewList.addAll(0, sectionList.get(index).getList());
				
					//set in view and initialize
					sectionList.get(index).setInView(true);
					size = sectionList.get(index).getList().size();
					prependToViews(sectionList.get(index).getList(), 0);
					int textTotal = text.getTotal();
					int brailleTotal = braille.getTotal();
					for(int i = size; i < viewList.size(); i++){
						viewList.get(i).start += textTotal;
						viewList.get(i).end += textTotal;
						for(int j = 0; j < viewList.get(i).brailleList.size(); j++){
							viewList.get(i).brailleList.get(j).start += brailleTotal;
							viewList.get(i).brailleList.get(j).end += brailleTotal;
						}
					}
				
					index --;
				} 	while(index >= 0 && text.view.getCharCount() < CHAR_COUNT);
				setCursorOffset(t, caretOffset);
			}
			initializeListeners();
		}
		return viewList;
	}
	
	public MapList bufferForward(){
		if(sectionList.size() > 1){
			removeListeners();
			int caretOffset = getCursorOffset();
			
			int startPos = findFirst();
			int endPos = findLast();
			if(startPos != endPos && endPos != sectionList.size() - 1){
				TextMapElement t = viewList.getCurrent();
				viewList.removeAll(sectionList.get(startPos).getList());
				int textOffset = sectionList.get(startPos).getList().getLast().end;
				int brailleOffset = sectionList.get(startPos).getList().getLast().brailleList.getLast().end;
				replaceTextRange(0, textOffset, 0, brailleOffset);
				sectionList.get(startPos).resetList();
				//int textOffset = viewList.getFirst().start;
				//int brailleOffset = viewList.getFirst().brailleList.getFirst().start;
				for(int i = 0; i < viewList.size(); i++){
					viewList.get(i).start -= textOffset;
					viewList.get(i).end -= textOffset;
					for(int j = 0; j < viewList.get(i).brailleList.size(); j++){
						viewList.get(i).brailleList.get(j).start -= brailleOffset;
						viewList.get(i).brailleList.get(j).end -= brailleOffset;
					}
				}
			
				int pos = endPos + 1;
				do {
					text.setTotal(text.view.getCharCount());
					braille.setTotal(braille.view.getCharCount());
					viewList.addAll(sectionList.get(pos).getList());
					sectionList.get(pos).setInView(true);
					appendToViews(sectionList.get(pos).getList(), 0);
					pos++;
				}while(pos < sectionList.size() && text.view.getCharCount() < CHAR_COUNT);
				setCursorOffset(t, caretOffset);
			}
			initializeListeners();
		}
		
		return viewList;
	}
	
	/** Buffers views by including one section before or after the given index if greter than zero and list than 
	 * list size, method is used when either arrowing up or down, or scrolling up or down.  Including one section 
	 * before the desired index
	 * @param index : index to be included in new views
	 * @return new maplist of views
	 */
	public MapList bufferViews(int index) {
		if(sectionList.size() > 1){
			removeListeners();
			TextMapElement t = sectionList.get(index).getList().getFirst();
			if(index != 0)
				index--;
			
			setViews(t, index);
		}
		
		return viewList;
	}
	
	/** Resets the views from the starting index, used to recreate a view when undoing events
	 * @param index: index of section to start from to recreate views
	 * @return a new maplist
	 */
	public MapList resetViews(int index) {
		if(sectionList.size() > 1){
			removeListeners();
			TextMapElement t = sectionList.get(index).getList().getFirst();
			setViews(t, index);
		}
		
		return viewList;
	}
	
	private void setViews(TextMapElement t, int index){
		int startPos = findFirst();
		int endPos = findLast();
			
		clearViewList(startPos, endPos);
		clearViews();
			
		int i = index;
		
		int totalChars = 0;
		while(i < sectionList.size() && (i < index + 2 || totalChars < CHAR_COUNT)){
			viewList.addAll(sectionList.get(i).getList());
			totalChars += sectionList.get(i).getCharCount();
			sectionList.get(i).setInView(true);
			i++;
		}
		appendToViews(viewList, 0);
		
		if(!viewList.getFirst().equals(t)){
			text.positionScrollbar(text.view.getLineAtOffset(t.start));
			braille.positionScrollbar(braille.view.getLineAtOffset(t.brailleList.getFirst().start));
		}
		initializeListeners();
	}
	
	private void clearViewList(int startPos, int endPos){
		for(int i = startPos; i <= endPos; i++){
			viewList.removeAll(sectionList.get(i).getList());
			sectionList.get(i).resetList();
		}
	}
	
	private void clearViews(){
		replaceTextRange(0, text.view.getCharCount(), 0, braille.view.getCharCount());
		text.setTotal(0);
		braille.setTotal(0);
	}
	
	private void replaceTextRange(int textStart, int textLength, int brailleStart, int brailleLength){
		text.view.replaceTextRange(textStart, textLength, "");
		braille.view.replaceTextRange(brailleStart, brailleLength, "");
	}
	
	private int getCursorOffset(){
		if(text.view.isFocusControl())
			return text.view.getCaretOffset() - viewList.getCurrent().start;
		else if(braille.view.isFocusControl())
			return braille.view.getCaretOffset() - viewList.getCurrent().brailleList.getFirst().start;
		
		return 0;
	}
	
	private void setCursorOffset(TextMapElement t, int offset){
		if(text.view.isFocusControl())
			text.view.setCaretOffset(t.start + offset);
		else if(braille.view.isFocusControl())
			braille.view.setCaretOffset(t.brailleList.getFirst().start + offset);
	}
	
	private void removeListeners(){
		text.removeListeners();
		braille.removeListeners();
		tree.removeListeners();
	}
	
	private void initializeListeners(){
		text.initializeListeners();
		braille.initializeListeners();
		tree.initializeListeners();
	}
	
	public int getStartIndex(){
		return findFirst();
	}
	
	private int findFirst(){
		for(int  i = 0; i < sectionList.size(); i++){
			if(sectionList.get(i).isVisible())
				return i;
		}
		
		return -1;
	}
	
	private int findLast(){
		int position = -1;
		for(int i = 0; i < sectionList.size(); i++){
			if(sectionList.get(i).isVisible())
				position = i;
		}
		
		return position;
	}
	
	/** Adds a new TextMapElement to both the list containing all element in the views and the section map list
	 * @param list: the visible list containing map elements in the text and braille views
	 * @param t: element to insert
	 * @param index: index at which element is being put in visible MapList, also used to determine which section to also add element
	 */
	public void addElementToSection(MapList list, TextMapElement t, int index){
		int sectionIndex;
		//if index is list size then element is appended and not inserted
		if(index == list.size())
			sectionIndex = findSectionIndex(list.get(index - 1));
		else
			sectionIndex = findSectionIndex(list.get(index));
		
		if(sectionIndex != -1){
			SectionElement section = sectionList.get(sectionIndex);
			int listIndex;
			if(index == list.size())
				section.getList().indexOf(section.getList().add(t));
			else {
				listIndex = section.getList().indexOf(list.get(index));
				section.getList().add(listIndex, t);
			}
			list.add(index, t);
		}
	}
	
	public void remove(MapList list, int pos){
		int start = findFirst();
		TextMapElement t = list.get(pos);
		for(int i = start; i < sectionList.size(); i++){
			if(sectionList.get(i).getList().contains(t)){
				sectionList.get(i).getList().remove(t);
				break;
			}
		}
		
		list.remove(pos);
	}
	
	/** Used to find a section element by view initializer methods
	 * @param t: text map element used to find section
	 * @return int with index value of section in the sectionlist
	 */
	private int findSectionIndex(TextMapElement t){
		int index = findFirst();
		for(int i = index; i < sectionList.size(); i++){
			if(sectionList.get(i).getList().contains(t))
				return i;
		}
		
		return -1;
	}

	public ArrayList<SectionElement> getSectionList() {
		return sectionList;
	}
	
	public void resetTree(BBTree tree){
		this.tree = tree;
	}
	
	protected abstract void findSections(Manager m, Element e);
	public abstract void initializeViews(Manager m);
	public abstract MapList getList(Manager m);
}
