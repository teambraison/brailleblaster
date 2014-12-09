package org.brailleblaster.perspectives.braille.views.wp.formatters;

import nu.xom.Attribute;
import nu.xom.Element;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.mapping.elements.BrailleMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.views.wp.WPView;

public class WhiteSpaceManager {
	WPView wpView;
	MapList list;
	Manager manager;
	
	public WhiteSpaceManager(Manager manager, WPView wpView, MapList list){
		this.wpView = wpView;
		this.list = list;
		this.manager = manager;
	}

	public int setLinesBefore(TextMapElement t, int start, Styles style){
		int prevPos = 0;
		int prevLinesAfter = 0;
		int linesBefore = 0;
		
		if(style.contains(StylesType.linesBefore)){	
			linesBefore = Integer.valueOf((String)style.get(StylesType.linesBefore));
			int index = list.indexOf(t);
			if(!isFirstInList(index)){  
				if(!(list.get(index - 1) instanceof PageMapElement) && !(list.get(index - 1) instanceof BrlOnlyMapElement)){
					Element prevParent = manager.getDocument().getParent(list.get(index - 1).n, true);
					String sem = getSemanticAttribute(prevParent);
					prevPos = list.get(index - 1).end;
					if(manager.getStyleTable().get(sem).contains(StylesType.linesAfter))
						prevLinesAfter = Integer.valueOf((String)manager.getStyleTable().get(sem).get(StylesType.linesAfter));
				}
				else
					prevPos = list.get(index - 1).end;
			}
		
			if(linesBefore < prevLinesAfter)
				linesBefore = prevLinesAfter;
		
			int diff = start- prevPos;
			if((diff > linesBefore)){
				int blankLinesBetween = diff - 1;
				linesBefore = linesBefore - blankLinesBetween;
			}
	
			if(linesBefore < 0|| (list.indexOf(t) == 0 && start == 1))
				linesBefore = 0;
			
			setLines(start, linesBefore);
		}
		return linesBefore;
	}
	
	public int setLinesBeforeBraille(TextMapElement t, BrailleMapElement b, int start, Styles style){
		int prevPos = 0;
		int prevLinesAfter = 0;
		int linesBefore = 0;
		
		if(style.contains(StylesType.linesBefore)){	
			linesBefore = Integer.valueOf((String)style.get(StylesType.linesBefore));
			int index = list.indexOf(t);
			if(!isFirstInList(index)){
				if(!(list.get(index - 1) instanceof PageMapElement) && !(list.get(index - 1) instanceof BrlOnlyMapElement)){
					Element prevParent = manager.getDocument().getParent(list.get(index - 1).n, true);
					String sem = getSemanticAttribute(prevParent);
					prevPos = list.get(index - 1).brailleList.getLast().end;
					if(manager.getStyleTable().get(sem).contains(StylesType.linesAfter))
						prevLinesAfter = Integer.valueOf((String)manager.getStyleTable().get(sem).get(StylesType.linesAfter));	
				}
				else
					prevPos = list.get(index - 1).brailleList.getLast().end;
			}
		
			if(linesBefore < prevLinesAfter)
				linesBefore = prevLinesAfter;
		
			int diff = start- prevPos;
			if((diff > linesBefore)){
				int blankLinesBetween = diff - 1;
				linesBefore = linesBefore - blankLinesBetween;
			}
	
			if(linesBefore < 0 || (list.indexOf(t) == 0 && start == 1))
				linesBefore = 0;
			
			setLines(start, linesBefore);
		}
		return linesBefore;
	}
	
	public int setLinesAfter(TextMapElement t, int start, Styles style){
		int linesAfter = 0;
		int nextLinesBefore = 0;
		int nextPos = 0;
		
		if(style.contains(StylesType.linesAfter)){
			int index = list.indexOf(t);	
			linesAfter = Integer.valueOf((String)style.get(StylesType.linesAfter));
			if(isLastInList(index)){
				Element prevParent = manager.getDocument().getParent(list.get(index - 1).n, true);
				String sem = getSemanticAttribute(prevParent);
				if(isLastInList(index))
					nextPos = manager.getText().view.getCharCount();
				else
					nextPos = list.get(index + 1).start;
				
				if(manager.getStyleTable().get(sem).contains(StylesType.linesBefore))
					nextLinesBefore = Integer.valueOf((String)manager.getStyleTable().get(sem).get(StylesType.linesBefore));
			}
			
			if(linesAfter < nextLinesBefore)
				linesAfter = nextLinesBefore;
		
			int diff = nextPos - start;
			if((diff > linesAfter)){
				int blankLinesBetween = diff - 1;
				linesAfter = linesAfter - blankLinesBetween;
			}
	
			if(linesAfter < 0)
				linesAfter = 0;
			
			setLines(start, linesAfter);
			//linesAfter = Integer.valueOf((String)style.get(StylesType.linesAfter));
		}
		
		return linesAfter;
	}
	
	public int setLinesAfterBraille(TextMapElement t, BrailleMapElement b, int start, Styles style){
		int linesAfter = 0;
		int nextLinesBefore = 0;
		int nextPos = 0;
		
		if(style.contains(StylesType.linesAfter)){
			int index = list.indexOf(t);	
			linesAfter = Integer.valueOf((String)style.get(StylesType.linesAfter));
			if(isLastInList(index)){
				Element prevParent = manager.getDocument().getParent(list.get(index - 1).n, true);
				String sem = getSemanticAttribute(prevParent);
				if(isLastInList(index))
					nextPos = manager.getBraille().view.getCharCount();
				else
					nextPos = list.get(index + 1).brailleList.getFirst().start;
				
				if(manager.getStyleTable().get(sem).contains(StylesType.linesBefore))
					nextLinesBefore = Integer.valueOf((String)manager.getStyleTable().get(sem).get(StylesType.linesBefore));
			}
			
			if(linesAfter < nextLinesBefore)
				linesAfter = nextLinesBefore;
		
			int diff = nextPos - start;
			if((diff > linesAfter)){
				int blankLinesBetween = diff - 1;
				linesAfter = linesAfter - blankLinesBetween;
			}
	
			if(linesAfter < 0)
				linesAfter = 0;
			
			setLines(start, linesAfter);
			//linesAfter = Integer.valueOf((String)style.get(StylesType.linesAfter));
		}
		
		return linesAfter;
	}
	
	private void setLines(int start, int lines){
		String text = makeInsertionString(lines,'\n');
		insert(start, text);
	}
	
	private void insert(int start, String text){
		int previousPosition = wpView.view.getCaretOffset();
		wpView.view.setCaretOffset(start);
		wpView.view.insert(text);
		wpView.view.setCaretOffset(previousPosition);
	}
	
	
	protected String makeInsertionString(int length, char c){
		String insertionString = "";
		for(int i = 0; i < length; i++){
			insertionString += c;
		}
		
		return insertionString;
	}
	
	private boolean isFirstInList(int index){
		if(index == 0)
			return true;
		else
			return false;
	}
	
	private boolean isLastInList(int index){
		if(index == list.size() - 1)
			return true;
		else
			return false;
	}
	
	private String getSemanticAttribute(Element e){
		Attribute atr = e.getAttribute("semantics");
		if(atr != null){
			String val = atr.getValue();
			String[] tokens = val.split(",");
			if(tokens.length > 1)
				return tokens[1];
		}
		
		return null;
	}
}
