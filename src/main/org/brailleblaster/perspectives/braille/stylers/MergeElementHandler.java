package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.elements.BrailleMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.messages.Message;

public class MergeElementHandler extends Handler{
	BrailleDocument document;
	
	public MergeElementHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);
		document = manager.getDocument();
	}
	
	public void merge(TextMapElement t1, TextMapElement t2){
		Element mergeTo = getBlockElement(t1.n);
		Element merging = getBlockElement(t2.n);
		
		ArrayList<TextMapElement> mergeList1 = list.findTextMapElements(list.indexOf(t1), t1.parentElement(), true);
		ArrayList<TextMapElement> mergeList2 = list.findTextMapElements(list.indexOf(t2), t2.parentElement(), true);
		
		int startIndex = list.indexOf(mergeList1.get(0));
		int textStart = mergeList1.get(0).start;
		int brailleStart = mergeList1.get(0).brailleList.getFirst().start;
				
		Element mergedElement = document.mergeElements(mergeTo, merging);
		clearListRange(startIndex, mergeList1.size() + mergeList2.size());
		int size = repopulateRange(mergedElement, startIndex);

		list.setCurrent(list.getCurrentIndex());
		clearViewRanges(mergeList1.get(0), mergeList2.get(mergeList2.size() - 1), startIndex + size);	
	
		ArrayList<TextMapElement>textList = getListRange(startIndex, size);	
		setViews(textList,startIndex, textStart, brailleStart);
		resetTree(mergeTo, merging, mergedElement, textList);
		text.setCurrentElement(text.view.getCaretOffset());
	}
	
	private void setViews(ArrayList<TextMapElement> elList, int index, int textOffset, int brailleOffset ){
		Message m = new Message(null);
		int count = elList.size();
		
	//	if(shouldInsertBlankLine(elList))
	//		createBlankLine(textOffset, brailleOffset, index);
		
		for(int i = 0; i < count; i++){
		//	if(i > 0 && (isBlockElement(elList.get(i)) || afterLineBreak(elList.get(i)))){
		//		createBlankLine(textOffset, brailleOffset, index);
		//		textOffset++;
		//		brailleOffset++;
		//	}
		//	
			int brailleLength = 0;
			
			manager.getText().resetElement(m, vi, list, index, textOffset, elList.get(i), true);
			textOffset = elList.get(i).end;
			
			for(int j = 0; j < elList.get(i).brailleList.size(); j++){
				manager.getBraille().resetElement(m, list, list.get(index), elList.get(i).brailleList.get(j), brailleOffset);
				brailleOffset = (Integer)m.getValue("brailleOffset");
				brailleLength += (Integer)m.getValue("brailleLength");
			}
			
			int textLength =list.get(index).end - list.get(index).start;
			
			textLength = (Integer)m.getValue("textLength");
			textOffset = (Integer)m.getValue("textOffset");
			list.shiftOffsetsFromIndex(index + 1, textLength, brailleLength);
			index++;
		}
	}
	
	private void resetTree(Element e1, Element e2, Element newParent, ArrayList<TextMapElement>textList){
		ArrayList<Element>elList = new ArrayList<Element>();
		elList.add(e1);
		elList.add(e2);
		
		for(int i = 0; i < textList.size(); i++){
			if(!textList.get(i).parentElement().equals(newParent)){
				textList.remove(i);
				i--;
			}
		}
		
		tree.merge(textList, elList);
	}
	
	private Element getBlockElement(Node n){
		return document.getParent(n, true);
	}
	
	private void clearListRange(int start, int length){
		for(int i = 0; i < length; i++)
			vi.remove(list, start);
	}
	
	private int repopulateRange(Element e, int index){
		ArrayList<TextMapElement> elList = 	constructMapElement(e);
		for(int i = 0; i < elList.size(); i++, index++)
			vi.addElementToSection(list, elList.get(i), index);
		
		return elList.size();
	}
	private ArrayList<TextMapElement> constructMapElement(Element e){
		ArrayList<TextMapElement> elList = new ArrayList<TextMapElement>();
		
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				elList.add(new TextMapElement(e.getChild(i)));
			}
			else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && !isBoxLine(e)){
				for(int j = 0; j < e.getChild(i).getChildCount(); j++){
					if(e.getChild(i).getChild(j) instanceof Text){
						elList.get(elList.size() - 1).brailleList.add(new BrailleMapElement(e.getChild(i).getChild(j)));
					}
				}
			}
			else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && isBoxLine(e))
				elList.add(new BrlOnlyMapElement(e.getChild(i), e));
			else if(e.getChild(i) instanceof Element)
				elList.addAll(constructMapElement((Element)e.getChild(i)));
		}
		
		return elList;
	}
	
	private void clearViewRanges(TextMapElement first, TextMapElement last, int index){
		int start = first.start;
		int end = last.end;
		
		int brailleStart = first.brailleList.getFirst().start;
		int brailleEnd = last.brailleList.getLast().end;
		
		text.replaceTextRange(start, end - start, "");
		braille.replaceTextRange(brailleStart, brailleEnd - brailleStart, "");
		
		list.shiftOffsetsFromIndex(index, -(end - start), -(brailleEnd - brailleStart));
	}
	
	private ArrayList<TextMapElement>getListRange(int start, int length){
		ArrayList<TextMapElement>textList = new ArrayList<TextMapElement>();
		for(int i = start; i < start + length; i++)
			textList.add(list.get(i));
		
		return textList;
	}
}
