package org.brailleblaster.settings.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.settings.SettingsManager;
import org.brailleblaster.settings.Table;
import org.brailleblaster.settings.TranslationConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import org.eclipse.swt.layout.FillLayout;
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

public class TranslationSettingsTab {
	private static final String transConPath = BBIni.getProgramDataPath() + BBIni.getFileSep() + "liblouisutdml" + BBIni.getFileSep() + "lbu_files" + BBIni.getFileSep() + "languageConfigurations.xml";
	private HashMap<String, String>settingsMap;
	TranslationConfiguration [] transCon;
	TabItem item;
	Composite composite; 
	Group langGroup, typeGroup, computerGroup; 
	Label langLabel, typeLabel, computerLabel, mathLabel;
	Combo languageCombo, typeCombo, computerCombo, mathCombo;
	Button math;
	LocaleHandler lh;
	TranslationSettingsTab(TabFolder folder, SettingsManager ppm, final HashMap<String, String>tempSettingsMap){
		lh = new LocaleHandler();
		settingsMap = tempSettingsMap;
		item = new TabItem(folder, 0);	
		item.setText(lh.localValue("translationSettings"));
		
		composite = new Composite(folder, 0);
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		item.setControl(composite);
		
		langGroup = new Group(composite, 0);
		langGroup.setLayout(new GridLayout(2, true));
		langGroup.setText(lh.localValue("selectLanguage"));
		langLabel = new Label(langGroup, 0);
		langLabel.setText("Languages");
		SettingsUIUtils.setGridData(langLabel);
		languageCombo = new Combo(langGroup, SWT.READ_ONLY);
		SettingsUIUtils.setGridData(languageCombo);
		
		typeGroup = new Group(composite, 0);
		typeGroup.setLayout(new GridLayout(2, true));
		typeGroup.setText(lh.localValue("selectType"));
		
		typeLabel = new Label(typeGroup, 0);
		typeLabel.setText(lh.localValue("brailleType"));
		SettingsUIUtils.setGridData(typeLabel);
		typeCombo = new Combo(typeGroup, SWT.READ_ONLY);
		SettingsUIUtils.setGridData(typeCombo);
		
		computerGroup = new Group(composite,0);
		computerGroup.setText(lh.localValue("selectComputerBraille"));
		computerGroup.setLayout(new GridLayout(2, true));
		
		computerLabel = new Label(computerGroup, 0);
		computerLabel.setText(lh.localValue("brailleType"));
		SettingsUIUtils.setGridData(computerLabel);
		
		computerCombo = new Combo(computerGroup, SWT.READ_ONLY);
		SettingsUIUtils.setGridData(computerCombo);
		
		
		transCon = loadTranslationConfigurations();
		loadView();
		setDefaults();
		addListeners();
	}
	
	private void addListeners(){
		typeCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingsMap.put("literaryTextTable", getTableFile(transCon[languageCombo.getSelectionIndex()].getTableList(), typeCombo));
				settingsMap.put("mathtextTable", getTableFile(transCon[languageCombo.getSelectionIndex()].getTableList(), typeCombo));
			}	
		});
		
		computerCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingsMap.put("compbrlTable", getTableFile(transCon[languageCombo.getSelectionIndex()].getCompBrailleList(),computerCombo));
			}
		});
		
		languageCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				typeCombo.removeAll();
				computerCombo.removeAll();
				int index = languageCombo.getSelectionIndex();
				populateCombo(transCon[index].getTableList(), typeCombo);
				populateCombo(transCon[index].getCompBrailleList(), computerCombo);
				settingsMap.put("mathexprTable", transCon[index].getMathTable());
			}
		});
	}
	
	private String getTableFile(ArrayList<Table>list, Combo c){
		return list.get(c.getSelectionIndex()).getTableFile();
	}
	
	private void loadView(){
		for(int i = 0; i < transCon.length; i++){
			if(transCon[i].getLocale() != null)
				languageCombo.add(transCon[i].getLanguage() + "(" + transCon[i].getLocale() + ")");
			else
				languageCombo.add(transCon[i].getLanguage());
		}
	}
	
	private void setDefaults(){
		if(settingsMap.containsKey("literaryTextTable"))
			findConfiguration(settingsMap.get("literaryTextTable"));
		else 
			checkLocale();
	}
	
	private void findConfiguration(String table){
		boolean found = false;
		for(int i = 0; i < transCon.length && !found; i++){
			for(int j = 0; j < transCon[i].getTableList().size() && !found; j++){
				if(transCon[i].getTableList().get(j).getTableFile().equals(table)){
					int index = i;
					populateCombo(transCon[index].getTableList(), typeCombo);
					populateCombo(transCon[index].getCompBrailleList(), computerCombo);
					languageCombo.select(i);
					typeCombo.select(j);
					if(settingsMap.containsKey("compbrlTable"))
						setCompBrailleCombo(transCon[index], settingsMap.get("compbrlTable"));
					
					found = true;
					break;
				}
			}
		}
	}
	
	private void populateCombo(ArrayList<Table>list, Combo c){
		for(int i = 0; i < list.size(); i++)
			c.add(list.get(i).getTableName());
	}
	
	private void setCompBrailleCombo(TranslationConfiguration tc, String table){
		for(int i = 0; i < tc.getCompBrailleList().size(); i++){
			if(tc.getCompBrailleList().get(i).getTableFile().equals(table)){
				computerCombo.select(i);
				break;
			}
		}
	}
	
	private void checkLocale(){
		String locale = Locale.getDefault().getCountry();
		for(int i = 0; i < transCon.length; i++){
			if(transCon[i].getLocale().equals(locale)){
				languageCombo.select(i);
			}
		}
	}
	
	private TranslationConfiguration[] loadTranslationConfigurations(){
		Document doc = buildDOM();
		Element root = doc.getRootElement();
		int count = root.getChildElements().size();
		TranslationConfiguration [] temp = new TranslationConfiguration[count];
		
		for(int i = 0; i < count; i++){
			temp[i] = new TranslationConfiguration(root.getChildElements().get(i));
		}
		
		return temp;
	}
	
	private Document buildDOM(){
		File f = new File(transConPath);
		Builder builder  = new Builder();
		
		try {
			Document d = builder.build(f);
			return d;
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean validate(){
		if(typeCombo.getSelectionIndex() != -1 && computerCombo.getSelectionIndex() != -1)
			return true;
		else
			return false;
	}
}
