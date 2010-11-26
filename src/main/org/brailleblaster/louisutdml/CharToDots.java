package org.brailleblaster.louisutdml;
import org.brailleblaster.jlouislibs.Jliblouisutdml;
public class CharToDots
{
public String CharToDots (String inbuf, String config, int mode)
{
byte [] inbufx;
byte[] outbuf;
String trantab;
Jliblouisutdml bindings = new Jliblouisutdml ();
bindings.lbu_charToDots (trrantab, inbufx, outbuf, mode);
}
}
