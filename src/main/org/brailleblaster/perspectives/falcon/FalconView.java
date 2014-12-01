package org.brailleblaster.perspectives.falcon;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.PointerInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;

public class FalconView {
	
	// UI Group.
	Group group;
	
	// The document we pull image paths from.
	FalconDocument doc;
	
	// Falcon Controller.
	FalconController cont;
	
	// Buttons.
	Button setImageTestBtn;
	
	// This image is displayed in the middle of the UI. When we move the 
	// mouse cursor over it, the Falcon will "shake" or "vibrate."
	Label imageLbl;
	
	// This robot helps us with screen capture.
	java.awt.Robot robit = null;
	
	// For tracking screen resolution.
	Monitor mon[] = Display.getDefault().getMonitors();
	Rectangle scr = mon[0].getBounds();
	
	////////////////////////////////////////////////////////////////////////////////////////
	// Constructor.
	public FalconView(Group group, FalconDocument document, FalconController controller)
	{
		// Initialize variables.
		this.group = group;
		this.doc = document;
		this.cont = controller;
		
		// Create image.
		imageLbl = new Label(group, SWT.CENTER | SWT.BORDER);
		imageLbl.setBackground(new Color(null, 255, 150, 150));
		if(doc != null)
			if(doc.getCurImg() != null)
				setImage( doc.getCurImg() );
		setFormData(imageLbl, 0, 40, 6, 90);
		imageLbl.addMouseMoveListener( new MouseMoveListener() 
		{

			@Override
			public void mouseMove(MouseEvent me) {
				
				// Create awt Robot!
				if(robit == null) {
					try { robit = new java.awt.Robot(); }
					catch (AWTException e) { e.printStackTrace(); }
				}
				
				// Get the x and y mouse position.
				PointerInfo pi = MouseInfo.getPointerInfo();
				
				// Get pixel color under mouse.
				java.awt.Color pixClr = robit.getPixelColor(pi.getLocation().x, pi.getLocation().y);
				int red   = pixClr.getRed() & 0x000000FF;
				int green = pixClr.getGreen() & 0x000000FF;
				int blue  = pixClr.getBlue() & 0x000000FF;
				
				// Show it for now.
//				System.out.println(me.x + ", " + red + ", " + green + ", " + blue + "||" + me.y);
				
				// Make the falcon "buzz" if we're on "blackish" pixels.
				if(red < 75 && green < 75 && blue < 75) {
					cont.setForce(0.0f, 0.0f, -5.0f);
					// Make it buzz!
//					cont.setForce(3.0f, 0.0f, 0.0f);
					try { Thread.sleep(100); }
					catch (InterruptedException e) { e.printStackTrace(); }
//					cont.setForce(-3.0f, 0.0f, 0.0f);
//					try { Thread.sleep(10); }
//					catch (InterruptedException e) { e.printStackTrace(); }
//					
//					// Reset to 0 or the Falcon will "lean."
//					cont.setForce(0.0f, 0.0f, 0.0f);
					
				} // if on black pixel...
				else {
					cont.setForce(0.0f, 0.0f, 0.0f);
					try { Thread.sleep(100); }
					catch (InterruptedException e) { e.printStackTrace(); }
				}
				
			} // mouseMove()
		 		
		}); // addMouseMoveListener()
		
		
		// Test setImage()
		setImageTestBtn = new Button(group, SWT.PUSH);
		setImageTestBtn.setText("Test setImage()");
		setFormData(setImageTestBtn, 0, 7, 0, 5);
		setImageTestBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
//				cont.setForce(0.0f, 7.0f, 0.0f);
				if(doc != null)
					if(doc.getCurImgPath() != null) {
						cont.setImage( doc.getCurImgPath() );
						return;
					}
				
				cont.setImage( "Rubber Frickin' Chicken" );
				
			} // widgetSelected()

		}); // setImageTestBtn.addSelectionListener...

		
	} // FalconView() - Constructor
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	// Sets dimensions for widgets/buttons/controls, and resizes fonts if 
	// necessary.
	private void setFormData(Control c, int left, int right, int top, int bottom){
		FormData data = new FormData();
		data.left = new FormAttachment(left);
		data.right = new FormAttachment(right);
		data.top = new FormAttachment(top);
		data.bottom = new FormAttachment(bottom);
		c.setLayoutData(data);
		
		// Change font size depending on screen resolution.
		FontData[] oldFontData = c.getFont().getFontData();
		if( scr.width >= 1920)
			oldFontData[0].setHeight(10);
		else if( scr.width >= 1600)
			oldFontData[0].setHeight(9);
		else if( scr.width >= 1280)
			oldFontData[0].setHeight(7);
		else if( scr.width >= 1024)
			oldFontData[0].setHeight(5);
		else if( scr.width >= 800)
			oldFontData[0].setHeight(4);
		c.setFont( new Font(null, oldFontData[0]) );
		
	} // setFormData()
	
	/////////////////////////////////////////////////////////////////////////////////////
	// Sets the image that our Falcon is analyzing.
	public void setImage(Image img) {
		imageLbl.setImage( img );
	} // setImage()
	
} // class FalconView
