package org.brailleblaster.localization;
import java.util.*;

/**
This class provides the methods used to deal with locales in other 
* packages and classes.
*/

public class LocaleHandler
{
private Locale locale;
private ResourceBundle translations;
private ListBundle listbun;

public Locale setLocale (String language, String country, String 
variant)
{
locale = Locale.getInstance (language, country, variant);
translations = ResourceBundle (MessageTranslations, 
locale);
listbun = new ListResourceBundle ();
return locale;
}

public Locale getLocale ()
{
return locale;
}

public String localValue (String key)
{
Object value = listbun.handleGetObject (key);
return (String)value;
}

}
