package org.brailleblaster.tpages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
/*
 * Handles reading from and writing to XML
 */
public class TPagesGenerator {
	String programDataPath;
	String[] xmlElements = {"booktitle", "gradelevel", "subtitle", "seriesname", "editionname", "authors", "translator", 
			"publisherpermission", "publisher", "publisherlocation", "publisherwebsite", "copyrighttext", "reproductionnotice", "isbn13", "isbn10", 
			"printhistory", "transcriptionyear", "transcriber", "tgs", "affiliation", "totalvolumes", "volumenumber", "customizedbraille",
			"braillepageinfo", "printpageinfo", "transcribernotes", "template", "specialsymbols"};
	String copyright, isbn13 = "", isbn10 = "", printHistoryGuess, titleGuess, authorGuess, publisherGuess, websiteGuess;
	final String defaultTemplate = "(booktitle): (gradelevel)\n(subtitle)\n(seriesname)\n(editionname)\n(authors)\n(linebreak)\n(publisherpermission)"
			+ "\n(publisher)\n(publisherwebsite)\n(linebreak)\n(copyrighttext)\n(reproductionnotice)\nTranscription of:\n"
			+ "  ISBN-13: (isbn13)\n  ISBN-10: (isbn10)\n(printhistory)\nTranscribed (transcriptionyear) by\n(transcriber)\n"
			+ "(linebreak)\n(totalvolumes)\n(volumenumber)\n(customizedbraille)\n(braillepageinfo)\n(printpageinfo)\n(linebreak)\n";
	private HashMap<String, String> xmlmap;
	
	public TPagesGenerator(){
		programDataPath = BBIni.getProgramDataPath();
		xmlmap = getEmptyXmlMap();
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
				while(tempAuthorString.length() > 0 && (tempAuthorString.charAt(0)==' ' || tempAuthorString.charAt(0)==';')){
					tempAuthorString = tempAuthorString.substring(1);
				}
				xmlmap.put("authors", tempAuthorString);
				if(xmlmap.get("booktitle").contains(": "))
					xmlmap.put("booktitle", xmlmap.get("booktitle").replace(": ", ""));
				xmlmap.put("isbn13", xmlmap.get("isbn13").replace("  ISBN-13: ", ""));
				xmlmap.put("isbn10", xmlmap.get("isbn10").replace("  ISBN-10: ", ""));
				if(xmlmap.get("transcriptionyear").contains("Transcribed")){
					xmlmap.put("transcriptionyear", xmlmap.get("transcriptionyear").replace("Transcribed ", ""));
					xmlmap.put("transcriptionyear", xmlmap.get("transcriptionyear").replace(" by", ""));
				}
				xmlmap.put("transcriber", xmlmap.get("transcriber").replace("  ", ""));
				xmlmap.put("tgs", xmlmap.get("tgs").replace("Tactile Graphics by ", "")); //This will need to change when I properly format it
				if(xmlmap.get("template")==null || xmlmap.get("template").equals("")){
					xmlmap.put("template", defaultTemplate);
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
			
			Element level1Element = doc.createElement("level1");
			bookElement.appendChild(level1Element);
			
			Element level2Element = doc.createElement("level2");
			level1Element.appendChild(level2Element);
			
			Element titleInfo = doc.createElement("titleinfo");
			level2Element.appendChild(titleInfo);
			
			Element ptag = newPTag(doc, titleInfo);
			
			Element title = doc.createElement("booktitle");
			ptag.appendChild(title);
			Element gradeLevel = doc.createElement("gradelevel");
			ptag.appendChild(gradeLevel);
			
			newPTag(doc, titleInfo).appendChild(doc.createElement("subtitle"));

			newPTag(doc, titleInfo).appendChild(doc.createElement("seriesname"));
			
			newPTag(doc, titleInfo).appendChild(doc.createElement("editionname"));
			
			Element linebreak = doc.createElement("p");
			linebreak.setTextContent(" "); //This is a non-breaking space. Coincidentally, this is a terrible line of code.
			level2Element.appendChild(linebreak);
			
			Element authorInfo = doc.createElement("authorinfo");
			level2Element.appendChild(authorInfo);
			
			newPTag(doc, authorInfo).appendChild(doc.createElement("authors"));
			
			newPTag(doc, authorInfo).appendChild(doc.createElement("translator"));
			
			Element linebreak2 = doc.createElement("p");
			linebreak2.setTextContent(" ");
			level2Element.appendChild(linebreak2);
			
			Element publisherInfo = doc.createElement("publisherinfo");
			level2Element.appendChild(publisherInfo);
			
			Element ptag2 = newPTag(doc, publisherInfo);
			ptag2.appendChild(doc.createElement("publisherpermission"));
			ptag2.appendChild(doc.createElement("publisher"));
			
			newPTag(doc, publisherInfo).appendChild(doc.createElement("publisherlocation"));
			newPTag(doc, publisherInfo).appendChild(doc.createElement("publisherwebsite"));
			
			Element linebreak3 = doc.createElement("p");
			linebreak3.setTextContent(" ");
			level2Element.appendChild(linebreak3);
			
			Element printInfo = doc.createElement("printinfo");
			level2Element.appendChild(printInfo);
			
			Element ptag3 = newPTag(doc, printInfo);
			ptag3.appendChild(doc.createElement("copyrighttext"));
			
			newPTag(doc, printInfo).appendChild(doc.createElement("reproductionnotice"));
			newPTag(doc, printInfo).appendChild(doc.createElement("isbn13"));
			newPTag(doc, printInfo).appendChild(doc.createElement("isbn10"));
			newPTag(doc, printInfo).appendChild(doc.createElement("printhistory"));

			Element linebreak4 = doc.createElement("p");
			linebreak4.setTextContent(" ");
			level2Element.appendChild(linebreak4);
			
			Element transcriptionInfo = doc.createElement("transcriptioninfo");
			level2Element.appendChild(transcriptionInfo);
			
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("transcriptionyear"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("transcriber"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("tgs"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("affiliation"));
			
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("totalvolumes"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("volumenumber"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("customizedbraille"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("braillepageinfo"));
			newPTag(doc, transcriptionInfo).appendChild(doc.createElement("printpageinfo"));
			
			Element linebreak5 = doc.createElement("p");
			linebreak5.setTextContent(" ");
			level2Element.appendChild(linebreak5);
			
			Element level2ElementSS = doc.createElement("level2");
			level1Element.appendChild(level2ElementSS);
			
			Element ssHeader = doc.createElement("p");
			ssHeader.setTextContent("Special Symbols");
			level2ElementSS.appendChild(ssHeader);
			newPTag(doc, level2ElementSS).appendChild(doc.createElement("specialsymbols"));
			
			Element level2ElementTN = doc.createElement("level2");
			level1Element.appendChild(level2ElementTN);
			
			Element tnHeader = doc.createElement("p");
			tnHeader.setTextContent("Transcriber Notes");
			level2ElementTN.appendChild(tnHeader);
			newPTag(doc, level2ElementTN).appendChild(doc.createElement("transcribernotes"));
			
			newPTag(doc, level2ElementTN).appendChild(doc.createElement("template"));
			
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
	
	public boolean generateTPage(String template, String filename){
		for (String xmlElement : xmlElements){
			template = template.replaceAll("\\Q(" + xmlElement + ")\\E", xmlmap.get(xmlElement));
		}
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
			
			Element level1Element = doc.createElement("level1");
			bookElement.appendChild(level1Element);
			
			String[] splitTemplate = template.split("\\r?\\n");
			for(String line : splitTemplate){
				Element newElement = doc.createElement("p");
				newElement.setTextContent(line);
				level1Element.appendChild(newElement);
			}
			
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
		if(!checkForFile(filename))
			createNewTPageXML(filename);
		try{
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			
			for(int i = 0; i < xmlElements.length; i++){
				if(newXmlMap.get(xmlElements[i])==null)
					newXmlMap.put(xmlElements[i], ""); //Initialize any empty fields
			}
			
			doc.getElementsByTagName("booktitle").item(0).setTextContent(newXmlMap.get("booktitle"));
			doc.getElementsByTagName("gradelevel").item(0).setTextContent(newXmlMap.get("gradelevel"));
			doc.getElementsByTagName("subtitle").item(0).setTextContent(newXmlMap.get("subtitle"));
			doc.getElementsByTagName("seriesname").item(0).setTextContent(newXmlMap.get("seriesname"));
			doc.getElementsByTagName("editionname").item(0).setTextContent(newXmlMap.get("editionname"));
			doc.getElementsByTagName("authors").item(0).setTextContent("");
			String[] authors = newXmlMap.get("authors").split(";");
			for (String tempAuthor : authors){
				Element newAuthor = doc.createElement("author");
				newAuthor.setTextContent(tempAuthor);
				newPTag(doc, (Element)doc.getElementsByTagName("authors").item(0)).appendChild(newAuthor);
			}
			doc.getElementsByTagName("translator").item(0).setTextContent(newXmlMap.get("translator"));
			doc.getElementsByTagName("publisherpermission").item(0).setTextContent(newXmlMap.get("publisherpermission"));
			doc.getElementsByTagName("publisher").item(0).setTextContent(newXmlMap.get("publisher"));
			doc.getElementsByTagName("publisherlocation").item(0).setTextContent(newXmlMap.get("publisherlocation"));
			doc.getElementsByTagName("publisherwebsite").item(0).setTextContent(newXmlMap.get("publisherwebsite"));
			doc.getElementsByTagName("copyrighttext").item(0).setTextContent(newXmlMap.get("copyrighttext"));
			doc.getElementsByTagName("reproductionnotice").item(0).setTextContent(newXmlMap.get("reproductionnotice"));
			doc.getElementsByTagName("isbn13").item(0).setTextContent(newXmlMap.get("isbn13"));
			doc.getElementsByTagName("isbn10").item(0).setTextContent(newXmlMap.get("isbn10"));
			doc.getElementsByTagName("printhistory").item(0).setTextContent(newXmlMap.get("printhistory"));
			doc.getElementsByTagName("transcriptionyear").item(0).setTextContent(newXmlMap.get("transcriptionyear"));
			doc.getElementsByTagName("transcriber").item(0).setTextContent(newXmlMap.get("transcriber"));
			doc.getElementsByTagName("tgs").item(0).setTextContent(newXmlMap.get("tgs"));
			doc.getElementsByTagName("affiliation").item(0).setTextContent(newXmlMap.get("affiliation"));
			doc.getElementsByTagName("totalvolumes").item(0).setTextContent(newXmlMap.get("totalvolumes"));
			doc.getElementsByTagName("volumenumber").item(0).setTextContent(newXmlMap.get("volumenumber"));
			doc.getElementsByTagName("customizedbraille").item(0).setTextContent(newXmlMap.get("customizedbraille"));
			doc.getElementsByTagName("braillepageinfo").item(0).setTextContent(newXmlMap.get("braillepageinfo"));
			doc.getElementsByTagName("printpageinfo").item(0).setTextContent(newXmlMap.get("printpageinfo"));
			doc.getElementsByTagName("transcribernotes").item(0).setTextContent(newXmlMap.get("transcribernotes"));
			doc.getElementsByTagName("template").item(0).setTextContent(newXmlMap.get("template"));
			doc.getElementsByTagName("specialsymbols").item(0).setTextContent(newXmlMap.get("specialsymbols"));
			
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
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();
			
			Document doc = docBuilder.parse(book);
			
			Node fmNode = doc.getElementsByTagName("frontmatter").item(0);
			if(fmNode == null){
				xmlmap.put("reproductionnotice", "Further reproduction or distribution in other than a specialized format is prohibited.");
				xmlmap.put("template", defaultTemplate);
				return;
			}
			
			//////Begin iterating through Head tags
			Node headNode = doc.getElementsByTagName("head").item(0);
			if(headNode != null)
				parseHeadTags(headNode);
			
			if(publisherGuess != null)
				xmlmap.put("publisher", publisherGuess);
			
			///////Title
			String title;
			if(titleGuess==null) //Couldn't find anything defined in Head tags. Search for <doctitle>
				 title = findTitle(doc);
			else
				title = titleGuess;
			String subtitle = "";
			if(!title.equals("")){
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
			}
			//////////Authors
			String authors;
			if(authorGuess == null) //Couldn't find anything defined in Head tags. Search for <docauthor>
				authors = findAuthors(doc);
			else
				authors = authorGuess;
			if(!authors.equals("")){
				if(authors.contains("by")){
					authors = authors.substring(authors.indexOf("by") + 2);
				}
				authors = authors.replace(", ", ";");
				authors = authors.replace("and ", "");
				while(authors.charAt(0)==' ' || authors.charAt(0)==';'){
					authors = authors.substring(1);
				}
			}
			xmlmap.put("booktitle", title);
			xmlmap.put("subtitle", subtitle);
			xmlmap.put("authors", authors);
			
			///////Begin iterating through front matter
			parseFrontMatter(fmNode);
			
			xmlmap.put("isbn13", isbn13);
			xmlmap.put("isbn10", isbn10);
			
			if(websiteGuess != null){
				xmlmap.put("publisherwebsite", websiteGuess);
			}
			if(printHistoryGuess != null){
				xmlmap.put("printhistory", printHistoryGuess);
			}
			
			if(copyright!=null){
				xmlmap.put("copyrighttext", copyright);
			}
			
			xmlmap.put("reproductionnotice", "Further reproduction or distribution in other than a specialized format is prohibited.");
			xmlmap.put("template", defaultTemplate);
			
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
	
	private void parseHeadTags(Node head) {
		List<Node> children = getAllChildren(head);
		NamedNodeMap attrMap;
		for(int i = 0; i < children.size(); i++){
			if(children.get(i).getNodeName().equals("meta")){
				attrMap = children.get(i).getAttributes();
				if(attrMap.getNamedItem("name")!=null){
					if(attrMap.getNamedItem("name").getNodeValue().equals("dc:Title")){
						titleGuess = attrMap.getNamedItem("content").getNodeValue();
					}
					if(attrMap.getNamedItem("name").getNodeValue().equals("dc:Creator")){
						authorGuess += ";" + attrMap.getNamedItem("content").getNodeValue();
					}
					if(attrMap.getNamedItem("name").getNodeValue().equals("dc:Publisher")){
						publisherGuess = attrMap.getNamedItem("content").getNodeValue();
					}
				}
			}
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
		Node doctitle = doc.getElementsByTagName("doctitle").item(0);
		if(doctitle!=null){
			List<Node> nodeChildren = getAllChildren(doctitle);
			for (int q = 0; q < nodeChildren.size(); q++){
				if(nodeChildren.get(q).getNodeName().equals("#text"))
					title += " " + nodeChildren.get(q).getNodeValue();
			}
		}
		return title;
	}
	
	private String findAuthors(Document doc){
		String authors = "";
		NodeList docAuthors = doc.getElementsByTagName("docauthor");
		if(docAuthors.getLength()>0){
			for(int i = 0; i < docAuthors.getLength(); i++){
				List<Node> daChildren = getAllChildren(docAuthors.item(i));
					for(int q = 0; q < daChildren.size(); q++){
						if(daChildren.get(q).getNodeName().equals("#text"))
							authors += ";" + daChildren.get(q).getNodeValue();
					}
			}
		}
		return authors;
	}
	
	private void parseFrontMatter(Node parent){
		NodeList nodes = parent.getChildNodes();
		Pattern isbn13pattern = Pattern.compile("(?<!\\S)[\\d]+-[\\d]+-[\\d]+-[\\d]+-[\\d]\\b");
		Pattern isbn10pattern = Pattern.compile("(?<!\\S)[\\d]+-[\\d]+-[\\d]+-[\\d]\\b");
		Pattern copyrightpattern1 = Pattern.compile("(Copyright [\\d][\\d][\\d][\\d])");
		Pattern copyrightpattern2 = Pattern.compile("© [\\d][\\d][\\d][\\d]"); // Only way I can get it to work
		Pattern websitepattern = Pattern.compile("www[.][\\w]*[.]com");
		Pattern printpattern = Pattern.compile("[\\d]+ [\\d]+ [\\d]+ [\\d]+ [\\d]+ [\\d]+");
		for(int i = 0; i < nodes.getLength(); i++){
			Node tempNode = nodes.item(i);
			if(tempNode.getNodeName().equals("#text")){
				String nodeVal = tempNode.getNodeValue();
				//////Copyright
				Matcher copyMatcher1 = copyrightpattern1.matcher(nodeVal);
				Matcher copyMatcher2 = copyrightpattern2.matcher(nodeVal);
				if(copyMatcher1.find()){
					copyright = nodeVal;
				} else if (copyMatcher2.find()){
					copyright = nodeVal;
				}
				
				//////ISBN
				Matcher isbn13Matcher = isbn13pattern.matcher(nodeVal);
				if(isbn13Matcher.find()){
					isbn13 = isbn13Matcher.group();
				}
				
				Matcher isbn10Matcher = isbn10pattern.matcher(nodeVal);
				if(isbn10Matcher.find()){
					isbn10 = isbn10Matcher.group();
				}
				
				//////Website
				Matcher websiteMatcher = websitepattern.matcher(nodeVal);
				if(websiteMatcher.find()){
					websiteGuess = websiteMatcher.group();
				}
				
				////Print History
				Matcher printMatcher = printpattern.matcher(nodeVal);
				if(printMatcher.find()){
					printHistoryGuess = nodeVal;
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
		newMap.put("reproductionnotice", "Further reproduction or distribution in other than a specialized format is prohibited.");
		newMap.put("template", defaultTemplate);
		return newMap;
	}
	
	public String[] getXmlElements(){
		return xmlElements;
	}
}
