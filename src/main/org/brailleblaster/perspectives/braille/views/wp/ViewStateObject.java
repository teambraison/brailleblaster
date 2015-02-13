package org.brailleblaster.perspectives.braille.views.wp;

public class ViewStateObject {
	private int currentStart, currentEnd;
	
	public ViewStateObject(){
		
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
}
