package org.brailleblaster.stylePanel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Element;

import org.brailleblaster.BBIni;
import org.brailleblaster.mapping.TextMapElement;
import org.brailleblaster.util.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class StyleTable {
	private final static int LEFT_MARGIN = 0;
	private final static int RIGHT_MARGIN = 15;
	private final static int TOP_MARGIN = 70;
	private final static int BOTTOM_MARGIN = 100;
	
	private Group group;
	private Table t;
	private Logger logger = BBIni.getLogger();
	private FileUtils fu;
	private StyleManager sm;
	private Button applyButton;
	
	public StyleTable(final StyleManager sm, Group documentWindow){
		this.fu = new FileUtils();
		this.sm = sm;
		this.group = new Group(documentWindow, SWT.FILL | SWT.BORDER);
		setLayoutData(this.group, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
		this.group.setLayout(new FormLayout());
		this.group.setVisible(false);
		
		this.t = new Table(this.group, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		setLayoutData(this.t, 0, 100, 0, 90);
	    
		TableColumn tc1 = new TableColumn(this.t, SWT.CENTER);
		tc1.setWidth(0);
		tc1.setResizable(false);
		
		final TableColumn tc2 = new TableColumn(this.t, SWT.CENTER);   
	    tc2.setText("Styles");
	   
	    this.t.setLinesVisible(true);
	    this.t.setHeaderVisible(true);	
	  
	//    Button newButton = new Button(this.group, SWT.NONE);
	//    newButton.setText("New");
	//   setLayoutData(newButton, 0, 50, 90, 100);
	    
	    applyButton = new Button(this.group, SWT.NONE);
	    applyButton.setText("Apply");
	    setLayoutData(applyButton, 0, 100, 90, 100);
		
	    group.pack();
	    tc2.setWidth(group.getClientArea().width);
	    t.getHorizontalBar().dispose();
	    
		this.group.addListener(SWT.Resize, new Listener(){
			@Override
			public void handleEvent(Event e) {
				 tc2.setWidth(group.getClientArea().width);
			}
			
		});
	
	   	populateTable();
	   	initializeListeners();
	}
	
	private void initializeListeners(){
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
	
	public void setSelection(TextMapElement item){
		Element parent = (Element)item.n.getParent();
		while(sm.getSemanticsTable().getSemanticTypeFromAttribute(parent) == null || sm.getSemanticsTable().getSemanticTypeFromAttribute(parent).equals("action")){
			parent = (Element)parent.getParent();
		}
		String text = sm.getSemanticsTable().getKeyFromAttribute(parent);
		t.setSelection(searchTree(text));
	}
	
    private void populateTable(){
    	String defaultName = BBIni.getDefaultConfigFile();
    	String fullPath = fu.findInProgramData ("liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + defaultName);
    	String currentLine;
    	
    	try {
    		FileReader file = new FileReader(fullPath);
			BufferedReader reader = new BufferedReader(file);
			
			while((currentLine = reader.readLine()) != null){
				if(currentLine.contains("style")){
					if(currentLine.length() >= 5 && currentLine.substring(0, 5).equals("style")){
						String item = currentLine.substring(6, currentLine.length()).trim();
						if(!item.equals("document"))
							addTableItem(item);
					}
				}
			}
			reader.close();
    	}
    	catch(FileNotFoundException e){
    		e.printStackTrace();
    		logger.log(Level.SEVERE, "File Not Found Exception", e);
    	}
    	catch(IOException e){
    		e.printStackTrace();
    		logger.log(Level.SEVERE, "File Not Found Exception", e);
    	}
    }
    
    private void addTableItem(String item){
    	TableItem tItem = new TableItem(t, SWT.CENTER);
    	tItem.setText(new String[]{"", item});
    }
    
    public boolean isVisible(){
    	return t.isVisible();
    }
    
    public Table getTable(){
    	return t;
    }
    
    public Group getGroup(){
    	return group;
    }
}
