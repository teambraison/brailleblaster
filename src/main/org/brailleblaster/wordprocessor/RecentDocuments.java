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

/* Author: Hanxiao Fu */

package org.brailleblaster.wordprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Combo;
import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;

/**
 * Pick a document from those recently opened and return its absolute 
 * path.
 */ 

class RecentDocuments {
    Shell shell;
    Combo combo;
    ArrayList<String> recentDocsList;//this contains the full path and name of recent files
    String[] recentDocsArr;//this contains only the name of a file if there's no duplicates of it
    File file;  
    private String recentFiles;
    private static final int MAX_NUM_FILES=50;
    private String fileSep;
    private DocumentManager dm;
    // FO
    LocaleHandler lh = new LocaleHandler();

    RecentDocuments(final DocumentManager dm) {
        this.dm = dm;
        recentFiles = BBIni.getRecentDocs();
        fileSep = BBIni.getFileSep();
        file = new File(recentFiles);
        readList();
    }

    void open() {
        readList();
        processDocsList();
        Display display = BBIni.getDisplay();
        shell = new Shell (display, SWT.DIALOG_TRIM);
        shell.setText("Recent documents");
        // FO
        Monitor primary = display.getPrimaryMonitor();
      	Rectangle bounds = primary.getBounds();
      	Rectangle rect = shell.getBounds();
      	int x = bounds.x + ((bounds.width - rect.width) / 2) + 10;
      	int y = bounds.y + ((bounds.height - rect.height) / 2) + 10;
      	shell.setLocation (x, y);
        
        GridLayout gridLayout = new GridLayout ();
        shell.setLayout (gridLayout);
        gridLayout.marginBottom = 20;
        gridLayout.marginLeft = 10; 
        gridLayout.marginRight = 10; 
        gridLayout.numColumns = 3;
        gridLayout.horizontalSpacing = 30;
        
        combo = new Combo (shell, SWT.DROP_DOWN | SWT.READ_ONLY);
        if (recentDocsArr.length > 25) {
            combo.setVisibleItemCount(25);
        } else {
            combo.setVisibleItemCount(recentDocsArr.length);
        }
        combo.setItems(recentDocsArr);
        combo.select(0);
               
        Button open = new Button(shell, SWT.PUSH);
        open.setText(lh.localValue("&Open"));
        
        Button cancel = new Button(shell, SWT.PUSH);
        cancel.setText(lh.localValue("buttonCancel"));
        
        shell.setDefaultButton(open);

        cancel.addSelectionListener (new SelectionAdapter() {
        	public void widgetSelected (SelectionEvent e) {
        		shell.close();
        	}
        });
        
        open.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int key = combo.getSelectionIndex();
                if(key != -1){
                String path = recentDocsList.get(key);
                shell.close();
                dm.recentOpen (path);
                }
//              shell.close();
            }
        });
        // show the SWT window
        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        // tear down the SWT window
        shell.dispose();     
    }

    private void processDocsList() {
        recentDocsArr = recentDocsList.toArray(new String[recentDocsList.size()]);
        boolean[] dupArr = new boolean[recentDocsArr.length];
        for(boolean b:dupArr) b=false;
        for( int i=0; (i < recentDocsArr.length-1)&&(!dupArr[i]);i++) {
            String s1 = recentDocsArr[i];
            int index = s1.lastIndexOf(fileSep);
            if (index>=0) s1 = s1.substring(index);
            for(int j=i+1; (j< recentDocsArr.length)&&(!dupArr[j]); j++) {
                String s2 = recentDocsArr[j];
                index = s2.lastIndexOf(fileSep);
                if (index>=0) s2 = s2.substring(index);
                if(s1.equals(s2)) {
                    dupArr[i]=true;
                    dupArr[j]=true;
                }
            }
        }
        for(int i=0; i<recentDocsArr.length;i++) {
            if(!dupArr[i])
                recentDocsArr[i] = recentDocsArr[i].substring(recentDocsArr[i].lastIndexOf(fileSep)+1);
        }
    }

    void addDocument(String document) {
        if(document!=null) {
        recentDocsList.remove(document); 
        recentDocsList.add(0, document);
        if(recentDocsList.size()>MAX_NUM_FILES) recentDocsList.remove(MAX_NUM_FILES);
        storeList();}
    }

    private void storeList() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            new Notify(e.getMessage());
        }
        try {
            for( String s:recentDocsList) {
                writer.write(s);
                writer.newLine();
            }
        } catch (IOException e) {
            new Notify(e.getMessage());
        }finally{
            try {
                writer.close();
            } catch (IOException e) {
                new Notify(e.getMessage());
            }
        }

    }

    private void readList() {
        //May need to set Charset when there are special characters
        //Charset charset = Charset.forName("US-ASCII");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            new Notify(e.getMessage());
        }
        recentDocsList = new ArrayList<String>();
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                recentDocsList.add(line);
            }
        } catch (IOException e) {
            new Notify(e.getMessage());
        }finally{
            try {
                reader.close();
            } catch (IOException e) {
                new Notify(e.getMessage());
            }
        }
    }
}
