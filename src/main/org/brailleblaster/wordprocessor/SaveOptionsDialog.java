package org.brailleblaster.wordprocessor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;


import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;

enum SaveSelection {
    TEXT_AND_BRAILLE, TEXT_ONLY, CANCELLED
}
public class SaveOptionsDialog extends Dialog {
        SaveSelection result = SaveSelection.CANCELLED;
        private LocaleHandler lh = new LocaleHandler();
        private boolean textAndBraille;
        private BrailleView braille;

                
        public SaveOptionsDialog (Shell parent, int style) {
                super (parent, style);
        }
        public SaveOptionsDialog (Shell parent) {
                this (parent, SWT.NONE);
        }
        public SaveSelection open () {
                Shell parent = getParent();
                Display display = parent.getDisplay();
                final Shell selShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.CENTER);
                selShell.setText(getText());
        selShell.setMinimumSize (300, 150);        

        FillLayout layout = new FillLayout(SWT.VERTICAL);
        layout.marginWidth = 8;
        selShell.setLayout(layout);
        final Composite radioGroup = new Composite(selShell, SWT.NONE);
        radioGroup.setLayout(new RowLayout(SWT.VERTICAL));  
        final Button b1 = new Button (radioGroup, SWT.RADIO);
		b1.setText(lh.localValue("saveTextBraille"));
		b1.setSelection(true);
		b1.pack();
		
		final Button b2 = new Button (radioGroup, SWT.RADIO);
		b2.setText(lh.localValue("saveTextOnly"));
		b2.pack();
		radioGroup.pack();
		Composite c = new Composite(selShell, SWT.NONE);
		RowLayout clayout = new RowLayout();
		clayout.type = SWT.HORIZONTAL;
		clayout.spacing = 30;
		clayout.marginWidth = 8;
		clayout.center = true;
		clayout.pack = true;
		clayout.justify = true;
		c.setLayout(clayout);
		
		Button b3 = new Button (c, SWT.PUSH);
		b3.setText(lh.localValue("buttonSave"));
		b3.pack();
		
		Button b4 = new Button (c, SWT.PUSH);
		b4.setText(lh.localValue("buttonCancel"));
		b4.pack();

		c.pack();
		
		Control tabList[] = new Control[] { radioGroup, c, radioGroup};
		try {
			selShell.setTabList(tabList);
		} catch (IllegalArgumentException e) {
			System.err.println ("setTabList exception " + e.getMessage());
		}
		selShell.setDefaultButton(b3);
		
		selShell.pack();
		
		Monitor primary = display.getPrimaryMonitor ();
		Rectangle bounds = primary.getBounds ();
		Rectangle rect = selShell.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		selShell.setLocation (x, y);

		/* text and Braille */
		b1.addSelectionListener (new SelectionAdapter() {
    		public void widgetSelected (SelectionEvent e) {
    			textAndBraille = true;
    		}
    	});

		/* Text only */
		b2.addSelectionListener (new SelectionAdapter() {
    		public void widgetSelected (SelectionEvent e) {
    			textAndBraille = false;
    		}
    	});

		/* Save */
    	b3.addSelectionListener (new SelectionAdapter() {
    		public void widgetSelected (SelectionEvent e) {

    			if ((b1.getSelection()) || (b2.getSelection()) ) {
    				if (textAndBraille) {
    					result = SaveSelection.TEXT_AND_BRAILLE;
    				} else {
    					result = SaveSelection.TEXT_ONLY;
    				}
    				selShell.dispose();
    			} else {
    				 new Notify (lh.localValue("mustSelect")); 
    			}
    		}
    	});
    	
    	/* Cancel */
    	b4.addSelectionListener (new SelectionAdapter() {
    		public void widgetSelected (SelectionEvent e) {
    	        selShell.dispose();
    		}
    	});


                selShell.open();
                while (!selShell.isDisposed()) {
                        if (!display.readAndDispatch()) display.sleep();
                }
                return result;
        }
}