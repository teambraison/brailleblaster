/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brailleblaster.perspectives.braille.stylers;

import java.util.List;
import nu.xom.Attribute;
import nu.xom.Element;
import org.brailleblaster.utd.IStyle;
import org.brailleblaster.utd.UTDTranslationEngine;

/**
 *
 * @author lblakey
 */
public final class ElementUtils {

	public ElementUtils() {
	}
	
	public static boolean containsStyle(Element elem) {
		return elem.getAttributeValue("style", "utd") != null;
	}
	
	public static boolean containsAction(Element elem) {
		return elem.getAttributeValue("action", "utd") != null;
	}
	
	public static IStyle getStyle(UTDTranslationEngine engine, Element elem) {
		String styleName = elem.getAttributeValue("style", "utd");
		if(styleName == null)
			return null;
		return engine.getStyleDefinitions().getStyleByName(styleName);
	}
}
