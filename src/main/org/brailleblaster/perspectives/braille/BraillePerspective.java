package org.brailleblaster.perspectives.braille;

import org.brailleblaster.perspectives.Perspective;
import org.brailleblaster.perspectives.braille.ui.BrailleToolBar;
import org.brailleblaster.perspectives.braille.ui.BrailleMenu;
import org.brailleblaster.wordprocessor.WPManager;

public class BraillePerspective extends Perspective{
	
	public BraillePerspective(WPManager wp, Manager editor){
		perspectiveType = Manager.class;
		controller = editor;
		menu = new BrailleMenu(wp, editor);
		toolbar = new BrailleToolBar(wp.getShell(), wp, editor);
	}
    
    @Override
	public void dispose(){
    	menu.dispose();
    	//controller.dispose();
    	toolbar.dispose();
    }
}
