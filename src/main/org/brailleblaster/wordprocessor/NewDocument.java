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
 * Maintained by John J. Boyer john.boyer@abilitiessoft.com
 */

package org.brailleblaster.wordprocessor;

import nu.xom.*;
import org.brailleblaster.util.Notify;
import java.io.IOException;
import org.eclipse.swt.custom.StyledText;

class NewDocument {
	Element headAdd;
	Element sectionAdd;
	Element frontAdd;
	Element bodyAdd;
	Element rearAdd;

	private final String framework = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<document "
			+ "profile='http://www.daisy.org/z3986/2011/vocab/profiles/default/' "
			+ "prefix='' xml:lang='' "
			+ "xmlns='http://www.daisy.org/ns/z3986/authoring/' "
			+ "xmlns:its='http://www.w3.org/2005/11/its' "
			+ "xmlns:ssml='http://www.w3.org/2001/10/synthesis' "
			+ "xmlns:xforms='http://www.w3.org/2002/xforms/' "
			+ "xmlns:m='http://www.w3.org/1998/Math/MathML' "
			+ "xmlns:sel='http://www.daisy.org/ns/z3986/authoring/features/select/' "
			+ "xmlns:rend='http://www.daisy.org/ns/z3986/authoring/features/rend/'>"
			+ "<head>"
			+ "<meta rel='z3986:profile' "
			+ "resource='http://www.daisy.org/z3986/2011/auth/profiles/genericdocument/0.7/'>"
			+ "<meta rel='z3986:feature' "
			+ "resource='http://www.daisy.org/z3986/2011/auth/features/its-ruby/0.6/'/>"
			+ "<meta rel='z3986:feature' "
			+ "resource='http://www.daisy.org/z3986/2011/auth/features/ssml/0.5/'/>"
			+ "<meta rel='z3986:feature' "
			+ "resource='http://www.daisy.org/z3986/2011/auth/features/forms/0.4/'/>"
			+ "<meta rel='z3986:feature' "
			+ "resource='http://www.daisy.org/z3986/2011/auth/features/mathml/0.4/'/>"
			+ "<meta rel='z3986:feature' "
			+ "resource='http://www.daisy.org/z3986/2011/auth/features/select/0.4/'/>"
			+ "<meta rel='z3986:feature' "
			+ "resource='http://www.daisy.org/z3986/2011/auth/features/rend/0.6/'/>"
			+ "<meta rel='z3986:feature' "
			+ "resource='http://www.daisy.org/z3986/2011/auth/features/svg-cdr/0.2/'/>"
			+ "</meta>" + "<meta property='dcterms:identifier' content=''/>"
			+ "<meta property='dcterms:publisher' content=''/>"
			+ "<meta property='dcterms:modified' content=''/>" + "</head>"
			+ "<body>" + "<section/>" + "</body>" + "</document>";

	Document doc;

	NewDocument() {
		startDocument();
	}

	private void startDocument() {
		Builder builder = new Builder();
		try {
			doc = builder.build(framework, null);
		} 
		catch (ParsingException e) {
			new Notify("Framework is malformed");
			return;
		} 
		catch (IOException e) {
		}
		Element rootElement = doc.getRootElement();
		findAddChildPoints(rootElement);
	}

	private void findAddChildPoints(Node node) {
		Node newNode = null;
		Element elementNode = null;
		String elementName;
		String attributeName;
		for (int i = 0; i < node.getChildCount(); i++) {
			newNode = node.getChild(i);
			if (newNode instanceof Element) {
				elementNode = (Element) newNode;
				elementName = elementNode.getLocalName();
				if (elementName.equals("head")) {
					headAdd = elementNode;
				} else if (elementName.equals("section")) {
					sectionAdd = elementNode;
				}
				findAddChildPoints(elementNode);
			}
		}
	}

	void restartDocument() {
		doc = null;
		startDocument();
	}

	void fillOutBody(StyledText view) {
		String text = view.getText();

		int length = text.length();
		int beginParagraph = 0;
		int endParagraph = 0;
		while (beginParagraph < length) {
			Element paragraph = new Element("p");
			char c = 0;
			int i;
			for (i = beginParagraph; (i < length
					&& (c = text.charAt(i)) != 0x0a && c != 0x0d); i++)
				;
			if (i < length) {
				// FO endParagraph = i - 1;
				endParagraph = i;
				if (c == 0x0a) {
					i++;
				} else if (text.charAt(i + 1) == 0x0a) {
					i += 1;
				} else {
					i++;
				}
			} else {
				endParagraph = length;
			}
			if (beginParagraph >= 0 && endParagraph > beginParagraph) {
				paragraph.appendChild(text.substring(beginParagraph,
						endParagraph));
			}
			sectionAdd.appendChild(paragraph);
			paragraph = null;
			beginParagraph = i;
		}
	}

	Document getDocument() {
		return doc;
	}

}
