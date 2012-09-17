package org.brailleblaster.wordprocessor;

class StyleManager{
	
    StylePanel sp;
    DocumentManager dm;
	
	public StyleManager(DocumentManager dm){
		this.dm = dm;
        sp = new StylePanel(this);
	}

    void createStyle(String styleName){
    	EditStyle es = new EditStyle(this);
    	es.create(styleName);
    }
    
    void modifyStyle(Style style){
    	EditStyle es = new EditStyle(this);
    	es.modify(style);
    }
    
    void stylePanel(){
    	sp.open();
    }
    
    void readStyleFiles(String styleName){
    	sp.readStyleFiles(styleName);
    }
}