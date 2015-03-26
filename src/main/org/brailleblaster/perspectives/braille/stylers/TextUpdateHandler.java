package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;
import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParentNode;
import nu.xom.Text;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.eventQueue.Event;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ModelEvent;
import org.brailleblaster.perspectives.braille.eventQueue.ViewEvent;
import org.brailleblaster.perspectives.braille.mapping.elements.BrailleMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.Message;
//import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;

public class TextUpdateHandler extends Handler {

	BrailleDocument document;

	public TextUpdateHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);
		document = manager.getDocument();
	}
	
	public void updateText(Message message){
		message.put("selection", tree.getSelection(list.getCurrent()));
		if(list.getCurrent().isMathML()){
			manager.dispatch(Message.createRemoveNodeMessage(list.getCurrentIndex(), list.getCurrent().end - list.getCurrent().start));
			message.put("diff", 0);
		}
		else {
			addUndoEvent();
			resetText(message);
		}
		manager.getArchiver().setDocumentEdited(true);
	}
	
	public void undoText(EventFrame f){
		//TODO:will need to be redesigned
		while(!f.empty() && f.peek().getEventType().equals(EventTypes.Update)){
	//		ModelEvent ev = (ModelEvent)f.pop();
	//		if(ev.getTextOffset() >= text.getCurrentStart() && ev.getTextOffset() <= text.getCurrentEnd())
	//			text.view.setCaretOffset(ev.getTextOffset());	
	//		else{
	//			list.setCurrent(ev.getListIndex());
	//			text.view.setCaretOffset(list.getCurrent().start);
	///			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	//		}
	//		int changes = calculateEditDifference(ev, list.getCurrent());
	//		addRedoEvent();
	//		Message m = Message.createUpdateMessage(list.getCurrent().start, ev.getNode().getValue(), changes);
	//		resetModelEvent(m, true);
	//		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	//		text.adjustCurrentElementValues(-changes);
		}
	}
	
	public void redoText(EventFrame f){
		//TODO:will need to be redesigned
		while(!f.empty() && f.peek().getEventType().equals(EventTypes.Update)){
//			ModelEvent ev = (ModelEvent)f.pop();
//			if(ev.getTextOffset() >= text.getCurrentStart() && ev.getTextOffset() <= text.getCurrentEnd())
//				text.view.setCaretOffset(ev.getTextOffset());	
//			else{
//				list.setCurrent(ev.getListIndex());
//				text.view.setCaretOffset(list.getCurrent().start);
//				manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
//			}
//			addUndoEvent();
//			int changes = calculateEditDifference(ev, list.getCurrent());
//			Message m = Message.createUpdateMessage(list.getCurrent().start, ev.getNode().getValue(), changes);
//			resetModelEvent(m, false);
//			manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
			//text.adjustCurrentElementValues(-changes);
		}
	}

	private int calculateEditDifference(ModelEvent ev, TextMapElement t){
		int evLength = ev.getNode().getValue().length();
		int viewLength = text.view.getTextRange(t.start, t.end - t.start).replace("\n", "").length();
		
		return evLength - viewLength;
	}
	
	private void resetModelEvent(Message message, boolean undoEvent){
		document.updateDOM(list, message);
		braille.updateBraille(list.getCurrent(), message);
		if(undoEvent)
			text.redoText(message);
		else {
			message.put("length", text.getCurrentEnd() - text.getCurrentStart());
			text.reformatText(list.getCurrent().n, message, manager);
			message.put("length", (Integer)message.getValue("length") + text.getCurrentChanges());
			text.adjustCurrentElementValues(0);
		}
		
		list.updateOffsets(list.getCurrentIndex(), message);
		list.checkList();
	}
	
	private void resetText(Message message){
		Element block = (Element)manager.getDocument().getEngine().findTranslationBlock(list.getCurrent().n);
		ArrayList<Integer>indexes = tree.getItemPath();
		ArrayList<TextMapElement>elList = list.findTextMapElements(list.getCurrentIndex(), block, true);
		
		LinkedList<Integer>nodeIndexes = null;
		boolean emptyNode = emptyNode((String)message.getValue("newText")); 
		if(emptyNode)
			nodeIndexes = nodeIndexes(list.getCurrent().n, block);
			
		int startIndex = list.indexOf(elList.get(0));
		int textOffset = elList.get(0).start;
		int brailleOffset = elList.get(0).brailleList.getFirst().start;
		int diff = ((String)message.getValue("newText")).length() - list.getCurrent().getText().length();
		
		document.updateDOM(list, message);
		block = (Element)message.getValue("Element");
		clearViews(elList, startIndex, diff);
		for(int i = 0; i < elList.size(); i++)
			list.remove(elList.get(i));	

	//	if(emptyNode)
	//		manager.getDocument().insertEmptyBrailleNode(block, nodeIndexes);

		int length = repopulateRange(block, startIndex);
		elList = getListRange(startIndex, length);
		setViews(elList, startIndex, textOffset, brailleOffset, false);
		tree.rebuildTree(indexes);
		list.setCurrent(list.getCurrentIndex());
		list.checkList();
	}
	
	private LinkedList<Integer> nodeIndexes(Node n, Element parent){
    	LinkedList<Integer> nodes = new LinkedList<Integer>();
    	ParentNode  e = n.getParent();
    	nodes.push(e.indexOf(n));
    	while(!e.equals(parent)){
    		n = e;
    		e = e.getParent();
    		nodes.push(e.indexOf(n));
    	}
    	
    	return nodes;
    }
	
	private void clearViews(ArrayList<TextMapElement>elList, int index, int diff){
		int start = elList.get(0).start;
		int end = elList.get(elList.size() - 1).end;
		int brailleStart = elList.get(0).brailleList.getFirst().start;
		int brailleEnd = elList.get(elList.size() - 1).brailleList.getLast().end;
		
		text.replaceTextRange(start, (end + diff) - start, "");
		braille.replaceTextRange(brailleStart, brailleEnd - brailleStart, "");
		list.shiftOffsetsFromIndex(index, -(end - start), -(brailleEnd - brailleStart));
	}
	
	private int repopulateRange(Element e, int index){
		ArrayList<TextMapElement> elList = 	constructMapElement(e);
		for(int i = 0; i < elList.size(); i++, index++)
			vi.addElementToSection(list, elList.get(i), index);
			
		return elList.size();
	}
	 
	private ArrayList<TextMapElement>getListRange(int start, int length){
		ArrayList<TextMapElement>textList = new ArrayList<TextMapElement>();
		for(int i = start; i < start + length; i++)
			textList.add(list.get(i));
		
		return textList;
	}
	private ArrayList<TextMapElement> constructMapElement(Element e){
		ArrayList<TextMapElement> elList = new ArrayList<TextMapElement>();
		
		for(int i = 0; i < e.getChildCount(); i++){
			if(e.getChild(i) instanceof Text){
				elList.add(new TextMapElement(e.getChild(i)));
			}
			else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && !isBoxLine(e)){
				for(int j = 0; j < e.getChild(i).getChildCount(); j++){
					if(e.getChild(i).getChild(j) instanceof Text){
						elList.get(elList.size() - 1).brailleList.add(new BrailleMapElement(e.getChild(i).getChild(j)));
					}
				}
			}
			else if(e.getChild(i) instanceof Element && ((Element)e.getChild(i)).getLocalName().equals("brl") && isBoxLine(e))
				elList.add(new BrlOnlyMapElement(e.getChild(i), e));
			else if(e.getChild(i) instanceof Element)
				elList.addAll(constructMapElement((Element)e.getChild(i)));
		}
		
		return elList;
	}
	
	private void setViews(ArrayList<TextMapElement> elList, int index, int textOffset, int brailleOffset, boolean format){
		Message m = new Message(null);
		int count = elList.size();
		
		for(int i = 0; i < count; i++){
			int brailleLength = 0;
			
			text.resetSelectionElement(m, vi, list, index, textOffset, elList.get(i), format);
			textOffset = elList.get(i).end;
			
			for(int j = 0; j < elList.get(i).brailleList.size(); j++){
				braille.resetSelectionElement(m, list, list.get(index), elList.get(i).brailleList.get(j), brailleOffset, format);
				brailleOffset = (Integer)m.getValue("brailleOffset");
				brailleLength += (Integer)m.getValue("brailleLength");
			}
				
			int textLength = (Integer)m.getValue("textLength");
			textOffset = (Integer)m.getValue("textOffset");
			list.shiftOffsetsFromIndex(index + 1, textLength, brailleLength);
			index++;
		}
	}
	
	private void addUndoEvent(){
		manager.addUndoEvent(addEvent());
	}
	
	private void addRedoEvent(){
		manager.addRedoEvent(addEvent());
	}
	
	private EventFrame addEvent(){
		EventFrame f = new EventFrame();
		TextMapElement t = list.getCurrent();
		Event e = new ModelEvent(EventTypes.Update, t.n, vi.getStartIndex(), list.getCurrentIndex(), t.start, 
				t.brailleList.getFirst().start, tree.getItemPath());
		f.addEvent(e);
		
		return f;
	}
	
	public void undoEdit(EventFrame f){
		EventFrame frame = recreateEditEvent(f);
		manager.addRedoEvent(frame);
	}
	
	public void redoEdit(EventFrame f){
		EventFrame frame = recreateEditEvent(f);
		manager.addUndoEvent(frame);
	}
	
	private EventFrame recreateEditEvent(EventFrame f){
		EventFrame frame = new EventFrame();
		while(!f.empty() && f.peek().getEventType().equals(EventTypes.Edit)){
			ViewEvent ev = (ViewEvent)f.pop();
			
			if(ev.getTextOffset() >= text.getCurrentStart() && ev.getTextOffset() <= text.getCurrentEnd())
				text.view.setCaretOffset(ev.getTextOffset());	
			else
				text.setCurrentElement(ev.getTextOffset());
			
			int start = ev.getTextOffset();
			int end =  ev.getTextOffset() + ev.getText().length();
			String replacedText = text.view.getTextRange(ev.getTextOffset(), ev.getTextEnd() - ev.getTextOffset());
		
			frame.addEvent(new ViewEvent(EventTypes.Edit, start, end, 0,0, replacedText));
			text.undoEdit(ev.getTextOffset(), ev.getTextEnd() - ev.getTextOffset(), ev.getText());
		}
		
		return frame;
	}
	
	private boolean emptyNode(String text){
		if(text.equals("") || isWhitespace(text))
			return true;
		
		return false;
	}
	
	/** Checks whether text is entirely whitespace
	 * @param text : text to check
	 * @return true if all whitespace, false if not
	 */
	private boolean isWhitespace(String text){
		if (text.trim().length() > 0) 
			return false;
		
		return true;
	}
}
