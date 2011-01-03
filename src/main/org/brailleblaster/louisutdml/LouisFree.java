package org.brailleblaster.louisutdml;

import org.liblouis.Jliblouisutdml;

/**
* Free all memory that may have been used by liblouis or 
* liblouisutdml. This class must be called at the end of the application.
* It should not ordinarily be called before then.
*/

public class LouisFree
{

public LouisFree ()
{
new Jliblouisutdml().lbu_free();
}

}
