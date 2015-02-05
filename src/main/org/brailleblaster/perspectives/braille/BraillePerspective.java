package org.brailleblaster.perspectives.braille;

import org.brailleblaster.perspectives.Perspective;
import org.brailleblaster.perspectives.braille.ui.BrailleToolBar;
import org.brailleblaster.perspectives.braille.ui.BrailleMenu;
import org.brailleblaster.wordprocessor.WPManager;

public class BraillePerspective extends Perspective{
	BrailleToolBar toolBar;
	
	public BraillePerspective(WPManager wp, Manager editor){
		perspectiveType = Manager.class;
		controller = editor;
		menu = new BrailleMenu(wp, editor);
		toolBar = new BrailleToolBar(wp.getShell(), wp, editor);
	}
	
    public BrailleToolBar getToolbar(){
    	return toolBar;
    }
    
    @Override
	public void dispose(){
    	menu.dispose();
    	//controller.dispose();
    	toolBar.dispose();
    }
}
