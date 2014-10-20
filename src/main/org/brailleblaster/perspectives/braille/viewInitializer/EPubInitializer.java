package org.brailleblaster.perspectives.braille.viewInitializer;

import java.util.ArrayList;

import nu.xom.Comment;
import nu.xom.Element;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.elements.SectionElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class EPubInitializer extends ViewInitializer{
	
	public EPubInitializer(BrailleDocument doc, TextView text, BrailleView braille, BBTree tree){
		super(doc, text, braille, tree);
		sectionList = new ArrayList<SectionElement>();
	}

	@Override
	protected void findSections(Manager m, Element e) {
		int size = e.getChildCount();
		for(int i = 0; i < size; i++){
			if(e.getChild(i) instanceof Comment){
				Comment c = (Comment)e.getChild(i);
				if(c.getValue().contains("BBBOOKMARK")){
					sectionList.add(new SectionElement(m, e, e.indexOf(e.getChild(i + 1))));
					i++;
					if(sectionList.get(sectionList.size() - 1).getList().size() == 0)
						sectionList.remove(sectionList.size() - 1);
				}
			}
			else if(e.getChild(i) instanceof Element)
				findSections(m, (Element)e.getChild(i));
		}
	}

	@Override
	public void initializeViews(Manager m) {
		braille.resetIndicator();
		findSections(m, document.getRootElement());
		if(sectionList.size() == 0){
			sectionList.add(new SectionElement(m, document.getRootElement()));
			appendToViews(sectionList.get(0).getList(), 0);
			sectionList.get(0).setInView(true);
		}
		else {
			int i = 0;
			while(i < sectionList.size() && (text.view.getCharCount() < CHAR_COUNT || i < 2)){
				appendToViews(sectionList.get(i).getList(), 0);
				sectionList.get(i).setInView(true);
				i++;
			}
		}
	}

	@Override
	public MapList getList(Manager m) {
		if(sectionList.size() > 0 && sectionList.get(0).getList() != null)
			return makeList(m);
		else
			return 	new MapList(m);
	}
	
	private MapList makeList(Manager m){
		viewList = new MapList(m);
		for(int i = 0; i < sectionList.size(); i++){
			if(sectionList.get(i).isVisible())
				viewList.addAll(sectionList.get(i).getList());
		}
		
		return viewList;
	}
}
