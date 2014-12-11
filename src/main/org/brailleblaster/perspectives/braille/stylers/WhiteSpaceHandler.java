package org.brailleblaster.perspectives.braille.stylers;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class WhiteSpaceHandler {

	Manager manager;
	TextView text;
	BrailleView braille;
	MapList list;
	
	public WhiteSpaceHandler(Manager manager, MapList list){
		this.manager = manager;
		this.text = manager.getText();
		this.braille = manager.getBraille();
		this.list = list;
	}
	
	public void removeWhitespace(Message message){
		int brailleStart = 0;
		list.checkList();
		if(!list.empty()){		
			int start = (Integer)message.getValue("offset");
			int index = list.findClosest(message, 0, list.size() - 1);
			TextMapElement t = list.get(index);
			if(start < t.start){
				if(index > 0){
					if(t.brailleList.size() > 0)
						brailleStart = t.brailleList.getFirst().start + (Integer)message.getValue("length");
				}
				else{
					brailleStart = 0;
				}
			}
			else if(t.brailleList.size() > 0)
				brailleStart = t.brailleList.getLast().end;
		
			braille.removeWhitespace(brailleStart, (Integer)message.getValue("length"));
		
			if(start >= t.end && index != list.size() - 1 && list.size() > 1)
				list.shiftOffsetsFromIndex(index + 1, (Integer)message.getValue("length"), (Integer)message.getValue("length"));
			else if(index != list.size() -1 || (index == list.size() - 1 && start < t.start))
				list.shiftOffsetsFromIndex(index, (Integer)message.getValue("length"), (Integer)message.getValue("length"));
		}
		else
			braille.removeWhitespace(0,  (Integer)message.getValue("length"));
	}

}
