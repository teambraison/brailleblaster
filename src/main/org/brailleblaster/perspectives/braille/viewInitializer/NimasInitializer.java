package org.brailleblaster.perspectives.braille.viewInitializer;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Elements;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.elements.SectionElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class NimasInitializer extends ViewInitializer{
	
	public NimasInitializer(BrailleDocument doc, TextView text, BrailleView braille, BBTree tree){
		super(doc, text, braille, tree);
		sectionList = new ArrayList<SectionElement>();
	}
	
	@Override
	protected void findSections(Manager m, Element e){
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			if(els.get(i).getLocalName().equals("level1")){
				sectionList.add(new SectionElement(m, els.get(i)));
			}
			else 
				findSections(m, els.get(i));
		}
	}
	
	@Override
	public void initializeViews(Manager m){
		braille.resetIndicator();
		findSections(m, document.getRootElement());
	//	if(sectionList.size() == 0)
	//		initializeViews(document.getRootElement());
	//	else {
		int i = 0;
		while(i < sectionList.size() && i < 2){
			appendToViews(sectionList.get(i).getList(), 0);
			sectionList.get(i).setInView(true);
			i++;
		}
	//	}
	}
	
	@Override
	public MapList getList(Manager m){
		if(sectionList.size() > 0 && sectionList.get(0).getList() != null)
			return makeList(m);
		else
			return 	new MapList(m);
	}
	
	private MapList makeList(Manager m){
		viewList = new MapList(m);
		for(int i = 0; i < sectionList.size(); i++){
			if(sectionList.get(i).isVisible()){
				viewList.addAll(sectionList.get(i).getList());
				viewList.setCurrent(viewList.indexOf(viewList.getCurrent()));
			}
		}
		
		return viewList;
	}
}
