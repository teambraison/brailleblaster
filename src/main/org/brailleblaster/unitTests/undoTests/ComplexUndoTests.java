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

public class ComplexUndoTests {
	private String line1 = "Paragraph 1";
	private String line2 = "Paragraph 2";
	private String line3 = "Paragraph 3";
	private String line4 = "Paragraph 4";
	private String line5 = "Paragraph 5";
	private String line6 = "Paragraph 6";
	
	private String brailleline = ",p>agraph #a";
	private String brailleline2 = ",p>agraph #b";
	private String brailleline3 = ",p>agraph #c";
	private String brailleline4 = ",p>agraph #d";
	private String brailleline5 = ",p>agraph #e";
	private String brailleline6 = ",p>agraph #f";
	
	private final static String XMLTREE = "XML";
	//private final static String BLANK_LINE = "";
	//private final String BOXLINE_TEXT = "----------------------------------------";
	//private final String TOP_BOXLINE = "7777777777777777777777777777777777777777";
	//private final String BOTTOM_BOXLINE = "gggggggggggggggggggggggggggggggggggggggg";
	
	protected static SWTBot bot;
	protected SWTBotStyledText textBot, brailleBot;
	protected SWTBotTree treeBot;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		new Thread(new Runnable() {	  
			@Override
	        public void run() {
				String [] args = {"-debug", "complexUndoTests.xml"};
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
	public void undo_redo_multiple_elements() {
		String line1After = "1Paragraph 1";
		String line2After = "P1aragraph 2";
		String line3After = "Pa1ragraph 3";
		String line4After = "Par1agraph 4";
		String line5After = "Para1graph 5";
		String line6After = "Parag1raph 6";
		
		String braillelineAfter = "#a;,p>agraph #a";
		String brailleline2After = ";,p#a;aragraph #b";
		String brailleline3After = ",pa#a;ragraph #c";
		String brailleline4After = ",p>#a;agraph #d";
		String brailleline5After = ",p>a#a;graph #e";
		String brailleline6After = ",p>ag#a;raph #f";
		
		textBot.navigateTo(1, 0);
		for(int i = 0; i < 6; i++){
			TextEditingTests.typeText(textBot, "1");
			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		}
		TextEditingTests.forceUpdate(bot, textBot);
		
		for(int i = 0; i < 12; i++)
			textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(line1, textBot.getTextOnLine(1));
		assertEquals(line2, textBot.getTextOnLine(2));
		assertEquals(line3, textBot.getTextOnLine(3));
		assertEquals(line4, textBot.getTextOnLine(4));
		assertEquals(line5, textBot.getTextOnLine(5));
		assertEquals(line6, textBot.getTextOnLine(6));
		
		assertEquals(brailleline, brailleBot.getTextOnLine(1));
		assertEquals(brailleline2, brailleBot.getTextOnLine(2));
		assertEquals(brailleline3, brailleBot.getTextOnLine(3));
		assertEquals(brailleline4, brailleBot.getTextOnLine(4));
		assertEquals(brailleline5, brailleBot.getTextOnLine(5));
		assertEquals(brailleline6, brailleBot.getTextOnLine(6));
		
		for(int i = 0; i < 12; i++)
			textBot.pressShortcut(SWT.MOD1, 'y');
		
		assertEquals(line1After, textBot.getTextOnLine(1));
		assertEquals(line2After, textBot.getTextOnLine(2));
		assertEquals(line3After, textBot.getTextOnLine(3));
		assertEquals(line4After, textBot.getTextOnLine(4));
		assertEquals(line5After, textBot.getTextOnLine(5));
		assertEquals(line6After, textBot.getTextOnLine(6));
		
		assertEquals(braillelineAfter, brailleBot.getTextOnLine(1));
		assertEquals(brailleline2After, brailleBot.getTextOnLine(2));
		assertEquals(brailleline3After, brailleBot.getTextOnLine(3));
		assertEquals(brailleline4After, brailleBot.getTextOnLine(4));
		assertEquals(brailleline5After, brailleBot.getTextOnLine(5));
		assertEquals(brailleline6After, brailleBot.getTextOnLine(6));
	}
	
	@Test
	public void multipleEdits(){
		String line1After = "Paragraph 11";
		String braillelineAfter = ",p>agraph #aa";
		
		String line1After2 = "Paragraph 11 is here";
		String brailleAfter2 = ",p>agraph #aa is \"h";
		
		String line1After3 = "Paragraph 11 s here";
		String brailleAfter3 = ",p>agraph #aa ;s \"h";
		
		textBot.navigateTo(1, 10);
		TextEditingTests.typeText(textBot, "1");
		TextEditingTests.forceUpdate(bot, textBot);
		
		assertEquals(line1After, textBot.getTextOnLine(1));
		assertEquals(braillelineAfter, brailleBot.getTextOnLine(1));
		
		for(int i = 0; i < 2; i++)
			textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(line1, textBot.getTextOnLine(1));
		assertEquals(brailleline, brailleBot.getTextOnLine(1));
		
		for(int i = 0; i < 2; i++)
			textBot.pressShortcut(SWT.MOD1, 'y');
		
		assertEquals(line1After, textBot.getTextOnLine(1));
		assertEquals(braillelineAfter, brailleBot.getTextOnLine(1));
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.typeText(textBot, " is here");
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		
		assertEquals(line1After2, textBot.getTextOnLine(1));
		assertEquals(brailleAfter2, brailleBot.getTextOnLine(1));
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 13);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		
		assertEquals(line1After3, textBot.getTextOnLine(1));
		assertEquals(brailleAfter3, brailleBot.getTextOnLine(1));
	}
	
	@Test
	public void delete_followedby_edit(){
		String line1After = "Paragrap 1";
		String braillelineAfter = ",p>agrap #a";
		
		String line1After2 = "1Paragrap 1";
		String braillelineAfter2 = "#a;,p>agrap #a";
		
		textBot.navigateTo(1, 8);
		TextEditingTests.pressKey(textBot, SWT.DEL, 1);
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		
		assertEquals(line1After, textBot.getTextOnLine(1));
		assertEquals(braillelineAfter, brailleBot.getTextOnLine(1));
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		TextEditingTests.typeText(textBot, "1");
		TextEditingTests.forceUpdate(bot, textBot);
		
		assertEquals(line1After2, textBot.getTextOnLine(1));
		assertEquals(braillelineAfter2, brailleBot.getTextOnLine(1));
	}
	
	@Test
	public void multiline_insert_undo_redo(){
		String line1After = "Paragraph 1 followed by more text which covers multiple lines";
		String braillelineAfter = ",p>agraph #a foll{$ 0m text : cov}s multiple l9es";
		
		textBot.navigateTo(1, 10);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.typeText(textBot, " followed by more text which covers multiple lines");
		TextEditingTests.forceUpdate(bot, textBot);
		
		assertEquals(line1After, textBot.getTextOnLine(1) + textBot.getTextOnLine(2));
		assertEquals(braillelineAfter, brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2));
		
		textBot.pressShortcut(SWT.MOD1, 'z');
		assertEquals(line1After, textBot.getTextOnLine(1));
		assertEquals(brailleline, brailleBot.getTextOnLine(1));
		
		textBot.pressShortcut(SWT.MOD1, 'y');
		assertEquals(line1After, textBot.getTextOnLine(1) + textBot.getTextOnLine(2));
		assertEquals(braillelineAfter, brailleBot.getTextOnLine(1) + brailleBot.getTextOnLine(2));
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		assertEquals(line2, textBot.getTextOnLine(3));
	}
	
	@Test
	public void sequenceChange(){
		String line1After = "1Paragraph 1";
		String line2After = "P1aragraph 2";
		String line3After = "Pa1ragraph 3";
		String line4After = "Par1agraph 4";
		String line5After = "Para1graph 5";
		String line6After = "Parag1raph 6";
		
		String line5After_secondEdit = "1Paragraph 5";
		String line6After_secondEdit = "P1aragraph 6";
		
		String braillelineAfter = "#a;,p>agraph #a";
		String brailleline2After = ";,p#a;aragraph #b";
		String brailleline3After = ",pa#a;ragraph #c";
		String brailleline4After = ",p>#a;agraph #d";
		String brailleline5After = ",p>a#a;graph #e";
		String brailleline6After = ",p>ag#a;raph #f";
		
		String brailleLine5After_secondEdit = "#a;,p>agraph #e";
		String brailleLine6After_secondEdit = ";,p#a;aragraph #f";
		
		TextEditingTests.navigateTo(textBot, 1);
		for(int i = 0; i < 6; i++){
			TextEditingTests.typeText(textBot, "1");
			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		}
		TextEditingTests.forceUpdate(bot, textBot);
		
		assertEquals(line1After, textBot.getTextOnLine(1));
		assertEquals(line2After, textBot.getTextOnLine(2));
		assertEquals(line3After, textBot.getTextOnLine(3));
		assertEquals(line4After, textBot.getTextOnLine(4));
		assertEquals(line5After, textBot.getTextOnLine(5));
		assertEquals(line6After, textBot.getTextOnLine(6));
		
		assertEquals(braillelineAfter, brailleBot.getTextOnLine(1));
		assertEquals(brailleline2After, brailleBot.getTextOnLine(2));
		assertEquals(brailleline3After, brailleBot.getTextOnLine(3));
		assertEquals(brailleline4After, brailleBot.getTextOnLine(4));
		assertEquals(brailleline5After, brailleBot.getTextOnLine(5));
		assertEquals(brailleline6After, brailleBot.getTextOnLine(6));
		
		for(int i = 0; i < 4; i++)
			textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(line5, textBot.getTextOnLine(5));
		assertEquals(line6, textBot.getTextOnLine(6));
		assertEquals(brailleline5, brailleBot.getTextOnLine(5));
		assertEquals(brailleline6, brailleBot.getTextOnLine(6));
		
		for(int i = 0; i < 2; i++){
			TextEditingTests.typeText(textBot, "1");
			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		}
		TextEditingTests.forceUpdate(bot, textBot);
		
		assertEquals(line5After_secondEdit, textBot.getTextOnLine(5));
		assertEquals(line6After_secondEdit, textBot.getTextOnLine(6));
		
		assertEquals(brailleLine5After_secondEdit, brailleBot.getTextOnLine(5));
		assertEquals(brailleLine6After_secondEdit, brailleBot.getTextOnLine(6));
		
		for(int i = 0; i < 12; i++)
			textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(line1, textBot.getTextOnLine(1));
		assertEquals(line2, textBot.getTextOnLine(2));
		assertEquals(line3, textBot.getTextOnLine(3));
		assertEquals(line4, textBot.getTextOnLine(4));
		assertEquals(line5, textBot.getTextOnLine(5));
		assertEquals(line6, textBot.getTextOnLine(6));
		
		assertEquals(brailleline, brailleBot.getTextOnLine(1));
		assertEquals(brailleline2, brailleBot.getTextOnLine(2));
		assertEquals(brailleline3, brailleBot.getTextOnLine(3));
		assertEquals(brailleline4, brailleBot.getTextOnLine(4));
		assertEquals(brailleline5, brailleBot.getTextOnLine(5));
		assertEquals(brailleline6, brailleBot.getTextOnLine(6));
		
		for(int i = 0; i < 12; i++)
			textBot.pressShortcut(SWT.MOD1, 'y');
		
		assertEquals(line1After, textBot.getTextOnLine(1));
		assertEquals(line2After, textBot.getTextOnLine(2));
		assertEquals(line3After, textBot.getTextOnLine(3));
		assertEquals(line4After, textBot.getTextOnLine(4));
		assertEquals(line5After_secondEdit, textBot.getTextOnLine(5));
		assertEquals(line6After_secondEdit, textBot.getTextOnLine(6));
		
		assertEquals(braillelineAfter, brailleBot.getTextOnLine(1));
		assertEquals(brailleline2After, brailleBot.getTextOnLine(2));
		assertEquals(brailleline3After, brailleBot.getTextOnLine(3));
		assertEquals(brailleline4After, brailleBot.getTextOnLine(4));
		assertEquals(brailleLine5After_secondEdit, brailleBot.getTextOnLine(5));
		assertEquals(brailleLine6After_secondEdit, brailleBot.getTextOnLine(6));
	}
	
	@Test
	public void sequenceChange2(){
		String line1After = "hey Paragraph 1";
		String brailleLineAfter = "hey ,p>agraph #a";
		
		String line1After_secondEdit = "1Paragraph 1";
		String line2After_secondEdit = "P1aragraph 2";
		String braillelineAfter_secondEdit = "#a;,p>agraph #a";
		String brailleline2After_secondEdit = ";,p#a;aragraph #b";
		
		TextEditingTests.navigateTo(textBot, 1);
		TextEditingTests.typeText(textBot, "hey ");
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		
		assertEquals(line1After, textBot.getTextOnLine(1));
		assertEquals(brailleLineAfter, brailleBot.getTextOnLine(1));
		
		for(int i = 0; i < 3; i++)
			textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(line1, textBot.getTextOnLine(1));
		assertEquals(brailleline, brailleBot.getTextOnLine(1));
		
		textBot.navigateTo(1, 0);
		for(int i = 0; i < 2; i++){
			TextEditingTests.typeText(textBot, "1");
			TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		}
		
		assertEquals(line1After_secondEdit, textBot.getTextOnLine(1));
		assertEquals(line2After_secondEdit, textBot.getTextOnLine(2));
		
		assertEquals(braillelineAfter_secondEdit, brailleBot.getTextOnLine(1));
		assertEquals(brailleline2After_secondEdit, brailleBot.getTextOnLine(2));
		
		for(int i = 0; i < 4; i++)
			textBot.pressShortcut(SWT.MOD1, 'z');
		
		assertEquals(line1, textBot.getTextOnLine(1));
		assertEquals(line2, textBot.getTextOnLine(2));
		assertEquals(brailleline, brailleBot.getTextOnLine(1));
		assertEquals(brailleline2, brailleBot.getTextOnLine(2));
		
		for(int i = 0; i < 4; i++)
			textBot.pressShortcut(SWT.MOD1, 'y');
		
		assertEquals(line1After_secondEdit, textBot.getTextOnLine(1));
		assertEquals(line2After_secondEdit, textBot.getTextOnLine(2));
		
		assertEquals(braillelineAfter_secondEdit, brailleBot.getTextOnLine(1));
		assertEquals(brailleline2After_secondEdit, brailleBot.getTextOnLine(2));
	}
}
