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

import java.util.ArrayList;
import java.util.Set;

import org.brailleblaster.document.ConfigFileHandler;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;

public class StyleManager{
	private SashForm container;
    private StyleTable table;
    private StylePanel editor;
    private String configFile;
    Manager dm;
    private int lastSelection;
    private BBSemanticsTable semanticsTable;
    
    public StyleManager(Manager dm, SashForm container) {
    	this.container = container;
    	this.dm = dm;
    	this.configFile = dm.getCurrentConfig();
    	this.semanticsTable = dm.getStyleTable();
       	this.table = new StyleTable(this, container);
	}
    
    public void displayTable(TextMapElement item){
    	if(table != null && !table.getGroup().isDisposed())
    		table.showTable(item);
    	else if(editor != null && !editor.getGroup().isDisposed()){
    		editor.showTable();
    	}
    }
    
    public void openNewStyleTable(){
    	int [] weights = container.getWeights();
    	lastSelection = table.getTable().getSelectionIndex();
    	this.table.dispose();
    	editor = new NewStyleView(this, container);
    	container.setWeights(weights);
    	dm.setTabList();
    }
    
    public void openEditStyle(){
    	int [] weights = container.getWeights();
    	lastSelection = table.getTable().getSelectionIndex();
    	String style = (String) table.getTable().getSelection()[0].getData();
    	this.table.dispose();
    	Styles styleForView = null;
    	try {
    		styleForView = indentToCell(semanticsTable.get(style).clone());
    	} catch (CloneNotSupportedException e) {
    		e.printStackTrace();
    		new Notify("An error occurred");
    	}
    	
    	if(semanticsTable.get(style).getName().equals("boxline"))
    		editor = new EditBoxLineView(this, container);
    	else
	    	editor = new EditStyleView(this, container, styleForView);
   
    	container.setWeights(weights);
    	dm.setTabList();
    }
    
    
    private Styles indentToCell (Styles style){
    	int cellPosition;
    	if (style.contains(BBSemanticsTable.StylesType.leftMargin)){
    		cellPosition = Integer.valueOf((String)style.get((BBSemanticsTable.StylesType.leftMargin))) + 1;
    	}else{ 
    		cellPosition =1;
    	}
    	int firstLineCellPosition;
		if (style.contains(BBSemanticsTable.StylesType.firstLineIndent)){ 
    		firstLineCellPosition = Integer.valueOf((String)style.get(BBSemanticsTable.StylesType.firstLineIndent)) + cellPosition;
    	}else{
    		firstLineCellPosition=1;
    	}
		style.put(BBSemanticsTable.StylesType.firstLineIndent, String.valueOf(firstLineCellPosition));
        style.put(BBSemanticsTable.StylesType.leftMargin, String.valueOf(cellPosition));
    	return style;	
    }
    
    
    private Styles  cellToIndent (Styles style){
		 int cellPosition;
		 if (style.contains(BBSemanticsTable.StylesType.leftMargin)){
			 cellPosition = Integer.valueOf((String)style.get((BBSemanticsTable.StylesType.leftMargin)))-1;
		 }else{
			 cellPosition = 1;
		 }
		 
		 int firstLineCellPosition;
		 if (style.contains(BBSemanticsTable.StylesType.firstLineIndent)){
			firstLineCellPosition = Integer.valueOf((String)style.get(BBSemanticsTable.StylesType.firstLineIndent))-cellPosition-1; 
		 }else{
		 	firstLineCellPosition = 1;
		 }
		 style.put(BBSemanticsTable.StylesType.firstLineIndent, String.valueOf(firstLineCellPosition));
		 style.put(BBSemanticsTable.StylesType.leftMargin, String.valueOf(cellPosition));
		 
		 return style;
	}


    public void closeEditStyle(String styleName){
    	int [] weights = container.getWeights();
    	editor.dispose();
    	table = new StyleTable(this, container);
    	displayTable(dm.getCurrent());
    	dm.getGroup().layout();
    	table.getTable().setSelection(lastSelection);
    	dm.setTabList();
    	
    	if(styleName != null)
    		table.setSelection(styleName);
    	
    	container.setWeights(weights);
    	container.layout();
    }
    
    public void apply(String item){
    	Styles style = semanticsTable.get(item);
    	
    	if(style != null){
    		boolean isBoxLine = style.getName().equals("boxline");
    		Message m = Message.createUpdateStyleMessage(style, dm.getText().isMultiSelected(), isBoxLine);
    		m.put("Style", style);
    		dm.dispatch(m);
    	}
    }
    
    public void remove(String item){
    	Styles style = semanticsTable.get("none");

    	if(style != null){
    		boolean isBoxline = item.equals("boxline");
    		Message m = Message.createUpdateStyleMessage(style, dm.getText().isMultiSelected(), isBoxline);
    		m.put("Style", style);
    		dm.dispatch(m);
    	}
    }
    
	//Values in the style object are saved using the liblouisutdml keyword
	//After saving to file, the style table is refreshed and the data type and value will correspond to the SWT object in the UI that applies them 
    protected void saveEditedStyle(Styles oldStyle, Styles newStyle){  	
    	ConfigFileHandler handler = new ConfigFileHandler(configFile);
    	Styles newStyleForView = cellToIndent(newStyle);
    	handler.updateStyle(newStyleForView);
    	semanticsTable.resetStyleTable(configFile);
    	dm.refresh();
    	closeEditStyle(newStyle.getName());
    }
    
    protected void saveBoxline(ArrayList<Styles>list){
    	ConfigFileHandler handler = new ConfigFileHandler(configFile);
    	for(int i = 0; i < list.size(); i++){
    		handler.updateStyle(list.get(i));
    		semanticsTable.resetStyleTable(configFile);
    	}
    	dm.refresh();
    	closeEditStyle("boxline");
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
        if(table.isVisible() || (!table.isVisible() && editor == null))
            return table.getGroup();
        else
            return editor.getGroup();
    }
    
    protected void closeTable(){
    	dm.toggleAttributeEditor();
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
    
    protected TextMapElement getCurrentItem(){
    	if(dm.getListSize() > 0)
    		return dm.getCurrent();
    	else
    		return null;
    }
}
