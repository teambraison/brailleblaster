package org.brailleblaster.perspectives.braille.spellcheck;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class SpellCheckView {
	protected Shell shell;
    protected Text text;
    protected org.eclipse.swt.widgets.List suggestionBox;
    protected Group group, suggestionGroup;
    protected Button replace, ignore, ignoreAll, add;
	private SpellCheckManager m;
	private int lastItem;
	private boolean locked = false;
	private LocaleHandler lh;
	
	SpellCheckView(Display display, final SpellCheckManager m){
		this.m = m;
		lh = new LocaleHandler();
		shell = new Shell(display, SWT.CLOSE);
    	shell.setText(lh.localValue("spellCheck"));
    	setShellScreenLocation(shell.getDisplay(), shell);
    	shell.setLayout(new FormLayout());
    	
    	shell.addListener(SWT.Close, new Listener(){
			@Override
			public void handleEvent(Event e) {
				close();
			}
    	});
    	
    	shell.addListener(SWT.Deactivate, new Listener(){
			@Override
			public void handleEvent(Event e) {
				if(!locked){
					shell.setMinimized(true);
					shell.setFocus();
					shell.setMinimized(false);
				}
			}		
    	});
    	
    	text = new Text(shell, SWT.BORDER);
    	setLayout(text, 5, 95, 5, 15);
    	text.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				suggestionBox.deselect(suggestionBox.getSelectionIndex());
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub			
			}
    	});
    	
    	group = new Group(shell, SWT.NONE);
    	group.setLayout(new FormLayout());
    	setLayout(group, 65,95,17,75);
    	
    	replace = new Button(group, SWT.PUSH);
    	replace.setText(lh.localValue("spellReplace"));
    	setLayout(replace, 0,100,0,25);
    	replace.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {			
				if(suggestionBox.getSelectionCount() == 0){
					m.replace(text.getText());
					m.checkWord();
				}
				else if(!(suggestionBox.getItemCount() == 1 && suggestionBox.getItem(0).equals(lh.localValue("noSuggestion")))){
					m.replace(suggestionBox.getSelection()[0]);
					m.checkWord();
				}
				else{
					locked = true;
					new Notify(lh.localValue("suggestionError"));
					locked = false;
				}
			}    		
    	});
    	
    	ignore = new Button(group, SWT.PUSH);
    	ignore.setText(lh.localValue("spellIgnore"));
    	setLayout(ignore, 0,100,25,50);
    	ignore.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				m.checkWord();
			}  		
    	});
    	
    	ignoreAll = new Button(group, SWT.PUSH);
    	ignoreAll.setText(lh.localValue("spellIgnoreAll"));
    	setLayout(ignoreAll, 0,100,50,75);
    	ignoreAll.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub			
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				m.ignoreWord(text.getText());
				m.checkWord();
			}  		
    	});
    	
    	add = new Button(group, SWT.NONE);
    	add.setText(lh.localValue("spellAdd"));
    	setLayout(add, 0,100, 75, 100);
    	add.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				m.addWord(text.getText());
				m.checkWord();
			}  		
    	});
    	
    	suggestionGroup = new Group(shell, SWT.NONE);
    	suggestionGroup.setLayout(new FormLayout());
    	suggestionGroup.setText(lh.localValue("Suggestions"));
    	setLayout(suggestionGroup, 5, 64, 17, 75);
    	
    	suggestionBox = new org.eclipse.swt.widgets.List(suggestionGroup, SWT.MULTI | SWT.BORDER);
    	setLayout(suggestionBox, 0, 100, 0, 100);
    	suggestionBox.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				suggestionBox.setSelection(lastItem);
			}

			@Override
			public void focusLost(FocusEvent e) {
				lastItem = suggestionBox.getSelectionIndex();
			} 		
    	});
    	
    	Control[] tabList = new Control[] { suggestionGroup, group, text};
    	shell.setTabList(tabList);
    	
    	shell.open();
    	
    	checkFontSize(replace);
    	checkFontSize(ignore);
    	checkFontSize(ignoreAll);
    	checkFontSize(add);
	}
	
    private void setLayout(Control c, int left, int right, int top, int bottom){
    	FormData data = new FormData();
    	data.left = new FormAttachment(left);
    	data.right = new FormAttachment(right);
    	data.top = new FormAttachment(top);
    	data.bottom = new FormAttachment(bottom);
    	c.setLayoutData(data);
    }
    
    private void setShellScreenLocation(Display display, Shell shell){
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		shell.setBounds(rect.x + (rect.width / 4), rect.y + (rect.height / 4), bounds.width / 4, bounds.height / 4);
	}
    
    protected void setWord(String word, String [] suggestions){
    	text.setText(word);
    	
    	suggestionBox.removeAll();
    	for(int i = 0; i < suggestions.length; i++){
    		suggestionBox.add(suggestions[i]);
    	}
    	suggestionBox.setFocus();
    	suggestionBox.select(0);
    }
    
    protected void close(){
    	shell.dispose();
    	m.closeSpellChecker();
    }
    
    private void checkFontSize(Button b){
    	int charWidth = getFontWidth(b);
    	int stringWidth = b.getText().length() * charWidth;
    	FontData[] fontData = b.getFont().getFontData();
    	 
    	if(stringWidth > b.getBounds().width){
    		while(stringWidth > b.getBounds().width){
    			int Ssize = fontData[0].getHeight() - 1;
    			fontData[0].setHeight(Ssize);
    			b.setFont(new Font(Display.getCurrent(), fontData[0]));
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
}
