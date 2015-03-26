package org.brailleblaster.perspectives.braille.views.wp;

public class Selection {
	private int selectionStart, selectionLength;
	
	public Selection(){
		
	}
	
	public Selection(int [] selection){
		setSelectionStart(selection[0]);
		setSelectionLength(selection[1]);
	}

	public int getSelectionStart() {
		return selectionStart;
	}

	public void setSelectionStart(int selectionStart) {
		this.selectionStart = selectionStart;
	}
	
	public void adjustSelectionStart(int val){
		selectionStart += val;
	}

	public int getSelectionLength() {
		return selectionLength;
	}

	public void setSelectionLength(int selectionLength) {
		this.selectionLength = selectionLength;
	}
	
	public void adjustSelectionLength(int val){
		selectionLength += val;
	}
	
	public int getSelectionEnd(){
		return selectionStart + selectionLength;
	}
}
