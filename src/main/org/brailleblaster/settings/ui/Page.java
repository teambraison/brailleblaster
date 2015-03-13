package org.brailleblaster.settings.ui;

import org.brailleblaster.utd.utils.PageUnitConverter;

class Page {
	String type;
	double width, height, mmWidth, mmHeight;

	public Page(String type, double width, double height, boolean convertToMM) {
		this.type = type;
		this.width = convertToMM ? PageUnitConverter.inchesToMM(width) : width;
		this.height = convertToMM ? PageUnitConverter.inchesToMM(height) : height;
	}

	@Override
	public String toString() {
		return type + " (" + width + ", " + height + ")";
	}
}
