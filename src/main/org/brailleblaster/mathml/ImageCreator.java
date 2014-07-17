package org.brailleblaster.mathml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.Element;

import org.brailleblaster.BBIni;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.swt.MathRenderer;

public class ImageCreator {
	private final static Logger logger = LoggerFactory.getLogger(ImageCreator.class);
	public static Image createImage(Display d, Element e, int fontHeight){
		MathRenderer mr = MathRenderer.getInstance();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Node n = dBuilder.parse(new InputSource(new StringReader(e.toXML().toString())));
			MutableLayoutContext params = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
			params.setParameter(Parameter.MATHSIZE, fontHeight);
			ImageData imageData = mr.render(n, params);
			return new Image(d, imageData);
		} 
		catch (ParserConfigurationException e1) {
			logger.error("Parser Config Error", e1);
		} 
		catch (SAXException e1) {
			logger.error("Sax Error", e1);
		} 
		catch (IOException e1) {
			logger.error("IOException Error", e1);
		}
		
		return null;
	}
}