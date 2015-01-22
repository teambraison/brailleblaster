package org.brailleblaster.perspectives.braille.eventQueue;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.stylers.InsertElementHandler;
import org.brailleblaster.perspectives.braille.stylers.RemoveElementHandler;
import org.brailleblaster.perspectives.braille.stylers.StyleHandler;
import org.brailleblaster.perspectives.braille.stylers.TextUpdateHandler;
import org.brailleblaster.perspectives.braille.stylers.WhiteSpaceHandler;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class UndoQueue extends EventQueue {
	
	private static final long serialVersionUID = 1658714661481118195L;
	
	public UndoQueue(){
		super();	
	}
	
	@Override
	protected void handleEvent(EventFrame frame, ViewInitializer vi, BrailleDocument doc, MapList list, Manager manager){
		while(!frame.empty()){
			Event event = frame.get(frame.size() - 1);
			switch(event.eventType){
				case Edit:
					TextUpdateHandler editHandler = new TextUpdateHandler(manager, vi, list);
					editHandler.undoEdit(frame);	
					break;
				case Update:
					TextUpdateHandler tuh = new TextUpdateHandler(manager, vi, list);
					tuh.undoText(frame);
					break;
				case Insert:
					RemoveElementHandler remover = new RemoveElementHandler(manager, vi, list);
					remover.undoInsert(frame);
					break;
				case Delete:
					InsertElementHandler inserter = new InsertElementHandler(manager, vi, list);
					inserter.insertElement(frame);
					break;
				case Hide:
					InsertElementHandler ei = new InsertElementHandler(manager, vi, list);
					ei.resetElement(frame);
					break;
				case Style_Change:
					StyleHandler sh = new StyleHandler(manager, vi, list);
					sh.undoStyle(frame);
					break;
				case Whitespace:
					WhiteSpaceHandler wsh = new WhiteSpaceHandler(manager, list);
					wsh.UndoDelete(frame);
					break;
				default:
					break;
			}
		}
	}
}
