package org.brailleblaster.perspectives.braille.views.tree;

import org.brailleblaster.abstractClasses.AbstractView;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
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
	
	protected Manager manager;
	
	public TreeView(final Manager dm, Group documentWindow){
		super(documentWindow, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		this.group = documentWindow;
		this.tree = new Tree(view, SWT.VIRTUAL | SWT.NONE);
		manager = dm;
		
		view.setLayout(new FillLayout());
		view.getVerticalBar().dispose();
		view.getHorizontalBar().dispose();
		
		view.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				tree.setFocus();
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}
		});		
	}

	@Override
	protected void setViewData(Message message) {
		// TODO Auto-generated method stub	
	}
	
	public void adjustLayout(boolean fullSize){
		if(fullSize)
			setLayout(LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		else
			setLayout(LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, ADJUSTED_BOTTOM_MARGIN);
		
		group.layout();
	}
	
	public void dispose(){
		tree.removeAll();
		tree.dispose();
		view.dispose();
	}

}
