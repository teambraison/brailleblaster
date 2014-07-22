package org.brailleblaster.perspectives.braille.viewInitializer;

import org.brailleblaster.archiver.Archiver;
import org.brailleblaster.archiver.EPub3Archiver;
import org.brailleblaster.archiver.TextArchiver;
import org.brailleblaster.archiver.WebArchiver;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class ViewFactory {
	public static ViewInitializer createUpdater(Archiver arch, BrailleDocument doc, TextView text, BrailleView braille, BBTree tree){
		if(arch instanceof EPub3Archiver)
			return new EPubInitializer(doc, text, braille, tree);
		else if(arch instanceof WebArchiver || arch instanceof TextArchiver)
			return new WebInitializer(doc, text, braille, tree);
		else
			return new NimasInitializer(doc, text, braille, tree);
	}
}
