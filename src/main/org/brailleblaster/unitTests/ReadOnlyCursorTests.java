package org.brailleblaster.unitTests;

import static org.junit.Assert.*;

import java.io.File;

import org.brailleblaster.BBIni;
import org.brailleblaster.Main;
import org.eclipse.swt.SWT;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Event;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.Position;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReadOnlyCursorTests {
	private final static String XMLTREE = "XML";
	
	protected static SWTBot bot;
	protected SWTBotStyledText textBot, brailleBot;
	protected SWTBotTree treeBot;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		new Thread(new Runnable() {	  
			@Override
	        public void run() {
				String [] args = {"-debug", "ReadOnlyCursorTests.xml"};
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
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		bot.menu("Exit").click();
	}

	@Before
	public void setUp() throws Exception {
		bot.menu("&Open").click();
		resetViewBots();
	}

	@After
	public void tearDown() throws Exception {
		bot.menu("&Close").click();
		File f = new File(BBIni.getTempFilesPath() + BBIni.getFileSep() + "TestXML.sem");
		if(f.exists())
			f.delete();
	}
	
	//Tests pressing down arrow into a pagenum element and correct update in xml tree
	@Test
	public void basicArrowDownTest(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(2, 2);
		String expectedText = "pagenum";
	
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		
		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//Tests pressing down arrow into a pagenum element  in the braille view and correct update in xml tree
	@Test
	public void basicArrowDownTest_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(2, 3);
		String expectedText = "pagenum";
		
		TextEditingTests.navigateTo(brailleBot, 1);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_DOWN, 1);
		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing right key at the end of a line into a pagenum element and correct update in xml tree
	@Test
	public void basicArrowRightTest(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(2, 0);
		String expectedText = "pagenum";
		
		TextEditingTests.navigateTo(textBot, 26);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);		
		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing right key at the end of a line into a pagenum element  in the braille view and correct update in xml tree
	@Test
	public void basicArrowRightTest_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(2, 0);
		String expectedText = "pagenum";
		
		TextEditingTests.navigateTo(brailleBot, 24);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_RIGHT, 1);		
		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing up arrow into a pagenum element and correct update in xml tree
	@Test
	public void basicArrowUpTest(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(2, 2);
		TextEditingTests.navigateTo(textBot, 67);
		String expectedText = "pagenum";
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);		
		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(expectedText, resultText);
		assertEquals(correctPos, textBot.cursorPosition());
	}
	
	//tests pressing up arrow into a pagenum element in the braille view and correct update in xml tree
	@Test
	public void basicArrowUpTest_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(2, 3);
		String expectedText = "pagenum";
		
		TextEditingTests.navigateTo(brailleBot, 66);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_UP, 1);		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing left arrow into a pagenum element and correct update in xml tree
	@Test
	public void basicArrowLeftTest(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(2, 39);
		String expectedText = "pagenum";
		
		TextEditingTests.navigateTo(textBot, 67);
		TextEditingTests.pressKey(textBot, SWT.ARROW_LEFT, 1);		
		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing left arrow in the braille view and correct update in xml tree
	@Test
	public void basicArrowLeftTest_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(2, 40);
		String expectedText = "pagenum";
		
		TextEditingTests.navigateTo(brailleBot, 66);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_LEFT, 1);		
		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}	
	
	
	//tests pressing down arrow into a sidebar element and correct update in xml tree
	@Test
	public void Boxline_basicArrowDown_Text(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 0);
		String expectedText = "sidebar";
		
		TextEditingTests.navigateTo(textBot, 452);
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);		
		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing down arrow in the braille view in sidebar element and correct update in xml tree
	@Test
	public void Boxline_basicArrowDown_Bralle(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 0);
		String expectedText = "sidebar";
		
		TextEditingTests.navigateTo(brailleBot, 455);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_DOWN, 1);		
		
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing right arrow into a sidebar element and correct update in xml tree
	@Test
	public void Boxline_basicArrowRight_Text(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 0);
		String expectedText = "sidebar";
			
		TextEditingTests.navigateTo(textBot, 491);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);		
			
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing right arrow into a sidebar element and correct update in xml tree
	@Test
	public void Boxline_basicArrowRight_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 0);
		String expectedText = "sidebar";
				
		TextEditingTests.navigateTo(brailleBot, 495);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_RIGHT, 1);		
				
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing up arrow into a sidebar element and correct update in xml tree
	@Test
	public void Boxline_basicArrowUP_Text(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 2);
		String expectedText = "sidebar";
				
		TextEditingTests.navigateTo(textBot, 533);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);		
				
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing up arrow into a sidebar element and correct update in xml tree
	@Test
	public void Boxline_basicArrowUp_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 3);
		String expectedText = "sidebar";
					
		TextEditingTests.navigateTo(brailleBot, 537);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_UP, 1);		
					
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing left arrow into a sidebar element and correct update in xml tree
	@Test
	public void Boxline_basicArrowLeft_Text(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 40);
		String expectedText = "sidebar";
					
		TextEditingTests.navigateTo(textBot, 533);
		TextEditingTests.pressKey(textBot, SWT.ARROW_LEFT, 1);		
					
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
		
	//tests pressing up arrow into a sidebar element and correct update in xml tree
	@Test
	public void Boxline_basicArrowLeft_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 40);
		String expectedText = "sidebar";
						
		TextEditingTests.navigateTo(brailleBot, 537);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_LEFT, 1);		
						
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing down arrow out of a sidebar element and correct update in xml tree
	@Test
	public void InsideBoxline_basicArrowDown_Text(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(17, 0);
		String expectedText = "p";
			
		TextEditingTests.navigateTo(textBot, 492);
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);		
			
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
		
	//tests pressing down arrow out of a sidebar element in braille view and correct update in xml tree
	@Test
	public void InsideBoxline_basicArrowDown_Bralle(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(17, 0);
		String expectedText = "p";
			
		TextEditingTests.navigateTo(brailleBot, 496);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_DOWN, 1);		
			
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
		
	//tests pressing right arrow out of a sidebar element and correct update in xml tree
	@Test
	public void InsideBoxline_basicArrowRight_Text(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(17, 0);
		String expectedText = "p";
				
		TextEditingTests.navigateTo(textBot, 532);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);		
				
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
		
	//tests pressing down arrow out of a sidebar element in braille view and correct update in xml tree
	@Test
	public void InsideBoxline_basicArrowRight_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(17, 0);
		String expectedText = "p";
					
		TextEditingTests.navigateTo(brailleBot, 536);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_RIGHT, 1);		
					
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing up arrow to move out of a sidebar element in braille view and correct update in xml tree
	@Test
	public void InsideBoxline_basicArrowUp_Text(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 2);
		String expectedText = "sidebar";
						
		TextEditingTests.navigateTo(textBot, 533);
		TextEditingTests.pressKey(textBot, SWT.ARROW_UP, 1);		
						
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing down arrow out of a sidebar element in braille view and correct update in xml tree
	@Test
	public void InsideBoxline_basicArrowUp_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(16, 3);
		String expectedText = "sidebar";
						
		TextEditingTests.navigateTo(brailleBot, 537);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_UP, 1);		
						
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//552 555
	//tests pressing right arrow into bottom boxline and correct update in xml tree
	@Test
	public void BottomBoxline_basicArrowRight_Text(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(18, 0);
		String expectedText = "sidebar";
					
		TextEditingTests.navigateTo(textBot, 552);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);		
					
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing right arrow into bottom boxline and correct update in xml tree
	@Test
	public void BottomBoxline_basicArrowRight_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(18, 0);
		String expectedText = "sidebar";
						
		TextEditingTests.navigateTo(brailleBot, 555);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_RIGHT, 1);		
						
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	@Test
	public void BottomBoxline_basicArrowDown_Text(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(18, 2);
		String expectedText = "sidebar";
					
		TextEditingTests.navigateTo(textBot, 533);
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);		
					
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, textBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	//tests pressing right arrow into bottom boxline and correct update in xml tree
	@Test
	public void BottomBoxline_basicArrowDown_Braille(){
		TextEditingTests.selectTree(bot, XMLTREE);
		Position correctPos = new Position(18, 3);
		String expectedText = "sidebar";
						
		TextEditingTests.navigateTo(brailleBot, 537);
		TextEditingTests.pressKey(brailleBot, SWT.ARROW_DOWN, 1);		
						
		String resultText = treeBot.selection().get(0, 0).toString();
		assertEquals(correctPos, brailleBot.cursorPosition());
		assertEquals(expectedText, resultText);
	}
	
	/*
	private void mouseClickOnText(final int x, final int y, final SWTBotStyledText stBot){
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Event e = new Event();
				e.type = SWT.MouseDown;
				e.x = x;
				e.y = y;
				stBot.widget.notifyListeners(SWT.MouseDown, e);
			}
		});
	}
	*/
	private void resetViewBots(){
		textBot = bot.styledText(0);
		brailleBot = bot.styledText(1);
		treeBot = bot.tree(0);
	}
}
