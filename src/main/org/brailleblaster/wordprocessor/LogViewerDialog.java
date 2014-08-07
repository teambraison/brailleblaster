package org.brailleblaster.wordprocessor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class LogViewerDialog extends Dialog {

	boolean result = false;
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
		dialogShell.setMinimumSize(300, 150);
		FormLayout dialogLayout = new FormLayout();
		dialogShell.setLayout(dialogLayout);;
		Button okButton = new Button(dialogShell, SWT.PUSH);
		FormData okData = new FormData(20,30);
		okData.bottom = new FormAttachment(100, -5);
		okButton.setLayoutData(okData);
		okButton.setText("OK");;
		okButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = true;
				dialogShell.dispose();
			}
			
		});
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
