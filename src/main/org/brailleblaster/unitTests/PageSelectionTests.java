package org.brailleblaster.unitTests;

import org.brailleblaster.Main;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.SWTBotTestCase;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class PageSelectionTests extends SWTBotTestCase {
	private final static String XMLTREE = "XML";
	
	protected static SWTBot bot;
	protected SWTBotStyledText textBot, brailleBot;
	protected SWTBotTree treeBot;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		new Thread(new Runnable() {	  
			@Override
	        public void run() {
				String [] args = {"-debug", "PageSelectionTests.xml"};
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
	
	@Test
	//inserts a character at the start of an element and checks that correct offset positions are maintained after the page
	//bot arrows right to the point where it should be at the first page offset
	public void simpleInsertAtStart(){
		String expectedBeforePage = "FFirst paragraph on page 1";
		String expectedBrailleBeforePage = ",,ff/ p>agraph on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		TextEditingTests.navigateTo(textBot, 1);
		textBot.typeText("F");
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 26);
		
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//inserts a character in the middle of an element and checks that correct offset positions are maintained after the page
	//bot arrows right to the point where it should be at the first page offset
	public void simpleInsertMiddle(){
		String expectedBeforePage = "First paragraphs on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraphs on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		TextEditingTests.navigateTo(textBot, 16);
		textBot.typeText("s");
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	
	@Test
	//inserts a character at the end of an element and checks that correct offset positions are maintained after the page
	//bot arrows right to the point where it should be at the first page offset
	public void simpleInsertAtEnd(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.selectTree(bot, XMLTREE);
		TextEditingTests.navigateTo(textBot, 26);
		textBot.typeText("1");
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//deletes first character of an element and checks that correct offset positions are maintained after the page
	//bot arrows right to the point where it should be at the first page offset
	public void simpleDeleteStart(){
		String expectedBeforePage = "irst paragraph on page 1";
		String expectedBrailleBeforePage = "ir/ p>agraph on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 25);
		
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//deletes a character in the middle of an element and checks that correct offset positions are maintained after the page
	//bot arrows right to the point where it should be at the first page offset
	public void simpleInsertDeleteInside(){
		String expectedBeforePage = "First paragraph on age 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on age #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.navigateTo(textBot, 20);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 6);
		
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//deletes a character at the end of an element and checks that correct offset positions are maintained after the page
	//bot arrows right to the point where it should be at the first page offset, which BB should skip, if not it fails
	public void simpleInsertDeleteEnd(){
		String expectedBeforePage = "First paragraph on page ";
		String expectedBrailleBeforePage = ",f/ p>agraph on page ";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.navigateTo(textBot, 25);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element to a point inside the elements and replaces it by hitting an alphanumeric key on the keyboard
	//bot arrows right to the point where it should be at the first page offset
	public void selection_insert_fromStartToMiddle(){
		String expectedBeforePage = "P on page 1";
		String expectedBrailleBeforePage = ";,p on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.typeTextInRange(textBot, "P", 1, 0, 15);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element to a point inside the elements and replaces it using the paste menu item
	//bot arrows right to the point where it should be at the first page offset
	public void selection_insert_fromStartToMiddle_Paste(){
		String expectedBeforePage = "paragraph page 1";
		String expectedBrailleBeforePage = "p>agraph page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.paste(bot, textBot, 1, 0, 18);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 8);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element to a point inside the elements and replaces it using the paste shortcut
	//bot arrows right to the point where it should be at the first page offset
	public void selection_insert_fromStartToMiddle_PasteShortcut(){
		String expectedBeforePage = "paragraph page 1";
		String expectedBrailleBeforePage = "p>agraph page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 18);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 8);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the inside the element to a point at the end of the elements range and replaces it by hitting an alphanumeric key on the keyboard
	//bot arrows right to the point where it should be at the first page offset
	public void selection_insert_fromMiddleToEnd(){
		String expectedBeforePage = "First paragraph 1";
		String expectedBrailleBeforePage = ",f/ p>agraph #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.typeTextInRange(textBot, "1", 1, 16, 9);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the inside the element to a point at the end of the elements range and replaces it by using the paste menu item
	//bot arrows right to the point where it should be at the first page offset
	public void selection_insert_fromMiddleToEnd_Paste(){
		String expectedBeforePage = "First paragraph paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph p>agraph";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.paste(bot, textBot, 1, 16, 9);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the inside the element to a point at the end of the elements range and replaces it by using the paste shortcut
	//bot arrows right to the point where it should be at the first page offset
	public void selection_insert_fromMiddleToEnd_PasteShortcut(){
		String expectedBeforePage = "First paragraph paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph p>agraph";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 16, 9);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of an element to a point at the end of the elements range and replaces it by hitting an alphanumeric key on the keyboard
	//bot arrows right to the point where it should be at the first page offset
	public void selection_insert_fromStartToEnd(){
		String expectedBeforePage = "1";
		String expectedBrailleBeforePage = "#a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.typeTextInRange(textBot, "1", 1, 0, 25);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of an element to a point at the end of the elements range and replaces it using the paste menu item
	//bot arrows right to the point where it should be at the first page offset
	public void selection_insert_fromStartToEnd_Paste(){
		String expectedBeforePage = "paragraph";
		String expectedBrailleBeforePage = "p>agraph";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.paste(bot, textBot, 1, 0, 25);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of an element to a point at the end of the elements range and replaces it using the paste using keyboard shortcut
	//bot arrows right to the point where it should be at the first page offset
	public void selection_insert_fromStartToEnd_PasteShortcut(){
		String expectedBeforePage = "paragraph";
		String expectedBrailleBeforePage = "p>agraph";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 25);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element range to a position inside the range and deletes it by pressing the delete key
	public void selection_delete_fromStartToMiddle(){
		String expectedBeforePage = " on page 1";
		String expectedBrailleBeforePage = "on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.deleteSelection(textBot, 1, 0, 15, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element range to a position inside the range and deletes it by pressing the backspace key
	public void selection_backspace_fromStartToMiddle(){
		String expectedBeforePage = " on page 1";
		String expectedBrailleBeforePage = "on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.deleteSelection(textBot, 1, 0, 15, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element range to a position inside the range and deletes it using the cut menu item
	public void selection_Cut_fromStartToMiddle(){
		String expectedBeforePage = " on page 1";
		String expectedBrailleBeforePage = "on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.cut(bot, textBot, 1, 0, 15);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element range to a position inside the range and deletes it using the cut shortcut
	public void selection_CutShortcut_fromStartToMiddle(){
		String expectedBeforePage = " on page 1";
		String expectedBrailleBeforePage = "on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 15);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from inside the element range to the end of the range and deletes it using the delete key
	public void selection_delete_fromMiddleToEnd(){
		String expectedBeforePage = "First paragraph ";
		String expectedBrailleBeforePage = ",f/ p>agraph ";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.deleteSelection(textBot, 1, 16, 9, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from inside the element range to the end of the range and deletes it using the backspace key
	public void selection_Backspace_fromMiddleToEnd(){
		String expectedBeforePage = "First paragraph ";
		String expectedBrailleBeforePage = ",f/ p>agraph ";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.deleteSelection(textBot, 1, 16, 9, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from inside the element range to the end of the range and deletes it using the cut menu item
	public void selection_Cut_fromMiddleToEnd(){
		String expectedBeforePage = "First paragraph ";
		String expectedBrailleBeforePage = ",f/ p>agraph ";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.cut(bot, textBot, 1, 16, 9);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from inside the element range to the end of the range and deletes it using the cut shortcut
	public void selection_CutShortcut_fromMiddleToEnd(){
		String expectedBeforePage = "First paragraph ";
		String expectedBrailleBeforePage = ",f/ p>agraph ";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 16, 9);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element range to the end of the range and deletes it using the delete key
	public void selection_delete_fromStartToEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.deleteSelection(textBot, 0, 1, 25, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element range to the end of the range and deletes it using the backspace key
	public void selection_Backspace_fromStartToEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.deleteSelection(textBot, 0, 1, 25, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element range to the end of the range and deletes it using the cut menu item
	public void selection_Cut_fromStartToEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.cut(bot, textBot, 0, 1, 25);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects text from the start of the element range to the end of the range and deletes it using the cut shortcut
	public void selection_CutShortcut_fromStartToEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 1, 25);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//types text before the start of an element at the beginning of a document and makes sure the range is adjusted to include the new text
	//TODO revisit later since \n following inserted char and text is not handled correctly on trnslation, this is a seperate issue though if it should occur
	public void beforeElement_Insert_ToStart(){
		String expectedBeforePage = "ZFirst paragraph on page 1";
		String expectedBrailleBeforePage = ",,zf/ p>agraph on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.typeText(textBot, "Z");
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 26);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element at the beginning of a document and makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_ToStart(){
		String expectedBeforePage = "ZFirst paragraph on page 1";
		String expectedBrailleBeforePage = ",,zf/ p>agraph on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.typeTextInRange(textBot, "Z", 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 26);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element and pastes text at the beginning of a document, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_Paste_ToStart(){
		String expectedBeforePage = "paragraphFirst paragraph on page 1";
		String expectedBrailleBeforePage = "p>agraph,f/ p>agraph on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.paste(bot, textBot, 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 28);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element and uses keyboard shortcut to paste text at the beginning of a document, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_PasteShortcut_ToStart(){
		String expectedBeforePage = "paragraphFirst paragraph on page 1";
		String expectedBrailleBeforePage = "p>agraph,f/ p>agraph on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 28);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element to a position inside the range and types a character, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_ToMiddle(){
		String expectedBeforePage = "Z on page 1";
		String expectedBrailleBeforePage = ";,z on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.typeTextInRange(textBot, "Z", 0, 0, 16);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element to a position inside the range and pastes a character, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_Paste_ToMiddle(){
		String expectedBeforePage = "paragraph on page 1";
		String expectedBrailleBeforePage = "p>agraph on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.paste(bot, textBot, 0, 0, 16);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element to a position inside the range and uses the keyboard shortcut to paste a character, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_PasteShortcut_ToMiddle(){
		String expectedBeforePage = "paragraph on page 1";
		String expectedBrailleBeforePage = "p>agraph on page #a";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 16);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element to the end position of an element and types a character, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_ToEnd(){
		String expectedBeforePage = "Z";
		String expectedBrailleBeforePage = ";,z";	
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.typeTextInRange(textBot, "Z", 0, 0, 26);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element to the end position of an element and pastes a character, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_Paste_ToEnd(){
		String expectedBeforePage = "paragraph";
		String expectedBrailleBeforePage = "p>agraph";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.paste(bot, textBot, 0, 0, 26);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element to the end position of an element and uses the keyboard shortcut to paste a character, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_PasteShortcut_ToEnd(){
		String expectedBeforePage = "paragraph";
		String expectedBrailleBeforePage = "p>agraph";
		String expectedAfterPage = "--------------------------------------1";
		String expectedBrailleAfterPage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		
		TextEditingTests.copy(textBot, 1, 6, 9);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 26);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element to the end position of an element and deletes the text, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_Delete_ToEnd(){
		TextEditingTests.selectTree(bot, XMLTREE);	
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.deleteSelection(textBot, 0, 0, 26, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
	
		//check line before page
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);	
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		expectedTreeItem = "p";
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		
		//check line after page
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//selects area before the start of an element to the end position of an element and and deletes the text using the backspace key, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_Backspace_ToEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.deleteSelection(textBot, 0, 0, 26, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//selects area before the start of an element to the end position of an element and and deletes the text using the cut menu item, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_cut_ToEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.cut(bot, textBot, 0, 0, 26);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//selects area before the start of an element to the end position of an element and and deletes the text using the cut keyboard shortcut, then makes sure the range is adjusted to include the new text
	public void beforeElement_Selection_cutShortcut_ToEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 26);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, deletes the line break before and checks that text is correct before and after page
	public void BeforeElement_Backspace(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.pressKey(textBot, SWT.BS, 2);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 28);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//turns the first element into a heading, selects area before and inserts a character, and checks that text before and after a page are correct
	public void BeforeElement_Selection_Insert(){
		String expectedBeforePage = "jFirst paragraph on page 1";
		String expectedBrailleBeforePage = "j,f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.typeTextInRange(textBot, "j", 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 28);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//turns the first element into a heading, selects area before and inserts a character using paste, and checks that text before and after a page are correct
	public void BeforeElement_Selection_Insert_Paste(){
		String expectedBeforePage = "FFirst paragraph on page 1";
		String expectedBrailleBeforePage = ",,ff/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.paste(bot, textBot, 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 28);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);	
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//turns the first element into a heading, selects area before and inserts a character using paste shortcut, and checks that text before and after a page are correct
	public void BeforeElement_Selection_Insert_PasteShortcut(){
		String expectedBeforePage = "FFirst paragraph on page 1";
		String expectedBrailleBeforePage = ",,ff/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 28);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);	
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	public void beforeElement_Selection_Delete_ToNextPage(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 67, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);	
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to before the element on the next page and replaces range with a character
	//should replace element on page before, leave page and second element text unchanged
	public void beforeElement_Selection_Backspace_ToNextPage(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 67, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);	
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	public void beforeElement_Selection_Cut_ToNextPage(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cut(bot, textBot, 0, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);	
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	public void beforeElement_Selection_CutShortcut_ToNextPage(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);	
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to before the element on the next page and replaces range with a character
	//should replace element on page before, leave page and second element text unchanged
	//TODO recheck line indent and possibly alignment of element after page
	public void beforeElement_Selection_Insert_ToNextPage(){
		String expectedBeforePage = "J";
		String expectedBrailleBeforePage = ";,j";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.typeTextInRange(textBot, "J", 0, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);	
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to before the element on the next page and replaces range with a character
	//should replace element on page before, leave page and second element text unchanged
	//TODO recheck line indent and possibly alignment of element after page
	public void beforeElement_Selection_Paste_ToNextPage(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.paste(bot, textBot, 0, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to before the element on the next page and replaces range with a character
	//should replace element on page before, leave page and second element text unchanged
	//TODO recheck line indent and possibly alignment of element after page
	public void beforeElement_Selection_PasteShortcut_ToNextPage(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test 
	//Selects area from before the element to past the first char of the element on the next page and deletes range
	public void beforeElement_Selection_delete_pastFirstChar(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 68, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test 
	//Selects area from before the element to past the first char of the element on the next page and deletes range using backspace
	public void beforeElement_Selection_Backspace_pastFirstChar(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 68, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test 
	//Selects area from before the element to past the first char of the element on the next page and deletes range using cut
	public void beforeElement_Selection_Cut_pastFirstChar(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cut(bot, textBot, 0, 0, 68);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test 
	//Selects area from before the element to past the first char of the element on the next page and deletes range using cut shortcut
	public void beforeElement_Selection_CutShortcut_pastFirstChar(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 68);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from before the element to past the first char of the element on the next page and inserts a char by typing
	//should replace first element and edit second, leaving page between unchanged
	public void beforeElement_Selection_Insert_pastFirstChar(){
		String expectedBeforePage = "J";
		String expectedBrailleBeforePage = ";,j";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.typeTextInRange(textBot, "J", 0, 0, 68);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to past the first char of the element on the next page and inserts a char using paste
	//should replace first element and edit second, leaving page between unchanged
	public void beforeElement_Selection_Paste_pastFirstChar(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.paste(bot, textBot, 0, 0, 68);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to past the first char of the element on the next page and inserts a char using paste shortcut
	//should replace first element and edit second, leaving page between unchanged
	public void beforeElement_Selection_PasteShortcut_pastFirstChar(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 68);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to end of range of the element on the next page and deletes
	//should replace first element and edit second, leaving page between unchanged
	public void beforeElement_Selection_Delete_ToNextPageElementEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 83, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from before the element to end of range of the element on the next page and deletes using backspace
	//should replace first element and edit second, leaving page between unchanged
	public void beforeElement_Selection_Backspace_ToNextPageElementEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 83, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from before the element to end of range of the element on the next page and deletes using cut
	//should replace first element and edit second, leaving page between unchanged
	public void beforeElement_Selection_Cut_ToNextPageElementEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cut(bot, textBot, 0, 0, 83);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from before the element to end of range of the element on the next page and deletes using cut shortcut
	//should replace first element and edit second, leaving page between unchanged
	public void beforeElement_Selection_CutShortcut_ToNextPageElementEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 83);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from before the element to end of range of the element on the next page and inserts a char
	//should replace first element and delete second leaving a blank line, leaving page between unchanged
	public void beforeElement_Selection_Insert_ToNextPageElementEnd(){
		String expectedBeforePage = "J";
		String expectedBrailleBeforePage = ";,j";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.typeTextInRange(textBot, "J", 0, 0, 83);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from before the element to end of range of the element on the next page and inserts a char using paste
	//should replace first element and delete second leaving a blank line, leaving page between unchanged
	public void beforeElement_Selection_Paste_ToNextPageElementEnd(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.paste(bot, textBot, 0, 0, 83);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to end of range of the element on the next page and inserts a char using paste shortcut
	//should replace first element and delete second leaving a blank line, leaving page between unchanged
	public void beforeElement_Selection_PasteShortcut_ToNextPageElementEnd(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 83);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to start of range of the element on the next page an deletes
	//both lines should be blank, leaving page between unchanged
	public void start_Selection_Delete_ToNextPageStart(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.deleteSelection(textBot, 1, 0, 66, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to start of range of the element on the next page an deletes using backspace
	//both lines should be blank, leaving page between unchanged
	public void start_Selection_Backspace_ToNextPageStart(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.deleteSelection(textBot, 1, 0, 66, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to start of range of the element on the next page an deletes using cut
	//both lines should be blank, leaving page between unchanged
	public void start_Selection_Cut_ToNextPageStart(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.cut(bot, textBot, 1, 0, 66);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to start of range of the element on the next page an deletes using cut shortcut
	//both lines should be blank, leaving page between unchanged
	public void start_Selection_CutShortcut_ToNextPageStart(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 66);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to start of range of the element on the next page and inserts a char
	//first line should have a char, other line should be blank, leaving page between unchanged
	//TODO recheck line indent and possibly alignment of element after page
	public void start_Selection_Insert_ToNextPageStart(){
		String expectedBeforePage = "J";
		String expectedBrailleBeforePage = ";,j";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.typeTextInRange(textBot, "J", 1, 0, 66);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to start of range of the element on the next page and pastes a char
	//first line should have a char, other line should be blank, leaving page between unchanged
	public void start_Selection_Paste_ToNextPageStart(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.paste(bot, textBot, 1, 0, 66);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from before the element to start of range of the element on the next page and pastes a char using a shortcut
	//first line should have a char, other line should be blank, leaving page between unchanged
	//TODO recheck line indent and possibly alignment of element after page
	public void start_Selection_PasteShortcut_ToNextPageStart(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 66);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from start of the element to start of range of the element on the next page and deletes
	//first line should have a char, other line should be blank, leaving page between unchanged
	public void start_Selection_Delete_pastFirstChar(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot,1);
		TextEditingTests.deleteSelection(textBot, 1, 0, 67, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from start of the element to start of range of the element on the next page and delets using backspace
	//first line should have a char, other line should be blank, leaving page between unchanged
	public void start_Selection_Backspace_pastFirstChar(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot,1);
		TextEditingTests.deleteSelection(textBot, 1, 0, 67, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from start of the element to start of range of the element on the next page and delets using cut s
	//first line should have a char, other line should be blank, leaving page between unchanged
	public void start_Selection_Cut_pastFirstChar(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot,1);
		TextEditingTests.cut(bot, textBot, 1, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from start of the element to start of range of the element on the next page and delets using cut shortcut
	//first line should have a char, other line should be blank, leaving page between unchanged
	public void start_Selection_CutShortcut_pastFirstChar(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot,1);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	}
	
	@Test
	//Selects area from start of the element to start of range of the element on the next page and inserts a char
	//first line should have a char, other line should be blank, leaving page between unchanged
	public void start_Selection_Insert_pastFirstChar(){
		String expectedBeforePage = "J";
		String expectedBrailleBeforePage = ";,j";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot,1);
		TextEditingTests.typeTextInRange(textBot, "J", 1, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from start of the element to start of range of the element on the next page and inserts a char
	//first line should have a char, other line should be blank, leaving page between unchanged
	public void start_Selection_Paste_pastFirstChar(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot,1);
		TextEditingTests.paste(bot, textBot, 1, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from start of the element to start of range of the element on the next page and inserts a char
	//first line should have a char, other line should be blank, leaving page between unchanged
	public void start_Selection_PasteShortcut_pastFirstChar(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot,1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from start of the element to end of range of the element on the next page and deletes
	public void start_Selection_delete_toNextPageEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 1, 0, 82, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from start of the element to end of range of the element on the next page and deletes using backspace
	public void start_Selection_Backspace_toNextPageEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 1, 0, 82, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from start of the element to end of range of the element on the next page and deletes using cut
	public void start_Selection_Cut_toNextPageEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cut(bot, textBot, 1, 0, 82);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from start of the element to end of range of the element on the next page and deletes using cut shortcut
	public void start_Selection_CutShortcut_toNextPageEnd(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 82);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from start of the element to end of range of the element on the next page and inserts a char
	//first line should have achar, second should be blank, page remains unchanged
	public void start_Selection_Insert_ToNextPageEnd(){
		String expectedBeforePage = "J";
		String expectedBrailleBeforePage = ";,j";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.typeTextInRange(textBot, "J", 1, 0, 82);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from start of the element to end of range of the element on the next page and pastes a char
	//first line should have achar, second should be blank, page remains unchanged
	public void start_Selection_Paste_ToNextPageEnd(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.paste(bot, textBot, 1, 0, 82);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from start of the element to end of range of the element on the next page and pastes a char using shortcut
	//first line should have achar, second should be blank, page remains unchanged
	public void start_Selection_PasteShortcut_ToNextPageEnd(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 82);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from insdie the element to the start of the element on the next page and deletes
	//TODO fix indent issue
	public void insideWord_Selection_Delete_NextPageStart(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.deleteSelection(textBot, 1, 15, 51, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from insdie the element to the start of the element on the next page and deletes using backspace
	//TODO fix indent issue
	public void insideWord_Selection_Backspace_NextPageStart(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.deleteSelection(textBot, 1, 15, 51, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from insdie the element to the start of the element on the next page and deletes using cut
	//TODO fix indent issue
	public void insideWord_Selection_Cut_NextPageStart(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.cut(bot, textBot, 1, 15, 51);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from insdie the element to the start of the element on the next page and deletes using cut using shortcut
	//TODO fix indent issue
	public void insideWord_Selection_CutShortcut_NextPageStart(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 15, 51);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to the start of the element on the next page and inserts a char
	//TODO fix indent issue
	public void insideWord_Selection_Inside_NextPageStart(){
		String expectedBeforePage = "First paragraphs";
		String expectedBrailleBeforePage = ",f/ p>agraphs";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.typeTextInRange(textBot, "s", 1, 15, 51);
		TextEditingTests.forceUpdate(bot, textBot);
		//TextEditingTests.pressKey(textBot, SWT.ARROW_LEFT, 1);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from inside the element to the start of the element on the next page and inserts a char
	//TODO fix indent issue
	public void insideWord_Selection_Paste_NextPageStart(){
		String expectedBeforePage = "First paragraphs";
		String expectedBrailleBeforePage = ",f/ p>agraphs";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 11, 13, 1);
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.paste(bot, textBot, 1, 15, 51);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
	}
	
	@Test
	//Selects area from inside the element to the start of the element on the next page and inserts a char
	//TODO fix indent issue
	public void insideWord_Selection_PasteShortcut_NextPageStart(){
		String expectedBeforePage = "First paragraphs";
		String expectedBrailleBeforePage = ",f/ p>agraphs";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 11, 13, 1);
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 15, 51);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to inside the start of the element on the next page and deletes
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Delete_pastFirstChar(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.deleteSelection(textBot, 1, 15, 52, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to inside the start of the element on the next page and deletes using backspace
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Backspace_pastFirstChar(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.deleteSelection(textBot, 1, 15, 52, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
//		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to inside the start of the element on the next page and deletes using cut
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Cut_pastFirstChar(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.cut(bot, textBot, 1, 15, 52);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to inside the start of the element on the next page and deletes using cut shortcut
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_CutShortcut_pastFirstChar(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 15, 52);
		TextEditingTests.forceUpdate(bot, textBot);
//		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to inside the start of the element on the next page and inserts a char
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Insert_pastFirstChar(){
		String expectedBeforePage = "First paragraphs";
		String expectedBrailleBeforePage = ",f/ p>agraphs";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.typeTextInRange(textBot, "s", 1, 15, 52);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to inside the start of the element on the next page and pastes a char
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Paste_pastFirstChar(){
		String expectedBeforePage = "First paragraphs";
		String expectedBrailleBeforePage = ",f/ p>agraphs";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		TextEditingTests.copy(textBot, 11, 13, 1);
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.paste(bot, textBot, 1, 15, 52);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to inside the start of the element on the next page and pastes a char using the shortcut
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_PasteShortcut_pastFirstChar(){
		String expectedBeforePage = "First paragraphs";
		String expectedBrailleBeforePage = ",f/ p>agraphs";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		TextEditingTests.copy(textBot, 11, 13, 1);
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 15, 52);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to the end of the element on the next page and deletes
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Delete_ToNextPageEnd(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.deleteSelection(textBot, 1, 15, 67, SWT.DEL);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to the end of the element on the next page and deletes using backspace
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Backspace_ToNextPageEnd(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.deleteSelection(textBot, 1, 15, 67, SWT.BS);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to the end of the element on the next page and deletes using cut
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Cut_ToNextPageEnd(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.cut(bot, textBot, 1, 15, 67);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to the end of the element on the next page and deletes using cut shortcut
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_CutShortcut_ToNextPageEnd(){
		String expectedBeforePage = "First paragraph";
		String expectedBrailleBeforePage = ",f/ p>agraph";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 15, 67);
//		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to the end of the element on the next page and inserts a char
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Insert_ToNextPageEnd(){
		String expectedBeforePage = "First paragraphs";
		String expectedBrailleBeforePage = ",f/ p>agraphs";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.typeTextInRange(textBot, "s", 1, 15, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to the end of the element on the next page and pastes a char
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_Paste_ToNextPageEnd(){
		String expectedBeforePage = "First paragraphs";
		String expectedBrailleBeforePage = ",f/ p>agraphs";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		TextEditingTests.copy(textBot, 11, 13, 1);
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.paste(bot, textBot, 1, 15, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from inside the element to the end of the element on the next page and pastes a char using the shortcut
	//Should edit the first line, leave the page entact, and delete the second element leaving a blank line
	public void insideWord_Selection_PasteShortcut_ToNextPageEnd(){
		String expectedBeforePage = "First paragraphs";
		String expectedBrailleBeforePage = ",f/ p>agraphs";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.copy(textBot, 11, 13, 1);
		TextEditingTests.navigateTo(textBot, 15);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 15, 67);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to the start of the element on the next page and deletes
	//Should leave both lines intact since currently you cannot move elements from one print page to another since it would drastically alter markup
	//TODO fix indent issue
	public void end_Selection_Delete_ToNextPageStart(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.deleteSelection(textBot, 1, 25, 41, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to the start of the element on the next page and deletes using backspace
	//Should leave both lines entac since currently you cannot move elements from one print page to another since it would drastically alter markup
	//TODO fix indent issue
	public void end_Selection_Backspace_ToNextPageStart(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.deleteSelection(textBot, 1, 25, 41, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to the start of the element on the next page and deletes using cut
	//Should leave both lines entac since currently you cannot move elements from one print page to another since it would drastically alter markup
	//TODO fix indent issue
	public void end_Selection_Cut_ToNextPageStart(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.cut(bot, textBot, 1, 25, 41);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to the start of the element on the next page and deletes using cut shortcut
	//Should leave both lines entact since currently you cannot move elements from one print page to another since it would drastically alter markup
	//TODO fix indent issue
	public void end_Selection_CutShortcut_ToNextPageStart(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 25, 41);
		TextEditingTests.forceUpdate(bot, textBot);
//		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to the start of the element on the next page and inserts a char at the end of the first
	//second element should remain unchanged
	//TODO fix indent issue
	public void end_Selection_Insert_ToNextPageStart(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.typeTextInRange(textBot, "1", 1, 25, 41);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to the start of the element on the next page and pastes a char at the end of the first
	//second element should remain unchanged
	//TODO fix indent issue
	public void end_Selection_Paste_ToNextPageStart(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 24, 1);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.paste(bot, textBot, 1, 25, 41);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to the start of the element on the next page and pastes a char at the end of the first using the shortcut
	//second element should remain unchanged
	//TODO fix indent issue
	public void end_Selection_PasteShortcut_ToNextPageStart(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 24, 1);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 25, 41);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to inside the element on the next page and deletes the first char of the 2nd element
	//since the selection starts at the end o the element at the end of the page, the element and page are unaltered, only the 2nd element changes
	public void end_Selection_Delete_pastFirstChar(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.deleteSelection(textBot, 1, 25, 42, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to inside the element on the next page and deletes the first char of the 2nd element using backspace
	//since the selection starts at the end o the element at the end of the page, the element and page are unaltered, only the 2nd element changes
	public void end_Selection_Backspace_pastFirstChar(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.deleteSelection(textBot, 1, 25, 42, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to inside the element on the next page and deletes the first char of the 2nd element using cut
	//since the selection starts at the end o the element at the end of the page, the element and page are unaltered, only the 2nd element changes
	public void end_Selection_Cut_pastFirstChar(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.cut(bot, textBot, 1, 25, 42);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to inside the element on the next page and deletes the first char of the 2nd element using cut shortcut
	//since the selection starts at the end o the element at the end of the page, the element and page are unaltered, only the 2nd element changes
	public void end_Selection_CutShortcut_pastFirstChar(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 25, 42);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to inside the element on the next page and inserts a char
	//since the selection starts at the end o the element at the end of the page, the first and second element are editied, the page is unaltered
	public void end_Selection_Insert_PastFirstChar(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.typeTextInRange(textBot, "1", 1, 25, 42);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to inside the element on the next page and pastes a char
	//since the selection starts at the end o the element at the end of the page, the first and second element are editied, the page is unaltered
	public void end_Selection_Paste_PastFirstChar(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 24, 1);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.paste(bot, textBot, 1, 25, 42);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end ot the current element to inside the element on the next page and pastes a char using the keyboard shortcut
	//since the selection starts at the end o the element at the end of the page, the first and second element are editied, the page is unaltered
	public void end_Selection_PasteShortcut_PastFirstChar(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "age 2 paragraph";
		String expectedBrailleAfterPage= "age #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 24, 1);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 25, 42);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end of the current element to the end of the element on the next page and deletes
	//second element should be a blank line
	public void end_Selection_Delete_ToNextEnd(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.deleteSelection(textBot, 1, 25, 57, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end of the current element to the end of the element on the next page and deletes using backspace
	//second element should be a blank line
	public void end_Selection_Backspace_ToNextEnd(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.deleteSelection(textBot, 1, 25, 57, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end of the current element to the end of the element on the next page and deletes using cut
	//second element should be a blank line
	public void end_Selection_Cut_ToNextEnd(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.cut(bot, textBot, 1, 25, 57);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end of the current element to the end of the element on the next page and deletes using cut shortcut
	//second element should be a blank line
	public void end_Selection_CutShortcut_ToNextEnd(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 25, 57);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end of the current element to the end of the element on the next page and inserts a char
	//char occurs at end of first element, page is unchanged, second element is now a blank line
	public void end_Selection_Insert_ToNextEnd(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.typeTextInRange(textBot, "1", 1, 25, 57);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end of the current element to the end of the element on the next page and pastes a char
	//char occurs at end of first element, page is unchanged, second element is now a blank line
	public void end_Selection_Paste_ToNextEnd(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.copy(textBot, 1, 24, 1);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.paste(bot, textBot, 1, 25, 57);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects area from the end of the current element to the end of the element on the next page and pastes a char using keyboard shortcuts
	//char occurs at end of first element, page is unchanged, second element is now a blank line
	public void end_Selection_PasteShortcut_ToNextEnd(){
		String expectedBeforePage = "First paragraph on page 11";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #aa";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "";
		String expectedBrailleAfterPage= "";
		
		TextEditingTests.copy(textBot, 1, 24, 1);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 25, 57);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, select the area before the text and deletes the line break before and checks that text is corect before and after page
	public void BeforeElement_Selection_Delete(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 1, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, select the area before the text and deletes the line break before and checks that text is corect before and after page
	public void BeforeElement_Selection_Backspace(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 1, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, select the area before the text and deletes the line break before and checks that text is corect before and after page
	public void BeforeElement_Selection_Cut(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cut(bot, textBot, 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, select the area before the text and deletes the line break before using the keyboard shortcut and checks that text is correct before and after page
	public void BeforeElement_Selection_CutShortcut(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, deletes the line break and checks that text is corect before and after page
	public void afterElement_Delete(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, deletes the line break by selecting the area an deleting with delete key and checks that text is corect before and after page
	public void afterElement__Selection_Delete(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.deleteSelection(textBot, 1, 25, 1, SWT.DEL);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, deletes the line break by selecting the area an deleting with backspace key and checks that text is corect before and after page
	public void afterElement__Selection_Backspace(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.deleteSelection(textBot, 1, 25, 1, SWT.BS);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, deletes the line break by selecting the area an deleting with cut menu item and checks that text is corect before and after page
	public void afterElement__Selection_Cut(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.cut(bot, textBot, 1, 25, 1);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test 
	//Makes first element a heading1 with a blank line after, deletes the line break by selecting the area an deleting with cut keyboard shortcut and checks that text is corect before and after page
	public void afterElement__Selection_CutUsingKeyboardShortcut(){
		String expectedBeforePage = "First paragraph on page 1";
		String expectedBrailleBeforePage = ",f/ p>agraph on page #a";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 3);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.cut(bot, textBot, 1, 25, 1);
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void beforeElement_Delete_NextPageHeading(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "paragraph";
		String expectedBrailleAfterPage= "p>agraph";
		TextEditingTests.navigateTo(textBot, 67);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 75, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void beforeElement_Backspace_NextPageHeading(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "paragraph";
		String expectedBrailleAfterPage= "p>agraph";
		
		TextEditingTests.navigateTo(textBot, 67);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 75, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void beforeElement_Cut_NextPageHeading(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "paragraph";
		String expectedBrailleAfterPage= "p>agraph";
		
		TextEditingTests.navigateTo(textBot, 67);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cut(bot, textBot, 0, 0, 75); 
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void beforeElement_CutShortcut_NextPageHeading(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "paragraph";
		String expectedBrailleAfterPage= "p>agraph";
		
		TextEditingTests.navigateTo(textBot, 67);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 75); 
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void beforeElement_Insert_NextPageHeading(){
		String expectedBeforePage = "J";
		String expectedBrailleBeforePage = ";,j";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "paragraph";
		String expectedBrailleAfterPage= "p>agraph";
		
		TextEditingTests.navigateTo(textBot, 67);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.typeTextInRange(textBot, "J", 0, 0, 75);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void beforeElement_Paste_NextPageHeading(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "paragraph";
		String expectedBrailleAfterPage= "p>agraph";
		
		TextEditingTests.navigateTo(textBot, 67);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 0, 0, 75);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void beforeElement_PasteShortcut_NextPageHeading(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "paragraph";
		String expectedBrailleAfterPage= "p>agraph";
		
		TextEditingTests.navigateTo(textBot, 67);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 75);
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		
		//check page line
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from start to end and presses a letter key, no change should occur
	public void completePageSelection_Edit(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		textBot.selectRange(2, 0, 39);
		TextEditingTests.typeText(textBot, "k");
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from start to end and presses a letter key, no change should occur
	public void completePageSelection_Paste(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 5);
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.paste(bot, textBot, 2, 0, 39);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from start to end and presses a letter key, no change should occur
	public void completePageSelection_PasteShortcut(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 5);
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 2, 0, 39);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from start to end and presses delete, no change should occur
	public void completePageSelection_Delete(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 2, 0, 39, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from start to end and presses delete, no change should occur
	public void completePageSelection_Backspace(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 2, 0, 39, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from start to end and presses delete, no change should occur
	public void completePageSelection_Cut(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cut(bot, textBot, 2, 0, 39); 
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from start to end and presses delete, no change should occur
	public void completePageSelection_CutShortcut(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 2, 0, 39); 
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from point after start to end and presses delete, no change should occur
	public void partialPageSelection_Edit(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		textBot.selectRange(2, 10, 29);
		TextEditingTests.typeText(textBot, "K");
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from point after start to end and presses delete, no change should occur
	public void partialPageSelection_Paste(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 10);
		TextEditingTests.navigateTo(textBot, 27);
		textBot.selectRange(2, 10, 29);
		TextEditingTests.paste(bot, textBot, 2, 10, 29);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from point after start to end and presses delete, no change should occur
	public void partialPageSelection_PasteShortcut(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 10);
		TextEditingTests.navigateTo(textBot, 27);
		textBot.selectRange(2, 10, 29);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 2, 10, 29);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from point after start to end and presses delete, no change should occur
	public void partialPageSelection_Delete(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 2, 10, 29, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from point after start to end and presses delete, no change should occur
	public void partialPageSelection_Backspace(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 2, 10, 29, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from point after start to end and presses delete, no change should occur
	public void partialPageSelection_Cut(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cut(bot, textBot, 2, 10, 29);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects a line containing a page indicator from point after start to end and presses delete, no change should occur
	public void partialPageSelection_CutShortcut(){
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 2, 10, 29);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_completePageSelection_Edit(){
		String expectedBeforePage = "F";
		String expectedBeforeBraillePage = ";,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		textBot.selectRange(1, 0, 65);
		TextEditingTests.typeTextInRange(textBot, "F", 1, 0, 65);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_completePageSelection_Paste(){
		String expectedBeforePage = "F";
		String expectedBeforeBraillePage = ";,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		//textBot.selectRange(1, 0, 65);
		TextEditingTests.paste(bot, textBot, 1, 0, 65);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_completePageSelection_PasteShortcut(){
		String expectedBeforePage = "F";
		String expectedBeforeBraillePage = ";,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 65);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_completePageSelection_Delete(){
		String expectedBeforePage = "";
		String expectedBeforeBraillePage = ""; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 1, 0, 65, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_completePageSelection_Backspace(){
		String expectedBeforePage = "";
		String expectedBeforeBraillePage = ""; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 1, 0, 65, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_completePageSelection_Cut(){
		String expectedBeforePage = "";
		String expectedBeforeBraillePage = ""; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cut(bot, textBot, 1, 0, 65);
		TextEditingTests.forceUpdate(bot, textBot);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_completePageSelection_CutShortcut(){
		String expectedBeforePage = "";
		String expectedBeforeBraillePage = ""; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 65);
		TextEditingTests.forceUpdate(bot, textBot);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_partialPageSelection_Edit(){
		String expectedBeforePage = "F";
		String expectedBeforeBraillePage = ";,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.typeTextInRange(textBot, "F", 1, 0, 45);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_partialPageSelection_Paste(){
		String expectedBeforePage = "F";
		String expectedBeforeBraillePage = ";,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 1, 0, 45);
		//TextEditingTests.typeTextInRange(textBot, "F", 1, 0, 45);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_partialPageSelection_PasteShortcut(){
		String expectedBeforePage = "F";
		String expectedBeforeBraillePage = ";,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 45);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_partialPageSelection_Delete(){
		String expectedBeforePage = "";
		String expectedBeforeBraillePage = ""; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 1, 0, 45, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_partialPageSelection_Backspace(){
		String expectedBeforePage = "";
		String expectedBeforeBraillePage = ""; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 1, 0, 45, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_partialPageSelection_Cut(){
		String expectedBeforePage = "";
		String expectedBeforeBraillePage = ""; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cut(bot, textBot, 1, 0, 45);
		TextEditingTests.forceUpdate(bot, textBot);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void FromStartOfElement_partialPageSelection_CutShortcut(){
		String expectedBeforePage = "";
		String expectedBeforeBraillePage = ""; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 45);
		TextEditingTests.forceUpdate(bot, textBot);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses a letter key, no change should occur to indicator
	public void FromInsideOfElement_completePageSelection_Edit(){
		String expectedBeforePage = "First paraF";
		String expectedBeforeBraillePage = ",f/ p>a,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.typeTextInRange(textBot, "F", 1, 10, 55);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses a letter key, no change should occur to indicator
	public void FromInsideOfElement_completePageSelection_Paste(){
		String expectedBeforePage = "First paraF";
		String expectedBeforeBraillePage = ",f/ p>a,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 1, 10, 55);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses a letter key, no change should occur to indicator
	public void FromInsideOfElement_completePageSelection_PasteShortcut(){
		String expectedBeforePage = "First paraF";
		String expectedBeforeBraillePage = ",f/ p>a,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 10, 55);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_completePageSelection_Delete(){
		String expectedBeforePage = "First para";
		String expectedBeforeBraillePage = ",f/ p>a"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 1, 10, 55, SWT.DEL); 
		TextEditingTests.forceUpdate(bot, textBot);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_completePageSelection_Backspace(){
		String expectedBeforePage = "First para";
		String expectedBeforeBraillePage = ",f/ p>a"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 1, 10, 55, SWT.BS); 
		TextEditingTests.forceUpdate(bot, textBot);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_completePageSelection_Cut(){
		String expectedBeforePage = "First para";
		String expectedBeforeBraillePage = ",f/ p>a"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cut(bot, textBot, 1, 10, 55);
		TextEditingTests.forceUpdate(bot, textBot);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_completePageSelection_CutShortcut(){
		String expectedBeforePage = "First para";
		String expectedBeforeBraillePage = ",f/ p>a"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 10, 55);
		TextEditingTests.forceUpdate(bot, textBot);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_partialPageSelection_Edit(){
		String expectedBeforePage = "First paraF";
		String expectedBeforeBraillePage = ",f/ p>a,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.typeTextInRange(textBot, "F", 1, 10, 35);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_partialPageSelection_Paste(){
		String expectedBeforePage = "First paraF";
		String expectedBeforeBraillePage = ",f/ p>a,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.paste(bot, textBot, 1, 10, 35);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_partialPageSelection_PasteShortcut(){
		String expectedBeforePage = "First paraF";
		String expectedBeforeBraillePage = ",f/ p>a,f"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.copy(textBot, 1, 0, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 10, 35);
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_partialPageSelection_Delete(){
		String expectedBeforePage = "First para";
		String expectedBeforeBraillePage = ",f/ p>a"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 1, 10, 35, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_partialPageSelection_Backspace(){
		String expectedBeforePage = "First para";
		String expectedBeforeBraillePage = ",f/ p>a"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.deleteSelection(textBot, 1, 10, 35, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_partialPageSelection_Cut(){
		String expectedBeforePage = "First para";
		String expectedBeforeBraillePage = ",f/ p>a"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cut(bot, textBot, 1, 10, 35);
		TextEditingTests.forceUpdate(bot, textBot);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	//Selects an element before a line with page indicator, selects from point after start to end and presses delete, no change should occur to indicator
	public void FromInsideOfElement_partialPageSelection_CutShortcut(){
		String expectedBeforePage = "First para";
		String expectedBeforeBraillePage = ",f/ p>a"; 
		String expectedPage = "--------------------------------------1";
		String expectedBraillePage= "--------------------------------------#a";
		String expectedTreeItem = "pagenum";
		String expectedAfterPage = "Page 2 paragraph";
		String expectedBrailleAfterPage= ",page #b p>agraph";
		
		TextEditingTests.navigateTo(textBot, 27);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 10, 35);
		TextEditingTests.forceUpdate(bot, textBot);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 2);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBeforeBraillePage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBeforeBraillePage, resultBeforeBraillePage);
		assertEquals("p", resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultPage = textBot.getTextOnCurrentLine();
		String resultBraillePage = brailleBot.getTextOnCurrentLine();
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedPage, resultPage);
		assertEquals(expectedBraillePage, resultBraillePage);
		assertEquals(expectedTreeItem, resultTreeItem);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		expectedTreeItem = "p";
		resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedAfterPage, resultAfterPage);
		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void replaceAll_Heading(){
		String expectedBeforePage = "F";
		String expectedBrailleBeforePage = ";,f";
		String expectedTreeItem = "p";
	//	String expectedAfterPage = "";
	//	String expectedBrailleAfterPage= "";
		TextEditingTests.navigateTo(textBot, 381);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.typeText(textBot, "F");	
		TextEditingTests.forceUpdate(bot, textBot);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
	//	for(int i = 0; i < 3; i++) {
	//		String result = textBot.getTextOnCurrentLine();
	//		String resultBraille = brailleBot.getTextOnCurrentLine();
	//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
	//		assertEquals(expectedAfterPage , result);
	//		assertEquals(expectedBrailleAfterPage, resultBraille);
	//		assertEquals(expectedTreeItem, resultTreeItem);
	//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
	//	} 
		//check after double page
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		String resultAfterPage = textBot.getTextOnCurrentLine();
//		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
////		String resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
//		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
//		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void deleteAll_Heading(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedTreeItem = "p";
	//	String expectedAfterPage = "";
	//	String expectedBrailleAfterPage= "";
		TextEditingTests.navigateTo(textBot, 381);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.pressDelete(textBot, 1);	
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 11);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
	//	for(int i = 0; i < 3; i++) {
	//		String result = textBot.getTextOnCurrentLine();
	//		String resultBraille = brailleBot.getTextOnCurrentLine();
	//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
	//		assertEquals(expectedAfterPage , result);
	//		assertEquals(expectedBrailleAfterPage, resultBraille);
	//		assertEquals(expectedTreeItem, resultTreeItem);
	//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
	//	} 
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		String resultAfterPage = textBot.getTextOnCurrentLine();
//		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
//		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
//		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void deleteAll_Backspace_Heading(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedTreeItem = "p";
	//	String expectedAfterPage = "";
	//	String expectedBrailleAfterPage= "";
		TextEditingTests.navigateTo(textBot, 381);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.pressDelete(textBot, 1);	
		TextEditingTests.forceUpdate(bot, textBot);
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 11);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
	//	for(int i = 0; i < 3; i++) {
	//		String result = textBot.getTextOnCurrentLine();
	///		String resultBraille = brailleBot.getTextOnCurrentLine();
	//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
	//		assertEquals(expectedAfterPage , result);
	//		assertEquals(expectedBrailleAfterPage, resultBraille);
	//		assertEquals(expectedTreeItem, resultTreeItem);
	//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
	//	} 
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
	//	String resultAfterPage = textBot.getTextOnCurrentLine();
	//	String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
	//	String resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expectedAfterPage, resultAfterPage);
	//	assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	//	assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expectedAfterPage, resultAfterPage);
	//	assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
	//	assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void deleteAll_Cut_Heading(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedTreeItem = "p";
//		String expectedAfterPage = "";
//		String expectedBrailleAfterPage= "";
		TextEditingTests.navigateTo(textBot, 381);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.cut(bot);	
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 11);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		for(int i = 0; i < 3; i++) {
//			String result = textBot.getTextOnCurrentLine();
//			String resultBraille = brailleBot.getTextOnCurrentLine();
//			String resultTreeItem = treeBot.selection().get(0, 0).toString();
//			assertEquals(expectedAfterPage , result);
//			assertEquals(expectedBrailleAfterPage, resultBraille);
//			assertEquals(expectedTreeItem, resultTreeItem);
//			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		} 
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		String resultAfterPage = textBot.getTextOnCurrentLine();
//		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
//		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
//		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void deleteAll_CutShortcut_Heading(){
		String expectedBeforePage = "";
		String expectedBrailleBeforePage = "";
		String expectedTreeItem = "p";
//		String expectedAfterPage = "";
//		String expectedBrailleAfterPage= "";
		TextEditingTests.navigateTo(textBot, 381);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.cut(bot);	
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 11);
		
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		for(int i = 0; i < 3; i++) {
//			String result = textBot.getTextOnCurrentLine();
//			String resultBraille = brailleBot.getTextOnCurrentLine();
//			String resultTreeItem = treeBot.selection().get(0, 0).toString();
//			assertEquals(expectedAfterPage , result);
//			assertEquals(expectedBrailleAfterPage, resultBraille);
//			assertEquals(expectedTreeItem, resultTreeItem);
//			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		} 
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		String resultAfterPage = textBot.getTextOnCurrentLine();
//		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
///		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
//		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void replaceAll(){
		String expectedBeforePage = "Z";
		String expectedBrailleBeforePage = ";,z";
		String expectedTreeItem = "p";
	//	String expectedAfterPage = "";
	//	String expectedBrailleAfterPage= "";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.typeText(textBot, "Z");
		TextEditingTests.forceUpdate(bot, textBot);
		String resultBeforePage = textBot.getTextOnCurrentLine();
		String resultBrailleBeforePage = brailleBot.getTextOnCurrentLine();
		assertEquals(expectedBeforePage, resultBeforePage);
		assertEquals(expectedBrailleBeforePage, resultBrailleBeforePage);
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		for(int i = 0; i < 3; i++) {
//			String result = textBot.getTextOnCurrentLine();
//			String resultBraille = brailleBot.getTextOnCurrentLine();
//			String resultTreeItem = treeBot.selection().get(0, 0).toString();
//			assertEquals(expectedAfterPage , result);
//			assertEquals(expectedBrailleAfterPage, resultBraille);
//			assertEquals(expectedTreeItem, resultTreeItem);
//			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		} 
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		String resultAfterPage = textBot.getTextOnCurrentLine();
//		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
//		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expectedAfterPage, resultAfterPage);
//		assertEquals(expectedBrailleAfterPage, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void deleteAll(){
		String expected = "";
		String expectedTreeItem = "p";
		String expectedBraille = "";
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 11);
		
	//	for(int i = 0; i < 4; i++) {
	//		String result = textBot.getTextOnCurrentLine();
	//		String resultBraille = brailleBot.getTextOnCurrentLine();
	//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
	//		assertEquals(expected , result);
	//		assertEquals(expectedBraille, resultBraille);
	//		assertEquals(expectedTreeItem, resultTreeItem);
	//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
	//	} 
		
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expected, resultAfterPage);
		assertEquals(expectedBraille, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
	//	TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
	//	resultTreeItem = treeBot.selection().get(0, 0).toString();
	//	assertEquals(expected, resultAfterPage);
	//	assertEquals(expectedBraille, resultBrailleAfterPage);	
	//	assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void deleteAll_Backspace(){
		String expected = "";
		String expectedTreeItem = "p";
		String expectedBraille = "";
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.BS, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 11);
		
//		for(int i = 0; i < 4; i++) {
			String result = textBot.getTextOnCurrentLine();
			String resultBraille = brailleBot.getTextOnCurrentLine();
			String resultTreeItem = treeBot.selection().get(0, 0).toString();
			assertEquals(expected , result);
			assertEquals(expectedBraille, resultBraille);
			assertEquals(expectedTreeItem, resultTreeItem);
			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		} 
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		String resultAfterPage = textBot.getTextOnCurrentLine();
//		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expected, resultAfterPage);
//		assertEquals(expectedBraille, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expected, resultAfterPage);
//		assertEquals(expectedBraille, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void deleteAll_Cut(){
		String expected = "";
		String expectedTreeItem = "p";
		String expectedBraille = "";
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.cut(bot);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 11);
		
//		for(int i = 0; i < 4; i++) {
			String result = textBot.getTextOnCurrentLine();
			String resultBraille = brailleBot.getTextOnCurrentLine();
			String resultTreeItem = treeBot.selection().get(0, 0).toString();
			assertEquals(expected , result);
			assertEquals(expectedBraille, resultBraille);
			assertEquals(expectedTreeItem, resultTreeItem);
			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		} 
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
//		String resultAfterPage = textBot.getTextOnCurrentLine();
//		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
//		String resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expected, resultAfterPage);
//		assertEquals(expectedBraille, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expected, resultAfterPage);
//		assertEquals(expectedBraille, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	@Test
	public void deleteAll_CutShortcut(){
		String expected = "";
		String expectedTreeItem = "p";
		String expectedBraille = "";
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.cutUsingKeyboardShortcut(textBot);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 11);
		
//		for(int i = 0; i < 4; i++) {
	//		String result = textBot.getTextOnCurrentLine();
//			String resultBraille = brailleBot.getTextOnCurrentLine();
//			String resultTreeItem = treeBot.selection().get(0, 0).toString();
//			assertEquals(expected , result);
//			assertEquals(expectedBraille, resultBraille);
//			assertEquals(expectedTreeItem, resultTreeItem);
//			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		} 
		
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		String resultAfterPage = textBot.getTextOnCurrentLine();
		String resultBrailleAfterPage = brailleBot.getTextOnCurrentLine();
		String resultTreeItem = treeBot.selection().get(0, 0).toString();
		assertEquals(expected, resultAfterPage);
		assertEquals(expectedBraille, resultBrailleAfterPage);	
		assertEquals(expectedTreeItem, resultTreeItem);
		
		//check last line
//		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 2);
//		resultTreeItem = treeBot.selection().get(0, 0).toString();
//		assertEquals(expected, resultAfterPage);
//		assertEquals(expectedBraille, resultBrailleAfterPage);	
//		assertEquals(expectedTreeItem, resultTreeItem);
	}
	
	private void resetViewBots(){
		textBot = bot.styledText(0);
		brailleBot = bot.styledText(1);
		
		treeBot = bot.tree(0);
	}
}