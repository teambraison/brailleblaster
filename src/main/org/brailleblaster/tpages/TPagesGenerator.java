package org.brailleblaster.tpages;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.brailleblaster.BBIni;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.Elements;
import nu.xom.XPathContext;
import nu.xom.Attribute;


/* Handles creation of XML elements */
public class TPagesGenerator {
	String programDataPath;
	String[] xmlElements = {"booktitle", "gradelevel", "subtitle", "seriesname", "editionname", "authors", "translator", 
			"publisherpermission", "publisher", "publisherlocation", "publisherwebsite", "copyrighttext", "reproductionnotice", "isbn13", "isbn10", 
			"printhistory", "transcriptionyear", "transcriber", "tgs", "affiliation", "totalvolumes", "volumenumber", "customizedbraille",
			"braillepageinfo", "printpageinfo", "transcribernotes", "template"};
	String copyright, isbn13 = "", isbn10 = "", printHistoryGuess, titleGuess, authorGuess = "", publisherGuess, websiteGuess;
	final String defaultTemplate = "<booktitle>: <gradelevel>\n<subtitle>\n<seriesname>\n<editionname>\n<authors>\n<linebreak>\n<publisherpermission>"
			+ "\n<publisher>\n<publisherwebsite>\n<linebreak>\n<copyrighttext>\n<reproductionnotice>\nTranscription of:\n"
			+ "  ISBN-13: <isbn13>\n  ISBN-10: <isbn10>\n<printhistory>\nTranscribed <transcriptionyear> by\n<transcriber>\n"
			+ "<linebreak>\n<totalvolumes>\n<volumenumber>\n<customizedbraille>\n<braillepageinfo>\n<printpageinfo>\n<linebreak>\n";
	private HashMap<String, String> xmlmap;
	
	public TPagesGenerator(){
		programDataPath = BBIni.getProgramDataPath();
		xmlmap = getEmptyXmlMap();
		testXmlMap();
	}
	
	public boolean checkForFile(String filename){
		File file = new File(filename);
		if(file.exists()){
			return true;
		} else {
			return false;
		}
	}
	
	public void testXmlMap(){
		for(int i = 0; i < xmlElements.length; i++){
			xmlmap.put(xmlElements[i], "Testing" + i);
		}
		xmlmap.put("specialsymbols", "a|Testing");
		xmlmap.put("transcribernotes", "Testing");
		xmlmap.put("template", defaultTemplate);
	}
	
	/* Auto-Fill button is pressed */
	public void autoPopulate(Document document){
		String nameSpace = document.getRootElement().getNamespaceURI();
		XPathContext context = new XPathContext("dtb", nameSpace);
		Nodes metaNodes = document.query("//dtb:meta[@name]", context);
		for(int i = 0; i < metaNodes.size(); i++){
			Element metaElement = (Element)metaNodes.get(i);
			if(metaElement.getAttribute("name").getValue().equalsIgnoreCase("dc:title")){
				titleGuess = metaElement.getAttributeValue("content");
			}
			if(metaElement.getAttribute("name").getValue().equalsIgnoreCase("dc:creator")){
				authorGuess += ";" + metaElement.getAttributeValue("content");
			}
			if(metaElement.getAttribute("name").getValue().equalsIgnoreCase("dc:publisher")){
				publisherGuess = metaElement.getAttributeValue("content");
			}
		}
		if(titleGuess==null){
			Nodes docTitleNode = document.query("//dtb:doctitle", context);
			if(docTitleNode.size()>0){
				titleGuess = docTitleNode.get(0).getValue();
			}
		}
		if(authorGuess.length()==0){
			Nodes docAuthorNode = document.query("//dtb:docauthor", context);
			if(docAuthorNode.size()>0){
				authorGuess = docAuthorNode.get(0).getValue();
			}
		}
		String subtitle = "";
		if(!titleGuess.equals("")){
			if(titleGuess.contains(":")){ //There is probably a subtitle
				subtitle = titleGuess.substring(titleGuess.indexOf(":") + 2);
				while(subtitle.charAt(0)==' '){ // Remove spaces from beginning of subtitle
					subtitle = subtitle.substring(1);
				}
				titleGuess = titleGuess.substring(0, titleGuess.indexOf(":"));
			}
			while(titleGuess.charAt(0)==' '){
				titleGuess = titleGuess.substring(1);
			}
		}
		if(!authorGuess.equals("")){
			if(authorGuess.contains("by")){
				authorGuess = authorGuess.substring(authorGuess.indexOf("by") + 2);
			}
			authorGuess = authorGuess.replaceAll(", ", ";");
			authorGuess = authorGuess.replaceAll("and ", "");
			while(authorGuess.charAt(0)==' ' || authorGuess.charAt(0)==';'){
				authorGuess = authorGuess.substring(1);
			}
		}
		
		//Parse front matter - Currently broken
		Pattern isbn13pattern = Pattern.compile("(?<!\\S)[\\d]+-[\\d]+-[\\d]+-[\\d]+-[\\d]\\b");
		Pattern isbn10pattern = Pattern.compile("(?<!\\S)[\\d]+-[\\d]+-[\\d]+-[\\d]\\b");
		Pattern copyrightpattern1 = Pattern.compile("(Copyright [\\d][\\d][\\d][\\d])");
		Pattern copyrightpattern2 = Pattern.compile("© [\\d][\\d][\\d][\\d]"); // Only way I can get it to work
		Pattern websitepattern = Pattern.compile("www[.][\\w]*[.]com");
		Pattern printpattern = Pattern.compile("[\\d]+ [\\d]+ [\\d]+ [\\d]+ [\\d]+ [\\d]+");
		
		Nodes fmChildren = document.query("//dtb:frontmatter/*", context);
		List<Element> allChildren = new ArrayList<Element>();
		for(int i = 0; i < fmChildren.size(); i++){
			allChildren.add((Element)fmChildren.get(i));
			List<Element> moreChildren = getAllChildren((Element)fmChildren.get(i), nameSpace);
			for(int q = 0; q < moreChildren.size(); q++){
				allChildren.add(moreChildren.get(q));
			}
		}
		//List<Node> allChildren = getAllChildren(fmChildren, context);
		for(int i = 0; i < allChildren.size(); i++){
			String nodeVal = removeBrlNode(allChildren.get(i));
			///////Copyright
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
		
		if(titleGuess!=null)
			xmlmap.put("booktitle", titleGuess);
		if(authorGuess!=null)
			xmlmap.put("authors", authorGuess);
		if(publisherGuess!=null)
			xmlmap.put("publisher", publisherGuess);
		if(copyright!=null)
			xmlmap.put("copyright", copyright);
		if(isbn13!=null)
			xmlmap.put("isbn13", isbn13);
		if(isbn10!=null)
			xmlmap.put("isbn10", isbn10);
		if(websiteGuess!=null)
			xmlmap.put("publisherwebsite", websiteGuess);
		if(printHistoryGuess!=null){
			xmlmap.put("printhistory", printHistoryGuess);
		}
	}

	/* Recursive method that assists autoPopulate */
	private List<Element> getAllChildren(Element parent, String namespace){ // Not working as expected - will revisit
		List<Element> returnedNodes = new ArrayList<Element>();
		Elements children = parent.getChildElements();
		for(int i = 0; i < children.size(); i++){
			Element element = children.get(i);
			returnedNodes.add(element);
			if(element.getChildCount()>0){
				if(element.getChildElements("brl", namespace).size()>0){
					element = (Element)element.copy();
					Elements brlChildren = element.getChildElements("brl", namespace);
					for(int q = 0; q < brlChildren.size(); q++){
						element.removeChild(brlChildren.get(q));
					}
				}
				List<Element> moreChildren = getAllChildren(element, namespace);
				for(int q = 0; q < moreChildren.size(); q++){
					if(!moreChildren.get(q).getLocalName().equalsIgnoreCase("brl")){
						returnedNodes.add(moreChildren.get(q));
					}
				}
			}
		}
		return returnedNodes;
	}
	
	public HashMap<String, String> getXmlMap(){
		return xmlmap;
	}
	
	/* Initialize xmlmap to get rid of those pesky nullpointerexceptions */
	public HashMap<String, String> getEmptyXmlMap(){
		HashMap<String, String> newMap = new HashMap<String,String>();
		for (int i = 0; i < xmlElements.length; i++){
			newMap.put(xmlElements[i], "");
		}
		newMap.put("reproductionnotice", "Further reproduction or distribution in other than a specialized format is prohibited.");
		newMap.put("template", defaultTemplate);
		newMap.put("specialsymbols", "");
		return newMap;
	}
	
	public String[] getXmlElements(){
		return xmlElements;
	}
	
	
	/* User has previously made a tpage with the tpage dialog */
	public Element getTPageElement(Document docRoot){
		String nameSpace = docRoot.getRootElement().getNamespaceURI();
		XPathContext context = new XPathContext("dtb", nameSpace);
		Nodes nodes = docRoot.query("//dtb:prodnote", context);
		if(nodes.size() > 0){
			Element pnElement = (Element)nodes.get(0);
			if(pnElement.getAttributeCount()>0){
				if(pnElement.getAttribute("id").getValue().equalsIgnoreCase("brl-tp-TitlePage")){
					return pnElement;
				}
			}
		}
		return null;
	}
	
	/* If user has previously created a tpage, fills the xmlmap with those values */
	public HashMap<String, String> pullFromElement(Element rootElement){
		HashMap<String, String> returnMap = getEmptyXmlMap();
		String nameSpace = rootElement.getNamespaceURI();
		XPathContext context = new XPathContext("dtb", nameSpace);
		Nodes divs = rootElement.query("//dtb:div", context);
		for(int i = 0; i < divs.size(); i++){
			Element element = (Element)divs.get(i);
			Attribute attr = element.getAttribute("class");
			if(attr!=null){
				if(attr.getValue().contains("brl-tp-")){
					Element finalElement = (Element)element.copy(); //Don't edit the xml file in memory
					for(int q = finalElement.getChildCount()-1; q > 0; q--){ //Remove those brl nodes
						finalElement.removeChild(q);
					}
					returnMap.put(attr.getValue().substring(7), finalElement.getValue());
				}
			}
		}
		Nodes ssPN = rootElement.query("//dtb:prodnote[@id='brl-tp-SpecialSymbolsPage']", context);
		if(ssPN.size()>0){
			String xmlToString = "";
			Element ssElement = (Element)ssPN.get(0);
			Nodes ssPtags = ssElement.query("dtb:p", context);
			for(int i = 1; i < ssPtags.size(); i++){
				String nodeVal = removeBrlNode((Element)ssPtags.get(i));
				xmlToString += "||" + nodeVal.replace(": ", "|");
			}
			returnMap.put("specialsymbols", xmlToString);
		} else
			returnMap.put("specialsymbols", "");
		
		Nodes tnPN = rootElement.query("//dtb:prodnote[@id='brl-tp-TranscriberNotesPage']", context);
		if(tnPN.size() > 0){
			String xmlToString = "";
			Element tnElement = (Element)tnPN.get(0);
			Nodes tnPtags = tnElement.query("dtb:p", context);
			for(int i = 1; i < tnPtags.size(); i++){
				String nodeVal = removeBrlNode((Element)tnPtags.get(i));
				if(i==tnPtags.size()-1)
					xmlToString += nodeVal;
				else
					xmlToString += nodeVal + "\r\n";
			}
			returnMap.put("transcribernotes", xmlToString);
		} else
			returnMap.put("transcribernotes", "");
		return returnMap;
	}
	
	/* Used by various methods to strip Brl nodes out of an element. Is not 100% accurate but not a priority to fix */
	private String removeBrlNode(Element element){
		String nameSpace = element.getNamespaceURI();
		XPathContext context = new XPathContext("dtb", nameSpace);
		
		Element returnElement = (Element) element.copy();
		
		if(returnElement.getChildCount() > 0){
			Nodes brlNodes = returnElement.query("dtb:brl", context);
			System.out.println("Found " + brlNodes.size() + " brl nodes");
			for(int i = 0; i < brlNodes.size(); i++){
				Element curNode = (Element)brlNodes.get(i);
				returnElement.removeChild(curNode);
			}
		}
		return returnElement.getValue();
	}
	
	/* If user has previously created a tpage, fills the Template text field with the tpage's current layout */
	public String elementToTemplate(Element rootElement){
		String nameSpace = rootElement.getNamespaceURI();
		XPathContext context = new XPathContext("dtb", nameSpace);
		Element newRoot = (Element)rootElement.copy();
		Nodes ptags = newRoot.query("//dtb:p", context);
		
		for(int i = 0; i < ptags.size(); i++){
			Element curElement = (Element)ptags.get(i);
			removeAllAttributes(curElement);
			if(curElement.getChildCount() > 0){
				Nodes divs = curElement.query("dtb:div|dtb:brl", context);
				for(int q = 0; q < divs.size(); q++){
					Element curDiv = (Element)divs.get(q);
					Attribute attr = curDiv.getAttribute("class");
					if(attr!=null){
						Element newElement = new Element(attr.getValue().substring(7));
						removeAllAttributes(newElement);
						curElement.replaceChild(curDiv, newElement);
					} else{
						curElement.removeChild(curDiv);
					}
				}
			}
		}
		String returnString = newRoot.toXML();
		returnString = returnString.replaceAll("<prodnote[^>]+>", "");
		returnString = returnString.replaceAll(" xmlns=\"[0-9a-zA-Z:/\\-\\.]*\" ", "");
		returnString = returnString.replaceAll("/>", ">");
		returnString = returnString.replace("</prodnote>", "");
		returnString = returnString.replaceAll("</p>", "\r\n");
		returnString = returnString.replaceAll("<p>", "");
		return returnString;
	}
	
	/* Strips all attributes out of a node for elementToTemplate */
	private void removeAllAttributes(Element element){
		for(int i = 0; i < element.getAttributeCount(); i++){
			element.removeAttribute(element.getAttribute(i));
		}
	}
	
	/* When inserting the tpage, creates the parent node that will passed to the BrailleDocument */
	public Element getTPageParent(String template){
		Element rootTPage = new Element("level1");
		
		/*Title Page*/
		Element titlePage = new Element("prodnote");
		Attribute titleAttr = new Attribute("id", "brl-tp-TitlePage");
		titlePage.addAttribute(titleAttr);
		String[] tempLines = template.split("\r|\n");
		for(String line : tempLines){
			if(!line.equals("")){
				Element finalLine = extractXmlTags(line);
				titlePage.appendChild(finalLine);
			}
		}
		/* ********* */
		
		/*Special Symbols*/
		Element ssPage = new Element("prodnote");
		Attribute ssAttr = new Attribute("id", "brl-tp-SpecialSymbolsPage");
		ssPage.addAttribute(ssAttr);
		Element ssHeader = new Element("p");
		ssHeader.appendChild("Special Symbols");
		ssPage.appendChild(ssHeader);
		
		String ssText = xmlmap.get("specialsymbols");
		if(ssText!=null){
			String[] ssSplit = ssText.split("\\|\\|");
			for(String string : ssSplit){
				String[] parts = string.split("\\|");
				if(parts[0]!=null){
					Element ptag = new Element("p");
					if(parts[1]!=null)
						ptag.appendChild(parts[0] + ": " + parts[1]);
					else
						ptag.appendChild(parts[0]);
					ssPage.appendChild(ptag);
				}
			}
		}
		/* ********* */
		
		/*Transcriber Notes*/
		Element tnPage = new Element("prodnote");
		Attribute tnAttr = new Attribute("id", "brl-tp-TranscriberNotesPage");
		tnPage.addAttribute(tnAttr);
		Element tnHeader = new Element("p");
		tnHeader.appendChild("Transcriber Notes");
		tnPage.appendChild(tnHeader);
		
		String tnText = xmlmap.get("transcribernotes");
		String[] tnLines = tnText.split("\r|\n");
		for(String line : tnLines){
			Element ptag = new Element("p");
			ptag.appendChild(line);
			tnPage.appendChild(ptag);
		}
		/* ******** */
		
		rootTPage.appendChild(titlePage);
		rootTPage.appendChild(ssPage);
		rootTPage.appendChild(tnPage);
		
		return rootTPage;
	}
	
	/* Takes the template defined in the tpage dialog and converts it into xml elements */
	private Element extractXmlTags(String tempLine){
		String line = tempLine.replaceAll("<p>", "");
		line = line.replaceAll("</p>", "");
		Element ptag = new Element("p");
		String temp = "";
		for(int i = 0; i < line.length(); i++){
			if(line.charAt(i)=='<'){
				ptag.appendChild(temp);
				temp = "";
				String findElement = "";
				for(int q = i+1; q < line.length(); q++){
					if(line.charAt(q)=='>'){
						i = q;
						Element newElement = new Element("div");
						Attribute newAttr = new Attribute("class", "brl-tp-" + findElement);
						newElement.addAttribute(newAttr);
						newElement.appendChild(xmlmap.get(findElement));
						ptag.appendChild(newElement);
						break;
					} else {
						findElement += line.charAt(q);
					}
				}
			} else{
				temp += line.charAt(i);
			}
		}
		ptag.appendChild(temp);
		return ptag;
	}
}
