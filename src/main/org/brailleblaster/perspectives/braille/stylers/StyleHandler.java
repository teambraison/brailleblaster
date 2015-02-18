package org.brailleblaster.perspectives.braille.stylers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Element;

import org.brailleblaster.document.ConfigFileHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.Styles;
import org.brailleblaster.perspectives.braille.document.BBSemanticsTable.StylesType;
import org.brailleblaster.perspectives.braille.eventQueue.Event;
import org.brailleblaster.perspectives.braille.eventQueue.EventFrame;
import org.brailleblaster.perspectives.braille.eventQueue.EventTypes;
import org.brailleblaster.perspectives.braille.eventQueue.ModelEvent;
import org.brailleblaster.perspectives.braille.eventQueue.StyleEvent;
import org.brailleblaster.perspectives.braille.mapping.elements.BrlOnlyMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.PageMapElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.messages.BBEvent;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.messages.Sender;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

public class StyleHandler extends Handler{

	BrailleDocument document;
	BBSemanticsTable semanticsTable;
	String configFile;
	
	EventFrame frame;
	
	public StyleHandler(Manager manager, ViewInitializer vi, MapList list){
		super(manager, vi, list);	
		this.document = manager.getDocument();
		this.semanticsTable = manager.getStyleTable();
		this.configFile = manager.getCurrentConfig();
	}
	
	public void updateStyle(Message message){
		frame = new EventFrame();
		
		if(message.getValue("multiSelect").equals(false)) 
			handleStyleSingleSelected(message);
		else 
			handleStyleMultiSelected(message);
		
		manager.addUndoEvent(frame);
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	}
	
	public void undoStyle(EventFrame f){
		frame = new EventFrame();
		updateStyle(f);
		manager.addRedoEvent(frame);
	}
	
	public void redoStyle(EventFrame f){
		frame = new EventFrame();
		updateStyle(f);
		manager.addUndoEvent(frame);
	}
	
	private void updateStyle(EventFrame f){
		while(!f.empty() && f.peek().getEventType().equals(EventTypes.Style_Change)){
			if(f.peek() instanceof ModelEvent){
				ModelEvent event = (ModelEvent)f.pop();
				list.setCurrent(event.getListIndex());
				manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
			
				Element e = (Element)event.getNode();
				String semantic = e.getAttributeValue(SEMANTICS).split(",")[1];
				Styles style = manager.getStyleTable().get(semantic);
				Message message = Message.createUpdateStyleMessage(style, false, false);
				handleStyleSingleSelected(message);
				tree.rebuildTree(event.getTreeIndex());	
			}
			else {
				StyleEvent ev = (StyleEvent)f.pop(); 
				try {
					Styles oldStyle = semanticsTable.get(ev.getStyle().getName()).clone();
					ConfigFileHandler handler = new ConfigFileHandler(configFile, manager.getWorkingPath());
			    	handler.updateDocumentStyle(ev.getStyle());
			    	semanticsTable.resetStyleTable(configFile, manager.getWorkingPath());
			    	addEditEvent(list.get(ev.getListIndex()), oldStyle);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
		manager.dispatch(Message.createUpdateCursorsMessage(Sender.TREE));
	}
	
	/***
	 * Handle style if user just move cursor
	 * @param message: Message object passed containing information from style table manager
	 */
	private void handleStyleSingleSelected(Message message) {	
		Element parent = parentStyle(list.getCurrent(), message);
		ArrayList<TextMapElement> itemList = list.findTextMapElements(list.getCurrentIndex(), parent, true);
		ModelEvent e = new ModelEvent(EventTypes.Style_Change, parent,  vi.getStartIndex(), list.indexOf(itemList.get(0)), itemList.get(0).start, itemList.get(0).brailleList.getFirst().start, tree.getItemPath());	
		document.changeSemanticAction(message, parent);
		adjustStyle(itemList, message);
		
		addModelEvent(e);
	}

	/***
	 * Apply styles to selected text for multiple elements
	 * @param start: starting offset of highlighted text used to find first block element
	 * @param end: end position of highlighted text used to find last block element
	 * @param message: Message object passed containing information from style table manager
	 */
	private void handleStyleMultiSelected(Message message){
		int start=text.getSelectedText()[0];
		int end=text.getSelectedText()[1];
		
		Set<TextMapElement> itemSet = manager.getElementInSelectedRange(start, end);		
		Iterator<TextMapElement> itr = itemSet.iterator();
		ArrayList<Element>parents = new ArrayList<Element>();
		
		while (itr.hasNext()) {
			TextMapElement tempElement= itr.next();
			if( (!((tempElement instanceof BrlOnlyMapElement) || (tempElement instanceof PageMapElement)))){
				Message styleMessage = Message.createUpdateStyleMessage((Styles)message.getValue("Style"), (Boolean)message.getValue("multiSelect"), (Boolean)message.getValue("isBoxline"));
				Element parent = parentStyle(tempElement, styleMessage);
				parents.add(parent);
				ArrayList<TextMapElement> itemList = list.findTextMapElements(list.getNodeIndex(tempElement), parent, true);
				Event event = new ModelEvent(EventTypes.Style_Change, parent,  vi.getStartIndex(), list.indexOf(itemList.get(0)), itemList.get(0).start, itemList.get(0).brailleList.getFirst().start, tree.getItemPath());	
				document.changeSemanticAction(message, parent);
				adjustStyle( itemList,styleMessage);
				frame.addEvent(event);
			}
		}
		
		text.clearSelection();
	}
	
	private Element parentStyle(TextMapElement current, Message message) {
		Element parent;
		if(current instanceof PageMapElement || current instanceof BrlOnlyMapElement)
			parent = current.parentElement();
		else
			parent = document.getParent(current.n, true);
		
		BBSemanticsTable styles = manager.getStyleTable();
		message.put("previousStyle", styles.get(styles.getKeyFromAttribute(parent)));
		return parent;
	}
	
	/***
	 * Adjust style of elements in the list base on previous and next element 
	 * @param itemList : all selected items which we want style to be applied
	 * @param message : passing information regarding styles
	 */
	private void adjustStyle(ArrayList<TextMapElement> itemList, Message message) {
		int start = list.indexOf(itemList.get(0));
		int end = list.indexOf(itemList.get(itemList.size() - 1));
	
		if (start > 0) {
			message.put("prev", list.get(start - 1).end);
			message.put("braillePrev",
					list.get(start - 1).brailleList.getLast().end);
		} else {
			message.put("prev", -1);
			message.put("braillePrev", -1);
		}

		if (end < list.size() - 1) {
			message.put("next", list.get(end + 1).start);
			message.put("brailleNext",
					list.get(end + 1).brailleList.getFirst().start);
		} else {
			message.put("next", -1);
			message.put("brailleNext", -1);
		}

		text.adjustStyle(message, itemList);
		braille.adjustStyle(message, itemList);

		if (message.contains("linesBeforeOffset"))
			list.shiftOffsetsFromIndex(start,
					(Integer) message.getValue("linesBeforeOffset"),
					(Integer) message.getValue("linesBeforeOffset"));
		if (message.contains("linesAfterOffset") && list.size() > 1
				&& end < list.size() - 1)
			list.shiftOffsetsFromIndex(end + 1,
					(Integer) message.getValue("linesAfterOffset"),
					(Integer) message.getValue("linesAfterOffset"));

		tree.adjustItemStyle(list.getCurrent());
	}	
	
	/*Local Config */
	public void createAndApplyStyle(TextMapElement t, Element e, Message message){
    	try {
    		Styles style = (Styles)semanticsTable.makeStylesElement(e, t.n).clone();
    		Styles oldStyle = style.clone();
        	
    		if(message.type.equals(BBEvent.ADJUST_INDENT)){    		
    			int indent = (Integer)message.getValue("indent");
				style.put(StylesType.firstLineIndent, String.valueOf(indent));
				updateAndApply(oldStyle, style, e, t);
    		}
    		else if(message.type.equals(BBEvent.ADJUST_MARGIN)){
    			int indent = (Integer)message.getValue("margin");
				style.put(StylesType.leftMargin, String.valueOf(indent));
				updateAndApply(oldStyle, style, e, t); 
    		}
    		else if(message.type.equals(BBEvent.ADJUST_ALIGNMENT)){
    			int alignment = (Integer)message.getValue("alignment");
				if(alignment == SWT.RIGHT)
	    			style.put(StylesType.format, "rightJustified");
	    		else if(alignment == SWT.CENTER)
	    			style.put(StylesType.format, "centered");
	    		else
	    			style.put(StylesType.format, "leftJustified");
				
				updateAndApply(oldStyle, style, e, t);
			}	
    		else if(message.type.equals(BBEvent.ADJUST_LINES)){
    			setAlignment(style);
    			int lines = (Integer)message.getValue("lines");
    			if((Boolean)message.getValue("linesBefore") == true)
    				style.put(StylesType.linesBefore, String.valueOf(lines));
    			else 
    				style.put(StylesType.linesAfter, String.valueOf(lines));	
    			
    			updateAndApply(oldStyle, style, e, t);
    		}
    	} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
		}
    }
    
    private void setEmphasis(Styles style){
    	if(style.contains(StylesType.emphasis)){
    		StyleRange emphasis = (StyleRange)style.get(StylesType.emphasis);
    		if(emphasis.fontStyle == SWT.BOLD)
    			style.put(StylesType.emphasis, "boldx");
    		else if(emphasis.fontStyle == SWT.UNDERLINE_SINGLE)
    			style.put(StylesType.emphasis, "underlinex");
    		else if(emphasis.fontStyle == SWT.ITALIC)
    			style.put(StylesType.emphasis, "italicx");
    	}
    }
    
    private void setAlignment(Styles style){
    	if(style.contains(StylesType.format)){
    		int format = Integer.valueOf((String)style.get(StylesType.format));
    		
    		if(format == SWT.RIGHT)
    			style.put(StylesType.format, "rightJustified");
    		else if(format == SWT.CENTER) 
    			style.put(StylesType.format, "centered");
    		else
    			style.put(StylesType.format, "leftJustified");
    	}
    }
    
    private boolean isHiddenStyle(Styles style){
    	return style.getName().contains("local_") ? true : false;
    }
    
    private void setName(Styles style, Element e){
    	Attribute atr = e.getAttribute("id");
    	if(atr == null){
    		manager.getDocument().addID(e);
    		atr = e.getAttribute("id");
    	}
    	String name = "local_" + semanticsTable.getKeyFromAttribute(e) + "_" + atr.getValue();
    	style.setName(name);
    	style.put(StylesType.name, "local_" +style.get(StylesType.name));
    }
    
    private void updateAndApply(Styles oldStyle, Styles style, Element e, TextMapElement t){
    	setEmphasis(style);
    	ConfigFileHandler handler = new ConfigFileHandler(configFile, manager.getWorkingPath());
    	
		if(!isHiddenStyle(style)){
			setName(style, e);	
    		handler.appendDocumentStyle(style);
    		semanticsTable.resetStyleTable(configFile, manager.getWorkingPath());
    		apply(style.getName());
		}
		else{
			handler.updateDocumentStyle(style);
			semanticsTable.resetStyleTable(configFile, manager.getWorkingPath());
			apply(style.getName());
			addStyleEditEvent(t, oldStyle);
		}	
    }
    
    private void apply(String item){
    	Styles style = semanticsTable.get(item);
    	
    	if(style != null){
    		boolean isBoxLine = style.getName().equals("boxline");
    		Message m = Message.createUpdateStyleMessage(style, text.isMultiSelected(), isBoxLine);
    		m.put("Style", style);
    		updateStyle(m);
    	}
    }
    
    private void addModelEvent(ModelEvent e){
    	if(!frame.empty() && frame.peek() instanceof StyleEvent){
    		Event ev = frame.pop();
    		frame.addEvent(e);
    		frame.addEvent(ev);
    	}
    	else
    		frame.addEvent(e);
    }
    private void addStyleEditEvent(TextMapElement t, Styles style){
    	manager.peekUndoEvent().addEvent(new StyleEvent(list.indexOf(t), vi.getStartIndex(), t.start, t.brailleList.getFirst().start, style, configFile));
    }
    
    private void addEditEvent(TextMapElement t, Styles style){
		frame.addEvent(new StyleEvent(list.indexOf(t), vi.getStartIndex(), t.start, t.brailleList.getFirst().start, style, configFile));
    }
}
