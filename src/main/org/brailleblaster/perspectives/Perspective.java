package org.brailleblaster.perspectives;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nu.xom.Document;

import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.perspectives.braille.BraillePerspective;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberPerspective;
import org.brailleblaster.perspectives.webView.WebViewController;
import org.brailleblaster.perspectives.webView.WebViewPerspective;
import org.brailleblaster.wordprocessor.BBMenu;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.widgets.TabItem;

public abstract class Perspective {
	private static final Map<Class<?>, Class<?>> PERSPECTIVE_MAP;
	static {
		HashMap<Class<?>, Class<?>> temp = new HashMap<Class<?>, Class<?>>();
		temp.put(Manager.class, BraillePerspective.class);
		temp.put(ImageDescriberController.class, ImageDescriberPerspective.class);
		temp.put(WebViewController.class, WebViewPerspective.class);
		PERSPECTIVE_MAP = Collections.unmodifiableMap(temp);
	}
	
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
		Controller c = instantiateController(wp, controllerClass, fileName);
		return instantiatePerspective(wp, c, controllerClass);
	}
	
	//creates a perspective when switching between tabs for the currently open tab.  If no tabs are currently open, perspective takes null as the controller
	public static Perspective getDifferentPerspective(Perspective current, WPManager wp, Class<?> controllerClass, Document doc){
		if(doc != null){
			Controller c = instantiateController(current, wp, controllerClass, doc, current.getController().arch);
			return instantiatePerspective(wp, c, controllerClass);
		}
		else {
			return instantiatePerspective(wp, null, controllerClass);
		}
	}
	
	//restores a perspective when switching between tabs that have different perspectives
	public static Perspective restorePerspective(WPManager wp, Controller c){
		return instantiatePerspective(wp, c, c.getClass());	
	}
	
	//returns a new controller when opening a document in an open tab containing an empty document
	public static Controller getNewController(WPManager wp, Class<?> perspectiveType, String fileName){	
		return instantiateController(wp, perspectiveType, fileName);
	}
	
	//returns the current perspectives class
	public Class<?> getType(){
		return perspectiveType;
	}
	
	private static Controller instantiateController(WPManager wp, Class<?>controllerClass, String fileName){
		try {
			Constructor<?> controller = controllerClass.getConstructor(new Class[]{WPManager.class, String.class});
			return (Controller)controller.newInstance(wp, fileName);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {		
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static Controller instantiateController(Perspective current, WPManager wp, Class<?>controllerClass, Document doc, Archiver arch){
		try {
			Constructor<?> controller = controllerClass.getConstructor(new Class[]{WPManager.class, Document.class, TabItem.class, Archiver.class});
			return (Controller)controller.newInstance(wp, doc, wp.getFolder().getSelection()[0], arch);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {		
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static Perspective instantiatePerspective(WPManager wp, Controller c, Class<?>controllerClass){
		try {	
			Constructor<?> perspective = PERSPECTIVE_MAP.get(controllerClass).getConstructor(new Class[]{WPManager.class, controllerClass});
			return (Perspective)perspective.newInstance(wp, c);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;	
	}
	//disposes of menu and any SWT components outside the tab area
	public abstract void dispose();
	
}
