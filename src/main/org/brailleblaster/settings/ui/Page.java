package org.brailleblaster.settings.ui;

import org.brailleblaster.utd.utils.UnitConverter;

class Page {
	String type;
	double width, height;

	public Page(String type, double width, double height, boolean convertToMM) {
		this.type = type;
		this.width = convertToMM ? UnitConverter.inchesToMM(width) : width;
		this.height = convertToMM ? UnitConverter.inchesToMM(height) : height;
	}

	@Override
	public String toString() {
		return type + " (" + width + ", " + height + ")";
	}
}
