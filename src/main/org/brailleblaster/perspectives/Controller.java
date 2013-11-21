package org.brailleblaster.perspectives;

import java.io.File;
import java.util.ArrayList;

import org.brailleblaster.BBIni;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Zipper;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.widgets.TabItem;

public abstract class Controller implements DocumentManager{
	protected TabItem item;
	protected WPManager wp;
	protected String workingFilePath;
	protected String currentConfig;
	protected static int docCount = 0;
	protected boolean documentEdited = false;
	protected String zippedPath;
	protected FileUtils fu;
	
	public Controller(WPManager wp, String fileName){
		this.wp = wp;
		
		if(fileName != null)
			currentConfig = BBIni.getDefaultConfigFile();
		else 
			currentConfig = "nimas.cfg";
		
		this.fu = new FileUtils();
	}
	
	protected void copySemanticsFile(String tempSemFile, String savedFilePath) {
		if(fu.exists(tempSemFile)){
    		fu.copyFile(tempSemFile, savedFilePath);
    	}
	}
	
	protected void addRecentFileEntry(String fileName){
		
		// Make sure there is a main menu up before we go messing with it.
		if(wp.getMainMenu() == null)
			return;
		
		////////////////
		// Recent Files.
			
		// Get recent file list.
		ArrayList<String> strs = wp.getMainMenu().getRecentDocumentsList();
			
		// Search list for duplicate. If one exists, don't add this new one.
		for(int curStr = 0; curStr < strs.size(); curStr++) {
			if(strs.get(curStr).compareTo(fileName) == 0) {
					
				// This isn't a new document. First, remove from doc list and recent item submenu.
				wp.getMainMenu().getRecentDocumentsList().remove(curStr);
				wp.getMainMenu().getRecentItemSubMenu().getItem(curStr).dispose();
					
				// We found a duplicate, so there is no point in going further.
				break;
					
			} // if(strs.get(curStr)...
				
		} // for(int curStr = 0...
			
		// Add to top of recent items submenu.
		wp.getMainMenu().addRecentEntry(fileName);
			
		// Recent Files.
		////////////////
	}
	
	protected  void zipDocument(){
		// Create zipper.
		Zipper zpr = new Zipper();
		// Input string.
		String sp = BBIni.getFileSep();
		String inPath = BBIni.getTempFilesPath() + zippedPath.substring(zippedPath.lastIndexOf(sp), zippedPath.lastIndexOf(".")) + sp;
//		String inPath = zippedPath.substring(0, zippedPath.lastIndexOf(".")) + BBIni.getFileSep();
		// Zip it!
		zpr.Zip(inPath, zippedPath);
	}
	
	protected void setTabTitle(String pathName) {
		if(pathName != null){
			int index = pathName.lastIndexOf(File.separatorChar);
			if (index == -1) {
				item.setText(pathName);
			} 
			else {
				item.setText(pathName.substring(index + 1));
			}
		}
		else {
			if(docCount == 1){
				item.setText("Untitled");			
			}
			else {
				item.setText("Untitled #" + docCount);
			}
		}
	}
	
	public String getWorkingPath(){
		return workingFilePath;
	}
	
	public String getCurrentConfig(){
		return currentConfig;
	}
	
	public boolean documentHasBeenEdited(){
		return documentEdited;
	}
	
	public void setDocumentEdited(boolean edited){
		documentEdited = edited;
	}
}
