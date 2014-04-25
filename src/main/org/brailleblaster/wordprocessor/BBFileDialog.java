package org.brailleblaster.wordprocessor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class BBFileDialog {
	private FileDialog dialog;
	
	public BBFileDialog(Shell shell, int type, String[] filterNames, String[] filterExtensions){
		dialog = new FileDialog(shell, type);
		String filterPath = "/";
		
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterPath = System.getProperty("user.home");
			if (filterPath == null){
				filterPath = "c:\\";
			}
		}
		else
			filterPath = System.getProperty("user.home");
		
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFilterPath(filterPath);
	}
	
	public String open(){
		return dialog.open();
	}
}
