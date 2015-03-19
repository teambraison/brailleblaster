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

public class BoxlineSelectionTests {
	private final String BOXLINE_TEXT = "----------------------------------------";
	private final String FULL_BOX = "========================================";
	private final String TOP_BOXLINE = "7777777777777777777777777777777777777777";
	private final String MIDDLE_BOX = "========================================";
	private final String BOTTOM_BOXLINE = "gggggggggggggggggggggggggggggggggggggggg";
	private final String BLANK_LINE = "";
	
	private final static String XMLTREE = "XML";
	
	protected static SWTBot bot;
	protected SWTBotStyledText textBot, brailleBot;
	protected SWTBotTree treeBot;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		new Thread(new Runnable() {	  
			@Override
	        public void run() {
				String [] args = {"-debug", "BoxLineSelectionTests.xml"};
				Main.main(args);
	        }
		}).start();
		  
		long start = System.currentTimeMillis();
		long end = start + (1000 * 5);
		while(start < end){
			start = System.currentTimeMillis();
		}
		bot = new SWTBot();
		TextEditingTests.selectTree(bot, XMLTREE);
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
	
	@Test
	//positions cursor at the beginning of a boxline, press a letter key, no change should occur
	public void BoxlineStart_Keypress() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.typeText(textBot, "F");
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the beginning of a boxline, attempts to paste text, no change should occur
	public void BoxlineStart_Paste() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.paste(bot, textBot, 2, 0, 0);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the middle of a boxline, attempts to paste text, no change should occur
	public void BoxlineStart_PasteShortcut() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 2, 0, 0);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the middle of a boxline, attempts to delete text, no change should occur
	public void BoxlineStart_Delete() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.pressDelete(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the middle of a boxline, attempts to delete textusing backspace, no change should occur
	public void BoxlineStart_Backspace() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the middle of a boxline, press a letter key, no change should occur
	public void BoxlineMiddle_Keypress() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 32);
		TextEditingTests.typeText(textBot, "F");
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the middle of a boxline, attempts to paste text, no change should occur
	public void BoxlineMiddle_Paste() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 32);
		TextEditingTests.paste(bot, textBot, 2, 10, 0);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the middle of a boxline, attempts to paste text, no change should occur
	public void BoxlineMiddle_PasteShortcut() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 32);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 2, 10, 0);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the middle of a boxline, attempts to paste text, no change should occur
	public void BoxlineMiddle_Delete() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		
		TextEditingTests.navigateTo(textBot, 32);
		TextEditingTests.pressDelete(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	
	@Test
	//positions cursor at the middle of a boxline, attempts to delete text using backspace, no change should occur
	public void BoxlineMiddle_Backspace() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		
		TextEditingTests.navigateTo(textBot, 32);
		TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the end of a boxline, press a letter key, no change should occur
	public void BoxlineEnd_Keypress() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
			
		TextEditingTests.navigateTo(textBot, 53);
		TextEditingTests.typeText(textBot, "F");
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the end of a boxline, attempts to paste text, no change should occur
	public void BoxlineEnd_Paste() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 53);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 2, 40, 0);	
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the end of a boxline, attempts to paste text, no change should occur
	public void BoxlineEnd_Delete() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 53);
		TextEditingTests.pressDelete(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor at the end of a boxline, attempts to delete text using backspace, no change should occur
	public void BoxlineEnd_Backspace() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 53);
		TextEditingTests.pressBackspace(textBot, 2);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects a boxline, attempts to delete text using delete, no change should occur
	public void Boxline_CompleteSelection_Delete() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		textBot.selectRange(2, 0, 40);
		TextEditingTests.pressDelete(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects a boxline, attempts to delete text using delete, no change should occur
	public void Boxline_CompleteSelection_Backspace() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		textBot.selectRange(2, 0, 40);
		TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects a boxline, attempts to delete text using cut, no change should occur
	public void Boxline_CompleteSelection_Cut() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.cut(bot, textBot, 2, 0, 40);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	

	@Test
	//selects a boxline, attempts to delete text using cut shortcut, no change should occur
	public void Boxline_CompleteSelection_CutShortcut() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 2, 0, 40);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using delete, no change should occur
	public void BeforeBoxline_Delete() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 12);
		TextEditingTests.pressDelete(textBot, 1);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void AfterBoxline_Backspace() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 54);
		TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void StartofElement_Boxline_Edit() {
		String expectedText = "P";
		String expectedBraille = ";,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.typeTextInRange(textBot, "P", 1, 0, 52);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void StartofElement_Boxline_Paste() {
		String expectedText = "P";
		String expectedBraille = ";,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 1, 0, 52);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void StartofElement_Boxline_PasteShortcut() {
		String expectedText = "P";
		String expectedBraille = ";,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 52);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void StartofElement_Boxline_Delete() {
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		textBot.selectRange(1, 0, 52);
		TextEditingTests.pressDelete(textBot, 1);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void StartofElement_Boxline_Backspace() {
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		textBot.selectRange(1, 0, 52);
		TextEditingTests.pressBackspace(textBot, 1);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void StartofElement_Boxline_Cut() {
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.cut(bot, textBot, 1, 0, 52);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void StartofElement_Boxline_CutShortcut() {
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 52);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void InsideofElement_Boxline_Edit() {
		String expectedText = "ParaP";
		String expectedBraille = ",p>a,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.typeTextInRange(textBot, "P", 1, 4, 48);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void InsideofElement_Boxline_Paste() {
		String expectedText = "ParaP";
		String expectedBraille = ",p>a,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		//TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 1, 4, 48);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void InsideofElement_Boxline_PasteShortcut() {
		String expectedText = "ParaP";
		String expectedBraille = ",p>a,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		//TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 4, 48);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void InsideofElement_Boxline_Delete() {
		String expectedText = "Para";
		String expectedBraille = ",p>a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		textBot.selectRange(1, 4, 48);
		TextEditingTests.pressDelete(textBot, 1);
		//TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();	
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void InsideofElement_Boxline_Backspace() {
		String expectedText = "Para";
		String expectedBraille = ",p>a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		textBot.selectRange(1, 4, 48);
		TextEditingTests.pressBackspace(textBot, 1);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();	
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void InsideofElement_Boxline_Cut() {
		String expectedText = "Para";
		String expectedBraille = ",p>a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		textBot.selectRange(1, 4, 48);
		TextEditingTests.cut(bot, textBot, 1, 4, 48);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();	
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void InsideofElement_Boxline_CutShortcut() {
		String expectedText = "Para";
		String expectedBraille = ",p>a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		textBot.selectRange(1, 4, 48);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 4, 48);
		
		//TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();	
		
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void EndofElement_Boxline_Edit() {
		String expectedText = "Paragraph 1P";
		String expectedBraille = ",p>a_.graph #a;,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 12);
		textBot.selectRange(1, 11, 41);
		TextEditingTests.typeTextInRange(textBot, "P", 1, 11, 41);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void EndofElement_Boxline_Paste() {
		String expectedText = "Paragraph 1P";
		String expectedBraille = ",p>a_.graph #a;,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 12);
		TextEditingTests.paste(bot, textBot, 1, 11, 41);
		//TextEditingTests.typeTextInRange(textBot, "P", 1, 11, 41);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void EndofElement_Boxline_PasteShortcut() {
		String expectedText = "Paragraph 1P";
		String expectedBraille = ",p>a_.graph #a;,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 12);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 11, 41);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void EndofElement_Boxline_Delete() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 12);
		textBot.selectRange(1, 11, 41);
		TextEditingTests.pressDelete(textBot, 1);
		//TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		//TextEditingTests.typeTextInRange(textBot, "P", 1, 11, 41);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void EndofElement_Boxline_Backspace() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 12);
		textBot.selectRange(1, 11, 41);
		TextEditingTests.pressBackspace(textBot, 1);
		//TextEditingTests.typeTextInRange(textBot, "P", 1, 11, 41);
		//TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void EndofElement_Boxline_Cut() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 12);
		//textBot.selectRange(1, 11, 41);
		//TextEditingTests.pressBackspace(textBot, 1);
		TextEditingTests.cut(bot, textBot, 1, 11, 41);
		//TextEditingTests.typeTextInRange(textBot, "P", 1, 11, 41);
		//TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	@Test
	//positions cursor before a boxline, attempts to delete text using backspace, no change should occur
	public void EndofElement_Boxline_CutShortcut() {
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 12);
		//textBot.selectRange(1, 11, 41);
		//TextEditingTests.pressBackspace(textBot, 1);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 11, 41);
		//TextEditingTests.typeTextInRange(textBot, "P", 1, 11, 41);
		//TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
			
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "p";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
			
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		expectedTreeItem = "sidebar";
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementStart_DoubleBoxline_Edit(){
		String expectedText = "P";
		String expectedBraille = ";,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 107);
		TextEditingTests.typeTextInRange(textBot, "P", 5, 0, 98);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementStart_DoubleBoxline_Paste(){
		String expectedText = "P";
		String expectedBraille = ";,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 107);
		TextEditingTests.copy(textBot, 5, 0, 1);
		TextEditingTests.paste(bot, textBot, 5, 0, 98);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementStart_DoubleBoxline_PasteShortcut(){
		String expectedText = "P";
		String expectedBraille = ";,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 107);
		TextEditingTests.copy(textBot, 5, 0, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 5, 0, 98);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementStart_DoubleBoxline_Delete(){
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 107);
		textBot.selectRange(5, 0, 98);
		TextEditingTests.pressDelete(textBot, 1);
		
		//TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementStart_DoubleBoxline_Backsspace(){
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 107);
		textBot.selectRange(5, 0, 98);
		TextEditingTests.pressBackspace(textBot, 1);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementStart_DoubleBoxline_Cut(){
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 107);
		//textBot.selectRange(5, 0, 98);
		TextEditingTests.cut(bot, textBot, 5, 0, 98);
		//TextEditingTests.pressBackspace(textBot, 1);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementStart_DoubleBoxline_CutShortcut(){
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 107);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 5, 0, 98);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideElement_DoubleBoxline_Edit(){
		String expectedText = "ParaP";
		String expectedBraille = ",p>a,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 111);
		TextEditingTests.typeTextInRange(textBot, "P", 5, 4, 94);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideELement_DoubleBoxline_Paste(){
		String expectedText = "ParaP";
		String expectedBraille = ",p>a,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";;
		
		TextEditingTests.navigateTo(textBot, 111);
		TextEditingTests.copy(textBot, 5, 0, 1);
		TextEditingTests.paste(bot, textBot, 5, 4, 94);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideELement_DoubleBoxline_PasteShortcut(){
		String expectedText = "ParaP";
		String expectedBraille = ",p>a,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 111);
		TextEditingTests.copy(textBot, 5, 0, 1);
		TextEditingTests.paste(bot, textBot, 5, 4, 94);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideElement_DoubleBoxline_Delete(){
		String expectedText = "Para";
		String expectedBraille = ",p>a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 111);
		textBot.selectRange(5, 4, 94);
		TextEditingTests.pressDelete(textBot, 1);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideElement_DoubleBoxline_Backsspace(){
		String expectedText = "Para";
		String expectedBraille = ",p>a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 111);
		textBot.selectRange(5, 4, 94);
		TextEditingTests.pressBackspace(textBot, 1);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideElement_DoubleBoxline_Cut(){
		String expectedText = "Para";
		String expectedBraille = ",p>a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 111);
		//textBot.selectRange(5, 0, 98);
		TextEditingTests.cut(bot, textBot, 5, 4, 94);
		//TextEditingTests.pressBackspace(textBot, 1);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideElement_DoubleBoxline_CutShortcut(){
		String expectedText = "Para";
		String expectedBraille = ",p>a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 111);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 5, 4, 94);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ElementEnd_DoubleBoxline_Edit(){
		String expectedText = "Paragraph 3P";
		String expectedBraille = ",p>agraph #c;,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 117);
		TextEditingTests.typeTextInRange(textBot, "P", 5, 11, 87);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementEnd_DoubleBoxline_Paste(){
		String expectedText = "Paragraph 3P";
		String expectedBraille = ",p>agraph #c;,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 117);
		TextEditingTests.copy(textBot, 5, 0, 1);
		TextEditingTests.paste(bot, textBot, 5, 11, 87);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementEnd_DoubleBoxline_PasteShortcut(){
		String expectedText = "Paragraph 3P";
		String expectedBraille = ",p>agraph #c;,p";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 117);
		TextEditingTests.copy(textBot, 5, 0, 1);
		TextEditingTests.paste(bot, textBot, 5, 11, 87);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ElementEnd_DoubleBoxline_Delete(){
		String expectedText = "Paragraph 3";
		String expectedBraille = ",p>agraph #c";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 117);
		textBot.selectRange(5, 11, 87);
		TextEditingTests.pressDelete(textBot, 1);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementEnd_DoubleBoxline_Backsspace(){
		String expectedText = "Paragraph 3";
		String expectedBraille = ",p>agraph #c";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 117);
		textBot.selectRange(5, 11, 87);
		TextEditingTests.pressBackspace(textBot, 1);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementEnd_DoubleBoxline_Cut(){
		String expectedText = "Paragraph 3";
		String expectedBraille = ",p>agraph #c";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		
		TextEditingTests.navigateTo(textBot, 117);
		//textBot.selectRange(5, 0, 98);
		TextEditingTests.cut(bot, textBot, 5, 11, 87);
		//TextEditingTests.pressBackspace(textBot, 1);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void ELementEnd_DoubleBoxline_CutShortcut(){
		String expectedText = "Paragraph 3";
		String expectedBraille = ",p>agraph #c";
		String expectedTreeItem = "p";
		String textAfterBoxline = "graph 4";
		String brailleAfterBoxline="graph #d";
		
		TextEditingTests.navigateTo(textBot, 117);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 5, 11, 87);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 3);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineStart__Selection_Edit(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.typeTextInRange(textBot, "P", 2, 0, 41);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineStart_Selection_Paste(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 2, 0, 41);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineStart_Selection_PasteShortcut(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 2, 0, 41);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineStart_Selection_Delete(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		textBot.selectRange(2, 0, 41);
		TextEditingTests.pressDelete(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineStart_Selection_Backsspace(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		textBot.selectRange(2, 0, 41);
		TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineStart_Selection_Cut(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		//textBot.selectRange(5, 0, 98);
		TextEditingTests.cut(bot, textBot, 2, 0, 41);
		//TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineStart_Selection_CutShortcut(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 2, 0, 41);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideBoxline__Selection_Edit(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.typeTextInRange(textBot, "P", 2, 4, 37);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideBoxline_Selection_Paste(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 2, 4, 37);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideBoxline_Selection_PasteShortcut(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 2, 4, 37);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideBoxline_Selection_Delete(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		textBot.selectRange(2, 4, 37);
		TextEditingTests.pressDelete(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideBoxline_Selection_Backsspace(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		textBot.selectRange(2, 4, 37);
		TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideBoxline_Selection_Cut(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		//textBot.selectRange(5, 0, 98);
		TextEditingTests.cut(bot, textBot, 2, 4, 37);
		//TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void InsideBoxline_Selection_CutShortcut(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 2, 4, 37);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineEnd__Selection_Edit(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.typeTextInRange(textBot, "P", 2, 40, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineEnd_Selection_Paste(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 2, 40, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineEnd_Selection_PasteShortcut(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 2, 40, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineEnd_Selection_Delete(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		textBot.selectRange(2, 40, 1);
		TextEditingTests.pressDelete(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineEnd_Selection_Backsspace(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		textBot.selectRange(2, 40, 1);
		TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineEnd_Selection_Cut(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		//textBot.selectRange(5, 0, 98);
		TextEditingTests.cut(bot, textBot, 2, 40, 1);
		//TextEditingTests.pressBackspace(textBot, 1);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//begins in a paragraph and tests that consecutive boxlines are not altered
	public void BoxlineEnd_Selection_CutShortcut(){
		String expectedText = "Paragraph 1";
		String expectedBraille = ",p>a_.graph #a";
		String expectedTreeItem = "p";
		String textAfterBoxline = "Paragraph 2";
		String brailleAfterBoxline=",p>agraph #b";
		
		TextEditingTests.navigateTo(textBot, 13);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 2, 40, 1);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(textAfterBoxline, resultText);
		assertEquals(brailleAfterBoxline, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void replaceAll_Edit(){
		String expectedText = "P";
		String expectedBraille = ";,p";
		String expectedTreeItem = "p";
	//	String textAfterBoxline = "";
	//	String brailleAfterBoxline="";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.typeText(textBot, "P");
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		///TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		//resultText = textBot.getTextOnCurrentLine();
		//resultBraille = brailleBot.getTextOnCurrentLine();
		//resultTreeItem = treeBot.selection().get(0, 0).toString();
		//assertEquals(textAfterBoxline, resultText);
		//assertEquals(brailleAfterBoxline, resultBraille);
		//assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(MIDDLE_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(BLANK_LINE, resultText);
//		assertEquals(BLANK_LINE, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
	}
	
	@Test
	public void replaceAll_Paste(){
		String expectedText = "P";
		String expectedBraille = ";,p";
		String expectedTreeItem = "p";
	//	String textAfterBoxline = "";
	//	String brailleAfterBoxline="";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.paste(bot, textBot, 0, 0, 400);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(MIDDLE_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
	}
	
	@Test
	public void replaceAll_PasteShortcut(){
		String expectedText = "P";
		String expectedBraille = ";,p";
		String expectedTreeItem = "p";
	//	String textAfterBoxline = "";
	//	String brailleAfterBoxline="";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 400);
		
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(MIDDLE_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
			
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(textAfterBoxline, resultText);
	//	assertEquals(brailleAfterBoxline, resultBraille);
	//	assertEquals("p", resultTreeItem);
	}
	
	@Test
	public void replaceAll_Delete(){
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.pressDelete(textBot, 1);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 14);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expectedText, resultText);
	//	assertEquals(expectedBraille, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expectedText, resultText);
	//	assertEquals(expectedBraille, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expectedText, resultText);
	//	assertEquals(expectedBraille, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(MIDDLE_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expectedText, resultText);
	//	assertEquals(expectedBraille, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expectedText, resultText);
	//	assertEquals(expectedBraille, resultBraille);
	//	assertEquals("p", resultTreeItem);
	}
	
	@Test
	public void replaceAll_Backspace(){
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.pressBackspace(textBot, 1);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 14);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
	//	assertEquals(expectedBraille, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(MIDDLE_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
			
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expectedText, resultText);
	//	assertEquals(expectedBraille, resultBraille);
	//	assertEquals("p", resultTreeItem);
	}
	
	@Test
	public void replaceAll_Cut(){
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cut(bot, textBot, 0, 0, 400);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 14);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	resultText = textBot.getTextOnCurrentLine();
	//	resultBraille = brailleBot.getTextOnCurrentLine();
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expectedText, resultText);
	//	assertEquals(expectedBraille, resultBraille);
	//	assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(MIDDLE_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);		

//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
	}
	
	@Test
	public void replaceAll_CutShortuct(){
		String expectedText = "";
		String expectedBraille = "";
		String expectedTreeItem = "p";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 400);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 14);
		String resultText = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(FULL_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(MIDDLE_BOX, resultBraille);
		assertEquals("sidebar", resultTreeItem);
			
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultText = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, resultText);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals("sidebar", resultTreeItem);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		resultText = textBot.getTextOnCurrentLine();
//		resultBraille = brailleBot.getTextOnCurrentLine();
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedText, resultText);
//		assertEquals(expectedBraille, resultBraille);
//		assertEquals("p", resultTreeItem);
	}
}