package org.brailleblaster.wordprocessor;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.MessageBox;
import org.brailleblaster.BBIni;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;

public class WPManager {

	/**
	 * This is the controller for the whole word processing operation. It is the
	 * entry point for the word processor, and therefore the only public class.
	 */

	private Display display;
	private SettingsDialogs settings;
	private DocumentManager[] documents = new DocumentManager[8];
	private int documentCount = -1;

	/**
	 * This constructor method is the entry point to the word prodessor. It gets
	 * things set up, handles multiple documents, etc.
	 */

	public WPManager() {

		display = BBIni.getDisplay();
		if (display == null) {
			System.exit(1);
		}
		checkLiblouisutdml();
		setListeners();
		addDocument(WP.NewDocument);
	}

	Listener openListener = new Listener() {
		public void handleEvent(Event event) {
			addDocument(event.detail);
		}
	};

	void setListeners() {
		display.addListener(SWT.OpenDocument, openListener);
	}

	void addDocument(int action) {
		documentCount++;
		documents[documentCount] = new DocumentManager(display, action);
		documents[documentCount] = null;
		documentCount--;
	}

	void checkLiblouisutdml() {
		if (BBIni.haveLiblouisutdml()) {
			return;
		}
		final Shell shell = new Shell(display, SWT.DIALOG_TRIM);
		MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO);
		mb.setMessage("The Braille facility is not usable." + " See the log."
				+ " Do you wish to continue?");
		int result = mb.open();
		if (result == SWT.NO) {
			System.exit(1);
		}
		shell.dispose();
	}

}
