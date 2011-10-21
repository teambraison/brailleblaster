package org.brailleblaster.wordprocessor;

import org.eclipse.swt.custom.StyledText;
import nu.xom.*;

abstract class AbstractContent implements StyledTextContent {

private StyledTextContent baseContent;

AbstractContent (final StyledText styledText) {
baseContent = styledText.getContent();
styledText.setContent(this);
}

void addTextChangeListener(final TextChangeListener listener)  {
baseContent.addTextChangeListener(listener);
}

}
