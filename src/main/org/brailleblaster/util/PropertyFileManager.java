 /* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2014
* American Printing House for the Blind, Inc. www.aph.org
* and
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * and
  * American Printing House for the Blind, Inc. www.aph.org www.aph.org
  *
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class PropertyFileManager {

	private String filePath;
	
	public PropertyFileManager(String filePath){
		this.filePath = filePath;
	}
	
	public void save(String property, String value){
		Properties prop = new Properties();
		FileInputStream fis = null;
		FileOutputStream fos = null;
    	try { 
    		fis = new FileInputStream(filePath);
    		prop.load(fis);
			prop.setProperty(property, value);
			fos = new FileOutputStream(filePath);
			prop.store(fos, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	finally {
    		if(fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		
    		if(fos != null)
    			try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    	}
	}

	public String getProperty(String property){
		Properties properties = new Properties();
		FileInputStream fis = null;
    	try {
    		fis = new FileInputStream(filePath);
			properties.load(fis);
			if(properties.containsKey(property)){
				return properties.getProperty(property);
			}
			else
				return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    	finally {
    		if(fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    	}
	}
	
	public Enumeration<?> getKeySet(){
		Properties properties = new Properties();
		FileInputStream fis = null;
    	try {
    		fis = new FileInputStream(filePath);
			properties.load(fis);
			return properties.propertyNames();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    	finally {
    		if(fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    	}
	}
}
