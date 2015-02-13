package org.brailleblaster.perspectives.braille.views.wp;

public class ViewStateObject {
	private int currentStart, currentEnd, previousEnd, nextStart, oldCursorPosition;
	
	public ViewStateObject(){
		setOldCursorPosition(-1);
	}

	public int getCurrentStart() {
		return currentStart;
	}

	public void setCurrentStart(int currentStart) {
		this.currentStart = currentStart;
	}
	
	public void adjustStart(int val){
		currentStart += val;
	}

	public int getCurrentEnd() {
		return currentEnd;
	}

	public void setCurrentEnd(int currentEnd) {
		this.currentEnd = currentEnd;
	}
	
	public void adjustEnd(int val){
		currentEnd += val;
	}

	public int getPreviousEnd() {
		return previousEnd;
	}

	public void setPreviousEnd(int previousEnd) {
		this.previousEnd = previousEnd;
	}

	public int getNextStart() {
		return nextStart;
	}

	public void setNextStart(int nextStart) {
		this.nextStart = nextStart;
	}
	
	public void adjustNextStart(int val){
		nextStart += val;
	}

	public int getOldCursorPosition() {
		return oldCursorPosition;
	}

	public void setOldCursorPosition(int oldCursorPosition) {
		this.oldCursorPosition = oldCursorPosition;
	}
}
