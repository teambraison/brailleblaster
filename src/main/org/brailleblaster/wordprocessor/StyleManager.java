package org.brailleblaster.wordprocessor;

public class StyleManager {
    CreateStyle cs;
   // SelectStyle ss;
    DocumentManager dm;
	
	public StyleManager(DocumentManager dm){
		this.dm = dm;
        cs = new CreateStyle(dm);
        //ss = new SelectStyle(dm);
	}
	
    void createStyle(){
    	cs.open();
    }
    
    void selectStyle(){
    	//ss.open();
    }
}
