package org.brailleblaster.louisutdml;
import org.brailleblaster.jlouislibs.Jliblouisutdml;
public class CharSize
{
public int charSize ()
{
Jliblouisutdml bindings = new Jliblouisutdml ();
return bindings.lbu_charSize ();
}
}
