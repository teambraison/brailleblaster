package org.brailleblaster.wordprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class EditStyle extends Dialog{
	final static int PROPERTYLABELWIDTH = 125; //SWT UI setting
	final static int PROPERTYTEXTWIDTH = 100;
	private static LocaleHandler lh = new LocaleHandler();
	private static int num_styles = 0;
	private static Label lastLabel;
    final static String STRUE = lh.localValue("styleTrue");
    final static String SFALSE = lh.localValue("styleFalse");
    final static int INDEXTRUE = 0; //index of "true" in the combo
    final static int INDEXFALSE = 1;
	private static String fileSep;
	
	private DocumentManager dm;
	private StyleManager sm;
	private String stylePath;
	private String propExtension;
	private ArrayList<String> formValues = new ArrayList<String>() ;

    private Text nameText;
    private Hashtable<String, Widget> formRelTab;
    //static ArrayList<String> propertiesNameList =new ArrayList<String>();
    
    static int CREATE = 1;
    static int MODIFY = 2;
    
	private static  FocusListener focusListener = new FocusListener() {
		public void focusGained(FocusEvent e) {
			Text t = (Text) e.widget;
			t.selectAll();
		}

		public void focusLost(FocusEvent e) {
			final Text t = (Text) e.widget;
			
			if (t.getSelectionCount() > 0) {
				t.clearSelection();
			}

			//FO  	   
			if(! t.getText().toString().matches("^[a-zA-Z0-9_]+$")) {
				new Notify(lh.localValue("styleNotBlank"));
				Display.getCurrent().asyncExec(new Runnable(){
					public void run(){
						t.setFocus();
					}
				});
			}
		}
	};
	
	public EditStyle (Shell parent, int style) {
		super (parent, style);
	}
    
	EditStyle (final StyleManager styleManager) {
		this (styleManager.dm.documentWindow, SWT.NONE);
		this.sm = styleManager;
		this.dm = styleManager.dm;
		fileSep = BBIni.getFileSep();
		stylePath = BBIni.getStylePath();
		this.propExtension =BBIni.propExtension;
	}

	private Shell initOpen(int mode){
		Shell parent = getParent();
		Shell stylePanel = new Shell(parent);
		String panelTitle = (mode == CREATE)?(lh.localValue("newStyle")):(lh.localValue("modifyStyle"));
		stylePanel.setText(panelTitle);
		FormLayout layout = new FormLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		stylePanel.setLayout(layout);

		FormData data = new FormData();
		formRelTab = new Hashtable<String, Widget>(); //Stores the mapping rel between labels and widgets

		Group attributesPanel = new Group(stylePanel, SWT.NONE);
		attributesPanel.setText(lh.localValue("styleAttributes"));
		FormLayout ownerLayout1 = new FormLayout();
		ownerLayout1.marginWidth = 5;
		ownerLayout1.marginHeight = 5;
		attributesPanel.setLayout(ownerLayout1);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		attributesPanel.setLayoutData(data);


		Label name = new Label(attributesPanel, SWT.NULL);
		name.setText(lh.localValue("styleName"));
		data = new FormData();
		data.width = PROPERTYLABELWIDTH;
		name.setLayoutData(data);
		nameText = new Text(attributesPanel, SWT.SINGLE | SWT.BORDER| SWT.RIGHT);
		nameText.setText("s"+ (num_styles+1));
		data = new FormData();
		data.left = new FormAttachment(name, 5);
		data.right = new FormAttachment(100, 0);
		data.width = PROPERTYTEXTWIDTH;
		nameText.setLayoutData(data);
		nameText.addFocusListener(focusListener);
		Accessible accName = name.getAccessible();
		Accessible accNameText = nameText.getAccessible();
		accName.addRelation(ACC.RELATION_LABEL_FOR, accNameText);
		accNameText.addRelation(ACC.RELATION_LABELLED_BY, accName);

		Group propertiesPanel = new Group(stylePanel, SWT.NONE);
		propertiesPanel.setText(lh.localValue("styleProperties"));
		FormLayout ownerLayout = new FormLayout();
		ownerLayout.marginWidth = 5;
		ownerLayout.marginHeight = 5;
		propertiesPanel.setLayout(ownerLayout);
		data = new FormData();
		data.top = new FormAttachment(attributesPanel, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		propertiesPanel.setLayoutData(data);

		loadProperties(propertiesPanel);
		String confirmButtText = (mode == CREATE)?lh.localValue("styleCreate"):lh.localValue("styleModify");
		addButtons(stylePanel, propertiesPanel, confirmButtText);
		
		formRelTab.put("styleName:", nameText);
		stylePanel.pack();
		stylePanel.open();
		return stylePanel;
	}
	
	//used to create new style
	void create(String styleName){
		Shell stylePanel = initOpen(CREATE);
		Shell parent = getParent();
		Display display = parent.getDisplay();
		HashMap<String, String> styleSet = new HashMap<String, String>(); 
		styleSet.put("styleName", styleName);
		Style style = new Style(styleSet);
		updateFields(style);
		nameText.setEditable(false);
		nameText.setEnabled(false);
		while (!stylePanel.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep();
		}
		stylePanel.dispose();
	}
	
	//used to modify existing style
	void modify(Style style){
		Shell stylePanel = initOpen(MODIFY);
		Shell parent = getParent();
		Display display = parent.getDisplay();
		if(style != null){
			nameText.setEditable(false);
			nameText.setEnabled(false);
			updateFields(style);
		}
		while (!stylePanel.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep();
		}
		stylePanel.dispose();
	}

	//add necessary fields from enum class StyleProperty into the panel
	private void loadProperties(Group propertiesPanel){
		boolean isFirstProperty = true;
		Accessible accPropertiesPanel = propertiesPanel.getAccessible();
		FormData data = new FormData();
		for(StyleProperty sp :StyleProperty.values()){
			if(isFirstProperty){
				Label firstLabel = new Label(propertiesPanel, SWT.NULL);
				firstLabel.setText(sp.toString()+":");
				data = new FormData();
				data.width = PROPERTYLABELWIDTH;
				firstLabel.setLayoutData(data);
				firstLabel.setToolTipText(sp.getToolTip());
				Text firstText = new Text(propertiesPanel, SWT.SINGLE | SWT.BORDER|SWT.RIGHT);
				data = new FormData();
				data.left = new FormAttachment(firstLabel, 5);
				data.right = new FormAttachment(100, 0);
				data.width = PROPERTYTEXTWIDTH;
				setNumericalValidation(firstText);//
				firstText.setLayoutData(data);
				firstText.setToolTipText(sp.getToolTip());
				firstText.setText(sp.getDefaultValue());
				firstText.addFocusListener(focusListener);
				//add accessible				
				Accessible accFirstLabel = firstLabel.getAccessible();
				Accessible accFirstText = firstText.getAccessible();
				accFirstLabel.addRelation(ACC.RELATION_LABEL_FOR, accFirstText);
				accFirstText.addRelation(ACC.RELATION_LABELLED_BY, accFirstLabel);
				accFirstText.addRelation(ACC.RELATION_MEMBER_OF, accPropertiesPanel);
				accFirstText.addRelation(ACC.RELATION_LABELLED_BY, accPropertiesPanel);
				lastLabel = firstLabel;
				isFirstProperty = false;
				//add to table
				formRelTab.put(firstLabel.getText(), firstText);
			}
			else{
				addFields(propertiesPanel, sp);
			}
		}
	}

	private void addButtons(final Shell parent ,Group propertiesPanel, String confirmText){
		FormData data = new FormData();
		Button confirmButt = new Button(parent, SWT.PUSH);
		Button cancelButt = new Button(parent, SWT.PUSH);
		confirmButt.setText(confirmText);
		cancelButt.setText(lh.localValue("styleCancel"));
		data.top = new FormAttachment(propertiesPanel, 5);
		data.right = new FormAttachment(100, -5);
		cancelButt.setLayoutData(data);
		data = new FormData();
		data.top = new FormAttachment(cancelButt, 0, SWT.TOP);
		data.right = new FormAttachment(cancelButt, -5);
		confirmButt.setLayoutData(data);
		parent.setDefaultButton(confirmButt);

		cancelButt.addSelectionListener(new SelectionAdapter(){ 
			public void widgetSelected(SelectionEvent e) {
				parent.dispose();
			}
		});

		confirmButt.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				Group abs = (Group) parent.getChildren()[0];
				Control aList[] = abs.getChildren();
				for(Control c: aList){
					String s= controlToString(c);
					formValues.add(s);
				}

				Group pps = (Group) parent.getChildren()[1];
				Control pList[] = pps.getChildren();
				for(Control c: pList){
					String s= controlToString(c);
					formValues.add(s);
				}
				String styleName= getStyleName();
				
				System.out.println("Style name is " + styleName);
				
				saveStyle(styleName);	
				parent.dispose();
				sm.readStyleFiles(styleName);
			}
		});
		
	}
	
	
	String sn = lh.localValue("styleName");
	String getStyleName(){
		for(int i = 0; i< formValues.size(); i++){
			if(formValues.get(i).equals(sn)){
				return formValues.get(i+1); 
			}
		}		
		return "";
	}

	static String controlToString(Control c){
		String s = "";
		if(c instanceof Text){
			Text m = (Text)c;
			s =m.getText();
		}
		else if (c instanceof Label){
			Label l = (Label)c;
			s = l.getText();
		}
		else if (c instanceof Combo){
			Combo cb = (Combo)c;
			s = cb.getText();
		}
		return s;
	}

	private void addFields(Group propertiesPanel, StyleProperty sp){
		Accessible accPropertiesPanel = propertiesPanel.getAccessible();
		Label l = addFieldLabel(propertiesPanel, sp.toString()+":");
		if(sp.getTypeIndex() == StyleProperty.INTEGER){
			Text t = addFieldText(propertiesPanel, sp.getDefaultValue(), sp.getToolTip());
			Accessible accLabel = l.getAccessible();
			Accessible accText = t.getAccessible();
			accLabel.addRelation(ACC.RELATION_LABEL_FOR, accText);
			accText.addRelation(ACC.RELATION_LABELLED_BY, accLabel);
			accText.addRelation(ACC.RELATION_MEMBER_OF, accPropertiesPanel);
			accText.addRelation(ACC.RELATION_LABELLED_BY, accPropertiesPanel);
			formRelTab.put(l.getText(), t);
		}
		else if(sp.getTypeIndex() == StyleProperty.BOOLEAN){
			Combo c = addFieldCombo(propertiesPanel, sp.getDefaultValue(), sp.getToolTip());
			Accessible accLabel = l.getAccessible();
			Accessible accCombo = c.getAccessible();
			accLabel.addRelation(ACC.RELATION_LABEL_FOR, accCombo);
			accCombo.addRelation(ACC.RELATION_LABELLED_BY, accLabel);
			accCombo.addRelation(ACC.RELATION_MEMBER_OF, accPropertiesPanel);
			accCombo.addRelation(ACC.RELATION_LABELLED_BY, accPropertiesPanel);
			formRelTab.put(l.getText(), c);
		}
	}
	
	private static Label addFieldLabel(Composite parent, String text){
		FormData data = new FormData();
		Label curLabel = new Label(parent, SWT.PUSH);
		data.top= new FormAttachment(lastLabel, 12);
		data.width = PROPERTYLABELWIDTH;
		curLabel.setLayoutData(data);
		curLabel.setText(text);
		curLabel.setToolTipText(text);
		lastLabel = curLabel;
		return curLabel;
	}

	private static Text addFieldText(Composite parent, String text, String toolTip){
		FormData data = new FormData();
		final Text curText = 
				new Text(parent, SWT.SINGLE | SWT.BORDER|SWT.RIGHT);
		setNumericalValidation(curText);

		data.top= new FormAttachment(lastLabel, -2, SWT.TOP);
		data.left = new FormAttachment(lastLabel, 5);
		data.right = new FormAttachment(100, 0);
		data.width = PROPERTYTEXTWIDTH;
		curText.setLayoutData(data);
		curText.setText(text);
		curText.setToolTipText(toolTip);
		curText.addFocusListener(focusListener);
		return curText;
	}

	private static Combo addFieldCombo(Composite parent, String text, String toolTip){
		FormData data = new FormData();
		Combo curText = 
				new  Combo(parent, SWT.BORDER|SWT.RIGHT|SWT.READ_ONLY);
		data.top= new FormAttachment(lastLabel, -2, SWT.TOP);
		data.left = new FormAttachment(lastLabel, 5);
		data.right = new FormAttachment(100, 0);
		data.width = PROPERTYTEXTWIDTH;
		curText.setLayoutData(data);
		curText.setText(text);
		curText.add(STRUE);
		curText.add(SFALSE);
		if(text.equals(STRUE)){
			curText.select(INDEXTRUE);
		}
		else{
			curText.select(INDEXFALSE);
		}
		curText.setToolTipText(toolTip);
		return curText;
	}

	private static void setNumericalValidation(final Text text){
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				String txt = ((Text) event.getSource()).getText();
				try {
					//	int num = Integer.parseInt(txt);
					// Checks on num
				} catch (NumberFormatException e) {
					// Show error
					new Notify(txt+ lh.localValue("styleNotValid"));
				}
			}
		});

		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				final String oldS = text.getText();
				final String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
				if (e.character == SWT.CR)  {
					e.doit = false;
				}
				else if(newS.equals("")){
					//only check if the string is not empty,
					//focusListener handles the empty string
					//do nothing
				}
				else{
					try {
						BigDecimal bd = new BigDecimal(newS);
						// value is decimal
						// Test value range
					} catch (final NumberFormatException numberFormatException) {
						// value is not decimal
						e.doit = false;
						new Notify(lh.localValue("styleNumOnly"));
					}
				}
			}
		});
	}

	/**
	 * 	Write to the properties file with the style name in the local directory;
	 * Create one if not existed yet.
	 * 
	 * @param styleName
	 */
	private void saveStyle(String styleName){
		FileUtils fu = new FileUtils();
		String fileName = stylePath+fileSep+styleName+propExtension;
//		int i = 1;
//		while(fu.exists(fileName)){
//			fileName = stylePath+fileSep+styleName +"("+ i +")" +propExtension;
//			i++;
//		}
		System.out.println(fileName);
		if(!fu.exists(fileName)){
			fu.create(fileName);
		}
		writeToFile(new File(fileName));
	}

	private void writeToFile(File file) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			new Notify(e.getMessage());
		}
		try {
			boolean isEven = false;
			int i = 1;
			for( String s:formValues) {
				if (i++ == 1) {
					s = "styleName=";
				} else if (s.charAt(s.length()-1)==':'){
					s = s.substring(0,s.length()-1)+"=";
				}
				writer.write(s);
				if(isEven){
					writer.newLine();
				}
				isEven = !isEven;
			}
		} catch (IOException e) {
			new Notify(e.getMessage());
		}finally{
			try {
				writer.close();
			} catch (IOException e) {
				new Notify(e.getMessage());
			}
		}
	}
	
	
	/**
	 * This method load existing style values to the form
	 *  @param style 
	 */	
	
	void updateFields(Style style){
//		for(String s: formRelTab.keySet()){
//			System.out.println(s);
//			Widget w = formRelTab.get(s);
//			if(w instanceof Text){
//				System.out.println(((Text) w).getText());
//			}
//		}			
		//style.print();
		
		
		for(String labelText: formRelTab.keySet()){
			Widget w = formRelTab.get(labelText);
			
			String key = labelText.substring(0,labelText.length()-1);// this filters out the ':' in the label text
			String value = style.styleSet.get(key);

			//System.out.println('+'+key + ":"+ value +", w:"+w.toString());
			if(value == null) continue;
			if(w instanceof Text){
				((Text) w).setText(value);
			}
			else if(w instanceof Combo){
				int i;
				if(value.equals(STRUE)) {
					i = INDEXTRUE;
				}
				else{
					i = INDEXFALSE;
				}
				((Combo) w).select(i);
			}
		}
	}
	
	public Hashtable<String, Widget> getFormRelTab(){
		return formRelTab;
	}
	
}