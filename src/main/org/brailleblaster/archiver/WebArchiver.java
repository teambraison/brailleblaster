package org.brailleblaster.archiver;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;

public class WebArchiver extends Archiver{
	private final String templateFile = BBIni.getProgramDataPath() + BBIni.getFileSep() + "xmlTemplates" + BBIni.getFileSep() + "textFileTemplate.html";
	
	String ext;
	
	WebArchiver(String docToPrepare) {
		super(docToPrepare);
		currentConfig = getAutoCfg("epub");
	
		ext = docToPrepare.substring(docToPrepare.lastIndexOf(".") + 1);
		filterNames = new String[] {ext, "BRF", "UTDML working document", };
		filterExtensions = new String[] {"*." + ext, "*.brf", "*.utd", };

		if(workingDocPath.equals(templateFile))

			originalDocPath = null;

	}

	@Override
	public void save(BBDocument doc, String path, boolean zip) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		FileUtils fu = new FileUtils();
		if(path == null)
			path = workingDocPath;
		
		if(fu.createXMLFile(doc.getNewXML(), path)){
			String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(path) + ".sem"; 
			copySemanticsFile(tempSemFile, fu.getPath(path) + BBIni.getFileSep() + fu.getFileName(path) + ".sem");
		}
		else {
			new Notify("An error occured while saving your document.  Please check your original document.");
		}
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
	}

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		if(ext.equals(this.ext))
			save(doc, path, true);
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
		
		UTDArchiver arch = new UTDArchiver(path, currentConfig);
		arch.save(doc, path, true);
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
		return arch;
	}
}
