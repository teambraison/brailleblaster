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

import org.brailleblaster.document.BBSemanticsTable;
import org.brailleblaster.document.BBSemanticsTable.Styles;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.messages.BBEvent;
import org.brailleblaster.perspectives.braille.messages.Message;
//import org.brailleblaster.views.PropertyView;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;

public class StyleManager{
	
    private StylePanel sp;
    private StyleTable table;
    //private PropertyView propView;
    private String configFile;
    Manager dm;
    
    private BBSemanticsTable semanticsTable;
    
    public StyleManager(Manager dm) {
    	this.dm = dm;
    	this.configFile = dm.getCurrentConfig();
    	this.table = new StyleTable(this, dm.getGroup());
    	this.semanticsTable = dm.getStyleTable();
	}

	void createStyle(String styleName){
    	EditStyle es = new EditStyle(this);
    	es.create(styleName);
    }
    
    void modifyStyle(Style style){
    	EditStyle es = new EditStyle(this);
    	es.modify(style);
    }
    
    public void stylePanel(){
    	sp.open();
    }
    
    void readStyleFiles(String styleName){
    	sp.readStyleFiles(styleName);
    }
    
    public void displayTable(TextMapElement item){
    	table.showTable(item);
    }
    
    public void apply(String item){
    	Message m = new Message(BBEvent.UPDATE_STYLE);
    	Styles style = semanticsTable.get(item);
    	if(style != null){
    		m.put("Style", style);
    		dm.dispatch(m);
    	}
    }
    
    public void setStyleTableItem(TextMapElement t){
    	if(table.isVisible())
    		table.setSelection(t);
    }
    
    public boolean tableIsVisible(){
    	return table.isVisible();
    }
    
    public BBSemanticsTable getSemanticsTable(){
    	return this.semanticsTable;
    }
    
    public StyleTable getStyleTable(){
    	return table;
    }
    
    public Table getTable(){
    	return table.getTable();
    }
    
    public Group getGroup(){
    	return table.getGroup();
    }
    
    public void hideTable(){
    	table.hideTable();
    }
    
    public String getConfigFile(){
    	return configFile;
    }
}
