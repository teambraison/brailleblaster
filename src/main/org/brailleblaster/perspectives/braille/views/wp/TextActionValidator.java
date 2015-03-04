package org.brailleblaster.perspectives.braille.views.wp;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.eclipse.swt.custom.StyledText;

public class TextActionValidator {
	StyledText view;
	Manager manager;
	
	public TextActionValidator(Manager manager, StyledText view){
		this.view = view;
		this.manager = manager;
	}
	
	protected boolean validCut(TextMapElement currentElement, ViewStateObject stateObj, int selectionStart, int selectionLength){
		int currentStart = stateObj.getCurrentStart();
		int currentEnd = stateObj.getCurrentEnd();
		int nextStart = stateObj.getNextStart();
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if(selectionStart == currentStart && selectionLength == (currentEnd - currentStart))
				return false;
			else if(selectionStart >= currentStart && selectionStart < currentEnd && selectionLength <= (currentEnd - selectionStart))
				return false;
			else if(selectionStart  == currentEnd && selectionLength == 1 && selectionStart + selectionLength == nextStart)
				return false;
		}
		return true;
	}
	
	protected boolean validPaste(TextMapElement currentElement, ViewStateObject stateObj, int selectionStart, int selectionLength){
		int currentStart = stateObj.getCurrentStart();
		int currentEnd = stateObj.getCurrentEnd();
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if(selectionStart == currentStart && selectionLength == (currentEnd - currentStart))
				return false;
			else if(selectionStart >= currentStart && selectionStart < currentEnd && selectionLength <= (currentEnd - selectionStart))
				return false;
			else if(view.getSelectionRanges()[1] == 0 || (selectionStart >= currentStart && selectionStart <= currentEnd ))
				return false;
		}
		return true;
	}
	
	protected boolean validDelete(TextMapElement currentElement, ViewStateObject stateObj, int selectionStart, int selectionLength){
		int currentStart = stateObj.getCurrentStart();
		int currentEnd = stateObj.getCurrentEnd();
		int nextStart = stateObj.getNextStart();
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if((selectionStart >= currentStart && selectionStart + selectionLength <= currentEnd) || selectionLength == 0)
				return false;
			else if(selectionLength <= 0 && view.getCaretOffset() == currentEnd)
				return false;
			else if(selectionStart == currentEnd && (selectionStart + selectionLength) == nextStart && selectionLength == 1)
				return false;
		}
		else if(selectionLength <= 0 && manager.inPrintPageRange(view.getCaretOffset() + 1) || (selectionLength <= 0 && (manager.getElementInRange(view.getCaretOffset() + 1) instanceof BrlOnlyMapElement)))
			return false;
		
		return true;
	}
	
	protected boolean validBackspace(TextMapElement currentElement, ViewStateObject stateObj, int selectionStart, int selectionLength){
		int currentStart = stateObj.getCurrentStart();
		int currentEnd = stateObj.getCurrentEnd();
		int nextStart = stateObj.getNextStart();
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if(selectionStart >= currentStart && selectionStart + selectionLength <= currentEnd)
				return false;
			else if(selectionLength <= 0)
				return false;
			else if(selectionStart == currentEnd && (selectionStart + selectionLength) == nextStart && selectionLength == 1)
				return false;
		}
		else if(selectionLength <= 0 && manager.inPrintPageRange(view.getCaretOffset() - 1) || (selectionLength <= 0 && (manager.getElementInRange(view.getCaretOffset() - 1) instanceof BrlOnlyMapElement)))
			return false;
		
		return true;
	}
	
	protected boolean validEdit(TextMapElement currentElement, ViewStateObject stateObj, int selectionStart, int selectionLength){
		int currentStart = stateObj.getCurrentStart();
		int currentEnd = stateObj.getCurrentEnd();
		if(currentElement instanceof PageMapElement || currentElement instanceof BrlOnlyMapElement){
			if(selectionLength <= 0)
				return false;
			else if(selectionStart == currentStart && selectionLength == (currentEnd - currentStart))
				return false;
			else if(selectionStart >= currentStart && selectionStart < currentEnd && selectionLength <= (currentEnd - selectionStart))
				return false;
			else if(selectionStart >= currentStart && selectionStart <= currentEnd)
				return false;
		}
		
		return true;
	}
}
