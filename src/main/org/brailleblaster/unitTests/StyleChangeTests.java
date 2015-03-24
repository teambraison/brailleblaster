package org.brailleblaster.unitTests;

import static org.junit.Assert.*;

import org.brailleblaster.Main;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StyleChangeTests {
	private final String BLANK_LINE = "";
	private final String BOXLINE_TEXT = "----------------------------------------";
	private final String TOP_BOXLINE = "7777777777777777777777777777777777777777";
	private final String BOTTOM_BOXLINE = "gggggggggggggggggggggggggggggggggggggggg";
	
	protected static SWTBot bot;
	protected SWTBotStyledText textBot, brailleBot;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new Thread(new Runnable() {	  
			@Override
	        public void run() {
				String [] args = {"-debug", "StyleChangeTest.xml"};
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
	}
	
	@Test
	public void heading1FirstElement(){
		String expectedText = "First paragraph on page 1";
		String expectedBraille = ",f/ p>_.agraph on page #a";
		textBot.setFocus();
		textBot.navigateTo(1, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		
		String result = textBot.getTextOnLine(0);
		String brailleResult = brailleBot.getTextOnLine(0);
		assertEquals(BLANK_LINE, result);
		assertEquals(BLANK_LINE, brailleResult);
		
		result = textBot.getTextOnLine(1);
		brailleResult = brailleBot.getTextOnLine(1);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, brailleResult);
		
		result = textBot.getTextOnLine(2);
		brailleResult = brailleBot.getTextOnLine(2);
		assertEquals(BLANK_LINE, result);
		assertEquals(BLANK_LINE, brailleResult);
	}
	
	@Test
	public void heading1InlineElement(){
		String expectedText = "First paragraph on page 1";
		String expectedBraille = ",f/ p>_.agraph on page #a";
		textBot.setFocus();
		textBot.navigateTo(1,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		
		String result = textBot.getTextOnLine(0);
		String brailleResult = brailleBot.getTextOnLine(0);
		assertEquals(BLANK_LINE, result);
		assertEquals(BLANK_LINE, brailleResult);
		
		result = textBot.getTextOnLine(1);
		brailleResult = brailleBot.getTextOnLine(1);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, brailleResult);
		
		result = textBot.getTextOnLine(2);
		brailleResult = brailleBot.getTextOnLine(2);
		assertEquals(BLANK_LINE, result);
		assertEquals(BLANK_LINE, brailleResult);
	}
	
	@Test
	public void heading1Middle(){
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille = ",second ,p>agraph on ,page #b";
		textBot.setFocus();
		textBot.navigateTo(4,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		
		String result = textBot.getTextOnLine(4);
		String brailleResult = brailleBot.getTextOnLine(4);
		assertEquals(BLANK_LINE, result);
		assertEquals(BLANK_LINE, brailleResult);
		
		result = textBot.getTextOnLine(5);
		brailleResult = brailleBot.getTextOnLine(5);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, brailleResult);
		
		result = textBot.getTextOnLine(6);
		brailleResult = brailleBot.getTextOnLine(6);
		assertEquals(BLANK_LINE, result);
		assertEquals(BLANK_LINE, brailleResult);
	}

	@Test
	public void heading1End(){
		String expectedText = "Final Paragraph";
		String expectedBraille = ",f9al ,p>agraph";
		textBot.setFocus();
		textBot.navigateTo(14,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "centered Heading");
		
		String result = textBot.getTextOnLine(14);
		String brailleResult = brailleBot.getTextOnLine(14);
		assertEquals(BLANK_LINE, result);
		assertEquals(BLANK_LINE, brailleResult);
		
		result = textBot.getTextOnLine(15);
		brailleResult = brailleBot.getTextOnLine(15);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, brailleResult);
		
		result = textBot.getTextOnLine(16);
		brailleResult = brailleBot.getTextOnLine(16);
		assertEquals(BLANK_LINE, result);
		assertEquals(BLANK_LINE, brailleResult);
	}
	
	@Test
	public void FirstElement_boxline(){
		String expectedText = "First paragraph on page 1";
		String expectedBraille = ",f/ p>_.agraph on page #a";
		textBot.setFocus();
		textBot.navigateTo(1, 0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		String result = textBot.getTextOnLine(1);
		String brailleResult = brailleBot.getTextOnLine(1);
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(TOP_BOXLINE, brailleResult);
		
		result = textBot.getTextOnLine(2);
		brailleResult = brailleBot.getTextOnLine(2);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, brailleResult);
		
		result = textBot.getTextOnLine(3);
		brailleResult = brailleBot.getTextOnLine(3);
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(BOTTOM_BOXLINE, brailleResult);
	}
	
	@Test
	public void InlineElement_boxline(){
		String expectedText = "First paragraph on page 1";
		String expectedBraille = ",f/ p>_.agraph on page #a";
		textBot.setFocus();
		textBot.navigateTo(1,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 11);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		String result = textBot.getTextOnLine(1);
		String brailleResult = brailleBot.getTextOnLine(1);
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(TOP_BOXLINE, brailleResult);
		
		result = textBot.getTextOnLine(2);
		brailleResult = brailleBot.getTextOnLine(2);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, brailleResult);
		
		result = textBot.getTextOnLine(3);
		brailleResult = brailleBot.getTextOnLine(3);
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(BOTTOM_BOXLINE, brailleResult);
	}
	
	@Test
	public void Middle_boxline(){
		String expectedText = "Second Paragraph on Page 2";
		String expectedBraille = ",second ,p>agraph on ,page #b";
		textBot.setFocus();
		textBot.navigateTo(4,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		String result = textBot.getTextOnLine(4);
		String brailleResult = brailleBot.getTextOnLine(4);
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(TOP_BOXLINE, brailleResult);
		
		result = textBot.getTextOnLine(5);
		brailleResult = brailleBot.getTextOnLine(5);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, brailleResult);
		
		result = textBot.getTextOnLine(6);
		brailleResult = brailleBot.getTextOnLine(6);
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(BOTTOM_BOXLINE, brailleResult);
	}
	
	@Test
	public void End_boxline(){
		String expectedText = "Final Paragraph";
		String expectedBraille = ",f9al ,p>agraph";
		textBot.setFocus();
		textBot.navigateTo(14,0);
		TextEditingTests.pressKey(textBot, SWT.ARROW_RIGHT, 1);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "boxline");
		
		String result = textBot.getTextOnLine(14);
		String brailleResult = brailleBot.getTextOnLine(14);
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(TOP_BOXLINE, brailleResult);
		
		result = textBot.getTextOnLine(15);
		brailleResult = brailleBot.getTextOnLine(15);
		assertEquals(expectedText, result);
		assertEquals(expectedBraille, brailleResult);
		
		result = textBot.getTextOnLine(16);
		brailleResult = brailleBot.getTextOnLine(16);
		assertEquals(BOXLINE_TEXT, result);
		assertEquals(BOTTOM_BOXLINE, brailleResult);
	}
	
	@Test
	//adds and then removes a headline from multiple elements
	public void multi_HeadLine(){
		String line1 = "Page 2 paragraph";
		String line2 = "Second Paragraph on Page 2";
		String line3 = "3rd paragraph on Page 2";
		String brailleLine1 = ",page #b p>agraph";
		String brailleLine2 = ",second ,p>agraph on ,page #b";
		String brailleline3 = "#crd p>agraph on ,page #b";
		String blank = "";
		
		textBot.selectRange(3, 0, 67);
		TextEditingTests.openStylePanel(bot);
		TextEditingTests.applyStyle(bot, "cell 5 heading");
		
		assertEquals(blank, textBot.getTextOnLine(3));
		assertEquals(blank, brailleBot.getTextOnLine(3));
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		assertEquals(line1, textBot.getTextOnLine(4));
		assertEquals(brailleLine1, brailleBot.getTextOnLine(4));
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		assertEquals(blank, textBot.getTextOnLine(5));
		assertEquals(blank, brailleBot.getTextOnLine(5));
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		assertEquals(line2, textBot.getTextOnLine(6));
		assertEquals(brailleLine2, brailleBot.getTextOnLine(6));
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		assertEquals(blank, textBot.getTextOnLine(7));
		assertEquals(blank, brailleBot.getTextOnLine(7));
		
		TextEditingTests.pressKey(textBot, SWT.ARROW_DOWN, 1);
		assertEquals(line3, textBot.getTextOnLine(8));
		assertEquals(brailleline3, brailleBot.getTextOnLine(8));
		
		TextEditingTests.navigateTo(textBot, 0);
		textBot.navigateTo(3, 0);
		textBot.selectRange(3, 0, 70);	
		TextEditingTests.removeStyle(bot, "cell 5 heading");
		
		assertEquals(line1, textBot.getTextOnLine(3));
		assertEquals(brailleLine1, brailleBot.getTextOnLine(3));
		
		assertEquals(line2, textBot.getTextOnLine(4));
		assertEquals(brailleLine2, brailleBot.getTextOnLine(4));
		
		assertEquals(line3, textBot.getTextOnLine(5));
		assertEquals(brailleline3, brailleBot.getTextOnLine(5));
	}
}
