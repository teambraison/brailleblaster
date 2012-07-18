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
import org.eclipse.swt.widgets.Shell;
import org.brailleblaster.BBIni;
import org.brailleblaster.util.YesNoChoice;
import org.brailleblaster.util.ShowBriefly;
import org.brailleblaster.util.Notify;
import java.util.ArrayList;

public class WPManager {
    /**
     * This is the controller for the whole word processing operation. It is the
     * entry point for the word processor, and therefore the only public class.
     */

    String fileName = null;
    int action;
    private Display display;
    private static final int MAX_NUM_DOCS = 4;//the max limit of total number of docs can have at the same time
    private static DocumentManager[] documents = new DocumentManager[MAX_NUM_DOCS];
    private static int documentIndex;
    private static DocumentManager curDoc;

    private static boolean isDeactivated = false; 
    private static DocumentManager prevDoc; 
    private static int prevIndex= -1;

    /**
     * This constructor is the entry point to the word processor. It gets
     * things set up, handles multiple documents, etc.
     */

    public WPManager(String fileName) {
        this.fileName = fileName;
        if (fileName != null) {
            action = WP.DocumentFromCommandLine;
        } else {
            action = WP.NewDocument;
        }
        display = BBIni.getDisplay();
        if (display == null) {
            System.out.println ("Could not find graphical interface environment");
            System.exit(1);
        }
        checkLiblouisutdml();        
        documentIndex = 0;
        curDoc = documents[0] =new DocumentManager(display, 
                documentIndex, action, fileName) ;
        do {
            findTrigger();
            switch (curDoc.returnReason) {
            case WP.DocumentClosed://6
                documents[documentIndex].finish();
                if (getNextAvailableDoc() == -1) return; //no more docs, exit
                WPManager.resumeAll(documentIndex);
                break;
            case WP.SwitchDocuments://4
                if(DocumentManager.recentFileNameIndex != -1){
                    documentIndex = DocumentManager.recentFileNameIndex;
                    DocumentManager.recentFileNameIndex = -1;
                }
                else {
                    documentIndex = getNextAvailableDoc();
                }
                curDoc = documents[documentIndex];
                //System.out.println("Switching...from "+ documentIndex+ "to" +getNextAvailableDoc() );
                curDoc.resume();
                break;
            case WP.NewDocument://1
                if (getNextAvailablePos() == -1){
                    new Notify ("Too many documents");
                    curDoc.resume();
                    break;
                }
                documentIndex = getNextAvailablePos();
                curDoc = documents[documentIndex] = new DocumentManager(display, 
                        documentIndex, WP.NewDocument, fileName);
                break;
            case WP.OpenDocumentGetFile://2
                if (getNextAvailablePos() == -1){
                    new Notify ("Too many documents to open a new file");
                    curDoc.resume();
                    break;
                }
                documentIndex = getNextAvailablePos();
                curDoc = documents[documentIndex] = new DocumentManager(display, 
                        documentIndex, WP.OpenDocumentGetFile, fileName);
                break;
            case WP.OpenDocumentGetRecent://8 open a recent doc in a new windows
                if (getNextAvailablePos() == -1){
                    new Notify ("Too many documents to open the recent document in a new window");
                    curDoc.resume();
                    break;
                }
                documentIndex = getNextAvailablePos();
                fileName = DocumentManager.getRecentFileName();
                curDoc = documents[documentIndex] = new DocumentManager(display, 
                        documentIndex, WP.OpenDocumentGetRecent, fileName);
                break;
            case WP.BBClosed://7
                while(getNextAvailableDoc()!= -1){
                    documents[getNextAvailableDoc()].finish();
                }
                return;
            default:
                break;
            }
        } while (curDoc.returnReason != WP.BBClosed);
    }

    private static void findTrigger(){
        int number = -1;
        int i = 0;
        for(boolean b:DocumentManager.getflags()){
            if(b) {
                number=i;
                break;
            }
            i++;
        }
        if(number != -1)
        {
            DocumentManager.setflags(number, false);
            documentIndex = number;
            curDoc = documents[documentIndex];
        }
    }

    //resume all the windows except the one with documentNumber
    public static void resumeAll(int documentNumber){
        for(int i = 0 ; i< documents.length; i++){
            if(i != documentNumber) {
                if(documents[i] != null) documents[i].resume(); 
            }
        }
    }

    static int getNextAvailableDoc(){
        //search in higher index first for the next available index
        //index-> MAX
        for(int i = documentIndex+1; i <MAX_NUM_DOCS; i++){
            if( documents[i] != null){
                if(documents[i].isFinished())documents[i] = null;
                else return i;
            }
        }
        //0->index
        for(int i = 0; i <= documentIndex; i++){
            if( documents[i] != null){
                if(documents[i].isFinished())documents[i] = null;
                else return i;
            }
        }
        //if no available doc
        return -1;
    }

    //check if a document named fileName is running, return its index or -1;
    static int isRunning(String fileName){
        int start = documentIndex;
        for(int i = 0; i <MAX_NUM_DOCS; i++){
            if( documents[i] != null){
                if(documents[i].isFinished()){
                    documents[i] = null;
                }
                else{
                    //System.out.println("isRunning: NO"+i+"'s name is "+documents[i].documentName);
                    if (documents[i].documentName.equals(fileName))
                        return i;
                }
            }
        }
        return -1;
    }

    int getNextAvailablePos(){
        //see if there is available position for one more document, -1 if it is full
        for(int i = 0; i <MAX_NUM_DOCS; i++){
            if( documents[i] == null) return i;
            else if (documents[i].isFinished()){documents[i] = null; return i;}
        }
        return -1;
    }

    void checkLiblouisutdml() {
        if (BBIni.haveLiblouisutdml()) {
            return;
        }
        if (new YesNoChoice
                ("The Braille facility is not usable." + " See the log."
                        + " Do you wish to continue?")
        .result == SWT.NO) {
            System.exit(1);
        }
    }

    static void setCurDoc(int documentNumber){
        //System.out.println("Something triggers current doc to change, now documentIndex = " + documentNumber );
        documentIndex = documentNumber;
        curDoc = documents[documentIndex];;
    }

    /**
     * Check to see if there are other documents.
     */
    static boolean haveOtherDocuments() {        
        return (getNextAvailableDoc()!= -1);
    }

    static int getMaxNumDocs(){
        return MAX_NUM_DOCS;
    }

}
