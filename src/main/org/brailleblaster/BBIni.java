package org.brailleblaster;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.BrailleblasterPath;
import org.liblouis.liblouisutdml;
import java.lang.UnsatisfiedLinkError;

/**
* Determine and set initial conditions.
*/

public final class BBIni {

private static Display display;
private static String brailleblasterPath;
private static String osName;
private static String osVersion;
private static String fileSep;
private static String nativeCommandPath;
private static String nativeLibraryPath;
private static String programDataPath;
private static String nativeCommandSuffix;
private static String nativeLibrarySuffix;
private static String settingsPath;
private static String tempFilesPath;
private static String platformName;
private static boolean hLiblouisutdml = true;

  /**
  * Private constructor prevents construction outside this class.
  */
  private BBIni() 
throws Exception
{
try {
display = new Display();
} catch (UnsatisfiedLinkError e)
{
e.getMessage();
System.out.println 
("The GUI facility is missing.");
display = null;
}
Main m = new Main();
brailleblasterPath = BrailleblasterPath.getPath (m);
osName = System.getProperty ("os.name");
osVersion = System.getProperty ("os.version");
fileSep = System.getProperty ("file.separator");
platformName = SWT.getPlatform();
nativeLibraryPath = brailleblasterPath + fileSep + "native" + fileSep + 
"lib";
if (platformName.equals("win32"))
nativeLibrarySuffix = ".dll";
else if (platformName.equals ("cocoa"))
nativeLibrarySuffix = ".dylib";
else nativeLibrarySuffix = ".so";
programDataPath = brailleblasterPath + fileSep + "programData";
try {
liblouisutdml.loadLibrary (nativeLibraryPath + fileSep + 
"liblouisutdml" + nativeLibrarySuffix);
liblouisutdml louisutdml = liblouisutdml.getInstance();
louisutdml.setDataPath (programDataPath);
} catch (UnsatisfiedLinkError e)
{
hLiblouisutdml = false;
}
}

public static Display getDisplay()
{
return display;
}

public static boolean haveLiblouisutdml()
{
return hLiblouisutdml;
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

