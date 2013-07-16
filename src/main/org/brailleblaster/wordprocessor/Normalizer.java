package org.brailleblaster.wordprocessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.brailleblaster.BBIni;
import org.brailleblaster.util.Notify;
import org.w3c.dom.Document;

import org.w3c.dom.CDATASection;
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
	Logger log = BBIni.getLogger();
	
	public Normalizer(String path){
		this.f = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			this.doc = dBuilder.parse(this.f);
		} 
		catch(ConnectException e){
			new Notify("Brailleblaster failed to access necessary materials from online.  Please check your internet connection and try again.");
			e.printStackTrace();
			printErrors(e);
		}
		catch (ParserConfigurationException e) {
			new Notify("An error occurred while reading the document. Please check whehther the document contains vaild XML.");
			e.printStackTrace();
			printErrors(e);
		}
		catch (SAXException e) {
			new Notify("An error occurred while reading the document. Please check whehther the document contains vaild XML.");
			e.printStackTrace();
			printErrors(e);
		} 
		catch (IOException e) {
			new Notify("An error occurred while reading the document.");
			e.printStackTrace();
			printErrors(e);
		}
	}
	
	private void printErrors(Exception e){
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		log.log(Level.SEVERE, errors.toString());
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
				text = text.replace("\n", " ");
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
