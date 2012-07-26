/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2010, 2012
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
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
 * Maintained by John J. Boyer john.boyer@abilitiessoft.com
 */

package org.brailleblaster.wordprocessor;

import org.eclipse.swt.widgets.*;

import org.eclipse.swt.custom.*;

import org.eclipse.swt.layout.*;
import org.brailleblaster.BBIni;
import org.eclipse.swt.printing.*;
import org.eclipse.swt.events.*;

import nu.xom.*;
import nu.xom.Text;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.liblouis.liblouisutdml;
import org.brailleblaster.util.Notify;
import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.daisy.printing.*;
import javax.print.PrintException;
import org.eclipse.swt.widgets.Listener;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.settings.Welcome;
import org.brailleblaster.util.FileUtils;
import org.brailleblaster.util.YesNoChoice;
// FO
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
//import org.apache.log4j.Level;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.apache.tika.sax.ToTextContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.SAXException;


/**
 * This class manages each document in an MDI environment. It controls 
 * the braille View and the daisy View.
 */
class DocumentManager {
    final Display display;
    final Shell documentWindow;
    final int documentNumber;
    final String docID;
    int action;
    int returnReason = 0;
    FormLayout layout;
    String documentName = null;
    boolean documentChanged = false;
    BBToolBar toolBar;
    BBMenu menu;
    RecentDocuments rd;
    UTD utd;
    AbstractView activeView;
    DaisyView daisy;
    BrailleView braille;
    BBStatusBar statusBar;
    boolean exitSelected = false;
    Document doc = null;
    NewDocument newDoc;
    String configFileList = null;
    static String tempPath = null;
    boolean haveOpenedFile = false;
    String translatedFileName = null;
    String brailleFileName = null;     // FO
    String daisyWorkFile = null;       // FO
    String tikaWorkFile = null;        // FO
    String newDaisyFile = "utdml-doc"; // FO
    static boolean daisyHasChanged = false; // FO
    static boolean brailleHasChanged = false; // FO
    boolean textAndBraille = false;
	boolean saveUtdml = false;
	boolean metaContent = false;
    
    liblouisutdml louisutdml;
    String logFile = "Translate.log";
    String configSettings = null;
    int mode = 0;
    boolean finished = false;
    private volatile boolean stopRequested = false;
    static final boolean[] flags = new boolean[WPManager.getMaxNumDocs()];
    //static final String[] runningFiles = new String[WPManager.getMaxNumDocs()];
    static String recentFileName = null;
    static int recentFileNameIndex = -1;
    int numLines;
    int numChars;
    // FO
    private Font simBrailleFont, daisyFont;
    boolean displayBrailleFont = false;
    int bvLineCount; 
    private boolean firstTime;
    static int daisyFontHeight = 10;
    static int brailleFontHeight = 14;
    static boolean SimBraille = false;
    static boolean Courier = false;
    static String altFont = "unibraille29";
    static String courierFont = "Courier New";
    LocaleHandler lh = new LocaleHandler();
    StringBuilder brailleLine = new StringBuilder (8192);
    StringBuilder daisyLine = new StringBuilder (8192);
    

    /**
     * Constructor that sets things up for a new document.
     */
    DocumentManager (Display display, int documentNumber, int action, 
            String documentName) {
        this.display = display;
        this.documentNumber = documentNumber;
        docID = new Integer (documentNumber).toString();
        this.action = action;
        this.documentName = documentName;
        tempPath = BBIni.getTempFilesPath() + BBIni.getFileSep();
        louisutdml = liblouisutdml.getInstance();
        documentWindow = new Shell (display, SWT.SHELL_TRIM);

        // FO setup WP window
        layout = new FormLayout();
        documentWindow.setLayout (layout);
        rd = new RecentDocuments(this);
        utd = new UTD(this);
        menu = new BBMenu (this);
        toolBar = new BBToolBar (this);
        /* text window is on the left */
        daisy = new DaisyView (documentWindow);
        braille = new BrailleView (documentWindow);
        // activeView = (ProtoView)daisy;
        statusBar = new BBStatusBar (documentWindow);
        documentWindow.setSize (1000, 700);
        documentWindow.layout(true, true);
        
        documentWindow.addListener(SWT.Close, new Listener(){
            public void handleEvent(Event event) {
//            setReturn(WP.DocumentClosed);
                //this way clicking close box is equivalent to the 'close' item on the menu

                setReturn(WP.BBClosed);
            }
        });

        documentWindow.addListener (SWT.Dispose, new Listener () {
            public void handleEvent (Event event) {
                handleShutdown(event);
            }
        });
        
        Monitor primary = display.getPrimaryMonitor ();
		Rectangle bounds = primary.getBounds ();
		Rectangle rect = documentWindow.getBounds ();
		int x = bounds.x + ((bounds.width - rect.width) / 2) + (documentNumber*30);
		int y = bounds.y + ((bounds.height - rect.height) / 2) + (documentNumber*30);
		documentWindow.setLocation (x, y);
        documentWindow.open();
        setWindowTitle (" untitled"); 
        
        daisy.view.addModifyListener(daisyMod);
        /** for later use 
        braille.view.addModifyListener(brailleMod);
        **/
// + FO        
        /* Find out which scalable fonts are installed */
        FontData[] fd = documentWindow.getDisplay().getFontList(null, true);
        String fn;

        for (int i = 0; i < fd.length; i++ ) {
            fn = fd[i].getName();
                if(fn.contentEquals("SimBraille")) {
                	SimBraille = true;
                	break;
                };
                if(fn.contentEquals(courierFont)) {
                	Courier = true;
                	break;
                };
        }

        if (! SimBraille) {
    		String fontPath = BBIni.getBrailleblasterPath() + "/programData/fonts/SimBraille.ttf";
            String platform = SWT.getPlatform();
        	if (platform.equals("win32") || platform.equals("wpf")) {
        		fontPath = BBIni.getBrailleblasterPath() + "\\programData\\fonts\\SimBraille.ttf";
        	}
        	if (!documentWindow.getDisplay().loadFont(fontPath)) {
        		new Notify(lh.localValue("fontNotLoadedBraille"));
        	}
        };
        
        if (Courier) {
            daisyFont = new Font (documentWindow.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
        } else {
        	
        	String fontPath = BBIni.getBrailleblasterPath() + "/programData/fonts/" + altFont + ".ttf";
        	String platform = SWT.getPlatform();
        	if (platform.equals("win32") || platform.equals("wpf")) {
        		fontPath = BBIni.getBrailleblasterPath() + "\\programData\\fonts\\" + altFont + ".ttf";
        	}
        	if (!documentWindow.getDisplay().loadFont(fontPath)) {
        		new Notify(lh.localValue("fontNotLoadedText"));
        	}
            daisyFont = new Font (documentWindow.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
        }
    	
        daisy.view.setFont(daisyFont) ;
        braille.view.setFont(daisyFont);
   		braille.view.setEditable(false);
                        
        if (documentNumber == 0) {
            new Welcome(); // This then calls the settings dialogs.
        }
        if (action == WP.OpenDocumentGetFile) {
            fileOpen();
        } else if ((action == WP.DocumentFromCommandLine)||(action == WP.OpenDocumentGetRecent)) {
            openDocument(documentName);
        }
        
        boolean stop = false;

        daisy.view.setFocus();

        while (!documentWindow.isDisposed() && (!stop)&&(returnReason == 0)) {
            if (!display.readAndDispatch())
                display.sleep();
            for(boolean b:flags){
                stop |= b;
            }
        }
        //get here if the window is disposed, or someone has a reason        
        if(flags[documentNumber]){
            WPManager.setCurDoc(documentNumber);
            flags[documentNumber] =false; //all should be false now
        }              
        //Then back to WPManager
    }

    // Listeners to set the change flag.
    ModifyListener daisyMod = new ModifyListener () {
      	public void modifyText(ModifyEvent e) {
			 daisyHasChanged = true;
      	}
   };
   
   ModifyListener brailleMod = new ModifyListener () {
     	public void modifyText(ModifyEvent e) {
			 brailleHasChanged = true;
     	}
  };
    
    /**
     * Handle application shutdown signal from OS;
     */
    void handleShutdown (Event event) {
    	
    	if (daisyHasChanged) {
       		YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged") );	
    		if (ync.result == SWT.YES) { 
    			fileSave();
    		} else {
    			daisyHasChanged = false;
    		}
    	}
        event.doit = true;
    }

    /**
     * Clean up before closing the document.
     */
    void finish() {
    	if (daisyHasChanged) {
       		YesNoChoice ync = new YesNoChoice(lh.localValue("hasChanged") );	
    		if (ync.result == SWT.YES) { 
    			fileSave();
    		} else {
    			daisyHasChanged = false;
    		}
    	}
        documentWindow.dispose();
        finished = true;
    }

    /**
     * Checks if a return request  is valid and does any necessary 
     * processing.
     */
    boolean setReturn (int reason) {
        switch (reason) {
        case WP.SwitchDocuments:
            if (WPManager.haveOtherDocuments()) {
                //System.out.println("Switching to next");
                returnReason = reason;
                flags[documentNumber] = true;//this fires the interrupt
                return true;
            }
            new Notify (lh.localValue("oneDocument"));
            return false;
        case WP.NewDocument:
            returnReason = reason;
            break;
        case WP.OpenDocumentGetFile:
            returnReason = reason;
            break;
        case WP.DocumentClosed:
            returnReason = reason;
        	//FO
        	if ((daisy.view.getCharCount() == 0) || (! daisyHasChanged)) {
        		returnReason = WP.BBClosed;
        	}
            break;
        case WP.BBClosed:
            returnReason = reason;
            break;
        default:
            break;
        }
        //WPManager.setCurDoc(documentNumber);
        flags[documentNumber] = true;//this fires the interrupt
        return true;
    }

    /**
     * This method is called to resume processing on this document after 
     * working on another.
     */
    void resume() {
        if (documentWindow.isDisposed())
            return;
        documentWindow.forceActive();
        boolean stop = false;        
        while (!documentWindow.isDisposed() && (!stop)) {
            if (!documentWindow.getDisplay().readAndDispatch())
                documentWindow.getDisplay().sleep();
            for(boolean b:DocumentManager.getflags()){
                stop |= b;
            }
        }
    }

    private void setWindowTitle (String pathName) {
        int index = pathName.lastIndexOf (File.separatorChar);
        if (index == -1) {
            documentWindow.setText ("BrailleBlaster " + pathName);
        } else {
            documentWindow.setText ("BrailleBlaster " + pathName.substring (index + 
                    1));
        }
    }

	void fileDocument () {
        newDoc = new NewDocument();
        newDoc.fillOutBody (daisy.view);
        doc = newDoc.getDocument();
    }

    void fileNew() {
    	// FO
    	if (daisyHasChanged)  {
    		YesNoChoice ync = new YesNoChoice("Warning: The current file has not been saved. Continue?");	
    		if (ync.result == SWT.NO) return; 
    	}

    	daisy.view.replaceTextRange(0, daisy.view.getCharCount(), "");
	    braille.view.replaceTextRange(0, braille.view.getCharCount(), "");
		haveOpenedFile = false;
		brailleFileName = null;
		documentName = null;
		daisyHasChanged = false;
		doc = null;
		BBIni.setUtd(false);
		stopRequested = false;
	    daisy.view.setFocus();
    }

    /* UTD DOCUMENT */
    void fileOpen () {
        if (doc != null){
            returnReason = WP.OpenDocumentGetFile;
            flags[documentNumber] = true;
            return;
        }
        haveOpenedFile = false;
        metaContent = false;
        
        Shell shell = new Shell (display, SWT.DIALOG_TRIM);
        FileDialog dialog = new FileDialog (shell, SWT.OPEN);
        String filterPath = "/";
    	String [] filterNames = new String [] {"UTDML file"};
    	String [] filterExtensions = new String [] {"*.utd"};

    	String platform = SWT.getPlatform();
    	if (platform.equals("win32") || platform.equals("wpf")) {
     		filterPath = System.getProperty ("user.home");
     		if (filterPath == null) filterPath = "c:\\";
    	}
    	dialog.setFilterNames (filterNames);
    	dialog.setFilterExtensions (filterExtensions);
    	dialog.setFilterPath (filterPath);

    	/* temporarily turn off tracking of changes */
    	daisy.view.removeModifyListener(daisyMod);
    	
    	documentName = dialog.open();
        shell.dispose();
        
        if (documentName != null) {
            openDocument (documentName);
    		BBIni.setUtd(true);
    	    haveOpenedFile = true;
    	    metaContent = true;
    	    brailleFileName = getBrailleFileName();
    	    utd.displayTranslatedFile(documentName, brailleFileName);
            braille.view.setEditable(false);
            daisyHasChanged = false;
            daisy.view.addModifyListener(daisyMod);
            setWindowTitle (documentName);
            daisy.view.setFocus();
        }
    }

    void recentOpen(String path){        
        if (doc != null){
            //see if this recent document is already opened in current windows set
            recentFileNameIndex = WPManager.isRunning(path);
            if(recentFileNameIndex!= -1){
                returnReason = WP.SwitchDocuments;
                flags[documentNumber] = true;
                return;
            }            
            recentFileName = path;
            returnReason = WP.OpenDocumentGetRecent;
            flags[documentNumber] = true;
            return;
        }
        documentName = path;
        openDocument (documentName);
        
        int dot = documentName.lastIndexOf(".");
        if (dot > 0) {
            String ext = documentName.substring(dot +1);
            if (ext.contentEquals("utd")) {
            	brailleFileName = getBrailleFileName();
            	utd.displayTranslatedFile(documentName, brailleFileName); 
            }	
        }
    }

    void openDocument (String fileName) {
    	
    	daisy.view.removeModifyListener(daisyMod);
    	
        Builder parser = new Builder();
        try {
            doc = parser.build (fileName);
        } catch (ParsingException e) {
            new Notify (lh.localValue("malformedDocument"));
            return;
        }
        catch (IOException e) {
            new Notify (lh.localValue("couldNotOpen") + " " + documentName);
            return;
        }
        //add this file to recentDocList
        rd.addDocument(fileName);
        setWindowTitle (documentName);
        haveOpenedFile = true;
        numLines = 0;
        numChars = 0;
        statusBar.setText (lh.localValue("loadingDocument") + " " + documentName);
        final Element rootElement = doc.getRootElement();//this needs to be final, because it will be used by a different thread        
        
//      new Thread() {
//        	public void run() {
                while (!stopRequested) {
                walkTree (rootElement);
                }
//          }
//       }
//        .start();
         daisy.view.addModifyListener(daisyMod);
    }

    private void walkTree (Node node) {
        Node newNode;
        numLines = 0;
		numChars = 0;
       
        for (int i = 0; i < node.getChildCount(); i++) {
            newNode = node.getChild(i);
            
            if (newNode instanceof Element) {
                walkTree (newNode);
            }
            else if (newNode instanceof Text) {
            	
            	String nname = ((Element) node).getLocalName();
            	if (! (nname.matches("span") || nname.matches("brl"))) {
            		final String value = newNode.getValue() + "\n";
            		numLines++;
            		numChars += value.length();
 
            		daisyLine.append(value);
            	};
                //the main thread gets to execute the block inside syncExec()
                if(daisyLine.length()>4096 || i == node.getChildCount()-1) {
                    	display.syncExec(new Runnable() {    
                        public void run() {
                            daisy.view.append (daisyLine.toString());
                            statusBar.setText ("Read " + numLines + " lines, " + numChars 
                                    + " characters.");
                        }
                    });
                      daisyLine.delete(0, daisyLine.length());
                }
            }
        }
        stopRequested = true;
    }
    

    void openTikaDocument (String fileName) {
    	
    	daisy.view.removeModifyListener(daisyMod);
    	stopRequested = false;
    	
        Builder parser = new Builder();
        try {
            doc = parser.build (fileName);
        } catch (ParsingException e) {
            new Notify (lh.localValue("malformedDocument"));
            return;
        }
        catch (IOException e) {
            new Notify (lh.localValue("couldNotOpen") + " " + documentName);
            return;
        }

        numLines = 0;
        numChars = 0;
        statusBar.setText (lh.localValue("loadingDocument") + " " + documentName);
        final Element rootElement = doc.getRootElement();//this needs to be final, because it will be used by a different thread        
        
        while (!stopRequested) {
           walkTikaTree (rootElement);
         }
         daisy.view.addModifyListener(daisyMod);
    }

    private void walkTikaTree (Node node) {
        Node newNode;
        numLines = 0;
        numChars = 0;
        
        String tags[] = {"title", "p", "i", "b", "u", "strong", "span"};
        		
        for (int i = 0; i < node.getChildCount(); i++) {
            newNode = node.getChild(i);
            
            if (newNode instanceof Element) {
                walkTikaTree (newNode);   // down one level
            }
            else if (newNode instanceof Text) {
            	
            	String nname = ((Element) node).getLocalName();
				Boolean match = false;
				int j = 0;
				while (!match && (j < tags.length)) {
					if (nname.matches(tags[j++])) match = true;
				}
            	if ( match) {
            		final String value = newNode.getValue() + "\n";
            		            	
            		numLines++;
            		numChars += value.length();
 
            		daisyLine.append(value);
            	};
                //the main thread gets to execute the block inside syncExec()
                if(daisyLine.length()>4096 || i == node.getChildCount()-1) {
                    	display.syncExec(new Runnable() {    
                        public void run() {
                            daisy.view.append (daisyLine.toString());
                            statusBar.setText ("Read " + numLines + " lines, " + numChars 
                                    + " characters.");
                        }
                    });
                      daisyLine.delete(0, daisyLine.length());
                }
            }
        }
        stopRequested = true;
    }

    
    void fileSave() {
    	if (! daisyHasChanged) {
            new Notify (lh.localValue("noChange") );
            return;
    	};
    	
    	/** no open file then do a Save As **/    	
        if (!haveOpenedFile) {
        	fileSaveAs();
        } else {
            saveDaisyWorkFile();
            String fileName = new File(documentName).getName();
        	statusBar.setText(lh.localValue("savingFile") + " " + fileName) ;
 
        	/* save utdml file */
        	if (!metaContent) {
        		System.out.println(fileName + " " + lh.localValue("saveTextOnly"));
        		new FileUtils().copyFile (daisyWorkFile, documentName);
            	statusBar.setText(lh.localValue("fileSaved")) ;
                new Notify (lh.localValue("fileSaved"));
        	} else if (translatedFileName != null) {
        		YesNoChoice ync = new YesNoChoice(lh.localValue("confirmTranslationSaved") );	
        		if (ync.result == SWT.YES) { 
        				System.out.println(fileName + " " + lh.localValue("saveTextBraille"));
        				new FileUtils().copyFile (translatedFileName, documentName);
        				statusBar.setText(lh.localValue("fileSaved")) ;
        				new Notify (lh.localValue("fileSaved"));
        		} else {
        			new Notify(lh.localValue("noChangeSaved")); 
        		}
        		
        	} else {
        		fileSaveAs();
        	}
        }
        daisyHasChanged = false;
    }

    void fileSaveAs () {
    	// FO
        if (daisyWorkFile == null) {
        	/* create the utd text file  */
        	saveDaisyWorkFile();
        };

		textAndBraille = false;
		saveUtdml = false;

        /* dialog asking for the type of UTDML file to save */

		if (braille.view.getCharCount() != 0) {
			SaveOptionsDialog saveChoice = new SaveOptionsDialog(documentWindow);
			saveChoice.setText(lh.localValue("optionSelect"));
			SaveSelection result = saveChoice.open();
			if (result.equals(SaveSelection.TEXT_AND_BRAILLE)) {
				textAndBraille = true;
			} else if (result.equals(SaveSelection.CANCELLED)) {
				return;
			}
			saveUtdml = true;
		}

		
        Shell shell = new Shell (display);
        FileDialog dialog = new FileDialog (shell, SWT.SAVE);
        String filterPath = System.getProperty ("user.home");
    	String [] filterNames = new String [] {"UTDML file"};
        String[] filterExtensions = new String [] {"*.utd"};

       	String platform = SWT.getPlatform();
       	if (platform.equals("win32") || platform.equals("wpf")) {
        		if (filterPath == null) filterPath = "c:\\";
       	}
       	dialog.setFilterPath (filterPath);
       	dialog.setFilterNames(filterNames);
       	dialog.setFilterExtensions (filterExtensions); 
       	if (haveOpenedFile) {
       		dialog.setFileName(documentName);
       	} else {
       		dialog.setFileName(newDaisyFile);
       	}
       	
        String saveTo = dialog.open();
        shell.dispose();
        if (saveTo == null) {
            return;
        }

        String fileName = new File(saveTo).getName();
    	statusBar.setText(lh.localValue("savingFile") + " " + fileName) ;

    	/* text and braille utd */
    	if (textAndBraille) {
            if (translatedFileName == null) {
            	new Notify(lh.localValue("noXlation"));
            	return;
            };
    		
    		new FileUtils().copyFile (translatedFileName, saveTo);
    		
    	} else {	
    		new FileUtils().copyFile (daisyWorkFile, saveTo);
    	}
    	
    	//add this file to recentDocList
    	rd.addDocument(saveTo);
    	statusBar.setText(lh.localValue("fileSaved")) ;
		               
    	if (saveUtdml) statusBar.setText(lh.localValue("fileSaved")) ;
    	
        daisyHasChanged = false;
    }

    void showBraille() {
        
        BufferedReader translation = null;
        try {
            translation = new BufferedReader (new FileReader 
                    (translatedFileName));
        } catch (FileNotFoundException e) {
            new Notify (lh.localValue("couldNotFindFile") + " " + translatedFileName);
        }
        numLines = 0;
        numChars = 0;
        // FO
        firstTime = true;
        boolean eof = false;
        String line;
        
        while(! eof){
        	try {
                line = translation.readLine();
            } catch (IOException e) {
                new Notify (lh.localValue("problemReading") + " " + translatedFileName);
                return;
            }
        	
        	if (line == null) {
        		eof = true;
        	} else {
        		brailleLine.append(line);
        		brailleLine.append('\n');
            	numChars += line.length();     		       
            	numLines++;
        	}
        		
            if ((brailleLine.length() > 4096) || (eof))  {
            		display.syncExec(new Runnable() {
                
            	public void run() {
                	// FO
                	/* Make sure we replace the braille view with the content of the file */
                	if (firstTime)  {
                    	braille.view.replaceTextRange(0, braille.view.getCharCount(), brailleLine.toString() );
                    	firstTime = false;
                	} else {
                		braille.view.append (brailleLine.toString());
                	}	
                	
                	brailleLine.delete (0, brailleLine.length());
                	statusBar.setText (lh.localValue("textTranslated") + " " + numLines + " " +
                			lh.localValue("textLines")  + ", " + numChars + " " +
                            lh.localValue("textCharacters"));
                }  });
            }
        } // end while

        try {
            translation.close();
        } catch (IOException e) {
            new Notify (lh.localValue("problemReading") + " " + translatedFileName);
        }
    }
    
    void saveDaisyWorkFile() {

    	 createWorkDocument();
    	 
         if (doc == null) {
             new Notify (lh.localValue("noOpenFile"));
             return;
         }
         /* UTD text file */
         daisyWorkFile = tempPath + docID + "-tempdoc.xml";
         FileOutputStream writer = null;
         try {
             writer = new FileOutputStream (daisyWorkFile);
         } catch (FileNotFoundException e) {
             new Notify (lh.localValue("cannotOpenFileW") + " " + daisyWorkFile);
             return;
         }
         Serializer outputDoc = new Serializer (writer);
         try {
             outputDoc.write (doc);
         } catch (IOException e) {
             new Notify (lh.localValue("cannotWriteFile"));
             return;
         }
    }

    void createWorkDocument () {
        newDoc = new NewDocument();
        newDoc.fillOutBody (daisy.view);
        doc = newDoc.getDocument();
    }


    void translate(boolean display) {

    	saveDaisyWorkFile();
        
    	if (! BBIni.useUtd()) {
    	    YesNoChoice ync = new YesNoChoice(lh.localValue("askUtdml"));	
		    if (ync.result == SWT.YES) { 
			    BBIni.setUtd(true);
		    } 
    	}
			
        configFileList = "preferences.cfg";
        configSettings = null;
    	if (BBIni.useUtd()) {
            configSettings = "formatFor utd\n" + "mode notUC\n";
        }
    	
        translatedFileName = getTranslatedFileName() ;
        
        boolean result = louisutdml.translateFile (configFileList, daisyWorkFile, 
                translatedFileName, logFile, configSettings, mode);

        if (!result) {
            new Notify (lh.localValue("translationFailed"));
            return;
        }
        
        if (! BBIni.useUtd()) {
        	brailleFileName = getBrailleFileName();
        	new FileUtils().copyFile (translatedFileName, brailleFileName);
        }
        
		metaContent = true;

        if (display) {
        	if (BBIni.useUtd()) {
        		brailleFileName = getBrailleFileName();
        		utd.displayTranslatedFile(translatedFileName, brailleFileName);
        	} else {
        		new Thread() {
        			public void run() {
                     showBraille();
        			}
        		}
        		.start();
        	}
        }
    }
    
    String getTranslatedFileName() {
    	String s = tempPath + docID + "-doc.brl";
    	return s;
    }
    
    String getBrailleFileName() {
    	String s = tempPath + docID + "-doc-brl.brl";
    	return s;
    }
    
    String getTikaTranslatedFileName() {
    	String s = tempPath + docID + "-doc.html";
    	return s;
    }
    
    int getCount(){
        return documentNumber;
    }
    void fileEmbossNow () {
        if ((brailleFileName == null) || (braille.view.getCharCount() == 0) ) {
        	new Notify (lh.localValue("noXlationEmb"));
            return;
        }
        Shell shell = new Shell (display, SWT.DIALOG_TRIM);
        PrintDialog embosser = new PrintDialog (shell);
        PrinterData data = embosser.open();
        shell.dispose();
        if (data == null || data.equals("")) {
            return;
        }
        File translatedFile = new File (brailleFileName);
        PrinterDevice embosserDevice;
        try {
            embosserDevice = new PrinterDevice (data.name, true);
            embosserDevice.transmit (translatedFile);
        } catch (PrintException e) {
//          new Notify ("Could not emboss on "  + data.name);
        	new Notify (lh.localValue("cannotEmboss") + ": " + data.name);
        }
    }
    
    void toggleBrailleFont() {
    	if (displayBrailleFont) {
    		displayBrailleFont = false;
    	} else {
    		displayBrailleFont = true;
    	}
    	setBrailleFont(displayBrailleFont);
    }
    
    void setBrailleFont (boolean toggle) {
    	if (toggle) {
            simBrailleFont = new Font (documentWindow.getDisplay(),"SimBraille", brailleFontHeight, SWT.NORMAL);
            braille.view.setFont(simBrailleFont) ;
	
    	} else {
    //		FontData[] fd = (documentWindow.getDisplay().getSystemFont()).getFontData();
    		if (Courier) {
    			simBrailleFont = new Font (documentWindow.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
    		} else {
        		simBrailleFont = new Font (documentWindow.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
    		}
            braille.view.setFont(simBrailleFont) ;
    	}
    }
    void increaseFont() {
    	
    	daisyFontHeight += daisyFontHeight/4;

    	if (daisyFontHeight >= 48) {
    		return;
    	}

    	if (Courier) {
    		daisyFont = new Font (documentWindow.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
    	} else {
    		daisyFont = new Font (documentWindow.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
    	}
		daisy.view.setFont(daisyFont);
		
		brailleFontHeight += brailleFontHeight/4;
		if (displayBrailleFont) {
			simBrailleFont = new Font (documentWindow.getDisplay(), "SimBraille", brailleFontHeight, SWT.NORMAL);
			braille.view.setFont(simBrailleFont);
		} else {
			braille.view.setFont(daisyFont);
		}
  }
    
    void decreaseFont() {

    	daisyFontHeight -= daisyFontHeight/5;
    	if (daisyFontHeight <= 8) {
    		return;
    	}

    	if (Courier) {
    		daisyFont = new Font (documentWindow.getDisplay(), courierFont, daisyFontHeight, SWT.NORMAL);
    	} else {
    		daisyFont = new Font (documentWindow.getDisplay(), altFont, daisyFontHeight, SWT.NORMAL);
    	}
    	daisy.view.setFont(daisyFont);

		brailleFontHeight -= brailleFontHeight/5;
		if (displayBrailleFont) {
			simBrailleFont = new Font (documentWindow.getDisplay(), "SimBraille", brailleFontHeight, SWT.NORMAL);
			braille.view.setFont(simBrailleFont);
		} else {
			braille.view.setFont(daisyFont);
		}
    }
    
    void importDocument() {
     	
        Shell shell = new Shell (display, SWT.DIALOG_TRIM);
        FileDialog dialog = new FileDialog (shell, SWT.OPEN);
        String filterPath = System.getProperty ("user.home");
    	String [] filterNames = new String [] {"Documents","All Files (*)"};
    	String filterExt = "*.doc;*.docx;*.rtf;*.txt;*.odt;*.pdf;*.xml";
    	String [] filterExtensions = new String [] {filterExt, "*"};

    	String platform = SWT.getPlatform();
    	if (platform.equals("win32") || platform.equals("wpf")) {
    		filterNames = new String [] {"Documents", "All Files (*.*)"};
     		filterExtensions = new String [] {filterExt,"*.*"};
     		filterPath = System.getProperty ("user.home");
     		if (filterPath == null) filterPath = "c:\\";
    	}
    	dialog.setFilterNames (filterNames);
    	dialog.setFilterExtensions (filterExtensions);
    	dialog.setFilterPath (filterPath);

    	/* turn off change tracking */
    	daisy.view.removeModifyListener(daisyMod);
    	
    	documentName = dialog.open();
        shell.dispose();

        /** Tika HTML **/
        try {
        	useTikaHtmlParser(documentName);
        } catch (Exception e) {
        	System.out.println ("Error importing: documentName" );
        }
        
        openTikaDocument(tikaWorkFile);
        	
        braille.view.setEditable(false);

        statusBar.setText (lh.localValue("importCompleted"));
        daisyHasChanged = true;
        setWindowTitle (documentName);
        daisy.view.setFocus();
    }
    
    /** Tika importer that creates a HTML formatted file **/
    void useTikaHtmlParser(String docName) throws Exception {

    	String fn = new File(docName).getName();
    	int dot = fn.lastIndexOf(".");
    	
    	tikaWorkFile = tempPath + docID + "-" + fn.substring(0, dot) + ".html";
   	
    	File workFile = new File(tikaWorkFile);
    	try {
    	    workFile.createNewFile();
    	       }
    	catch (IOException e) {
    	    System.out.println ("useHtmlParser: Error creating Tika workfile " + e);
    	    return;
    	   }
    	
    	InputStream stream = TikaInputStream.get(new File(docName));
        
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        Parser parser = new AutoDetectParser();
        
        OutputStream output = new FileOutputStream(workFile);
  
        try { 
        	ToXMLContentHandler handler = new ToXMLContentHandler(output, "ISO-8859-1");
//        	ToXMLContentHandler handler = new ToXMLContentHandler(output, "UTF-8");
                parser.parse(stream, handler, metadata, context);
        }  catch (IOException e) {
            System.err.println("useHtmlParser IOException: " + e.getMessage());
        }  catch (SAXException e) {
        	System.err.println("useHtmlParser SAXException: " + e.getMessage());
        }  catch (TikaException e) {
        	System.err.println("useHtmlParser TikaException: " + e.getMessage());
        }
        finally {
        	output.close();
        	stream.close();
        }
    }

  
  	void showDaisy(String content) {
              
 		/* Make sure we replace the existing view with the content of the file */
        daisy.view.replaceTextRange(0, daisy.view.getCharCount(), content + "\n");
 	
    }

    void placeholder() {
        new Notify (lh.localValue("funcNotAvail") );
    }

    boolean isFinished(){
        return finished;
    }

    void recentDocuments(){
        rd.open();
    }

    //5/3
    void switchDocuments(){

    }

    static boolean[] getflags(){
        return flags;
    }

    static void setflags(int i,boolean b){
        flags[i] = b;
    }

    static void printflags(){
        for(boolean b:flags)    System.out.print (b+", ");
    }

    static String getRecentFileName(){
        return recentFileName;
    }

    boolean getDaisyHasChanged() {
    	return daisyHasChanged;
    }
}

