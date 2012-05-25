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

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.layout.FormLayout;
import org.brailleblaster.BBIni;
import org.eclipse.swt.printing.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.custom.StyledText;
import nu.xom.*;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.liblouis.liblouisutdml;
import org.brailleblaster.util.Notify;
import java.io.File;
import org.daisy.printing.*;
import javax.print.PrintException;
import org.eclipse.swt.widgets.Listener;
import org.brailleblaster.settings.Welcome;
import org.eclipse.swt.widgets.MessageBox;
import org.brailleblaster.util.FileUtils;

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
    String tempPath;
    boolean haveOpenedFile = false;
    String translatedFileName = null;
    liblouisutdml louisutdml;
    String logFile = "Translate.log";
    String configSettings = null;
    int mode = 0;
    String buffer;
    boolean finished = false;
    private volatile boolean stopRequested = false;
    static final boolean[] flags = new boolean[WPManager.getMaxNumDocs()];
    //static final String[] runningFiles = new String[WPManager.getMaxNumDocs()];
    static String recentFileName = null;
    static int recentFileNameIndex = -1;
    int numLines;
    int numChars;

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
        layout = new FormLayout();
        documentWindow.setLayout (layout);
        rd = new RecentDocuments(this);
        utd = new UTD(this);
        menu = new BBMenu (this);
        toolBar = new BBToolBar (this);
        daisy = new DaisyView (documentWindow);
        braille = new BrailleView (documentWindow);
        // activeView = (ProtoView)daisy;
        buffer="";
        statusBar = new BBStatusBar (documentWindow);
        documentWindow.setSize (1000, 700);
        documentWindow.layout(true, true);
        documentWindow.addListener (SWT.Close, new Listener () {
            public void handleEvent (Event event) {
                handleShutdown(event);
            }
        });
        documentWindow.open();
        setWindowTitle (" untitled");
        if (documentNumber == 0) {
            new Welcome(); // This then calls the settings dialogs.
        }
        if (action == WP.OpenDocumentGetFile) {
            fileOpen();
        } else if ((action == WP.DocumentFromCommandLine)||(action == WP.OpenDocumentGetRecent)) {
            openDocument(documentName);
        }
        boolean stop = false;
        while (!documentWindow.isDisposed() && (!stop)&&(returnReason == 0)) {
            if (!display.readAndDispatch())
                display.sleep();
            for(boolean b:flags){
                stop |= b;
            }
        }
        //get here iff the window is disposed, or someone has a reason        
        if(flags[documentNumber]){
            WPManager.setCurDoc(documentNumber);
            flags[documentNumber] =false; //all should be false now
        }              
        //Then back to WPManager
    }


    /**
     * Handle application shutdown signal from OS;
     */
    void handleShutdown (Event event) {
        event.doit = true;
    }

    /**
     * Clean up before closing the document.
     */
    void finish() {
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
            new Notify("There is only one document.");
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

    void createDocument () {
        newDoc = new NewDocument();
        newDoc.fillOutBody (daisy.view);
        doc = newDoc.getDocument();
    }

    void fileNew() {
        placeholder();
    }

    void fileOpen () {
        if (doc != null){
            returnReason = WP.OpenDocumentGetFile;
            flags[documentNumber] = true;
            return;
        }
        Shell shell = new Shell (display, SWT.DIALOG_TRIM);
        FileDialog dialog = new FileDialog (shell, SWT.OPEN);
        //changed for testing recentDocument
        dialog.setFilterExtensions (new String[] {"*.xml", "utd"});
        //dialog.setFilterNames (new String[] {"DAISY xml file", "DAISY file with UTDML"});
        documentName = dialog.open();
        shell.dispose();
        if (documentName == null) {
            new Notify ("File not found");
            return;
        }
        openDocument (documentName);
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
        Builder parser = new Builder();
        try {
            doc = parser.build (fileName);
        } catch (ParsingException e) {
            new Notify ("Malformed document");
            return;
        }
        catch (IOException e) {
            new Notify ("Could not open " + documentName);
            return;
        }
        //add this file to recentDocList
        rd.addDocument(fileName);
        setWindowTitle (documentName);
        haveOpenedFile = true;
        numLines = 0;
        numChars = 0;
        statusBar.setText ("Loading " + documentName);
        final Element rootElement = doc.getRootElement();//this needs to be final, because it will be used by a different thread        
        //Use threading to keep the control of the window
        new Thread() {
            public void run() {
                while (!stopRequested) {
                    walkTree (rootElement);
                }
            }
        }
        .start();

    }

    private void walkTree (Node node) {
        Node newNode;
        for (int i = 0; i < node.getChildCount(); i++) {
            newNode = node.getChild(i);
            if (newNode instanceof Element) {
                walkTree (newNode);
            }
            else if (newNode instanceof Text) {
                final String value = newNode.getValue();
                numLines++;
                numChars += value.length();
                buffer = buffer.concat(value);
                //the main thread gets to execute the block inside syncExec()
                if(buffer.length()>2048 || i== node.getChildCount()-1){
                    display.syncExec(new Runnable() {    
                        public void run() {
                            daisy.view.append (buffer);
                            statusBar.setText ("Read " + numLines + " lines, " + numChars 
                                    + " characters.");
                        }
                    });
                    buffer = "";
                }
            }
        }
        stopRequested = true;
    }

    void fileSave() {
        placeholder();
    }

    void fileSaveAs () {
        Shell shell = new Shell (display, SWT.DIALOG_TRIM);
        FileDialog dialog = new FileDialog (shell, SWT.SAVE);
        String saveTo = dialog.open();
        shell.dispose();
        if (saveTo == null) {
            new Notify ("could not write to " + saveTo);
            return;
        }
        if (translatedFileName == null) {
            new Notify ("There is no translated file to be saved.");
            return;
        }
        new FileUtils().copyFile (translatedFileName, saveTo);
        //add this file to recentDocList
        rd.addDocument(translatedFileName);
    }

    void showBraille() {
        String line;
        BufferedReader translation = null;
        try {
            translation = new BufferedReader (new FileReader 
                    (translatedFileName));
        } catch (FileNotFoundException e) {
            new Notify ("Could not fine " + translatedFileName);
        }
        numLines = 0;
        numChars = 0;
        
        while(true){
            try {
                line = translation.readLine();
            } catch (IOException e) {
                new Notify ("Problem reading " + translatedFileName);
                return;
            }
            if (line == null) {
                break;
            }
            numLines++;
            numChars += line.length();
            final String myLine = line;
            display.syncExec(new Runnable() {
                public void run() {
                    braille.view.append (myLine + "\n");
                    statusBar.setText ("Translated " + numLines + " lines, " + numChars 
                            + " characters.");
                    
                }
            });
        }
        try {
            translation.close();
        } catch (IOException e) {
        }
    }

    void translate() {
        if (!haveOpenedFile) {
            /* We have a new file. */
            createDocument();
        }
        if (doc == null) {
            new Notify ("There is no open file.");
            return;
        }
        configFileList = "preferences.cfg";
        if (BBIni.useUtd()) {
            configSettings = "formatFor utd\n"
                    + "mode notUC\n";
        }
        String docFile = tempPath + docID + "-tempdoc.xml";
        translatedFileName = tempPath + docID + "-doc.brl";
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream (docFile);
        } catch (FileNotFoundException e) {
            new Notify ("could not open file for writing");
            return;
        }
        Serializer outputDoc = new Serializer (writer);
        try {
            outputDoc.write (doc);
        } catch (IOException e) {
            new Notify ("Could not write to file");
            return;
        }
        boolean result = louisutdml.translateFile (configFileList, docFile, 
                translatedFileName, logFile, configSettings, mode);
        if (!result) {
            new Notify ("Translation failed.");
            return;
        }
        if (BBIni.useUtd()) {
            utd.displayTranslatedFile();
        } else {
            braille.view.setEditable(false);
            new Thread() {
                public void run() {
                        showBraille();
                }
            }
            .start();
        }
    }

    int getCount(){
        return documentNumber;
    }
    void fileEmbossNow () {
        if (translatedFileName == null) {
            translate();
        }
        if (translatedFileName == null) {
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
            new Notify ("Could not emboss on "  + data.name);
        }
    }

    void placeholder() {
        new Notify ("This menu item is not yet implemented. Sorry.");
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

