package org.brailleblaster.importers;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Document;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;
import org.brailleblaster.wordprocessor.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tika.*;
import org.apache.tika.detect.TypeDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ParserDecorator;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.EmbeddedContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;

public class ImportersManager
{
	String fileName;
    Boolean isZip;
    String tempPath;
    String docID;
    LocaleHandler lh = new LocaleHandler();
    static int fileCount;
    String[] orderedDocList;
    int orderedDocSeq;
    String opfPath;

    
    // Constructor
	public ImportersManager (String fileName, String tempPath, String docID, boolean zip) 
			throws Exception  {
	    this.fileName = fileName;
		this.tempPath = tempPath;
		this.docID = docID;
	    this.isZip = zip;
	 }

	 public String[]  extractZipFiles () {
			if (isZip) {
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
			System.err.println(e.getLocalizedMessage());
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
			   if (ext.contentEquals("xml") || ext.contentEquals("opf")) {
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
			System.err.println(e.getLocalizedMessage());
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
			opfPath = of.getParent() + BBIni.getFileSep();
			orderedDocList = getOrderedFileList(a.length, tempPath+opfFile);
		} else {
			orderedDocList = a.clone();
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
					if (mt.contentEquals("application/x-dtbook+xml")) {
						String href =  ((Element) newNode).getAttributeValue("href");
						orderedDocList[orderedDocSeq++] = opfPath + href;
					}
				}
			}
		}
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
}

