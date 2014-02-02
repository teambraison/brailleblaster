package org.brailleblaster.perspectives.braille.stylepanel;

import java.util.Set;

import nu.xom.Element;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.mapping.TextMapElement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class StyleTable {
	private final static int LEFT_MARGIN = 0;
	private final static int RIGHT_MARGIN = 15;
	private final static int TOP_MARGIN = 50;
	private final static int BOTTOM_MARGIN = 100;
	
	private Group group;
	private Table t;
	private Font initialFont;
	
	private StyleManager sm;
	private Button restoreButton, newButton, editButton, deleteButton, applyButton;
	
	public StyleTable(final StyleManager sm, Group documentWindow){
		LocaleHandler lh = new LocaleHandler();
		this.sm = sm;
		this.group = new Group(documentWindow, SWT.FILL | SWT.BORDER);
		setLayoutData(this.group, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		this.group.setLayout(new FormLayout());
		this.group.setVisible(false);
		
		Listener resizeListener = new Listener(){
			@Override
			public void handleEvent(Event e) {
				checkFontSize((Button)e.widget);
			}		
		};
		
		restoreButton = new Button(this.group, SWT.CHECK);
		restoreButton.setText(lh.localValue("restore"));
		setLayoutData(restoreButton, 1, 100, 0, 5);
		
		this.t = new Table(this.group, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		setLayoutData(this.t, 0, 100, 5, 90);
	    
		TableColumn tc1 = new TableColumn(this.t, SWT.CENTER);
		tc1.setWidth(0);
		tc1.setResizable(false);
		
		final TableColumn tc2 = new TableColumn(this.t, SWT.CENTER);   
	    tc2.setText(lh.localValue("styles"));
	   
	    this.t.setLinesVisible(true);
	    this.t.setHeaderVisible(true);	
	   	
	    newButton = new Button(this.group, SWT.NONE);
	    newButton.setText(lh.localValue("new"));
	    setLayoutData(newButton, 0, 25, 90, 100);
	    initialFont = newButton.getFont();
	    newButton.addListener(SWT.Resize, resizeListener);
	    
	    editButton = new Button(this.group, SWT.NONE);
	   	editButton.setText(lh.localValue("edit"));
	   	setLayoutData(editButton, 25, 50, 90, 100);
	   	editButton.addListener(SWT.Resize, resizeListener);
	   	
	   	deleteButton = new Button(this.group, SWT.NONE);
	   	deleteButton.setText(lh.localValue("delete"));
	   	setLayoutData(deleteButton, 50, 75, 90, 100);
	   	deleteButton.addListener(SWT.Resize, resizeListener);
	   	
	    applyButton = new Button(this.group, SWT.NONE);
	    applyButton.setText(lh.localValue("apply"));
	    setLayoutData(applyButton, 75, 100, 90, 100);
	    applyButton.addListener(SWT.Resize, resizeListener);
	    
	    group.pack();
	    tc2.setWidth(group.getClientArea().width);
	   
	    t.getHorizontalBar().dispose();
		
		this.group.addListener(SWT.Resize, new Listener(){
			@Override
			public void handleEvent(Event e) {
				 tc2.setWidth(group.getClientArea().width);
			}
			
		});
		
		this.t.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent e) {
				if(Character.isLetter(e.keyCode)){
					int loc = searchTree((char)e.keyCode);
					if(loc != -1)
						t.setSelection(loc);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub			
			}
		});
	
	   	populateTable(sm.getKeySet());
	   	initializeListeners();
	}
	
	private void initializeListeners(){
		restoreButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				LocaleHandler lh = new LocaleHandler();
				MessageBox mb = new MessageBox(group.getShell(), SWT.OK | SWT.CANCEL);
				mb.setText(lh.localValue("restoreMB"));
				mb.setMessage(lh.localValue("restoreMBMessage"));
				int choice = mb.open();
				
				if(choice == SWT.OK){
					sm.restoreDefaults();
					restoreButton.setSelection(false);
				}
				else
					restoreButton.setSelection(false);
			}	
		});
		
		newButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				sm.openNewStyleTable();
			}
		});
		
		editButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {	
				sm.openEditStyle();
			}		
		});
		
		deleteButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteStyle();
			}			
		});
		
		applyButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				sm.apply(t.getSelection()[0].getText(1));
			}		
		});
	}
	
	private void setLayoutData(Control c, int left, int right, int top, int bottom){
		FormData location = new FormData();
		
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		
		c.setLayoutData(location);
	}
	
	public void showTable(TextMapElement item){
		group.setVisible(true);
		t.setFocus();
		if(item != null){
			setSelection(item);
		}
		else
			t.setSelection(0);
	}
	
	public void hideTable(){
		group.setVisible(false);
	}
	
	private int searchTree(String text){
		for(int i = 0; i < t.getItemCount(); i++){
			if(t.getItem(i).getText(1).equals(text)){
				return i;
			}
		}
		return 0;
	}
	
	private int searchTree(char c){
		for(int i = 0; i < t.getItemCount(); i++){
			if(t.getItem(i).getText(1).charAt(0) == c)
				return i;
		}
		return -1;
	}
	
	public void setSelection(TextMapElement item){
		Element parent = (Element)item.n.getParent();
		while(sm.getSemanticsTable().getSemanticTypeFromAttribute(parent) == null || sm.getSemanticsTable().getSemanticTypeFromAttribute(parent).equals("action")){
			parent = (Element)parent.getParent();
		}
		String text = sm.getSemanticsTable().getKeyFromAttribute(parent);
		t.setSelection(searchTree(text));
	}
	
	public void setSelection(String style){
		for(int i = 0; i < t.getItemCount(); i++){
			if(t.getItem(i).getText(1).equals(style)){
				t.setSelection(i);
				break;
			}
		}
	}
	
    private void populateTable(Set<String> list){  	
    	for(String s : list){
    		if(!s.equals("document"))
    			addTableItem(s);
    	}
    	
    }
    
    private void deleteStyle(){
    	LocaleHandler lh = new LocaleHandler();
    	MessageBox mb = new MessageBox(group.getShell(), SWT.OK | SWT.CANCEL);
		mb.setText(lh.localValue("deleteMB"));
		mb.setMessage(lh.localValue("deleteMBMessage"));
		
		int open = mb.open();
		
		if(open == SWT.OK){
			sm.deleteStyle(t.getSelection()[0].getText(1));
			t.remove(t.getSelectionIndex());
		}
    }
    
    public void resetTable(String configFile){
    	t.removeAll();
    	populateTable(sm.getKeySet());
    }
    
    private void checkFontSize(Button b){
    	Font newFont = (Font)b.getData();
    	
    	b.setFont(initialFont);
    	int charWidth = getFontWidth(b);
    	int stringWidth = b.getText().length() * charWidth;
    	FontData[] fontData = b.getFont().getFontData();
    	 
    	int Ssize = fontData[0].getHeight();
    	if(stringWidth > b.getBounds().width){
    		while(stringWidth > b.getBounds().width && Ssize > 0){
    			Ssize = fontData[0].getHeight() - 1;
    			fontData[0].setHeight(Ssize);
    			if(newFont != null && !newFont.isDisposed())
    				newFont.dispose();
    			newFont = new Font(Display.getCurrent(), fontData[0]);
    			b.setFont(newFont);
    			b.setData(newFont);
    			charWidth = getFontWidth(b);
    			stringWidth = b.getText().length() * charWidth;
    		}
    	}
    }
    
    protected int getFontWidth(Button b){
		GC gc = new GC(b);
		FontMetrics fm = gc.getFontMetrics();
		gc.dispose();
		return fm.getAverageCharWidth();
	}
    
    private void addTableItem(String item){
    	TableItem tItem = new TableItem(t, SWT.CENTER);
    	tItem.setText(new String[]{"", item});
    }
    
    public boolean isVisible(){
        if(!t.isDisposed() && t.isVisible())
            return true;
        else
            return false;
    }
    
    public Table getTable(){
    	return t;
    }
    
    public Group getGroup(){
    	return group;
    }
    
    protected void dispose(){
    	t.dispose();
    	disposeFont(newButton);
    	disposeFont(editButton);
    	disposeFont(deleteButton);
    	disposeFont(applyButton);
    	group.dispose();
    	
    }
    
    private void disposeFont(Button b){
    	Font f = (Font)b.getData();
    	if(f != null && !f.isDisposed())
    		f.dispose();
    }
}
