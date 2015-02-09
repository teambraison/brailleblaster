package org.brailleblaster.settings.ui;

public class Page {
	String type;
	double width, height, mmWidth, mmHeight;
	
	public Page(String type, double width, double height){
		this.type = type;
		this.width = width;
		this.height = height;
		this.mmWidth = inchesToMM(width);
		this.mmHeight = inchesToMM(height);
	}
	
	private double inchesToMM(double inches){
		double denominator = 0.039370;
		return Math.round((inches / denominator) * 10.0) / 10.0;
	}
}
