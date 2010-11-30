package org.brailleblaster.louisutdml;
import org.brailleblaster.jlouislibs.Jliblouisutdml;
public class CharToDots
{
public void charToDots (String trantab, String inbuf, String 
outbuf, int mode)
{
byte[] inbufx = null;
byte[] outbufx = null;
String logFile = null;
Jliblouisutdml bindings = new Jliblouisutdml ();
bindings.lbu_charToDots (trantab, inbufx, outbufx, logFile, 
mode);
}
}
