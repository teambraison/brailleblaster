// Chuck M. - Image helper functions, such as scaled images.

/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2010, 2012
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
 * All rights reserved
 *
 * This file may contain code borrowed from files produced by various 
 * Java development teams. These are gratefully acknoledged.
 *
 * This file is free software; you can redistribute it and/or modify it
 * under the terms of the Apache 2.0 License, as given at
 * http://www.apache.org/licenses/
 *
 * This file is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
 * See the Apache 2.0 License for more details.
 *
 * You should have received a copy of the Apache 2.0 License along with 
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
 */

package org.brailleblaster.util;

import org.eclipse.swt.graphics.Image;

public class ImageHelper {
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Creates an image from another to fit a particular resolution, without 
	// changing the aspect ratio. Scales up or down to match values given.
	public Image createScaledImage(Image img, int maxWidth, int maxHeight)
	{
		// Calc percentage needed to match max width.
		float percW = ((maxWidth * 100) / img.getImageData().width) / 100.0f;
		
		// Store new dimensions.
		int newWidth = maxWidth;
		int newHeight = (int)(img.getImageData().height * percW);
		
		// Calculate new dimensions based on height.
		if(newHeight > maxHeight)
		{
			// Calc percentage needed to match max height.
			float percH = ((maxHeight * 100) / img.getImageData().height) / 100.0f;
			
			// Store new dimensions.
			newWidth = (int)(img.getImageData().width * percH);
			newHeight = maxHeight;
		
		} // if(newHeight > maxHeight)
		
		// Return a new, scaled image.
		return new Image( null, img.getImageData().scaledTo(newWidth, newHeight) );
	
	} // createScaledImage()

} // public class ImageHelper {