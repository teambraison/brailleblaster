package org.brailleblaster.louisutdml;

import org.liblouis.Jliblouisutdml;

/**
* Obtain the size in bytes of the characters used by liblouis and 
* liblouisutdml.
*/

public class CharSize
{
public int getCharSize ()
{
Jliblouisutdml bindings = new Jliblouisutdml ();
return bindings.lbu_charSize ();
}
}
