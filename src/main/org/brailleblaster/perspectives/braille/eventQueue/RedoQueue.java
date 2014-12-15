package org.brailleblaster.perspectives.braille.eventQueue;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.stylers.ElementRemover;
import org.brailleblaster.perspectives.braille.stylers.HideActionHandler;
import org.brailleblaster.perspectives.braille.stylers.StyleHandler;
import org.brailleblaster.perspectives.braille.stylers.TextUpdateHandler;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class RedoQueue extends EventQueue{

	private static final long serialVersionUID = 3665037341723873085L;

	public RedoQueue(){
		super();
	}
	
	@Override
	protected void handleEvent(EventFrame f, ViewInitializer vi, BrailleDocument doc, MapList list, Manager manager) {
		for(int i = 0; i < f.size(); i++){
			Event event = f.get(i);
			switch(event.eventType){
				case Update:
					TextUpdateHandler tuh = new TextUpdateHandler(manager, vi, list);
					tuh.redoText(event);
					break;
				case Insert:
					break;
				case Style_Change:
					StyleHandler s = new StyleHandler(manager, vi, list);
					s.redoStyle(f);
					break;
				case Delete:
					ElementRemover remover = new ElementRemover(manager, list, vi);
					remover.removeNode(event);
					break;
				case Hide:
					HideActionHandler h = new HideActionHandler(manager, list, vi);
					h.hideText(event);
					break;
				default:
					break;
			}
		}
	}

}
