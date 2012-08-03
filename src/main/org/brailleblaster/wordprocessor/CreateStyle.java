package org.brailleblaster.wordprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

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

public class CreateStyle extends Dialog{
	final static int PROPERTYLABELWIDTH = 125;
	final static int PROPERTYTEXTWIDTH = 100;
	private static LocaleHandler lh = new LocaleHandler();
	private static int num_styles = 0;
	private static Label lastLabel;
	//private static final int MAX_NUM_FILES=50;
	private String fileSep;
	private DocumentManager dm;
	private String stylePath;
	private String propExtension;
	private ArrayList<String> formValues = new ArrayList<String>() ;
    final String sTrue = lh.localValue("styleTrue");
    final String sFalse = lh.localValue("styleFalse");
    
	private  FocusListener focusListener = new FocusListener() {
		public void focusGained(FocusEvent e) {
			Text t = (Text) e.widget;
			t.selectAll();
			//System.out.println("selecting all");
		}

		public void focusLost(FocusEvent e) {
			final Text t = (Text) e.widget;
			
			if (t.getSelectionCount() > 0) {
				t.clearSelection();
			}

//FO  	    if(t.getText().length() == 0){
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
	
	public CreateStyle (Shell parent, int style) {
		super (parent, style);
	}

	CreateStyle (final DocumentManager dm) {
		this (dm.documentWindow, SWT.NONE);
		this.dm = dm;
		fileSep = BBIni.getFileSep();
		stylePath = BBIni.getStylePath();
		this.propExtension =BBIni.propExtension;
	}

	void open(){
		Shell parent = getParent();
		Display display = parent.getDisplay();
		Shell stylePanel = new Shell(parent);
		stylePanel.setText(lh.localValue("newStyle"));
		FormLayout layout = new FormLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		stylePanel.setLayout(layout);

		FormData data = new FormData();

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
		name.setText(lh.localValue("styleName"));;
		data = new FormData();
		data.width = PROPERTYLABELWIDTH;
		name.setLayoutData(data);
		Text nameText = new Text(attributesPanel, SWT.RIGHT|SWT.WRAP | SWT.BORDER);
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
		addButtons(stylePanel, propertiesPanel);


		stylePanel.pack();
		stylePanel.open();
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
				Text firstText = new Text(propertiesPanel, SWT.RIGHT|SWT.WRAP | SWT.BORDER);
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
			}
			else{
				Label l = addFieldLabel(propertiesPanel, sp.toString()+":");
				if(sp.getTypeIndex() == StyleProperty.INTEGER){
					Text t = addFieldText(propertiesPanel, sp.getDefaultValue(), sp.getToolTip());
					Accessible accLabel = l.getAccessible();
					Accessible accText = t.getAccessible();
					accLabel.addRelation(ACC.RELATION_LABEL_FOR, accText);
					accText.addRelation(ACC.RELATION_LABELLED_BY, accLabel);
					accText.addRelation(ACC.RELATION_MEMBER_OF, accPropertiesPanel);
					accText.addRelation(ACC.RELATION_LABELLED_BY, accPropertiesPanel);
				}
				else if(sp.getTypeIndex() == StyleProperty.BOOLEAN){
					Combo c = addFieldCombo(propertiesPanel, sp.getDefaultValue(), sp.getToolTip());
					Accessible accLabel = l.getAccessible();
					Accessible accCombo = c.getAccessible();
					accLabel.addRelation(ACC.RELATION_LABEL_FOR, accCombo);
					accCombo.addRelation(ACC.RELATION_LABELLED_BY, accLabel);
					accCombo.addRelation(ACC.RELATION_MEMBER_OF, accPropertiesPanel);
					accCombo.addRelation(ACC.RELATION_LABELLED_BY, accPropertiesPanel);
				}
			}
		}
	}

	private void addButtons(final Shell parent ,Group propertiesPanel){
		FormData data = new FormData();
		Button confirmButt = new Button(parent, SWT.PUSH);
		Button cancelButt = new Button(parent, SWT.PUSH);
		confirmButt.setText(lh.localValue("styleCreate"));
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
					//System.out.print(s+'\n');
					formValues.add(s);
				}
				//saveStyle();

				Group pps = (Group) parent.getChildren()[1];
				Control pList[] = pps.getChildren();
				for(Control c: pList){
					String s= controlToString(c);
					//System.out.print(s+'\n');
					formValues.add(s);
				}

				saveStyle(getStyleName());	
				parent.dispose();
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

	private Text addFieldText(Composite parent, String text, String toolTip){
		FormData data = new FormData();
		final Text curText = 
				new Text(parent, SWT.WRAP | SWT.BORDER|SWT.RIGHT);
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
		curText.add("True");
		curText.add("False");
		if(text.equals("True")){
			curText.select(0);
		}
		else{
			curText.select(1);
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
				//System.out.println( e.character); 
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

	private void saveStyle(String styleName){
		//file = new File(recentFiles);
		FileUtils fu = new FileUtils();
		String fileName = stylePath+fileSep+styleName+propExtension;
		int i = 1;
		while(fu.exists(fileName)){
			fileName = stylePath+fileSep+styleName +"("+ i +")" +propExtension;
			i++;
		}
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
			for( String s:formValues) {
				if(s.charAt(s.length()-1)==':'){
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
}
