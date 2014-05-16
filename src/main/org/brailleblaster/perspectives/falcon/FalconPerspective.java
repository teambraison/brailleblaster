package org.brailleblaster.perspectives.falcon;

import org.brailleblaster.perspectives.Perspective;
import org.brailleblaster.perspectives.imageDescriber.UIComponents.ImageDescriberToolBar;
import org.brailleblaster.wordprocessor.WPManager;

public class FalconPerspective extends Perspective {
	FalconToolBar toolBar;
	public FalconPerspective(WPManager wp, FalconController controller){
		perspectiveType = FalconController.class;
		this.controller = controller;
		this.menu = new FalconMenu(wp, controller);
		toolBar = new FalconToolBar(wp.getShell(), wp, controller);
	}
	
	@Override
	public void dispose() {
		menu.dispose();
		toolBar.dispose();
	}

}
