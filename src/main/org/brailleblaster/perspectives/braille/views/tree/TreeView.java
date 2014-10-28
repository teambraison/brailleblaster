package org.brailleblaster.perspectives.braille.views.tree;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import nu.xom.Element;
import nu.xom.Text;

import org.brailleblaster.BBIni;
import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.util.PropertyFileManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Tree;

public abstract class TreeView extends AbstractView implements BBTree{
	private final static int LEFT_MARGIN = 0;
	private final static int RIGHT_MARGIN = 15;
	private final static int TOP_MARGIN = 0;
	private final static int BOTTOM_MARGIN = 100;
	private final static int ADJUSTED_BOTTOM_MARGIN = 49;
	
	protected Tree tree;
	private SashForm sash;
	
	public TreeView(final Manager manager, SashForm sash){
		super(manager, sash);
		this.sash = sash;
		tree = new Tree(sash, SWT.VIRTUAL | SWT.BORDER);	
		setLayout(tree, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
	}
	
	public static BBTree loadTree(Manager m, SashForm sash){
		PropertyFileManager prop = BBIni.getPropertyFileManager();
		String tree = prop.getProperty("tree");
		if(tree == null){
			prop.save("tree", BookTree.class.getCanonicalName().toString());
			return new BookTree(m, sash);
		}
		else {			
			try {
				Class<?> clss = Class.forName(tree);
				return TreeView.createTree(clss, m, sash);	
			} catch (ClassNotFoundException e) {		
				logger.error("Class Not Found Exception", e);
			} 
		}
		
		return null;
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
		
		sash.layout();
	}
	
	@Override
	public void dispose(){
		tree.removeAll();
		tree.dispose();
	}
	
	public static BBTree createTree(Class<?>clss, Manager manager, SashForm sashform){
		try {
			Constructor<?> constructor = clss.getConstructor(new Class[]{Manager.class, SashForm.class});
			return (BBTree)constructor.newInstance(manager, sashform);
		} catch (NoSuchMethodException e) {
			logger.error("No Such Method Exception", e);
		} catch (SecurityException e) {
			logger.error("Security Exception", e);
		} catch (InstantiationException e) {
			logger.error("Instantiation Exception", e);
		} catch (IllegalAccessException e) {
			logger.error("Illegal Access Exception", e);
		} catch (IllegalArgumentException e) {
			logger.error("Illegal Argument Exception", e);
		} catch (InvocationTargetException e) {
			logger.error("Invocation Exception", e);
		}
		
		return null;
	}
	
	protected Text findPageNode(Element e){
		if(e.getChildCount() > 1){
			if(e.getChild(1) instanceof Element && ((Element)e.getChild(1)).getLocalName().equals("brl")){
				Element brlNode = (Element)e.getChild(1);
				if(brlNode.getChild(0) instanceof Element && ((Element)brlNode.getChild(0)).getLocalName().equals("span")){
					Element spanNode = (Element)brlNode.getChild(0);
					if(spanNode.getChild(0) instanceof Text){
						return (Text)spanNode.getChild(0);
					}
				}	
			}
		}
		return null;
	}
}
