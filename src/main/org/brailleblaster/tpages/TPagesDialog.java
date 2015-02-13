package org.brailleblaster.tpages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.tpages.TPagesGenerator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class TPagesDialog extends Dialog{
	private Manager m;
	private Shell shlTPages;
	private Object result;
	private Display display;
	final int TEXT_BOX_WIDTH = 500;
	final int LABEL_WIDTH = 150;
	TPagesGenerator tpGenerator;
	HashMap<String, String> xmlmap;
	
	public TPagesDialog(Shell parent, int style, Manager brailleViewController){
		super(parent, style);
		setText("Transcriber-Generated Pages");
		m = brailleViewController;
	}
	
	public Object open(){
		tpGenerator = new TPagesGenerator();
		if(!tpGenerator.checkForFile("filename")){
			tpGenerator.createNewTPageXML();
		}
		tpGenerator.openTPageXML("filename");
		xmlmap = tpGenerator.getXmlMap();
		createContents();
		
		shlTPages.open();
		shlTPages.layout();
		
		display = getParent().getDisplay();
		while(!shlTPages.isDisposed()){
			if (!display.readAndDispatch()) display.sleep();
		}
		
		return result;
	}
	
	public void createContents(){
		shlTPages = new Shell(getParent(), SWT.DIALOG_TRIM);
		shlTPages.setText("Transcriber-Generated Pages");
		shlTPages.setVisible(true);
		shlTPages.setLayout(new GridLayout(5,true));
		
		GridData tfData = newTpData(5);
		TabFolder folder = new TabFolder(shlTPages, SWT.NONE);
		folder.setLayoutData(tfData);
		
		TabItem titleTab = new TabItem(folder, SWT.NONE);
		titleTab.setText("Title Page(s)");
		
		TabItem symbolsTab = new TabItem(folder, SWT.NONE);
		symbolsTab.setText("Special Symbols");
		
		/////////////////////////////
		///////Title Page Tab////////
		/////////////////////////////
		Composite titleComposite = new Composite(folder, SWT.NONE);
		GridLayout titlePageLayout = new GridLayout(2,false);
		titleComposite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Group titleGroup = new Group(titleComposite, SWT.NONE);
		titleGroup.setText("Title");
		titleGroup.setLayout(titlePageLayout);
		
		createLabel(titleGroup, "Title", 1);
		final Text titleText = createText(titleGroup, 1, "title");
		
		createLabel(titleGroup, "Grade Level", 1);
		final Text gradeLevelText = createText(titleGroup, 1, "gradelevel");
		
		createLabel(titleGroup, "Subtitle", 1);
		final Text subtitleText = createText(titleGroup, 1, "subtitle");
		
		createLabel(titleGroup, "Series Name", 1);
		final Text seriesText = createText(titleGroup, 1, "seriesname");
		
		createLabel(titleGroup, "Edition Name or Number", 1);
		final Text editionText = createText(titleGroup, 1, "editionname");
		
		Group authorGroup = new Group(titleComposite, SWT.NONE);
		authorGroup.setText("Author");
		authorGroup.setLayout(new GridLayout(2,false));
		
		createLabel(authorGroup, "Author(s)", 1);
		final Text authorText = new Text(authorGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData newData = newTpData(1);
		newData.widthHint = TEXT_BOX_WIDTH;
		newData.heightHint = 70;
		if(xmlmap.get("authors")!=null)
			authorText.setText(xmlmap.get("authors").replaceAll(";", "\r\n"));
		authorText.setLayoutData(newData);
		
		createLabel(authorGroup, "Translator", 1);
		final Text translatorText = createText(authorGroup, 1, "translator");
		
		Group publisherGroup = new Group(titleComposite, SWT.NONE);
		publisherGroup.setText("Publisher");
		publisherGroup.setLayout(new GridLayout(2,false));
		
		createLabel(publisherGroup, "Permission", 1);
		final Combo permissionCombo = new Combo(publisherGroup, SWT.READ_ONLY);
		permissionCombo.setLayoutData(newTpData(1));
		permissionCombo.setItems(new String[]{"Published by", "With permission of the publisher,"});
		if (xmlmap.get("pubpermission").equals("0")||xmlmap.get("pubpermission").equals("1"))
			permissionCombo.select(Integer.parseInt(xmlmap.get("pubpermission")));
		else
			permissionCombo.select(0);
		
		createLabel(publisherGroup, "Publisher", 1);
		final Text publisherText = createText(publisherGroup, 1, "publisher");
		
		createLabel(publisherGroup, "City and State", 1);
		final Text pubLocationText = createText(publisherGroup, 1, "location");
		
		createLabel(publisherGroup, "Website", 1);
		final Text pubWebsiteText = createText(publisherGroup, 1, "website");
		
		Group printGroup = new Group (titleComposite, SWT.NONE);
		printGroup.setText("Printing Info");
		printGroup.setLayout(new GridLayout(2,false));
		
		final Button copyrightButton = new Button(printGroup, SWT.CHECK);
		copyrightButton.setText("Copyright");
		copyrightButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		if(xmlmap.get("copyrighted")!=null){
			copyrightButton.setSelection(xmlmap.get("copyrighted").equals("true"));
		} else{
			copyrightButton.setSelection(true);
		}
		final Button copySymbolButton = new Button(printGroup, SWT.CHECK);
		copySymbolButton.setText("Copyright Symbol");
		copySymbolButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		if(xmlmap.get("copyrightsymbol")!=null){
			copySymbolButton.setSelection(xmlmap.get("copyrightsymbol").equals("true"));
		} else{
			copySymbolButton.setSelection(true);
		}
		
		createLabel(printGroup, "Copyright Date", 1);
		final Text copyDateText = createText(printGroup, 1, "copyrightdate");
		
		createLabel(printGroup, "Copyright Text", 1);
		final Text copyText = createText(printGroup, 1, "copyrighttext");
		
		createLabel(printGroup, "Reproduction Notice", 1);
		final Text reproText = createText(printGroup, 1, "repronotice");
		//reproText.setText("Further reproduction or distribution in other than a specialized format is prohibited.");
		
		createLabel(printGroup,"ISBN-13", 1);
		final Text isbn13Text = createText(printGroup, 1, "isbn13");
		
		createLabel(printGroup, "ISBN-10", 1);
		final Text isbn10Text = createText(printGroup, 1, "isbn10");
		
		createLabel(printGroup, "Printing History", 1);
		final Text printHistoryText = createText(printGroup, 1, "printhistory");
		
		Group transcriberGroup = new Group (titleComposite, SWT.NONE);
		transcriberGroup.setText("Transcriber");
		transcriberGroup.setLayout(new GridLayout(2, false));
		
		createLabel(transcriberGroup, "Transcription Year", 1);
		final Text transYearText = createText(transcriberGroup, 1, "year");
		
		createLabel(transcriberGroup, "Transcriber", 1);
		final Text transText = createText(transcriberGroup, 1, "transcriber");
		
		createLabel(transcriberGroup, "Tactile Graphics Specialist", 1);
		final Text tgsText = createText(transcriberGroup, 1, "tgs");
		
		createLabel(transcriberGroup, "Affiliation", 1);
		final Text affiliationText = createText(transcriberGroup, 1, "affiliation");
		
		Group volumesGroup = new Group (titleComposite, SWT.NONE);
		volumesGroup.setText("Volumes");
		volumesGroup.setLayout(new GridLayout(2,false));
		createLabel(volumesGroup, "Not yet implemented", 1);
		
		titleTab.setControl(titleComposite);
		/////////////////////////////
		
		/////////////////////////////
		/////Special Symbols Tab/////
		/////////////////////////////
		Composite symbolsComposite = new Composite(folder, SWT.NONE);
		symbolsComposite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Group symbolsGroup = new Group(symbolsComposite, SWT.NONE);
		symbolsGroup.setLayout(new GridLayout(1, false));
		
		createLabel(symbolsGroup, "lolidk", 1);
		
		symbolsTab.setControl(symbolsComposite);
		/////////////////////////////
		
		Button closeButton = new Button(shlTPages, SWT.PUSH);
		GridData buttonData = new GridData(SWT.RIGHT, SWT.BEGINNING, false, false, 4, 1);
		buttonData.widthHint = 100;
		buttonData.heightHint = 30;
		closeButton.setText("Close");
		closeButton.setLayoutData(buttonData);
		
		closeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlTPages.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		Button generateButton = new Button(shlTPages, SWT.PUSH);
		buttonData = new GridData(SWT.LEFT, SWT.BEGINNING, false, false, 1, 1);
		buttonData.widthHint = 100;
		buttonData.heightHint = 30;
		generateButton.setText("Generate");
		generateButton.setLayoutData(buttonData);
		
		generateButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				xmlmap.put("title", titleText.getText());
				xmlmap.put("gradelevel", gradeLevelText.getText());
				xmlmap.put("subtitle", subtitleText.getText());
				xmlmap.put("seriesname", seriesText.getText());
				xmlmap.put("editionname", editionText.getText());
				authorText.setText(authorText.getText().replaceAll("\r\n", ";"));
				xmlmap.put("authors", authorText.getText());
				xmlmap.put("translator", translatorText.getText());
				xmlmap.put("pubpermission", "" + permissionCombo.getSelectionIndex());
				xmlmap.put("publisher", publisherText.getText());
				xmlmap.put("location", pubLocationText.getText());
				xmlmap.put("website", pubWebsiteText.getText());
				xmlmap.put("copyrighted", String.valueOf(copyrightButton.getSelection()));
				xmlmap.put("copyrightsymbol", String.valueOf(copySymbolButton.getSelection()));
				xmlmap.put("copyrightdate", copyDateText.getText());
				xmlmap.put("copyrighttext", copyText.getText());
				xmlmap.put("repronotice", reproText.getText());
				xmlmap.put("isbn13", isbn13Text.getText());
				xmlmap.put("isbn10", isbn10Text.getText());
				xmlmap.put("printhistory", printHistoryText.getText());
				xmlmap.put("year", transYearText.getText());
				xmlmap.put("transcriber", transText.getText());
				xmlmap.put("tgs", tgsText.getText());
				xmlmap.put("affiliation", affiliationText.getText());
				tpGenerator.saveNewTPage("filename", xmlmap);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		shlTPages.pack(true);
		
	}
	
	public GridData newTpData(int columns){
		return new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, columns, 1);
	}
	
	public Label createLabel(Composite comp, String text, int horizSpan){ 
		Label newLabel = new Label(comp, SWT.NONE);
		newLabel.setText(text);
		GridData labelData = newTpData(horizSpan);
		labelData.widthHint = LABEL_WIDTH;
		newLabel.setLayoutData(labelData);
		return newLabel;
	}
	
	public Text createText(Composite comp, int horizSpan, String xmlmapKey){
		Text newText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		GridData newData = newTpData(horizSpan);
		newData.widthHint = TEXT_BOX_WIDTH;
		newText.setLayoutData(newData);
		if(xmlmapKey!=null){
			if(xmlmap.get(xmlmapKey)!=null){
				newText.setText(xmlmap.get(xmlmapKey));
			}
		}
		return newText;
	}
}
