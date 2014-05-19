/////////////////////////////////////////////////////////////////////////////////////////
// Java Native Interface for Novint Falcon. Wraps C functions so we can use 
// the Falcon from Java.

package org.brailleblaster.perspectives.imageDescriber;

public class Falconator
{
	//////////////////////////////////////
	// Constructor - Loads the native DLL.
	public Falconator(String absPathToDll)
	{
		// Load library with native code.
		System.load(absPathToDll);
		
		// Initialize the Falcon.
		init();
	}
	
	// Initializes the Falcon before using it. Called in constructor.
	public native void init();
	// Sets the current forces on the device. XYZ.
	public native void setForce(float x, float y, float z);
	// Shuts down the device and cleans up libraries/memory/etc.
	public native void shutdown();
}