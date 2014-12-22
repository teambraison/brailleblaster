package org.brailleblaster.perspectives.braille.eventQueue;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.stylers.RemoveElementHandler;
import org.brailleblaster.perspectives.braille.stylers.HideActionHandler;
import org.brailleblaster.perspectives.braille.stylers.StyleHandler;
import org.brailleblaster.perspectives.braille.stylers.TextUpdateHandler;
import org.brailleblaster.perspectives.braille.stylers.WhiteSpaceHandler;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class RedoQueue extends EventQueue{

	private static final long serialVersionUID = 3665037341723873085L;

	public RedoQueue(){
		super();
	}
	
	@Override
	protected void handleEvent(EventFrame frame, ViewInitializer vi, BrailleDocument doc, MapList list, Manager manager) {
		for(int i = 0; i < frame.size(); i++){
			Event event = frame.get(i);
			switch(event.eventType){
				case Update:
					TextUpdateHandler tuh = new TextUpdateHandler(manager, vi, list);
					tuh.redoText(frame);
					break;
				case Insert:
					break;
				case Delete:
					RemoveElementHandler remover = new RemoveElementHandler(manager, vi, list);
					remover.removeNode(frame);
					break;
				case Hide:
					HideActionHandler h = new HideActionHandler(manager, vi, list);
					h.hideText(frame);
					break;
				case Style_Change:
					StyleHandler s = new StyleHandler(manager, vi, list);
					s.redoStyle(frame);
					break;
				case Whitespace:
					WhiteSpaceHandler wsh = new WhiteSpaceHandler(manager, list);
					wsh.redoDelete(frame);
					break;
				default:
					break;
			}
		}
	}

}
