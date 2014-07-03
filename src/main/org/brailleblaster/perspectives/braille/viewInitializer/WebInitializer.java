package org.brailleblaster.perspectives.braille.viewInitializer;

import java.util.ArrayList;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.Text;

import org.brailleblaster.document.SemanticFileHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.document.BrailleDocument;
import org.brailleblaster.perspectives.braille.mapping.elements.SectionElement;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.views.tree.BBTree;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;

public class WebInitializer extends ViewInitializer{
	
	public WebInitializer(BrailleDocument doc, TextView text, BrailleView braille, BBTree tree) {
		super(doc, text, braille, tree);
		sectionList = new ArrayList<SectionElement>();
	}

	@Override
	protected void findSections(Manager m, Element e) {
		Elements els = e.getChildElements();
		for(int i = 0; i < els.size(); i++){
			if(els.get(i).getLocalName().equals("head") || els.get(i).getLocalName().equals("body")){
				sectionList.add(new SectionElement(m, els.get(i)));
				if(sectionList.get(sectionList.size() - 1).getList().size() == 0)
					sectionList.remove(sectionList.size() - 1);
			}
			else 
				findSections(m, els.get(i));
		}
	}

	@Override
	public void initializeViews(Manager m) {
		findSections(m, document.getRootElement());
		if(sectionList.size() == 0){
			sectionList.add(new SectionElement(m, document.getRootElement()));
			appendToViews(sectionList.get(0).getList(), 0);
			sectionList.get(0).setInView(true);
			if(sectionList.get(0).getList().size() == 0)
				formatTemplateDocument(m, sectionList.get(0).getList());
		}
		else {
			int i = 0;
			while(i < sectionList.size() && text.view.getCharCount() < CHAR_COUNT){
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
			if(sectionList.get(i).isVisible()){
				viewList.addAll(sectionList.get(i).getList());
				viewList.setCurrent(viewList.indexOf(viewList.getCurrent()));
			}
		}
		
		return viewList;
	}

	//adds or tracks a text node for a blank document when user starts 
	private void formatTemplateDocument(Manager m, MapList list){
		Nodes n = document.query("/*[1]/*[2]");
			
		if(n.get(0).getChildCount() > 0){
			if(n.get(0).getChild(0).getChildCount() == 0)
				((Element)n.get(0).getChild(0)).appendChild(new Text(""));
				
			list.add(new TextMapElement(0, 0, n.get(0).getChild(0).getChild(0)));
		}
		else {
			Element p = new Element("p", document.getRootElement().getNamespaceURI());
			SemanticFileHandler sfh = new SemanticFileHandler(m.getArchiver().getCurrentConfig());
			p.addAttribute(new Attribute("semantics","styles," + sfh.getDefault("p")));	
			p.appendChild(new Text(""));
			((Element)n.get(0)).appendChild(p);
			list.add(new TextMapElement(0, 0, n.get(0).getChild(0).getChild(0)));
		}
	}
}
