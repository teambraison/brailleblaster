package org.brailleblaster.importers;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Document;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportersManager
{
	final String fileName;
    boolean isNimas;
    boolean isEpub;
    final String mediaType;
    final String tempPath;
    final String docID;
    LocaleHandler lh = new LocaleHandler();
    static int fileCount;
    String[] orderedDocList;
    int orderedDocSeq;
    String opfPath;
    String encoding;

    
    // Constructor
	public ImportersManager (String fileName, String tempPath, String docID, String arcType) 
			throws Exception  {
	    this.fileName = fileName;
		this.tempPath = tempPath;
		this.docID = docID;
		isNimas = false;
		isEpub = false;
		if (arcType.contains("epub")) {
			isEpub = true;
			mediaType = "application/xhtml+xml";
		} else {	
	        isNimas = true;
	        mediaType = "application/x-dtbook+xml";
		}
		
	    if ((isNimas && isEpub) || (!isNimas && !isEpub)) {
	    	System.err.println("extractZipFiles - Error - doc type - Nimas " + isNimas +
	    			" isEpub " + isNimas);
	    }
	 }

	 public String[]  extractPubFiles () {
			if (isEpub || isNimas) {
				return extractZip(fileName, tempPath, docID);
			} else {
				return null;
			}
		}
	
		
	//returns a list of extracted files
	private String[] extractZip (String fileName, String tempPath, String docID) {
		FileInputStream fis;
		try {
 		    fis = new FileInputStream(fileName);
 		    
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			return null;
		}
		ZipInputStream zis = new 
				  ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry;
		StringBuffer arcNames = new StringBuffer();
		int count = 0;
		
		int BUFFER = 4096;
		
		try {
		   while((entry = zis.getNextEntry()) != null) {
			   String fn = entry.getName();
			   String ext = getFileExt(fn);
			   if (ext.contentEquals("xml") || ext.contentEquals("opf") || ext.contentEquals("html")
					   || ext.contentEquals("htm")) {
			     arcNames.append(fn + "|");
			     count++;
			   } else {
				   continue;
			   }
			   int c;
	           byte data[] = new byte[BUFFER];
			   
			   // extract data
			   // open output streams
			   String destFile = tempPath + fn;
			   File of = new File (destFile);
			   of.getParentFile().mkdirs();
			   FileOutputStream fos = new FileOutputStream(destFile);
			   BufferedOutputStream dest = new 
						  BufferedOutputStream(fos, BUFFER);
			   while ((c = zis.read(data, 0, BUFFER)) != -1) {
				  
				   dest.write(data, 0, c);
				}
			    dest.flush();
	            dest.close();
		   }
		   zis.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return null;
		}
		
		String[] a = new String[count] ;
		String opfFile = "";
		int from = 0;

		// list of archives as found in the zip
		for (int i = 0; i<count; i++) {
			int bar = arcNames.indexOf("|", from);
	   		if (bar > 0) {
	   			a[i] = arcNames.substring(from, bar);
	   			from = bar+1;
	   			if (getFileExt(a[i]).contentEquals("opf")) {
	   				opfFile = a[i];
	   			}
	   		}
		}
		
		if (!opfFile.isEmpty()) {
			File of = new File(opfFile);
			if (of.getParent() == null) {
				opfPath = BBIni.getFileSep();
			} else {
			    opfPath = of.getParent() + BBIni.getFileSep();
			}

			orderedDocList = getOrderedFileList(a.length, tempPath+opfFile);
			encoding = getEncoding(tempPath+opfFile);
		} else {
			orderedDocList = a.clone();
			encoding = "UTF-8"; 
		}
				
		return orderedDocList  ;
	}
	
	// the opf lists the order of files in manifest/item[@href]
	String[] getOrderedFileList(int count, String opfFile) {
	    orderedDocList = new String[count - 1];
	    orderedDocSeq = 0;
	    Builder parser = new Builder();
	    Document doc;
		try {
			doc = parser.build(opfFile);
		} catch (ParsingException e) {
			new Notify(lh.localValue("malformedDocument") + "\n" + opfFile);
			return null;
		} catch (IOException e) {
			new Notify(lh.localValue("couldNotOpen") + "\n" + opfFile);
			return null;
		}
		
		final Element rootElement = doc.getRootElement();// this needs to be

		walkOpfTree(rootElement);
	    
		return orderedDocList;
	}
	
	void walkOpfTree (Node node ) {
		
		Node newNode;
		
		for (int i = 0; i < node.getChildCount(); i++) {
			newNode = node.getChild(i);

			if (newNode instanceof Element) {
				String nn = ((Element) newNode).getLocalName();
				if (nn.contentEquals("manifest")){
				    walkOpfTree(newNode);
				}
				if (nn.contentEquals("item")) {
					String mt = ((Element) newNode).getAttributeValue("media-type");
					if (mt.contentEquals(mediaType)) {
						String href =  ((Element) newNode).getAttributeValue("href");
						orderedDocList[orderedDocSeq++] = opfPath + href;
					}
				}
			}
		}
	}
	
	private String getEncoding(String fileName) {

		String line;
		
		try {
			  FileInputStream ois = new FileInputStream(fileName);
			  BufferedReader br = new BufferedReader(new InputStreamReader(ois));

			  line = br.readLine();
			  //System.out.println(line);
			  ois.close();	  
			
			} catch (FileNotFoundException e) {
			    System.err.println(e.getLocalizedMessage());
			    return null;
			} catch (IOException e) {
			    System.err.println(e.getLocalizedMessage());
			    return null;
		    }
		
		if (line == null) return null;
		
		String e1 = line.substring(line.indexOf("encoding="));
		String e2 = e1.substring(e1.indexOf("\"")+1, e1.lastIndexOf("\"") );
		
		return e2;
	}
	

	private String getFileExt(String fileName) {
		String ext = "";
		String fn = fileName.toLowerCase();
		int dot = fn.lastIndexOf(".");
		if (dot > 0) {
			ext = fn.substring(dot + 1);
		}
		return ext;
	}

	public boolean isNimas() {
		return this.isNimas;
	}

	public boolean isEpub() {
		return this.isEpub;
	}

	public String getEncoding() {
		return encoding;
	}
}

