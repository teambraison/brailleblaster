// Chuck M. - Zipper class that can unzip and rezip files.

/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
 * All rights reserved
 *
 * This file may contain code borrowed from files produced by various 
 * Java development teams. These are gratefully acknowledged.
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

package org.brailleblaster.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.brailleblaster.BBIni;

public class Zipper {
	
	// Every file/folder in the zip.
	LinkedList<String> zipEntries;
	
	// Path and name of xml file that may have been unzipped.
	String xmlUnzippedPath;
	
	// Path we've unzipped to.
	String unzippedPath;
	
	// Full paths to extracted files.
	ArrayList<String> unzippedFilePaths;

	//////////////////////////////////////////////////////
	// Extracts files from .zip archive.
	// 
	// zipFilePath is the file to be unzipped.
	// extractPath is the folder/directory to extract to.
	// 
	// Returns a path to the unzipped xml file.
	public String Unzip(final String zipFilePath, final String extractToHere)
	{
		try
  		{
			// Clear out previous zip path if it exists.
			unzippedFilePaths = new ArrayList<String>();
	        xmlUnzippedPath = "";
	        if(zipEntries != null)
				zipEntries.clear();
			else
				zipEntries = new LinkedList<String>();
			
			// Store where we're sticking the files.
			unzippedPath =  extractToHere;
					
			// Grab the zip entries.
  			ZipFile zipF = new ZipFile( zipFilePath );
  			Enumeration<? extends ZipEntry> entries = zipF.entries();
  			
      		// Holds the current entry in zip file.
      		ZipEntry entry;
      		
      		// Loop through every entry, extract, then load the xml file.
      		while( entries.hasMoreElements() )
      		{
      			// Point to the current entry.
      			entry = entries.nextElement();
      			
      			// Add this entry name to our list.
      			zipEntries.add( entry.getName() );
      			
      			// If this is the xml file, hold onto its path.
      			if( entry.getName().toLowerCase().endsWith(".xml") || entry.getName().toLowerCase().endsWith(".epub")) {
      				xmlUnzippedPath = extractToHere + entry.getName().replace("/", BBIni.getFileSep());
      				entry.getName();
      			}
      			
				 // Get path + filename that we'll be saving out to.
	      		String unzipPath = extractToHere + entry.getName().replace("/", BBIni.getFileSep());
	      		unzippedFilePaths.add(unzipPath);
      			
      			// Create any subdirectories that this file may need.
      			CreateDirsFromPath( unzipPath );
      			
	      		// If this zip entry is a directory itself, skip it.
      			if(entry.isDirectory())
      				continue;
	      		
				 // Get output stream for writing this file out.
				 BufferedOutputStream out = 
				 new BufferedOutputStream( new FileOutputStream(unzipPath), 1000000 );
				 
				 // Create input stream for this zip entry.
				 InputStream in = zipF.getInputStream(entry);
	      			
      			 // Number of bytes read.
				 int count;
				 // Buffer to hold read bytes for writing.
				 byte data[] = new byte[1000000];
				 
				 // Read some data, then write some data.
				 while ( (count = in.read(data, 0, 1000000)) != -1 )
				 {
					 // Write it!
					 out.write(data, 0, count);
				 }
				 
				 // Flush and close output stream.
				 out.flush();
				 out.close();
				 
      		} // while(...
      		
      		// Close the zip file. We don't need it anymore.
      		zipF.close();
  		}
  		catch(Exception e) { e.printStackTrace(); }
		
		// Return xml file we found.
		return xmlUnzippedPath;
	}
	
	//////////////////////////////////////////////////////
	// Rezips previously unzipped files and directories 
	// back into archive.
	// 
	// appendThese[] - List of files(paths) to zip into archive 
	// along with previously unzipped ones.
	public void Rezip(final String appendThese[])
	{
		
	}
	
	//////////////////////////////////////////////////////
	// Zips a FOLDER into specified output file.
	// 
	// inputPath is the source FOLDER to zip.
	// outputPath is the PATH AND FILENAME of the output file.
	// 
	public void Zip(final String inputPath, final String outputPath)
	{
		try
		{
	    	// Create output variables and get ready to write.
		    FileOutputStream fileout = new FileOutputStream( outputPath );
		    ZipOutputStream zipout = new ZipOutputStream(fileout);
		    
		    // Get a list of files in given input path.
		    File dir = new File(inputPath);
		    LinkedList<File> files = GetFileList(dir);
		    
	    	// For every file that we unzipped, zip it back up.
	    	for( int curE = 0; curE < files.size(); curE++ )
	    	{
	    		// Current file or directory.
	    		ZipEntry curEntry = null;
	    		
	    		// Create zip entry path. This is a relative path with all the 'C:\something\somethings' removed.
	    		String zeStr = files.get(curE).getPath().substring( inputPath.length() );
	            
	    		// Point to the current entry.
	    		if( files.get(curE).isDirectory() == false)
	    			curEntry = new ZipEntry( zeStr );
	    		else {
	    			// Must put / on the end, or empty directories are missed.
	    			curEntry = new ZipEntry( zeStr + "/" );
	    		}
	    		
	    		// Get the full path to the input file.
	    		String unzippedEntryPath = files.get(curE).getPath().replace("/", BBIni.getFileSep());
				    
			    // Prepare the zip for writing.
			    zipout.putNextEntry(curEntry);
	    		
			    // If the entry path ends with \, then that means 
	    		// it's a directory. We just created the directory 
			    // with putNextEntry(), so skip the rest.
			    if( files.get(curE).isDirectory() )
	    			continue;
			    
	    		// Create an input stream for this file.
			    FileInputStream filein = new FileInputStream( unzippedEntryPath );
			    
			    // Write the file.
			    int count;
			    byte buff[] = new byte[1000];
	            while( (count = filein.read(buff, 0, buff.length)) != -1 )
	               zipout.write( buff, 0, count );
	            
	            // Close this file's input stream.
	            filein.close();
			    
	    	} // for(int curE = 0...

            // Close output streams.
		    zipout.flush();
		    zipout.close();
		    fileout.flush();
		    fileout.close();
		    
		} // try
		catch(FileNotFoundException e) { e.printStackTrace(); }
		catch(IOException e) { e.printStackTrace(); }
	}
	
	// Creates an unzip path for a Zip Entry, and uses the selected file path 
	// as a base. This is a full path. For example: C:\daisy\nimas\temp\zipDirName\zipentry.xml
	public String CreateZipOutputPath(String selectedZip, ZipEntry zipE)
	{
		// Create string path for file in zip.
		String fileName = zipE.getName();
		fileName = fileName.replace("/", BBIni.getFileSep());
		String filePath = selectedZip.replace("/", BBIni.getFileSep());
		filePath = filePath.substring( 0, filePath.lastIndexOf(BBIni.getFileSep()) );
		filePath += BBIni.getFileSep( ) + "temp" + BBIni.getFileSep();
		return filePath += fileName;
	}
	
	// Using a string path, this function recursively creates directories.
	public void CreateDirsFromPath(String path)
	{
		// Create temp path.
		String filePath = path;
		
		// Grab the shorter path.
		filePath = filePath.substring( 0, filePath.lastIndexOf(BBIni.getFileSep()) );
		
		// Don't even bother with the C:\ drive.
		if(filePath.length() < 3)
			return;

		// Keep going.
		CreateDirsFromPath( filePath );
		
		// Create the directory.
		File newDir = new File(filePath);
		newDir.mkdir();
		
	} // CreateDirsFromPath(String path)
	
	// Returns path and name of xml file, if one was extracted.
	public String GetXmlPath()
	{
		return xmlUnzippedPath;
	}
	
	// Returns path that we unzipped to.
	public String GetUnzippedPath()
	{
		return unzippedPath;
	}
	
	// Recursively traverses directory tree of given folder.
	// Returns list of files and folders with full paths.
	public LinkedList<File> GetFileList(File root)
	{
		// Grab all files and directories.
	    File[] files = root.listFiles();
	    
	    // Create linked list for files in this particular directory.
	    LinkedList<File> fileList = new LinkedList<File>();
	    
	    // For every file/folder, traverse and add.
	    for( int curF = 0; curF < files.length; curF++ )
	    {
	    	// Go ahead and add the current file or folder to list.
	    	fileList.add( files[curF] );
	    	
	    	// If this is a directory, traverse it.
	    	if( files[curF].isDirectory() )
	    	{
	    		// Traverse: get new file list.
	    		LinkedList<File> childList = GetFileList(files[curF]);
	    		
	    		// Add to main list.
	    		fileList.addAll(childList);
	    		
	    	} // if( files[curF].isDirectory() )
	    	
	    } // for(int curF = 0...
	    
	    // Return list of files/folders.
	    return fileList;
	    
	} // GetFileList()
	
	//////////////////////////////////////////////////////////////////////////////////
	// Returns list of files that were unzipped.
	public ArrayList<String> getUnzippedFilePaths()
	{
		// Return the list of files that were unzipped, with full paths.
		return unzippedFilePaths;
		
	} // getUnzippedFilePaths()
	
} // Zipper
