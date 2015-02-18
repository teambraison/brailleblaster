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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
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
	HashMap<Text, String> uimap;
	Text titleText, gradeLevelText, subtitleText, seriesText, editionText, authorText, translatorText, publisherText,
	pubLocationText, pubWebsiteText, copyDateText, copyText, reproText, isbn13Text, isbn10Text, printHistoryText,
	transYearText, transText, tgsText, affiliationText;
	Combo permissionCombo;
	Button copyrightButton, copySymbolButton;
	TPagesGenerator tpGenerator;
	HashMap<String, String> xmlmap;
	
	public TPagesDialog(Shell parent, int style, Manager brailleViewController){
		super(parent, style);
		setText("Transcriber-Generated Pages");
		m = brailleViewController;
	}
	
	public Object open(){
		tpGenerator = new TPagesGenerator();
		
		if(m.getDocumentName()!=null)
			if(m.getDocumentName().substring(m.getDocumentName().length()-4).equals(".xml"))
				tpGenerator.autoPopulate(m.getDocumentName());
		xmlmap = tpGenerator.getXmlMap();
		uimap = new HashMap<Text, String>();
		createContents();
		
		if(m.getLastTPage()!=null){
			if(tpGenerator.checkForFile(m.getLastTPage())){
				openFromXml(m.getLastTPage());
				updateContents();
			}
		}
		
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
		
		TabItem transNotesTab = new TabItem(folder, SWT.NONE);
		transNotesTab.setText("Transcriber's Notes");
		
		/////////////////////////////
		///////Title Page Tab////////
		/////////////////////////////
		ScrolledComposite sc = new ScrolledComposite(folder, SWT.V_SCROLL);
		sc.setLayout(new FillLayout());
		Composite titleComposite = new Composite(sc, SWT.NONE);
		GridLayout titlePageLayout = new GridLayout(2,false);
		titleComposite.setLayout(new RowLayout(SWT.VERTICAL));
		//titleComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		Group titleGroup = new Group(titleComposite, SWT.NONE);
		titleGroup.setText("Title");
		titleGroup.setLayout(titlePageLayout);
		
		createLabel(titleGroup, "Title", 1);
		 titleText = createText(titleGroup, 1, "title");
		
		createLabel(titleGroup, "Grade Level", 1);
		 gradeLevelText = createText(titleGroup, 1, "gradelevel");
		
		createLabel(titleGroup, "Subtitle", 1);
		 subtitleText = createText(titleGroup, 1, "subtitle");
		
		createLabel(titleGroup, "Series Name", 1);
		 seriesText = createText(titleGroup, 1, "seriesname");
		
		createLabel(titleGroup, "Edition Name or Number", 1);
		 editionText = createText(titleGroup, 1, "editionname");
		
		Group authorGroup = new Group(titleComposite, SWT.NONE);
		authorGroup.setText("Author");
		authorGroup.setLayout(new GridLayout(2,false));
		
		createLabel(authorGroup, "Author(s)", 1);
		 authorText = new Text(authorGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData newData = newTpData(1);
		newData.widthHint = TEXT_BOX_WIDTH;
		newData.heightHint = 70;
		if(xmlmap.get("authors")!=null)
			authorText.setText(xmlmap.get("authors").replaceAll(";", "\r\n"));
		authorText.setLayoutData(newData);
		
		createLabel(authorGroup, "Translator", 1);
		 translatorText = createText(authorGroup, 1, "translator");
		
		Group publisherGroup = new Group(titleComposite, SWT.NONE);
		publisherGroup.setText("Publisher");
		publisherGroup.setLayout(new GridLayout(2,false));
		
		createLabel(publisherGroup, "Permission", 1);
		permissionCombo = new Combo(publisherGroup, SWT.READ_ONLY);
		permissionCombo.setLayoutData(newTpData(1));
		permissionCombo.setItems(new String[]{"Published by", "With permission of the publisher,"});
		permissionCombo.select(0);
		if(xmlmap.get("pubpermission")!=null){
			if (xmlmap.get("pubpermission").equals("0")||xmlmap.get("pubpermission").equals("1"))
				permissionCombo.select(Integer.parseInt(xmlmap.get("pubpermission")));
		}
			
		createLabel(publisherGroup, "Publisher", 1);
		 publisherText = createText(publisherGroup, 1, "publisher");
		
		createLabel(publisherGroup, "City and State", 1);
		 pubLocationText = createText(publisherGroup, 1, "location");
		
		createLabel(publisherGroup, "Website", 1);
		 pubWebsiteText = createText(publisherGroup, 1, "website");
		
		Group printGroup = new Group (titleComposite, SWT.NONE);
		printGroup.setText("Printing Info");
		printGroup.setLayout(new GridLayout(2,false));
		
		copyrightButton = new Button(printGroup, SWT.CHECK);
		copyrightButton.setText("Copyright");
		copyrightButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		if(xmlmap.get("copyrighted")!=null){
			copyrightButton.setSelection(xmlmap.get("copyrighted").equals("true"));
		} else{
			copyrightButton.setSelection(true);
		}
		copySymbolButton = new Button(printGroup, SWT.CHECK);
		copySymbolButton.setText("Copyright Symbol");
		copySymbolButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		if(xmlmap.get("copyrightsymbol")!=null){
			copySymbolButton.setSelection(xmlmap.get("copyrightsymbol").equals("true"));
		} else{
			copySymbolButton.setSelection(true);
		}
		
		createLabel(printGroup, "Copyright Date", 1);
		 copyDateText = createText(printGroup, 1, "copyrightdate");
		
		createLabel(printGroup, "Copyright Text", 1);
		 copyText = createText(printGroup, 1, "copyrighttext");
		
		createLabel(printGroup, "Reproduction Notice", 1);
		 reproText = createText(printGroup, 1, "repronotice");
		
		createLabel(printGroup,"ISBN-13", 1);
		 isbn13Text = createText(printGroup, 1, "isbn13");
		
		createLabel(printGroup, "ISBN-10", 1);
		 isbn10Text = createText(printGroup, 1, "isbn10");
		
		createLabel(printGroup, "Printing History", 1);
		 printHistoryText = createText(printGroup, 1, "printhistory");
		
		Group transcriberGroup = new Group (titleComposite, SWT.NONE);
		transcriberGroup.setText("Transcriber");
		transcriberGroup.setLayout(new GridLayout(2, false));
		
		createLabel(transcriberGroup, "Transcription Year", 1);
		 transYearText = createText(transcriberGroup, 1, "year");
		
		createLabel(transcriberGroup, "Transcriber", 1);
		 transText = createText(transcriberGroup, 1, "transcriber");
		
		createLabel(transcriberGroup, "Tactile Graphics Specialist", 1);
		 tgsText = createText(transcriberGroup, 1, "tgs");
		
		createLabel(transcriberGroup, "Affiliation", 1);
		affiliationText = createText(transcriberGroup, 1, "affiliation");
		
		Group volumesGroup = new Group (titleComposite, SWT.NONE);
		volumesGroup.setText("Volumes");
		volumesGroup.setLayout(new GridLayout(2,false));
		createLabel(volumesGroup, "Not yet implemented", 1);
		
		sc.setContent(titleComposite);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.setMinHeight(500);
		sc.setSize(800, 500);
		titleTab.setControl(sc);
		/////////////////////////////
		
		/////////////////////////////
		/////Special Symbols Tab/////
		/////////////////////////////
		Composite symbolsComposite = new Composite(folder, SWT.NONE);
		symbolsComposite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Group symbolsGroup = new Group(symbolsComposite, SWT.NONE);
		symbolsGroup.setLayout(new GridLayout(1, false));
		
		createLabel(symbolsGroup, "Not yet implemented", 1);
		
		symbolsTab.setControl(symbolsComposite);
		/////////////////////////////
		
		/////////////////////////////
		////Transcriber Notes Tab////
		/////////////////////////////
		Composite transNotesComposite = new Composite(folder, SWT.NONE);
		transNotesComposite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Group transNotesGroup = new Group(transNotesComposite, SWT.NONE);
		transNotesGroup.setLayout(new GridLayout(1,false));
		
		createLabel(transNotesGroup, "Not yet implemented", 1);
		
		transNotesTab.setControl(transNotesComposite);
		/////////////////////////////
		
		Button closeButton = new Button(shlTPages, SWT.PUSH);
		GridData buttonData = new GridData(SWT.RIGHT, SWT.BEGINNING, false, false, 3, 1);
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
		
		Button openButton = new Button(shlTPages, SWT.PUSH);
		buttonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
		buttonData.widthHint = 100;
		buttonData.heightHint = 30;
		openButton.setText("Open");
		openButton.setLayoutData(buttonData);
		
		openButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				FileDialog openFile = new FileDialog(shlTPages, SWT.OPEN);
				openFile.setText("Open Transcriber-Generated Page");
				openFile.setFilterExtensions(new String[] {"*.xml"});
				String filePath = openFile.open();
				if(filePath!=null){
					if(openFromXml(filePath)){
						updateContents();
					} else {
						createError("Error reading file. Was this file created by this dialog?");
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0){
				
			}
		});
		
		Button generateButton = new Button(shlTPages, SWT.PUSH);
		buttonData = new GridData(SWT.LEFT, SWT.BEGINNING, false, false, 1, 1);
		buttonData.widthHint = 100;
		buttonData.heightHint = 30;
		generateButton.setText("Save XML");
		generateButton.setLayoutData(buttonData);
		
		generateButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				authorText.setText(authorText.getText().replaceAll("\r\n", ";"));
				xmlmap.put("authors", authorText.getText());
				xmlmap.put("pubpermission", "" + permissionCombo.getSelectionIndex());
				xmlmap.put("copyrighted", String.valueOf(copyrightButton.getSelection()));
				xmlmap.put("copyrightsymbol", String.valueOf(copySymbolButton.getSelection()));
				for(Map.Entry<Text, String> entry : uimap.entrySet())
					xmlmap.put(entry.getValue(), entry.getKey().getText());
				
				FileDialog saveFile = new FileDialog(shlTPages, SWT.SAVE);
				saveFile.setFilterExtensions(new String[] { "*.xml" });	
				String result = saveFile.open();
				if(result!=null){
					m.setLastTPage(result);
					tpGenerator.saveNewTPage(result, xmlmap);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		shlTPages.pack(true);
		
	}
	
	public void createError(String errorMessage){
		MessageBox errorDialog = new MessageBox(shlTPages, SWT.ICON_ERROR | SWT.OK);
		errorDialog.setText("Error");
		errorDialog.setMessage(errorMessage);
		errorDialog.open();
	}
	
	private void updateContents(){
		for(Map.Entry<Text, String> entry : uimap.entrySet())
			entry.getKey().setText(xmlmap.get(entry.getValue()));
		permissionCombo.select(Integer.parseInt(xmlmap.get("pubpermission")));
		copyrightButton.setSelection(xmlmap.get("copyrighted").equals("true"));
		copySymbolButton.setSelection(xmlmap.get("copyrightsymbol").equals("true"));
		authorText.setText(xmlmap.get("authors").replaceAll(";", "\r\n"));
		affiliationText.setText(xmlmap.get("affiliation"));
	}
	
	private boolean openFromXml(String filepath){
		if(filepath!=null){
			if(tpGenerator.openTPageXML(filepath))
				return true;
		}
		return false;
	}
	private GridData newTpData(int columns){
		return new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, columns, 1);
	}
	
	private Label createLabel(Composite comp, String text, int horizSpan){ 
		Label newLabel = new Label(comp, SWT.NONE);
		newLabel.setText(text);
		GridData labelData = newTpData(horizSpan);
		labelData.widthHint = LABEL_WIDTH;
		newLabel.setLayoutData(labelData);
		return newLabel;
	}
	
	private Text createText(Composite comp, int horizSpan, String xmlmapKey){
		Text newText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		GridData newData = newTpData(horizSpan);
		newData.widthHint = TEXT_BOX_WIDTH;
		newText.setLayoutData(newData);
		if(xmlmapKey!=null){
			if(xmlmap.get(xmlmapKey)!=null){
				newText.setText(xmlmap.get(xmlmapKey));
			}
		}
		uimap.put(newText, xmlmapKey);
		return newText;
	}
}
