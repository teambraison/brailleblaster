package org.brailleblaster.unitTests;

import static org.junit.Assert.*;

import org.brailleblaster.Main;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BoxlineTests {
	//private final String BLANK_LINE = "";
	private final String BOXLINE_TEXT = "----------------------------------------";
	private final String TOP_BOXLINE = "7777777777777777777777777777777777777777";
	private final String FULL_BOX = "========================================";
	private final String BOTTOM_BOXLINE = "gggggggggggggggggggggggggggggggggggggggg";
	private final String BOOKTREE = "Book";
	private final String XMLTREE = "XML";
	
	protected static SWTBot bot;
	protected SWTBotStyledText textBot, brailleBot;
	protected SWTBotTree treeBot;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new Thread(new Runnable() {	  
			@Override
	        public void run() {
				String [] args = {"-debug", "BoxLineTests.xml"};
				Main.main(args);
	        }
		}).start();
		  
		long start = System.currentTimeMillis();
		long end = start + (1000 * 5);
		while(start < end){
			start = System.currentTimeMillis();
		}
		bot = new SWTBot();
	}
	
	@Before
	public void setUp() throws Exception {
		bot.menu("&Open").click();
		resetViewBots();
	}
	
	@After
	public void tearDown() throws Exception {
		bot.menu("&Close").click();
	}
	
	@AfterClass
	public static void after(){
		bot.menu("E&xit").click();
	}
	
	private void resetViewBots(){
		textBot = bot.styledText(0);
		brailleBot = bot.styledText(1);
		treeBot = bot.tree(0);
	}
	
	/**Tests adding a boxline to a single paragraph surround by two other paragraphs with Book Tree
	 * 
	 */
	@Test
	public void basicBoxline_BookTree(){
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille = ",second ,p>agraph on ,page #b";
		TextEditingTests.selectTree(bot, BOOKTREE);
		textBot.navigateTo(4,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(4), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(5), expectedText);
		assertEquals(textBot.getTextOnLine(6), BOXLINE_TEXT);
		
		assertEquals(brailleBot.getTextOnLine(4), TOP_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(5), expectedBraille);
		assertEquals(brailleBot.getTextOnLine(6), BOTTOM_BOXLINE);
	}
	
	
	/**Tests adding a boxline to a single paragraph surround by two other paragraphs with XMLTree
	 * 
	 */
	@Test
	public void basicBoxline_BookTree_XML(){
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille = ",second ,p>agraph on ,page #b";
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(4), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(5), expectedText);
		assertEquals(textBot.getTextOnLine(6), BOXLINE_TEXT);
		
		assertEquals(brailleBot.getTextOnLine(4), TOP_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(5), expectedBraille);
		assertEquals(brailleBot.getTextOnLine(6), BOTTOM_BOXLINE);
	}

	/*
	/**Adds a simple boxline to the first element at the start of a document
	 * 
	 */
	@Test
	public void basicBoxline_startOfDocument_BOOKTREE(){
		String expectedText = "First paragraph on page 1";
		String expectedBraille = ",f/ p>_.agraph on page #a";
		TextEditingTests.selectTree(bot, BOOKTREE);
		textBot.navigateTo(1,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(1), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(2), expectedText);
		assertEquals(textBot.getTextOnLine(3), BOXLINE_TEXT);
		
		assertEquals(brailleBot.getTextOnLine(1), TOP_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(2), expectedBraille);
		assertEquals(brailleBot.getTextOnLine(3), BOTTOM_BOXLINE);
	}
	
	/*
	/**Adds a simple boxline to the first element at the start of a document
	 * 
	 */
	@Test
	public void basicBoxline_startOfDocument_XMLTREE(){
		String expectedText = "First paragraph on page 1";
		String expectedBraille = ",f/ p>_.agraph on page #a";
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(1,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(1), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(2), expectedText);
		assertEquals(textBot.getTextOnLine(3), BOXLINE_TEXT);
		
		assertEquals(brailleBot.getTextOnLine(1), TOP_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(2), expectedBraille);
		assertEquals(brailleBot.getTextOnLine(3), BOTTOM_BOXLINE);
	}
	
	/**Adds a basic boxline to an element at the end of a document
	 * 
	 */
	@Test
	public void basicBoxline_endtOfDocument_BOOKTREE(){
		String expectedText = "Final Paragraph";
		String expectedBraille = ",f9al ,p>agraph";
		TextEditingTests.selectTree(bot, BOOKTREE);
		textBot.navigateTo(15,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(15), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(16), expectedText);
		assertEquals(textBot.getTextOnLine(17), BOXLINE_TEXT);
		
		assertEquals(brailleBot.getTextOnLine(15), TOP_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(16), expectedBraille);
		assertEquals(brailleBot.getTextOnLine(17), BOTTOM_BOXLINE);
	}
	
	/**Adds a basic boxline to an element at the end of a document
	 * 
	 */
	@Test
	public void basicBoxline_endtOfDocument_XMLTREE(){
		String expectedText = "Final Paragraph";
		String expectedBraille = ",f9al ,p>agraph";
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(15,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(15), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(16), expectedText);
		assertEquals(textBot.getTextOnLine(17), BOXLINE_TEXT);
		
		assertEquals(brailleBot.getTextOnLine(15), TOP_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(16), expectedBraille);
		assertEquals(brailleBot.getTextOnLine(17), BOTTOM_BOXLINE);
	}
	
	/**Adds a boxline, then tests adding another boxline within the boxline
	 * 
	 */
	@Test
	public void boxlineInBoxline_BOOKTREE(){
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille = ",second ,p>agraph on ,page #b";
		TextEditingTests.selectTree(bot, BOOKTREE);
		textBot.navigateTo(4,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		textBot.navigateTo(5,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(4), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(5), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(6), expectedText);
		assertEquals(textBot.getTextOnLine(7), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(8), BOXLINE_TEXT);
		
		assertEquals(brailleBot.getTextOnLine(4), FULL_BOX);
		assertEquals(brailleBot.getTextOnLine(5), TOP_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(6), expectedBraille);
		assertEquals(brailleBot.getTextOnLine(7), BOTTOM_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(8), FULL_BOX);
	}
	
	/**Adds a boxline, then tests adding another boxline within the boxline
	 * 
	 */
	@Test
	public void boxlineInBoxline_XMLTREE(){
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille = ",second ,p>agraph on ,page #b";
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		textBot.navigateTo(5,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(4), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(5), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(6), expectedText);
		assertEquals(textBot.getTextOnLine(7), BOXLINE_TEXT);
		assertEquals(textBot.getTextOnLine(8), BOXLINE_TEXT);
		
		assertEquals(brailleBot.getTextOnLine(4), FULL_BOX);
		assertEquals(brailleBot.getTextOnLine(5), TOP_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(6), expectedBraille);
		assertEquals(brailleBot.getTextOnLine(7), BOTTOM_BOXLINE);
		assertEquals(brailleBot.getTextOnLine(8), FULL_BOX);
	}
	
	/**Selects three paragraphs, then adds a boxline around them as a group
	 * 
	 */
	@Test
	public void multipleSelectionBoline_BOOKTREE(){
		String expectedText = "Page 2 paragraph";
		String expectedBraille = ",page #b p>agraph";
		String expectedText2 = "Second Paragraph on Page 2";
		String expectedBraille2 = ",second ,p>agraph on ,page #b";
		String expectedText3 = "3rd paragraph on Page 2";
		String expectedBraille3 = "#crd p>agraph on ,page #b";
		TextEditingTests.selectTree(bot, BOOKTREE);
		textBot.navigateTo(3, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 2);
		textBot.selectRange(3, 2, 45);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		assertEquals(textBot.getTextOnLine(3), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(3), TOP_BOXLINE);
		
		assertEquals(textBot.getTextOnLine(4), expectedText);
		assertEquals(brailleBot.getTextOnLine(4), expectedBraille);
		
		assertEquals(textBot.getTextOnLine(5), expectedText2);
		assertEquals(brailleBot.getTextOnLine(5), expectedBraille2);
		
		assertEquals(textBot.getTextOnLine(6), expectedText3);
		assertEquals(brailleBot.getTextOnLine(6), expectedBraille3);
		
		assertEquals(textBot.getTextOnLine(7), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(7), BOTTOM_BOXLINE);
	}
	
	/**Selects three paragraphs, then adds a boxline around them as a group
	 * 
	 */
	@Test
	public void multipleSelectionBoline_XMLTREE(){
		String expectedText = "Page 2 paragraph";
		String expectedBraille = ",page #b p>agraph";
		String expectedText2 = "Second Paragraph on Page 2";
		String expectedBraille2 = ",second ,p>agraph on ,page #b";
		String expectedText3 = "3rd paragraph on Page 2";
		String expectedBraille3 = "#crd p>agraph on ,page #b";
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(3, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 2);
		textBot.selectRange(3, 2, 45);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		assertEquals(textBot.getTextOnLine(3), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(3), TOP_BOXLINE);
		
		assertEquals(textBot.getTextOnLine(4), expectedText);
		assertEquals(brailleBot.getTextOnLine(4), expectedBraille);
		
		assertEquals(textBot.getTextOnLine(5), expectedText2);
		assertEquals(brailleBot.getTextOnLine(5), expectedBraille2);
		
		assertEquals(textBot.getTextOnLine(6), expectedText3);
		assertEquals(brailleBot.getTextOnLine(6), expectedBraille3);
		
		assertEquals(textBot.getTextOnLine(7), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(7), BOTTOM_BOXLINE);
	}
	
	/** Adds a boxline around a paragraph, then selects the paragraph before the boxline, the top boxline, and the paragraph inside the boxline.
	 * This test checks that the second boxline is not added since it only selects the top boxline.
	 */
	@Test
	public void invalidBoxLine_BOOKTREE(){
		String expectedText1 = "Page 2 paragraph";
		String expectedBraille1 = ",page #b p>agraph";
		String expectedText2 = "Second Paragraph on Page 2";
		String expectedBraille2 = ",second ,p>agraph on ,page #b";
		TextEditingTests.selectTree(bot, BOOKTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		TextEditingTests.pressKey(textBot, SWT.ARROW_LEFT, 10);
		textBot.selectRange(3, 2, 60);
		
		TextEditingTests.applyStyle(bot, "boxline");
		assertEquals(textBot.getTextOnLine(3), expectedText1);
		assertEquals(brailleBot.getTextOnLine(3), expectedBraille1);
		
		assertEquals(textBot.getTextOnLine(4), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(4), TOP_BOXLINE);
		
		assertEquals(textBot.getTextOnLine(5), expectedText2);
		assertEquals(brailleBot.getTextOnLine(5), expectedBraille2);
		
		assertEquals(textBot.getTextOnLine(6), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(6), BOTTOM_BOXLINE);
	}
	
	/** Adds a boxline around a paragraph, then selects the paragraph before the boxline, the top boxline, and the paragraph inside the boxline.
	 * This test checks that the second boxline is not added since it only selects the top boxline.
	 */
	@Test
	public void invalidBoxLine_XMLTREE(){
		String expectedText1 = "Page 2 paragraph";
		String expectedBraille1 = ",page #b p>agraph";
		String expectedText2 = "Second Paragraph on Page 2";
		String expectedBraille2 = ",second ,p>agraph on ,page #b";
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		TextEditingTests.pressKey(textBot, SWT.ARROW_LEFT, 10);
		textBot.selectRange(3, 2, 60);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(3), expectedText1);
		assertEquals(brailleBot.getTextOnLine(3), expectedBraille1);
		
		assertEquals(textBot.getTextOnLine(4), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(4), TOP_BOXLINE);
		
		assertEquals(textBot.getTextOnLine(5), expectedText2);
		assertEquals(brailleBot.getTextOnLine(5), expectedBraille2);
		
		assertEquals(textBot.getTextOnLine(6), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(6), BOTTOM_BOXLINE);
	}
	
	@Test
	public void boxlineInBoxline_BookTree(){
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille = ",second ,p>agraph on ,page #b";
		TextEditingTests.selectTree(bot, BOOKTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(4), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(4), FULL_BOX);
		
		assertEquals(textBot.getTextOnLine(5), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(5), TOP_BOXLINE);
		
		assertEquals(textBot.getTextOnLine(6), expectedText);
		assertEquals(brailleBot.getTextOnLine(6), expectedBraille);
		
		assertEquals(textBot.getTextOnLine(7), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(7), BOTTOM_BOXLINE);
		
		assertEquals(textBot.getTextOnLine(8), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(8), FULL_BOX);
	}
	
	@Test
	public void lastElement_XMLTree(){
		String line1 = "last element in view";
		String brailleLine = "la/ ele;t 9 view";
		TextEditingTests.selectTree(bot, XMLTREE);
		TextEditingTests.navigateTo(textBot, 525);
		textBot.navigateTo(18, 0);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(18), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(18), TOP_BOXLINE);
		
		assertEquals(textBot.getTextOnLine(19), line1);
		assertEquals(brailleBot.getTextOnLine(19), brailleLine);
		
		assertEquals(textBot.getTextOnLine(20), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(20), BOTTOM_BOXLINE);
		
		textBot.navigateTo(18, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.removeStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(18), line1);
		assertEquals(brailleBot.getTextOnLine(18), brailleLine);
	}
	
	@Test
	public void lastElement_BookTree(){
		String line1 = "last element in view";
		String brailleLine = "la/ ele;t 9 view";
		TextEditingTests.selectTree(bot, BOOKTREE);
		TextEditingTests.navigateTo(textBot, 525);
		textBot.navigateTo(18, 0);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(18), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(18), TOP_BOXLINE);
		
		assertEquals(textBot.getTextOnLine(19), line1);
		assertEquals(brailleBot.getTextOnLine(19), brailleLine);
		
		assertEquals(textBot.getTextOnLine(20), BOXLINE_TEXT);
		assertEquals(brailleBot.getTextOnLine(20), BOTTOM_BOXLINE);	
		
		textBot.navigateTo(18, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.removeStyle(bot, "boxline");
		
		assertEquals(textBot.getTextOnLine(18), line1);
		assertEquals(brailleBot.getTextOnLine(18), brailleLine);
	}
	
	@Test
	//places the cursor ina boxline and attempts to remove it, top and bottom lines should be removed
	public void boxLine_Remove_Selection(){
		String expectedText = "Page 2 paragraph";
		String expectedBraille = ",page #b p>agraph";
		String expectedText2 = "Second Paragraph on Page 2";
		String expectedBraille2 = ",second ,p>agraph on ,page #b";
		String expectedText3 = "3rd paragraph on Page 2";
		String expectedBraille3 = "#crd p>agraph on ,page #b";
		//String expectedTreeItem = "p";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		
		TextEditingTests.applyStyle(bot, "boxline");
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);	
		textBot.selectRange(4, 0, 75);
		TextEditingTests.removeStyle(bot, "boxline");
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
	//	String resultTreeItem = treeBot.selection().get(0, 0).toString();
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText2, result);
		assertEquals(expectedBraille2, resultBraille);
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText3, result);
		assertEquals(expectedBraille3, resultBraille);
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//places the cursor ina boxline and attempts to remove it, top and bottom lines should be removed
	public void boxLine_Remove_Multiple_Selection(){
		String expectedText = "Page 2 paragraph";
		String expectedBraille = ",page #b p>agraph";
		String expectedText2 = "Second Paragraph on Page 2";
		String expectedBraille2 = ",second ,p>agraph on ,page #b";
		String expectedText3 = "3rd paragraph on Page 2";
		String expectedBraille3 = "#crd p>agraph on ,page #b";
		//String expectedTreeItem = "p";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		
		TextEditingTests.applyStyle(bot, "boxline");
		TextEditingTests.applyStyle(bot, "boxline");
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);	
		
		textBot.selectRange(4, 0, 170);
		TextEditingTests.removeStyle(bot, "boxline");
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
				
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
	//	String resultTreeItem = treeBot.selection().get(0, 0).toString();
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText2, result);
		assertEquals(expectedBraille2, resultBraille);
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText3, result);
		assertEquals(expectedBraille3, resultBraille);
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//places the cursor ina boxline and attempts to remove it, top and bottom lines should be removed
	public void boxLine_Remove_Multiple_Selection2(){
		String expectedText = "Page 2 paragraph";
		String expectedBraille = ",page #b p>agraph";
		String expectedText2 = "Second Paragraph on Page 2";
		String expectedBraille2 = ",second ,p>agraph on ,page #b";
		String expectedText3 = "3rd paragraph on Page 2";
		String expectedBraille3 = "#crd p>agraph on ,page #b";
		//String expectedTreeItem = "p";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		
		TextEditingTests.applyStyle(bot, "boxline");
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);	
		
		textBot.selectRange(3, 0, 140);
		TextEditingTests.applyStyle(bot, "boxline");
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		System.out.println("arrowed up ---------------------------------------");
		textBot.selectRange(3, 0, 200);
		TextEditingTests.removeStyle(bot, "boxline");
				
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
	//	String resultTreeItem = treeBot.selection().get(0, 0).toString();
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText2, result);
		assertEquals(expectedBraille2, resultBraille);
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText3, result);
		assertEquals(expectedBraille3, resultBraille);
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
}
