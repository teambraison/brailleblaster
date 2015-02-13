package org.brailleblaster.tpages;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.brailleblaster.BBIni;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
/*
 * Handles reading from and writing to XML
 */
public class TPagesGenerator {
	String programDataPath;
	String[] xmlElements = {"title", "gradelevel", "subtitle", "seriesname", "editionname", "authors", "translator", 
			"pubpermission", "publisher", "location", "website", "copyrighted", "copyrightsymbol", "copyrightdate", 
			"copyrighttext", "repronotice", "isbn13", "isbn10", "printhistory", "year", "transcriber", "tgs", "affiliation"}; 
	private HashMap<String, String> xmlmap;
	
	public TPagesGenerator(){
		programDataPath = BBIni.getProgramDataPath();
		xmlmap = new HashMap<String, String>();
	}
	
	/*
	 * Reload previously made T-Page
	 */
	public boolean openTPageXML(String filename){
		try{
			File file = new File(filename);
			
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			doc.getDocumentElement().normalize();
			
			//NodeList nodes = doc.getChildNodes();
			//System.out.println("nodelist = " + nodes.getLength() + " nodes");
			for (int i = 0; i < xmlElements.length; i++){
				xmlmap.put(xmlElements[i], doc.getElementsByTagName(xmlElements[i]).item(0).getTextContent());
			}
			
			
		} catch(ParserConfigurationException e){
			e.printStackTrace();
			return false;
		} catch(IOException e){
			e.printStackTrace();
			return false;
		}catch (SAXException e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean checkForFile(String filename){
		File file = new File(filename);
		if(file.exists()){
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * User is using T-Pages for the first time. This will at some point require a String argument for the filename
	 */
	public boolean createNewTPageXML(String filename){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement("tpage");
			doc.appendChild(rootElement);
			
			Element titleInfo = doc.createElement("titleinfo");
			rootElement.appendChild(titleInfo);
			
			titleInfo.appendChild(doc.createElement("title"));
			titleInfo.appendChild(doc.createElement("gradelevel"));
			titleInfo.appendChild(doc.createElement("subtitle"));
			titleInfo.appendChild(doc.createElement("seriesname"));
			titleInfo.appendChild(doc.createElement("editionname"));
			
			Element authorInfo = doc.createElement("authorinfo");
			rootElement.appendChild(authorInfo);
			
			authorInfo.appendChild(doc.createElement("authors"));
			authorInfo.appendChild(doc.createElement("translator"));
			
			Element publisherInfo = doc.createElement("publisherinfo");
			rootElement.appendChild(publisherInfo);
			
			publisherInfo.appendChild(doc.createElement("pubpermission"));
			publisherInfo.appendChild(doc.createElement("publisher"));
			publisherInfo.appendChild(doc.createElement("location"));
			publisherInfo.appendChild(doc.createElement("website"));
			
			Element printInfo = doc.createElement("printinfo");
			rootElement.appendChild(printInfo);
			
			printInfo.appendChild(doc.createElement("copyrighted"));
			printInfo.appendChild(doc.createElement("copyrightsymbol"));
			printInfo.appendChild(doc.createElement("copyrightdate"));
			printInfo.appendChild(doc.createElement("copyrighttext"));
			printInfo.appendChild(doc.createElement("repronotice"));
			printInfo.appendChild(doc.createElement("isbn13"));
			printInfo.appendChild(doc.createElement("isbn10"));
			printInfo.appendChild(doc.createElement("printhistory"));
			
			Element transcriptionInfo = doc.createElement("transcriptioninfo");
			rootElement.appendChild(transcriptionInfo);
			
			transcriptionInfo.appendChild(doc.createElement("year"));
			transcriptionInfo.appendChild(doc.createElement("transcriber"));
			transcriptionInfo.appendChild(doc.createElement("tgs"));
			transcriptionInfo.appendChild(doc.createElement("affiliation"));
			
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			//StreamResult result = new StreamResult(System.out);
			StreamResult result = new StreamResult(new File(filename));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
			
		} catch(ParserConfigurationException e){
			e.printStackTrace();
			return false;
		} catch(TransformerException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean saveNewTPage(String filename, HashMap<String, String> newXmlMap){
		File file = new File(filename);
		if(!checkForFile(filename))
			createNewTPageXML(filename);
		Set mapSet = newXmlMap.entrySet();
		Iterator iterator = mapSet.iterator();
		try{
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			
			while(iterator.hasNext()){
				Map.Entry mapEntry = (Map.Entry)iterator.next();
				doc.getElementsByTagName((String)mapEntry.getKey()).item(0).setTextContent((String)mapEntry.getValue());
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filename));
			transformer.transform(source, result);
			
		} catch(ParserConfigurationException e){
			e.printStackTrace();
			return false;
		} catch(IOException e){
			e.printStackTrace();
			return false;
		}catch (SAXException e){
			e.printStackTrace();
			return false;
		}catch(TransformerException e){
			e.printStackTrace();
			return false;
		}
		
			
		return true;
	}
	
	public HashMap<String, String> getXmlMap(){
		return xmlmap;
	}
}
