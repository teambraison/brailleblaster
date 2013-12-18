package org.brailleblaster.perspectives.braille;

import org.brailleblaster.perspectives.Perspective;
import org.brailleblaster.perspectives.braille.ui.BBToolBar;
import org.brailleblaster.perspectives.braille.ui.BrailleMenu;
import org.brailleblaster.wordprocessor.WPManager;

public class BraillePerspective extends Perspective{
	BBToolBar toolBar;
	
	public BraillePerspective(WPManager wp, Manager editor){
		perspectiveType = Manager.class;
		controller = editor;
		menu = new BrailleMenu(wp, editor);
		toolBar = new BBToolBar(wp.getShell(), wp, editor);
	}
	
    public BBToolBar getToolbar(){
    	return toolBar;
    }
    
    @Override
	public void dispose(){
    	menu.dispose();
    	//controller.dispose();
    	toolBar.dispose();
    }
}
