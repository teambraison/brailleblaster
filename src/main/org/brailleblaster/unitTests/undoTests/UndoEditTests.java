package org.brailleblaster.unitTests.undoTests;

import static org.junit.Assert.*;

import org.brailleblaster.Main;
import org.brailleblaster.unitTests.TextEditingTests;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UndoEditTests {

	private final static String XMLTREE = "XML";
	private final static String BLANK_LINE = "";
	
	protected static SWTBot bot;
	protected SWTBotStyledText textBot, brailleBot;
	protected SWTBotTree treeBot;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		new Thread(new Runnable() {	  
			@Override
	        public void run() {
				String [] args = {"-debug", "UndoEditTests.xml"};
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
	public void basicEditsTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Pjaragraph 1";
		
		TextEditingTests.navigateTo(textBot, 2);	
		TextEditingTests.typeText(textBot, "j");
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
	
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void basicBackspaceTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Pararaph 1";
		
		TextEditingTests.navigateTo(textBot, 6);	
		TextEditingTests.pressKey(textBot, SWT.BS, 1);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void basicDeleteTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Pararaph 1";
		
		TextEditingTests.navigateTo(textBot, 5);	
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void basicCutTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Pararaph 1";
		
		TextEditingTests.navigateTo(textBot, 5);
		TextEditingTests.cut(bot, textBot, 1, 4, 1);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void basicCutShortcutTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Pararaph 1";
		
		TextEditingTests.navigateTo(textBot, 5);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 4, 1);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void multipleCharReplaceTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Parajaph 1";
		
		TextEditingTests.navigateTo(textBot, 4);	
		textBot.selectRange(1, 4, 2);
		TextEditingTests.typeText(textBot, "j");
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void multipleCharPasteTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Para1aph 1";
		
		TextEditingTests.copy(textBot, 1, 10, 1);
		TextEditingTests.navigateTo(textBot, 4);	
		TextEditingTests.paste(bot, textBot, 1, 4, 2);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void multipleCharPasteShortcutTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Para1aph 1";
		
		TextEditingTests.copy(textBot, 1, 10, 1);
		TextEditingTests.navigateTo(textBot, 4);	
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 4, 2);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void multipleCharBackspaceTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Paraaph 1";
		
		TextEditingTests.navigateTo(textBot, 4);	
		textBot.selectRange(1, 4, 2);
		TextEditingTests.pressKey(textBot, SWT.BS, 1);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void multipleCharDeleteTest(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Paraaph 1";
		
		TextEditingTests.navigateTo(textBot, 4);	
		textBot.selectRange(1, 4, 2);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void fullReplace(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "t";
		TextEditingTests.navigateTo(textBot, 1);	
		textBot.selectRange(1, 0, 11);
		TextEditingTests.typeText(textBot, "t");
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void fullPaste(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "P";
		
		TextEditingTests.navigateTo(textBot, 1);	
		TextEditingTests.copy(textBot, 1, 0, 1);
		
		TextEditingTests.navigateTo(textBot, 1);	
		TextEditingTests.paste(bot, textBot, 1, 0, 11);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void fullPasteShortcut(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "P";
		
		TextEditingTests.navigateTo(textBot, 1);	
		TextEditingTests.copy(textBot, 1, 0, 1);
		
		TextEditingTests.navigateTo(textBot, 1);	
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 1, 0, 11);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void fullDelete(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		
		TextEditingTests.navigateTo(textBot, 1);	
		textBot.selectRange(1, 0, 11);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		
		assertEquals(BLANK_LINE, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(BLANK_LINE, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void fullBackspace(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		
		TextEditingTests.navigateTo(textBot, 1);	
		textBot.selectRange(1, 0, 11);
		TextEditingTests.pressKey(textBot, SWT.BS, 1);
		
		assertEquals(BLANK_LINE, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(BLANK_LINE, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void fullCut(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		
		TextEditingTests.navigateTo(textBot, 1);	
		TextEditingTests.cut(bot, textBot, 1, 0, 11);
		
		assertEquals(BLANK_LINE, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(BLANK_LINE, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void fullCutShortcut(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		
		TextEditingTests.navigateTo(textBot, 1);	
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 1, 0, 11);
		
		assertEquals(BLANK_LINE, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(BLANK_LINE, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
	}
	
	@Test
	public void multiLineReplace(){
		String textBefore = "Paragraph 5 contains a long string of text that \ncovers multiples lines in the document";
		String brailleBefore = ",p>agraph #e 3ta9s a l;g /r+ ( text t \ncov}s multiples l9es 9 ! docu;t";
		
		String textAfter = "Paragraph 5 contains t in the document";
		TextEditingTests.navigateTo(textBot, 6);
		textBot.selectRange(6, 21, 50);
		TextEditingTests.typeText(textBot, "t");
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
	}
	
	@Test
	public void multiLinePaste(){
		String textBefore = "Paragraph 5 contains a long string of text that \ncovers multiples lines in the document";
		String brailleBefore = ",p>agraph #e 3ta9s a l;g /r+ ( text t \ncov}s multiples l9es 9 ! docu;t";
		
		String textAfter = "Paragraph 5 contains t in the document";
		TextEditingTests.navigateTo(textBot, 6);
		TextEditingTests.copy(textBot, 6, 15, 1);
		TextEditingTests.paste(bot, textBot, 6, 21, 50);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
	}
	
	@Test
	public void multiLinePasteShortcut(){
		String textBefore = "Paragraph 5 contains a long string of text that \ncovers multiples lines in the document";
		String brailleBefore = ",p>agraph #e 3ta9s a l;g /r+ ( text t \ncov}s multiples l9es 9 ! docu;t";
		
		String textAfter = "Paragraph 5 contains t in the document";
		TextEditingTests.navigateTo(textBot, 6);
		TextEditingTests.copy(textBot, 6, 15, 1);
		TextEditingTests.pasteUsingKeyboardShortcut(textBot, 6, 21, 50);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
	}
	
	@Test
	public void multiLineDelete(){
		String textBefore = "Paragraph 5 contains a long string of text that \ncovers multiples lines in the document";
		String brailleBefore = ",p>agraph #e 3ta9s a l;g /r+ ( text t \ncov}s multiples l9es 9 ! docu;t";
		
		String textAfter = "Paragraph 5 contains  in the document";
		TextEditingTests.navigateTo(textBot, 6);
		textBot.selectRange(6, 21, 50);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
	}
	
	@Test
	public void multiLineBackspace(){
		String textBefore = "Paragraph 5 contains a long string of text that \ncovers multiples lines in the document";
		String brailleBefore = ",p>agraph #e 3ta9s a l;g /r+ ( text t \ncov}s multiples l9es 9 ! docu;t";
		
		String textAfter = "Paragraph 5 contains  in the document";
		TextEditingTests.navigateTo(textBot, 6);
		textBot.selectRange(6, 21, 50);
		TextEditingTests.pressKey(textBot, SWT.BS, 1);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
	}
	
	@Test
	public void multiLineCut(){
		String textBefore = "Paragraph 5 contains a long string of text that \ncovers multiples lines in the document";
		String brailleBefore = ",p>agraph #e 3ta9s a l;g /r+ ( text t \ncov}s multiples l9es 9 ! docu;t";
		
		String textAfter = "Paragraph 5 contains  in the document";
		TextEditingTests.navigateTo(textBot, 6);
		TextEditingTests.cut(bot, textBot, 6, 21, 50);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
	}
	
	@Test
	public void multiLineCutShortcut(){
		String textBefore = "Paragraph 5 contains a long string of text that \ncovers multiples lines in the document";
		String brailleBefore = ",p>agraph #e 3ta9s a l;g /r+ ( text t \ncov}s multiples l9es 9 ! docu;t";
		
		String textAfter = "Paragraph 5 contains  in the document";
		TextEditingTests.navigateTo(textBot, 6);
		TextEditingTests.cutUsingKeyboardShortcut(textBot, 6, 21, 50);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());

		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n"+ textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine() + "\n" + brailleBot.getTextOnLine(7));
	}
	
	@Test
	public void addspace_undo_redo(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Paragraph 1 ";
		
		textBot.navigateTo(1, 11);
		TextEditingTests.typeText(textBot, " ");
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
	}
	
	@Test
	public void addword_end_undo_redo(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Paragraph 1 here";
		
		textBot.navigateTo(1, 11);
		TextEditingTests.typeText(textBot, " here");
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
	}
	
	@Test
	public void addword_beginning_undo_redo(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "A Paragraph 1";
		
		textBot.navigateTo(1, 0);
		TextEditingTests.typeText(textBot, "A ");
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
	}
	
	@Test
	public void edit_plus_multiple_words_undo_redo(){
		String textBefore = "Paragraph 1";
		String brailleBefore = ",p>agraph #a";
		String textAfter = "Paragraphs including 1";
		
		textBot.navigateTo(1, 9);
		TextEditingTests.typeText(textBot, "s including");
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
	}
	
	@Test
	public void delete_multiple_words(){
		String textBefore = "Paragraph 5 contains a long string of text that \ncovers multiples lines in the document";
		String brailleBefore = ",p>agraph #e 3ta9s a l;g /r+ ( text t \ncov}s multiples l9es 9 ! docu;t";
		
		String textAfter = "Paragraph 5 string of text that ";
		
		textBot.navigateTo(6, 27);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.pressKey(textBot, SWT.BS, 16);
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		for(int i = 0; i < 4; i++)
			textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n" + textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine()+ "\n" + brailleBot.getTextOnLine(7));
		
		for(int i = 0; i < 4; i++)
			textBot.pressShortcut(SWT.MOD1, 'y');
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
	}
	
	@Test
	public void delete_selection_multiple_words(){
		String textBefore = "Paragraph 5 contains a long string of text that \ncovers multiples lines in the document";
		String brailleBefore = ",p>agraph #e 3ta9s a l;g /r+ ( text t \ncov}s multiples l9es 9 ! docu;t";
		
		String textAfter = "Paragraph 5 string of text that ";
		
		textBot.navigateTo(6, 11);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		textBot.selectRange(6, 12, 16);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		assertEquals(textAfter, textBot.getTextOnCurrentLine());	
		
		textBot.pressShortcut(SWT.MOD1, 'z');	
		assertEquals(textBefore, textBot.getTextOnCurrentLine() + "\n" + textBot.getTextOnLine(7));
		assertEquals(brailleBefore, brailleBot.getTextOnCurrentLine()+ "\n" + brailleBot.getTextOnLine(7));
		
		textBot.pressShortcut(SWT.MOD1, 'y');	
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
	}
	
	@Test 
	public void paste_atEnd(){	
		String textBefore = "Paragraph 1";
		String textAfter = "Paragraph 1 contains a long string of text that ";
		String brailleAfter = ",p>agraph #a 3ta9s a l;g /r+ ( text t ";
		
		TextEditingTests.copy(textBot, 6, 11, 37);
		TextEditingTests.navigateTo(textBot, 12);
		TextEditingTests.paste(bot, textBot, 1, 11, 0);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		textBot.pressShortcut(SWT.MOD1, 'z');
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		TextEditingTests.forceUpdate(bot, textBot);
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		assertEquals(brailleAfter, brailleBot.getTextOnCurrentLine());
	}
	
	@Test 
	public void paste_atStart(){	
		String textBefore = "Paragraph 1";
		String textAfter = " contains a long string of text that Paragraph 1";
		String brailleAfter = "3ta9s a l;g /r+ ( text t ,p>agraph #a";
		
		TextEditingTests.copy(textBot, 6, 11, 37);
		TextEditingTests.navigateTo(textBot, 2);
		TextEditingTests.pressKey(textBot, SWT.ARROW_LEFT, 1);
		TextEditingTests.paste(bot, textBot, 1, 0, 0);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		textBot.pressShortcut(SWT.MOD1, 'z');
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		
		TextEditingTests.forceUpdate(bot, textBot);
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		assertEquals(brailleAfter, brailleBot.getTextOnCurrentLine());
	}
	
	@Test 
	public void paste_Middle(){	
		String textBefore = "Paragraph 1";
		String textAfter = "Paragraph contains a long string of text that  1";
		String brailleAfter = ",p>agraph 3ta9s a l;g /r+ ( text t #a";
		
		TextEditingTests.copy(textBot, 6, 11, 37);
		textBot.navigateTo(1, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 9);
		textBot.selectRange(1, 9, 1);
		TextEditingTests.paste(bot, textBot, 1, 9, 0);
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		textBot.pressShortcut(SWT.MOD1, 'z');
		assertEquals(textBefore, textBot.getTextOnCurrentLine());
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		brailleBot.setFocus();
		
		assertEquals(textAfter, textBot.getTextOnCurrentLine());
		assertEquals(brailleAfter, brailleBot.getTextOnCurrentLine());
	}
	
	//cuts a wrod then types a new word checks that a blank occurs when hitting undo, then old word when hitting undo again
	@Test
	public void blank_between_edits(){
		String text = "Paragraph 1 simple test";
		String braille = ",p>agraph #a simple te/";
		
		String text_after_cut = "Paragraph 1 test";
		String text_after_edit = "Paragraph 1 basic test";
		
		textBot.navigateTo(2, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_LEFT, 1);
		TextEditingTests.typeText(textBot, " simple test");
		TextEditingTests.forceUpdate(bot, textBot);
		
		assertEquals(text, textBot.getTextOnCurrentLine());
		assertEquals(braille, brailleBot.getTextOnCurrentLine());
		
		TextEditingTests.cut(bot, textBot, 1, 12, 7);
		assertEquals(text_after_cut, textBot.getTextOnCurrentLine());
		
		TextEditingTests.typeText(textBot, "basic ");
		assertEquals(text_after_edit, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		assertEquals(text_after_cut, textBot.getTextOnCurrentLine());
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		assertEquals(text, textBot.getTextOnCurrentLine());
	}
}
