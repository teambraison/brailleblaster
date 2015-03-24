package org.brailleblaster.louisutdml;

/**
* Handles exceptions that other classes in this package throw when a 
* call to a method in liblouisutdml fails.
*/

public class LiblouisutdmlException extends RuntimeException
{

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public LiblouisutdmlException ()
{
super();
}

public LiblouisutdmlException (String message)
{
super (message);
}

}

