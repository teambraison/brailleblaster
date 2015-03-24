package org.brailleblaster.unitTests;

import java.io.File;

import org.brailleblaster.BBIni;
import org.brailleblaster.Main;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.SWTBotTestCase;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class EditingTests extends SWTBotTestCase {
	protected static SWTBot bot;
	protected SWTBotStyledText textBot, brailleBot;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new Thread(new Runnable() {	  
			@Override
	        public void run() {
				String [] args = {"-debug", "EditingTests.xml"};
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
	
	@Test
	//Types a character at first offset
	public void test_SimpleInsert_AtStart(){
		String expected = "PPage 8 Sample Presentations";
		String expectedBraille = "_.,,ppage _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 1);	
		TextEditingTests.typeText(textBot, "P");
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 28);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 37);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//types a character inside element range
	public void test_SimpleInsert_Middle(){
		String expected = "Page 8 Samples Presentations";
		String expectedBraille = "_.,page _.#h _.,samples _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 14);	
		TextEditingTests.typeText(textBot, "s");
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 28);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 36);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//types a character at last position of element range
	public void test_SimpleInsert_End(){
		String expected = "Page 8 Sample Presentations'";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns'";
		TextEditingTests.navigateTo(textBot, 29);	
		TextEditingTests.typeText(textBot, "'");
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 28);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 36);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//uses delete key to remove first character
	public void test_SimpleDelete_Start(){
		String expected = "age 8 Sample Presentations";
		String expectedBraille = "_.age _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 1);	
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 26);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 33);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//uses delete key to remove character inside range
	public void test_SimpleDelete_Middle(){
		String expected = "Page 8 ample Presentations";
		String expectedBraille = "_.,page _.#h _.ample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 8);	
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 26);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 33);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//uses delete key to remove last character in range
	public void test_SimpleDelete_End(){
		String expected = "Page 8 Sample Presentation";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,n";
		TextEditingTests.navigateTo(textBot, 28);	
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 26);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 34);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//uses delete key to remove first character
	public void test_SimpleBackspace_Start(){
		String expected = "age 8 Sample Presentations";
		String expectedBraille = "_.age _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 2);	
		TextEditingTests.pressKey(textBot, SWT.BS, 1);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 26);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 33);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//uses backspace key to remove character inside range
	public void test_SimpleBackspace_Middle(){
		String expected = "Page 8 ample Presentations";
		String expectedBraille = "_.,page _.#h _.ample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 9);	
		TextEditingTests.pressKey(textBot, SWT.BS, 1);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 26);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 33);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//uses backspace key to remove last character in range
	public void test_SimpleBackspace_End(){
		String expected = "Page 8 Sample Presentation";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,n";
		TextEditingTests.navigateTo(textBot, 29);	
		TextEditingTests.pressKey(textBot, SWT.BS, 1);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 26);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 34);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//highlights a single character within the current range and replaces it by typing another single char
	//replacement is same length
	public void test_Inside_Word_Selection_SameLength(){
		String expected = "PaZe 8 Sample Presentations";
		String expectedBraille = "_.,pa,ze _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.typeTextInRange(textBot, "Z", 1, 2, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 27);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 36);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//highlights a single character at the start of the current range and replaces it by typing another single char
	//replacement is same length
	public void test_AtStart_Selection_SameLength(){
		String expected = "Gage 8 Sample Presentations";
		String expectedBraille = "_.,gage _.#h _.,sample _.,pres5t,ns";	
		TextEditingTests.typeTextInRange(textBot,"G", 1, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 27);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 35);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//highlights the last character at the end of the current range and replaces it by typing another single char
	//replacement is same length
	public void test_AtEnd_Selection_SameLength(){
		String expected = "Page 8 Sample PresentationZ";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,n,z";
		TextEditingTests.typeTextInRange(textBot,"Z", 1, 27, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 27);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 36);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes the first character in range, uses delete key
	public void test_AtStart_Selection_SameLength_Delete(){
		String expected = "age 8 Sample Presentations";
		String expectedBraille = "_.age _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.deleteSelection(textBot, 1, 0, 1, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes the first character in range, uses backspace key
	public void test_AtStart_Selection_SameLength_Backspace(){
		String expected = "age 8 Sample Presentations";
		String expectedBraille = "_.age _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.deleteSelection(textBot, 1, 0, 1, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes the first character in range, uses cut from menu
	public void test_AtStart_Selection_SameLength_Cut(){
		String expected = "age 8 Sample Presentations";
		String expectedBraille = "_.age _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.cut(bot, textBot, 1, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes the first character in range, uses cut with the keyboard shortcut
	public void test_AtStart_Selection_SameLength_CutShortcut(){
		String expected = "age 8 Sample Presentations";
		String expectedBraille = "_.age _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes a character inside the range, uses delete key
	public void test_Inside_Selection_SameLength_Delete(){
		String expected = "Page 8 Sample resentations";
		String expectedBraille = "_.,page _.#h _.,sample _.res5t,ns";
		TextEditingTests.deleteSelection(textBot, 1, 15, 1, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);		
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes a character inside the range, uses backspace key
	public void test_Inside_Selection_SameLength_Backspace(){
		String expected = "Page 8 Sample resentations";
		String expectedBraille = "_.,page _.#h _.,sample _.res5t,ns";
		TextEditingTests.deleteSelection(textBot, 1, 15, 1, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);		
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes a character inside the range, uses cut menu item
	public void test_Inside_Selection_SameLength_Cut(){
		String expected = "Page 8 Sample resentations";
		String expectedBraille = "_.,page _.#h _.,sample _.res5t,ns";		
		TextEditingTests.cut(bot, textBot, 1, 15, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);		
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes a character inside the range, uses cut keyboard shortcut
	public void test_Inside_Selection_SameLength_CutShortcut(){
		String expected = "Page 8 Sample resentations";
		String expectedBraille = "_.,page _.#h _.,sample _.res5t,ns";
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 15, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);		
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes a character at the end of the range, uses delete key
	public void test_AtEnd_Selection_SameLength_Delete(){
		String expected = "Page 8 Sample Presentation";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,n";
		TextEditingTests.deleteSelection(textBot, 1, 27, 1, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);		
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}

	@Test
	//Selects and deletes a character at the end of the range, uses backspace key
	public void test_AtEnd_Selection_SameLength_Backspace(){
		String expected = "Page 8 Sample Presentation";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,n";
		TextEditingTests.deleteSelection(textBot, 1, 27, 1, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);		
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes a character at the end of the range, uses cut menu item
	public void test_AtEnd_Selection_SameLength_Cut(){
		String expected = "Page 8 Sample Presentation";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,n";
		TextEditingTests.cut(bot, textBot, 1, 27, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);		
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects and deletes a character at the end of the range, uses cut keyboard shortcut
	public void test_AtEnd_Selection_SameLength_CutShortcut(){
		String expected = "Page 8 Sample Presentation";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,n";
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 27, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);		
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	
	@Test
	//Selects multiple characters inside a range and replaces it with an amount less than selection length
	//in this case a single typed character
	public void test_Inside_Selection_Shorter(){
		String expected = "Page 8 aple Presentations";
		String expectedBraille = "_.,page _.#h _.aple _.,pres5t,ns";
		TextEditingTests.typeTextInRange(textBot, "a", 1, 7, 3);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects multiple characters inside a range and replaces it with an amount less than selection length
	//uses paste menu item
	public void test_Inside_Selection_Shorter_Paste(){
		String expected = "Page 8 aple Presentations";
		String expectedBraille = "_.,page _.#h _.aple _.,pres5t,ns";
		TextEditingTests.copy(textBot, 1, 8, 1);
		TextEditingTests.paste(bot, textBot, 1, 7, 3);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects multiple characters inside a range and replaces it with an amount less than selection length
	//uses paste keyboard shortcut
	public void test_Inside_Selection_Shorter_PasteShortcut(){
		String expected = "Page 8 aple Presentations";
		String expectedBraille = "_.,page _.#h _.aple _.,pres5t,ns";
		TextEditingTests.copy(textBot, 1, 8, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 7, 3);
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	
	@Test
	//Selects multiple characters at the start of a range and replaces it with an amount less than selection length
	//in this case a single typed character
	public void test_AtStart_Selection_Shorter(){
		String expected = "a 8 Sample Presentations";
		String expectedBraille = "_.a _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.typeTextInRange(textBot, "a", 1, 0, 4);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects multiple characters at the start of a range and replaces it with an amount less than selection length
	//uses paste
	public void test_AtStart_Selection_Paste(){
		String expected = "a 8 Sample Presentations";
		String expectedBraille = "_.a _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.copy(textBot, 1, 1, 1);
		TextEditingTests.paste(bot, textBot, 1, 0, 4);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects multiple characters at the start of a range and replaces it with an amount less than selection length
	//uses paste shortcut
	public void test_AtStart_Selection_PasteShortcut(){
		String expected = "a 8 Sample Presentations";
		String expectedBraille = "_.a _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.copy(textBot, 1, 1, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 4);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects multiple characters at the end of a range and replaces it with an amount less than selection length
	//in this case a single typed character
	public void test_AtEnd_Selection_Shorter(){
		String expected = "Page 8 Sample Presentad";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5tad";
		TextEditingTests.typeTextInRange(textBot, "d", 1, 23, 5);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Selects multiple characters at the end of a range and replaces it with an amount less than selection length
	//uses paste menu item
	public void test_AtEnd_Selection_Shorter_Paste(){
		String expected = "Page 8 Sample Presentad";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5tad";
		TextEditingTests.copy(textBot, 3, 18, 1);
		TextEditingTests.paste(bot, textBot, 1, 23, 5);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	
	@Test
	//Selects multiple characters at the end of a range and replaces it with an amount less than selection length
	//uses paste shortcut
	public void test_AtEnd_Selection_Shorter_PasteShortcut(){
		String expected = "Page 8 Sample Presentad";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5tad";
		TextEditingTests.copy(textBot, 3, 18, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 23, 5);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From start of range deletes a multiple char selection shorter than the element length
	//uses delete key
	public void test_AtStart_Selection_Shorter_Delete(){
		String expected = "8 Sample Presentations";
		String expectedBraille = "_.#h _.,sample _.,pres5t,ns";
		TextEditingTests.deleteSelection(textBot,1, 0, 5, SWT.DEL);
		TextEditingTests.forceUpdate(bot,textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From start of range deletes a multiple char selection shorter than the element length
	//uses backspace
	public void test_AtStart_Selection_Shorter_Backspace(){
		String expected = "8 Sample Presentations";
		String expectedBraille = "_.#h _.,sample _.,pres5t,ns";
		TextEditingTests.deleteSelection(textBot,1, 0, 5, SWT.BS);
		TextEditingTests.forceUpdate(bot,textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From start of range deletes a multiple char selection shorter than the element length
	//uses cut menu item
	public void test_AtStart_Selection_Shorter_Delete_Cut(){
		String expected = "8 Sample Presentations";
		String expectedBraille = "_.#h _.,sample _.,pres5t,ns";
		TextEditingTests.cut(bot, textBot, 1, 0, 5);
		TextEditingTests.forceUpdate(bot,textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From start of range deletes a multiple char selection shorter than the element length
	//uses cut keyboard shortcut
	public void test_AtStart_Selection_Shorter_Delete_CutShortcut(){
		String expected = "8 Sample Presentations";
		String expectedBraille = "_.#h _.,sample _.,pres5t,ns";
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 5);
		TextEditingTests.forceUpdate(bot,textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside range deletes a multiple char selection shorter than the element length, but not reaching the end position
	//uses delete
	public void test_Inside_Selection_Shorter_Delete(){
		String expected = "Page 8 e Presentations";
		String expectedBraille = "_.,page _.#h _.;e _.,pres5t,ns";
		TextEditingTests.deleteSelection(textBot, 1, 7, 5, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside range deletes a multiple char selection shorter than the element length, but not reaching the end position
	//uses backspace
	public void test_Inside_Selection_Shorter_Backspace(){
		String expected = "Page 8 e Presentations";
		String expectedBraille = "_.,page _.#h _.;e _.,pres5t,ns";
		TextEditingTests.deleteSelection(textBot, 1, 7, 5, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside range deletes a multiple char selection shorter than the element length, but not reaching the end position
	//uses cut menu item
	public void test_Inside_Selection_Shorter_Cut(){
		String expected = "Page 8 e Presentations";
		String expectedBraille = "_.,page _.#h _.;e _.,pres5t,ns";
		TextEditingTests.cut(bot, textBot, 1, 7, 5);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside range deletes a multiple char selection shorter than the element length, but not reaching the end position
	//uses cut keyboard shortcut
	public void test_Inside_Selection_Shorter_CutShortcut(){
		String expected = "Page 8 e Presentations";
		String expectedBraille = "_.,page _.#h _.;e _.,pres5t,ns";
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 7, 5);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//deletes a multiple char selection from inside range to last position
	//uses delete
	public void test_AtEnd_Selection_Shorter_Delete(){
		String expected = "Page 8 Sample Present";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t";
		TextEditingTests.deleteSelection(textBot, 1, 22, 6, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//deletes a multiple char selection from inside range to last position
	//uses backspace
	public void test_AtEnd_Selection_Shorter_Backspace(){
		String expected = "Page 8 Sample Present";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t";
		TextEditingTests.deleteSelection(textBot, 1, 22, 6, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//deletes a multiple char selection from inside range to last position
	//uses cut menu item
	public void test_AtEnd_Selection_Shorter_Cut(){
		String expected = "Page 8 Sample Present";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t";
		TextEditingTests.cut(bot, textBot, 1, 22, 6);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//deletes a multiple char selection from inside range to last position
	//uses cut keyboard shortcut
	public void test_AtEnd_Selection_Shorter_CutShortcut(){
		String expected = "Page 8 Sample Present";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t";
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 22, 6);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From start of element range enters text longer than the selected text
	public void test_AtStart_Selection_Longer(){
		String expected = "Presentations 8 Sample Presentations";
		String expectedBraille = "_.,pres5t,ns _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.typeTextInRange(textBot, "Presentations", 1, 0, 4);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From start of element range pastes text longer than the selected text
	//uses menu item
	public void test_AtStart_Selection_Longer_Paste(){
		String expected = "Presentations 8 Sample Presentations";
		String expectedBraille = "_.,pres5t,ns _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.copy(textBot, 2, 0, 13);
		TextEditingTests.paste(bot, textBot, 1, 0, 4);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From start of element range pastes text longer than the selected text
	//uses keyboard shortcut
	public void test_AtStart_Selection_Longer_PasteShortcut(){
		String expected = "Presentations 8 Sample Presentations";
		String expectedBraille = "_.,pres5t,ns _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.copy(textBot, 2, 0, 13);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 4);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside element range enters text longer than the selected text, but not the the last position
	public void test_Inside_Selection_Longer(){
		String expected = "Page 8 Presentations Presentations";
		String expectedBraille = "_.,page _.#h _.,pres5t,ns _.,pres5t,ns";
		TextEditingTests.typeTextInRange(textBot, "Presentations", 1, 7, 6);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside element range pastes text longer than the selected text, but not the the last position
	//uses menu item
	public void test_Inside_Selection_Longer_Paste(){
		String expected = "Page 8 Presentations Presentations";
		String expectedBraille = "_.,page _.#h _.,pres5t,ns _.,pres5t,ns";
		TextEditingTests.copy(textBot, 2, 0, 13);
		TextEditingTests.paste(bot, textBot, 1, 7, 6);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside element range pastes text longer than the selected text, but not the the last position
	//uses keyboard shortcut
	public void test_Inside_Selection_Longer_PasteShortcut(){
		String expected = "Page 8 Presentations Presentations";
		String expectedBraille = "_.,page _.#h _.,pres5t,ns _.,pres5t,ns";
		TextEditingTests.copy(textBot, 2, 0, 13);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 7, 6);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside element range enters text longer than the selected text to the the last position
	public void test_atEnd_Selection_Longer(){
		String expected = "Page 8 Sample hand the householder";
		String expectedBraille = "_..,page #h ,sample h& ! _.h|sehold}";
		TextEditingTests.typeTextInRange(textBot, "hand the householder", 2, 0, 13);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside element range pastes text longer than the selected text to the the last position
	//uses menu item
	public void test_atEnd_Selection_Longer_Paste(){
		String expected = "Page 8 Sample Hand the householder";
		String expectedBraille = "_..,page #h ,sample ,h& ! _.h|sehold}";
		TextEditingTests.copy(textBot, 8, 14, 20);
		TextEditingTests.paste(bot, textBot, 2, 0, 13);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From inside element range pastes text longer than the selected text, to the the last position
	//uses keyboard shortcut
	public void test_atEnd_Selection_Longer_PasteShortcut(){
		String expected = "Page 8 Sample Hand the householder";
		String expectedBraille = "_..,page #h ,sample ,h& ! _.h|sehold}";
		TextEditingTests.copy(textBot, 8, 14, 20);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 2, 0, 13);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space before an element at the start of a document using the delete key
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BeforeElement_OutsideRange_pressDelete(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pressDelete(textBot, 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0) + textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(0) + brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space before an element at the start of a document by selecting range and pressing delete key
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BeforeElement_OutsideRange_Selection_Delete(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.deleteSelection(textBot, 0, 0, 1, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0) + textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(0) + brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space before an element at the start of a document by selecting range and pressing backspace key
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BeforeElement_OutsideRange_Selection_Backspace(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.deleteSelection(textBot, 0, 0, 1, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0) + textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(0) + brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space before an element at the start of a document by selecting range and using cut menu item
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BeforeElement_OutsideRange_Selection_Cut(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.cut(bot, textBot, 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0) + textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(0) + brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space before an element at the start of a document by selecting range and using cut keyboard shortcut
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BeforeElement_OutsideRange_Selection_CutShortcut(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0) + textBot.getTextOnLine(1);
		String resultBraille = brailleBot.getTextOnLine(0) + brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space between two element using delete key
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BetweenElement_OutsideRange_delete(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 29);
		TextEditingTests.pressDelete(textBot, 2, 13, 1);
		TextEditingTests.refresh(textBot);
		resetViewBots();
		textBot.setFocus();
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space between two element by selecting range and using delete key
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BetweenElement_OutsideRange_Selection_Delete(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 29);
		TextEditingTests.deleteSelection(textBot, 2, 13, 1, SWT.DEL);
		TextEditingTests.refresh(textBot);
		resetViewBots();
		textBot.setFocus();
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space between two element by selecting range and using backspace key
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BetweenElement_OutsideRange_Selection_Backspace(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 29);
		TextEditingTests.deleteSelection(textBot, 2, 13, 1, SWT.BS);
		TextEditingTests.refresh(textBot);
		resetViewBots();
		textBot.setFocus();
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space between two element by selecting range and using cut menu item
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BetweenElement_OutsideRange_Selection_Cut(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 29);
		TextEditingTests.cut(bot,textBot, 2, 13, 1);
		TextEditingTests.refresh(textBot);
		resetViewBots();
		textBot.setFocus();
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space between two element by selecting range and using cut keyboard shortcut
	//text should remained unchanged since spaces removed are outside of element range
	public void test_BetweenElement_OutsideRange_Selection_CutShortcut(){
		String expected = "Page 8 Sample Presentations";
		String expectedBraille = "_.,page _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 29);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 2, 13, 1);
		TextEditingTests.refresh(textBot);
		resetViewBots();
		textBot.setFocus();
		String result = textBot.getTextOnLine(1) + textBot.getTextOnLine(2);
		String resultBraille = brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space after last element by pressing delete key
	//text should remained unchanged since spaces removed are outside of element range
	public void test_AfterElement_OutsideRange_delete(){
		String expected = "THE END";
		String expectedBraille = ",,! ,,5d";
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.navigateTo(textBot, 1235);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 1243);
		TextEditingTests.pressDelete(textBot, 33, 7, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(32) + textBot.getTextOnLine(33);
		String resultBraille =  brailleBot.getTextOnLine(32) + brailleBot.getTextOnLine(33);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space after last element by selecting range and pressing delete key
	//text should remained unchanged since spaces removed are outside of element range
	public void test_AfterElement_OutsideRange_Selection_Delete(){
		String expected = "THE END";
		String expectedBraille = ",,! ,,5d";
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.navigateTo(textBot, 1235);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 1243);
		TextEditingTests.deleteSelection(textBot, 33, 7, 1, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(32) + textBot.getTextOnLine(33);
		String resultBraille =  brailleBot.getTextOnLine(32) + brailleBot.getTextOnLine(33);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space after last element by selecting range and pressing backspace key
	//text should remained unchanged since spaces removed are outside of element range
	public void test_AfterElement_OutsideRange_Selection_Backspace(){
		String expected = "THE END";
		String expectedBraille = ",,! ,,5d";
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.navigateTo(textBot, 1235);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 1243);
		TextEditingTests.deleteSelection(textBot, 33, 7, 1, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(32) + textBot.getTextOnLine(33);
		String resultBraille =  brailleBot.getTextOnLine(32) + brailleBot.getTextOnLine(33);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space after last element by selecting range and using cut menu item
	//text should remained unchanged since spaces removed are outside of element range
	public void test_AfterElement_OutsideRange_Selection_Cut(){
		String expected = "THE END";
		String expectedBraille = ",,! ,,5d";
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.navigateTo(textBot, 1235);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 1243);
		TextEditingTests.cut(bot, textBot, 33, 7, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(33);
		String resultBraille =  brailleBot.getTextOnLine(32) + brailleBot.getTextOnLine(33);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//Deletes the space after last element by selecting range and using cut keyboard shorcut
	//text should remained unchanged since spaces removed are outside of element range
	public void test_AfterElement_OutsideRange_Selection_CutShortcut(){
		String expected = "THE END";
		String expectedBraille = ",,! ,,5d";
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.navigateTo(textBot, 1235);
		TextEditingTests.applyStyle(bot, "centered Heading");
		TextEditingTests.navigateTo(textBot, 1243);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 33, 7, 1);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(32) + textBot.getTextOnLine(33) ;
		String resultBraille =  brailleBot.getTextOnLine(32) + brailleBot.getTextOnLine(33);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}

	@Test
	//From area between elements to inside the element is selected and text is typed
	//highlighted area is deleted and replaced
	public void test_BeforeElement_OutsideRangeToInsideRange_Selection_Insert(){
		String expected = "dge 8 Sample Presentations";
		String expectedBraille = "_.dge _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.typeTextInRange(textBot, "d", 0, 0, 3);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to inside the element is selected and new text inserted using paste menu item
	//highlighted area is deleted and replaced
	public void test_BeforeElement_OutsideRangeToInsideRange_Selection_Paste(){
		String expected = "Presentations 8 Sample Presentations";
		String expectedBraille = "_.,pres5t,ns _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.copy(textBot, 2, 0, 13);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.paste(bot, textBot, 0, 0, 5);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0) + textBot.getTextOnLine(1);
		String resultBraille =  brailleBot.getTextOnLine(0) + brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to inside the element is selected and new text inserted using paste keyboard shortcut
	//highlighted area is deleted and replaced
	public void test_BeforeElement_OutsideRangeToInsideRange_Selection_PasteShortcut(){
		String expected = "Presentations 8 Sample Presentations";
		String expectedBraille = "_.,pres5t,ns _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.copy(textBot, 2, 0, 13);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 5);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0) + textBot.getTextOnLine(1);
		String resultBraille =  brailleBot.getTextOnLine(0) + brailleBot.getTextOnLine(1);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	public void test_BeforeElement_OutsideRangeToInsideRange_Delete(){
		String expected = "ge 8 Sample Presentations";
		String expectedBraille = "_.ge _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pressDelete(textBot, 0, 0, 3);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to inside the element is selected and deleted using delete key
	//highlighted area is deleted and replaced
	public void test_BeforeElement_OutsideRangeToInsideRange_Selection_Delete(){
		String expected = "ge 8 Sample Presentations";
		String expectedBraille = "_.ge _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 3, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to inside the element is selected and deleted using backspace key
	//highlighted area is deleted and replaced
	public void test_BeforeElement_OutsideRangeToInsideRange_Selection_Backspace(){
		String expected = "ge 8 Sample Presentations";
		String expectedBraille = "_.ge _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 3, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to inside the element is selected and deleted using cut menu item
	//highlighted area is deleted and replaced
	public void test_BeforeElement_OutsideRangeToInsideRange_Selection_Cut(){
		String expected = "ge 8 Sample Presentations";
		String expectedBraille = "_.ge _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cut(bot, textBot, 0, 0, 3);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to inside the element is selected and deleted using cut keyboard shortcut
	//highlighted area is deleted and replaced
	public void test_BeforeElement_OutsideRangeToInsideRange_Selection_CutShortcut(){
		String expected = "ge 8 Sample Presentations";
		String expectedBraille = "_.ge _.#h _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 3);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to the end of the element range is selected and new text is inserted
	public void test_BeforeElement_ToEnd_Insert(){
		String expected = "d";
		String expectedBraille = "_.;d";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.typeTextInRange(textBot, "d", 0, 0, 29);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to the end of the element range is selected and new text is inserted using paste menu item
	public void test_BeforeElement_ToEnd_Paste(){
		String expected = "Presentations";
		String expectedBraille = "_.,pres5t,ns";
		TextEditingTests.copy(textBot, 2, 0, 13);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.paste(bot, textBot, 0, 0, 29);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to the end of the element range is selected and new text is inserted using paste shortcut
	public void test_BeforeElement_ToEnd_PasteShortcut(){
		String expected = "Presentations";
		String expectedBraille = "_.,pres5t,ns";
		TextEditingTests.copy(textBot, 2, 0, 13);
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 0, 0, 29);
		TextEditingTests.forceUpdate(bot, textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to the end of the element range is selected and deleted using delete key
	//Tests next element to insure text has not been altered
	public void test_BeforeElement_ToEnd_Selection_Delete(){
		String expected = "To Start Bible Studies on the First ";
		String expectedBraille = "_..,6,/>t ,bi# ,/udies on ! ,f/ ";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 29, SWT.DEL);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressDelete(textBot, 0, 0, 1);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to the end of the element range is selected and deleted using backspace
	//Tests next element to insure text has not been altered
	public void test_BeforeElement_ToEnd_Selection_Backspace(){
		String expected = "To Start Bible Studies on the First ";
		String expectedBraille = "_..,6,/>t ,bi# ,/udies on ! ,f/ ";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.deleteSelection(textBot, 0, 0, 29, SWT.BS);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressDelete(textBot, 0, 0, 1);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to the end of the element range is selected and deleted using cut menu item
	//Tests next element to insure text has not been altered
	public void test_BeforeElement_ToEnd_Selection_Cut(){
		String expected = "To Start Bible Studies on the First ";
		String expectedBraille = "_..,6,/>t ,bi# ,/udies on ! ,f/ ";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cut(bot, textBot, 0, 0, 29);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressDelete(textBot, 0, 0, 1);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//From area between elements to the end of the element range is selected and deleted using cut shortcut
	//Tests next element to insure text has not been altered
	public void test_BeforeElement_ToEnd_Selection_CutShortcut(){
		String expected = "To Start Bible Studies on the First ";
		String expectedBraille = "_..,6,/>t ,bi# ,/udies on ! ,f/ ";
		TextEditingTests.navigateTo(textBot, 0);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 0, 0, 29);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.pressDelete(textBot, 0, 0, 1);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	public void test_replaceALL(){
		String expected = "Z";
		String expectedBraille = "_.;,z";
		TextEditingTests.navigateTo(textBot, 109);
		TextEditingTests.typeTextInRange(textBot, " additional", 7, 26, 0);
		TextEditingTests.forceUpdate(bot, textBot);
		///TextEditingTests.openStylePanel(bot);
		
		//TextEditingTests.navigateTo(textBot, 1250);
		//TextEditingTests.applyStyle(bot, "para");
		TextEditingTests.refresh(textBot);
		resetViewBots();
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.typeText(textBot, "Z");
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}

	@Test
	public void test_deleteALL(){
		String expected = "";
		String expectedBraille = "";
		TextEditingTests.navigateTo(textBot, 109);
		TextEditingTests.typeTextInRange(textBot, " additional", 7, 26, 0);
		TextEditingTests.forceUpdate(bot, textBot);
		//TextEditingTests.openStylePanel(bot);
		//TextEditingTests.navigateTo(textBot, 1250);
		//TextEditingTests.applyStyle(bot, "para");
		TextEditingTests.refresh(textBot);
		resetViewBots();
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.pressDelete(textBot, 1);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	public void test_deleteALL_Backspace(){
		String expected = "";
		String expectedBraille = "";
		TextEditingTests.navigateTo(textBot, 109);
		TextEditingTests.typeTextInRange(textBot, " additional", 7, 26, 0);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.openStylePanel(bot);
	//	TextEditingTests.navigateTo(textBot, 1250);
	//	TextEditingTests.applyStyle(bot, "para");
		TextEditingTests.refresh(textBot);
		resetViewBots();
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.pressBackspace(textBot, 1);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	public void test_deleteALL_Cut(){
		String expected = "";
		String expectedBraille = "";
		TextEditingTests.navigateTo(textBot, 109);
		TextEditingTests.typeTextInRange(textBot, " additional", 7, 26, 0);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.openStylePanel(bot);
	//	TextEditingTests.navigateTo(textBot, 1250);
	//	TextEditingTests.applyStyle(bot, "para");
		TextEditingTests.refresh(textBot);
		resetViewBots();
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.cut(bot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	public void test_deleteALL_CutKeyboardShortcut(){
		String expected = "";
		String expectedBraille = "";
		TextEditingTests.navigateTo(textBot, 109);
		TextEditingTests.typeTextInRange(textBot, " additional", 7, 26, 0);
		TextEditingTests.forceUpdate(bot, textBot);
	//	TextEditingTests.openStylePanel(bot);
	//	TextEditingTests.navigateTo(textBot, 1250);
	//	TextEditingTests.applyStyle(bot, "para");
		TextEditingTests.refresh(textBot);
		resetViewBots();
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.cutUsingKeyboardShortcut(textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	public void test_replaceALL_SpacesAfter_CutKeyboardShortcut(){
		String expected = "Z";
		String expectedBraille = "_.;,z";
		TextEditingTests.navigateTo(textBot, 109);
		TextEditingTests.typeTextInRange(textBot, " additional", 7, 26, 0);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.navigateTo(textBot, 1247);
		TextEditingTests.applyStyle(bot, "heading1");
		TextEditingTests.refresh(textBot);
		resetViewBots();
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.typeText(textBot, "Z");
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	public void test_deleteALL_SpacesAfter_CutKeyboardShortcut(){
		System.out.println("Starting test");
		String expected = "";
		String expectedBraille = "";
		TextEditingTests.navigateTo(textBot, 109);
		TextEditingTests.typeTextInRange(textBot, " additional", 7, 26, 0);
		TextEditingTests.forceUpdate(bot, textBot);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.navigateTo(textBot, 1247);
		TextEditingTests.applyStyle(bot, "heading1");
		TextEditingTests.refresh(textBot);
		resetViewBots();
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.selectAll(bot, textBot);
		TextEditingTests.cutUsingKeyboardShortcut(textBot);
		String result = textBot.getTextOnLine(0);
		String resultBraille =  brailleBot.getTextOnLine(0);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	@Test
	//selects a single character, replaces it with a single character
	//checks that update occurs if no change in length
	public void zeroLengthChange(){
		String expected = "Page 1 Sample Presentations";
		String expectedBraille = "_.,page _.#a _.,sample _.,pres5t,ns";
		TextEditingTests.navigateTo(textBot, 5);
		textBot.selectRange(1, 5, 1);
		TextEditingTests.typeText(textBot, "1");
		TextEditingTests.forceUpdate(bot, textBot);	
		String result = TextEditingTests.getResultBySelection(textBot, 1, 0, 27);
		String resultBraille = TextEditingTests.getResultBySelection(brailleBot, 1, 0, 35);
		assertEquals(expected, result);
		assertEquals(expectedBraille, resultBraille);
	}
	
	private void resetViewBots(){
		textBot = bot.styledText(0);
		brailleBot = bot.styledText(1);
	}
}
