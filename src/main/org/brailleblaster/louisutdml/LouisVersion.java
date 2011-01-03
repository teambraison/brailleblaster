package org.brailleblaster.louisutdml;

import org.liblouis.Jliblouisutdml;

/**
* Return the virsions of liblouis and liblouisutdml.
*/

public class LouisVersion
{
public String getVersion ()
{
Jliblouisutdml bindings = new Jliblouisutdml ();
return bindings.lbu_version();
}
}
