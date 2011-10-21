import org.eclipse.swt.custom.Styled/Text;
import nu.xom.*;

class BrailleContent implements StyledTextContent
entends Node
{
private StyledTextContent baseContent;

BrailleContent (final StyledText styledText) {
baseContent = styledText.getContent();
styledText.setContent(this);
}

void addTextChangeListener(final TextChangeListener listener)  {
baseContent.addTextChangeListener(listener);
}

