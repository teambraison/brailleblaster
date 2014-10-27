package org.brailleblaster.settings.ui;

import java.util.HashMap;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.settings.SettingsManager;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class PagePropertiesTab {
	HashMap<String, String>settingsMap;
	SettingsManager sm;
	TabItem item;
	Composite  group;
	
	Group sizeGroup, marginGroup, pageGroup, buttonGroup; 
	Label pageSizeLabel, widthLabel, heightLabel, linesPerPageLabel, cellsPerLineLabel, minLinesLabel, minCellsLabel, marginTopLabel, marginBottomLabel, marginLeftLabel, marginRightLabel;
	
	Combo pageTypes;
	Text widthBox, heightBox, linesBox, cellsBox, minLinesBox, minCellsBox, marginTopBox, marginLeftBox, marginRightBox, marginBottomBox;
	Button okButton, cancelButton;
	
	boolean listenerLocked;
	LocaleHandler lh;
	
	PagePropertiesTab(TabFolder folder, final SettingsManager sm, HashMap<String, String>settingsMap){
		lh = new LocaleHandler();
		this.sm = sm;
		this.settingsMap = settingsMap;
		listenerLocked = false;
		item = new TabItem(folder, 0);	
		item.setText(lh.localValue("pageProperties"));
		
		group = new Composite(folder, 0);
		group.setLayout(new FormLayout());
		item.setControl(group);
		setFormLayout(group, 0, 100, 0, 60);
		
		sizeGroup = new Group(group, SWT.BORDER);
		sizeGroup.setText(lh.localValue("pageSize"));
		sizeGroup.setLayout(new FillLayout());
		setFormLayout(sizeGroup, 0, 100, 0, 60);
		
		pageGroup = new Group(sizeGroup, 0);
		pageGroup.setLayout(new GridLayout(2, true));
		
		pageSizeLabel = new Label(pageGroup, 0);
		pageSizeLabel.setText(lh.localValue("pageSize"));
		setGridData(pageSizeLabel);
		
		pageTypes = new Combo(pageGroup, SWT.NONE);
		setStandardPages();
		setDefault();
		setGridData(pageTypes);
		
		widthLabel = new Label(pageGroup, 0);
		widthLabel.setText(lh.localValue("width"));
		widthBox  = new Text(pageGroup, SWT.BORDER);
		addDoubleListener(widthBox);
		setGridData(widthBox);
		setValue(widthBox, "paperWidth");
		
		heightLabel = new Label(pageGroup, 0);
		heightLabel.setText(lh.localValue("height"));
		heightBox = new Text(pageGroup, SWT.BORDER);
		addDoubleListener(heightBox);
		setGridData(heightBox);
		setValue(heightBox, "paperHeight");
		
		linesPerPageLabel = new Label(pageGroup, 0);
		linesPerPageLabel.setText(lh.localValue("linesPerPage"));
		
		linesBox = new Text(pageGroup, SWT.BORDER);
		setGridData(linesBox);
		linesBox.setText((String.valueOf(calculateLinesPerPage(Double.valueOf(settingsMap.get("paperHeight"))))));
		linesBox.setEditable(false);
		
		cellsPerLineLabel = new Label(pageGroup, 0);
		cellsPerLineLabel.setText(lh.localValue("cellsPerLine"));
		
		cellsBox = new Text(pageGroup, SWT.BORDER);
		setGridData(cellsBox);
		cellsBox.setText(String.valueOf(calculateCellsPerLine(Double.valueOf(settingsMap.get("paperWidth")))));
		cellsBox.setEditable(false);
		
		//////////////
		// Min Values
		
			minLinesLabel = new Label(pageGroup, 0);
			minLinesLabel.setText(lh.localValue("minLinesPerPage"));
			
			minLinesBox = new Text(pageGroup, SWT.BORDER);
			setGridData(minLinesBox);
			minLinesBox.setText( "6" );
		

		// Min Values
		//////////////
		
		
		marginGroup = new Group(group, SWT.BORDER);
		marginGroup.setLayout(new GridLayout(2, true));
		marginGroup.setText(lh.localValue("margins"));
		setFormLayout(marginGroup, 0, 100, 60, 100);
		
		marginTopLabel = new Label(marginGroup, 0);
		marginTopLabel.setText(lh.localValue("topMargin"));
		marginTopBox = new Text(marginGroup, SWT.BORDER);
		addDoubleListener(marginTopBox);
		setGridData(marginTopBox);
		setValue(marginTopBox, "topMargin");
		addMarginListener(marginTopBox, "topMargin");
		
		marginBottomLabel = new Label(marginGroup, 0);
		marginBottomLabel.setText(lh.localValue("bottomMargin"));
		marginBottomBox = new Text(marginGroup, SWT.BORDER);
		addDoubleListener(marginBottomBox);
		setGridData(marginBottomBox);
		setValue(marginBottomBox, "bottomMargin");
		addMarginListener(marginBottomBox, "bottomMargin");
		
		marginLeftLabel= new Label(marginGroup, 0);
		marginLeftLabel.setText(lh.localValue("leftMargin"));
		marginLeftBox = new Text(marginGroup, SWT.BORDER);
		addDoubleListener(marginLeftBox);
		setGridData(marginLeftBox);
		setValue(marginLeftBox, "leftMargin");
		addMarginListener(marginLeftBox, "leftMargin");
		
		marginRightLabel = new Label(marginGroup, 0);
		marginRightLabel.setText(lh.localValue("rightMargin"));
		marginRightBox = new Text(marginGroup, SWT.BORDER);
		addDoubleListener(marginRightBox);
		setGridData(marginRightBox);
		setValue(marginRightBox, "rightMargin");
		addMarginListener(marginRightBox, "rightMargin");
		
		Control [] tabList = {sizeGroup, marginGroup};
		group.setTabList(tabList);
		
		if(widthBox.getText().length() > 0 || heightBox.getText().length() > 0)
			checkStandardSizes();
			
		addListeners();
	}
	
	private void addDoubleListener(final Text t){
		t.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if(!Character.isDigit(e.character)  && e.keyCode != SWT.BS && e.keyCode != SWT.DEL && e.keyCode != SWT.ARROW_LEFT && e.keyCode != SWT.ARROW_RIGHT){
					if(e.character != '.' || (e.character == '.' && t.getText().contains(".")))
						e.doit = false;
				}
			}	
		});
	}
	
	private void addMarginListener(final Text t, final String type){
		if(type.equals("leftMargin") || type.equals("rightMargin")){
			t.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent e) {
					settingsMap.put(type, getStringValue(t));
					cellsBox.setText(String.valueOf(calculateCellsPerLine(Double.valueOf(widthBox.getText()))));
					linesBox.setText(String.valueOf(calculateLinesPerPage(Double.valueOf(heightBox.getText()))));
				}		
			});
		}
		else {
			t.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent e) {
					settingsMap.put(type, getStringValue(t));
					cellsBox.setText(String.valueOf(calculateCellsPerLine(Double.valueOf(widthBox.getText()))));
					linesBox.setText(String.valueOf(calculateLinesPerPage(Double.valueOf(heightBox.getText()))));
				}		
			});
		}
	}
	
	private void setStandardPages(){
		Page [] temp = sm.getStandardSizes();
		for(int i = 0; i < temp.length; i++){
			if(sm.isMetric())
				pageTypes.add(temp[i].type + " (" + temp[i].mmWidth + ", " + temp[i].mmHeight + ")");
			else
				pageTypes.add(temp[i].type + " (" + temp[i].width + ", " + temp[i].height + ")");
		}
	}
	
	private int searchList(char c){
		for(int i = 0; i < pageTypes.getItemCount(); i++){
			if(pageTypes.getItem(i).toLowerCase().charAt(0) == c)
				return i;
		}
		
		return -1;
	}
	
	private void setGridData(Control c){
		 GridData gridData = new GridData();
         gridData.horizontalAlignment = GridData.FILL;
         gridData.verticalAlignment = GridData.FILL;
         gridData.grabExcessHorizontalSpace = true;
         c.setLayoutData(gridData);
	}
	
	private void addListeners(){
		widthBox.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(getDoubleValue(widthBox) == 0){
					if(e.keyCode == SWT.TAB){
						new Notify(lh.localValue("widthZero"));
						e.doit = false;
					}
				}
			}
		});
		
		heightBox.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(getDoubleValue(heightBox) == 0){
					if(e.keyCode == SWT.TAB){
						new Notify(lh.localValue("heightZero"));
						e.doit = false;
					}
				}
			}
		});
		
		widthBox.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				if(!listenerLocked){
					settingsMap.put("paperWidth", getStringValue(widthBox));
					checkStandardSizes();
				}
			}
		});
		
		heightBox.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				if(!listenerLocked){
					settingsMap.put("paperHeight", getStringValue(heightBox));
					checkStandardSizes();
				}
			}
		});
		
		cellsBox.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				if(!listenerLocked){
					settingsMap.put("cellsPerLine", getStringValue(cellsBox));
				}
			}	
		});
		
		linesBox.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				if(!listenerLocked){
					settingsMap.put("linesPerPage", getStringValue(linesBox));
				}
			}	
		});
		
		pageTypes.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Page p = sm.getStandardSizes()[pageTypes.getSelectionIndex()];
				if(sm.isMetric()){
					widthBox.setText(String.valueOf(p.mmWidth));
					heightBox.setText(String.valueOf(p.mmHeight));
					cellsBox.setText(String.valueOf(calculateCellsPerLine(p.mmWidth)));
					linesBox.setText(String.valueOf(calculateLinesPerPage(p.mmHeight )));
				}
				else {
					widthBox.setText(String.valueOf(p.width));
					heightBox.setText(String.valueOf(p.height));
					cellsBox.setText(String.valueOf(calculateCellsPerLine(p.width)));
					linesBox.setText(String.valueOf(calculateLinesPerPage(p.height)));
				}
				
				if(pageTypes.getItem(pageTypes.getItemCount() - 1).equals(lh.localValue("custom")))
					pageTypes.remove(pageTypes.getItemCount() - 1);
			}
		});
		
		pageTypes.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode != SWT.ARROW_DOWN && e.keyCode != SWT.ARROW_UP){
					int loc = searchList(e.character);
					if(loc != -1){
						e.doit = false;
						pageTypes.select(loc);
						cellsBox.setText(String.valueOf(calculateCellsPerLine(Double.valueOf(sm.getStandardSizes()[loc].width))));
						linesBox.setText(String.valueOf(calculateLinesPerPage(Double.valueOf(sm.getStandardSizes()[loc].height))));
					}
					else
						e.doit = false;
				}
			}
		});
	}
	
	private void checkStandardSizes(){
		Double width = getDoubleValue(widthBox);
		Double height = getDoubleValue(heightBox);
		
		if(width != 0 && height != 0){
			boolean found = false;
			for(int i = 0; i < sm.getStandardSizes().length && !found; i++){
				if(checkEqualHeight(sm.getStandardSizes()[i], height) && checkEqualWidth(sm.getStandardSizes()[i], width)){
					pageTypes.select(i);
					found = true;
				
					if(!sm.isMetric()){
						cellsBox.setText(String.valueOf(calculateCellsPerLine(sm.getStandardSizes()[i].width)));
						linesBox.setText(String.valueOf(calculateLinesPerPage(sm.getStandardSizes()[i].height)));
					}
					else {
						cellsBox.setText(String.valueOf(calculateCellsPerLine(sm.getStandardSizes()[i].mmWidth)));
						linesBox.setText(String.valueOf(calculateLinesPerPage(sm.getStandardSizes()[i].mmHeight)));
					}
					
					if(pageTypes.getItem(pageTypes.getItemCount() - 1).equals(lh.localValue("custom")))
						pageTypes.remove(pageTypes.getItemCount() - 1);
				}
			}
	
			if(!found){
				if(pageTypes.getItemCount() == sm.getStandardSizes().length){
					pageTypes.add(lh.localValue("custom"));
					pageTypes.select(pageTypes.getItemCount() - 1);
				}
				else
					pageTypes.select(pageTypes.getItemCount() - 1);
			
				cellsBox.setText(String.valueOf(calculateCellsPerLine(Double.valueOf(widthBox.getText()))));
				linesBox.setText(String.valueOf(calculateLinesPerPage(Double.valueOf(heightBox.getText()))));
			}
		}
	}
	
	public boolean validate(){		
		if(Integer.valueOf(cellsBox.getText()) <= 0)
			return false;
		else if(Integer.valueOf(linesBox.getText()) <= 0)
			return false;
		
		return true;
	}
	
	private void setValue(Text text, String key){
		if(settingsMap.containsKey(key))
			text.setText(settingsMap.get(key));
	}
	
	private void setDefault(){
		if(settingsMap.containsKey("paperWidth") && settingsMap.containsKey("paperHeight")){
			for(int i = 0; i < sm.getStandardSizes().length; i++){
				if(checkEqualWidth(sm.getStandardSizes()[i], Double.valueOf(settingsMap.get("paperWidth"))) && checkEqualHeight(sm.getStandardSizes()[i], Double.valueOf(settingsMap.get("paperHeight")))){
					pageTypes.select(i);
					break;
				}
			}
		}
	}
	
	public TabItem getTab(){
		return item;
	}
	
	private double getDoubleValue(Text t){
		if(t.getText().length() == 0)
			return 0.0;
		else
			return Double.valueOf(t.getText());
	}
	
	private String getStringValue(Text t){
		if(t.getText().length() == 0)
			return "0";
		else
			return t.getText();
	}
	
	private int calculateCellsPerLine(double pWidth){
		double cellWidth;
		if(!sm.isMetric())
			cellWidth = 0.246063;
		else
			cellWidth = 6.25;
		
		if(settingsMap.containsKey("leftMargin"))
			pWidth -= Double.valueOf(settingsMap.get("leftMargin"));
		
		if(settingsMap.containsKey("rightMargin"))
			pWidth -= Double.valueOf(settingsMap.get("rightMargin"));
		
		return (int)(pWidth / cellWidth);
	}
	
	private int calculateLinesPerPage(double pHeight){
		double cellHeight;
		if(!sm.isMetric()) 
			cellHeight = 0.393701;
		else
			cellHeight = 10;
		
		if(settingsMap.containsKey("topMargin"))
			pHeight -= Double.valueOf(settingsMap.get("topMargin"));
		
		if(settingsMap.containsKey("bottomMargin"))
			pHeight -= Double.valueOf(settingsMap.get("bottomMargin"));
		
		return (int)(pHeight / cellHeight);
	}
	
	private boolean checkEqualWidth(Page p, double width){
		if(sm.isMetric())
			return p.mmWidth == width;
		else
			return p.width == width;
	}
	
	private boolean checkEqualHeight(Page p, double height){
		if(sm.isMetric())
			return p.mmHeight == height;
		else
			return p.height == height;
	}
	
	private void setFormLayout(Control c, int left, int right, int top, int bottom){
		FormData location = new FormData();
		
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		
		c.setLayoutData(location);
	}
}
