package org.brailleblaster.perspectives;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.brailleblaster.BBIni;
import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.EPub3Archiver;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.PropertyFileManager;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author cmyers
 *
 */
public abstract class Controller implements DocumentManager{
	protected final String templateFile = BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "textFileTemplate.html";
	
	protected TabItem item;
	protected WPManager wp;

	protected static int docCount = 0;

	protected FileUtils fu;
	protected Archiver arch;
	
	// Can be SWT.YES/NO/CANCEL. Use in derived DocumentManager.close().
	// close() should catch return value of a dialog asking user if changes should be saved.
	protected int saveDialogState = -1; 
	
	public Controller(WPManager wp){
		this.wp = wp;
		fu = new FileUtils();
	}
	
	/////////////////////////////////////////////////////////////////////	
	// Checks the settings file for values belonging to auto save.
	// Determines if we should restore, and returns path to 
	// previous file. Null if we shouldn't restore.
	public String docRestore()
	{
		String returnPath = null;
		if( wp.getList().size() < 1 ) {
			// Restore a previous session?
			PropertyFileManager props = BBIni.getPropertyFileManager();
			String wkPath = props.getProperty("prevWorkingFile");
			String origDocPathStr = props.getProperty("originalDocPath");
			if( origDocPathStr != null ) {
				if(origDocPathStr.length() > 0) {
					File f = new File(wkPath);
					if(f.exists())
						returnPath = origDocPathStr;
					else
						new EPub3Archiver().clearPrevSession();
				} // if(origDocPathStr.length() > 0)
			} // if( origDocPathStr != null )
		} // if( wp.getList().size() < 1 )
		
		// Return the path we found, if any.
		return returnPath;
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
		return arch.getWorkingFilePath();
	}
	
	public Archiver getArchiver(){
		return arch;
	}
	
	public void setCurrentConfig(String config){
		if(arch.getWorkingFilePath() != null)
			arch.setCurrentConfig(config);
	}
	
	public String getCurrentConfig(){
		if(arch == null)
			return BBIni.getDefaultConfigFile();
		else
			return arch.getCurrentConfig();
	}
	
	public boolean documentHasBeenEdited(){
		return arch.getDocumentEdited();
	}
	
	public void setDocumentEdited(boolean edited){
		arch.setDocumentEdited(edited);
	}
	
	////////////////////////////////////////////////////////////////
	// Opens auto config file, and sets the given setting 
	// to given value.
	// 
	// Pass these to settingStr: epub, nimas.
	// 
	// You can pass whatever you want to fileNameStr, but it's 
	// highly recommended you pass the filename of an existing 
	// config file. epub.cfg, nimas.cfg, etc.
	public void setAutoCfg(String settingStr, String fileNameStr)
	{
		// Init and load properties.
		Properties props = new Properties();
		try
		{
			// Load it!
			props.load( new FileInputStream(BBIni.getAutoConfigSettings()) );
			// Set the property.
			props.setProperty(settingStr, fileNameStr);
			// Now write the properties back to the file.
			props.store( new FileOutputStream(BBIni.getAutoConfigSettings()), null );
			
		}
		catch (IOException e) { e.printStackTrace(); }
		
	} // setAutoCfg()
	
	protected String getFileExt(String fileName) {
		String ext = "";
		String fn = fileName.toLowerCase();
		int dot = fn.lastIndexOf(".");
		if (dot > 0) {
			ext = fn.substring(dot + 1);
		}
		return ext;
	}
	
	// After calling DocumentManager.close(), one should be able to call this and get 
	// the return value of a save dialog.
	public int getSaveDlgState() {
		return saveDialogState;
	}
}
