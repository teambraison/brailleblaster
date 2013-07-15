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
  * this program; see the file LICENSE.txt
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by John J. Boyer john.boyer@abilitiessoft.com
*/

package org.brailleblaster.wordprocessor;

import java.util.HashMap;

//Passes data between different views and the parent DocumentManager class
public class Message {
	public BBEvent type;
	HashMap<String, Object> args;
	
	public Message(BBEvent type){
		this.type = type;
		this.args = new HashMap<String, Object>();
	}
	
	public void put(String key, Object value){
		args.put(key, value);
	}
	
	public <T> Object getValue(String key){
		return args.get(key);
	}
	
	public void clearMessage(){
		this.args.clear();
	}
	
	public boolean contains(String key){
		return this.args.containsKey(key);
	}
	
	public void remove(String key){
		this.args.remove(key);
	}
}
