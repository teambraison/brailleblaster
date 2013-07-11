package org.brailleblaster.wordprocessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.w3c.dom.Document;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Normalizer {
	File f;
	Document doc;
	
	public Normalizer(String path){
		this.f = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			this.doc = dBuilder.parse(this.f);
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createNewNormalizedFile(String path){
		normalize();
		write(this.doc, path);
	}
	
	private void normalize(){
		doc.normalize();
		removeEscapeChars(doc.getDocumentElement());
	}
	
	private void removeEscapeChars(Element e){
		NodeList list = e.getChildNodes();
		
		for(int i = 0; i < list.getLength(); i++){
			if(list.item(i) instanceof Element){
				removeEscapeChars((Element)list.item(i));
			}
			else if(list.item(i) instanceof Text){
				Text t = (Text)list.item(i);
				String text = t.getTextContent();
				text = text.replace("\n", "");
				t.setTextContent(text);
			}
		}
	}
	private boolean onlyWhitespace(String text){
		for(int j = 0; j < text.length(); j++){
			if(!Character.isWhitespace(text.charAt(j))){
				return false;
			}
		}
		return true;
	}
	
	public void write(Document document, String path) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
	
    }
}
