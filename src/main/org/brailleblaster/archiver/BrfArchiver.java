package org.brailleblaster.archiver;

import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.Notify;

//not yet fully implemented
public class BrfArchiver extends Archiver{

	BrfArchiver(String docToPrepare) {
		super(docToPrepare);
		currentConfig = "preferences.cfg";
		//if implemented, set currentConfig
	}

	@Override
	public void save(BBDocument doc, String path) {
		if(path == null)
			path = workingDocPath;
		if(!doc.createBrlFile(path)){
			new Notify("An error has occurred.  Please check your original document");
		}
	}

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
