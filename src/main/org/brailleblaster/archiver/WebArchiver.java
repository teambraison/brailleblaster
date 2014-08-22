package org.brailleblaster.archiver;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;

public class WebArchiver extends Archiver{
	private final String templateFile = BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "textFileTemplate.html";
	
	String ext;
	
	WebArchiver(String docToPrepare, boolean restore) {
		super(docToPrepare, restore);
		currentConfig = getAutoCfg("epub");
	
		ext = docToPrepare.substring(docToPrepare.lastIndexOf(".") + 1);
		filterNames = new String[] {ext, "BRF", "UTDML working document", };
		filterExtensions = new String[] {"*." + ext, "*.brf", "*.utd", };

		if(workingDocPath.equals(templateFile))
			originalDocPath = null;
		
		copyFileToTemp(docToPrepare);
	}
	
	/**This constructor is used when saveAs is called
	 * @param oldFile: Path to previous file name used to create a new semantic file
	 * @param newFile: name of document with new name
	 */
	WebArchiver(String oldFile, String newFile, boolean restore) {
		super(newFile, restore);
		currentConfig = getAutoCfg("epub");
	
		ext = newFile.substring(newFile.lastIndexOf(".") + 1);
		filterNames = new String[] {ext, "BRF", "UTDML working document", };
		filterExtensions = new String[] {"*." + ext, "*.brf", "*.utd", };

		if(workingDocPath.equals(templateFile))
			originalDocPath = null;
		
    	copySemanticsForNewFile(oldFile, newFile);
    	
		workingDocPath = BBIni.getTempFilesPath() + BBIni.getFileSep() + newFile.substring(newFile.lastIndexOf(BBIni.getFileSep()) + 1);
	}

	@Override
	public void save(BBDocument doc, String path) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		FileUtils fu = new FileUtils();
		if(path == null)
			path = workingDocPath;
		
		if(fu.createXMLFile(doc.getNewXML(), workingDocPath)){
			fu.copyFile(workingDocPath, originalDocPath);
			String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(path) + ".sem";
			String newSemFile = fu.getPath(originalDocPath) + BBIni.getFileSep() + fu.getFileName(originalDocPath) + ".sem";
			copySemanticsFile(tempSemFile, newSemFile);
		}
		else {
			new Notify("An error occured while saving your document.  Please check your original document.");
		}
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
	}

	private void saveAsWeb(BBDocument doc, String path){
		// Stop the autosave feature until we're done.
		pauseAutoSave();
				
		String tempFilePath = BBIni.getTempFilesPath() + BBIni.getFileSep() + path.substring(path.lastIndexOf(BBIni.getFileSep()) + 1);
				
		FileUtils fu = new FileUtils();
		if(fu.createXMLFile(doc.getDOM(), tempFilePath)){
			fu.copyFile(tempFilePath, path);
			copySemanticsForNewFile(workingDocPath, path);
		}
		else {
			new Notify("An error occured while saving your document.  Please check your original document.");
		}
		
		originalDocPath = path;
		workingDocPath = tempFilePath;
				
		// Resume autosave feature.
		resumeAutoSave(doc, path);
	}
	
	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		if(ext.equals(this.ext))
			saveAsWeb(doc, path);
		else if(ext.equals("brf"))
			saveBrf(doc, path);
		else if(ext.equals("utd"))
			return saveAsUTD(doc, path);
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
		return this;	
	}
	
	private UTDArchiver saveAsUTD(BBDocument doc, String path){
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		UTDArchiver arch = new UTDArchiver(workingDocPath, path, currentConfig, false);
		arch.save(doc, path);
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
		return arch;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// Each Archiver type opens and saves their content in a 
	// different way. They will implement this method to 
	// save their content to the temp folder.
	@Override
	public void backup(BBDocument __doc, String __path) {}
}
