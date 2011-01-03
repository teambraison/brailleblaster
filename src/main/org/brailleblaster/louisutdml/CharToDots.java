package org.brailleblaster.louisutdml;

import org.liblouis.Jliblouisutdml;

public class CharToDots
{

public CharToDots (String trantab, String inbuf, String 
outbuf, int mode)
throws LiblouisutdmlException
{
byte[] inbufx = null;
byte[] outbufx = null;
String logFile = null;
Jliblouisutdml bindings = new Jliblouisutdml ();
bindings.lbu_charToDots (trantab, inbufx, outbufx, logFile, 
mode);
}

}
