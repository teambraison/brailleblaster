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
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.brailleblaster.BBIni;


/**
 * This class contains methods for creating, deleting and testing files 
 * and for searching directories.
 */
public class FileUtils {

    public FileUtils() {
    }

    public boolean exists (String fileName) {
        File file = new File (fileName);
        return file.exists();
    }

    public void create (String fileName) {
        File f = new File(fileName);
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException e) {
                new Notify ("Could not create file" + fileName);
            }
        }
    }
	
    public boolean deleteFile(String fileName) {
		File f= new File(fileName);
		if(f.exists()){
			return f.delete();
		}
		return true;
	}

    public void copyFile (String inputFileName, 
            String outputFileName) {
        FileInputStream inFile = null;
        FileOutputStream outFile = null;
        try {
            inFile = new FileInputStream (new File(inputFileName));
        } catch (FileNotFoundException e) {
            new Notify ("Could not open input file " + inputFileName);
            return;
        }
        try {
            outFile = new FileOutputStream (new File(outputFileName));
        } catch (FileNotFoundException e) {
            new Notify ("Could not open output file " + outputFileName);
            return;
        }
        byte[] buffer = new byte[1024];
        int length = 0;
        while (length != -1) {
            try {
                length = inFile.read (buffer, 0, buffer.length);
            } catch (IOException e) {
                new Notify ("Problem reading " + inputFileName);
                break;
            }
            if (length == -1) {
                break;
            }
            try {
                outFile.write (buffer, 0, length);
            } catch (IOException e) {
                new Notify ("Problem writing to " + outputFileName);
                break;
            }
        }
        try {
            outFile.close();
        } catch (IOException e) {
            new Notify ("output file " + outputFileName + 
                    "could not be completed");
        }
    }

	public boolean deleteDirectory(File directory) {


		  if (directory == null)
		    return false;
		  if (!directory.exists())
		    return true;
		  if (!directory.isDirectory())
		    return false;

		  String[] list = directory.list();

		  // Some JVMs return null for File.list() when the
		  // directory is empty.
		  if (list != null) {
		    for (int i = 0; i < list.length; i++) {
		      File entry = new File(directory, list[i]);

		      if (entry.isDirectory())
		      {
		        if (!deleteDirectory(entry))
		          return false;
		      }
		      else
		      {
		        if (!entry.delete())
		          return false;
		      }
		    }
		  }
		  return directory.delete();
	}

/**
 * Search for partialPathName first in the user's programData directory 
 * and then in the built-in programData directory.
 * @param partialPathName: a name like 
 * liblouisutdml/lbu_files/preferences.cfg
 */
public String findInProgramData (String partialPath) {
File file;
String completePath = BBIni.getUserProgramDataPath() + partialPath;
file = new File (completePath);
if (file.exists()) {
return completePath;
}
completePath = BBIni.getProgramDataPath() + partialPath;
file = new File (completePath);
if (file.exists()) {
return completePath;
}
return null;
}

/**
 * Write a file to userProgramData.
 * @param partialPath: pathname based at userProgramDataPath.
 */
public boolean writeToUserProgrramData (String partialPath) {
return true;
}

}

