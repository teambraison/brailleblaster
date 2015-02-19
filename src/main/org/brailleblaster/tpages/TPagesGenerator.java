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
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
/*
 * Handles reading from and writing to XML
 */
public class TPagesGenerator {
	String programDataPath;
	String[] xmlElements = {"booktitle", "gradelevel", "subtitle", "seriesname", "editionname", "authors", "translator", 
			"pubpermission", "publisher", "location", "website", "copyrighted", "copyrightsymbol", "copyrightdate", 
			"copyrighttext", "repronotice", "isbn13", "isbn10", "printhistory", "year", "transcriber", "tgs", "affiliation"};
	String pub, pubLoc, pubWeb, copyright, isbn13 = "", isbn10 = "", printHistory;
	private HashMap<String, String> xmlmap;
	
	public TPagesGenerator(){
		programDataPath = BBIni.getProgramDataPath();
		xmlmap = getEmptyXmlMap();//= new HashMap<String, String>();
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
				if(doc.getFirstChild().getNodeType() == Node.ELEMENT_NODE){
					Element rootE = (Element)doc.getFirstChild();
					if(!doc.getFirstChild().getNodeName().equals("dtbook")){
						if(!rootE.getAttribute("type").equals("tpage"))
							return false;
					}
				}
				for (int i = 0; i < xmlElements.length; i++){
					xmlmap.put(xmlElements[i], doc.getElementsByTagName(xmlElements[i]).item(0).getTextContent());
				}
				String tempAuthorString = "";
				for (int q = 0; q < doc.getElementsByTagName("author").getLength(); q++){
					tempAuthorString += ";" + doc.getElementsByTagName("author").item(q).getTextContent();
				}
				while(tempAuthorString.charAt(0)==' ' || tempAuthorString.charAt(0)==';'){
					tempAuthorString = tempAuthorString.substring(1);
				}
				xmlmap.put("authors", tempAuthorString);
				//This will probably all change when I figure out how to do templates
				if(xmlmap.get("booktitle").contains(": "))
					xmlmap.put("booktitle", xmlmap.get("booktitle").replace(": ", ""));
				if(xmlmap.get("pubpermission").equals("Published by "))
					xmlmap.put("pubpermission", "0");
				if(xmlmap.get("pubpermission").equals("With permission of the publisher, "))
					xmlmap.put("pubpermission", "1");
				if(xmlmap.get("copyrighted").equals("Copyright "))
					xmlmap.put("copyrighted", "true");
				if(xmlmap.get("copyrighted").equals("Printed "))
					xmlmap.put("copyrighted", "false");
				if(xmlmap.get("copyrightsymbol").equals("&#x00A9; "))
					xmlmap.put("copyrightsymbol", "true");
				else
					xmlmap.put("copyrightsymbol", "false");
				xmlmap.put("isbn13", xmlmap.get("isbn13").replace("  ISBN-13: ", ""));
				xmlmap.put("isbn10", xmlmap.get("isbn10").replace("  ISBN-10: ", ""));
				if(xmlmap.get("year").contains("Transcribed")){
					xmlmap.put("year", xmlmap.get("year").replace("Transcribed ", ""));
					xmlmap.put("year", xmlmap.get("year").replace(" by", ""));
				}
				xmlmap.put("transcriber", xmlmap.get("transcriber").replace("  ", ""));
				xmlmap.put("tgs", xmlmap.get("tgs").replace("Tactile Graphics by ", "")); //This will need to change when I properly format it
					
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
	 * User is using T-Pages for the first time.
	 */
	public boolean createNewTPageXML(String filename){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			DocumentType docType = doc.getImplementation().createDocumentType("doctype", "-//NISO//DTD dtbook 2005-3//EN", "http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd");
			
			Element rootElement = doc.createElement("dtbook");
			rootElement.setAttribute("type", "tpage");
			doc.appendChild(rootElement);
			
			Element bookElement = doc.createElement("book");
			rootElement.appendChild(bookElement);
			
			Element levelElement = doc.createElement("level1");
			bookElement.appendChild(levelElement);
			
			Element titleInfo = doc.createElement("titleinfo");
			levelElement.appendChild(titleInfo);
			
			Element ptag = newPTag(doc, titleInfo);
			
			Element title = doc.createElement("booktitle");
			ptag.appendChild(title);
			Element gradeLevel = doc.createElement("gradelevel");
			ptag.appendChild(gradeLevel);
			
			newPTag(doc, titleInfo).appendChild(doc.createElement("subtitle"));

			newPTag(doc, titleInfo).appendChild(doc.createElement("seriesname"));
			
			newPTag(doc, titleInfo).appendChild(doc.createElement("editionname"));
			
			Element linebreak = doc.createElement("p");
			linebreak.setTextContent("----------------------------------------");
			levelElement.appendChild(linebreak);
			
			Element authorInfo = doc.createElement("authorinfo");
			levelElement.appendChild(authorInfo);
			
			newPTag(doc, authorInfo).appendChild(doc.createElement("authors"));
			
			newPTag(doc, authorInfo).appendChild(doc.createElement("translator"));
			
			Element linebreak2 = doc.createElement("p");
			linebreak2.setTextContent("----------------------------------------");
			levelElement.appendChild(linebreak2);
			
			Element publisherInfo = doc.createElement("publisherinfo");
			levelElement.appendChild(publisherInfo);
			
			Element ptag2 = newPTag(doc, publisherInfo);
			ptag2.appendChild(doc.createElement("pubpermission"));
			ptag2.appendChild(doc.createElement("publisher"));
			
			newPTag(doc, publisherInfo).appendChild(doc.createElement("location"));
			newPTag(doc, publisherInfo).appendChild(doc.createElement("website"));
			
			Element linebreak3 = doc.createElement("p");
			linebreak3.setTextContent("----------------------------------------");
			levelElement.appendChild(linebreak3);
			
			Element printInfo = doc.createElement("printinfo");
			levelElement.appendChild(printInfo);
			
			Element ptag3 = newPTag(doc, printInfo);
			ptag3.appendChild(doc.createElement("copyrighted"));
			ptag3.appendChild(doc.createElement("copyrightsymbol"));
			ptag3.appendChild(doc.createElement("copyrightdate"));
			ptag3.appendChild(doc.createElement("copyrighttext"));
			
			newPTag(doc, printInfo).appendChild(doc.createElement("repronotice"));
			newPTag(doc, printInfo).appendChild(doc.createElement("isbn13"));
			newPTag(doc, printInfo).appendChild(doc.createElement("isbn10"));
			newPTag(doc, printInfo).appendChild(doc.createElement("printhistory"));

			Element linebreak4 = doc.createElement("p");
			linebreak4.setTextContent("----------------------------------------");
			levelElement.appendChild(linebreak4);
			
			Element transcriptionInfo = doc.createElement("transcriptioninfo");
			levelElement.appendChild(transcriptionInfo);
			
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("year"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("transcriber"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("tgs"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("affiliation"));
			
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filename));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
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
	
	private Element newPTag(Document doc, Element parent){
		Element ptag = doc.createElement("p");
		parent.appendChild(ptag);
		return ptag;
	}
	
	public boolean saveNewTPage(String filename, HashMap<String, String> newXmlMap){
		File file = new File(filename);
		//if(!checkForFile(filename))
			createNewTPageXML(filename);
		/*Set mapSet = newXmlMap.entrySet();
		Iterator iterator = mapSet.iterator();*/
		try{
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			
			/*while(iterator.hasNext()){
				Map.Entry mapEntry = (Map.Entry)iterator.next();
				doc.getElementsByTagName((String)mapEntry.getKey()).item(0).setTextContent((String)mapEntry.getValue());
			}*/
			if(newXmlMap.get("booktitle") != null){
				if(!newXmlMap.get("gradelevel").equals("")){
					doc.getElementsByTagName("booktitle").item(0).setTextContent(newXmlMap.get("booktitle") + ": ");
					doc.getElementsByTagName("gradelevel").item(0).setTextContent(newXmlMap.get("gradelevel"));
				} else {
					doc.getElementsByTagName("booktitle").item(0).setTextContent(newXmlMap.get("booktitle"));
				}
			}
			doc.getElementsByTagName("subtitle").item(0).setTextContent(newXmlMap.get("subtitle"));
			doc.getElementsByTagName("seriesname").item(0).setTextContent(newXmlMap.get("seriesname"));
			doc.getElementsByTagName("editionname").item(0).setTextContent(newXmlMap.get("editionname"));
			
			String[] authors = newXmlMap.get("authors").split(";");
			for (String tempAuthor : authors){
				Element newAuthor = doc.createElement("author");
				newAuthor.setTextContent(tempAuthor);
				newPTag(doc, (Element)doc.getElementsByTagName("authors").item(0)).appendChild(newAuthor);
			}
			
			doc.getElementsByTagName("translator").item(0).setTextContent(newXmlMap.get("translator"));
			
			if(newXmlMap.get("pubpermission").equals("0")){
				doc.getElementsByTagName("pubpermission").item(0).setTextContent("Published by ");
			} else{
				doc.getElementsByTagName("pubpermission").item(0).setTextContent("With permission of the publisher, ");
			}
			doc.getElementsByTagName("publisher").item(0).setTextContent(newXmlMap.get("publisher"));
			doc.getElementsByTagName("location").item(0).setTextContent(newXmlMap.get("location"));
			doc.getElementsByTagName("website").item(0).setTextContent(newXmlMap.get("website"));
			
			if(newXmlMap.get("copyrighted").equals("true")){
				doc.getElementsByTagName("copyrighted").item(0).setTextContent("Copyright ");
			} else {
				doc.getElementsByTagName("copyrighted").item(0).setTextContent("Printed ");
			}
			if(newXmlMap.get("copyrightsymbol").equals("true")){
				doc.getElementsByTagName("copyrightsymbol").item(0).setTextContent("&#x00A9; ");
			}
			doc.getElementsByTagName("copyrightdate").item(0).setTextContent(newXmlMap.get("copyrightdate") + " ");
			doc.getElementsByTagName("copyrighttext").item(0).setTextContent(newXmlMap.get("copyrighttext"));
			doc.getElementsByTagName("repronotice").item(0).setTextContent(newXmlMap.get("repronotice"));
			if(newXmlMap.get("isbn13").length() > 1 || newXmlMap.get("isbn10").length() > 1){
				//doc.insertBefore(doc.getElementsByTagName("isbn13").item(0), doc.createTextNode("Transcription of")); //Probably doesn't work
			}
			if(newXmlMap.get("isbn13").length() > 1){
				doc.getElementsByTagName("isbn13").item(0).setTextContent("  ISBN-13: " +newXmlMap.get("isbn13"));
			}
			if(newXmlMap.get("isbn10").length() > 1){
				doc.getElementsByTagName("isbn10").item(0).setTextContent("  ISBN-10: " +newXmlMap.get("isbn10"));
			}
			doc.getElementsByTagName("printhistory").item(0).setTextContent(newXmlMap.get("printhistory"));
			if(newXmlMap.get("transcriber").length() > 1){
				doc.getElementsByTagName("year").item(0).setTextContent("Transcribed " + newXmlMap.get("year") + " by");
				doc.getElementsByTagName("transcriber").item(0).setTextContent("  " + newXmlMap.get("transcriber"));
			}
			if(newXmlMap.get("tgs").length()>1){
				doc.getElementsByTagName("tgs").item(0).setTextContent("Tactile Graphics by " + newXmlMap.get("tgs"));
			}
			doc.getElementsByTagName("affiliation").item(0).setTextContent(newXmlMap.get("affiliation"));
			
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
			if(fmNode == null){
				return;
			}
			
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
			xmlmap.put("booktitle", title);
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
					if(copyYear.length()==4){ //If the copyright year isn't 4 digits, we were probably very off. Throw it all out!
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
	
	public HashMap<String, String> getEmptyXmlMap(){
		HashMap<String, String> newMap = new HashMap<String,String>();
		for (int i = 0; i < xmlElements.length; i++){
			newMap.put(xmlElements[i], "");
		}
		return newMap;
	}
}
