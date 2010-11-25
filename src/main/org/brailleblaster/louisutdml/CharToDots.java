package org.brailleblaster.liblouisutdml;
import org.brailleblaster.jlouislibs.Jliblouisutdml;
public class CharToDots
{
public String CharToDots (Strring inbuf, String config, int mode)
{
byte [] inbufx;
byte[] outbuf;

Jliblouisutdml bindings = new Jliblouisutdml ();
bindings.lbu_charToDots (trrantab, inbufx, outbuf, mode);
}
}
