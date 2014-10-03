package org.brailleblaster.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;


// Simple message dialog
public class ProgressDialog extends Dialog {
	Object result;
	Shell parent = null;
    Shell shell = null;
    public ProgressBar pb = null;
    Display pardisp = null;
	int progressIndex = 0;
    long timer = System.currentTimeMillis();
	
	public ProgressDialog (Shell parent, int style) {
		super (parent, style);
	}
	public ProgressDialog (Shell parent) {
		this (parent, 0); // your default style bits go here (not the Shell's style bits)
	}
	public void updateProgressBar() {
		if(System.currentTimeMillis() - timer > 100) {
			timer = System.currentTimeMillis();
			pb.setSelection(progressIndex);
			if((progressIndex += 25) > 100)
				progressIndex = 0;
		}
	}
	public Object open (String txt) {
	    parent = getParent();
	    shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.CENTER);
	    shell.setText("Unzipping Document.");
	    Rectangle pr = parent.getBounds();
	    shell.setBounds( pr.x + pr.width / 2 - pr.width / 5 / 2, 
	    				 pr.y + pr.height / 2 - pr.height / 5 / 2, 
			    		 parent.getClientArea().width / 5, 
			    		 parent.getClientArea().height / 5 );
	    
	    Label lbl = new Label(shell, SWT.CENTER);
		lbl.setText(txt);
		lbl.setBounds(shell.getClientArea().width / 2 - shell.getClientArea().width / 2 / 2, 
				 	  shell.getClientArea().height / 3, 
				 	  shell.getClientArea().width / 2, 
				 	  shell.getClientArea().height / 10);
		
		pb = new ProgressBar(shell, SWT.SMOOTH);
		pb.setBounds(shell.getClientArea().width / 2 - shell.getClientArea().width / 2 / 2, 
					 shell.getClientArea().height / 2, 
					 shell.getClientArea().width / 2, 
					 shell.getClientArea().height / 10);
		pb.setVisible(true);
	    
		shell.open();

		Display.getDefault().asyncExec(new Runnable() {
			 public void run() {
			    while (!shell.isDisposed()) {
			    	if (!Display.getDefault().readAndDispatch()) Display.getDefault().sleep();
			    }
			 }
		});	    
	    
	    return result;
	} // open()
	public void close() {
		Display.getDefault().asyncExec(new Runnable() {
			 public void run() {
				shell.close();
			 }
		});
	} // close()
} // class ProgressDialog