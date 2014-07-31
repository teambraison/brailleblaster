/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org www.aph.org
  *
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknowledged.
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
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.perspectives.braille.stylepanel;

import java.util.Set;

import org.brailleblaster.document.ConfigFileHandler;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.BBEvent;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;

public class StyleManager{
	
    private StyleTable table;
    private StylePanel editor;
    private String configFile;
    Manager dm;
    TextMapElement t;
    private int lastSelection;
    private BBSemanticsTable semanticsTable;
    
    public StyleManager(Manager dm) {
    	this.dm = dm;
    	this.configFile = dm.getCurrentConfig();
    	this.semanticsTable = dm.getStyleTable();
       	this.table = new StyleTable(this, dm.getGroup());
	}
    
    public void displayTable(TextMapElement item){
    	if(table != null && !table.getGroup().isDisposed())
    		table.showTable(item);
    	else if(editor != null && !editor.getGroup().isDisposed()){
    		editor.showTable();
    	}
    }
    
    public void openNewStyleTable(){
    	lastSelection = table.getTable().getSelectionIndex();
    	this.table.dispose();
    	editor = new NewStyleView(this, dm.getGroup());
    	dm.setTabList();
    }
    
    public void openEditStyle(){
    	lastSelection = table.getTable().getSelectionIndex();
    	String style = table.getTable().getSelection()[0].getText(1);
    	this.table.dispose();
    	if(semanticsTable.get(style).getName().equals("boxline"))
    		editor = new EditBoxLineView(this, dm.getGroup(), semanticsTable.get(style));
    	else
    		editor = new EditStyleView(this, dm.getGroup(), semanticsTable.get(style));
    	dm.setTabList();
    }
    
    public void closeEditStyle(String styleName){
    	editor.dispose();
    	table = new StyleTable(this, dm.getGroup());
    	displayTable(dm.getCurrent());
    	dm.getGroup().layout();
    	table.getTable().setSelection(lastSelection);
    	dm.setTabList();
    	
    	if(styleName != null)
    		table.setSelection(styleName);
    }
    
    public void apply(String item){
    	Message m = new Message(BBEvent.UPDATE_STYLE);
    	Styles style = semanticsTable.get(item);

    	if(style != null){
    		m.put("Style", style);
    	   	if(dm.getText().isMultiSelected()==true)
        	{
        		m.put("multiSelect", true);
        	}
        	else
        	{
        		m.put("multiSelect", false);
        		
        	}
    		dm.dispatch(m);
    	}
    }
    
	//Values in the style object are saved using the liblouisutdml keyword
	//After saving to file, the style table is refreshed and the data type and value will correspond to the SWT object in the UI that applies them 
    protected void saveEditedStyle(Styles oldStyle, Styles newStyle){  	
    	ConfigFileHandler handler = new ConfigFileHandler(configFile);
    	handler.updateStyle(newStyle);
    	semanticsTable.resetStyleTable(configFile);
    	dm.refresh();
    	closeEditStyle(newStyle.getName());
    }
    
    protected void saveNewItem(Styles style){
    	if(semanticsTable.containsKey(style.getName())){
    		LocaleHandler lh = new LocaleHandler();
    		new Notify(lh.localValue("styleExists"));
    	}
    	else {
    		ConfigFileHandler handler = new ConfigFileHandler(configFile);
    		handler.appendStyle(style);
    		semanticsTable.resetStyleTable(configFile);
    		closeEditStyle(style.getName());
    	}
    }
    
    protected void deleteStyle(String style){
    	ConfigFileHandler handler = new ConfigFileHandler(configFile);
    	handler.deleteStyle(style);
    }
    
    protected void restoreDefaults(){
    	ConfigFileHandler handler = new ConfigFileHandler(configFile);
    	handler.restoreDefaults();
    	semanticsTable.resetStyleTable(configFile);
    	resetStylePanel(configFile);
    }
    
    public void setStyleTableItem(TextMapElement t){
    	if(table.isVisible())
    		table.setSelection(t);
    }
    
    public boolean panelIsVisible(){
        if((table != null && table.isVisible()) || (editor != null && editor.isVisible()))
            return true;
        else
            return false;
    }
    
    public void resetStylePanel(String configFile){
    	this.configFile = configFile;
    	table.resetTable(configFile);
    }
    
    public BBSemanticsTable getSemanticsTable(){
    	return this.semanticsTable;
    }
    
    public Set<String>getKeySet(){
    	return semanticsTable.getKeySet();
    }
    
    public StyleTable getStyleTable(){
    	return table;
    }
    
    public Table getTable(){
    	return table.getTable();
    }
    
    public Group getGroup(){
        if(table.isVisible())
            return table.getGroup();
        else
            return editor.getGroup();
    }
    
    public void hideTable(){
    	if(table != null && !table.getGroup().isDisposed())
    		table.hideTable();
    	else if(editor != null && !editor.getGroup().isDisposed())
    		editor.hideTable();
    }
    
    public String getConfigFile(){
    	return configFile;
    }
}
