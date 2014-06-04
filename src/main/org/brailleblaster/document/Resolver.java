package org.brailleblaster.document;

import java.io.File;
import java.io.StringReader;
import org.brailleblaster.BBIni;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public 	class Resolver implements EntityResolver {
	static String dtdPath = new File(BBIni.getProgramDataPath(), "DTDs").getAbsolutePath();
	String dtdName;
	String originalPublicId;
	String originalSystemId;
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		if ((isOnlineResource(systemId) && checkForSystemId(systemId)) || checkForSystemId(systemId)) {
			if(systemId.endsWith(".dtd")){
				originalSystemId = systemId;
				originalPublicId = publicId;
			}
			return new MyReader(publicId, dtdName);
		} 
		else {
			//use default(null) or add empty source for modularization files until determined if they should be added to project
			if(systemId.endsWith(".mod") || systemId.endsWith(".ent"))
				return new InputSource(new StringReader(""));
			else
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