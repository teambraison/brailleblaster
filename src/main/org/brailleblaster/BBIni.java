package org.brailleblaster;

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
private static String osName;
private static String osVersion;

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
osName = System.getProperty("os.name");
osVersion = System.getProperty("os.version");
nativeLibraryPath = brailleblasterPath + fileSep + "native" + fileSep + 
"lib";
nativeLibrarySuffix = ".so";
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

public static String getOsName()
{
return osName;
}

public static String getOsVersion()
{
return osVersion;
}

}

