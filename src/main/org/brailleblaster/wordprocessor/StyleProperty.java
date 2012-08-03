package org.brailleblaster.wordprocessor;

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
