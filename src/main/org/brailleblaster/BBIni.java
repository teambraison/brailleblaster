package org.brailleblaster;

import org.eclipse.swt.SWT;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.BrailleblasterPath;
import org.liblouis.liblouisutdml;

/**
* Determine and set initial conditions.
*/

public final class BBIni {

private static String brailleblasterPath;
private static String fileSep;
private static String nativeCommandPath;
private static String nativeLibraryPath;
private static String programDataPath;
private static String nativeCommandSuffix;
private static String nativeLibrarySuffix;
private static String settingsPath;
private static String tempFilesPath;
private static String platformName;

/**
  * Single instance created upon class loading.
  */

private static BBIni singleInstance = new BBIni ();

  public static BBIni getInstance() 
{
     return singleInstance;
  }

  /**
  * Private constructor prevents construction outside this class.
  */
  private BBIni() 
{
Main m = new Main();
brailleblasterPath = BrailleblasterPath.getPath (m);
fileSep = System.getProperty ("file.separator");
platformName = SWT.getPlatform();
nativeLibraryPath = brailleblasterPath + fileSep + "native" + fileSep + 
"lib";
if (platformName.equals("win32"))
nativeLibrarySuffix = ".dll";
else if (platformName.equals ("cocoa"))
nativeLibrarySuffix = ".dylib";
else nativeLibrarySuffix = ".so";
try {
liblouisutdml.loadLibrary (nativeLibraryPath + fileSep + 
"liblouisutdml" + nativeLibrarySuffix);
} catch (Exception e)
{
e.printStackTrace();
}
programDataPath = brailleblasterPath + fileSep + "programData";
liblouisutdml louisutdml = liblouisutdml.getInstance();
louisutdml.setDataPath (programDataPath);
}

public static String getBrailleblasterPath()
{
return brailleblasterPath;
}

public static String getFileSep()
{
return fileSep;
}

public static String getNativeCommandPath()
{
return nativeCommandPath;
}

public static String getNativeLibraryPath()
{
return nativeLibraryPath;
}

public static String getprogramDataPath()
{
return programDataPath;
}

public static String getNativeCommandSuffix()
{
return nativeCommandSuffix;
}

public static String getNativeLibrarySuffix()
{
return nativeLibrarySuffix;
}

public static String getSettingsPath()
{
return settingsPath;
}

public static String getTempFilesPath ()
{
return tempFilesPath;
}

public static String getplatformName()
{
return platformName;
}

}

