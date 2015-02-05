package org.brailleblaster.wordprocessor;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.imageDescriber.ImageDescriberController;
import org.brailleblaster.util.ImageHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class BBToolBar {
	protected int MAX_W = 32;
	protected int MAX_H = 32;
	
	protected ToolBar toolBar;
	protected WPManager wordProc;
	protected ImageHelper imgHelper;
	protected Controller currentEditor;
	
	protected LocaleHandler lh = null;
	
	protected String sep = null;
	protected String distPath = null;
	
	protected String tlabel = null;
	
	public BBToolBar(Shell shell, final WPManager wp, Controller contrlr) {
		lh = new LocaleHandler();

		wordProc = wp;
		imgHelper = new ImageHelper();
		
		// Calculate max width and height for toolbar buttons.
		Monitor mon[] = Display.getDefault().getMonitors();
		Rectangle screenSize = mon[0].getBounds();
		
		MAX_W = screenSize.width / 30;
		MAX_H = MAX_W;

		sep = BBIni.getFileSep();
		distPath = BBIni.getProgramDataPath().substring(0, BBIni.getProgramDataPath().lastIndexOf(sep));
		distPath += sep + "programData";
		
		toolBar = new ToolBar(shell, SWT.NONE);
		
		FormData location = new FormData();
		location.left = new FormAttachment(0);
		location.right = new FormAttachment(40);
		location.top = new FormAttachment(0);
		toolBar.setLayoutData(location);
	}

	public void setCurrent(Controller controller) {
		currentEditor = controller;
	}
	
	public void setEditor(Controller editor){
		currentEditor = editor;
	}
	
	public void dispose(){
		toolBar.dispose();
	}
	
	protected String fileOpenDialog(){
		String tempName = null;

		if(!BBIni.debugging()){
			String[] filterNames = new String[] { "XML", "XML ZIP", "XHTML", "HTML","HTM", "EPUB", "TEXT", "UTDML working document"};
			String[] filterExtensions = new String[] { "*.xml", "*.zip", "*.xhtml","*.html", "*.htm", "*.epub", "*.txt", "*.utd"};
			BBFileDialog dialog = new BBFileDialog(wordProc.getShell(), SWT.OPEN, filterNames, filterExtensions);
			tempName = dialog.open();
		}
		else
			tempName = BBIni.getDebugFilePath();
		
		return tempName;
	}
}
