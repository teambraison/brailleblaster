package org.brailleblaster.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.brailleblaster.BBIni;
import org.brailleblaster.archiver.NimasArchiver;
import nu.xom.Document;

public class Converter {
	NimasArchiver nimasArch;
	ArrayList<Document> result;

	public Converter(NimasArchiver nimasArch){
		this.nimasArch=nimasArch;
		result=this.nimasArch.manageNimas();
		tansformer(result.get(0));
	}
	void tansformer(Document doc){
		String xslPath=BBIni.getProgramDataPath() + BBIni.getFileSep()+"xsl"+BBIni.getFileSep() +"NimasToEpub.xsl";
	
		//need to change for something to output
		String outputpath=BBIni.getProgramDataPath() + BBIni.getFileSep()+"xsl"+BBIni.getFileSep() +"NimasTemp.xml";
		String docStr=doc.toXML();
		InputStream in=null;
		try {
			 in = IOUtils.toInputStream(docStr, "UTF-8");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		StreamSource domSource = new StreamSource( in);
		Source xsl = new StreamSource(new File(xslPath));
		Result xmlOutput = new StreamResult(new File(outputpath));

		try {
		    Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);
		    transformer.transform(domSource, xmlOutput);
		} catch (TransformerException e) {
		    // Handle.
		}
	}

}
