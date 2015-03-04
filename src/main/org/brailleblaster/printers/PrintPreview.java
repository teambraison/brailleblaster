package org.brailleblaster.printers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


public class PrintPreview {
	Shell shell;
	BBDocument doc;
	PreviewText previewText;
	
	final int MARGINS = 38;
	final int PAGE_WIDTH = 619;
	final int PAGE_HEIGHT = 825;
	
	private class PreviewText {
		String text;
		File f;
		StyledText view;
		
		public PreviewText(Manager manager, Group group){
			view = new StyledText(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			view.setEditable(false);
			setLayout(view, 0, 100, 0, 100);
		}

		public void setPreviewText(Manager dm, BBDocument doc){
			String tempFilePath = BBIni.getTempFilesPath() + BBIni.getFileSep() + "tempBRF.brf"; 
			if(doc.createBrlFile(tempFilePath)){
				try {
					this.f = new File(tempFilePath);
					Scanner scanner = new Scanner(this.f);
					this.text = scanner.useDelimiter("\\Z").next();
					scanner.close();
					this.view.setText(this.text);
					Font font = dm.getFontManager().getFont(dm.isSimBrailleDisplayed());
					this.view.setFont(font);
					this.view.setMargins(MARGINS, MARGINS, MARGINS, MARGINS);
					this.view.getShell().open();
				} catch (FileNotFoundException e) {
					new Notify("Print Preview failed to open properly.  Check to ensure your document does not contain errors");
					e.printStackTrace();
					this.f.delete();
					this.view.getShell().dispose();
				}
			}
			else {
				new Notify("An error has occurred.  Please check your original document");
			}
		}
		
		public void deleteTempFile(){
			this.f.delete();
		}
	}
	
	public PrintPreview(Display display, BBDocument doc, Manager manager){
		this.doc = doc;
		
		this.shell = new Shell(display, SWT.SHELL_TRIM);
		this.shell.setLayout(new FormLayout());
		this.shell.setText("Braille Preview");
		this.shell.setSize(PAGE_WIDTH, PAGE_HEIGHT);
		
		Group gp = new Group(this.shell, SWT.NONE);
		FormData location = new FormData();
	    location.left = new FormAttachment(0);
	    location.right = new FormAttachment(100);
	    location.top = new FormAttachment (0);
	    location.bottom = new FormAttachment(100);
	    gp.setLayoutData (location);
		gp.setLayout(new FormLayout());
		
		this.previewText = new PreviewText(manager, gp);
		
		previewText.view.addVerifyKeyListener(new VerifyKeyListener(){
			@Override
			public void verifyKey(VerifyEvent e) {
				if(e.stateMask == SWT.MOD1 && e.keyCode == 'q'){
					close();
					e.doit = false;
				}
			}
		});
		
		shell.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.keyCode == SWT.ESC)
					close();
			}
		});
		
		this.shell.addListener(SWT.Close, new Listener() { 
			@Override
			public void handleEvent(Event event) { 
				previewText.deleteTempFile();
				shell.dispose();
	        } 
	     });	
		
		this.previewText.setPreviewText(manager, this.doc);	
	}
	
	protected void setLayout(Control c, int left, int right, int top, int bottom){
		FormData location = new FormData();
		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);
		c.setLayoutData(location);
	}
	
	private void close(){
		previewText.deleteTempFile();
		shell.dispose();
	}
}
