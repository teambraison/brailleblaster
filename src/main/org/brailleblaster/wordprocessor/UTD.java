/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org
  *
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.wordprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Text;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.util.Notify;
import org.brailleblaster.BBIni;

/**
 * This class encapsulates handling of the Universal 
 * TactileDocument Markup Language (UTDML);
 */
class UTD { 

    int braillePageNumber; //number of braille pages
    String firstTableName;
    int dpi; // resolution
    int paperWidth;
    int paperHeight;
    int leftMargin;
    int rightMargin;
    int topMargin;
    int bottomMargin;
    String currentBraillePageNumber;
    String currentPrintPageNumber;
    int[] brlIndex;
    int brlIndexPos;
    int[] brlonlyIndex;
    int brlonlyIndexPos;
    Node beforeBrlNode;
    Node beforeBrlonlyNode;
    private boolean firstPage;
    private boolean firstLineOnPage;
    StringBuilder brailleLine = new StringBuilder (1024);
//  StringBuilder printLine = new StringBuilder (1024);
    Manager dm;
    Document doc;
    boolean utdFound = false;	// FO
    boolean firstTime;
    //for threading
    int numLines;
    int numPages;
    int numChars;
    int bvLineCount;
    BufferedWriter bufferedWriter = null;
    static Logger logger;

    UTD (final Manager dm) {
        this.dm = dm;
        logger = BBIni.getLogger();
    }

   	
    void displayTranslatedFile(String utdFileName, String brailleFileName) {
        beforeBrlNode = null;
        beforeBrlonlyNode = null;
        brlIndex = null;
        brlIndexPos = 0;
        brlonlyIndex = null;
        brlonlyIndexPos = 0;
        firstPage = true;
        firstLineOnPage = true;
        braillePageNumber = 0; //number of braille pages
        firstTableName = null;
        dpi = 0; // resolution
        paperWidth = 0;
        paperHeight = 0;
        leftMargin = 0;
        rightMargin = 0;
        topMargin = 0;
        bottomMargin = 0;
        currentBraillePageNumber = "0";
        currentPrintPageNumber = "0";
        
        Builder parser = new Builder();
        try {
            doc = parser.build (new File(utdFileName));
        } catch (ParsingException e) {
        	logger.log(Level.SEVERE, "Malformed document: " + utdFileName);
            new Notify ("Malformed document");
            return;
        }
        catch (IOException e) {
//          System.out.println("Could not open"+ utdFileName +" because" + e.getMessage());
        	logger.log(Level.SEVERE, "Could not open"+ utdFileName +" because" + e.getMessage());
            new Notify ("Could not open " + utdFileName);
            return;
        }
        final Element rootElement = doc.getRootElement();
        
      
        try {
            //Construct the BufferedWriter object
            bufferedWriter = new BufferedWriter(new FileWriter(brailleFileName));
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
        //Use threading to keep the control of the window

//        new Thread() {
//            public void run() {
              findBrlNodes (rootElement);
//            }
//        }
//        .start();
           try {
                  if (bufferedWriter != null) {
                      bufferedWriter.flush();
                      bufferedWriter.close();
                  }
              } catch (IOException ex) {
                  ex.printStackTrace();
           }
    }

    private void findBrlNodes (Element node) {
        Node newNode;
        Element element;
        String elementName = null;
        firstTime = true;	// FO
        for (int i = 0; (i < node.getChildCount()); i++) {
            newNode = node.getChild(i);
            if (newNode instanceof Element) {
                element = (Element)newNode;
                elementName = element.getLocalName();
                if (elementName.contentEquals("head")) {
                	Element utdElement = findUtdMeta(element);
                	doUtdMeta (utdElement);   // found it
                	
                } else if (elementName.contentEquals("brl")) {
                    if (i > 0) {
                        try{
                            beforeBrlNode = newNode.getChild(i - 1);
                        }
                        catch(IndexOutOfBoundsException e ){
                            //The brl child, newNode, may not have any grandchild of node
                            System.err.println("findBrlNodes: a brl Node does not have child, i = "+i ); 
                            beforeBrlNode = null; 
                            return;
                        }
                    } else {
                        beforeBrlNode = null;
                    }
                        doBrlNode (element);
                    
                } else if (elementName.contentEquals("p") ) {
                	
                	for (int j = 0; j < node.getChildCount(); j++) {
                		// we need to dig down the element for the <brl> elements 
                		Node pNode = node.getChild(j);
                		Element pElement = (Element)pNode;
                		for (int k = 0; k < pElement.getChildCount(); k++) {
                			Node bNode = pElement.getChild(k);
                			if (bNode instanceof Element) {
                	            doBrlNode((Element)bNode);
                			}
                		}
                	}
                } else {
					findBrlNodes(element);
                }
            }
         
//            if(brailleLine.length() > 4096 || printLine.length() > 4096 || i == node.getChildCount()-1) {
              if(brailleLine.length() > 4096 || i == node.getChildCount()-1) {

            	dm.getDisplay().syncExec(new Runnable() {    
                    @Override
					public void run() {    
                    	// FO
                    	if (firstTime) {
                    		dm.getBraille().view.replaceTextRange(0, dm.getBraille().view.getCharCount(), 
                    				brailleLine.toString() );
                    		firstTime = false;
                    	} else {
                            dm.getBraille().view.append(brailleLine.toString() );
                    	} 
                    	
                    	try {
                        bufferedWriter.write(brailleLine.toString());
                    	} catch (IOException e) {
                    		System.err.println("findBrlNodes IOException: " + e.getMessage());
                    	}
                    	brailleLine.delete (0, brailleLine.length());
                    }
                 });    
        
            }
            /** p elements have their own structure **/
            if (!(elementName == null)) {
                if (elementName.contentEquals("p"))  i = node.getChildCount() + 1;
            }
        }  // end for
    }
     
    
    private Element findUtdMeta (Element node) {
    	String elementName;
    	String attValue;
    	Element child = null;
    	Node childNode;
    	
    	for (int i = 0; i < node.getChildCount(); i++) {
    		childNode = node.getChild(i);
    		if (childNode instanceof Element) {
    			child = (Element)childNode;
    		
    		elementName = child.getLocalName();
    		if (elementName.equals ("meta")) {
            	// FO
            	if (child.getAttribute ("name") == null) {
            		continue;
            	};
            	attValue = child.getAttributeValue("name");
            	if (attValue.equals ("utd")) {
            		utdFound = true;
            		break;
            	}
    		}
    		}
    	}
    	return child;
    }

    private void doUtdMeta (Element node) {
        if (braillePageNumber != 0) {
            return;
        }
        String metaContent;
        metaContent = node.getAttributeValue ("name");

        if (metaContent == null ) {
        	logger.log(Level.INFO, "doUtdMeta: metaContent is null");
        	// System.err.println("doUtdMeta: metaContent is null");
        	dm.setMetaContent(false);
        	return;
        }
        if (!(metaContent.equals ("utd"))) {
            return;
        }
        
        dm.setMetaContent(true);
        metaContent = node.getAttributeValue ("content");
        String[] keysValues = metaContent.split (" ", 20);
        for (int i = 0; i < keysValues.length; i++) {
            String keyValue[] = keysValues[i].split ("=", 2);
            if (keyValue[0].equals ("braillePageNumber"))
                braillePageNumber = Integer.parseInt (keyValue[1]);
            else if (keyValue[0].equals ("firstTableName"))
                firstTableName = keyValue[1];
            else if (keyValue[0].equals ("dpi"))
                dpi = Integer.parseInt (keyValue[1]);
            else if (keyValue[0].equals ("paperWidth"))
                paperWidth = Integer.parseInt (keyValue[1]);
            else if (keyValue[0].equals ("paperHeight"))
                paperHeight = Integer.parseInt (keyValue[1]);
            else if (keyValue[0].equals ("leftMargin"))
                leftMargin = Integer.parseInt (keyValue[1]);
            else if (keyValue[0].equals ("rightMargin"))
                rightMargin = Integer.parseInt (keyValue[1]);
            else if (keyValue[0].equals ("topMargin"))
                topMargin = Integer.parseInt (keyValue[1]);
            else if (keyValue[0].equals ("bottomMargin"))
                bottomMargin = Integer.parseInt (keyValue[1]);
        }
    }

    void showLines () {
        brailleLine.append ("\n");
    }

    void doBrlNode (Element node) {
        String tmp = node.getAttributeValue("index");
        String[] indices = null;
        if(tmp != null) indices = node.getAttributeValue ("index").split (" ", 20000);
        if (indices != null) {
            brlIndex = new int[indices.length];
            for (int i = 0; i < indices.length; i++) {
                brlIndex[i] = Integer.parseInt (indices[i]);
            }
        }
        brlIndexPos = 0;
        indices = null;
        Node newNode;
        Element element;
        String elementName;
        for (int i = 0; i < node.getChildCount(); i++) {
            newNode = node.getChild(i);
            if (newNode instanceof Element) {
                element = (Element)newNode;
                elementName = element.getLocalName();

                if (elementName.equals ("newpage")) {
                    //page number is updated in doNewpage
                    doNewpage (element);
                } else if (elementName.equals ("newline")) {
                    numLines++;
                    doNewline (element);
                } else if (elementName.equals ("span")) {
                    doSpanNode (element);
                } else if (elementName.equals ("graphic")) {
                    doGraphic (element);
                }
            }
            else if (newNode instanceof Text) {
                doTextNode (newNode);
            }
        }
        finishBrlNode();
        brlIndex = null;
        brlIndexPos = 0;
    }

    private void doSpanNode (Element node) {
        String whichSpan = node.getAttributeValue ("class");
        if (whichSpan.equals ("brlonly")) {
            doBrlonlyNode (node);
        }
        else if (whichSpan.equals ("locked")) {
            doLockedNode (node);
        }
    }

    private void doBrlonlyNode (Element node) {
        Node newNode;
        Element element;
        String elementName;
        for (int i = 0; i < node.getChildCount(); i++) {
            newNode = node.getChild(i);
            if (newNode instanceof Element) {
                element = (Element)newNode;
                elementName = element.getLocalName();
                if (elementName.equals ("brl")) {
                    insideBrlonly (node);
                }
            }
            else if (newNode instanceof Text) {
                beforeBrlonlyNode = newNode;
            }
        }
    }

    private void insideBrlonly (Element node) {
        String tmp = node.getAttributeValue("index");
        String[] indices = null;
        if(tmp != null) indices = tmp.split (" ", 20000);
        if (indices != null) {
            brlonlyIndex = new int[indices.length];
            for (int i = 0; i < indices.length; i++) {
                brlonlyIndex[i] = Integer.parseInt (indices[i]);
            }
        }
        brlonlyIndexPos = 0;
        indices = null;
        Node newNode;
        Element element;
        String elementName;
        for (int i = 0; i < node.getChildCount(); i++) {
            newNode = node.getChild(i);
            if (newNode instanceof Element) {
                element = (Element)newNode;
                elementName = element.getLocalName();
                if (elementName.equals ("newpage")) {
                    doNewpage (element);
                } else if (elementName.equals ("newline")) {
                    doNewline (element);
                } else if (elementName.equals ("graphic")) {
                    doGraphic (element);
                }
            }
            else if (newNode instanceof Text) {
                doBrlonlyTextNode (newNode);
            }
        }
        brlonlyIndex = null;
        brlonlyIndexPos = 0;
    }

    private void doLockedNode (Element node) {
    }

    private void doNewpage (Element node) {
        String pageNumber = node.getAttributeValue ("brlnumber");
        if(pageNumber != null) {
            currentBraillePageNumber = pageNumber;
            numPages++;
        }
        pageNumber = node.getAttributeValue ("printnumber");
        if(pageNumber!= null) currentPrintPageNumber =pageNumber; 
        //this may need to be reconsidered
        firstLineOnPage = true;
        if (firstPage) {
            firstPage = false;
            return;
        }
        showLines();
    }

    private void doNewline (Element node) {
        String positions = node.getAttributeValue ("xy");
        if (positions != null) {
            String[] horVertPos = positions.split (",", 2);
        }
        if (firstLineOnPage) {
            firstLineOnPage = false;
            return;
        }
        showLines();
    }

    private void doTextNode (Node node) {
        Text text = (Text)node;
        brailleLine.append (text.getValue());
    }

    private void doBrlonlyTextNode (Node node) {
        Text text = (Text)node;
        brailleLine.append (text.getValue());
    }

    private void doGraphic (Element node) {
    }

    private void finishBrlNode() {
        return;
    } 
}
