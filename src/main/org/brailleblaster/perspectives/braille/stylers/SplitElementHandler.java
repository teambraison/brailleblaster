package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Text;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class SplitElementHandler extends Handler{
	
	TextView text;
	BrailleView braille;
	BBTree tree;
	
	public SplitElementHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);
		
		text = manager.getText();
		braille = manager.getBraille();
		tree = manager.getTreeView();
	}
	
	public void splitElement(Message m){
		int treeIndex = tree.getBlockElementIndex();
		
		ArrayList<Integer> originalElements = list.findTextMapElementRange(list.getCurrentIndex(), (Element)list.getCurrent().parentElement(), true);
		ArrayList<Element> els = manager.getDocument().splitElement(list, list.getCurrent(), m);
		
		int textStart = list.get(originalElements.get(0)).start;
		int textEnd = list.get(originalElements.get(originalElements.size() - 1)).end;
		
		int brailleStart = list.get(originalElements.get(0)).brailleList.getFirst().start;	
		int brailleEnd = list.get(originalElements.get(originalElements.size() - 1)).brailleList.getLast().end;
				
		int currentIndex = list.getCurrentIndex();
		
		for(int i = originalElements.size() - 1; i >= 0; i--){
			int pos = originalElements.get(i);
			
			if(pos < currentIndex){
				vi.remove(list, pos);
				currentIndex--;
			}
			else if(pos >= currentIndex){
				vi.remove(list, pos);
			}
		}
		
		text.clearTextRange(textStart, textEnd - textStart);
		braille.clearTextRange(brailleStart, brailleEnd - brailleStart);
		list.shiftOffsetsFromIndex(currentIndex, -(textEnd - textStart), -(brailleEnd - brailleStart));	
		
		int firstElementIndex = currentIndex;
		currentIndex = insertElement(els.get(0), currentIndex, textStart, brailleStart) - 1;
		
		String insertionString = "";
		Styles style = manager.getStyleTable().get(manager.getStyleTable().getKeyFromAttribute(manager.getDocument().getParent(list.get(currentIndex).n, true)));

		if(style.contains(StylesType.linesBefore)){
			for(int i = 0; i < Integer.valueOf((String)style.get(StylesType.linesBefore)) + 1; i++)
				insertionString += "\n";
		}
		else if(style.contains(StylesType.linesAfter)){
			for(int i = 0; i < Integer.valueOf((String)style.get(StylesType.linesAfter)) + 1; i++)
				insertionString += "\n";
		}
		else {
			insertionString = "\n";
		}

		text.insertText(list.get(currentIndex).end, insertionString);
		braille.insertText(list.get(currentIndex).brailleList.getLast().end, insertionString);
		m.put("length", insertionString.length());
		
		int secondElementIndex = currentIndex + 1;
		currentIndex = insertElement(els.get(1), currentIndex + 1, list.get(currentIndex).end + insertionString.length(), list.get(currentIndex).brailleList.getLast().end + insertionString.length());

		list.shiftOffsetsFromIndex(currentIndex, list.get(currentIndex - 1).end - textStart, list.get(currentIndex - 1).brailleList.getLast().end - brailleStart);
		
		tree.split(Message.createSplitTreeMessage(firstElementIndex, secondElementIndex, currentIndex, treeIndex));
	}

	private int insertElement(Element e, int index, int start, int brailleStart){
		int count = e.getChildCount();
		int currentIndex = index;
		int currentStart = start;
		int currentBrailleStart = brailleStart;
		
		for(int i = 0; i < count; i++){
			if(e.getChild(i) instanceof Text){
				text.insertText(vi,list, currentIndex, currentStart, e.getChild(i));
				currentStart = list.get(currentIndex).end;
				i++;
				insertBraille((Element)e.getChild(i), currentIndex, currentBrailleStart);
				currentBrailleStart = list.get(currentIndex).brailleList.getLast().end;
				currentIndex++;
			}
			else if(e.getChild(i) instanceof Element && !((Element)e.getChild(i)).getLocalName().equals("brl")){
				currentIndex = insertElement((Element)e.getChild(i), currentIndex, currentStart, currentBrailleStart);
				currentStart = list.get(currentIndex - 1).end;
				currentBrailleStart = list.get(currentIndex - 1).brailleList.getLast().end;
			}
		}
		
		return currentIndex;
	}
	
	private void insertBraille(Element e, int index, int brailleStart){
		int count = e.getChildCount();
		
		for(int i = 0; i < count; i++){
			if(e.getChild(i) instanceof Text){
				braille.insert(list.get(index), e.getChild(i), brailleStart);
				brailleStart = list.get(index).brailleList.getLast().end;
			}
		}
	}
}
