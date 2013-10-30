package org.brailleblaster.perspectives.imageDescriber;

import org.brailleblaster.perspectives.Perspective;
import org.brailleblaster.perspectives.imageDescriber.UIComponents.ImageDescriberMenu;
import org.brailleblaster.wordprocessor.WPManager;

public class ImageDescriberPerspective extends Perspective{
	
	public ImageDescriberPerspective(WPManager wp, ImageDescriberController controller){
		perspectiveType = ImageDescriberController.class;
		this.controller = controller;
		this.menu = new ImageDescriberMenu(wp, controller);
	}
	
	@Override
	public void dispose() {
		menu.dispose();
		//controller.dispose();
	}
}
