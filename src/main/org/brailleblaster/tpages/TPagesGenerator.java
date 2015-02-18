package org.brailleblaster.tpages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
	String pub, pubLoc, pubWeb, copyright, isbn13 = "", isbn10 = "", printHistory;
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
			if(checkForFile(filename)){
				File file = new File(filename);
				
				DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = docBuilder.parse(file);
				doc.getDocumentElement().normalize();
				if(!doc.getFirstChild().getNodeName().equals("tpage")){
					return false;
				}
				for (int i = 0; i < xmlElements.length; i++){
					xmlmap.put(xmlElements[i], doc.getElementsByTagName(xmlElements[i]).item(0).getTextContent());
				}
			} else {
				return false;
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
	
	/*
	 * Attempts to find data in the book to automatically populate the text fields
	 */
	public void autoPopulate(String bookPath){
		File book = new File(bookPath);
		try{
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(book);
			
			Node fmNode = doc.getElementsByTagName("frontmatter").item(0);
			
			///////Title
			String title = findTitle(doc);
			String subtitle = "";
			if(title.contains(":")){ //There is probably a subtitle
				subtitle = title.substring(title.indexOf(":") + 2);
				while(subtitle.charAt(0)==' '){ // Remove spaces from beginning of subtitle
					subtitle = subtitle.substring(1);
				}
				title = title.substring(0, title.indexOf(":"));
			}
			while(title.charAt(0)==' '){
				title = title.substring(1);
			}
			//////////Authors
			String authors = findAuthors(doc);
			if(authors.contains("by")){
				authors = authors.substring(authors.indexOf("by") + 2);
			}
			authors = authors.replace(", ", ";");
			authors = authors.replace("and ", "");
			while(authors.charAt(0)==' ' || authors.charAt(0)==';'){
				authors = authors.substring(1);
			}
			xmlmap.put("title", title);
			xmlmap.put("subtitle", subtitle);
			xmlmap.put("authors", authors);
			
			///////Begin iterating through front matter
			parseFrontMatter(fmNode);
			xmlmap.put("isbn13", isbn13);
			xmlmap.put("isbn10", isbn10);
			
			if(copyright!=null){
				if(copyright.contains("&#169;")){
					xmlmap.put("copyrighted", "true");
					xmlmap.put("copyrightsymbol", "true");
				}
				if(copyright.contains("&#x00A9")){
					xmlmap.put("copyrighted", "true");
					xmlmap.put("copyrightsymbol", "true");
				}
				else if(copyright.contains("Copyright")){
					xmlmap.put("copyrighted", "true");
				}
				else {
					xmlmap.put("copyrighted", "false");
					xmlmap.put("copyrightsymbol", "false");
				}
				if(!copyright.equals("")){
					//Find copyright year and publisher
					int tempInt = 0;
					String copyYear = "";
					String copyPub = "";
					while(tempInt < copyright.length()){
						if(copyYear.equals("")){
							if(Character.isDigit(copyright.charAt(tempInt))){
								copyYear = copyright.substring(tempInt, tempInt + 4);
								tempInt += 4;
							}
						} else { //Found the year, now look for publisher
							if(copyright.charAt(tempInt)=='b'){
								if(copyright.charAt(tempInt+1)=='y'){
									tempInt += 3;
									for(int i = tempInt; i < copyright.length(); i++){
										if(copyright.charAt(i)!='.')
											copyPub += copyright.charAt(i);
										else
											i = copyright.length();
									}
									break;
								}
							}
							if(Character.isUpperCase(copyright.charAt(tempInt))){
								for(int i = tempInt; i < copyright.length(); i++){
									if(copyright.charAt(i)!='.')
										copyPub += copyright.charAt(i);
									else
										i = copyright.length();
								}
								break;
							}
						}
						
						tempInt++;
					}
					if(copyYear.length()==4){ //If the copyright year isn't 4 digits, we were likely very off. Throw it all out!
						xmlmap.put("copyrightdate", copyYear);
						xmlmap.put("copyrighttext", copyPub);
					}
				}
			}
			
			xmlmap.put("repronotice", "Further reproduction or distribution in other than a specialized format is prohibited.");
			
		} catch(ParserConfigurationException e){
			e.printStackTrace();
			return;
		} catch(IOException e){
			e.printStackTrace();
			return;
		}catch (SAXException e){
			e.printStackTrace();
			return;
		}
	}
	
	private List<Node> getAllChildren(Node parent){ // Could likely be made more efficient
		List<Node> returnedNodes = new ArrayList<Node>();
		NodeList nodes = parent.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++){
			Node tempNode = nodes.item(i);
			returnedNodes.add(tempNode);
			if(tempNode.hasChildNodes()){
				List<Node> childNodes = new ArrayList<Node>(getAllChildren(tempNode));
				for(int q = 0; q < childNodes.size(); q++){
					returnedNodes.add(childNodes.get(q));
				}
			}
		}
		return returnedNodes;
	}
	
	private String findTitle(Document doc){
		String title = "";
		List<Node> nodeChildren = getAllChildren(doc.getElementsByTagName("doctitle").item(0));
		for (int q = 0; q < nodeChildren.size(); q++){
			if(nodeChildren.get(q).getNodeName().equals("#text"))
				title += " " + nodeChildren.get(q).getNodeValue();
		}
		return title;
	}
	
	private String findAuthors(Document doc){
		String authors = "";
		List<Node> nodeChildren = getAllChildren(doc.getElementsByTagName("docauthor").item(0));
		for(int q = 0; q < nodeChildren.size(); q++){
			if(nodeChildren.get(q).getNodeName().equals("#text"))
				authors += ";" + nodeChildren.get(q).getNodeValue();
		}
		return authors;
	}
	
	private void parseFrontMatter(Node parent){
		NodeList nodes = parent.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++){
			Node tempNode = nodes.item(i);
			if(tempNode.getNodeName().equals("#text")){
				String nodeVal = tempNode.getNodeValue();
				//////Copyright
				if(nodeVal.contains("Copyright") || nodeVal.contains("&#169;") || nodeVal.contains("&#x00A9;")){
					copyright = nodeVal;
				}
				
				//////ISBN
				if(nodeVal.contains("ISBN-")){
					if(nodeVal.contains("ISBN-13")){
						for(int q = nodeVal.indexOf("ISBN-13") + 8; q < nodeVal.length(); q++){
							if(Character.isDigit(nodeVal.charAt(q))||nodeVal.charAt(q) == '-'){
								isbn13 += nodeVal.charAt(q);
							}
							if(nodeVal.charAt(q)==' ' && !isbn13.equals("")){
								break;
							}
						}
					}
					if(nodeVal.contains("ISBN-10")){
						for(int q = nodeVal.indexOf("ISBN-10") + 8; q < nodeVal.length(); q++){
							if(Character.isDigit(nodeVal.charAt(q))||nodeVal.charAt(q) == '-'){
								isbn10 += nodeVal.charAt(q);
							}
							if(nodeVal.charAt(q)==' ' && !isbn10.equals("")){
								break;
							}
						}
					}
				} else if (nodeVal.contains("ISBN")){
					String tempIsbn = "";
					int numbers = 0;
					for(int q = nodeVal.indexOf("ISBN") + 4; q < nodeVal.length(); q++){
						if(Character.isDigit(nodeVal.charAt(q))){
							tempIsbn += nodeVal.charAt(q);
							numbers++;
						}
						if(nodeVal.charAt(q)=='-'){
							tempIsbn += nodeVal.charAt(q);
						}
						if(nodeVal.charAt(q)==' '&&!tempIsbn.equals("")){
							break;
						}
					}
					if(numbers==13){
						isbn13 = tempIsbn;
					}
					if(numbers==10){
						isbn10 = tempIsbn;
					}
					
				}
			}
			
			//////
			if(tempNode.hasChildNodes()){
				parseFrontMatter(tempNode);
			}
		}
	}
	
	public HashMap<String, String> getXmlMap(){
		return xmlmap;
	}
}
