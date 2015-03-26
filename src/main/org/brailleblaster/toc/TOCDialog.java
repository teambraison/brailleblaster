package org.brailleblaster.toc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.utd.TableOfContents;
import org.eclipse.swt.widgets.Shell;

import nu.xom.Document;
import nu.xom.Node;
import nu.xom.XPathContext;

public class TOCDialog extends Dialog {
	/**
	 * 
	 */
	private Manager manager;
	private Shell shlTOC;
	protected Object result;
	private final String title = "Table of Contents";
	private String keyword = "generate";
	Display display = null;
	TableOfContents toc = new TableOfContents();
	Document document = null;
	Node location = null;
	
	public TOCDialog(Shell parent, int style, Manager brailleView, Document doc) {
		super(parent, style);
		setText(title);
		manager = brailleView;
		document = doc;
	}
	
	public XPathContext getContext(Document doc) {
		String namespace = doc.getRootElement().getNamespaceURI();
		XPathContext context = new XPathContext("dtb", namespace);
		return context;
	}

	public Object open() {
		createContents();
		shlTOC.open();
		shlTOC.layout();
		
		display = getParent().getDisplay();
		while (!shlTOC.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return result;
	}
	
	private void createContents() {
		shlTOC = new Shell(getParent(), SWT.DIALOG_TRIM);
		setPanelSize();
		
		shlTOC.setText(title);
		shlTOC.setVisible(true);
		
		GridLayout tocLayout = new GridLayout(1, false);
		tocLayout.numColumns = 1;
		tocLayout.marginLeft = 5;
		tocLayout.marginRight = 5;
		tocLayout.marginTop = 10;
		
		shlTOC.setLayout(tocLayout);
		
//		Group group = new Group (shlTOC, SWT.NONE);
//		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
//		group.setLayout(new RowLayout());
//		group.setText("Generate/Update");

		Button genRadioBtn = new Button(shlTOC, SWT.RADIO);
		genRadioBtn.setText("Generate TOC based on headings");
		genRadioBtn.setSelection(true);
		
		Group genGrp = new Group (shlTOC, SWT.NONE);
		
		genRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				genGrp.setEnabled(true);
			}
		});
		
		
		GridData grid = new GridData(SWT.FILL);
		grid.horizontalIndent = 20;
		genGrp.setLayoutData(grid);
		genGrp.setLayout(new RowLayout(SWT.VERTICAL));
		genGrp.setText("Location");
		
		//Default location
		Button pLocRadioBtn = new Button(genGrp, SWT.RADIO);
		pLocRadioBtn.setText("End of p-pages");
		pLocRadioBtn.setSelection(true);
		pLocRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				keyword = "generate";
				//retrieve p node
				location = null;
			}
		});

		
		Button tLocRadioBtn = new Button(genGrp, SWT.RADIO);
		tLocRadioBtn.setText("End of t-pages");
		tLocRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				keyword = "generate";
				//retrieve t node
				location = null;
			}
		});
		
		Button chooseLocRadioBtn = new Button(genGrp, SWT.RADIO);
		chooseLocRadioBtn.setText("Before node:");
		chooseLocRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				keyword = "generate";
				//base location on another button
				location = null;
			}
		});
		
		Button upRadioBtn = new Button(shlTOC, SWT.RADIO);
		upRadioBtn.setText("Update TOC with correct page numbers");
		upRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				keyword = "update";
				location = null;
				genGrp.setEnabled(false);
			}
		});
		
		GridData rightGrid = new GridData();
		rightGrid.horizontalAlignment = GridData.FILL;
		
		MessageBox dialog = new MessageBox(shlTOC, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		dialog.setText("TOC Confirmation");
		dialog.setMessage("The TOC has been added or updated.");
		
		Button confirmBtn = new Button(shlTOC, SWT.NONE);
		confirmBtn.setLayoutData(rightGrid);
		confirmBtn.setText("Confirm");
		shlTOC.setDefaultButton(confirmBtn);
		confirmBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toc.applyTOC(keyword, document, location);
				manager.refresh();
				dialog.open();
				shlTOC.close();
				
			}
		});
		
		Button cancelBtn = new Button(shlTOC, SWT.NONE);
		cancelBtn.setLayoutData(rightGrid);
		cancelBtn.setText("Cancel");
		cancelBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlTOC.close();
				//manager.refresh();
			}
		});
		
		shlTOC.pack(true);
	}
	
	private void setPanelSize() {
		Monitor monitor = shlTOC.getDisplay().getPrimaryMonitor();
		Rectangle bounds = monitor.getBounds();
		
		int x = (bounds.width / 2) - ((bounds.width / 6) / 2);
		int y = (bounds.height / 2) - ((bounds.height / 2) / 2);
		
		shlTOC.setSize(bounds.width / 6, (int) (bounds.height / 3));
		shlTOC.setLocation(x, y);
	}
	
}



























