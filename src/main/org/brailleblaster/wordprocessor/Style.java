/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org
  *
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.wordprocessor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

class Style {
	//  This maybe used if this way is overcomplicated
	//	int lines_before;
	//	int lines_after;
	//	/* The following are percentages.*/
	//	int left_margin;
	//	int first_line_indent;
	//	int right_margin;
	//	/* Various controls */
	//	boolean keep_with_next;
	//	boolean dont_split;
	//	int orphan_control;
	//	boolean newpage_before;
	//	boolean newpage_after;
	//StyleFormat format; /*StyleFormat is an enumeration class*/
	/* used for output */
	String elementName;
	String attr1Name;
	String attr1Value;
	String attr2Name;
	String attr2Value;
	String attr3Name;
	String attr3Value;
	String styleName;
	private boolean isBuildIn;//whether it is a buildin style, default to false
	//this stores different properties of a style
	HashMap<String, String> styleSet 
	= new HashMap<String, String>();

	/**
	 * Useful to load the build-in style properties files. 
	 * i.e. "dist\programData\styles\[styleName].properties"
	 * 
	 * @param styleName
	 */
	public Style(String styleName){
		this.styleName = styleName;
		ResourceBundle labels = ResourceBundle.getBundle(styleName);
		Enumeration bundleKeys = labels.getKeys();
		while (bundleKeys.hasMoreElements()) {
			String key = (String)bundleKeys.nextElement();
			String value = labels.getString(key);
			styleSet.put(key, value);
		}
		this.isBuildIn = false; 
	}	
	
	/**
	 * Load default style setting in dist\programData\styles\default.properties
	 */
	public Style(){
		this("default");
	}

	/**
	 * Useful to create a new style from scratch
	 * 
	 * @param styleProperties
	 */
	public Style(HashMap<String, String> styleProperties){
		this();
		styleSet.putAll(styleProperties);
		if(styleSet.get("styleName") != null){
			this.styleName = styleSet.get("styleName");
		}
	}


	/**
	 * 	This enables BB to load style using absolute url; useful when loading user's
	 * local properties files
	 * 
	 * @param styleName
	 * @param loader
	 */
	public Style(String styleName, ClassLoader loader){
		this.styleName = styleName;
		ResourceBundle labels = ResourceBundle.getBundle(styleName, Locale.getDefault() , loader);
		Enumeration bundleKeys = labels.getKeys();
		while (bundleKeys.hasMoreElements()) {
			String key = (String)bundleKeys.nextElement();
			String value = labels.getString(key);
			styleSet.put(key, value);
		}
	}

	public String toString(){
		return styleName;
	}

	public String getName(){
		return styleName;
	}
	
	public void setProperty(String key, String value){
		styleSet.put(key, value);
	}

	public String getProperty(String key){
		return styleSet.get(key);
	}
	
	public void setIsBuildIn(boolean b){
		this.isBuildIn = b;
	}
	public boolean getIsBuildIn(){
		return this.isBuildIn;
	}
	

	public void print(){
		System.out.println();
		for(String s:styleSet.keySet()){
			System.out.println("key = '"+s + "': '" + styleSet.get(s)+"'");
		}
		System.out.println();
	}
}
