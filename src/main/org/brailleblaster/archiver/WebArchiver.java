package org.brailleblaster.archiver;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;

public class WebArchiver extends Archiver{
	String ext;
	
	WebArchiver(String docToPrepare) {
		super(docToPrepare);
		currentConfig = getAutoCfg("epub");
		ext = docToPrepare.substring(docToPrepare.lastIndexOf(".") + 1);
		filterNames = new String[] {ext, "BRF", "UTDML working document", };
		filterExtensions = new String[] {"*." + ext, "*.brf", "*.utd", };
	}

	@Override
	public void save(BBDocument doc, String path) {
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
	}

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		if(ext.equals(this.ext))
			save(doc, path);
		else if(ext.equals("brf"))
			saveBrf(doc, path);
		else if(ext.equals("utd"))
			return saveAsUTD(doc, path);
		
		return this;	
	}
	
	private UTDArchiver saveAsUTD(BBDocument doc, String path){
		UTDArchiver arch = new UTDArchiver(path, currentConfig);
		arch.save(doc, path);
		return arch;
	}
}
