package org.brailleblaster.wordprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;

import org.brailleblaster.BBIni;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewerDialog extends Dialog {

	private static Logger log = LoggerFactory.getLogger(LogViewerDialog.class);
	boolean result = false;
	private static String readFileToString(File inFile) throws IOException {
		StringBuffer buf = new StringBuffer();
		String line = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inFile));
			while ((line = br.readLine()) != null) {
				buf.append(line);
				buf.append('\n');
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return buf.toString();
	}
	public LogViewerDialog(Shell parent, int style) {
		super(parent, style);
	}
	public LogViewerDialog(Shell parent) {
		this(parent, SWT.NONE);
	}
	public boolean open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();
		final Shell dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.CENTER);
		dialogShell.setText(getText());
		dialogShell.setMinimumSize(300, 400);
		FormLayout dialogLayout = new FormLayout();
		dialogShell.setLayout(dialogLayout);;
		Button closeButton = new Button(dialogShell, SWT.PUSH);
		FormData closeData = new FormData(30,20);
		closeData.bottom = new FormAttachment(100, -5);
		closeData.left = new FormAttachment(50,-15);
		closeButton.setLayoutData(closeData);
		closeButton.setText("Close");;
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = true;
				dialogShell.dispose();
			}
		});
		StyledText logText = new StyledText(dialogShell, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		FormData logTextData = new FormData();
		logTextData.top = new FormAttachment(0, 5);
		logTextData.left = new FormAttachment(0, 5);
		logTextData.right = new FormAttachment(100, -5);
		logTextData.bottom = new FormAttachment(closeButton, -10);
		logText.setLayoutData(logTextData);
		logText.setEditable(false);
		try {
		logText.setText(readFileToString(new File(BBIni.getLogFilesPath(), "bb.log")));
		} catch(IOException e) {
			log.error("Problem opening the log file", e);
			MessageBox msgBox = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK);
			msgBox.setText("Unable to open log file");
			msgBox.setMessage("There was a problem in reading the log file, so the log viewer will not be opened.");
			result = false;
			return result;
		}
		dialogShell.setTabList(new Control[] {logText, closeButton, logText});
		dialogShell.pack();
		dialogShell.open();
		while(!dialogShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
}
