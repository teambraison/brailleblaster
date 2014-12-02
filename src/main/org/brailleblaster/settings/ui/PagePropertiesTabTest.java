package org.brailleblaster.settings.ui;
import static org.junit.Assert.*;

import java.util.HashMap;

import org.brailleblaster.settings.SettingsManager;
import org.eclipse.swt.widgets.TabFolder;
import org.junit.Test;


public class PagePropertiesTabTest {
	@Test
	public static void main(String[]args) {
		PagePropertiesTab PPT = new PagePropertiesTab();
		PPT.calcWidthFromCells(10);
		assertEquals(62.5,PPT.calcWidthFromCells(10));
	
		
	}
}
