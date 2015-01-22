package org.brailleblaster.wordprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewerDialog extends Dialog {

	private LocaleHandler lh;
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
	private static void writeStringToFile(File outFile, String content) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outFile));
			bw.write(content);
		} finally {
			if (bw != null)
				bw.close();
		}
	}

	public LogViewerDialog(Shell parent, int style) {
		super(parent, style);
		lh = new LocaleHandler();
	}

	public LogViewerDialog(Shell parent) {
		this(parent, SWT.NONE);
	}

	public boolean open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();
		final Shell dialogShell = new Shell(parent, SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		dialogShell.setText(getText());
		FormLayout dialogLayout = new FormLayout();
		dialogShell.setLayout(dialogLayout);
		
		// Create the control objects first so we create them in the order for
		// tabbing
		final Text logText = new Text(dialogShell, SWT.BORDER
				| SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		Button saveButton = new Button(dialogShell, SWT.PUSH);
		Button closeButton = new Button(dialogShell, SWT.PUSH);

		FormData closeData = new FormData();
		closeData.height = 20;
		closeData.bottom = new FormAttachment(100, -10);
		// closeData.left = new FormAttachment(saveButton, 5);
		closeData.right = new FormAttachment(100, -15);
		closeButton.setLayoutData(closeData);
		closeButton.setText(lh.localValue("buttonClose"));
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = true;
				dialogShell.dispose();
			}
		});

		FormData saveData = new FormData();
		saveData.height = 20;
		saveData.right = new FormAttachment(closeButton, -10);
		saveData.bottom = new FormAttachment(closeButton, 0, SWT.BOTTOM);
		saveButton.setLayoutData(saveData);
		saveButton.setText(lh.localValue("LogViewer.SaveLog"));
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog saveDialog = new FileDialog(dialogShell, SWT.SAVE);
				saveDialog.setFilterNames(new String[] {"Log file (*.log)"});
				saveDialog.setFilterExtensions(new String[] {"*.log"});
				saveDialog.setFilterPath(System.getProperty("user.home"));
				saveDialog.setOverwrite(true);
				String saveResult = saveDialog.open();
				if (saveResult != null) {
					try {
						writeStringToFile(new File(saveResult), logText.getText());
						MessageBox savedMsg = new MessageBox(dialogShell, SWT.ICON_INFORMATION | SWT.OK);
						savedMsg.setText(lh.localValue("LogViewer.SavedMsgBox.Title"));
						savedMsg.setMessage(lh.localValue("LogViewer.SavedMsgBox.Message"));
						savedMsg.open();
					} catch(IOException e) {
						MessageBox saveErrorMsg = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
						saveErrorMsg.setText(lh.localValue("LogViewer.SaveErrorMsgBox.Title"));
						saveErrorMsg.setMessage(lh.localValue("LogViewer.SaveErrorMsgBox.Message"));
						saveErrorMsg.open();
					}
					
				}
			}
		});
		saveButton.pack();
		closeData.width = saveButton.getSize().x;
		FormData logTextData = new FormData();
		logTextData.top = new FormAttachment(0, 5);
		logTextData.left = new FormAttachment(0, 5);
		logTextData.right = new FormAttachment(100, -5);
		logTextData.width = 400;
		logTextData.bottom = new FormAttachment(closeButton, -15);
		logTextData.height = 400;
		logText.setLayoutData(logTextData);
		logText.setEditable(false);
		try {
			logText.setText(readFileToString(new File(BBIni.getLogFilesPath(),
					"bb.log")));
		} catch (IOException e) {
			log.error("Problem opening the log file", e);
			MessageBox msgBox = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK);
			msgBox.setText("Unable to open log file");
			msgBox.setMessage("There was a problem in reading the log file, so the log viewer will not be opened.");
			result = false;
			return result;
		}
		// logText.setKeyBinding('a' | SWT.MOD1, ST.SELECT_ALL);
		logText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = true;
				if ((e.stateMask == SWT.MOD1) && (e.keyCode == 'a')) {
					logText.selectAll();
					e.doit = false;
				}
			}
		});
		dialogShell.pack();
		dialogShell.open();
		while (!dialogShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
}
