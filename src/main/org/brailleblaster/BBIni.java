package org.brailleblaster;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.BrailleblasterPath;
import org.liblouis.Jliblouisutdml;

/**
* Determine and set initial conditions.
*/

public final class BBIni {

private static String brailleblasterPath;
private static String fileSep;
private static String nativeCommandPath;
private static String nativeLibraryPath;
private static String nativeDataPath;
private static String nativeCommandSuffix;
private static String nativeLibrarySuffix;
private static String settingsPath;
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
Jliblouisutdml.loadLibrary (nativeLibraryPath + fileSep + 
"liblouisutdml" + nativeLibrarySuffix);
} catch (Exception e)
{
}
nativeDataPath = brailleblasterPath + fileSep + "native" + fileSep + 
"share";
Jliblouisutdml louisutdml = Jliblouisutdml.getInstance();
louisutdml.setDataPath (nativeDataPath);
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

public static String getNativeDataPath()
{
return nativeDataPath;
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

public static String getOsName()
{
return osName;
}

public static String getOsVersion()
{
return osVersion;
}

}

