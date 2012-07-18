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
    String tempPath = null;
    boolean haveOpenedFile = false;
    String translatedFileName = null;
    String daisyWorkFile = null;       // FO
    String newDaisyFile = "utdml-doc"; // FO
    String newBrailleFile = "brl-doc"; // FO
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
        documentWindow.addListener(SWT.Close, new Listener(){
            public void handleEvent(Event event) {
                setReturn(WP.DocumentClosed);
                //this way clicking close box is equivalent to the 'close' item on the menu
            }
        });
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
        documentWindow.addListener (SWT.Close, new Listener () {
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
		doc = null;
		daisyHasChanged = false;
	    daisy.view.setFocus();
    }

    /* UTD DOCUMENT */
    void fileOpen () {
        if (doc != null){
            returnReason = WP.OpenDocumentGetFile;
            flags[documentNumber] = true;
            return;
        }
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
    		utd.displayTranslatedFile(documentName);
        }
        
        if (metaContent) {
        	translatedFileName = getTranslatedFileName();
        	new FileUtils().copyFile (documentName, translatedFileName);
        }
        
        braille.view.setEditable(false);
        daisyHasChanged = false;
        daisy.view.addModifyListener(daisyMod);
        daisy.view.setFocus();
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
        final Shell selShell = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.CENTER) ;
        selShell.setMinimumSize (300, 150);        

        FillLayout layout = new FillLayout(SWT.VERTICAL);
        layout.marginWidth = 8;
        selShell.setLayout(layout);
  
        final Button b1 = new Button (selShell, SWT.CHECK);
		b1.setText(lh.localValue("saveTextBraille"));
		if (braille.view.getCharCount() == 0) {
			b1.setEnabled(false);
		}
		b1.pack();
		
		final Button b2 = new Button (selShell, SWT.CHECK);
		b2.setText(lh.localValue("saveTextOnly"));
		if (braille.view.getCharCount() == 0) {
			b2.setSelection(true);
		}
		b2.pack();

		Composite c = new Composite(selShell, SWT.NONE);
		RowLayout clayout = new RowLayout();
		clayout.type = SWT.HORIZONTAL;
		clayout.spacing = 30;
		clayout.marginWidth = 8;
		clayout.center = true;
		clayout.pack = true;
		clayout.justify = true;
		c.setLayout(clayout);
		
		Button b3 = new Button (c, SWT.PUSH);
		b3.setText(lh.localValue("buttonSave"));
		b3.pack();
		
		Button b4 = new Button (c, SWT.PUSH);
		b4.setText(lh.localValue("buttonCancel"));
		b4.pack();

		c.pack();
		
		selShell.setText(lh.localValue("optionSelect"));
		selShell.pack();
		
		Monitor primary = display.getPrimaryMonitor ();
		Rectangle bounds = primary.getBounds ();
		Rectangle rect = selShell.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		selShell.setLocation (x, y);

		/* text and Braille */
		b1.addSelectionListener (new SelectionAdapter() {
    		public void widgetSelected (SelectionEvent e) {
    			b2.setSelection(false);
    			textAndBraille = true;
    		}
    	});

		/* Text only */
		b2.addSelectionListener (new SelectionAdapter() {
    		public void widgetSelected (SelectionEvent e) {
    			b1.setSelection(false);
    			textAndBraille = false;
    		}
    	});

		/* Save */
    	b3.addSelectionListener (new SelectionAdapter() {
    		public void widgetSelected (SelectionEvent e) {

    			if ((b1.getSelection()) || (b2.getSelection()) ) {
    				saveUtdml = true;
    				selShell.dispose();
    			} else {
    				 new Notify (lh.localValue("mustSelect")); 
    			}
    		}
    	});
    	
    	/* Cancel */
    	b4.addSelectionListener (new SelectionAdapter() {
    		public void widgetSelected (SelectionEvent e) {
    	        selShell.dispose();
    		}
    	});

		selShell.open();
		
		while (!selShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		/* Nothing selected */
		if (! saveUtdml) {
			return;
		};
		
        Shell shell = new Shell (display);
        FileDialog dialog = new FileDialog (shell, SWT.SAVE);
        String filterPath = "/";
    	String [] filterNames = new String [] {"UTDML file"};
        String[] filterExtensions = new String [] {"*.utd"};

       	String platform = SWT.getPlatform();
       	if (platform.equals("win32") || platform.equals("wpf")) {
        		filterPath = System.getProperty ("user.home");
        		if (filterPath == null) filterPath = "c:\\";
       	}
       	dialog.setFilterNames(filterNames);
       	dialog.setFilterExtensions (filterExtensions); 
       	dialog.setFilterPath (filterPath);
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
//          new Notify ("Could not find " + translatedFileName);
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
//              new Notify ("Problem reading " + translatedFileName);
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

		metaContent = true;

        if (display) {
        	if (BBIni.useUtd()) {
        		utd.displayTranslatedFile(translatedFileName);
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
    
    int getCount(){
        return documentNumber;
    }
    void fileEmbossNow () {
        if ((translatedFileName == null) || (braille.view.getCharCount() == 0)) {
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
        File translatedFile = new File (translatedFileName);
        PrinterDevice embosserDevice;
        try {
            embosserDevice = new PrinterDevice (data.name, true);
            embosserDevice.transmit (translatedFile);
        } catch (PrintException e) {
//          new Notify ("Could not emboss on "  + data.name);
        	new Notify (lh.localValue("cannotEmboss") + ": " + data.name);
        }
    }
    
    //+ FO 
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
    // - FO
    // + FO
    void increaseFont() {
//    	FontData[] fd = (documentWindow.getDisplay().getSystemFont()).getFontData();
//    	System.out.println("font = " + fd[0].getName());
    	
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
//    	FontData[] fd = (documentWindow.getDisplay().getSystemFont()).getFontData();
//    	System.out.println("font = " + fd[0].getName());

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
     	
     	Tika tika = new Tika();
     	
        Shell shell = new Shell (display, SWT.DIALOG_TRIM);
        FileDialog dialog = new FileDialog (shell, SWT.OPEN);
        String filterPath = "/";
    	String [] filterNames = new String [] {"Documents","All Files (*)"};
    	String [] filterExtensions = new String [] {"*.doc;*.docx;*.rtf;*.txt;*.odt;*.pdf","*"};

    	String platform = SWT.getPlatform();
    	if (platform.equals("win32") || platform.equals("wpf")) {
    		filterNames = new String [] {"Documents", "All Files (*.*)"};
     		filterExtensions = new String [] {"*.doc;*.docx;*.rtf;*.txt;*.odt;*.pdf","*.*"};
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
        if (documentName != null) {
         	statusBar.setText ("Importing " + documentName);
        	StringBuffer buf = new StringBuffer();
        	try {
        		/* Use incremental parsing in case file is large */
            	Reader in = tika.parse(new File (documentName));
                for(int c = in.read(); c != -1; c = in.read()) { 
                        buf.append((char)c); 
                } 
        		in.close();
        		
				} catch (IOException e) {
	            new Notify (lh.localValue("couldNotOpen") + " " + documentName);
				e.printStackTrace();
			}
        	/* display content of file */

         	statusBar.setText (lh.localValue("importCompleted"));
         	showDaisy(buf.toString());
         	daisyHasChanged = true;
         	daisy.view.addModifyListener(daisyMod);
         	daisy.view.setFocus();
        }
    }
    
  
  	void showDaisy(String content) {
              
 		/* Make sure we replace the existing view with the content of the file */
        daisy.view.replaceTextRange(0, daisy.view.getCharCount(), content + "\n");
 	
    }

  	  	
    void placeholder() {
    	
//      new Notify ("This menu item is not yet implemented. Sorry.");
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

}

