/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

/* Authors: Hanxiao Fu, John J. Boyer */
 
package org.brailleblaster.localization;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import org.brailleblaster.util.Notify;

/**
 * This class is intended to be used only by the LocaleHandler class. It 
 * contains methods used by that class. Its fields preserve localization 
 * information across instantiations of LocaleHandler.
*/

class LocalizationBase {
private static Locale locale = null;
private static ResourceBundle bundle = null;
private static final String BUNDLEPATH = "i18n";

/* Prevent anyone from trying to instantiate this class */
private LocalizationBase () {}

/**
 * Set the locale to the user's Default locale. 
 */
static Locale setLocale () {
locale = Locale.getDefault();
bundle = ResourceBundle.getBundle(BUNDLEPATH, locale);
return locale;
}

/**
 * Set the locale to one specified by the user.
 */
static Locale setLocale (String loc) {
  Locale lo = validateLocale(loc);
  if (isValid(lo)) {
    bundle = ResourceBundle.getBundle(BUNDLEPATH, lo);
    locale = lo;
  } else {
    new Notify ("invalid Locale: " + locale);
    return null;
  }
return locale;
}

private static Locale validateLocale(String locale) {
	String[] parts = locale.split("_");
	switch (parts.length) {
	case 3: return new Locale(parts[0], parts[1], parts[2]);
	case 2: return new Locale(parts[0], parts[1]);
	case 1: return new Locale(parts[0]);
	default: throw new IllegalArgumentException("Invalid locale: " + locale);
	}
}

private static boolean isValid(Locale locale) {
	try {
		return locale.getISO3Language() != null && locale.getISO3Country() != null;
	} catch (MissingResourceException e) {
		return false;
	}
}

/**
 * Return the last locale set.
 */
static  Locale getLocale () {
return locale;
}

static String localValue (String key) {
String value;
try {
	value = bundle.getString(key);
	}
catch (MissingResourceException e){
	value = e.getKey();
}
return value;
}

}
