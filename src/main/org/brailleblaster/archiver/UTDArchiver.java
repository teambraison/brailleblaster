package org.brailleblaster.archiver;

import java.io.File;
import java.io.IOException;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.BBDocument;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.Notify;

public class UTDArchiver extends Archiver{

	UTDArchiver(String docToPrepare) {
		super(docToPrepare);
		filterNames = new String[] { "BRF", "UTDML working document"};
		filterExtensions = new String[] { "*.brf", "*.utd"};
		currentConfig = findConfig();
	}
	
	UTDArchiver(String docToPrepare, String config){
		super(docToPrepare);
		currentConfig = config;
		filterNames = new String[] { "TEXT", "BRF", "UTDML working document"};
		filterExtensions = new String[] { "*.txt", "*.brf", "*.utd"};
	}

	@Override
	public void save(BBDocument doc, String path, boolean zip) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		FileUtils fu = new FileUtils();
		if(path == null)
			path = workingDocPath;
		
		doc.setOriginalDocType(doc.getDOM());
		setMetaData(doc);
		if(fu.createXMLFile(doc.getDOM(), path)){
			String tempSemFile = BBIni.getTempFilesPath() + BBIni.getFileSep() + fu.getFileName(path) + ".sem"; 
			copySemanticsFile(tempSemFile, fu.getPath(path) + BBIni.getFileSep() + fu.getFileName(path) + ".sem");
		}
		else {
			new Notify("An error occured while saving your document.  Please check your original document.");
		}
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
	}
	
	private void setMetaData(BBDocument doc){
		if(findMetaElement(doc.getDOM()) == null){
			Element root = doc.getRootElement();
			Element head = root.getChildElements().get(0);
			Element meta = doc.makeElement("meta", "configurationFile", currentConfig);
			head.appendChild(meta);
		}
	}

	@Override
	public Archiver saveAs(BBDocument doc, String path, String ext) {
		
		// Stop the autosave feature until we're done.
		pauseAutoSave();
		
		if(ext.equals("brf"))
			saveBrf(doc, path);
		else if(ext.equals("utd"))
			save(doc, path, true);
		
		// Resume autosave feature.
		resumeAutoSave(doc, path);
		
		return this;
	}
	
	private String findConfig(){
		File file = new File (workingDocPath);
		Builder parser = new Builder();
		
		try {
			Document doc = parser.build(file);
			Element meta = findMetaElement(doc);
			if(meta != null){
				String config = meta.getAttributeValue("configurationFile");
				return config;
			}
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		//return nimas if no element, this means utd was created before changes
		return "nimas.cfg";
	}
	
	private Element findMetaElement(Document doc){
		Element root = doc.getRootElement();
		Element head = root.getChildElements().get(0);
		Elements children = head.getChildElements();
		
		for(int i = 0; i < children.size(); i++){
			if(children.get(i).getAttribute("configurationFile") != null)
				return children.get(i);
		}
		
		return null;
	}
}
