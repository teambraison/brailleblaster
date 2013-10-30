package org.brailleblaster.perspectives;

import nu.xom.Document;

import org.brailleblaster.perspectives.braille.BraillePerspective;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberPerspective;
import org.brailleblaster.wordprocessor.BBMenu;
import org.brailleblaster.wordprocessor.WPManager;

public abstract class Perspective {
	protected BBMenu menu;
	protected Class<?> perspectiveType;
	protected Controller controller;
	
	public BBMenu getMenu(){
		return menu;
	}
	
	public Controller getController(){
		return controller;
	}
	
	public void setController(Controller c){
		controller = c;
	}
	
	//Returns a perspective based on controllerClass parameter
	public static Perspective getPerspective(WPManager wp, Class<?> controllerClass, String fileName){
		if(controllerClass.equals(Manager.class)){
			Manager editor = new Manager(wp, fileName);
			return new BraillePerspective(wp, editor);
		}
		else if(controllerClass.equals(ImageDescriberController.class)){
			ImageDescriberController idc = new ImageDescriberController(wp, fileName);
			return new ImageDescriberPerspective(wp, idc);
		}
		return null;
	}
	
	//creates a perspective when switching between tabs for the currently open tab.  If no tabs are currently open, perspective takes null as the controller
	public static Perspective getDifferentPerspective(Perspective current, WPManager wp, Class<?> controllerClass, Document doc){
		if(controllerClass.equals(Manager.class)){
			if(doc != null){
				Manager editor = new Manager(wp, current.getController().getWorkingPath(), doc, wp.getFolder().getSelection()[0]);
				setCommonVariables(editor, current.getController());
				return new BraillePerspective(wp, editor);
			}
			else {
				return new BraillePerspective(wp, null);
			}
		}
		else if(controllerClass.equals(ImageDescriberController.class)){
			if(doc != null) {
				ImageDescriberController idc = new ImageDescriberController(wp, current.getController().getWorkingPath(), doc, wp.getFolder().getSelection()[0]);
				setCommonVariables(idc, current.getController());
				return new ImageDescriberPerspective(wp, idc);
			}
			else {
				return new ImageDescriberPerspective(wp, null);
			}
		}
		
		return null;
	}
	
	//restores a perspective when switching between tabs that have different perspectives
	public static Perspective restorePerspective(WPManager wp, Controller c){
		if(Manager.class.isInstance(c))
			return new BraillePerspective(wp, (Manager)c);
		else if(ImageDescriberController.class.isInstance(c))
			return new ImageDescriberPerspective(wp, (ImageDescriberController)c);
		
		return null;		
	}
	
	//returns a new controller when opening a document in an open tab containing an empty document
	public static Controller getNewController(WPManager wp, Class<?> perspectiveType, String fileName){
		if(perspectiveType.equals(Manager.class))
			return new Manager(wp, fileName);
		else if(perspectiveType.equals(ImageDescriberController.class))
			return new ImageDescriberController(wp, fileName);

		return null;
	}
	
	//returns the current perspectives class
	public Class<?> getType(){
		return perspectiveType;
	}
	
	//private method thats set common class variables when switching perspectives
	private static void setCommonVariables(Controller newController, Controller oldController){
		newController.workingFilePath = oldController.workingFilePath;
		newController.zippedPath = oldController.zippedPath;
		newController.currentConfig = oldController.currentConfig;
		newController.documentEdited = oldController.documentEdited;	
	}
	
	//disposes of menu and any SWT components outside the tab area
	public abstract void dispose();
	
}
