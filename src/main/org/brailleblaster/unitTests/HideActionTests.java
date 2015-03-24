package org.brailleblaster.unitTests;

import static org.junit.Assert.*;

import java.io.File;

import org.brailleblaster.BBIni;
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

public class HideActionTests {
	private final String XMLTREE = "XML";
	private final String BOXLINE_TEXT = "----------------------------------------";
	private final String TOP_BOXLINE = "7777777777777777777777777777777777777777";
	private final String BOTTOM_BOXLINE = "gggggggggggggggggggggggggggggggggggggggg";
	private final String BOXLINE_TREEITEM = "sidebar";
	
	protected static SWTBot bot;
	protected SWTBotStyledText textBot, brailleBot;
	protected SWTBotTree treeBot;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		new Thread(new Runnable() {	  
			@Override
	        public void run() {
				String [] args = {"-debug", "HideActionTests.xml"};
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
		File f = new File(BBIni.getTempFilesPath() + BBIni.getFileSep() + "HideActionTests.sem");
		if(f.exists())
			f.delete();
	}
	
	@AfterClass
	public static void after(){
		bot.menu("E&xit").click();
	}
	
	private void pressHide(){
		textBot.pressShortcut(SWT.MOD1 ,'h');
	}
	
	private void resetViewBots(){
		textBot = bot.styledText(0);
		brailleBot = bot.styledText(1);
		
		treeBot = bot.tree(0);
	}
	
	@Test
	public void BasicHide_FirstElement() {
		String expectedText = "--------------------------------------1";
		String expectedBraille="--------------------------------------#a";
		String expectedtreeItem = "pagenum";
		
		String expectedText2 = "Page 2 paragraph";
		String expectedBraille2=",page #b p>agraph";
		String expectedtreeItem2 = "p";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(1, 0);
		pressHide();
		
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultAfterPage);
		assertEquals(expectedBraille, resultBrailleAfterPage);
		assertEquals(expectedtreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultAfterPage = textBot.getTextOnCurrentLine();
		resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText2, resultAfterPage);
		assertEquals(expectedBraille2, resultBrailleAfterPage);
		assertEquals(expectedtreeItem2, resultTreeItem);
	}
	
	@Test
	public void BasicHide_LastElement() {
		String expectedText = "--------------------------------------8";
		String expectedBraille="--------------------------------------#h";
		String expectedtreeItem = "pagenum";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(18, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		pressHide();
		
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultAfterPage);
		assertEquals(expectedBraille, resultBrailleAfterPage);
		assertEquals(expectedtreeItem, resultTreeItem);
	}
	
	@Test
	public void BasicHide_Selection() {
		String expectedText = "--------------------------------------1";
		String expectedBraille="--------------------------------------#a";
		String expectedtreeItem = "pagenum";
		
		String expectedText2 = "Page 2 paragraph";
		String expectedBraille2=",page #b p>agraph";
		String expectedtreeItem2 = "p";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(1, 0);
		textBot.selectRange(1, 0, 25);
		pressHide();
		
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultAfterPage);
		assertEquals(expectedBraille, resultBrailleAfterPage);
		assertEquals(expectedtreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultAfterPage = textBot.getTextOnCurrentLine();
		resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText2, resultAfterPage);
		assertEquals(expectedBraille2, resultBrailleAfterPage);
		assertEquals(expectedtreeItem2, resultTreeItem);
	}
	
	@Test
	public void BasicHide_Selection_MultipleElements() {
		String expectedText = "Page 2 paragraph";
		String expectedBraille=",page #b p>agraph";
		String expectedtreeItem = "p";
		
		String expectedText2 = "Second Paragraph on Page 2";
		String expectedBraille2=",second ,p>agraph on ,page #b";
		String expectedtreeItem2 = "p";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(1, 0);
		textBot.selectRange(1, 0, 28);
		pressHide();
		
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, resultAfterPage);
		assertEquals(expectedBraille, resultBrailleAfterPage);
		assertEquals(expectedtreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		resultAfterPage = textBot.getTextOnCurrentLine();
		resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText2, resultAfterPage);
		assertEquals(expectedBraille2, resultBrailleAfterPage);
		assertEquals(expectedtreeItem2, resultTreeItem);
	}
	
	@Test
	//places the cursor in a boxline and calls hide, since top and bottom are not both selected, nothing should occur
	public void Hide_Single_Boxline(){
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille =",second ,p>agraph on ,page #b";
		String expectedtreeItem = "p";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		pressHide();
		
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(BOXLINE_TREEITEM, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedtreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(BOXLINE_TREEITEM, resultTreeItem);
	}
	
	@Test
	//places the cursor in a boxline and calls hide, since top and bottom are not both selected, nothing should occur
	public void Hide_Single_Heading1(){
		String expectedText = "3rd paragraph on Page 2";
		String expectedBraille ="#crd p>agraph on ,page #b";
		String expectedtreeItem = "p";
		
		String expectedText2 = "4th paragraph on Page 2";
		String expectedBraille2 ="#d? p>agraph on ,page #b";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		pressHide();
		
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedtreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText2, result);
		assertEquals(expectedBraille2, resultBraille);
		assertEquals(expectedtreeItem, resultTreeItem);
	}
	
	@Test
	//places the cursor in a boxline and calls hide, since top and bottom are not both selected, nothing should occur
	public void Hide_Single_Heading1_Selection(){
		String pageBefore = "--------------------------------------1";
		String braillePageBefore = "--------------------------------------#a";
		String pageItem = "pagenum";
		
		String expectedText = "4th paragraph on Page 2";
		String expectedBraille ="#d? p>agraph on ,page #b";
		String expectedtreeItem = "p";
		
		String pageAfter = "--------------------------------------2";
		String braillePageAfter = "--------------------------------------#b";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		textBot.selectRange(3, 0, 50);
		pressHide();
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(pageBefore, result);
		assertEquals(braillePageBefore, resultBraille);
		assertEquals(pageItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedtreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(pageAfter, result);
		assertEquals(braillePageAfter, resultBraille);
		assertEquals(pageItem, resultTreeItem);
	}
	
	@Test
	//places the cursor in a boxline, selects text into the next paragraph and calls hide, since top and bottom are not both selected, nothing should occur
	public void Hide_Boxline_Selection_Invalid(){
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille =",second ,p>agraph on ,page #b";
		String expectedtreeItem = "p";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		textBot.selectRange(4, 0, 45);
		pressHide();
		
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(TOP_BOXLINE, resultBraille);
		assertEquals(BOXLINE_TREEITEM, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedtreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(BOTTOM_BOXLINE, resultBraille);
		assertEquals(BOXLINE_TREEITEM, resultTreeItem);
	}
	
	@Test
	//places the cursor in a boxline, selects text into the bottom boxline and calls hide, all should be removed
	public void Hide_Boxline_Selection_Valid(){
		String expectedText = "3rd paragraph on Page 2";
		String expectedText2 = "4th paragraph on Page 2";
		String expectedBraille ="#crd p>agraph on ,page #b";
		String expectedBraille2 ="#d? p>agraph on ,page #b";
		String expectedtreeItem = "p";	
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		textBot.selectRange(4, 0, 75);
		pressHide();
		
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedtreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText2, result);
		assertEquals(expectedBraille2, resultBraille);
		assertEquals(expectedtreeItem, resultTreeItem);
	}
	
	@Test
	//places the cursor in a boxline and calls hide, since top and bottom are not both selected, nothing should occur
	public void Hide_Multiple_Boxline(){
		String expectedText = "--------------------------------------1";
		String expectedText2 = "--------------------------------------2";
		String expectedBraille ="--------------------------------------#a";
		String expectedBraille2 ="--------------------------------------#b";
		String expectedtreeItem = "pagenum";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(3, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		textBot.selectRange(3, 0, 80);
	
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		TextEditingTests.applyStyle(bot, "boxline");
	
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 3);
		textBot.selectRange(3, 0, 230);
		pressHide();
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedtreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText2, result);
		assertEquals(expectedBraille2, resultBraille);
		assertEquals(expectedtreeItem, resultTreeItem);
	}
	
	@Test
	public void Hide_Before_Heading(){
		String  blankLine = "";
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille = ",second ,p>agraph on ,page #b";
		String expectedText2 = "3rd paragraph on Page 2";
		String expectedBraille2 = "#crd p>agraph on ,page #b";
		String expectedTreeItem = "p";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		
		textBot.navigateTo(3, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		pressHide();
		
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		textBot.navigateTo(3, 0);
		assertEquals(blankLine, textBot.getTextOnLine(3));
		assertEquals(blankLine, brailleBot.getTextOnLine(3));
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		assertEquals(blankLine,  textBot.getTextOnCurrentLine());
		assertEquals(blankLine, brailleBot.getTextOnCurrentLine());
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText2, result);
		assertEquals(expectedBraille2, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	public void Hide_After_Heading(){
		String  blankLine = "";
		String expectedText = "Page 2 paragraph";
		String expectedBraille = ",page #b p>agraph";
		String expectedText2 = "Second Paragraph on Page 2";
		String expectedBraille2 = ",second ,p>agraph on ,page #b";
		String expectedText3 = "4th paragraph on Page 2";
		String expectedBraille3 = "#d? p>agraph on ,page #b";
		String expectedTreeItem = "p";
		TextEditingTests.selectTree(bot, XMLTREE);
		textBot.navigateTo(4, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		
		textBot.navigateTo(7, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		pressHide();
		
		textBot.navigateTo(3, 0);
		String result = textBot.getTextOnCurrentLine();
		String resultBraille = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		assertEquals(blankLine,  textBot.getTextOnCurrentLine());
		assertEquals(blankLine, brailleBot.getTextOnCurrentLine());
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText2, result);
		assertEquals(expectedBraille2, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		assertEquals(blankLine,  textBot.getTextOnCurrentLine());
		assertEquals(blankLine, brailleBot.getTextOnCurrentLine());
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		result = textBot.getTextOnCurrentLine();
		resultBraille = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText3, result);
		assertEquals(expectedBraille3, resultBraille);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
}
