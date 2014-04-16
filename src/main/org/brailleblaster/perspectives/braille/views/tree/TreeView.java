package org.brailleblaster.perspectives.braille.views.tree;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;

public abstract class TreeView extends AbstractView implements BBTree{
	private final static int LEFT_MARGIN = 0;
	private final static int RIGHT_MARGIN = 15;
	private final static int TOP_MARGIN = 0;
	private final static int BOTTOM_MARGIN = 100;
	private final static int ADJUSTED_BOTTOM_MARGIN = 49;
	
	protected Tree tree;
	private Group group;
	
	public TreeView(final Manager manager, Group documentWindow){
		super(manager, documentWindow);
		group = documentWindow;
		tree = new Tree(group, SWT.VIRTUAL | SWT.BORDER);	
		setLayout(tree, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
	}

	@Override
	protected void setViewData(Message message) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void adjustLayout(boolean fullSize){
		if(fullSize)
			setLayout(tree, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		else
			setLayout(tree, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, ADJUSTED_BOTTOM_MARGIN);
		
		group.layout();
	}
	
	@Override
	public void dispose(){
		tree.removeAll();
		tree.dispose();
	}
	
	public static BBTree createTree(Class<?>clss, Manager manager, Group group){
		try {
			Constructor<?> constructor = clss.getConstructor(new Class[]{Manager.class, Group.class});
			return (BBTree)constructor.newInstance(manager, group);
		} catch (NoSuchMethodException e) {
			logger.log(Level.SEVERE, "No Such Method Exception", e);
		} catch (SecurityException e) {
			logger.log(Level.SEVERE, "Security Exception", e);
		} catch (InstantiationException e) {
			logger.log(Level.SEVERE, "Instantiation Exception", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "Illegal Access Exception", e);
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "Illegal Argument Exception", e);
		} catch (InvocationTargetException e) {
			logger.log(Level.SEVERE, "Invocation Exception", e);
		}
		
		return null;
	}
}
