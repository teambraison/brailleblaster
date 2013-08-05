package org.brailleblaster.document;

import org.brailleblaster.wordprocessor.Encodings;
import org.xml.sax.InputSource;

public class MyReader extends InputSource{
	public MyReader(String publicId, String systemId){
		this.setEncoding(Encodings.UTF_8.encoding());
		this.setPublicId(publicId);
		this.setSystemId(systemId);
	}
	
	public void checkForId(){
		
	}
}
