package org.brailleblaster.perspectives.falcon;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.ImageHelper;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class FalconToolBar {

	int MAX_W = 32;
	int MAX_H = 32;
	private ToolBar toolBar;
	WPManager wordProc;
	ImageHelper imgHelper;
	FalconController currentEditor;
	
	public FalconToolBar(Shell shell, final WPManager wp, FalconController controller)
	{
		setEditor(controller);
		String sep = BBIni.getFileSep();
		LocaleHandler lh = new LocaleHandler();
		toolBar = new ToolBar(shell, SWT.NONE);
		FormData location = new FormData();
		location.left = new FormAttachment(0);
		location.right = new FormAttachment(40);
		location.top = new FormAttachment(0);
		toolBar.setLayoutData(location);
		wordProc = wp;
		imgHelper = new ImageHelper();
		
		// Calculate max width and height for toolbar buttons.
		Monitor mon[] = Display.getDefault().getMonitors();
		Rectangle screenSize = mon[0].getBounds();
		MAX_W = screenSize.width / 30;
		MAX_H = MAX_W;
		
		// Change font size depending on screen resolution.
		FontData[] oldFontData = toolBar.getFont().getFontData();
		if( screenSize.width >= 1920)
			oldFontData[0].setHeight(9);
		else if( screenSize.width >= 1600)
			oldFontData[0].setHeight(8);
		else if( screenSize.width >= 1280)
			oldFontData[0].setHeight(6);
		else if( screenSize.width >= 1024)
			oldFontData[0].setHeight(4);
		else if( screenSize.width >= 800)
			oldFontData[0].setHeight(3);
		toolBar.setFont( new Font(null, oldFontData[0]) );
		
		// Path to dist folder.
		String distPath = BBIni.getProgramDataPath().substring(0, BBIni.getProgramDataPath().lastIndexOf(sep));
		distPath += sep + "programData";
		
		// FO
		String tlabel;
		ToolItem openItem = new ToolItem(toolBar, SWT.PUSH);
		tlabel = lh.localValue("&Open");
		openItem.setText(tlabel.replace("&", ""));
		openItem.setImage( imgHelper.createScaledImage(new Image(null, distPath + sep + "images" + sep + "open.png"), MAX_W, MAX_H) );
		openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (BBIni.debugging()) {
				} 
				else {
					int index= wp.getFolder().getSelectionIndex();
					if(index == -1){
						wp.addDocumentManager(null);
						setEditor((FalconController)wp.getList().getLast());
						currentEditor.fileOpenDialog();
					}
					else {
						currentEditor.fileOpenDialog();
					}
				}
			}
		});
		
		toolBar.pack();
		
	} // FalconToolBar()
	
	public void setEditor(FalconController editor){
		currentEditor = editor;
	}
	
	public void dispose() {
		toolBar.dispose();
	}
	
} // class FalconToolBar
