package org.brailleblaster.tpages;

import java.util.HashMap;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.tpages.TPagesGenerator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class TPagesDialog extends Dialog{
	private Manager m;
	private Shell shlTPages;
	private Object result;
	private Display display;
	final int TEXT_BOX_WIDTH = 400;
	final int LABEL_WIDTH = 150;
	Table symbolsTable;
	Text titleText, gradeLevelText, subtitleText, seriesText, editionText, authorText, translatorText, publisherText, permissionText,
	pubLocationText, pubWebsiteText, copyText, reproText, isbn13Text, isbn10Text, printHistoryText,
	transYearText, transText, tgsText, affiliationText, transNotesText, totalVolText, volNumberText, customBrailleText, 
	braillePagesText, printPagesText, templateText;
	TPagesGenerator tpGenerator;
	HashMap<String, String> xmlmap;
	Group titleGroup, authorGroup, printGroup, publisherGroup, transcriberGroup, volumesGroup;
	Boolean changed = false;
	final String WINDOW_TITLE = "Transcriber-Generated Pages";
	
	public TPagesDialog(Shell parent, int style, Manager brailleViewController){
		super(parent, style);
		setText(WINDOW_TITLE);
		m = brailleViewController;
	}
	
	public Object open(){
		tpGenerator = new TPagesGenerator();
		xmlmap = tpGenerator.getXmlMap();
		createContents();
		
		if(m.getLastTPage()!=null){
			if(tpGenerator.checkForFile(m.getLastTPage())){
				openFromXml(m.getLastTPage());
			}
		}
		updateContents();
		shlTPages.setText(WINDOW_TITLE);
		changed = false;
		
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
		shlTPages.setText(WINDOW_TITLE);
		shlTPages.setVisible(true);
		shlTPages.setLayout(new GridLayout(5,false));
		
		GridData tfData = newTpData(5);
		TabFolder folder = new TabFolder(shlTPages, SWT.NONE);
		folder.setLayoutData(tfData);
		
		TabItem titleTab = new TabItem(folder, SWT.NONE);
		titleTab.setText("Title Page(s)");
		
		TabItem symbolsTab = new TabItem(folder, SWT.NONE);
		symbolsTab.setText("Special Symbols");
		
		TabItem transNotesTab = new TabItem(folder, SWT.NONE);
		transNotesTab.setText("Transcriber's Notes");
		
		TabItem templateTab = new TabItem(folder, SWT.NONE);
		templateTab.setText("Template");
		
		/////////////////////////////
		///////Title Page Tab////////
		/////////////////////////////
		final Composite titleComposite = new Composite(folder, SWT.NONE);
		titleComposite.setLayout(new GridLayout(2, false));
		final Composite listComposite = new Composite(titleComposite, SWT.NONE);
		listComposite.setLayout(new RowLayout(SWT.VERTICAL));
		listComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true, 1, 2));
		final List titlePageList = new List(listComposite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		titlePageList.setLayoutData(new RowData(100, 100));
		titlePageList.add("Title");
		titlePageList.add("Author");
		titlePageList.add("Publisher");
		titlePageList.add("Printing");
		titlePageList.add("Transcription");
		titlePageList.add("Volumes");
		
		Label spacingLabel = new Label(listComposite, SWT.NONE);
		spacingLabel.setLayoutData(new RowData(100, 10));
		
		Button autoFillButton = new Button(listComposite, SWT.PUSH);
		autoFillButton.setText("Auto-Fill Fields");
		autoFillButton.setLayoutData(new RowData(120, 30));
		autoFillButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e){
				if(m.getDocumentName()!=null){
					if(m.getDocumentName().toLowerCase().substring(m.getDocumentName().length()-4).equals(".xml")){
						tpGenerator.autoPopulate(m.getDocumentName());
						updateContents();
					}
				}
			}
			
			@Override 
			public void widgetDefaultSelected(SelectionEvent e){
				
			}
		});
		
		createTitleGroup(titleComposite);
		
		
		titlePageList.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e){
				Boolean tempChanged = changed;
				saveCurrentGroup();
				disposeAll();
				if(titlePageList.getSelection()[0].equals("Title"))
					createTitleGroup(titleComposite);
				if(titlePageList.getSelection()[0].equals("Author"))
					createAuthorGroup(titleComposite);
				if(titlePageList.getSelection()[0].equals("Printing"))
					createPrintGroup(titleComposite);
				if(titlePageList.getSelection()[0].equals("Publisher"))
					createPublisherGroup(titleComposite);
				if(titlePageList.getSelection()[0].equals("Transcription"))
					createTranscriberGroup(titleComposite);
				if(titlePageList.getSelection()[0].equals("Volumes"))
					createVolumesGroup(titleComposite);
				updateContents();
				if(!tempChanged){
					shlTPages.setText(WINDOW_TITLE);
					changed = false;
				}
				shlTPages.pack();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		titlePageList.select(0);
		
		titleTab.setControl(titleComposite);
		/////////////////////////////
		
		/////////////////////////////
		/////Special Symbols Tab/////
		/////////////////////////////
		Composite symbolsComposite = new Composite(folder, SWT.NONE);
		symbolsComposite.setLayout(new RowLayout(SWT.VERTICAL));
		
		symbolsTable = new Table(symbolsComposite, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION); 
		symbolsTable.setLinesVisible(true);
		symbolsTable.setHeaderVisible(true);
		symbolsTable.setLayoutData(new RowData(500, 200));
		TableColumn symbolColumn = new TableColumn(symbolsTable, SWT.NONE);
		symbolColumn.setWidth(100);
		symbolColumn.setText("Symbol");
		TableColumn descColumn = new TableColumn(symbolsTable, SWT.NONE);
		descColumn.setWidth(415);
		descColumn.setText("Description");
		
		Composite ssButtonPanel = new Composite(symbolsComposite, SWT.NONE);
		ssButtonPanel.setLayout(new GridLayout(3,true));
		
		Button ssEditButton = new Button(ssButtonPanel, SWT.PUSH);
		GridData ssButtonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
		ssButtonData.widthHint = 120;
		ssEditButton.setText("Edit...");
		ssEditButton.setLayoutData(ssButtonData);
		ssEditButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e){
				if(symbolsTable.getSelectionCount()>0){
					final Shell editDialog = new Shell(shlTPages, SWT.DIALOG_TRIM);
					editDialog.setLayout(new RowLayout(SWT.VERTICAL));
					editDialog.setText("Edit Special Symbol");
					Composite editContents = new Composite(editDialog, SWT.NONE);
					editContents.setLayout(new GridLayout(2,false));
					
					final TableItem editingItem = symbolsTable.getSelection()[0];
					
					Label symbolLabel = new Label(editContents, SWT.NONE);
					GridData labelData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1);
					labelData.widthHint = 100;
					symbolLabel.setLayoutData(labelData);
					symbolLabel.setText("Symbol:");
					final Text symbolText = new Text(editContents, SWT.SINGLE | SWT.BORDER);
					GridData editTextData = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1);
					editTextData.widthHint = 300;
					symbolText.setLayoutData(editTextData);
					symbolText.setText(editingItem.getText(0));
					
					Label descLabel = new Label(editContents, SWT.NONE);
					labelData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1);
					labelData.widthHint = 100;
					descLabel.setLayoutData(labelData);
					descLabel.setText("Description:");
					final Text descText = new Text(editContents, SWT.NONE);
					editTextData = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1);
					editTextData.widthHint = 300;
					descText.setLayoutData(editTextData);
					descText.setText(editingItem.getText(1));
					
					Composite editButtonPanel = new Composite(editContents, SWT.CENTER);
					editButtonPanel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 2, 1));
					editButtonPanel.setLayout(new GridLayout(2,true));
					
					Button okButton = new Button(editButtonPanel, SWT.PUSH);
					GridData editButtonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
					editButtonData.widthHint = 100;
					okButton.setLayoutData(editButtonData);
					okButton.setText("Ok");
					okButton.addSelectionListener(new SelectionListener(){
						@Override
						public void widgetSelected(SelectionEvent e){
							editingItem.setText(new String[]{symbolText.getText(), descText.getText()});
							editDialog.close();
						}
	
						@Override
						public void widgetDefaultSelected(SelectionEvent arg0) {						
						}
					});
					
					Button cancelButton = new Button(editButtonPanel, SWT.PUSH);
					editButtonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
					editButtonData.widthHint = 100;
					cancelButton.setLayoutData(editButtonData);
					cancelButton.setText("Cancel");
					cancelButton.addSelectionListener(new SelectionListener(){
						@Override
						public void widgetSelected(SelectionEvent e){
							editDialog.close();
						}
	
						@Override
						public void widgetDefaultSelected(SelectionEvent arg0) {
						}
					});
					
					editDialog.open();
					editDialog.pack();
				} 
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		Button ssAddButton = new Button(ssButtonPanel, SWT.PUSH);
		ssButtonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
		ssButtonData.widthHint = 120;
		ssAddButton.setText("Add...");
		ssAddButton.setLayoutData(ssButtonData);
		ssAddButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e){
				final Shell addDialog = new Shell(shlTPages, SWT.DIALOG_TRIM);
				addDialog.setLayout(new RowLayout(SWT.VERTICAL));
				addDialog.setText("Add New Special Symbol");
				Composite addContents = new Composite(addDialog, SWT.NONE);
				addContents.setLayout(new GridLayout(2, false));
				
				Label symbolLabel = new Label(addContents, SWT.NONE);
				GridData labelData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1);
				labelData.widthHint = 100;
				symbolLabel.setLayoutData(labelData);
				symbolLabel.setText("Symbol:");
				final Text symbolText = new Text(addContents, SWT.SINGLE | SWT.BORDER);
				GridData addTextData = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1);
				addTextData.widthHint = 300;
				symbolText.setLayoutData(addTextData);
				
				Label descLabel = new Label(addContents, SWT.NONE);
				labelData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1);
				labelData.widthHint = 100;
				descLabel.setLayoutData(labelData);
				descLabel.setText("Description:");
				final Text descText = new Text(addContents, SWT.SINGLE | SWT.BORDER);
				addTextData = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1);
				addTextData.widthHint = 300;
				descText.setLayoutData(addTextData);
				
				Composite addButtonPanel = new Composite(addContents, SWT.CENTER);
				addButtonPanel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 2, 1));
				addButtonPanel.setLayout(new GridLayout(2,true));
				
				Button okButton = new Button(addButtonPanel, SWT.PUSH);
				GridData addButtonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
				addButtonData.widthHint = 100;
				okButton.setLayoutData(addButtonData);
				okButton.setText("Ok");
				okButton.addSelectionListener(new SelectionListener(){
					@Override
					public void widgetSelected(SelectionEvent e){
						if(symbolText.getText().length()>0){
							TableItem newTableItem = new TableItem(symbolsTable, SWT.NONE);
							newTableItem.setText(new String[]{ symbolText.getText(), descText.getText()});
						}
						addDialog.close();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						
					}
				});
				
				Button cancelButton = new Button(addButtonPanel, SWT.PUSH);
				addButtonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
				addButtonData.widthHint = 100;
				cancelButton.setLayoutData(addButtonData);
				cancelButton.setText("Cancel");
				cancelButton.addSelectionListener(new SelectionListener(){
					@Override
					public void widgetSelected(SelectionEvent e){
						addDialog.close();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						
					}
				});
				
				addDialog.open();
				addDialog.pack();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		Button ssDeleteButton = new Button(ssButtonPanel, SWT.PUSH);
		ssButtonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
		ssButtonData.widthHint = 120;
		ssDeleteButton.setText("Delete");
		ssDeleteButton.setLayoutData(ssButtonData);
		
		ssDeleteButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e){
				if(symbolsTable.getSelectionCount() > 0){
					symbolsTable.remove(symbolsTable.getSelectionIndex());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		symbolsTab.setControl(symbolsComposite);
		/////////////////////////////
		
		/////////////////////////////
		////Transcriber Notes Tab////
		/////////////////////////////
		Composite transNotesComposite = new Composite(folder, SWT.NONE);
		transNotesComposite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Group transNotesGroup = new Group(transNotesComposite, SWT.NONE);
		transNotesGroup.setLayout(new GridLayout(1,false));
		transNotesGroup.setText("Transcriber's Notes");
		
		transNotesText = new Text(transNotesGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData newData = newTpData(1);
		newData.widthHint = 700;
		newData.heightHint = 300;
		transNotesText.setLayoutData(newData);
		
		transNotesTab.setControl(transNotesComposite);
		/////////////////////////////
		
		/////////////////////////////
		////////Template Tab/////////
		/////////////////////////////
		Composite templateComposite = new Composite(folder, SWT.NONE);
		templateComposite.setLayout(new GridLayout(2, false));
		
		final List xmlList = new List(templateComposite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		GridData xmlListData = newTpData(1);
		xmlListData.widthHint = 150;
		xmlListData.heightHint = 300;
		xmlList.setLayoutData(xmlListData);
		for(String element : tpGenerator.getXmlElements()){
			xmlList.add(element);
		}
		xmlList.add("linebreak");
		xmlList.add("pagebreak");
		xmlList.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				templateText.insert("(" + xmlList.getSelection()[0] + ")");
				xmlList.deselectAll();
			}
			
		});
		
		templateText = new Text(templateComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		GridData templateData = newTpData(1);
		templateData.widthHint = 400;
		templateData.heightHint = 300;
		templateText.setLayoutData(templateData);	
		
		templateTab.setControl(templateComposite);
		/////////////////////////////
		
		/////////////////////////////
		///////////Buttons///////////
		/////////////////////////////
		Button closeButton = new Button(shlTPages, SWT.PUSH);
		GridData buttonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
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
						xmlmap = tpGenerator.getXmlMap();
						updateContents();
						changed = false;
						shlTPages.setText(WINDOW_TITLE);
					} else {
						createError("Improper format");
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0){
				
			}
		});
		
		Button saveXMLButton = new Button(shlTPages, SWT.PUSH);
		buttonData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
		buttonData.widthHint = 100;
		buttonData.heightHint = 30;
		saveXMLButton.setText("Save");
		saveXMLButton.setLayoutData(buttonData);
		
		saveXMLButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveCurrentGroup();
				FileDialog saveFile = new FileDialog(shlTPages, SWT.SAVE);
				saveFile.setFilterExtensions(new String[] { "*.xml" });	
				String result = saveFile.open();
				if(result!=null){
					m.setLastTPage(result);
					tpGenerator.saveNewTPage(result, xmlmap);
					changed = false;
					shlTPages.setText(WINDOW_TITLE);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		Label spacingLabel2 = new Label(shlTPages, SWT.NONE);
		GridData spacingLabelData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 1);
		spacingLabelData.widthHint = 200;
		spacingLabel2.setLayoutData(spacingLabelData);
		
		Button generateButton = new Button(shlTPages, SWT.PUSH);
		buttonData = new GridData(SWT.RIGHT, SWT.BEGINNING, false, false, 1, 1);
		buttonData.widthHint = 120;
		buttonData.heightHint = 30;
		generateButton.setText("Generate T-Pages");
		generateButton.setLayoutData(buttonData);
		
		generateButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				saveCurrentGroup();
				FileDialog saveFile = new FileDialog(shlTPages, SWT.SAVE);
				saveFile.setFilterExtensions(new String[] {"*.xml"});
				String result = saveFile.open();
				if(result!=null){
					m.setLastTPage(result);
					tpGenerator.generateTPage(templateText.getText(), result);
				}
			}
			
		});
		/////////////////////////////
		
		shlTPages.addListener(SWT.Close, new Listener(){
			public void handleEvent(Event event){
				if(changed){
					MessageBox changedDialog = new MessageBox(shlTPages, SWT.ICON_ERROR | SWT.YES | SWT.NO);
					changedDialog.setText("Close without saving");
					changedDialog.setMessage("Close without saving?");
					if(changedDialog.open()==SWT.YES)
						event.doit=true;
					else
						event.doit=false;
				} else
					event.doit=true;
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
		if(titleText!=null && !titleText.isDisposed())
			titleText.setText(xmlmap.get("booktitle"));
		if(gradeLevelText!=null && !gradeLevelText.isDisposed())
			gradeLevelText.setText(xmlmap.get("gradelevel"));
		if(subtitleText!=null && !subtitleText.isDisposed())
			subtitleText.setText(xmlmap.get("subtitle"));
		if(seriesText!=null && !seriesText.isDisposed())
			seriesText.setText(xmlmap.get("seriesname"));
		if(editionText!=null && !editionText.isDisposed())
			editionText.setText(xmlmap.get("editionname"));
		if(authorText!=null && !authorText.isDisposed())
			authorText.setText(xmlmap.get("authors").replaceAll(";", "\r\n"));
		if(translatorText!=null && !translatorText.isDisposed())
			translatorText.setText(xmlmap.get("translator"));
		if(permissionText!=null && !permissionText.isDisposed())
			permissionText.setText(xmlmap.get("publisherpermission"));
		if(publisherText!=null && !publisherText.isDisposed())
			publisherText.setText(xmlmap.get("publisher"));
		if(pubLocationText!=null && !pubLocationText.isDisposed())
			pubLocationText.setText(xmlmap.get("publisherlocation"));
		if(pubWebsiteText!=null && !pubWebsiteText.isDisposed())
			pubWebsiteText.setText(xmlmap.get("publisherwebsite"));
		if(copyText!=null && !copyText.isDisposed())
			copyText.setText(xmlmap.get("copyrighttext"));
		if(reproText!=null && !reproText.isDisposed())
			reproText.setText(xmlmap.get("reproductionnotice"));
		if(isbn13Text!=null && !isbn13Text.isDisposed())
			isbn13Text.setText(xmlmap.get("isbn13"));
		if(isbn10Text!=null && !isbn10Text.isDisposed())
			isbn10Text.setText(xmlmap.get("isbn10"));
		if(printHistoryText!=null && !printHistoryText.isDisposed())
			printHistoryText.setText(xmlmap.get("printhistory"));
		if(transYearText!=null && !transYearText.isDisposed())
			transYearText.setText(xmlmap.get("transcriptionyear"));
		if(transText!=null && !transText.isDisposed())
			transText.setText(xmlmap.get("transcriber"));
		if(tgsText!=null && !tgsText.isDisposed())
			tgsText.setText(xmlmap.get("tgs"));
		if(affiliationText!=null && !affiliationText.isDisposed())
			affiliationText.setText(xmlmap.get("affiliation"));
		if(totalVolText!=null&&!totalVolText.isDisposed())
			totalVolText.setText(xmlmap.get("totalvolumes"));
		if(volNumberText!=null&&!volNumberText.isDisposed())
			volNumberText.setText(xmlmap.get("volumenumber"));
		if(customBrailleText!=null&&!customBrailleText.isDisposed())
			customBrailleText.setText(xmlmap.get("customizedbraille"));
		if(braillePagesText!=null&&!braillePagesText.isDisposed())
			braillePagesText.setText(xmlmap.get("braillepageinfo"));
		if(printPagesText!=null&&!printPagesText.isDisposed())
			printPagesText.setText(xmlmap.get("printpageinfo"));
		if(transNotesText!=null&&!transNotesText.isDisposed())
			transNotesText.setText(xmlmap.get("transcribernotes"));
		if(templateText!=null&&!templateText.isDisposed())
			if(!xmlmap.get("template").equals(""))
				templateText.setText(xmlmap.get("template"));
		if(symbolsTable != null && !symbolsTable.isDisposed())
			stringToTable(xmlmap.get("specialsymbols"), symbolsTable);
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
	
	private void createTitleGroup(Composite comp){
		GridLayout titlePageLayout = new GridLayout(2,false);
		titleGroup = new Group(comp, SWT.NONE);
		titleGroup.setText("Title");
		titleGroup.setLayout(titlePageLayout);
		createLabel(titleGroup, "Title", 1);
		titleText = createText(titleGroup, 1, "booktitle");
		createLabel(titleGroup, "Grade Level", 1);
		gradeLevelText = createText(titleGroup, 1, "gradelevel");
		createLabel(titleGroup, "Subtitle", 1);
		subtitleText = createText(titleGroup, 1, "subtitle");
		createLabel(titleGroup, "Series Name", 1);
		seriesText = createText(titleGroup, 1, "seriesname");
		createLabel(titleGroup, "Edition Name or Number", 1);
		editionText = createText(titleGroup, 1, "editionname");
		comp.layout(true);
	}
	
	private void createAuthorGroup(Composite comp){
		authorGroup = new Group(comp, SWT.NONE);
		authorGroup.setText("Author");
		authorGroup.setLayout(new GridLayout(2,false));
		
		createLabel(authorGroup, "Author(s)", 1);
		 authorText = new Text(authorGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData newData = newTpData(1);
		newData.widthHint = TEXT_BOX_WIDTH - 17;
		newData.heightHint = 70;
		if(xmlmap.get("authors")!=null)
			authorText.setText(xmlmap.get("authors").replaceAll(";", "\r\n"));
		authorText.setLayoutData(newData);
		
		createLabel(authorGroup, "Translator", 1);
		translatorText = createText(authorGroup, 1, "translator");
		comp.layout(true);
	}
	
	private void createPublisherGroup(Composite comp){
		publisherGroup = new Group(comp, SWT.NONE);
		publisherGroup.setText("Publisher");
		publisherGroup.setLayout(new GridLayout(2,false));
		
		createLabel(publisherGroup, "Permission", 1);
		permissionText = createText(publisherGroup, 1, "publisherpermission");
		createLabel(publisherGroup, "Publisher", 1);
		publisherText = createText(publisherGroup, 1, "publisher");
		createLabel(publisherGroup, "City and State", 1);
		pubLocationText = createText(publisherGroup, 1, "publisherlocation");
		createLabel(publisherGroup, "Website", 1);
		pubWebsiteText = createText(publisherGroup, 1, "publisherwebsite");
		comp.layout(true);
	}
	
	private void createPrintGroup(Composite comp){
		printGroup = new Group (comp, SWT.NONE);
		printGroup.setText("Printing Info");
		printGroup.setLayout(new GridLayout(2,false));
		
		createLabel(printGroup, "Copyright Text", 1);
		 copyText = createText(printGroup, 1, "copyrighttext");
		
		createLabel(printGroup, "Reproduction Notice", 1);
		 reproText = createText(printGroup, 1, "reproductionnotice");
		
		createLabel(printGroup,"ISBN-13", 1);
		 isbn13Text = createText(printGroup, 1, "isbn13");
		
		createLabel(printGroup, "ISBN-10", 1);
		 isbn10Text = createText(printGroup, 1, "isbn10");
		
		createLabel(printGroup, "Printing History", 1);
		 printHistoryText = createText(printGroup, 1, "printhistory");
		 comp.layout(true);
	}
	
	private void createTranscriberGroup(Composite comp){
		transcriberGroup = new Group (comp, SWT.NONE);
		transcriberGroup.setText("Transcriber");
		transcriberGroup.setLayout(new GridLayout(2, false));
		
		createLabel(transcriberGroup, "Transcription Year", 1);
		 transYearText = createText(transcriberGroup, 1, "transcriptionyear");
		
		createLabel(transcriberGroup, "Transcriber", 1);
		 transText = createText(transcriberGroup, 1, "transcriber");
		
		createLabel(transcriberGroup, "Tactile Graphics Specialist", 1);
		 tgsText = createText(transcriberGroup, 1, "tgs");
		
		createLabel(transcriberGroup, "Affiliation", 1);
		affiliationText = createText(transcriberGroup, 1, "affiliation");
		comp.layout(true);
	}
	
	private void createVolumesGroup(Composite comp){
		volumesGroup = new Group (comp, SWT.NONE);
		volumesGroup.setText("Volumes");
		volumesGroup.setLayout(new GridLayout(2, false));
		
		createLabel(volumesGroup, "Total Number of Volumes", 1);
		totalVolText = createText(volumesGroup, 1, "totalvolumes");
		createLabel(volumesGroup, "Volume Number", 1);
		volNumberText = createText(volumesGroup, 1, "volumenumber");
		createLabel(volumesGroup, "Customized Braille", 1);
		customBrailleText = createText(volumesGroup, 1, "customizedbraille");
		createLabel(volumesGroup, "Braille Page Info",1);
		braillePagesText = createText(volumesGroup,1,"braillepageinfo");
		createLabel(volumesGroup, "Print Page Info", 1);
		printPagesText = createText(volumesGroup,1,"printpageinfo");
		comp.layout(true);
	}
	
	private void disposeAll(){
		if(titleGroup!=null&&!titleGroup.isDisposed())
			titleGroup.dispose();
		if(authorGroup!=null&&!authorGroup.isDisposed())
			authorGroup.dispose();
		if(publisherGroup!=null&&!publisherGroup.isDisposed())
			publisherGroup.dispose();
		if(printGroup!=null&&!printGroup.isDisposed())
			printGroup.dispose();
		if(transcriberGroup!=null&&!transcriberGroup.isDisposed())
			transcriberGroup.dispose();
		if(volumesGroup!=null&&!volumesGroup.isDisposed())
			volumesGroup.dispose();
	}
	
	private void saveCurrentGroup(){
		if(!titleText.isDisposed())
			xmlmap.put("booktitle", titleText.getText());
		if(!gradeLevelText.isDisposed())
			xmlmap.put("gradelevel", gradeLevelText.getText());
		if(!subtitleText.isDisposed())
			xmlmap.put("subtitle", subtitleText.getText());
		if(!seriesText.isDisposed())
			xmlmap.put("seriesname", seriesText.getText());
		if(!editionText.isDisposed())
			xmlmap.put("editionname", editionText.getText());
		if(authorText!=null && !authorText.isDisposed())
			xmlmap.put("authors", authorText.getText().replaceAll("\r\n", ";"));
		if(translatorText!=null && !translatorText.isDisposed())
			xmlmap.put("translator", translatorText.getText());
		if(permissionText!=null && !permissionText.isDisposed())
			xmlmap.put("publisherpermission", permissionText.getText());
		if(publisherText!=null && !publisherText.isDisposed())
			xmlmap.put("publisher", publisherText.getText());
		if(pubLocationText!=null && !pubLocationText.isDisposed())
			xmlmap.put("publisherlocation", pubLocationText.getText());
		if(pubWebsiteText!=null && !pubWebsiteText.isDisposed())
			xmlmap.put("publisherwebsite", pubWebsiteText.getText());
		if(copyText!=null && !copyText.isDisposed())
			xmlmap.put("copyrighttext", copyText.getText());
		if(reproText!=null && !reproText.isDisposed())
			xmlmap.put("reproductionnotice", reproText.getText());
		if(isbn13Text!=null && !isbn13Text.isDisposed())
			xmlmap.put("isbn13", isbn13Text.getText());
		if(isbn10Text!=null && !isbn10Text.isDisposed())
			xmlmap.put("isbn10", isbn10Text.getText());
		if(printHistoryText!=null && !printHistoryText.isDisposed())
			xmlmap.put("printhistory", printHistoryText.getText());
		if(transYearText!=null && !transYearText.isDisposed())
			xmlmap.put("transcriptionyear", transYearText.getText());
		if(transText!=null && !transText.isDisposed())
			xmlmap.put("transcriber", transText.getText());
		if(tgsText!=null && !tgsText.isDisposed())
			xmlmap.put("tgs", tgsText.getText());
		if(affiliationText!=null && !affiliationText.isDisposed())
			xmlmap.put("affiliation", affiliationText.getText());
		if(totalVolText!=null&&!totalVolText.isDisposed())
			xmlmap.put("totalvolumes", totalVolText.getText());
		if(volNumberText!=null&&!volNumberText.isDisposed())
			xmlmap.put("volumenumber", volNumberText.getText());
		if(customBrailleText!=null&&!customBrailleText.isDisposed())
			xmlmap.put("customizedbraille", customBrailleText.getText());
		if(braillePagesText!=null&&!braillePagesText.isDisposed())
			xmlmap.put("braillepageinfo", braillePagesText.getText());
		if(printPagesText!=null&&!printPagesText.isDisposed())
			xmlmap.put("printpageinfo",printPagesText.getText());
		if(transNotesText!=null&&!transNotesText.isDisposed())
			xmlmap.put("transcribernotes", transNotesText.getText());
		if(templateText!=null&&!templateText.isDisposed())
			if(templateText.getText().length() > 0)
				xmlmap.put("template", templateText.getText());
		if(symbolsTable != null && !symbolsTable.isDisposed()){
			xmlmap.put("specialsymbols", tableToString(symbolsTable));
		}
	}
	
	/*Used when creating tpage xml file*/
	
	private String tableToString(Table table){
		String returnString = "";
		for(int i = 0; i < table.getItems().length; i++){
			TableItem item = table.getItem(i);
			returnString += item.getText(0) + "|" + item.getText(1) + "||"; // "|" is used because it has no meaning in ASCII Braille
		}
		return returnString;
	}
	
	/*Used when creating final tpage */
	private String tableToTpage(Table table){
		String returnString = "";
		for(int i = 0; i < table.getItems().length; i++){
			TableItem item = table.getItem(i);
			returnString += item.getText(0) + " " + item.getText(1) + "\r\n";
		}
		return returnString;
	}
	
	/*Used when reading xml file*/
	
	private void stringToTable(String string, Table table){
		if(string.contains("||")){
			String[] splitString = string.split("\\|\\|"); // "|" has to be escaped because it is a regex character
			for(String newString : splitString){
				if(newString.length()>0){
					String[] secondSplit = newString.split("\\|");
					if(secondSplit.length>0){
						TableItem newItem = new TableItem(table, SWT.NONE);
						newItem.setText(secondSplit);
					}
				}
			}
		}
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
		newText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e){
				shlTPages.setText(WINDOW_TITLE + "*");
				changed = true;
			}
		});
		return newText;
	}
}
