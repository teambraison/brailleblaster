package org.brailleblaster.wordprocessor;

import java.io.IOException;

import javax.swing.text.View;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeView extends AbstractView {
	
	static Tree tree;
	
	public TreeView(Shell documentWindow){
		super(documentWindow, 0, 15, 12, 92);
		tree = new Tree(view, SWT.NONE);
		/*
		 * Code below was used for testing purposes only.
		 * It should be deleted once Document class is properly implemented
		try {
			Builder builder = new Builder();
			Document doc = builder.build("File:/Users/broller/Documents/TestProject/build.xml");
			populateTree(doc);
		}
		catch(IOException e){
			System.out.println("IO Error ");
			e.printStackTrace();
		}
		catch (ParsingException e){
			System.out.println("Parse Error");
			e.printStackTrace();
		}
		*/
        
		view.setLayout(new FillLayout());
		tree.pack();
	}
	
	public static  void populateTree(Document doc){
		TreeItem root = new TreeItem(tree, 0);
		root.setText(doc.getRootElement().getLocalName());
			
		Elements rootNode = doc.getRootElement().getChildElements();
		
        for(int i = 0; i < rootNode.size(); i++){
        	Element e = rootNode.get(i);
        	TreeItem temp = new TreeItem(root, 0);
        	temp.setText(e.getLocalName());
        	populateHelper(e, temp);
        }
	}
	
	private static void populateHelper(Element e, TreeItem item){
		Elements n = e.getChildElements();
		
		for(int i = 0; i < n.size(); i++){
			Element e2 = n.get(i);
        	TreeItem temp = new TreeItem(item, 0);
        	temp.setText(e2.getLocalName());
        	populateHelper(e2, temp);
		}
	}
	
	public void clearTree(){
		tree.removeAll();
	}
}
