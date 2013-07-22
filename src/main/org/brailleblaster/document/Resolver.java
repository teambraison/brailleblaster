package org.brailleblaster.document;

import java.io.File;

import org.brailleblaster.BBIni;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public 	class Resolver implements EntityResolver {
	static String dtdPath = BBIni.getProgramDataPath() + BBIni.getFileSep() + "DTDs";
	String dtdName;
	String originalPublicId;
	String originalSystemId;
	
	public InputSource resolveEntity(String publicId, String systemId) {
		originalSystemId = systemId;
		originalPublicId = publicId;
		if (isOnlineResource(systemId) && checkForSystemId(systemId)) {
			return new MyReader(publicId, dtdName);
		} else {
	         // use default
			return null;
		}
	}
	   
	private boolean isOnlineResource(String systemId){
		if(systemId.length() >= 4 && systemId.substring(0, 4).contains("http"))
			return true;
		else
			return false;
	}
	   
	private boolean checkForSystemId(String systemId){
		dtdName = dtdPath + BBIni.getFileSep() + getDTD(systemId);
		File f = new File(dtdName);
		if(f.exists()){
			return true;
		}
		else
			return false;
	}
	
	private String getDTD(String systemId){
		return systemId.substring(systemId.lastIndexOf("/") + 1);
	}
	
	public String getOriginalSystemId(){
		return originalSystemId;
	}
	
	public String getOriginalpubId(){
		return originalPublicId;
	}
}