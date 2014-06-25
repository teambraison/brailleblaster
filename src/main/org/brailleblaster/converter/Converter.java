package org.brailleblaster.converter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.brailleblaster.BBIni;
import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.NimasArchiver;
import org.w3c.dom.Node;

import sun.nio.cs.StandardCharsets;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;

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
	
		
		String outputpath=BBIni.getProgramDataPath() + BBIni.getFileSep()+"xsl"+BBIni.getFileSep() +"NimasTemp.xml";
		String docStr=doc.toXML();
		InputStream in=null;
		try {
			 in = IOUtils.toInputStream(docStr, "UTF-8");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/**Builder builder = new Builder();
		Document xslDoc;
		try {
			xslDoc = builder.build(xslPath);
			Source xsl = new StreamSource(new File(xslPath));
			String test=xsl.toString();
			//XSLTransform xslt = new XSLTransform(xslDoc);
			//Nodes newDocNodes = xslt.transform(doc);
			//Document transformedDoc = xslt.toDocument(newDocNodes);
			
		} catch (ValidityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}**/
		
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
