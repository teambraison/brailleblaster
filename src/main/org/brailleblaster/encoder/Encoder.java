package org.brailleblaster.encoder;

import org.brailleblaster.localization.LocaleHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class Encoder {
		static LocaleHandler lh = new LocaleHandler();
		static String encoding = null;
		// encoding for Import
		public static String getEncodingString(Display display) {
			//String encoding = null;
			
			// final Shell selShell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.CENTER);
			final Shell selShell = new Shell(display, SWT.DIALOG_TRIM | SWT.CENTER);
			selShell.setText(lh.localValue("specifyEncoding"));
			selShell.setMinimumSize(250, 100);

			FillLayout layout = new FillLayout(SWT.VERTICAL);
			layout.marginWidth = 8;
			selShell.setLayout(layout);
			Composite radioGroup = new Composite(selShell, SWT.NONE);
			radioGroup.setLayout(new RowLayout(SWT.VERTICAL));

			Button b1 = new Button(radioGroup, SWT.RADIO);
			b1.setText(lh.localValue("encodeUTF8"));
			b1.setSelection(true); // default
			b1.pack();

			Button b2 = new Button(radioGroup, SWT.RADIO);
			b2.setText(lh.localValue("encodeISO88591"));
			b2.pack();

			Button b3 = new Button(radioGroup, SWT.RADIO);
			b3.setText(lh.localValue("encodeWINDOWS1252"));
			b3.pack();

			Button b4 = new Button(radioGroup, SWT.RADIO);
			b4.setText(lh.localValue("encodeUSASCII"));
			b4.pack();

			// if (SWT.getPlatform().equals("win32") ||
			// SWT.getPlatform().equals("wpf")) {
			// b1.setSelection(false);
			// b3.setSelection(true);
			// }

			radioGroup.pack();

			Composite c = new Composite(selShell, SWT.NONE);
			RowLayout clayout = new RowLayout();
			clayout.type = SWT.HORIZONTAL;
			// clayout.spacing = 30;
			// clayout.marginWidth = 8;
			clayout.marginTop = 20;
			clayout.center = true;
			clayout.pack = true;
			clayout.justify = true;
			c.setLayout(clayout);

			Button bsel = new Button(c, SWT.PUSH);
			bsel.setText(lh.localValue("encodeSelect"));
			bsel.pack();

			c.pack();

			Control tabList[] = new Control[] { radioGroup, c, radioGroup };
			try {
				selShell.setTabList(tabList);
			} 
			catch (IllegalArgumentException e) {
				System.err.println("setTabList exception " + e.getMessage());
			}

			selShell.setDefaultButton(bsel);
			selShell.pack();

			Monitor primary = display.getPrimaryMonitor();
			Rectangle bounds = primary.getBounds();
			Rectangle rect = selShell.getBounds();
			int x = bounds.x + (bounds.width - rect.width) / 2;
			int y = bounds.y + (bounds.height - rect.height) / 2;
			selShell.setLocation(x, y);

			b1.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					encoding = Encodings.UTF_8.encoding();
				}
			});

			b2.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					encoding = Encodings.ISO_8859_1.encoding();
				}
			});

			b3.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					encoding = Encodings.WINDOWS_1252.encoding();
				}
			});

			b4.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					encoding = Encodings.US_ASCII.encoding();
				}
			});

			/* Select */
			bsel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					selShell.dispose();
				}
			});

			selShell.addListener(SWT.Close, new Listener() {
				public void handleEvent(Event event) {
					event.doit = false; // must pick a value
				}
			});

			selShell.open();

			while (!selShell.isDisposed()) {
				if (!display.readAndDispatch()){
					display.sleep();
				}
				// nothing clicked
				if (encoding == null) {
					if (b1.getSelection()) {
						encoding = Encodings.UTF_8.encoding();
					}
					else if (b2.getSelection()) {
						encoding = Encodings.ISO_8859_1.encoding();
					}
					else if (b3.getSelection()) {
						encoding = Encodings.WINDOWS_1252.encoding();
					}
					else if (b4.getSelection()) {
						encoding = Encodings.US_ASCII.encoding();
					}
				}
			}
			return encoding;
		}
}
