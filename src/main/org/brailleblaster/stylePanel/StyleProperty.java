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

package org.brailleblaster.stylePanel;

public enum StyleProperty {
		//  Each property takes two parameters
		//the first one is type index, which indicates
		//the type of acceptable value of this prop,
		//i.e. integer, boolean for now. The other
		//is the default value displayed in the form
		
		linesBefore(0,1),
		linesAfter(0,1),
		/* The following are percentages.*/
		leftMargin(0,5),
		firstLineIndent(0,2),
		rightMargin(0,5),
		/* Various controls */
		keepWithNext(1,true),
		dontSplit(1,false),
		orphanControl(1,false),
		newpageBefore(1,true),
		newpageAfter(1,true);

		final static int INTEGER = 0;
		final static int BOOLEAN = 1;
		private int typeIndex;
		private int defaultInt;
		private boolean defaultBool;

		StyleProperty(int index, int defaultValue){
			typeIndex = index;
			defaultInt = defaultValue;
		}
		StyleProperty(int index, boolean defaultValue){
			typeIndex = index;
			defaultBool = defaultValue;
		}

		String getToolTip(){
			if(typeIndex == INTEGER){
				return this.toString()+": Please enter an integer";
			}
			else if (typeIndex == BOOLEAN){
				return this.toString()+": Please select a boolean value";
			}
			return "";
		}

		int getTypeIndex(){
			return typeIndex;
		}

		String getDefaultValue(){
			if(typeIndex == INTEGER){
				return Integer.toString(this.defaultInt);
			}
			else if(typeIndex == BOOLEAN){
				if(this.defaultBool)return "True";
				else return "False";
			}
			return "";
		}
}
